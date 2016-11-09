package factory.conveyor;

import control.*;
import control.order.*;
import factory.*;
import main.*;
import transformation.*;

public class Machine extends Conveyor {

    public enum Type {
        A, B, C;

        public enum Set {
            /**
             * Set of machines on SerialCell
             */
            AB,
            /**
             * Set of machines on ParallelCell
             */
            BC;

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

    private boolean holdBlock = false;

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

    @Override
    public void update() {
        super.update();
        tool.update();

        // Position machine head correctly (only done on initialization)
        xMotor.control(!xSensor.on(), true);
        zMotor.control(!zSensor.on(), false);
        
        // Tool has finished
        if (holdBlock && tool.isIdle()) {
            
            // Update block type
            getOneBlock().applyNextTransformation();
            
            // Start next machining cycle here, or release block
            startMachiningIfNecessary();
        }

    }

    public boolean canPreSelectTool() {
        return tool.isIdle();
    }
    public void preSelectTool(Tool.Type type) {
        tool.select(type);
    }
    
    private void startMachiningIfNecessary() {
        Block block = getOneBlock();
        holdBlock = false;

        if (block.order instanceof MachiningOrder) {
            if (block.hasNextTransformation()) {
                Transformation next = block.getNextTransformation();

                // If block can be machined here
                if (next.machine == type) {
                    tool.selectAndActivate(next.tool, next.duration);
                    holdBlock = true;
                }
            }
        }
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
        startMachiningIfNecessary();
    }

    @Override
    protected boolean isBlockTransferPossible() {
        return !holdBlock;
    }

    @Override
    protected void blockTransferPrepare() {

    }

    @Override
    protected boolean isBlockTransferReady() {
        return true;
    }

    @Override
    public boolean isIdle() {
        return super.isIdle() && tool.isIdle();
    }

    public static class Tool {

        public enum Type {
            T1, T2, T3
        }

        private enum State {
            Idle, Selecting, Machining;
        }

        private static final long toolSelectionTime = ((360 / 3) / (long) Main.config.getI("tool.rotateSpeed")) * 1000;

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
                        state = State.Idle;

                        if (machiningDuration != -1) {
                            activate(machiningDuration);
                            //System.out.format("[tool] update: done selecting tool=%s, start machining duration=%d%n", toolSelectTarget, machiningDuration);
                        }
                        else {
                            //System.out.format("[tool] update: done selecting tool=%s, no machining%n", toolSelectTarget);
                        }
                    }
                    break;
                case Machining:
                    if (Main.time() - startTime > machiningDuration) {
                        //System.out.format("[tool] update: done machining duration=%d%n", machiningDuration);
                        Main.modbus.setOutput(toolActivationMotorID, false);
                        state = State.Idle;
                    }
                    break;
            }
        }

        public void select(Type type) {
            //System.out.format("[tool] select: tool=%s%n", type);
            
            // If already selecting that tool, do nothing
            if (state == State.Selecting && toolSelectTarget == type) {
                //System.out.format("[tool] select: do nothing%n");
                return;
            }
            
            // If doing something else, throw error
            if (state != State.Idle) {
                throw new Error("select called on tool when tool is not on Idle state");
            }
            
            // If Idle and needs to select new tool, start tool selecting process
            if (state == State.Idle && currentTool != type) {
                //System.out.format("[tool] select: start selection%n");
                toolSelectTarget = type;

                boolean direction;
                switch (currentTool) {
                    case T1:
                        direction = toolSelectTarget == Type.T2;
                        break;
                    case T2:
                        direction = toolSelectTarget == Type.T3;
                        break;
                    case T3:
                        direction = toolSelectTarget == Type.T1;
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
            switch (state) {
                case Idle:
                    //System.out.format("[tool] activate: start machining with duration=%d%n", duration);
                    machiningDuration = duration;
                    state = State.Machining;
                    startTime = Main.time();
                    Main.modbus.setOutput(toolActivationMotorID, true);
                    break;
                case Selecting:
                    //System.out.format("[tool] activate: still selecting, set duration=%d%n", duration);
                    machiningDuration = duration;
                    break;
                case Machining:
                    throw new Error("activate called on tool when machining is already in progress");
            }

        }

        public void selectAndActivate(Type type, long duration) {
            //System.out.format("[tool] selectAndActivate: tool=%s duration=%d%n", type, duration);
            select(type);
            activate(duration);
        }

        public boolean isIdle() {
            return state == State.Idle;
        }

    }

}
