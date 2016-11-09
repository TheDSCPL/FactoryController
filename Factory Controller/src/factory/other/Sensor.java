/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.other;

import main.*;

/**
 *
 * @author Alex
 */
public class Sensor {
    
    public final int index;
    
    public Sensor(int index) {
        this.index = index;
    }
    
    public boolean on() {
        return Main.modbus.getInput(index);
    }
    
}
