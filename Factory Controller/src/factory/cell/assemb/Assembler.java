package factory.cell.assemb;

import factory.cell.*;
import factory.conveyor.*;
import factory.other.*;
import java.util.*;
import main.*;

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

    private final List<Transfer> pendingTransfers;  //add at the end. get from the front

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

        pendingTransfers = new ArrayList<>();
        
        transferBlock(0, 0, 1, 3);
        transferBlock(0, 2, 1, 4);
        transferBlock(1, 3, 0, 2);
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
        if(!gantry.update())
            return;

        /*// DEMO
        if (t1.isPresenceSensorOn(0) && pendingTransfers.isEmpty()) {
            transferBlock(0, 0, 1, 3);
        }
        if (t2.isPresenceSensorOn(0) && pendingTransfers.isEmpty()) {
            transferBlock(0, 2, 1, 4);
        }
        if (t3.isPresenceSensorOn(0) && pendingTransfers.isEmpty()) {
            transferBlock(1, 3, 0, 2);
        }*/

        if (!pendingTransfers.isEmpty()) {
            if (pendingTransfers.get(0).update()) { // if transaction completed
                System.out.println("pop");
                pendingTransfers.remove(0);
            }
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
    public Rotator getTopTransferConveyor() {
        return t2;
    }

    @Override
    public Rotator getBottomTransferConveyor() {
        return t5;
    }

    //TODO: add Transfer field to Block
    public class Transfer {

        private final int fromX, fromY, toX, toY;
        private int whereToX;   //where the gantry is supposed to be going atm in the X direction
        private int whereToY;   //where the gantry is supposed to be going atm in the X direction
        private TRANSFER_STATE status = TRANSFER_STATE.MOVING_TO_ORIGIN;

        public Transfer(int fromX, int fromY, int toX, int toY) {
            if ((fromX > 1 || fromX < 0) || (toX > 1 || toX < 0) || (fromY > 4 || fromY < 0) || (toY > 4 || toY < 0)) {
                throw new IndexOutOfBoundsException("Tried to transfer a block with Gantry from/to invalid coordinates");
            }
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.whereToX = 2 * fromX + 1;
            this.whereToY = 2 * fromY + 1;
        }

        boolean isCompleted()
        {
            return status==TRANSFER_STATE.FINISHED;
        }
        
        boolean isActive()
        {
            return status != TRANSFER_STATE.WAITING;
        }
        
        private long grabTimer;

        /**
         * Updates the FSM (to transfer the block)
         *
         * @return
         */
        boolean update() {
            boolean Xready;
            boolean Yready;
            switch (status) {
                case WAITING:
                    status = TRANSFER_STATE.MOVING_TO_ORIGIN;
                    //intentional fall-through
                case MOVING_TO_ORIGIN:
                    gantry.openGrab();
                    Xready = (gantry.isAtX == whereToX);
                    if (Xready) {
                        gantry.XMotor.turnOff();
                    }
                    else if (gantry.isAtX < whereToX) {
                        gantry.XMotor.turnOnPlus();
                    }
                    else {
                        gantry.XMotor.turnOnMinus();
                    }

                    Yready = (gantry.isAtY == whereToY);
                    if (Yready) {
                        gantry.YMotor.turnOff();
                    }
                    else if (gantry.isAtY < whereToY) {
                        gantry.YMotor.turnOnPlus();
                    }
                    else {
                        gantry.YMotor.turnOnMinus();
                    }

                    if (Xready && Yready) {    //arrived at origin
                        status = TRANSFER_STATE.GO_DOWN_ORIGIN;
                    }
                    break;
                case GO_DOWN_ORIGIN:
                    gantry.openGrab();
                    gantry.ZMotor.turnOnMinus();

                    if (gantry.downZ.on()) //fully down
                    {
                        gantry.ZMotor.turnOff();
                        grabTimer = System.currentTimeMillis();
                        if(gantry.presenceSensor.on())
                            status = TRANSFER_STATE.GRAB_ORIGIN;
                    }
                    break;
                case GRAB_ORIGIN: //espera 1 segundo, como indicado na descrição da fábrica
                    //System.err.println("GRAB " + gantry.presenceSensor.on());
                    gantry.closeGrab();
                    if (System.currentTimeMillis() - grabTimer >= 1200) {
                        status = TRANSFER_STATE.GO_UP_ORIGIN;
                    }
                    break;
                case GO_UP_ORIGIN:
                    gantry.ZMotor.turnOnPlus();
                    gantry.closeGrab();

                    if (gantry.upZ.on()) //fully up
                    {
                        gantry.ZMotor.turnOff();
                        whereToX = 2 * toX + 1;
                        whereToY = 2 * toY + 1;
                        status = TRANSFER_STATE.MOVING_TO_DESTINATION;
                    }
                    break;
                case MOVING_TO_DESTINATION:
                    gantry.closeGrab();
                    Xready = (gantry.isAtX == whereToX);
                    if (Xready) {
                        //System.out.print("Xready ");
                        gantry.XMotor.turnOff();
                    }
                    else if (gantry.isAtX < whereToX) {
                        //System.out.println("XPlus ");
                        gantry.XMotor.turnOnPlus();
                    }
                    else {
                        //System.out.println("XMinus ");
                        gantry.XMotor.turnOnMinus();
                    }

                    Yready = (gantry.isAtY == whereToY);
                    if (Yready) {
                        //System.out.println("Yready");
                        gantry.YMotor.turnOff();
                    }
                    else if (gantry.isAtY < whereToY) {
                        //System.out.println("YPlus");
                        gantry.YMotor.turnOnPlus();
                    }
                    else {
                        //System.out.println("YMinus");
                        gantry.YMotor.turnOnMinus();
                    }

                    if (Xready && Yready) //arrived at destination
                    {
                        gantry.XMotor.turnOff();
                        gantry.YMotor.turnOff();
                        grabTimer = System.currentTimeMillis();
                        status = TRANSFER_STATE.DROP_DESTINATION;
                    }
                    break;
                case DROP_DESTINATION:
                    gantry.openGrab();
                    if (System.currentTimeMillis() - grabTimer >= 1000) {
                        status = TRANSFER_STATE.FINISHED;
                    }
                    break;
                case FINISHED:
                    gantry.XMotor.turnOff();
                    gantry.YMotor.turnOff();
                    gantry.ZMotor.turnOff();
                    return true;
                default:
                    throw new IllegalStateException("Illegal state reached at the Transfer FSM");
            }
            return false;
        }
        
        /**
         * cancels this transfer
         */
        void cancel()
        {
            pendingTransfers.remove(this);
            if(!isCompleted())
                status = TRANSFER_STATE.CANCELED;
            System.gc();
        }
    }

    public Transfer transferBlock(int fromX, int fromY, int toX, int toY) {
        Transfer transfer = new Transfer(fromX, fromY, toX, toY);
        pendingTransfers.add(transfer);
        return transfer;
    }

    private enum TRANSFER_STATE {
        WAITING, MOVING_TO_ORIGIN, GO_DOWN_ORIGIN, GRAB_ORIGIN, GO_UP_ORIGIN, MOVING_TO_DESTINATION, DROP_DESTINATION, FINISHED, CANCELED; //considering that there is no need for the gantry to go down in the destination
    }

}
