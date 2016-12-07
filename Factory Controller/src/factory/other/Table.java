package factory.other;

import factory.*;

public class Table extends Container {
    private final Sensor sensor;
    
    public Table(int sensorId) {
        super(1);
        sensor = new Sensor(sensorId);
    }
    
    public boolean sensorOn() {
        return sensor.on();
    }
}
