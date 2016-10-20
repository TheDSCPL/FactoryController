/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class Configuration {
    
    private final Properties general = new Properties();
    public final Map<String, Integer> inputIds = new HashMap<>();
    public final Map<String, Integer> outputIds = new HashMap<>();
    
    private static final String CSVFileName = "src/main/io.csv";
    private static final String PropertiesFileName = "src/main/config.properties";
    
    
    Configuration() {
        try { general.load(new FileInputStream(new File(PropertiesFileName))); }
        catch (Exception ex) { Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex); }
        
        List<String> errorList = new ArrayList<>();
        
        // Load io map
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(CSVFileName));
            String line;
            for( int lineNumber=1; (line = br.readLine()) != null; lineNumber++)
            {
                String[] values = line.split(";");
                if(values.length != 3)
                    throw new Exception("Line " + lineNumber + " doesn't have 3 values.");
                //decide if it is an input or and output
                
                int newValue = Integer.parseInt(values[2]);
                
                switch(values[1])
                {
                    case "I":
                        if (newValue < inputIds.getOrDefault(values[0], Integer.MAX_VALUE)) {
                            inputIds.put(values[0], Integer.parseInt(values[2]));
                        }
                        break;
                    case "O":
                        if (newValue < outputIds.getOrDefault(values[0], Integer.MAX_VALUE)) {
                            outputIds.put(values[0], Integer.parseInt(values[2]));
                        }
                        break;
                    default:
                      throw new Exception("Invalid I/O classifier \"" + values[1] + "\" at line " + lineNumber);  
                }
            }
        }
        catch(Exception e)
        {
            System.err.println( "Error while opening/reading the csv file." + "\n" +
                                "Exception msg: " + e.getMessage() + "\n" +
                                "Program will exit now.");
            try{System.in.read();}catch(Exception ignored){}
            System.exit(10);
        }
    }
    
    public String getS(String key) {
        return general.getProperty(key);
    }
    
    public int getI(String key) {
        return Integer.parseInt(general.getProperty(key));
    }
    
    
    public int getBaseInput(String id) {
        return inputIds.getOrDefault(id, -1);
    }
    
    public int getBaseOutput(String id) {
        return outputIds.getOrDefault(id, -1);
    }
}
