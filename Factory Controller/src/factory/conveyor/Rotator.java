package factory.conveyor;

import factory.other.*;
import main.*;

public class Rotator extends Conveyor {

    private final Motor rotateMotor;
    private final Sensor rotateSensorMinus;
    private final Sensor rotateSensorPlus;

    private final boolean prefersHorizontalOrientation;
    private long timeSinceIdle = Main.time();

    public Rotator(String id) {
        this(id, true);
    }

    public Rotator(String id, boolean prefersHorizontalOrientation) {
        super(id, 1, 4);
        rotateMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        rotateSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        rotateSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
        this.prefersHorizontalOrientation = prefersHorizontalOrientation;
    }

    @Override
    public void update() {
        super.update();

        if (prefersHorizontalOrientation) {
            if (isIdle()) {
                if (rotateSensorPlus.on()) {
                    rotateMotor.turnOff();
                }
                else if (Main.time() - timeSinceIdle > Main.controlLoopDelay * 50) {
                    // Some delay ("*50") to allow for neighboring containers to register themselves as transferPartners
                    rotateMotor.turnOnPlus();
                }
            }
            else {
                timeSinceIdle = Main.time();
            }
        }
    }

    @Override
    protected boolean transferMotorDirection(Conveyor partner, boolean sending) {
        boolean leftOrTop = partner == connections[0] || partner == connections[1];

        if (sending) {
            return leftOrTop;
        }
        else {
            return !leftOrTop;
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
        rotateMotor.turnOn(rotateMotorDirection());
    }

    @Override
    protected boolean isBlockTransferReady() {
        boolean ready
                = (rotateMotorDirection() && rotateSensorPlus.on())
                  || (!rotateMotorDirection() && rotateSensorMinus.on());

        if (ready) {
            timeSinceIdle = Main.time();
            rotateMotor.turnOff();
        }
        return ready;
    }

    private boolean rotateMotorDirection() {
        return transferPartner == connections[0] || transferPartner == connections[2];
    }

}
