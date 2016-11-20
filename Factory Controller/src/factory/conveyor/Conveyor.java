package factory.conveyor;

import control.*;
import factory.*;
import factory.other.*;
import java.util.*;
import main.*;

public abstract class Conveyor extends BlockContainer {

    /**
     * Contains a conveyor that is to transfer a block to this conveyor when
     * both are ready
     */
    Conveyor transferPartner;
    private Sensor receivingFinishedSensor;

    private final Block[] blocks;
    private State conveyorState;
    private final Motor transferMotor;
    private final Sensor[] presenceSensors;
    private final int length;
    private final Double[] queueWeights;
    
    public final String id;
    private boolean sendingFrozen;
    //public Integer highestPriorityConnection = null;
    public Conveyor[] connections;

    public enum State {
        Standby, PrepareToReceive, Receiving,
        PrepareToSend, ReadyToSend, Sending;
    }

    /**
     * Constructor of the abstract superclass
     *
     * @param id Name of the conveyor
     * @param length Size of the conveyor
     * @param connectionCount Represents how many connections with other
     * conveyors this conveyor has
     */
    public Conveyor(String id, int length, int connectionCount) {
        this.id = id;

        this.length = length;
        blocks = new Block[length];
        presenceSensors = new Sensor[length];
        connections = new Conveyor[connectionCount];
        queueWeights = new Double[connectionCount];

        transferMotor = new Motor(Main.config.getBaseOutput(id) + 0);

        for (int i = 0; i < length; i++) {
            presenceSensors[i] = new Sensor(Main.config.getBaseInput(id) + i);
        }

        for (int i = 0; i < connectionCount; i++) {
            queueWeights[i] = 0.0;
        }

        conveyorState = State.Standby;
    }

    public void update() {
        updateQueueWeights();

        switch (conveyorState) {
            case Standby:
                if (hasBlock() && !sendingFrozen) {
                    Block block = getOneBlock();

                    if (block.path.hasNext()) {
                        if (isBlockTransferPossible()) {
                            transferPartner = block.path.getNext();
                            conveyorState = State.PrepareToSend;
                            blockTransferPrepare();
                        }
                    }
                }
                else if (transferPartner != null) {
                    blockTransferPrepare();
                    conveyorState = State.PrepareToReceive;
                }

                break;
            case PrepareToReceive:
                if (isBlockTransferReady()) {
                    receivingFinishedSensor = presenceSensors[0];
                    blockTransferStart();
                    transferPartner.blockTransferStart();
                }
                break;
            case Receiving:
                if (receivingFinishedSensor.on()) {
                    blockTransferStop(transferPartner.blockTransferStop(null));
                }
                break;
            case PrepareToSend:
                if (isBlockTransferReady()) {
                    conveyorState = State.ReadyToSend;
                }
                break;
            case ReadyToSend:
                transferPartner.blockTransferRegister(this);
                break;
            case Sending:
                break;
        }
    }

    private void blockTransferRegister(Conveyor c) {
        if (!isSending() && !isReceiving()) {
            if (transferPartner != null && transferPartner != c) {
                System.out.println("blockTransferRegister chooseNextConveyor id = " + id + " transferPartner = " + transferPartner.id + " c = " + c.id);
                transferPartner = chooseNextConveyor(transferPartner, c);
            }
            else {
                transferPartner = c;
            }
        }
    }

    private void blockTransferStart() {
        transferMotor.turnOn(transferMotorDirection(transferPartner, isSending()));
        if (isSending()) {
            conveyorState = State.Sending;
        }
        else if (isReceiving()) {
            conveyorState = State.Receiving;
        }
    }

    /**
     *
     * @param newBlock If receiving, expects the new block object to be passed.
     * If sending, parameter is null
     * @return If sending, returns the block sent
     */
    private Block blockTransferStop(Block newBlock) {
        transferMotor.turnOff();

        Block b = null;
        if (isSending()) {
            while (b == null) {
                b = shiftOneBlock(transferMotorDirection(transferPartner, isSending()), null);
            }

            Main.stats.inc(id, Statistics.Type.BlocksSent, b.type);
        }
        else if (isReceiving()) {
            shiftOneBlock(transferMotorDirection(transferPartner, isSending()), newBlock);
            newBlock.path.advance();
            blockTransferFinished();

            Main.stats.inc(id, Statistics.Type.BlocksReceived, newBlock.type);
        }

        transferPartner = null;
        conveyorState = State.Standby;

        return b;
    }

    private Block shiftOneBlock(boolean shiftLeft, Block insert) {
        Block ret;

        if (shiftLeft) {
            ret = blocks[0];

            System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
            // Equivalent to: TODO: verify
            //for (int i = blocks.length - 1; i > 0; i--) {
            //    blocks[i - 1] = blocks[i];
            //}

            blocks[blocks.length - 1] = insert;
        }
        else {
            ret = blocks[blocks.length - 1];

            System.arraycopy(blocks, 0, blocks, 1, blocks.length - 1);
            // Equivalent to: TODO: verify
            //for (int i = 0; i < blocks.length - 1; i++) {
            //    blocks[i + 1] = blocks[i];
            //}

            blocks[0] = insert;
        }

        return ret;
    }

