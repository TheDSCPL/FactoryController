package main;

import coms.*;
import config.Configuration;
import control.*;
import factory.*;
import java.io.IOException;
import transformation.*;

// PULL -> WORK -> ADD -> COMMIT -> PULL -> PUSH

/*
Major TODO list:

Luis:
  [.]  Gantry (Class: Gantry)
  [ ]  Assembler cell processing and optimization (Class: Assembler)

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
  [ ]  Parallel cell opimization: entry of blocks on cell is not prioritized over block being transfer-rotated already on cell
  [ ]  Parallel cell opimization: path.length > 3, not 2
  [ ]  Order processing algorithm and distribution by the various cells (Class: Factory)

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
    public static final Statistics stats = new Statistics();
    private static final Console console = new Console();
    public static final long controlLoopDelay = (long) config.getI("controlLoopDelay");

    /**
     * @return Time in milliseconds
     */
    public static long time() {
        return System.nanoTime() / (long) 1_000_000;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        console.setDaemon(true);
        console.start();
        controlLoop();
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

    private static void controlLoop() {
        try {
            modbus.connect();

            while (true) {
                modbus.refreshInputs();
                factory.update();
                orderc.update();
                modbus.refreshOutputs();

                processCommand();
                Thread.sleep(controlLoopDelay);
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
