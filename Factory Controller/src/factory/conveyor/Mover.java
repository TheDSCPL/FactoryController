/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

/**
 *
 * @author Alex
 */
public class Mover extends Conveyor {

    public Mover(String id, int length) {
        super(id, length, 2);
    }
    
    @Override
    public void update() {
        super.update();
        
        transferMotor.control(presenceSensors[0].on(), true);
    }
    
    @Override
    public boolean transferMotorDirection()
    {
        if (isSending()) return transferPartner == connectedConveyors[0];
        else if (isReceiving()) return transferPartner == connectedConveyors[1];
        else throw new Error("FATAL ERROR: transferMotorDirection called when not transfering block");
    }

    @Override
    public void blockTransferFinished() {}

    @Override
    public boolean isBlockTransferPossible() { return true; }

    @Override
    public void blockTransferPrepare() {}

    @Override
    public boolean isBlockTransferReady() { return true; }
    
}
