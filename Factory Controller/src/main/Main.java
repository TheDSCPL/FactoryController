/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main; // PULL -> WORK -> PULL -> PUSH

import coms.*;
import factory.*;

/**
 *
 * @author Alex
 */
public class Main {
    
    public static final Configuration config = new Configuration();
    public static final ModbusMaster modbus = new ModbusMaster();
    public static final Factory factory = new Factory();
    public static final TransformationManager transfm = new TransformationManager();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            modbus.connect();
            
            while (true) {
                modbus.refreshInputs();
                factory.update();
                modbus.refreshOutputs();
                
                Thread.sleep(100);
            }

        } catch (Exception ex) { ex.printStackTrace(); } 
    }
    
}
