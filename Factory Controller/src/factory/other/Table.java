package factory.other;

import control.*;
import factory.*;

public class Table
{
    public Block block;
    private final Sensor sensor;
    
    public Table(int sensorId) {
        sensor = new Sensor(sensorId);
    }
    
    public boolean sensorOn() {
        return sensor.on();
    }
}