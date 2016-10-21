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
    }
    
    public void update()
    {
        if (presenceSensors[0].on()) {
            transferMotor.turnOn(true);
        }
        
        // TODO: write state machine (Alex will do that)
        
        /*switch (conveyorState)
        {
            case Standby:
                if ( hasBlocks() )
                {
                    if (isBlockTransferPossible())
                    {
                        
                        blockTransferPrepare();
                        conveyorState = State.PrepareToSend;
                    }
                }
                else if (transferPartner != null)
                {
                    if (transferPartner.conveyorState == State.PrepareToSend) //can receive from transferPartner
                    {
                        blockTransferPrepare();
                        conveyorState = State.PrepareToReceive;
                    }
                }
            break;
            case PrepareToReceive:
                if (isBlockTransferReady())
                {
                    blockTransferStart();
                    transferPartner.blockTransferStart();
                }
            break;
            case Receiving:
                if (sensor false)
                {
                    Block newBlock = transferPartner.blockTransferStop();
                    // add newBlock to list from correct side
                    blockTransferStop();
                }
            break;
            case PrepareToSend:
                if (isBlockTransferReady())
                {
                    conveyorState = State.ReadyToSend;
                }
            break;
            case ReadyToSend:
                transferPartner.blockTransferRegister(this);
            break;
            case Sending:
                
            break;
        }*/
    }
    
    /**
     * Checks if there are blocks in the conveyor
     * @return <i>true</i> if there is at least one block. <i>false</i> otherwise
     */
    private boolean hasBlock()
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
    
    public void blockTransferRegister(Conveyor c)
    {
        transferPartner = c;
    }
    public void blockTransferStart()
    {
        transferMotor.turnOn(transferMotorDirection());
        if (conveyorState == State.ReadyToSend) { conveyorState = State.Sending; }
        else if (conveyorState == State.PrepareToReceive) { conveyorState = State.Receiving; }
    }
    public Block blockTransferStop()
    {
        transferMotor.turnOff();
        
        Block b = null;
        if (conveyorState == State.Sending) {
            // remove block from list from correct side
            // b = ...;
        }
        
        conveyorState = State.Standby;
                
        transferPartner = null;
        return b;
    }
    
    public abstract boolean transferMotorDirection();
    public abstract void blockTransferFinished();
    public abstract boolean isBlockTransferPossible();
    public abstract void blockTransferPrepare();
    public abstract boolean isBlockTransferReady();
    
    public enum State {
        Standby, PrepareToReceive, Receiving,
        PrepareToSend, ReadyToSend, Sending;
    }
}
