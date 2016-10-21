/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;

import main.*;

/**
 *
 * @author Alex
 */
public class Motor {
    
    public final int baseIndex;
    
    public Motor(int baseIndex) {
        this.baseIndex = baseIndex;
    }
    
    /**
     * Controls the motor.
     * @param on represents the state of the motor. <i>true</i> if it is moving and <i>false</i> if it is stopped
     * @param plus represents the way in which the motor is moving. <i>true</i> if moving in the positive way and <i>false</i> if moving in the negative way
     */
    public void control(boolean on, boolean plus) {
        Main.modbus.setOutput(baseIndex, on && plus);
        Main.modbus.setOutput(baseIndex + 1, on && !plus);
    }
    
    public void turnOff() {
        control(false, false);
    }
    
    public void turnOn(boolean plus) {
        control(true, plus);
    }
    
    public void turnOnPlus() {
        turnOn(true);
    }
    
    public void turnOnMinus() {
        turnOn(false);
    }
    
}
