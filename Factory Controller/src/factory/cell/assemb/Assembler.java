/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openGrab the template in the editor.
 */
package factory.cell.assemb;

import factory.cell.*;
import factory.conveyor.*;
import java.util.ArrayDeque;
import main.*;

/**
 *
 * @author Alex
 */
public class Assembler extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Mover t4;
    private final Rotator t5;
    private final Mover t6;
    
    public final Table table1;
    public final Table table2;
    public final Table table3;
    
    public final Gantry gantry;
    
    private final ArrayDeque<Transfer> pendingTransfers;

    public Assembler(String id) {
        super(id);

        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t3 = new Mover(id + "T3", 2);
        t4 = new Mover(id + "T4", 1);
        t5 = new Rotator(id + "T6");
        t6 = new Mover(id + "T5", 1);
        conveyorList = new Conveyor[]{t1, t2, t3, t4, t5, t6};

        t1.connections = new Conveyor[]{null, t2};
        t2.connections = new Conveyor[]{t1, null, null, t3};
        t3.connections = new Conveyor[]{t2, t4};
        t4.connections = new Conveyor[]{t3, t5};
        t5.connections = new Conveyor[]{t6, t4, null, null};
        t6.connections = new Conveyor[]{null, t5};

        table1 = new Table(Main.config.getBaseInput(id + "M") + 0);
        table2 = new Table(Main.config.getBaseInput(id + "M") + 1);
        table3 = new Table(Main.config.getBaseInput(id + "M") + 2);

        gantry = new Gantry(id);
        
        pendingTransfers = new ArrayDeque<>();
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t5;
            case 3: return t6;
            default:
                throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void update() {
        super.update();
        gantry.update();
        if(gantry.isInitializing())
            return;
        if(pendingTransfers.isEmpty())
            return;
        if(pendingTransfers.peek().update())    //if transaction completed
        {
            System.out.println("pop");
            pendingTransfers.pop();
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        t2.connections[2] = right.getCornerConveyor(0);
        t5.connections[2] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connections[0] = left.getCornerConveyor(1);
        t6.connections[0] = left.getCornerConveyor(2);
    }
    
    @Override
    public Rotator getEntryConveyor() {
        return t2;
    }
    
    @Override
    public Rotator getExitConveyor() {
        return t5;
    }

    //TODO: fill this
    @Override
    public long getEntryDelayTimeEstimate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //TODO: add Transfer field to Block
    public class Transfer
    {
        private final int fromX, fromY, toX, toY;
        private int whereToX;
        private int whereToY;
        private TRANSFER_STATE status = TRANSFER_STATE.MOVING_TO_ORIGIN;
        public Transfer(int fromX, int fromY, int toX, int toY) {
            if ((fromX > 1 || fromX < 0) || (toX > 1 || toX < 0) || (fromY > 4 || fromY < 0) || (toY > 4 || toY < 0))
            {
                throw new IndexOutOfBoundsException("Tried to transfer a block with Gantry from/to invalid coordinates");
            }
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            whereToX = 2*fromX +1;
            whereToY = 2*fromY +1;
        }
        
        private long grabTimer;
        
        /**
         * Updates the FSM (to transfer the block)
         * @return 
         */
        boolean update()
        {
            boolean Xready;
            boolean Yready;
            switch(status)
            {
                case MOVING_TO_ORIGIN:
                    Xready = (gantry.isAtX == whereToX);
                    if(Xready)
                    {
                        gantry.XMotor.turnOff();
                        gantry.XState = Gantry.MOVEMENT_STATE.IDLE;
                    }
                    else
                    {
                        if(gantry.isAtX < whereToX)
                        {
                            gantry.XMotor.turnOnPlus();
                            gantry.XState = Gantry.MOVEMENT_STATE.MOVINGPLUS;
                        }
                        else
                        {
                            gantry.XMotor.turnOnMinus();
                            gantry.XState = Gantry.MOVEMENT_STATE.MOVINGMINUS;
                        }
                    }
                    
                    Yready = (gantry.isAtY == whereToY);
                    if(Yready)
                    {
                        gantry.YMotor.turnOff();
                        gantry.YState = Gantry.MOVEMENT_STATE.IDLE;
                    }
                    else
                    {
                        if(gantry.isAtY < whereToY)
                        {
                            gantry.YMotor.turnOnPlus();
                            gantry.YState = Gantry.MOVEMENT_STATE.MOVINGPLUS;
                        }
                        else
                        {
                            gantry.YMotor.turnOnMinus();
                            gantry.YState = Gantry.MOVEMENT_STATE.MOVINGMINUS;
                        }
                    }
                    
                    if(Xready && Yready)    //arrived at origin
                        status=TRANSFER_STATE.GO_DOWN_ORIGIN;
                break;
                case GO_DOWN_ORIGIN:
                    gantry.ZMotor.turnOnMinus();
                    
                    if(gantry.downZ.on())   //fully down
                    {
                        gantry.ZMotor.turnOff();
                        grabTimer = System.currentTimeMillis();
                        status=TRANSFER_STATE.GRAB_ORIGIN;
                    }
                break;
                case GRAB_ORIGIN:   //espera 1 segundo, como indicado na descrição da fábrica
                    gantry.closeGrab();
                    if(System.currentTimeMillis() - grabTimer >= 1200)
                        status=TRANSFER_STATE.GO_UP_ORIGIN;
                break;
                case GO_UP_ORIGIN:
                    gantry.ZMotor.turnOnPlus();
                    
                    if(gantry.upZ.on())   //fully up
                    {
                        gantry.ZMotor.turnOff();
                        whereToX = 2*toX +1;
                        whereToY = 2*toY +1;
                        status=TRANSFER_STATE.MOVING_TO_DESTINATION;
                    }
                break;
                case MOVING_TO_DESTINATION:
                    Xready = (gantry.isAtX == whereToX);
                    if(Xready)
                    {
                        System.out.print("Xready ");
                        gantry.XState = Gantry.MOVEMENT_STATE.IDLE;
                        gantry.XMotor.turnOff();
                    }
                    else
                    {
                        if(gantry.isAtX < whereToX)
                        {
                            System.out.println("XPlus ");
                            gantry.XMotor.turnOnPlus();
                            gantry.XState = Gantry.MOVEMENT_STATE.MOVINGPLUS;
                        }
                        else
                        {
                            System.out.println("XMinus ");
                            gantry.XMotor.turnOnMinus();
                            gantry.XState = Gantry.MOVEMENT_STATE.MOVINGMINUS;
                        }
                    }
                    
                    Yready = (gantry.isAtY == whereToY);
                    if(Yready)
                    {
                        System.out.println("Yready");
                        gantry.YState = Gantry.MOVEMENT_STATE.IDLE;
                        gantry.YMotor.turnOff();
                    }
                    else
                    {
                        if(gantry.isAtY < whereToY)
                        {
                            System.out.println("YPlus");
                            gantry.YMotor.turnOnPlus();
                            gantry.YState = Gantry.MOVEMENT_STATE.MOVINGPLUS;
                        }
                        else
                        {
                            System.out.println("YMinus");
                            gantry.YMotor.turnOnMinus();
                            gantry.YState = Gantry.MOVEMENT_STATE.MOVINGMINUS;
                        }
                    }
                    
                    if(Xready && Yready)    //arrived at origin
                    {
                        gantry.XMotor.turnOff();
                        gantry.YMotor.turnOff();
                        grabTimer = System.currentTimeMillis();
                        status=TRANSFER_STATE.DROP_DESTINATION;
                    }
                break;
                case DROP_DESTINATION:
                    gantry.openGrab();
                    if(System.currentTimeMillis() - grabTimer >= 1000)
                        status=TRANSFER_STATE.FINISHED;
                break;
                case FINISHED:
                    gantry.XMotor.turnOff();
                    gantry.YMotor.turnOff();
                    return true;
                default:
                    throw new IllegalStateException("Illegal state reached at the Transfer FSM");
            }
            return false;
        }
    }
    
    public Transfer tranferBlock(int fromX, int fromY, int toX, int toY) {
        Transfer transfer = new Transfer(fromX,fromY,toX,toY);
        pendingTransfers.add( transfer );
        return transfer;
    }
    
    private enum TRANSFER_STATE {
        MOVING_TO_ORIGIN, GO_DOWN_ORIGIN, GRAB_ORIGIN, GO_UP_ORIGIN, MOVING_TO_DESTINATION, DROP_DESTINATION, FINISHED; //considering that there is no need for the gantry to go down in the destination
    }
    
}
