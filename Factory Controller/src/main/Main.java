package main;

import transformation.*;
import coms.*;
import control.*;
import factory.*;

// PULL -> WORK -> ADD -> COMMIT -> PULL -> PUSH

/*
Major TODO list:
  [ ]  Gantry (Class: Gantry, Assigned to: Luis)
  [ ]  Assembler cell processing and optimization (Class: Assembler, Assigned to: Luis)
  [ ]  Serial cell processing and optimization (Class: SerialCell, Assigned to: Alex)
  [ ]  Parallel cell processing and optimization (Class: ParalleCell, Assigned to: ?)
  [ ]  Linear conveyors with multiple blocks (Class: Conveyor, Assigned to: Alex)
  [ ]  Order processing algorithm and distribution by the various cells (Class: Factory, Assigned to: ?)
  [X]  Transformation sequences (Class: TransformationManager, Assigned to: Alex)
  [ ]  TODO's in code (Class: -, Assigned to: all)
 */

public class Main {

    public static final Configuration config = new Configuration();
    public static final ModbusMaster modbus = new ModbusMaster();
    public static final Factory factory = new Factory();
    public static final TransformationManager transfm = new TransformationManager();
    public static final OrderController orderc = new OrderController();

    private static void connectAndRun() throws Exception {
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

    private static void tryToOpenSFS() {
        System.out.println("Simulator not running. Trying to start it.");
        try {
            sfs = Runtime.getRuntime().exec("java -jar sfs.jar");

            if (!sfs.isAlive()) {
                // TODO: create a class Error extends java.lang.Error that, in
                // the constructor, has a printStackTrace and a System.exit so
                // it is a fatal error and can't be caught
                throw new Error("Error starting simulator. Check permissions and if all of the simulation's files are present.");
            }
            Thread.sleep(100);
        }
        catch (Exception e) {
            /*System.exit(31);*/
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            connectAndRun();
        }
        catch (java.net.ConnectException ex) //if simulator not running try to start it
        {
            tryToOpenSFS();
            try {
                connectAndRun();
            }
            catch (Exception e) {
                /*System.exit(32)*/
            }
        }
        catch (Exception ex) {
        }
        //System.exit(33);
    }
}
