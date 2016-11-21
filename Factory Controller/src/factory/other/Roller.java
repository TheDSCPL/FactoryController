package factory.other;

import factory.*;
import main.*;

public class Roller extends BlockContainer{
    public final String id;
    private final Sensor sensor;
    
    public Roller(String id) {
        this.id = id;
        sensor = new Sensor(Main.config.getBaseInput(id) + 0);
    }
    
    public boolean isFull() {
        return sensor.on();
    }
}
