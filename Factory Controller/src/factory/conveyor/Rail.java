package factory.conveyor;

import factory.other.*;
import main.*;

public class Rail extends Conveyor {

    private final Motor railMotor;
    private final Sensor railSensorMinus;
    private final Sensor railSensorPlus;

    private final boolean prefersLeftPosition;
    private long timeSinceIdle = Main.time();

    public Rail(String id) {
        this(id, true);
    }

    public Rail(String id, boolean prefersLeftPosition) {
        super(id, 1, 4);
        railMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        railSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        railSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
        this.prefersLeftPosition = prefersLeftPosition;
    }

    @Override
    public void update() {
        super.update();

        if (prefersLeftPosition) {
            if (isIdle()) {
                if (railSensorPlus.on()) {
                    railMotor.turnOff();
                }
                else if (Main.time() - timeSinceIdle > Main.controlLoopDelay * 50) {
                    // Some delay ("*50") to allow for neighboring containers to register themselves as transferPartners
                    railMotor.turnOnPlus();
                }
            }
            else {
                timeSinceIdle = Main.time();
            }
        }
    }

    @Override
    protected boolean transferMotorDirection(Conveyor partner, boolean sending) {
        boolean top = partner == connections[0]
                      || partner == connections[1];

        if (sending) {
            return top;
        }
        else {
            return !top;
        }
    }

    @Override
    protected void blockTransferFinished() {
    }

    @Override
    protected boolean isBlockTransferPossible() {
        return true;
    }

    @Override
    protected void blockTransferPrepare() {
        railMotor.turnOn(railMotorDirection());
    }

    @Override
    protected boolean isBlockTransferReady() {
        boolean ready
                = (railMotorDirection() && railSensorPlus.on())
                  || (!railMotorDirection() && railSensorMinus.on());

        if (ready) {
            timeSinceIdle = Main.time();
            railMotor.turnOff();
        }
        return ready;
    }

    private boolean railMotorDirection() {
        return transferPartner == connections[0] || transferPartner == connections[3];
    }

    /*public long transferTimeEstimate(Conveyor from, Conveyor to) {
        long added = 0;
        
        if (fro)
        
        return super.transferTimeEstimate(from, to) + added;
    }*/
}