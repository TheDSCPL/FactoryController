/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import factory.conveyor.*;

/**
 *
 * @author Alex
 */
public abstract class Cell {
    public String id;
    public Conveyor[] conveyorList;
    
    public Cell(String id) {
        this.id = id;
    }
    
    public abstract Conveyor getCornerConveyor(int position); // 0 = top left, turns clockwise    
    public abstract void connectWithRightCell(Cell right);
    public abstract void connectWithLeftCell(Cell left);
    
    public abstract void update();
    
    public static void connect(Cell... cells) {
        if (cells.length == 0) { return; }
        for (int i = 1; i < cells.length; i++) {
            Cell.connect(cells[i - 1], cells[i]);
        }
    }
    public static void connect(Cell left, Cell right) {
        left.connectWithRightCell(right);
        right.connectWithLeftCell(left);
    }
}
