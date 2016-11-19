/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openGrab the template in the editor.
 */
package factory.cell.assemb;

import factory.other.*;
import main.*;

/**
 *
 * @author luisp
 */
public class Gantry {

    //Outputs
    public final Motor XMotor, YMotor, ZMotor;

    //Inputs
    public final Sensor upZ, downZ;
    public final Sensor[] Xsensors, Ysensors;

    private final long initializationTimeoutMillis = 1500; // TODO: this should be a property in the config file
    private final long maxInitTries = 2;

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
    int isAtX = -1;
    /**
     * including spaces between sensors (space above sensor 0 at position 0,
     * sensor 0 at position 1, space between sensor 0 and sensor 1 at position
     * 2, sensor 1 at position 3, ...) -1 means undefined position
     */
    int isAtY = -1;

    /**
     * number of X initialization attempts
     * -1 means it is initialized
     */
    int initXTries = 0;
    /**
     * number of Y initialization attempts
     * -1 means it is initialized
     */
    int initYTries = 0;
    //initialize z
    private boolean Zready = false;

    Gantry(String id) {
        this.id = id;
        int inputSensorId = Main.config.getBaseInput(id + "R");
        int outputActionId = Main.config.getBaseOutput(id + "R");

        // initialize outputs
        gripOutputId = outputActionId + 6;
        System.err.println("gripOutputId="+gripOutputId);
        XMotor = new Motor(outputActionId + 0);
        YMotor = new Motor(outputActionId + 2);
        ZMotor = new Motor(outputActionId + 4);

        // initialize inputs
        Sensor y0, y1, y2, y3, y4;
        Sensor maxX, minX;
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
     * @return The 1st active Xsensor's index or -1 if none are active
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
     * @return The 1st active Ysensor's index or -1 if none are active
     */
    private int getActiveYSensor() {
        for (int s = 0; s < Ysensors.length; s++) {
            if (Ysensors[s].on()) {
                return s;
            }
        }
        return -1;
    }

    boolean isInitializingX()
    {
        return initXTries >= 0;
    }
    
    boolean isInitializingY()
    {
        return initYTries >= 0;
    }
    
    boolean isInitializingZ()
    {
        return !Zready;
    }

    boolean isInitializing()
    {
        return isInitializingX() || isInitializingY() || isInitializingZ();
    }
    
    private void initializeX()
    {
        if(!isInitializingX())
            return;
        if(initXTimer == -1)    //new attempt
            initXTimer = System.currentTimeMillis();
        if(System.currentTimeMillis() - initXTimer >= initializationTimeoutMillis)  //the current attempt timed out
        {
            if(initXTries < maxInitTries)   //switch between attemps and change motor direction (by incrementing the number of tries)
            {
                initXTries++;
                initXTimer = -1;
                XMotor.turnOff();
                return;
            }
            else    //all attempts done and failed
            {
                XMotor.turnOff();
                throw new Error("Gantry initialization timed out");
            }
        }
        if(getActiveXSensor() != -1)    //found sensor. done initializing X
        {
            XMotor.turnOff();
            initXTimer = -1;
            initXTries = -1;
        }
        else    //if still initializing
            XMotor.turnOn( initXTries%2 == 1);  //plus in odd tries and minus in even tries
    }
    
    private void initializeY()
    {
        if(!isInitializingY())
            return;
        if(initYTimer == -1)    //new attempt
            initYTimer = System.currentTimeMillis();
        if(System.currentTimeMillis() - initYTimer >= initializationTimeoutMillis)  //the current attempt timed out
        {
            if(initYTries < maxInitTries)   //switch between attemps and change motor direction (by incrementing the number of tries)
            {
                initYTries++;
                initYTimer = -1;
                YMotor.turnOff();
                return;
            }
            else    //all attempts done and failed
            {
                YMotor.turnOff();
                throw new Error("Gantry initialization timed out");
            }
        }
        if(getActiveXSensor() != -1)    //found sensor. done initializing Y
        {
            YMotor.turnOff();
            initYTimer = -1;
            initYTries = -1;
        }
        else    //if still initializing
            YMotor.turnOn( initYTries%2 == 1);  //plus in odd tries and minus in even tries
    }
    
    private void initializeZ()
    {
        if(!isInitializingZ())
            return;
        if (upZ.on()) {
            Zready = true;
            ZMotor.turnOff();
        } else {
            ZMotor.turnOnPlus();
        }
    }
    
    /**
     * Initializes the gantry
     * @return true if the Gantry is initialized
     */
    private boolean initialize()
    {
        if(!isInitializing())
            return true;
        openGrab();
        if(isInitializingZ())
            initializeZ();
        else
        {
            initializeX();
            initializeY();
        }
        return !isInitializing();
    }
    
    /**
     * Updates <i>isAtX</i> and <i>isAtY</i> variables.
     * <b>It's asured that this method only changes the XY state machines if
     * they are initializing.</b> (so you don't have to worry about this
     * changing the states after the initialization is done)
     */
    private void updateIsAt() {
        //Do nothing if initializing
        if(isInitializing())
            return;
        
        //---------------UPDATE isAtX-----------------
        if (getActiveXSensor() != -1) //is at a sensor
            isAtX = getActiveXSensor() * 2 + 1; //for example, sensor 1 is at position 3
        else //is in a space
            if(XMotor.on()) //the motor is on. if it is not, leave the isAtX as it was
                if (isAtX % 2 != 0) //previous state was a sensor -> we have to decide which space it went to. if the previous isAtX was a space, then do nothing because it is still in that space
                    isAtX = isAtX + (XMotor.plus() ? 1 : -1); //if moving in the positive direction, to go the next space, else go to the previous space

        //---------------UPDATE isAtY-----------------
        if (getActiveYSensor() != -1) //is at a sensor
            isAtY = getActiveYSensor() * 2 + 1; //for example, sensor 1 is at position 3
        else //is in a space
            if(YMotor.on()) //the motor is on. if it is not, leave the isAtY as it was
                if (isAtY % 2 != 0) //previous state was a sensor -> we have to decide which space it went to. if the previous isAtY was a space, then do nothing because it is still in that space
                    isAtY = isAtY + (YMotor.plus() ? 1 : -1); //if moving in the positive direction, to go the next space, else go to the previous space
    }

    /**
     * Updates the gantry state
     * @return true if initialized and operation. false otherwise
     */
    boolean update() {
        
        if(!initialize())   //if the gantry is not initialized, do nothing and return false meaning it is not yet initialized
            return false;
            
        //updates the isAtX/Y variables
        updateIsAt();
        //System.out.println("isAtX=" + isAtX + " isAtY=" + isAtY + "isInitializing=" + isInitializing());
        return !isInitializing();
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
