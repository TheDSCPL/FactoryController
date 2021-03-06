package factory.conveyor;

import control.*;
import control.order.*;
import factory.other.Motor;
import factory.other.Sensor;
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

        tool.machineID = id;

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

            // Update stats
            Main.stats.inc(id, Statistics.Type.TransformationsOperated, getOneBlock().getNextTransformation());
            Main.stats.inc(id, Statistics.Type.TotalTimeInOperation, (int) getOneBlock().getNextTransformation().duration);

            // Update block type
            getOneBlock().applyNextTransformation();

            // Start next machining cycle here, or release block
            startMachiningIfNecessary();
        }

    }

    public boolean canPreSelectTool() {
        return tool.canSelect();
    }

    public void preSelectTool(Tool.Type type) {
        if (id.startsWith("P")) {
            //System.out.format("[machine %s] pre-select tool %s%n", id, type);
        }
        tool.select(type);
    }

    private void startMachiningIfNecessary() {
        Block block = getOneBlock();
        holdBlock = false;

        if (id.startsWith("P")) {
            //System.out.format("[machine %s] startMachiningIfNecessary %s%n", id, block);
        }
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

    public boolean isMachining()
    {
        return tool.isMachining();
    }
    
    public static class Tool {

        public String machineID;

        public enum Type {
            T1, T2, T3
        }

        private enum State {
            Idle, Selecting, Machining;
        }

        private static final long toolSelectionTime = ((360 / Type.values().length) / (long) Main.config.getI("timing.tool.rotationSpeed")) * 1000;

        private final Sensor toolPresentSensor;
        private final Motor toolSelectMotor;
        private final int toolActivationMotorID;

        private Type currentTool = Type.T1;
        private State state = State.Idle;

        private Type toolSelectTarget;
        private long machiningDuration;
        private long stopTime;

        public Tool(Sensor toolPresentSensor, Motor toolSelectMotor, int toolActivationMotorID) {
            this.toolPresentSensor = toolPresentSensor;
            this.toolSelectMotor = toolSelectMotor;
            this.toolActivationMotorID = toolActivationMotorID;
        }

        public void update() {
            switch (state) {
                case Idle: break;
                case Selecting:
                    if (toolPresentSensor.on() && Main.time() > stopTime) {
                        toolSelectMotor.turnOff();
                        currentTool = toolSelectTarget;
                        state = State.Idle;

                        if (machiningDuration != -1) {
                            activate(machiningDuration);
                            if (machineID.startsWith("P")) {
                                //System.out.format("[tool %s] update: done selecting tool=%s, start machining duration=%d%n", machineID, toolSelectTarget, machiningDuration);
                            }
                        }
                        else if (machineID.startsWith("P")) {
                            //System.out.format("[tool %s] update: done selecting tool=%s, no machining%n", machineID, toolSelectTarget);
                        }
                    }
                    break;
                case Machining:
                    if (Main.time() > stopTime) {
                        if (machineID.startsWith("P")) {
                            //System.out.format("[tool %s] update: done machining duration=%d%n", machineID, machiningDuration);
                        }
                        Main.modbus.setOutput(toolActivationMotorID, false);
                        state = State.Idle;
                    }
                    break;
            }
        }

        public void select(Type type) {
            if (machineID.startsWith("P")) {
                //System.out.format("[tool %s] select: tool=%s%n", machineID, type);
            }

            // If machining, throw error
            if (state == State.Machining) {
                throw new Error("Tool::select called when tool is machining");
            }

            // If Idle and needs to select new tool, start tool selecting process
            if (state == State.Idle && currentTool != type) {
                startSelection(type);
            }
            else if (state == State.Selecting && toolSelectTarget != type) {
                //System.out.format("[tool %s] tool on %s, already selecting %s, changing to selecting %s%n", machineID, currentTool, toolSelectTarget, type);

                if (toolPresentSensor.on()) {
                    if (Main.time() > stopTime) { // Already on toolSelectTarget
                        currentTool = toolSelectTarget;
                    }

                    startSelection(type);
                }
                else if (type == currentTool) {
                    currentTool = toolSelectTarget;
                    startSelection(type);
                    stopTime = Main.time();
                }
                else {
                    // currentTool = currentTool;
                    startSelection(type);
                    stopTime = Main.time() + toolSelectionTime;
                }
            }
        }
        
        private void startSelection(Type type) {
            if (machineID.startsWith("P")) {
                //System.out.format("[tool %s] startSelection type = %s%n", machineID, type);
            }
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
                default: throw new Error("Invalid tool type");
            }

            machiningDuration = -1;
            state = State.Selecting;
            stopTime = Main.time() + toolSelectionTime / 2;
            toolSelectMotor.turnOn(direction);
        }

        public void activate(long duration) {
            switch (state) {
                case Idle:
                    if (machineID.startsWith("P")) {
                        //System.out.format("[tool %s] activate: start machining with duration=%d%n", machineID, duration);
                    }
                    state = State.Machining;
                    stopTime = Main.time() + duration;
                    Main.modbus.setOutput(toolActivationMotorID, true);
                    break;
                case Selecting:
                    if (machineID.startsWith("P")) {
                        //System.out.format("[tool %s] activate: still selecting, set duration=%d%n", machineID, duration);
                    }
                    machiningDuration = duration;
                    break;
                case Machining:
                    throw new Error("Tool::activate called when machining is already in progress");
            }

        }

        public void selectAndActivate(Type type, long duration) {
            if (machineID.startsWith("P")) {
                //System.out.format("[tool %s] selectAndActivate: tool=%s duration=%d%n", machineID, type, duration);
            }
            select(type);
            activate(duration);
        }

        public boolean canSelect() {
            return state != State.Machining;
        }

        public boolean isIdle() {
            return state == State.Idle;
        }
        
        public boolean isMachining()
        {
            return state == State.Machining;
        }

    }

}
