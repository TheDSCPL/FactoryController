package main;

import coms.*;
import config.Configuration;
import control.*;
import factory.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
  [.]  Rotator/[Mover] conveyor optimization: automatically rotate back to horizontal position to speed up block traveling time (Class: Rotator, Mover)
  [X]  Transformation sequences (Class: TransformationManager)
  [X]  Tool class for selecting and activating tools in machines (Class: Machine)
  [X]  Algorithm for selecting which transfer partner should be chosen first (Class: Conveyor)
  [.]  Parallel cell processing and optimization (Class: ParalleCell - last thing: entry of blocks on cell is not prioritized)
  [X]  Tool pre-selection on Parallel cell (Class: ParallelCell)
  [.]  Statistics module (Class: Main, Statistics)

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
    public static final long controlLoopDelay = (long) config.getI("controlLoopDelay");

    private static final Thread controlThread = new Thread(Main::controlLoop);

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
        controlThread.setDaemon(true);
        controlThread.start();
        inputLoop();
    }

    public static void inputLoop() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            System.out.print("> ");
            String input = reader.readLine();
            String output;

            if (!input.equals("\n") && !input.isEmpty()) {
                switch (input) {
                    case "factory":
                        output = factory.toString();
                        break;
                    case "orders":
                        output = orderc.toString();
                        break;
                    case "exit":
                        return;
                    default:
                        if (input.startsWith("O")) {
                            output = orderc.orderInfo(input.replaceFirst("^O", ""));
                        }
                        else {
                            output = stats.processCmd(input);
                        }
                        break;
                }

                if (output != null) {
                    System.out.println(output);
                }
                else {
                    System.out.println("Invalid command " + input);
                }                
            }
        }
    }

    public static void controlLoop() {
        try {
            modbus.connect();

            while (true) {
                modbus.refreshInputs();
                factory.update();
                orderc.update();
                modbus.refreshOutputs();

                Thread.sleep(controlLoopDelay);
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
