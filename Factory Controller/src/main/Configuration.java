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
 * @author Luis Paulo
 * @author Alex
 */
public class Configuration {
    
    private final Properties general = new Properties();
    public final Map<String, Integer> inputIds = new HashMap<>();
    public final Map<String, Integer> outputIds = new HashMap<>();
    
    private static final String CSVFileName = "src/main/io.csv";
    private static final String PropertiesFileName = "src/main/config.properties";
    
    Configuration() {
        try
        {
            general.load(new FileInputStream(new File(PropertiesFileName)));
        }
        catch (Exception ex)
        {
            Logger.getLogger(Configuration.class.getSimpleName()).log(Level.SEVERE, null, ex);
        }
        
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
                    errorList.add("Line " + lineNumber + " doesn't have 3 values.");
                //decide if it is an input or and output
                
                int newValue = Integer.parseInt(values[2]);
                
                try
                {
                    switch(values[1])
                    {
                        case "I":
                            if (newValue < inputIds.getOrDefault(values[0], Integer.MAX_VALUE))
                                inputIds.put(values[0], Integer.parseInt(values[2]));
                            break;
                        case "O":
                            if (newValue < outputIds.getOrDefault(values[0], Integer.MAX_VALUE)) //only add if it is smaller than the value that is already in that key or if it is the first value for that key
                                outputIds.put(values[0], Integer.parseInt(values[2]));
                            break;
                        default:
                            errorList.add("Invalid I/O classifier \"" + values[1] + "\" at line " + lineNumber);
                    }
                }
                catch(NumberFormatException ne)
                {
                    errorList.add("Non-integer 3rd value on line " + lineNumber);
                }
            }
        }
        catch(FileNotFoundException e)
        {
            System.err.println( "Couldn't open file \"" + CSVFileName + "\"." + System.lineSeparator() +
                                "Application will now exit!" );
            try{System.in.read();}catch(Exception ignored){}
            System.exit(9);
        }
        catch(Exception e)
        {
            System.err.println( "Major error occurred while opening/reading file." + System.lineSeparator() +
                                "Application will now exit!" );
            try{System.in.read();}catch(Exception ignored){}
            System.exit(10);
        }
        if(errorList.size()>0)
        {
            System.err.println( errorList.size()==1?"An error":"Some errors" + " occurred while reading the file:" );
            for(String s : errorList)
                System.err.println(s);
            System.err.print(   "It is not recommended to use the program like this." + System.lineSeparator() +
                                "Type \"Y\" if you want to continue or type anything else if you don't: ");
            String answer = new Scanner(System.in).next();
            if(answer != "Y")
                System.exit(11);
        }
    }
    
    public String getS(String key) {
        return general.getProperty(key);
    }
    
    public int getI(String key) {
        return Integer.parseInt(general.getProperty(key));
    }
    
    
    public int getBaseInput(String id) {
        int ret = inputIds.getOrDefault(id, -1);
        if( ret <0 )
            throw new Error( "FATAL ERROR: ( getBaseInput(String) )\"" + id + "\" is an invalid id.");
        return ret;
    }
    
    public int getBaseOutput(String id) {
        int ret = outputIds.getOrDefault(id, -1);
        if( ret <0 )
            throw new Error( "FATAL ERROR: ( getBaseOutput(String) )\"" + id + "\" is an invalid id.");
        return ret;
    }
}
