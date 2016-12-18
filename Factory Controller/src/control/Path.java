package control;

import factory.conveyor.*;
import java.util.*;
import static java.util.stream.Collectors.toList;

public final class Path {

    private final List<Conveyor> path = new ArrayList<>();

    /**
     * Pushes the conveyor to the end of the FIFO queue
     *
     * @param c conveyor to be added to the queue
     * @return 
     */
    public Path push(Conveyor c) {
        if (c == null) {
            return this;
        }

        // Adds only if the last conveyor in the path is connected to the conveyor that we are trying to add
        if (!path.isEmpty()) {
            if (!getLast().isConnectedToConveyor(c)) {
                throw new Error("Invalid path");
            }
        }

        path.add(c);

        return this;
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
     * @return 
     */
    public Path append(Path newPath) {
        if (path == null) {
            return this;
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
        return this;
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

    public int length() {
        return path.size();
    }

    public Conveyor get(int i) {
        return path.get(i);
    }

    public boolean contains(Conveyor c) {
        return path.contains(c);
    }
    
    public boolean contains(Conveyor... conveyors) {        
        return Collections.indexOfSubList(path, Arrays.asList(conveyors)) != -1;
    }

    /**
     * @return A new, independent Path object that contains the same Conveyor
     * list as the callee
     */
    public Path copy() {
        Path p = new Path();
        p.append(this);
        return p;
    }
    
    public double timeEstimate() {
        if (length() < 2) {
            return 0;
        }

        double time = 0;

        for (int i = 0; i < length() - 1; i++) {
            time += Conveyor.transferTimeEstimate(path.get(i), path.get(i + 1));
        }

        return time;
    }

    public String toString() {
        return path.stream().map(c -> c.id).collect(toList()).toString();
    }
}
