package factory.conveyor;

import main.*;
import factory.*;
import java.util.Random;

public class Machine extends Conveyor {

    public enum Type {
        A, B, C;

        public enum Set {
            AB, BC;

            public boolean contains(Machine.Type machine) {
                switch (machine) {
                    case A: return this == AB;
                    case B: return true;
                    case C: return this == BC;
                    default: return false;
                }
            }
        }
    }

    public final Type type;
    private final Sensor ZSensor, zSensor, XSensor, xSensor;
    private final Motor xMotor, zMotor;
    private final Tool tool;

    public Machine(String id, Type type) {
        super(id, 1, 2);

        this.type = type;
        this.tool = new Tool(
                new Sensor(Main.config.getBaseInput(id) + 1),
                new Motor(Main.config.getBaseOutput(id) + 2),
                Main.config.getBaseOutput(id) + 4
        );

        xMotor = new Motor(Main.config.getBaseOutput(id) + 5);
        zMotor = new Motor(Main.config.getBaseOutput(id) + 7);

        XSensor = new Sensor(Main.config.getBaseInput(id) + 2);
        xSensor = new Sensor(Main.config.getBaseInput(id) + 3);
        ZSensor = new Sensor(Main.config.getBaseInput(id) + 4);
        zSensor = new Sensor(Main.config.getBaseInput(id) + 5);
    }

    // DEMO
    boolean last1 = false;
    boolean last2 = false;
    boolean last3 = false;

    @Override
    public void update() {
        super.update();
        tool.update();

        // DEMO
        if (xSensor.on()) {
            last1 = false;
        }
        if (XSensor.on()) {
            last1 = true;
        }
        xMotor.turnOn(last1);

        if (zSensor.on()) {
            last2 = true;
        }
        if (ZSensor.on()) {
            last2 = false;
        }
        zMotor.turnOn(last2);
        
        // DEMO
        if (tool.isIdle()) {
            Tool.Type[] list = Tool.Type.values();
            
            Tool.Type next = list[new Random().nextInt(list.length)];
            long time = (new Random().nextInt(4) + 1) * (long)1000.0;
                        
            tool.selectAndActivate(next, time);
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
    } // TODO: start machining

    @Override
    public boolean isBlockTransferPossible() {
        return true;
    } // TODO: has machining ended

    @Override
    public void blockTransferPrepare() {
    }

    @Override
    public boolean isBlockTransferReady() {
        return true;
    }

    public static class Tool {

        public enum Type {
            T1, T2, T3
        }

        private enum State {
            Idle, Selecting, Machining;
        }
        
        private static final long toolSelectionTime = 12_000;

        private final Sensor toolPresentSensor;
        private final Motor toolSelectMotor;
        private final int toolActivationMotorID;

        private Type currentTool = Type.T1;
        private State state = State.Idle;

        private Type toolSelectTarget;
        private long machiningDuration;
        private long startTime;

        public Tool(Sensor toolPresentSensor, Motor toolSelectMotor, int toolActivationMotorID) {
            this.toolPresentSensor = toolPresentSensor;
            this.toolSelectMotor = toolSelectMotor;
            this.toolActivationMotorID = toolActivationMotorID;
        }

        public void update() {
            switch (state) {
                case Idle: break;
                case Selecting:
                    if (toolPresentSensor.on() && Main.time() - startTime > toolSelectionTime / 2) {
                        toolSelectMotor.turnOff();
                        currentTool = toolSelectTarget;

                        if (machiningDuration != -1) {
                            activate(machiningDuration);
                        }
                        else {
                            state = State.Idle;
                        }
                    }
                    break;
                case Machining:
                    if (Main.time() - startTime > machiningDuration) {
                        Main.modbus.setOutput(toolActivationMotorID, false);
                        state = State.Idle;
                    }
                    break;
            }
        }

        public void select(Type type) {
            if (type != currentTool) {
                toolSelectTarget = type;

                boolean direction;
                switch (currentTool) {
                    case T1:
                        direction = toolSelectTarget == Type.T3;
                        break;
                    case T2:
                        direction = toolSelectTarget == Type.T1;
                        break;
                    case T3:
                        direction = toolSelectTarget == Type.T2;
                        break;
                    default: throw new Error("XXX"); // TODO
                }

                machiningDuration = -1;
                state = State.Selecting;
                startTime = Main.time();
                toolSelectMotor.turnOn(direction);
            }
        }

        public void activate(long duration) {
            machiningDuration = duration;
            state = State.Machining;
            startTime = Main.time();
            Main.modbus.setOutput(toolActivationMotorID, true);
        }

        public void selectAndActivate(Type type, long duration) {
            if (type != currentTool) {
                select(type);
                machiningDuration = duration;
            }
            else {
                activate(duration);
            }
        }

        public boolean isIdle() {
            return state == State.Idle;
        }

    }

}
