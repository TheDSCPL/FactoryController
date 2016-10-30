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

    /**
     * @param id name/id of the mover
     * @param length Length of the mover
     */
    public Mover(String id, int length) {
        super(id, length, 2);
    }

    @Override
    public boolean transferMotorDirection() {
        if (isSending()) {
            return transferPartner == connections[0];
        }
        else if (isReceiving()) {
            return transferPartner == connections[1];
        }
        else {
            throw new Error("transferMotorDirection called when not transfering block");
        }
    }

    @Override
    public void blockTransferFinished() {
        
    }

    @Override
    public boolean isBlockTransferPossible() {
        return true;
    }

    @Override
    public void blockTransferPrepare() {
        
    }

    @Override
    public boolean isBlockTransferReady() {
        return true;
    }

}
