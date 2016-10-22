/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import main.*;
import factory.*;

/**
 * @author Luis Paulo
 * @author Alex
 */
public class Machine extends Conveyor {

    public enum Type { A, B, C }
    
    public final Type type;
    public final Sensor ZSensor, zSensor, XSensor, xSensor;
    /**
     * Motor that makes the tool work
     */
    public final Motor toolActivateMotor;
    /**
     * Motor that changes the tool
     */
    public final Motor toolSelectMotor;
    public final Motor xMotor, zMotor;
    
    public Machine(String id, Type type) {
        super(id, 1, 2);
        this.type = type;
        
        toolActivateMotor = new Motor(Main.config.getBaseOutput(id) + 4);
        toolSelectMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        xMotor = new Motor(Main.config.getBaseOutput(id) + 5);
        zMotor = new Motor(Main.config.getBaseOutput(id) + 7);
        
        XSensor = new Sensor(Main.config.getBaseInput(id) + 2);
        xSensor = new Sensor(Main.config.getBaseInput(id) + 3);
        ZSensor = new Sensor(Main.config.getBaseInput(id) + 4);
        zSensor = new Sensor(Main.config.getBaseInput(id) + 5);
    }
    
    boolean last1 = false;
    boolean last2 = false;
    boolean last3 = false;
    
    @Override
    public void update() {
        super.update();
        
        if (xSensor.on()) { last1 = false; }
        if (XSensor.on()) { last1 = true; }
        xMotor.turnOn(last1);
        
        if (zSensor.on()) { last2 = true; }
        if (ZSensor.on()) { last2 = false; }
        zMotor.turnOn(last2);
        
        toolSelectMotor.turnOn(true);
    }
    
    @Override
    public boolean transferMotorDirection()
    {
        if (isSending()) return transferPartner == connections[0];
        else if (isReceiving()) return transferPartner == connections[1];
        else throw new Error("transferMotorDirection called when not transfering block");
    }

    @Override
    public void blockTransferFinished() {} // TODO: start machining

    @Override
    public boolean isBlockTransferPossible() { return true; } // TODO: has machining ended

    @Override
    public void blockTransferPrepare() {}

    @Override
    public boolean isBlockTransferReady() { return true; }
    
}
