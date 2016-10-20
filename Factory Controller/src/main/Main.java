/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            modbus.connect();
            
            Motor m1 = new Motor(9);
            Sensor s11 = new Sensor(4);
            Sensor s12 = new Sensor(5);
            
            Motor m2 = new Motor(196);
            Sensor s21 = new Sensor(141);
            Sensor s22 = new Sensor(142);
            
            while (true) {
                modbus.refreshInputs();
                
                if (s11.on()) { m1.turnOnMinus(); }
                else if (s12.on()) { m1.turnOnPlus(); }
                
                if (s21.on()) { m2.turnOnMinus(); }
                else if (s22.on()) { m2.turnOnPlus(); }
                
                modbus.refreshOutputs();
                
                Thread.sleep(100);
            }

        } catch (Exception ex) { ex.printStackTrace(); } 
    }
    
}
