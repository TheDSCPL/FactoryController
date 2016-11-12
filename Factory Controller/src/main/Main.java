package main;

import coms.*;
import config.Configuration;
import control.*;
import factory.*;
import transformation.*;

// PULL -> WORK -> ADD -> COMMIT -> PULL -> PUSH

/*
Major TODO list:

Luis:
  [.]  Gantry (Class: Gantry)
  [ ]  Assembler cell processing and optimization (Class: Assembler)
  [ ]  Order processing algorithm and distribution by the various cells (Class: Factory)

Alex:
  [X]  Serial cell processing and optimization (Class: SerialCell)
  [X]  Tool pre-selection on Serial cell (Class: SerialCell)
  [\]  Linear conveyors with multiple blocks (Class: Conveyor)
  [X]  Rotator conveyor optimization: automatically rotate back to horizontal position to speed up block traveling time (Class: Conveyor, Rotator)
  [X]  Transformation sequences (Class: TransformationManager)
  [X]  Tool class for selecting and activating tools in machines (Class: Machine)
  [X]  Algorithm for selecting which transfer partner should be chosen first (Class: Conveyor)
  [.]  Parallel cell processing and optimization (Class: ParalleCell - last thing: entry of blocks on cell is not prioritized)
  [X]  Tool pre-selection on Parallel cell (Class: ParallelCell)
  [ ]  Statistics module (Class: Stats, ...?)

Unassigned:
  [ ]  TODO's in code (Class: N/A)

Legend:
   X   Done
   .   In progress
   \   Skipped
 */

public class Main {

    public static final Configuration config = new Configuration();
    public static final ModbusMaster modbus = new ModbusMaster();
    public static final Factory factory = new Factory();
    public static final TransformationManager transfm = new TransformationManager();
    public static final OrderController orderc = new OrderController();
    public static final long controlLoopDelay = (long)config.getI("controlLoopDelay");
    
    /**
     * @return Time in milliseconds
     */
    public static long time() {
        return System.nanoTime() / (long)1_000_000;
    }

    private static void connectAndRun() throws Exception {
        modbus.connect();

        while (true) {
            modbus.refreshInputs();
            factory.update();
            orderc.update();
            modbus.refreshOutputs();

            Thread.sleep(controlLoopDelay);
        }
    }

    private static Process sfs = null;

    private static void tryToOpenSFS() {
        System.out.println("Simulator not running. Trying to start it.");
        try {
            sfs = Runtime.getRuntime().exec("java -jar sfs.jar");

            if (!sfs.isAlive()) {
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
    public static void main(String[] args) throws Exception {
        /*try {
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
            /*}
        }
        catch (Exception ex) {
        }
        //System.exit(33);*/
        connectAndRun();
    }
}
