/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main; // PULL -> WORK -> ADD -> COMMIT -> PULL -> PUSH

import coms.*;
import control.*;
import factory.*;

//TODO: create a class Error extends java.lang.Error that, in the constructor, has a printStackTrace and a System.exit so it is a fatal error and can't be caught.

/**
 *
 * @author Alex
 */
public class Main {
    
    public static final Configuration config = new Configuration();
    public static final ModbusMaster modbus = new ModbusMaster();
    public static final Factory factory = new Factory();
    public static final TransformationManager transfm = new TransformationManager();
    public static final OrderController orderc = new OrderController();

    private static void connectAndRun() throws Exception
    {
        modbus.connect();

        while (true) {
            modbus.refreshInputs();
            factory.update();
            orderc.update();
            modbus.refreshOutputs();

            Thread.sleep(1);
        }
    }
    
    private static Process sfs = null;
    private static Thread killSFS = null;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try { connectAndRun(); }
        catch (java.net.ConnectException ex)    //if simulator not running try to start it
        {
            System.err.println("Simulator not running. Trying to start it.");
            try
            {
                sfs = Runtime.getRuntime().exec("java -jar sfs.jar");
                
                if(!sfs.isAlive())
                    throw new Error("Error starting simulator. Check permissions and if all of the simulation's files are present.");
                Thread.sleep(100);
                connectAndRun();
            }
            catch(Error e)
            {
                e.printStackTrace();
                return;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Error("Error starting simulator. Check permissions and if all of the simulation's files are present.");
            }
        }
        catch(Error e)
        {
            e.printStackTrace();
        }
        catch (Exception ex)
        { ex.printStackTrace(); } 
    }
}
