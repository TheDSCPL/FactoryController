/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.*;
import java.util.*;

/**
 * @author Luis Paulo
 * @author Alex
 */
public class Configuration {
    
    private final Properties general = new Properties();
    public final Map<String, Integer> inputIDs = new HashMap<>();
    public final Map<String, Integer> outputIDs = new HashMap<>();
    
    private static final String CSVFileName = "src/config/io.csv";
    private static final String PropertiesFileName = "src/config/config.properties";
    
    public Configuration() {
        
        // Load config file
        try { general.load(new FileInputStream(new File(PropertiesFileName))); }
        catch (Exception ex) { throw new Error("Could not access config file: " + ex); }
                
        // Load io map
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(CSVFileName));
            String line;
            
            for (int lineNumber = 1; (line = br.readLine()) != null; lineNumber++)
            {
                String[] values = line.split(";");
                int newValue;
                
                if (values.length != 3) throw new Error("Line " + lineNumber + " doesn't have 3 values.");                
                
                try { newValue = Integer.parseInt(values[2]); }
                catch(NumberFormatException ne) { throw new Error("Non-integer 3rd value on line " + lineNumber); }
                
                switch(values[1])
                {
                    case "I":
                        if (newValue < inputIDs.getOrDefault(values[0], Integer.MAX_VALUE)) inputIDs.put(values[0], newValue);
                        break;
                    case "O":
                        if (newValue < outputIDs.getOrDefault(values[0], Integer.MAX_VALUE)) outputIDs.put(values[0], newValue);
                        break;
                    default:
                        throw new Error("Invalid I/O classifier \"" + values[1] + "\" at line " + lineNumber);
                }
            }
        }
        catch(Exception ex) { throw new Error("Could not access IO file: " + ex); }
    }
    
    public String getS(String key) {
        return general.getProperty(key);
    }
    
    public int getI(String key) {
        return Integer.parseInt(general.getProperty(key));
    }
    
    
    public int getBaseInput(String id) {
        int ret = inputIDs.getOrDefault(id, -1);
        if (ret < 0) throw new Error(id + " is an invalid id.");
        return ret;
    }
    
    public int getBaseOutput(String id) {
        int ret = outputIDs.getOrDefault(id, -1);
        if (ret < 0) throw new Error(id + " is an invalid id.");
        return ret;
    }
}
