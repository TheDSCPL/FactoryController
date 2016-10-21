/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import factory.conveyor.Conveyor;

/**
 * Represents a block.
 * @author Luis Paulo
 * @auhor Alex
 */
public class Block
{
    Block(Block.Type type)
    {
        this.type=type;
    }
    
    public enum Type
    {
        P1,P2,P3,P4,P5,P6,P7,P8,P9;
    }
    
    public Type type;
    public Path path = new Path();
    
    public Conveyor getNextConveyor()
    {
        //use Path somehow
        return null;
    }
}
