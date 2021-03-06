package main;

import coms.*;
import config.*;
import control.*;
import factory.*;
import java.io.*;
import transformation.*;

// PULL -> WORK -> ADD -> COMMIT -> PULL -> PUSH

/*
Major TODO list:

Luis:
  [X]  Gantry (Class: Gantry)
  [.]  Assembler cell processing and optimization (Class: Assembler)
  [ ]  "TODO L:" in code (Class: N/A)

Alex:
  [X]  Serial cell processing and optimization (Class: SerialCell)
  [X]  Tool pre-selection on Serial cell (Class: SerialCell)
  [\]  Linear conveyors with multiple blocks (Class: Conveyor)
  [X]  Rotator/Mover conveyor optimization: automatically rotate back to horizontal position to speed up block traveling time (Class: Rotator, Mover)
  [X]  Transformation sequences (Class: TransformationManager)
  [X]  Tool class for selecting and activating tools in machines (Class: Machine)
  [X]  Algorithm for selecting which transfer partner should be chosen first (Class: Conveyor)
  [X]  Parallel cell processing and optimization (Class: ParalleCell)
  [X]  Tool pre-selection on Parallel cell (Class: ParallelCell)
  [X]  Statistics module + thread safety! (Class: Main, Statistics)
  [X]  "Select called on tool when tool is not on Idle state" tool pre-selection algorithm bug (Class: Machine)
  [X]  Rework parallel cell processing algorithm: currently it is not taking advantage of parallelism as much as it should
  [X]  Order processing algorithm and distribution by the various cells (Class: Optimizer)
  [X]  "TODO A:" in code (Class: N/A)

Unassigned:
   -

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
    public static final Optimizer optimizer = new Optimizer();
    public static final Statistics stats = new Statistics();
    public static final Console console = new Console();
    public static final long controlLoopDelay = (long) config.getI("controlLoopDelay");

    /**
     * @return Time in milliseconds
     */
    public static long time() {
        return System.nanoTime() / (long) 1_000_000;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Factory controller ready");
        console.setDaemon(true);
        console.start();
        controlLoop();
    }

    private static boolean alreadyTriedToOpen = false;

    private static void controlLoop() {
        try {
            modbus.connect();

            while (true) {
                modbus.refreshInputs();
                factory.update();
                orderc.update();
                optimizer.distributeNextOrder();
                modbus.refreshOutputs();

                processCommand();
                Thread.sleep(controlLoopDelay);
            }
        }
        catch (java.net.ConnectException ex) {
            if (alreadyTriedToOpen) {
                System.err.println("Couldn't open SFS! Halting.");
                return;
            }
            alreadyTriedToOpen = true;
            System.out.println("SFS not running. Attempting to run it...");
            Runtime rt = Runtime.getRuntime();
            try {
                Process pr = rt.exec("java -jar SFS.jar");
                controlLoop();
            }
            catch (Exception ex1) {
                System.err.println("Couldn't open SFS! Halting.");
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static void processCommand() {
        String input = console.getInput();
        String output;

        if (input != null) {
            switch (input) {
                case "factory":
                    output = factory.toString();
                    break;
                case "orders":
                    output = orderc.toString();
                    break;
                default:
                    if (input.startsWith("O")) {
                        output = orderc.orderInfo(input.replaceFirst("^O", ""));
                    }
                    else {
                        output = stats.processCmd(input);
                    }
            }

            console.setOutput(output != null ? output : "Invalid command " + input);
        }
    }
}
