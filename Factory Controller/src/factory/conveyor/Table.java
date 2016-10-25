/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.conveyor;

import factory.*;
import control.*;

/**
 *
 * @author luisp
 */
public class Table
{
    public Block block;
    private final Sensor sensor;
    
    public Table(int sensorId)
    {
        sensor = new Sensor(sensorId);
    }
    
    /*Block.Type getBlockType()
    {
        if(block == null)
            return null;
        return block.type;
    }*/
}
