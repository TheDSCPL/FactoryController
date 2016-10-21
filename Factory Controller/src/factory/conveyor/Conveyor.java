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
 *
 * @author Alex
 */
public abstract class Conveyor {
    /**
     * Contains a conveyor that is to transfer a block to this conveyor when both are ready
     */
    public Conveyor transferPartner;
    public Block[] blocks;
    public ConveyorState conveyorState;
    final Motor transferMotor;
    final Sensor[] presenceSensors;
    public String id;
    public Conveyor[] connectedConveyors;
    
    /**
     * Constructor of the abstract superclass
     * @param id Name of the conveyor
     * @param length Size fo the conveyor
     * @param connections Represents how many connections with other conveyors this conveyor has
     */
    public Conveyor(String id, int length, int connections) {
        this.id = id;
        
        blocks = new Block[length];
        presenceSensors = new Sensor[length];
        connectedConveyors = new Conveyor[connections];
        
        transferMotor = new Motor(Main.config.getBaseOutput(id) + 0);
        for (int i = 0; i < length; i++) { presenceSensors[i] = new Sensor(Main.config.getBaseInput(id) + i); }
    }
    
    public void update()
    {
        /*switch (conveyorState)
        {
            case Standby:
                if ( hasBlocks() )
                {
                    if (isBlockTransferPossible())
                    {
                        
                        blockTransferPrepare();
                        conveyorState = ConveyorState.PrepareToSend;
                    }
                }
                else if (transferPartner != null)
                {
                    if (transferPartner.conveyorState == ConveyorState.PrepareToSend) //can receive from transferPartner
                    {
                        blockTransferPrepare();
                        conveyorState = ConveyorState.PrepareToReceive;
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
                    conveyorState = ConveyorState.ReadyToSend;
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
    private boolean hasBlocks()
    {
        for(Block b : blocks) {
            if(b != null) {
                return true;
            }
        }            
        return false;
    }
    
    public boolean isSending()
    {
        return conveyorState == ConveyorState.PrepareToSend ||
               conveyorState == ConveyorState.ReadyToSend ||
               conveyorState == ConveyorState.Sending;
    }
    public boolean isReceiving()
    {
        return conveyorState == ConveyorState.PrepareToReceive ||
               conveyorState == ConveyorState.Receiving;
    }
    
    public void blockTransferRegister(Conveyor c)
    {
        transferPartner = c;
    }
    public void blockTransferStart()
    {
        transferMotor.turnOn(transferMotorDirection());
        if (conveyorState == ConveyorState.ReadyToSend) { conveyorState = ConveyorState.Sending; }
        else if (conveyorState == ConveyorState.PrepareToReceive) { conveyorState = ConveyorState.Receiving; }
    }
    public Block blockTransferStop()
    {
        transferMotor.turnOff();
        
        Block b = null;
        if (conveyorState == ConveyorState.Sending) {
            // remove block from list from correct side
            // b = ...;
        }
        
        conveyorState = ConveyorState.Standby;
                
        transferPartner = null;
        return b;
    }
    
    public abstract boolean transferMotorDirection();
    public abstract void blockTransferFinished();
    public abstract boolean isBlockTransferPossible();
    public abstract void blockTransferPrepare();
    public abstract boolean isBlockTransferReady();
    
    public enum ConveyorState {
        Standby, PrepareToReceive, Receiving,
        PrepareToSend, ReadyToSend, Sending;
    }
}
