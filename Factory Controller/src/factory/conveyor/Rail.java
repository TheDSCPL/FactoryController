/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import factory.*;
import java.util.Arrays;
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
    public boolean transferMotorDirection()
    {
        boolean top = transferPartner == connections[0] ||
                      transferPartner == connections[1];
        
        if (isSending()) return top;
        else if (isReceiving()) return !top;
        else throw new Error("transferMotorDirection called when not transfering block");
    }

    @Override
    public void blockTransferFinished() {}

    @Override
    public boolean isBlockTransferPossible() {
        return true;
    }

    @Override
    public void blockTransferPrepare() {
        System.out.println("Rail blockTransferPrepare " + railMotorDirection());
        railMotor.turnOn(railMotorDirection());
    }

    @Override
    public boolean isBlockTransferReady() {
        boolean ready = 
                (railMotorDirection() && railSensorPlus.on()) ||
                (!railMotorDirection() && railSensorMinus.on());
        
        System.out.println("----");
        System.out.println("Rail " + railMotorDirection() + " " + railSensorPlus.on() + " " + railSensorMinus.on());
        System.out.println(transferPartner);
        System.out.println(Arrays.toString(connections));
        
        if (ready) railMotor.turnOff();
        return ready;
    }
    
    private boolean railMotorDirection() {
        return transferPartner == connections[0] || transferPartner == connections[3];
    }
    
}
