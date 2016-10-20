/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import factory.conveyor.Conveyor;

/**
 *
 * @author Alex
 */
public abstract class Cell {
    public String id;
    
    public abstract Conveyor cornerConveyor(int position); // 1= top left, turns clockwise
    public abstract void update();
    
    public abstract void connectWithRightCell(Cell right);
    public abstract void connectWithLeftCell(Cell left);
    
    public static void connect(Cell left, Cell right) {
        left.connectWithRightCell(right);
        right.connectWithLeftCell(left);
    }
}
