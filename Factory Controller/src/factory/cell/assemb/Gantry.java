/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openGrab the template in the editor.
 */
package factory.cell.assemb;

import factory.other.Sensor;
import factory.other.Motor;
import factory.*;
import main.*;

/**
 *
 * @author luisp
 */
public class Gantry {

    //Outputs
    public final Motor XMotor, YMotor, ZMotor;

    //Inputs
    public final Sensor maxX, minX, upZ, downZ;
    public final Sensor[] Xsensors, Ysensors;

    private final long initializationTimeoutMillis = 1500; // TODO this should be a property in the config file

    public final String id;
    /**
     * <i>false</i> if openGrab. <i>true</i> if closed
     */
    private boolean gripState = false;
    private final int gripOutputId;
    public final Sensor presenceSensor;
    /**
     * including spaces between sensors (space above sensor 0 at position 0,
     * sensor 0 at position 1, space between sensor 0 and sensor 1 at position
     * 2, sensor 1 at position 3, ...) -1 means undefined position
     */
    int isAtX;
    /**
     * including spaces between sensors (space above sensor 0 at position 0,
     * sensor 0 at position 1, space between sensor 0 and sensor 1 at position
     * 2, sensor 1 at position 3, ...) -1 means undefined position
     */
    int isAtY;

    MOVEMENT_STATE   XState = MOVEMENT_STATE.INITIALIZING1ST,
                     YState = MOVEMENT_STATE.INITIALIZING1ST;
    //initialize z
    private boolean Zready = false;

    Gantry(String id) {
        this.id = id;
        int inputSensorId = Main.config.getBaseInput(id + "R");
        int outputActionId = Main.config.getBaseOutput(id + "R");

        // initialize outputs
        gripOutputId = outputActionId + 6;
        XMotor = new Motor(outputActionId + 0);
        YMotor = new Motor(outputActionId + 2);
        ZMotor = new Motor(outputActionId + 4);

        // initialize inputs
        Sensor y0, y1, y2, y3, y4;
        minX = new Sensor(inputSensorId + 0);
        maxX = new Sensor(inputSensorId + 1);
        Xsensors = new Sensor[]{minX, maxX};
        y0 = new Sensor(inputSensorId + 2);
        y1 = new Sensor(inputSensorId + 3);
        y2 = new Sensor(inputSensorId + 4);
        y3 = new Sensor(inputSensorId + 5);
        y4 = new Sensor(inputSensorId + 6);
        Ysensors = new Sensor[]{y0, y1, y2, y3, y4};
        downZ = new Sensor(inputSensorId + 7);
        upZ = new Sensor(inputSensorId + 8);

        presenceSensor = new Sensor(inputSensorId);
    }

    private long initXTimer = -1, initYTimer = -1;

    /**
     * @return The Xsensor that is active or -1 if none are active
     */
    private int getActiveXSensor() {
        for (int s = 0; s < Xsensors.length; s++) {
            if (Xsensors[s].on()) {
                return s;
            }
        }
        return -1;
    }

    /**
     * @return The Ysensor that is active or -1 if none are active
     */
    private int getActiveYSensor() {
        for (int s = 0; s < Ysensors.length; s++) {
            if (Ysensors[s].on()) {
                return s;
            }
        }
        return -1;
    }

    enum MOVEMENT_STATE {
        INITIALIZING1ST, INITIALIZING2ND, IDLE, MOVINGPLUS, MOVINGMINUS;
    }

    boolean isInitializing()
    {
        return (XState == MOVEMENT_STATE.INITIALIZING1ST || XState == MOVEMENT_STATE.INITIALIZING2ND || YState == MOVEMENT_STATE.INITIALIZING1ST || YState == MOVEMENT_STATE.INITIALIZING2ND || !Zready);
    }
    
