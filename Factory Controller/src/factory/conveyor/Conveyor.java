/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import control.*;
import factory.*;
import main.*;

/**
 * @author Alex
 * @author Luis Paulo
 */
public abstract class Conveyor {

    /**
     * Contains a conveyor that is to transfer a block to this conveyor when
     * both are ready
     */
    Conveyor transferPartner;
    private final Block[] blocks;
    private State conveyorState;
    /*private*/ public final Motor transferMotor;
    private final Sensor[] presenceSensors;
    private final int length;

    public String id;
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

        transferMotor = new Motor(Main.config.getBaseOutput(id) + 0);

        for (int i = 0; i < length; i++) {
            presenceSensors[i] = new Sensor(Main.config.getBaseInput(id) + i);
        }

        conveyorState = State.Standby;
    }

    public void update() {
        switch (conveyorState) {
            case Standby:
                if (hasBlock()) {
                    // TODO deciding which block is transfered next should be
                    // better - right now only works for conveyors of length 1
                    Block block = null;
                    for (Block b : blocks) {
                        if (b != null) {
                            block = b;
                        }
                    }

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
                    blockTransferStart();
                    transferPartner.blockTransferStart();
                }
                break;
            case Receiving:
                Sensor s = presenceSensors[0]; // TODO only works for conveyors of lenght 1
                if (s.on()) {
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
     * Get block at position <i>position</i> in this conveyor
     *
     * @param position
     * @return Block, or null if no block is present
     */
    public Block getBlock(int position) {
        if (position >= length) {
            throw new IndexOutOfBoundsException("XXX"); // TODO exception text
        }
        return blocks[position];
    }
    
    public boolean getPresenceSensorState(int position) {
        return presenceSensors[position].on();
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
            throw new IndexOutOfBoundsException("XXX"); // TODO exception text
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

    public boolean isSending() {
        return conveyorState == State.PrepareToSend
               || conveyorState == State.ReadyToSend
               || conveyorState == State.Sending;
    }

    public boolean isReceiving() {
        return conveyorState == State.PrepareToReceive
               || conveyorState == State.Receiving;
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

    private void blockTransferRegister(Conveyor c) {
        if (!isSending() && !isReceiving()) {
            //if (transferPartner != null) {
            // TODO it is possible that this may be called twice in the same loop
            // for two different conveyors. Needs way of
            // identifying best transferpartner
            //}
            //else {
            transferPartner = c;
            //}
        }
    }

    private void blockTransferStart() {
        transferMotor.turnOn(transferMotorDirection());
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
                b = shiftOneBlock(!transferMotorDirection(), null);
            }
        }
        else if (isReceiving()) {
            shiftOneBlock(!transferMotorDirection(), newBlock);
            newBlock.path.advance();
            blockTransferFinished();
        }

        transferPartner = null;
        conveyorState = State.Standby;

        return b;
    }

    private Block shiftOneBlock(boolean shiftRight, Block insert) {
        Block ret;

        if (shiftRight) {
            ret = blocks[blocks.length - 1];

            System.arraycopy(blocks, 0, blocks, 1, blocks.length - 1);
            // Equivalent to: TODO verify
            //for (int i = 0; i < blocks.length - 1; i++) {
            //    blocks[i + 1] = blocks[i];
            //}

            blocks[0] = insert;
        }
        else {
            ret = blocks[0];

            System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
            // Equivalent to: TODO verify
            //for (int i = blocks.length - 1; i > 0; i--) {
            //    blocks[i - 1] = blocks[i];
            //}

            blocks[blocks.length - 1] = insert;
        }

        return ret;
    }

    public abstract boolean transferMotorDirection();

    public abstract void blockTransferFinished();

    public abstract boolean isBlockTransferPossible();

    public abstract void blockTransferPrepare();

    public abstract boolean isBlockTransferReady();
}
