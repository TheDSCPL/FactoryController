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
        
        // DEMO
        //if (pushSensorMinus.on()) { pushMotor.turnOn(true); }
        //if (pushSensorPlus.on()) { pushMotor.turnOn(false); }
    }

    @Override
    public boolean transferMotorDirection()
    {
        if (isSending()) return transferPartner == connections[0];
        else if (isReceiving()) return transferPartner == connections[1];
        else throw new Error("transferMotorDirection called when not transfering block");
    }

    @Override
    public void blockTransferFinished() {} // TODO: push if necessary

    @Override
    public boolean isBlockTransferPossible() { return true; } // TODO: see if pusching has ended

    @Override
    public void blockTransferPrepare() {}

    @Override
    public boolean isBlockTransferReady() { return true; }
    
}
