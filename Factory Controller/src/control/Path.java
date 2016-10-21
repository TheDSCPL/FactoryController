/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.util.*;
import factory.conveyor.*;

/**
 *
 * @author Alex
 */
public class Path {
    public List<Conveyor> path = new ArrayList<>();
    
    public void push(Conveyor c)
    {
        if (c == null)
            return;
        //adds only if the last conveyor in the path is connected to the conveyor that we are trying to add
        if (!path.isEmpty())
        {
            for (Conveyor ci : path.get(path.size() - 1).connections) //checks all of the conveyors connected to the last element of the list
                if (c == ci)
                {
                    path.add(c);
                    return;
                }
            throw new Error("Path has a bug!");
        }
        else
            path.add(c);
    }
    
    public Conveyor current() {
        return path.isEmpty() ? null : path.get(0);
    }
    
    public Conveyor next() {
        return path.size() <= 1 ? null : path.get(1);
    }
    
    public void pop() {
        if (!path.isEmpty()) path.remove(0);
    }
}
