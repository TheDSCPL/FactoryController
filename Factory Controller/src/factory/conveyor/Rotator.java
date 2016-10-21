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
    private final Sensor rotateSensorMinus;
    private final Sensor rotateSensorPlus;
    
    public Rotator(String id) {
        super(id, 1, 4);
        rotateMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        rotateSensorMinus = new Sensor(Main.config.getBaseInput(id) + 1);
        rotateSensorPlus = new Sensor(Main.config.getBaseInput(id) + 2);
    }
    
    @Override
    public void update() {
        super.update();
        
        if (rotateSensorMinus.on()) { rotateMotor.turnOn(false); }
        if (rotateSensorPlus.on()) { rotateMotor.turnOn(true); }
    }
    
    @Override
    public boolean transferMotorDirection()
    {
        boolean leftOrTop = transferPartner == connections[0] ||
                            transferPartner == connections[1];
        
        if (isSending()) return leftOrTop;
        else if (isReceiving()) return !leftOrTop;
        else throw new Error("FATAL ERROR: transferMotorDirection called when not transfering block");
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