    /**
     * Updates <i>isAtX</i> and <i>isAtY</i> variables.
     * <b>It's asured that this method only changes the XY state machines if
     * they are initializing.</b> (so you don't have to worry about this
     * changing the states after the initialization is done)
     */
    private void updateIsAt() {
        //Initialize claw
        if(isInitializing())
            openGrab();
        
        //---------------UPDATE isAtX-----------------
        if (getActiveXSensor() != -1) //is at a sensor
        {
            if (XState == MOVEMENT_STATE.INITIALIZING1ST || XState == MOVEMENT_STATE.INITIALIZING2ND) //it's initializing and found a sensor
            {
                isAtX = getActiveXSensor() * 2 + 1; //for example, sensor 1 is at position 3
                XMotor.turnOff();
                XState = MOVEMENT_STATE.IDLE;
            }
            else {
                isAtX = getActiveXSensor() * 2 + 1; //for example, sensor 1 is at position 3
            }
        }
        else //is in a space
        {
            switch (XState) //evaluates last cycle's isAtX
            {
                case INITIALIZING1ST: //if undefined position; 1st attempt
                    if (initXTimer == -1) {
                        initXTimer = System.currentTimeMillis();
                        XMotor.turnOnMinus();
                    }
                    else if (System.currentTimeMillis() - initXTimer >= initializationTimeoutMillis) {
                        XState = MOVEMENT_STATE.INITIALIZING2ND;
                        initXTimer = System.currentTimeMillis();
                        XMotor.turnOff();
                    }
                    break;
                case INITIALIZING2ND:    //if undefined position; 2nd attempt
                    if (System.currentTimeMillis() - initXTimer >= initializationTimeoutMillis) {
                        //If the gatry is now trying to initialize in the other direction (second attempt) and it times out, then it's an error
                        throw new Error("Gantry initialization timed out");
                    }
                    XMotor.turnOnPlus();
                    break;
                case IDLE:
                    XMotor.turnOff();
                    break;
                default:    //It's moving and not initializing
                    if (isAtX % 2 != 0) //previous state was a sensor, we have to decide which space it went to
                    {
                        isAtX = isAtX + (XState == MOVEMENT_STATE.MOVINGPLUS ? 1 : -1); //if moving in the positive direction, to go the next space, else go to the previous space
                    }
            }
        }

        //---------------UPDATE isAtY-----------------
        if (getActiveYSensor() != -1) //is at a sensor
        {
            if (YState == MOVEMENT_STATE.INITIALIZING1ST || YState == MOVEMENT_STATE.INITIALIZING2ND /*isAtX == -1*/) //i'm initializing and found a sensor
            {
                isAtY = getActiveYSensor() * 2 + 1; //for example, sensor 1 is at position 3
                YMotor.turnOff();
                YState = MOVEMENT_STATE.IDLE;
            }
            else {
                isAtY = getActiveYSensor() * 2 + 1; //for example, sensor 1 is at position 3
            }
        }
        else //is in a space
        {
            switch (YState) //evaluates last cycle's isAtY
            {
                case INITIALIZING1ST: //if undefined position; 1st attempt
                    if (initYTimer == -1) {
                        initYTimer = System.currentTimeMillis();
                        YMotor.turnOnMinus();
                    }
                    else if (System.currentTimeMillis() - initYTimer >= initializationTimeoutMillis) {
                        YState = MOVEMENT_STATE.INITIALIZING2ND;
                        initYTimer = System.currentTimeMillis();
                        YMotor.turnOff();
                    }
                    break;
                case INITIALIZING2ND:    //if undefined position; 2nd attempt
                    if (System.currentTimeMillis() - initYTimer >= initializationTimeoutMillis) {
                        //If the gatry is now trying to initialize in the other direction (second attempt) and it times out, then it's an error
                        throw new Error("Gantry initialization timed out");
                    }
                    YMotor.turnOnPlus();
                    break;
                case IDLE:
                    YMotor.turnOff();
                    break;
                default:
                    if (isAtY % 2 != 0) //previous state was a sensor, we have to decide which space it went to
                    {
                        isAtY = isAtY + (YState == MOVEMENT_STATE.MOVINGPLUS ? 1 : -1); //if moving in the positive direction, to go the next space, else go to the previous space
                    }
            }
        }
    }

    void update() {
        //Gantry must be up
        if(!Zready)
        {
            System.out.print("initializing z | ");
            if(upZ.on())
            {
                Zready = true;
                ZMotor.turnOff();
            }
            else
            {
                System.out.println("not yet up");
                ZMotor.turnOnPlus();
            }
            return;
        }
            
        //updates the isAtX/Y variables
        updateIsAt();
        //System.out.println("isAtX=" + isAtX + " isAtY=" + isAtY + "isInitializing=" + isInitializing());
    }

    public boolean hasBlock() {
        return presenceSensor.on();
    }

    public void openGrab() {
        Main.modbus.setOutput(gripOutputId, gripState = true);
    }

    public void closeGrab() {
        Main.modbus.setOutput(gripOutputId, gripState = false);
    }

    public boolean isOpen() {
        return !gripState;
    }

    
}