    /**
     * Get block at position <i>position</i> in this conveyor
     *
     * @param position
     * @return Block, or null if no block is present
     */
    private Block getBlock(int position) {
        if (position >= length) {
            throw new IndexOutOfBoundsException("Invalid block position " + position);
        }
        return blocks[position];
    }

    private int indexForConveyor(Conveyor c) {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] == c) {
                return i;
            }
        }

        return -1;
    }

    private Conveyor chooseNextConveyor(Conveyor c1, Conveyor c2) {
        /*if (highestPriorityConnection != null) {
            if (indexForConveyor(c1) == highestPriorityConnection) {
                return c1;
            }

            if (indexForConveyor(c2) == highestPriorityConnection) {
                return c2;
            }
        }*/

        if (getQueueWeight(c1) > getQueueWeight(c2)) {
            return c1;
        }
        else {
            return c2;
        }
    }

    public double getQueueWeight(Conveyor from) {
        return queueWeights[indexForConveyor(from)];
    }

    private void setQueueWeight(Conveyor from, double weight) {
        queueWeights[indexForConveyor(from)] = weight;
    }

    private void updateQueueWeights() {
        for (Conveyor c : connections) {
            if (c == null) {
                continue;
            }

            double priority = 0;

            if (hasBlock()) {
                if (getOneBlock().path != null) {
                    if (getOneBlock().path.getNext() == c) {
                        List<Double> wList = new ArrayList(Arrays.asList(queueWeights));
                        wList.remove(indexForConveyor(c));
                        priority = queueWeightsFunction(wList);
                    }
                }
            }

            c.setQueueWeight(this, priority);
        }
    }

    private double queueWeightsFunction(List<Double> list) {
        return list.stream().reduce(0.0, Double::sum) + 1;
    }

    /**
     * Checks if there are blocks in the conveyor
     *
     * @return <i>true</i> if there is at least one block. <i>false</i>
     * otherwise
     */
    public boolean hasBlock() {
        for (Block b : blocks) {
            if (b != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns one block on the conveyor or null if no blocks are present
     *
     * @return
     */
    public Block getOneBlock() {
        for (Block b : blocks) {
            if (b != null) {
                return b;
            }
        }

        return null;
    }

    /**
     * Places a block object on the conveyor
     *
     * @param b the block to place
     * @param position the index (from 0 to length-1) of where to place
     * @return <i>true</i> if there was no other block on that position and the
     * block was placed. <i>false</i> otherwise
     */
    public boolean placeBlock(Block b, int position) {
        if (position >= length) {
            throw new IndexOutOfBoundsException("Inavlid block position " + position);
        }
        if (blocks[position] == null) {
            blocks[position] = b;
            return true;
        }

        return false;
    }

    /**
     * Removes a block object from the conveyor
     *
     * @param position the position where to remove the block
     * @return the block that was removed, or null if no block was there
     */
    public Block removeBlock(int position) {
        Block ret = blocks[position];
        blocks[position] = null;
        return ret;
    }

    /**
     * Loops for all the conveyors connected to this conveyor and returns true
     * if the given conveyor is on that list
     *
     * @param c the conveyor to search for
     * @return true if <code>c</code> is on <code>connections</code>
     */
    public boolean isConnectedToConveyor(Conveyor c) {
        for (Conveyor c2 : connections) {
            if (c == c2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return if presence sensor is on at the specified position
     *
     * @param position The position
     * @return Sensor is on
     */
    public boolean isPresenceSensorOn(int position) {
        return presenceSensors[position].on();
    }

    /**
     * Indicates if the conveyor is not doing anything and is ok with an
     * external class changing the variable `blocks`. Should be overridden by
     * subclasses for subclasses that process blocks, for example: return
     * super.isIdle() && machiningStopped
     *
     * @return is idle
     */
    public boolean isIdle() {
        return conveyorState == State.Standby;
    }

    public boolean isSending() {
        return conveyorState == State.PrepareToSend
               || conveyorState == State.ReadyToSend
               || conveyorState == State.Sending;
    }

    public boolean isReceiving() {
        return conveyorState == State.PrepareToReceive
               || conveyorState == State.Receiving;
    }

    public double transferTimeEstimate(Conveyor from, Conveyor to) {
        if (!isConnectedToConveyor(from) || !isConnectedToConveyor(to)) {
            throw new Error("XXX"); // TODO: error message
        }

        // TODO: not finished
        return length * (double) Main.config.getI("conveyor.sizeUnit") / Main.config.getD("timing.conveyor.speed") * 1000; // *1000 to convert to milliseconds
    }

    public void setSendingFrozen(boolean sendingFrozen)
    {
        this.sendingFrozen = sendingFrozen;
    }
    
    protected abstract boolean transferMotorDirection(Conveyor partner, boolean sending);

    protected abstract void blockTransferFinished();

    protected abstract boolean isBlockTransferPossible();

    protected abstract void blockTransferPrepare();

    protected abstract boolean isBlockTransferReady();
}
