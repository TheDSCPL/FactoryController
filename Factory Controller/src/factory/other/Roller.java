package factory.other;

import main.Main;

public class Roller {
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
