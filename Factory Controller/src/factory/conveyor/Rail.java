/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import factory.other.Sensor;
import factory.other.Motor;
import factory.*;
import main.*;

/**
 *
 * @author Alex
 */
public class Rail extends Conveyor {

    private final Motor railMotor;
    private final Sensor railSensorMinus;
    private final Sensor railSensorPlus;
    
    public Rail(String id) {
        super(id, 1, 4);
        railMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        railSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        railSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
    }
    
    @Override
    protected boolean transferMotorDirection(Conveyor partner, boolean sending)
    {
        boolean top = partner == connections[0] ||
                      partner == connections[1];
        
        if (sending) return top;
        else return !top;
    }

    @Override
    protected void blockTransferFinished() {}

    @Override
    protected boolean isBlockTransferPossible() {
        return true;
    }

    @Override
    protected void blockTransferPrepare() {
        railMotor.turnOn(railMotorDirection());
    }

    @Override
    protected boolean isBlockTransferReady() {
        boolean ready = 
                (railMotorDirection() && railSensorPlus.on()) ||
                (!railMotorDirection() && railSensorMinus.on());
                
        if (ready) railMotor.turnOff();
        return ready;
    }
    
    private boolean railMotorDirection() {
        return transferPartner == connections[0] || transferPartner == connections[3];
    }
    
}
