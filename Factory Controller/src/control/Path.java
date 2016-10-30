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

    public final List<Conveyor> path = new ArrayList<>();

    /**
     * Pushes the conveyor to the end of the FIFO queue
     *
     * @param c conveyor to be added to the queue
     */
    public void push(Conveyor c) {
        if (c == null) {
            return;
        }

        // Adds only if the last conveyor in the path is connected to the conveyor that we are trying to add
        if (!path.isEmpty()) {
            if (!getLast().isConnectedToConveyor(c)) {
                throw new Error("Invalid path");
            }
        }

        path.add(c);

    }
    
    public void push(Conveyor... list) {
        for (Conveyor c : list) {
            push(c);
        }
    }

    /**
     * Appends newPath to the end of this path
     *
     * @param newPath the path to add to the end
     */
    public void append(Path newPath) {
        if (path == null) {
            return;
        }
        
        if (!path.isEmpty() && !newPath.path.isEmpty()) {
            // If next path starts on the same conveyor this stops, remove one
            // of the conveyors, so that it doesn't repeat
            if (getLast() == newPath.getCurrent()) {
                newPath.advance();
            }

            if (!getLast().isConnectedToConveyor(newPath.getCurrent())) {
                throw new Error("Trying to append a path but the two paths are not connected");
            }
        }

        path.addAll(newPath.path);
    }

    public Conveyor getCurrent() {
        return path.isEmpty() ? null : path.get(0);
    }

    public Conveyor getNext() {
        return hasNext() ? path.get(1) : null;
    }

    public Conveyor getLast() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }
    
    public boolean hasNext() {
        return path.size() > 1;
    }

    public void advance() {
        if (!path.isEmpty()) {
            path.remove(0);
        }
    }
    
    public String toString() {
        return path.stream().map(c -> c.id).reduce("Path {", (a,b) -> (a + ", " + b)) + "}";
    }
}
