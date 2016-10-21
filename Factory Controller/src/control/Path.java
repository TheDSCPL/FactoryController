/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.util.*;

/**
 *
 * @author Alex
 */
public class Path {
    public List<String> path = new ArrayList<>();
    
    public void push(String id) {
        path.add(id);
    }
    
    public String current() {
        return path.isEmpty() ? null : path.get(0);
    }
    
    public String next() {
        return path.size() < 2 ? null : path.get(1);
    }
    
    public void pop() {
        if (!path.isEmpty()) path.remove(0);
    }
}
