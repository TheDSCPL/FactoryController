package main;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Console extends Thread {

    private String input;
    private String output;

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String in = null;
            try {
                in = reader.readLine();
            }
            catch (IOException ex) {
                Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (in != null && !in.equals("\n") && !in.isEmpty()) {
                setInput(in);

                while (getOutput() == null) {
                    try {
                        Thread.sleep(Main.controlLoopDelay);
                    }
                    catch (InterruptedException ex) {
                        Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                System.out.println(getOutput());
                setInput(null);
                setOutput(null);
            }
        }
    }

    public synchronized String getInput() {
        return input;
    }

    public synchronized void setInput(String i) {
        input = i;
    }

    public synchronized String getOutput() {
        return output;
    }

    public synchronized void setOutput(String o) {
        output = o;
    }
}
