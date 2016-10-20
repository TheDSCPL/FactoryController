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
    public Conveyor transferPartner;
    public Block[] blocks;
    public ConveyorState conveyorState;
    private Motor transferMotor;
    private Sensor[] presenceSensors;
    public String id;
    public Conveyor[] connectedConveyors;
    public int firstI,firstO;
    
    public Conveyor(String id, int length, int connections) {
        this.id = id;
        blocks = new Block[length];
        presenceSensors = new Sensor[length];
        connectedConveyors = new Conveyor[connections];
        
        transferMotor = new Motor(Main.config.getBaseOutput(id) + 0);
        for (int i = 0; i < length; i++) {
            presenceSensors[i] = new Sensor(Main.config.getBaseOutput(id) + i);
        }
        
        
        
    }
    
    public void update() {
        switch (conveyorState) {
            case Standby:
                if (/*has block to send*/ false) {
                    if (isBlockTransferPossible()) {
                        
                    }
                }
                else if (transferPartner != null) {
                    if (/* can receive from transferPartner*/ false) {
                        blockTransferPrepare();
                        conveyorState = ConveyorState.PrepareToReceive;
                    }
                }
            case PrepareToReceive:
                if (isBlockTransferReady()) {
                    blockTransferStart();
                    transferPartner.blockTransferStart();
                }
            case Receiving:
                if (/*sensor*/false) {
                    Block newBlock = transferPartner.blockTransferStop();
                    // add newBlock to list from correct side
                    blockTransferStop();
                }
            case PrepareToSend:
                if (isBlockTransferReady()) {
                    conveyorState = ConveyorState.ReadyToSend;
                }
            case ReadyToSend:
                transferPartner.blockTransferRegister(this);
            case Sending: break;
        }
    }
    
    public void blockTransferRegister(Conveyor c) {
        transferPartner = c;
    }
    public void blockTransferStart() {
        transferMotor.turnOn(transferMotorDirection());
        if (conveyorState == ConveyorState.ReadyToSend) { conveyorState = ConveyorState.Sending; }
        else if (conveyorState == ConveyorState.PrepareToReceive) { conveyorState = ConveyorState.Receiving; }
    }
    public Block blockTransferStop() {
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
        PrepareToSend, ReadyToSend, Sending
    }
}
