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
    private final Sensor railSensorLeft;
    private final Sensor railSensorRight;
    
    public Rail(String id) {
        super(id, 1, 4);
        railMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        railSensorLeft = new Sensor(Main.config.getBaseInput(id) + 1);
        railSensorRight = new Sensor(Main.config.getBaseInput(id) + 2);
    }
    
    @Override
    public boolean transferMotorDirection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
