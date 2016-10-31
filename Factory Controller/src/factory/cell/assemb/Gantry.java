/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openGrab the template in the editor.
 */
package factory.cell.assemb;

import main.*;
import factory.*;

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

    private final long initializationTimeoutMillis = 1500;

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
    private int isAtX;
    /**
     * including spaces between sensors (space above sensor 0 at position 0,
     * sensor 0 at position 1, space between sensor 0 and sensor 1 at position
     * 2, sensor 1 at position 3, ...) -1 means undefined position
     */
    private int isAtY;

    private MOVEMENT_STATE   XState = MOVEMENT_STATE.INITIALIZING1ST,
                            YState = MOVEMENT_STATE.INITIALIZING1ST;
    //initialize z
    private boolean Zready = false;

    Gantry(String id) {
        this.id = id;
        int inputSensorId = Main.config.getBaseInput(id + "R");
        int outputActionId = Main.config.getBaseOutput(id + "R");

        // initialize outputs
        gripOutputId = outputActionId;
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

    private enum MOVEMENT_STATE {
        INITIALIZING1ST, INITIALIZING2ND, IDLE, MOVINGPLUS, MOVINGMINUS;
    }

    private boolean isInitializing()
    {
        return (XState == MOVEMENT_STATE.INITIALIZING1ST || XState == MOVEMENT_STATE.INITIALIZING2ND || YState == MOVEMENT_STATE.INITIALIZING1ST || YState == MOVEMENT_STATE.INITIALIZING2ND || !Zready);
    }
    
    /**
     * Updates <i>isAtX</i> and <i>isAtY</i> variables.
     * <b>It's asured that this method only changes the XY state machines if
     * they are initializing.</b> (so you don't have to worry about this
     * changing the states after the initialization is done)
     *
     */
    private void updateIsAt() {
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
            if(upZ.on())
            {
                Zready = true;
                ZMotor.turnOff();
            }
            else
                ZMotor.turnOnPlus();
            return;
        }
            
        //updates the isAtX/Y variables
        updateIsAt();
        
        //If initializing, don't do anything else
        if (isInitializing()) {
            return;
        }
        
        
    }

    public boolean hasBlock() {
        return presenceSensor.on();
    }

    public void openGrab() {
        Main.modbus.setOutput(gripOutputId, gripState = false);
    }

    public void closeGrab() {
        Main.modbus.setOutput(gripOutputId, gripState = true);
    }

    public boolean isOpen() {
        return !gripState;
    }

    private static class Transfer
    {
        private final int fromX, fromY, toX, toY;
        private int whereToX;
        private int whereToY;
        private final Gantry gantry;
        private TRANSFER_STATE status = TRANSFER_STATE.MOVING_TO_ORIGIN;
        public Transfer(Gantry gantry, int fromX, int fromY, int toX, int toY) {
            if ((fromX > 1 || fromX < 0) || (toX > 1 || toX < 0) || (fromY > 4 || fromY < 0) || (toY > 4 || toY < 0))
            {
                throw new IndexOutOfBoundsException("Tried to transfer a block with Gantry from/to invalid coordinates");
            }
            this.gantry = gantry;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            whereToX = 2*fromX +1;
            whereToY = 2*fromY +1;
        }
        
        private long grabTimer;
        
        /**
         * Updates the FSM (to transfer the block)
         * @return 
         */
        boolean update()
        {
            switch(status)
            {
                case MOVING_TO_ORIGIN:
                    boolean Xready = (gantry.isAtX == whereToX);
                    if(Xready)
                    {
                        gantry.XMotor.turnOff();
                    }
                    else
                    {
                        gantry.XMotor.turnOn(gantry.isAtX > whereToX);
                    }
                    
                    boolean Yready = (gantry.isAtY == whereToY);
                    if(Yready)
                    {
                        gantry.YMotor.turnOff();
                    }
                    else
                    {
                        gantry.YMotor.turnOn(gantry.isAtY > whereToY);
                    }
                    
                    if(Xready && Yready)    //arrived at origin
                        status=TRANSFER_STATE.GO_DOWN_ORIGIN;
                break;
                case GO_DOWN_ORIGIN:
                    gantry.ZMotor.turnOnMinus();
                    
                    if(gantry.downZ.on())   //fully down
                    {
                        gantry.ZMotor.turnOff();
                        grabTimer = System.currentTimeMillis();
                        status=TRANSFER_STATE.GRAB_ORIGIN;
                    }
                break;
                case GRAB_ORIGIN:   //espera 1 segundo, como indicado na descrição da fábrica
                    gantry.closeGrab();
                    if(System.currentTimeMillis() - grabTimer >= 1000)
                        status=TRANSFER_STATE.GO_UP_ORIGIN;
                break;
                case GO_UP_ORIGIN:
                    gantry.ZMotor.turnOnPlus();
                    
                    if(gantry.upZ.on())   //fully down
                    {
                        gantry.ZMotor.turnOff();
                        status=TRANSFER_STATE.MOVING_TO_DESTINATION;
                        whereToX = 2*toX +1;
                        whereToY = 2*toY +1;
                    }
                break;
                case MOVING_TO_DESTINATION:
                    
                break;
                case DROP_DESTINATION:
                    
                break;
                case FINISHED:
                    return true;
                default:
                    throw new IllegalStateException("Illegal state reached at the Transfer FSM");
            }
            return false;
        }
        
        private enum TRANSFER_STATE {
        MOVING_TO_ORIGIN, GO_DOWN_ORIGIN, GRAB_ORIGIN, GO_UP_ORIGIN, MOVING_TO_DESTINATION, DROP_DESTINATION, FINISHED; //considering that there is no need for the gantry to go down in the destination
        }
    }
    
    void tranferBlock(int fromX, int fromY, int toX, int toY) {
        

    }
}
