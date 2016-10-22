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
     * Contains a conveyor that is to transfer a block to this conveyor when both are ready
     */
    public Conveyor transferPartner;
    public Block[] blocks;
    public State conveyorState;
    final Motor transferMotor;
    public final Sensor[] presenceSensors;
    public String id;
    public Conveyor[] connections;
    
    public enum State {
        Standby, PrepareToReceive, Receiving,
        PrepareToSend, ReadyToSend, Sending;
    }
    
    /**
     * Constructor of the abstract superclass
     * @param id Name of the conveyor
     * @param length Size of the conveyor
     * @param connectionCount Represents how many connections with other conveyors this conveyor has
     */
    public Conveyor(String id, int length, int connectionCount) {
        this.id = id;
        
        blocks = new Block[length];
        presenceSensors = new Sensor[length];
        connections = new Conveyor[connectionCount];
        
        transferMotor = new Motor(Main.config.getBaseOutput(id) + 0);
        for (int i = 0; i < length; i++) { presenceSensors[i] = new Sensor(Main.config.getBaseInput(id) + i); }
        
        conveyorState = State.Standby;
    }
    
    public void update()
    {           
        switch (conveyorState)
        {
            case Standby:
                if (hasBlock()) {
                    
                    Block block = null;
                    for (Block b : blocks) if (b != null) block = b;
                    
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
                Sensor s = presenceSensors[0]; // TODO
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
            case Sending: break;
        }
    }
    
    /**
     * Checks if there are blocks in the conveyor
     * @return <i>true</i> if there is at least one block. <i>false</i> otherwise
     */
    public boolean hasBlock()
    {
        for (Block b : blocks) {
            if (b != null) return true;
        }            
        return false;
    }
    public boolean isSending()
    {
        return conveyorState == State.PrepareToSend ||
               conveyorState == State.ReadyToSend ||
               conveyorState == State.Sending;
    }
    public boolean isReceiving()
    {
        return conveyorState == State.PrepareToReceive ||
               conveyorState == State.Receiving;
    }
    
    private void blockTransferRegister(Conveyor c)
    {
        if (!isSending() && !isReceiving()) {
            transferPartner = c;
        }
    }
    private void blockTransferStart()
    {
        transferMotor.turnOn(transferMotorDirection());
        if (isSending()) { conveyorState = State.Sending; }
        else if (isReceiving()) { conveyorState = State.Receiving; }
    }
    /**
     * 
     * @param newBlock If receiving, expects the new block object to be
     * passed. If sending, parameter is null
     * @return If sending, returns the block sent
     */
    private Block blockTransferStop(Block newBlock)
    {
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
    private Block shiftOneBlock(boolean shiftRight, Block insert)
    {
        Block ret = null;
        
        if (shiftRight) {
            ret = blocks[blocks.length - 1];
            
            for (int i = 0; i < blocks.length - 1; i++) {
                blocks[i + 1] = blocks[i];
            }
            
            blocks[0] = insert;
        }
        else {
            ret = blocks[0];
            
            for (int i = blocks.length - 1; i > 0; i--) {
                blocks[i - 1] = blocks[i];
            }
            
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
