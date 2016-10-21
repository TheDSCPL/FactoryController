/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import factory.*;
import main.*;

/**
 *
 * @author Alex
 */
public class Pusher extends Conveyor {
    
    private final Motor pushMotor;
    private final Sensor pushSensorPlus;
    private final Sensor pushSensorMinus;
    
    public Pusher(String id) {
        super(id, 1, 2);
        pushMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        pushSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        pushSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
    }
    
    @Override
    public void update() {
        super.update();
        
        if (pushSensorMinus.on()) { pushMotor.turnOn(true); }
        if (pushSensorPlus.on()) { pushMotor.turnOn(false); }
    }

    @Override
    public boolean transferMotorDirection()
    {
        if (isSending()) return transferPartner == connectedConveyors[0];
        else if (isReceiving()) return transferPartner == connectedConveyors[1];
        else throw new Error("transferMotorDirection called when not transfering block");
    }

    @Override
    public void blockTransferFinished() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBlockTransferPossible() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void blockTransferPrepare() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBlockTransferReady() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
