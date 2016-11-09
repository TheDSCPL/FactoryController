package factory;

import main.*;

public class Motor {

    public final int baseIndex;
    private boolean on;
    private boolean plus;

    public Motor(int baseIndex) {
        this.baseIndex = baseIndex;
    }

    /**
     * Controls the motor.
     *
     * @param on represents the state of the motor. <i>true</i> if it is moving
     * and <i>false</i> if it is stopped
     * @param plus represents the way in which the motor is moving. <i>true</i>
     * if moving in the positive way and <i>false</i> if moving in the negative
     * way
     */
    public void control(boolean on, boolean plus) {
        this.on = on;
        this.plus = plus;
        
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
    
    public boolean on() {
        return on;
    }
    public boolean plus() {
        return plus;
    }

}
