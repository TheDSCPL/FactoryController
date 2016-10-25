/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell.assemb;

import main.*;
import factory.*;

/**
 * 
 * @author luisp
 */
public class Gantry
{
    
    //Outputs
    public final Motor XMotor, YMotor, ZMotor;
    
    //Inputs
    public final Sensor maxX,minX,upZ,downZ;
    public final Sensor[] Ysensors;
    
    public final String id;
    /**
     * <i>false</i> if open. <i>true</i> if closed
     */
    private boolean gripState = false;
    private final int gripOutputId;
    public final Sensor presenceSensor;
    
    Gantry(String id)
    {
        this.id = id;
        int inputSensorId  = Main.config.getBaseInput (id + "R");
        int outputActionId = Main.config.getBaseOutput(id + "R");
        
        // initialize outputs
        gripOutputId = outputActionId;
        XMotor   = new Motor(outputActionId + 0);
        YMotor   = new Motor(outputActionId + 2);
        ZMotor   = new Motor(outputActionId + 4);
        
        // initialize inputs
        Sensor y0, y1, y2, y3, y4;
        minX  = new Sensor(inputSensorId + 0);
        maxX  = new Sensor(inputSensorId + 1);
        y0    = new Sensor(inputSensorId + 2);
        y1    = new Sensor(inputSensorId + 3);
        y2    = new Sensor(inputSensorId + 4);
        y3    = new Sensor(inputSensorId + 5);
        y4    = new Sensor(inputSensorId + 6);
        Ysensors = new Sensor[] {y0, y1, y2, y3, y4};
        downZ = new Sensor(inputSensorId + 7);
        upZ   = new Sensor(inputSensorId + 8);
        
        presenceSensor = new Sensor(inputSensorId);
    }
    
    public boolean hasBlock()
    {
        return presenceSensor.on();
    }
    
    public void open()
    {
        Main.modbus.setOutput(gripOutputId, gripState = false);
    }
    
    public void close()
    {
        Main.modbus.setOutput(gripOutputId, gripState = true);
    }
    
    public boolean isOpen()
    {
        return !gripState;
    }
}
