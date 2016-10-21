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
    public void update() {
        super.update();
        
        if (railSensorMinus.on()) { railMotor.turnOn(true); }
        if (railSensorPlus.on()) { railMotor.turnOn(false); }
    }
    
    @Override
    public boolean transferMotorDirection()
    {
        boolean top = transferPartner == connectedConveyors[0] ||
                      transferPartner == connectedConveyors[1];
        
        if (isSending()) return top;
        else if (isReceiving()) return !top;
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
