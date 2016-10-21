/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell.assemb;

import factory.*;
import main.*;

/**
 *
 * @author luisp
 */
public class Gantry
{
    // TODO: style inconsistency: other neg/pos motors are written with lower/upper case xX yY zZ
    public final Motor posXMotor, negXMotor, posYMotor, negYMotor, posZMotor, negZMotor;
    /**
     * 
     * @param id id of the Assembler where this will be
     */
    Gantry(String id)
    {
        negXMotor = new Motor(Main.config.getBaseOutput(id) + 0);
        posXMotor = new Motor(Main.config.getBaseOutput(id) + 1);
        negYMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        posYMotor = new Motor(Main.config.getBaseOutput(id) + 3);
        posZMotor = new Motor(Main.config.getBaseOutput(id) + 4);
        negZMotor = new Motor(Main.config.getBaseOutput(id) + 5);
    }
    
}