/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import factory.*;

/**
 *
 * @author Alex
 */
public class Mover extends Conveyor {

    public Mover(String id, int length) {
        super(id, length, 2);
    }
    
    @Override
    public boolean transferMotorDirection() {
        return transferPartner == connectedConveyors[0];
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
