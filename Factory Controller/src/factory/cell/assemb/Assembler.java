package factory.cell.assemb;

import control.*;
import control.order.*;
import factory.*;
import factory.cell.*;
import factory.conveyor.*;
import factory.other.*;
import java.util.*;
import java.util.logging.*;
import main.*;
import main.Optimizer.*;

public final class Assembler extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Mover t4;
    private final Rotator t5;
    private final Mover t6;

    private final List<Conveyor> queueConveyors;

    public final Table table1;
    public final Table table2;
    public final Table table3;

    public final Table[] tables;
    
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
        conveyors = new Conveyor[]{t1, t2, t3, t4, t5, t6};
        queueConveyors = Arrays.asList(new Conveyor[] {t3,t4});

        t1.connections = new Conveyor[]{null, t2};
        t2.connections = new Conveyor[]{t1, null, null, t3};
        t3.connections = new Conveyor[]{t2, t4};
        t4.connections = new Conveyor[]{t3, t5};
        t5.connections = new Conveyor[]{t6, t4, null, null};
        t6.connections = new Conveyor[]{null, t5};

        table1 = new Table(Main.config.getBaseInput(id + "M") + 0);
        table2 = new Table(Main.config.getBaseInput(id + "M") + 1);
        table3 = new Table(Main.config.getBaseInput(id + "M") + 2);
        tables = new Table[]{table1, table2, table3};

        gantry = new Gantry(id);

        pendingTransfers = new ArrayList<>();
        
        final AssemblyOrder testOrder = new AssemblyOrder(1,2,Block.Type.P1,Block.Type.P2);
        
        Block block1 = new Block(Block.Type.P1, testOrder);
        t1.placeBlock(block1, 0);
        blocksInside.add(block1);
        
        Block block2 = new Block(Block.Type.P2, testOrder);
        t4.placeBlock(block2, 0);
        blocksInside.add(block2);
        
        Thread test_thread1 = new Thread(){
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(1000*50);
                }
                catch(Throwable ignored)
                {
                    return;
                }
            }
        };
        
        /*Transfer _transfer = transferBlock(t1, table2);
        Block _test_block = new Block(Block.Type.P1);
        _test_block.order = new AssemblyOrder(0, 1, Block.Type.P1, Block.Type.P1);
        t3.placeBlock(_test_block, 0);
        System.out.println("block order type: "+ t3.getOneBlock().order.getClass().getName());
        Thread _t = new Thread(()->{
            try {
                while(_transfer.status == TRANSFER_STATE.WAITING_FOR_START)
                    Thread.sleep(1);
                Thread.sleep(2000);
                
                if(!_transfer.changeOrigin(t2))
                    System.err.println("Error changing origin!");
                else
                    System.err.println("Changed origin to t2");
                
                Thread.sleep(3*1000);
                
                Block anotherBlock = new Block(Block.Type.P1);
                anotherBlock.order = _test_block.order;
                t2.placeBlock(anotherBlock, 0);
                System.err.println("Placed block at t2!");
                
                while(_transfer.status != TRANSFER_STATE.MOVING_TO_DESTINATION)
                    Thread.sleep(1);
                Thread.sleep(3000);
                
                if(!_transfer.changeDestination(t3))
                    System.err.println("Error changing destination!");
                else
                    System.err.println("Changed destination to t3");
                
                Thread.sleep(5*1000);
                
                t3.removeBlock(0);
                System.err.println("Removed block from t3!");
            }
            catch(Throwable ignored) {}
        });
        _t.setDaemon(true);
        _t.start();*/
        
        /*Block b1 = new Block(Block.Type.P1), b2 = new Block(Block.Type.P2);
        
        b1.order = b2.order = new AssemblyOrder(0, 1, Block.Type.P1, Block.Type.P1);
        
        t1.placeBlock(b1, 0);
        t2.placeBlock(b2, 0);
        
        transferBlock(t1, table1);
        transferBlock(t2, table1);
        transferBlock(table1, t2);*/
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
    
    private Table existsIncompleteOrderInTables(Order o)
    {
        for(Table t : tables)
            if(t.getOneBlock() != null && t.getOneBlock().order == o && !t.getOneBlock().isStacked())
                return t;
        return null;
    }
    
    private Table getAssembledTable()
    {
        for(Table t : tables)
            if(t.getOneBlock() != null && t.getOneBlock().isStacked())
                return t;
        return null;
    }
    
    private Table getAnEmptyTable()
    {
        for(Table t : tables)
            if(!t.hasBlock())
                return t;
        return null;
    }
    
    /**
     * Tells the Gantry what movements to make by adding Transfer objects to the pendingTransfers list
     */
    private void gantryController()
    {
        if(!pendingTransfers.isEmpty() || blocksInside.isEmpty())
            return;
        
        System.err.println("Conveyors.length: " + conveyors.length);
        
        for(int i = 0; i< conveyors.length ; i++)
        {
            Conveyor c = conveyors[i];
            if(!c.hasBlock())
            {
                //System.out.println("t" + (i+1) + " contains no blocks. (" + c.getOneBlock() + ")");
                continue;
            }
            String type2;
            if(c.getOneBlock().otherAssemblyBlock != null)
                type2 = c.getOneBlock().otherAssemblyBlock.type.name();
            else
                type2 = null;
            System.out.println("t" + (i+1) + "-> typeBottom=" + c.getOneBlock().type.name() + " typeTop=" + type2);
        }
        
        Table couldHaveGoneToExit = null;  //contains a Table that could have been the source of a transfer to the exit conveyor but was skipped because it wasn't the most efficient move at the beginning of the algorithm. if null, no Tables could have gone to the exit.
        Table t = getAssembledTable();
        if(t != null)
        {
            if((getExitConveyor().hasBlock() && !getExitConveyor().isSending()) || getExitConveyor().isReceiving()) //go do another transfer to give time for t2 to get ready
            {
                getEntryConveyor().setSendingFrozen(true);  //freeze the only conveyor that sends blocks to t2 so it eventually stays empty
                couldHaveGoneToExit = t;
            }
            else
            {
                transferBlock(t, getExitConveyor());
                return;
            }
        }
        
        for(int i=3 ; i>=0 ; i--)
        {
            Block block = conveyors[i].getOneBlock();
            if(block == null)
                continue;
            Table tableTo = existsIncompleteOrderInTables(block.order);
            if(tableTo == null) //no table contains a block waiting to be assembled with this order
            {
                tableTo = getAnEmptyTable();
                if(tableTo == null || !block.canBeBottomBlock()) //there are no empty tables for this block to go to
                    continue;
                transferBlock(conveyors[i], tableTo);
                return;
            }
            else if(block.type == ((AssemblyOrder)block.order).topType)    //there is a table that has a block waiting with this order
            {
                transferBlock(conveyors[i], tableTo);
                return;
            }
        }
        
        if(couldHaveGoneToExit == null)
            throw new Error("Deadlock in Assembler");
        transferBlock(couldHaveGoneToExit, getExitConveyor());
    }
    
    @Override
    public void update() {
        super.update();
        if(!gantry.update())
            return;
        
        gantryController();
        
        if (!pendingTransfers.isEmpty()) {
            if (pendingTransfers.get(0).update()) { // if transaction completed
                pendingTransfers.remove(0);
                System.gc();
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
    public Conveyor getTopTransferConveyor() {
        return t1;
    }
    
    @Override
    public Conveyor getBottomTransferConveyor() {
        return t5;
    }

    @Override
    public Conveyor getEntryConveyor() {
        return t1;
    }

    @Override
    public Conveyor getExitConveyor() {
        return t2;
    }

    private static final int MAX_BLOCKS = 9; //3 conveyors that can contain blocks and 2*3 blocks in the tables = 9 max blocks
    
    private static final double AVERAGE_ASSEMBLE_DURATION = Main.config.getD("timing.assembly.duration");
    
    private static final double TIME_TO_GET_TO_ASSEMBLER = Main.config.getD("timing.assembly.timeToArriveAssembler");
    
    private static final int SPACE_OCCUPIED_PER_ASSEMBLY = 3; //tunning parameter
    
    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate) {
        List<OrderPossibility> ret = new ArrayList<>();
        orders.stream().filter( o -> (o instanceof AssemblyOrder) ).map((o) -> ((AssemblyOrder) o)).forEach((AssemblyOrder o) -> {
            final int space = MAX_BLOCKS - blocksInside.size() - blocksIncoming.size();
            
            if(space <= 0)
                return;
            
            int priority = 1;
            
            int possibleExecutionCount = Math.max(space, 0)/SPACE_OCCUPIED_PER_ASSEMBLY;
            
            boolean entersImmediately = true;
            
            double totalDuration = AVERAGE_ASSEMBLE_DURATION + TIME_TO_GET_TO_ASSEMBLER;
            
            Object info = null;
            
            ret.add(new OrderPossibility(this, o, possibleExecutionCount, info, totalDuration, entersImmediately, priority));
        });
        return ret;
    }
    
    @Override
    protected boolean processBlockIn(Block block) {
        /*boolean t3Occupied = !t3.isIdle() || t3.hasBlock();
        boolean t4Occupied = !t4.isIdle() || t4.hasBlock();*/
        //int nBlocksInConveyorsInside = queueConveyors.stream().mapToInt((Conveyor _t) -> {return _t.hasBlock() ? 1 : 0;}).sum();
        int nBlocksInConveyorsInside = queueConveyors.stream().mapToInt((Conveyor _t) -> {return _t.hasBlock() ? 1 : 0;}).sum();
        if(nBlocksInConveyorsInside < queueConveyors.size())
        {
            block.path.push(t2,t3,t4);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected Cell processBlockOut(Block block) {
        System.err.println("processBlockOut " + block.type.name());
        block.path = new Path().push(t2);
        return Main.factory.loadUnloadCell;
    }

    public final class Coordinates {

        public final int x;
        public final int y;
        public final int z;

        public Coordinates(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Coordinates(int x, int y) {
            this(x, y, 0);
        }

        public Coordinates(Container bc) {
            this.z = 0;
            if (bc instanceof Table) {
                this.x = 0; //all tables are at x=0
                for (int i = 0; i < tables.length; i++) {
                    if (tables[i] == bc) {
                        this.y = i + 1;
                        return;
                    }
                }
                throw new Error("Table passed is not contained in this Assembler.");
            } else if (bc instanceof Conveyor) {
                int totalLengthSoFar = 0;
                for (int i = 0; i < conveyors.length; i++) {
                    if (conveyors[i] == bc) //found a match
                    {
                        this.x = (i == 0 || i == conveyors.length - 1) ? 0 : 1;   //only the 1st and last conveyors have x=0. the others have x=1
                        if (i == 0) {
                            this.y = 0;
                        } else if (i == conveyors.length - 1) {
                            this.y = conveyors.length - 2;
                        } else {
                            this.y = totalLengthSoFar;
                        }
                        return;
                    }
                    if (i != 0 && i != conveyors.length - 1) //if one of the vertical conveyors
                    {
                        totalLengthSoFar += conveyors[i].getLength();   //necessary because not all convayors have size 1
                    }
                }
                throw new Error("Conveyor passed is not contained in this Assembler.");
            } else {
                throw new Error("Coordinates constructor received an object that is neither a table nor a conveyor");
            }
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        public Container getBlockContainer()
        {
            return getBlockContainerFromCoordinates(this.x, this.y);
        }
    }
    
    public Container getBlockContainerFromCoordinates(int x, int y) {
        if (x == 0) {
            if (y == 0) {
                return t1;
            }
            if (y == 4) {
                return t6;
            }
            return tables[y - 1];
        } else {
            return conveyors[y <= 1 ? y + 1 : y];
        }
    }
    
    //TODO: add Transfer field to Block
    public final class Transfer {

        private Coordinates from, to;
        private int whereToX;   //where the gantry is supposed to be going atm in the X direction
        private int whereToY;   //where the gantry is supposed to be going atm in the X direction
        private TRANSFER_STATE status = TRANSFER_STATE.WAITING_FOR_START;

        public Transfer(Coordinates from, Coordinates to)
        {
            if(from == null || to == null)
                throw new NullPointerException();
            if ((from.x > 1 || from.x < 0) || (to.x > 1 || to.x < 0) || (from.y > 4 || from.y < 0) || (to.y > 4 || to.y < 0)) {
                throw new IndexOutOfBoundsException("Tried to transfer a block with Gantry from/to invalid coordinates");
            }
            this.from = from;
            this.to = to;
            this.whereToX = 2 * from.x + 1;
            this.whereToY = 2 * from.y + 1;
        }
        
        public Transfer(int fromX, int fromY, int toX, int toY) {
            this(new Coordinates(fromX,fromY), new Coordinates(toX,toY));
        }

        boolean isCompleted()
        {
            return status==TRANSFER_STATE.FINISHED;
        }
        
        boolean isActive()
        {
            return status != TRANSFER_STATE.WAITING_FOR_START;
        }

        private Conveyor getPreviousConveyor(Conveyor _c)
        {
            if(_c == null)
                throw new NullPointerException();
            for(int i=0;i<conveyors.length;i++)
            {
                if(conveyors[i]==_c)
                {
                    if(i > 0)
                        return conveyors[i-1];
                    //if i==0, it's not possible to get the previous conveyor
                    Logger.getLogger(Assembler.class.getSimpleName()).log(Level.WARNING, "Tried (and failed) to get the previous conveyor");
                    return null;
                }
            }
            return null;
        }
        
        public boolean changeOrigin(Coordinates from)
        {
            if(from == null)
            {
                return false;
            }
            if(from.x <0 | from.x > 1 || from.y < 0 || from.y > 4)
            {
                return false;
            }
            if(status.id > TRANSFER_STATE.MOVING_TO_ORIGIN.id)
            {
                if(status == TRANSFER_STATE.WAITING_FOR_ORIGIN_CONVEYOR_TO_BE_READY)    //the gantry is waiting for the previous origin conveyor to be ready so there is still time to change the origin
                    status = TRANSFER_STATE.MOVING_TO_ORIGIN;
                else    //can't change the origin after the gantry has started (or has finished) going down at the previously set origin
                    return false;
            }
            if (this.from.getBlockContainer() instanceof Conveyor) {    //unfreeze the previous origin
                Conveyor _c = (Conveyor) this.from.getBlockContainer();
                _c.setSendingFrozen(false);  //freeze the origin conveyor so it doesn't send the block that the gantry is supposed to go fetch to the next conveyor
            }
            this.from = from;
            if (from.getBlockContainer() instanceof Conveyor) { //freeze the new origin
                Conveyor _c = (Conveyor) from.getBlockContainer();
                _c.setSendingFrozen(true);  //freeze the origin conveyor so it doesn't send the block that the gantry is supposed to go fetch to the next conveyor
            }
            this.whereToX = 2*from.x+1;
            this.whereToY = 2*from.y+1;
            return true;
        }
        
        public boolean changeOrigin(Container bc)
        {
            if(bc == null)
                return false;
            return changeOrigin(new Coordinates(bc));
        }
        
        public boolean changeDestination(Coordinates to)
        {
            if(to == null)
                return false;
            if(to.x <0 | to.x > 1 || to.y < 0 || to.y > 4)
                return false;
            if(status.id > TRANSFER_STATE.MOVING_TO_DESTINATION.id)    //can't change the destination after the gantry has started (or has finished) going down at the previously set destination
            {
                if(status == TRANSFER_STATE.WAITING_FOR_EMPTY_SPACE_TO_DROP)    //the gantry is waiting for having space to drop so there is still time to change destination
                    status = TRANSFER_STATE.MOVING_TO_DESTINATION;
                else    //can't change destination because the transfer is almost over
                    return false;
            }
            if (this.to.getBlockContainer() instanceof Conveyor) {  //unfreeze the previous conveyor of the old origin
                Conveyor _c = getPreviousConveyor((Conveyor) this.to.getBlockContainer());
                if (_c == null) {
                    throw new Error("Tried to transfer block to a conveyor that either is the Entry-Conveyor of this Assembler or is not in this Cell at all");
                }
                _c.setSendingFrozen(false);
            }
            this.to = to;
            if (to.getBlockContainer() instanceof Conveyor) {   //freeze the previous conveyor of the new origin
                Conveyor _c = getPreviousConveyor((Conveyor) to.getBlockContainer());
                if (_c == null) {
                    throw new Error("Tried to transfer block to a conveyor that either is the Entry-Conveyor of this Assembler or is not in this Cell at all");
                }
                _c.setSendingFrozen(true);
            }
            this.whereToX = 2*to.x+1;
            this.whereToY = 2*to.y+1;
            return true;
        }
        
        public boolean changeDestination(Container bc)
        {
            if(bc == null)
                return false;
            return changeDestination(new Coordinates(bc));
        }
        
        private long grabTimer;

        private void unfreezeAssembler()
        {
            for(Conveyor _c : conveyors)
            {
                _c.setSendingFrozen(false);
            }
        }
        
        //private TRANSFER_STATE prevState  = TRANSFER_STATE.WAITING_FOR_START;
        
        /**
         * Updates the FSM (to transfer the block)
         *
         * @return
         */
        boolean update() {
            /*if(prevState != status)
            {
                System.out.println(status.name());
                prevState = status;
            }*/
            boolean Xready;
            boolean Yready;
            switch (status) {
                case WAITING_FOR_START:
                    //either the origin is a table or it's a conveyor and is ready
                    status = TRANSFER_STATE.MOVING_TO_ORIGIN;
                    if(from.getBlockContainer() instanceof Conveyor)
                    {
                        Conveyor _c = (Conveyor)from.getBlockContainer();
                        _c.setSendingFrozen(true);  //freeze the origin conveyor so it doesn't send the block that the gantry is supposed to go fetch to the next conveyor
                    }
                    if(to.getBlockContainer() instanceof Conveyor)
                    {
                        Conveyor _c = getPreviousConveyor( (Conveyor)to.getBlockContainer() );
                        if(_c == null)
                            throw new Error("Tried to transfer block to a conveyor that either is the Entry-Conveyor of this Assembler or is not in this Cell at all");
                        _c.setSendingFrozen(true);
                    }
                    break;
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
                        if(from.getBlockContainer() instanceof Conveyor)
                        {
                            status = TRANSFER_STATE.WAITING_FOR_ORIGIN_CONVEYOR_TO_BE_READY;
                        }
                        else
                            status = TRANSFER_STATE.GO_DOWN_ORIGIN;
                    }
                    break;
                case WAITING_FOR_ORIGIN_CONVEYOR_TO_BE_READY:
                    if (from.getBlockContainer() instanceof Conveyor)
                    {
                        Conveyor _c = (Conveyor) from.getBlockContainer();
                        if (!_c.isIdle() || !_c.hasBlock()) //if the origin conveyor is not yet ready, wait
                            return false;
                        else
                            status = TRANSFER_STATE.GO_DOWN_ORIGIN;
                    }
                    else
                        throw new IllegalStateException(status.name() + " should only be reached by Conveyor-originated transfers");
                    break;
                case GO_DOWN_ORIGIN:
                    gantry.openGrab();

                    if (gantry.downZ.on()) //fully down
                    {
                        gantry.ZMotor.turnOff();
                        grabTimer = Main.time();
                        if(gantry.isOpen())
                            status = TRANSFER_STATE.GRAB_ORIGIN;
                    }
                    else
                    {
                        gantry.ZMotor.turnOnMinus();
                    }
                    break;
                case GRAB_ORIGIN: //espera 1 segundo, como indicado na descrição da fábrica
                    //System.err.println("GRAB " + gantry.presenceSensor.on());
                    gantry.closeGrab();
                    if (Main.time() - grabTimer >= 1500) {
                        status = TRANSFER_STATE.GO_UP_ORIGIN;
                    }
                    break;
                case GO_UP_ORIGIN:
                    gantry.ZMotor.turnOnPlus();
                    gantry.closeGrab();

                    if (gantry.upZ.on()) //fully up
                    {
                        gantry.ZMotor.turnOff();
                        whereToX = 2 * to.x + 1;
                        whereToY = 2 * to.y + 1;
                        if(from.getBlockContainer() instanceof Conveyor)    //release the origin conveyor
                        {
                            Conveyor _c = (Conveyor)from.getBlockContainer();
                            _c.setSendingFrozen(false);
                        }
                        
                        if(gantry.hasBlock())
                            throw new Error("Tried to get block from a Block Container but the Gantry was already occupied!");
                        
                        Block _block;
                        Container bc = from.getBlockContainer();
                        _block = bc.getOneBlock();
                        bc.removeAllBlocks();
                        gantry.setBlock(_block);
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
                        grabTimer = Main.time();
                        if(to.getBlockContainer() instanceof Conveyor)
                            status = TRANSFER_STATE.WAITING_FOR_EMPTY_SPACE_TO_DROP;
                        else
                            status = TRANSFER_STATE.GO_DOWN_DESTINATION;
                    }
                    break;
                case WAITING_FOR_EMPTY_SPACE_TO_DROP:
                    if(!(to.getBlockContainer() instanceof Conveyor))
                        throw new IllegalStateException(status.name() + " should only be reached by Conveyor destinations");
                    Conveyor _toConveyor = (Conveyor)to.getBlockContainer();
                    if(_toConveyor.isIdle() && !_toConveyor.hasBlock())
                    {
                        status = TRANSFER_STATE.GO_DOWN_DESTINATION;
                    }
                    break;
                case GO_DOWN_DESTINATION:
                    gantry.closeGrab();

                    if (gantry.downZ.on()) //fully down
                    {
                        gantry.ZMotor.turnOff();
                        grabTimer = Main.time();
                        status = TRANSFER_STATE.DROP_DESTINATION;
                    }
                    else
                    {
                        gantry.ZMotor.turnOnMinus();
                    }
                    break;
                case DROP_DESTINATION:
                    gantry.openGrab();
                    if (Main.time() - grabTimer >= 1500) {
                        status = TRANSFER_STATE.GO_UP_DESTINATION;
                    }
                    break;
                case GO_UP_DESTINATION:
                    gantry.openGrab();
                    if (gantry.upZ.on())    //arrived at top
                    {
                        status = TRANSFER_STATE.FINISHED;
                        Container bc = to.getBlockContainer();
                        
                        //get the block which is inside the gantry
                        Block gantryBlock = gantry.getBlock();
                        if (gantryBlock == null)
                            throw new Error("Gantry finished transfering but didn't contain a block!");
                        
                        //place the gantry block at the destination block container
                        if(bc instanceof Table)
                        {
                            Table _table = (Table) bc;
                            if(_table.getOneBlock() != null)
                            {
                                if(!_table.getOneBlock().placeOnTop(gantryBlock))   //Place the gantry block on top of the block that is already in the table
                                    throw new Error("Failed at placing a block on top of another one");
                            }
                            else
                            {
                                _table.placeBlock(gantryBlock, 0);
                            }
                        }
                        else if(bc instanceof Conveyor)
                        {
                            Conveyor _conveyor = (Conveyor) bc;
                            if(_conveyor.hasBlock())
                                throw new Error("The gantry has just tried to transfer a block to a conveyor that already had a block in it");
                            _conveyor.placeBlock(gantryBlock, 0);
                        }
                        gantry.setBlock(null);  //remove the block from the gantry
                        gantry.ZMotor.turnOff();
                    }
                    else    //still going up
                    {
                        gantry.ZMotor.turnOnPlus();
                    }
                    break;
                case CANCELED:
                    unfreezeAssembler();
                    return true;
                case FINISHED:
                    gantry.XMotor.turnOff();
                    gantry.YMotor.turnOff();
                    gantry.ZMotor.turnOff();
                    if(to.getBlockContainer() instanceof Conveyor)
                    {
                        Conveyor _c = getPreviousConveyor( (Conveyor)to.getBlockContainer() );
                        if(_c == null)
                            throw new Error("Tried to transfer block to a conveyor that either is the Entry-Conveyor of this Assembler or is not in this Cell at all");
                        _c.setSendingFrozen(false);
                    }
                    unfreezeAssembler();
                    return true;
                default:
                    throw new IllegalStateException("Illegal state reached at the Transfer FSM");
            }
            return false;
        }
        
        /**
         * cancels this transfer
         * @return <p>Whether the transfer was successfully canceled.</p> <p>If the transfer was already happening, this will fail and return false</p> <p>If the transfer has already finished, returns true</p>
         */
        boolean cancel()
        {
            if(isCompleted())
                return true;
            if(isActive())
                return false;
            pendingTransfers.remove(this);
            status = TRANSFER_STATE.CANCELED;
            System.gc();
            return true;
        }
    }
    
    public Transfer transferBlock(Container from, Container to)
    {
        System.out.println("Registered a transfer");
        return transferBlock(new Coordinates(from), new Coordinates(to));
    }
    
    public Transfer transferBlock(Coordinates from, Coordinates to)
    {
        Transfer transfer = new Transfer(from, to);
        pendingTransfers.add(transfer);
        return transfer;
    }
    
    public Transfer transferBlock(int fromX, int fromY, int toX, int toY) {
        Transfer transfer = new Transfer(fromX, fromY, toX, toY);
        pendingTransfers.add(transfer);
        return transfer;
    }

    private enum TRANSFER_STATE {
        
        WAITING_FOR_START(0), MOVING_TO_ORIGIN(1), WAITING_FOR_ORIGIN_CONVEYOR_TO_BE_READY(2), GO_DOWN_ORIGIN(3), GRAB_ORIGIN(4), GO_UP_ORIGIN(5), MOVING_TO_DESTINATION(6), WAITING_FOR_EMPTY_SPACE_TO_DROP(7), GO_DOWN_DESTINATION(8), DROP_DESTINATION(9), GO_UP_DESTINATION(10), FINISHED(11), CANCELED(12); //considering that there is no need for the gantry to go down in the destination
        
        public final int id;

        private TRANSFER_STATE(int id) {
            this.id = id;
        }
        
    }

}
