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
public class Rotator extends Conveyor {

    private final Motor rotateMotor;
    private final Sensor rotateSensorLeft;
    private final Sensor rotateSensorRight;
    
    public Rotator(String id) {
        super(id, 1, 4);
        rotateMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        rotateSensorLeft = new Sensor(Main.config.getBaseInput(id) + 1);
        rotateSensorRight = new Sensor(Main.config.getBaseInput(id) + 2);
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
