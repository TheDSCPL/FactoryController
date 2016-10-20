/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;

import factory.cell.*;

/**
 *
 * @author Alex
 */
public class Factory {
    
    Warehouse cw;
    ParallelCell c1, c3;
    SerialCell c2, c4;
    Assembler ca;
    LoadUnloadBay cb;
    Cell[] cellList;
    
    public Factory() {
        
        // Create cells
        cw = new Warehouse("A");
        c1 = new ParallelCell("Pa");
        c2 = new SerialCell("Sa");
        c3 = new ParallelCell("Pb");
        c4 = new SerialCell("Sb");
        ca = new Assembler("M");
        cb = new LoadUnloadBay("C");
        
        // Connect cells
        cellList = new Cell[] {cw, c1, c2, c3, c4, ca, cb};
        Cell.connect(cellList);
    }
    
    public void update() {
        for (Cell cell : cellList) {
            cell.update();
        }
    }

}
