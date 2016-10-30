package factory.conveyor;

import control.*;
import control.order.*;
import factory.*;
import main.*;

public class Pusher extends Conveyor {

    private final Motor pushMotor;
    private final Sensor pushSensorPlus;
    private final Sensor pushSensorMinus;
    private boolean lastUpdateWasIdle;
    public boolean blockPlacedManually;
    private State pusherState = State.Idle;

    private enum State {
        Idle, Pushing, Retracting
    }

    public Pusher(String id) {
        super(id, 1, 2);
        pushMotor = new Motor(Main.config.getBaseOutput(id) + 2);
        pushSensorPlus = new Sensor(Main.config.getBaseInput(id) + 1);
        pushSensorMinus = new Sensor(Main.config.getBaseInput(id) + 2);
        lastUpdateWasIdle = true;
    }

    @Override
    public void update() {
        super.update();

        // Detect if block has been placed manually by a person
        if (lastUpdateWasIdle && isIdle() && !hasBlock() && isPresenceSensorOn(0)) {
            // Create block
            Block block = new Block(Block.Type.Unknown);
            block.path.push(this);

            // Place block on this conveyor
            placeBlock(block, 0);
            blockPlacedManually = true;
        }
        else {
            blockPlacedManually = false;
        }

        lastUpdateWasIdle = isIdle();

        // Push block
        switch (pusherState) {
            case Pushing:
                if (pushSensorMinus.on()) {
                    pushMotor.turnOn(true);
                    pusherState = State.Retracting;
                }
                break;
            case Retracting:
                if (pushSensorPlus.on()) {
                    pushMotor.turnOff();
                    pusherState = State.Idle;
                    
                    Block block = removeBlock(0);
                    block.completeOrder();                    
                }
                break;
        }
    }

    @Override
    public boolean transferMotorDirection() {
        if (isSending()) {
            return transferPartner == connections[0];
        }
        else if (isReceiving()) {
            return transferPartner == connections[1];
        }
        else {
            throw new Error("transferMotorDirection called when not transfering block");
        }
    }

    @Override
    public void blockTransferFinished() {

        // Push if necessary
        if (getBlock(0) != null) {
            Block block = getBlock(0);

            // Block has stopped here and has an Unload order
            if (!block.path.hasNext() && block.order != null) {
                Order order = block.order;

                if (order instanceof UnloadOrder) {
                    pusherState = State.Pushing;
                    pushMotor.turnOn(false);
                }
            }
        }
    }

    @Override
    public boolean isBlockTransferPossible() {
        return pusherState == State.Idle;
    }

    @Override
    public void blockTransferPrepare() {

    }

    @Override
    public boolean isBlockTransferReady() {
        return true;
    }

}
