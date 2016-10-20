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
    private final ??? io = ???;
    
    Configuration() {
        try { general.load(new FileInputStream(new File("src/main/config.properties"))); }
        catch (Exception ex) { Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex); }
        
        // Load io map
        io = 
    }
    
    public String getS(String key) {
        return general.getProperty(key);
    }
    
    public int getI(String key) {
        return Integer.parseInt(general.getProperty(key));
    }
    
    
    public int getBaseInput(String id) {
        return 0;
    }
    
    public int getBaseOutput(String id) {
        return 0;
    }
}
