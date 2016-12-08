package factory.conveyor;

import control.*;
import control.order.*;
import factory.other.Motor;
import factory.other.Roller;
import factory.other.Sensor;
import main.*;

public class Pusher extends Conveyor {

    public final Roller roller;
    private final Motor pushMotor;
    private final Sensor pushSensorPlus;
    private final Sensor pushSensorMinus;
    private State pusherState = State.Idle;

    private enum State {
        Idle, WaitingForSpace, Pushing, Retracting
    }

    public Pusher(String id, String rollerid) {
        super(id, 1, 2);
        roller = new Roller(rollerid);
        pushMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        pushSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        pushSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
    }

    @Override
    public void update() {
        super.update();

        // Push block
        switch (pusherState) {
            case WaitingForSpace:
                if (!roller.isFull()) {
                    startPushing();
                }
                break;
            case Pushing:
                if (pushSensorMinus.on()) {
                    startRetracting();
                }
                break;
            case Retracting:
                if (pushSensorPlus.on()) {
                    stopPusher();
                    Block block = removeBlock(0);
                    block.completeOrder();
                    Main.stats.inc(id, Statistics.Type.BlocksPushed, block.type);
                }
                break;
        }
    }

    private void startPushing() {
        pusherState = State.Pushing;
        pushMotor.turnOnMinus();
    }

    private void startRetracting() {
        pusherState = State.Retracting;
        pushMotor.turnOnPlus();
    }

    private void stopPusher() {
        pusherState = State.Idle;
        pushMotor.turnOff();

    }

    @Override
    protected boolean transferMotorDirection(Conveyor partner, boolean sending) {
        if (sending) {
            return partner == connections[0];
        }
        else {
            return partner == connections[1];
        }
    }

    @Override
    protected void blockTransferFinished() {

        // Push if necessary
        if (hasBlock()) {
            Block block = getOneBlock();

            // Block has stopped here, has an Unload order and roller has space
            if (!block.path.hasNext() && (block.order instanceof UnloadOrder)) {
                if (roller.isFull()) {
                    pusherState = State.WaitingForSpace;
                }
                else {
                    startPushing();
                }
            }
            // Block has stopped here, is a stacked block and roller has space
            // Do not go to waiting for space if block is stacked, because block might go to another conveyor
            else if (!block.path.hasNext() && block.isStacked() && !roller.isFull()) {
                startPushing();
            }
        }
    }

    @Override
    protected boolean isBlockTransferPossible() {
        return pusherState == State.Idle;
    }

    @Override
    protected void blockTransferPrepare() {

    }

    @Override
    protected boolean isBlockTransferReady() {
        return true;
    }
}
