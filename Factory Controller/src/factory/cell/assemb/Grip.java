/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell.assemb;

import main.*;

public class Grip
{
    /**
     * <i>false</i> if open. <i>true</i> if closed
     */
    private boolean gripState = false;
    private final int outputActionId;
    private final int inputSensorId;
    Grip(int inputSensorId, int outputActionId)
    {
        this.inputSensorId = inputSensorId;
        this.outputActionId = outputActionId;
    }
    
    public boolean hasBlock()
    {
        return Main.modbus.getInput(inputSensorId);
    }
    
    public void open()
    {
        Main.modbus.setOutput(outputActionId, gripState = false);
    }
    
    public void close()
    {
        Main.modbus.setOutput(outputActionId, gripState = true);
    }
    
    public boolean isOpen()
    {
        return !gripState;
    }
    
}
