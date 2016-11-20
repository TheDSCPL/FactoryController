package coms;

import java.net.*;
import main.*;
import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.*;
import net.wimpi.modbus.util.*;

public class ModbusMaster {

    public final int inputCount, outputCount, registerCount;
    private TCPMasterConnection conn;
    private BitVector inputs;
    private final BitVector outputs;
    private Register[] registers;

    public ModbusMaster() {
        inputCount = Main.config.getI("modbus.inputCount");
        outputCount = Main.config.getI("modbus.outputCount");
        registerCount = Main.config.getI("modbus.registerCount");
        inputs = new BitVector(inputCount);
        outputs = new BitVector(outputCount);
    }

    public void connect() throws UnknownHostException, Exception {
        conn = new TCPMasterConnection(InetAddress.getByName(Main.config.getS("modbus.ip")));
        conn.setPort(Main.config.getI("modbus.port"));
        conn.connect();
    }

    public void close() {
        conn.close();
    }

    public void refreshInputs() throws ModbusException {
        ReadInputDiscretesRequest req1 = new ReadInputDiscretesRequest(0, inputCount);

        ModbusTCPTransaction trans1 = new ModbusTCPTransaction(conn);
        trans1.setRequest(req1);
        trans1.execute();

        inputs = ((ReadInputDiscretesResponse) trans1.getResponse()).getDiscretes();

        ReadMultipleRegistersRequest req2 = new ReadMultipleRegistersRequest(0, registerCount);

        ModbusTCPTransaction trans2 = new ModbusTCPTransaction(conn);
        trans2.setRequest(req2);
        trans2.execute();

        registers = ((ReadMultipleRegistersResponse) trans2.getResponse()).getRegisters();
    }

    public void refreshOutputs() throws ModbusException {
        WriteMultipleCoilsRequest req1 = new WriteMultipleCoilsRequest(0, outputs);

        ModbusTCPTransaction trans1 = new ModbusTCPTransaction(conn);
        trans1.setRequest(req1);
        trans1.execute();

        WriteMultipleRegistersRequest req2 = new WriteMultipleRegistersRequest(0, registers);

        ModbusTCPTransaction trans2 = new ModbusTCPTransaction(conn);
        trans2.setRequest(req2);
        trans2.execute();
    }

    public boolean getInput(int index) {
        return inputs.getBit(index);
    }

    private boolean[] lastValue = new boolean[7];

    public void setOutput(int index, boolean value) {
        /*//if (index >= 169 && index <= 175)
        if (index == 175) {
            if (value != lastValue[index - 169]) {
                System.out.println("Index changed: " + index + " " + value);
            }
            lastValue[index - 169] = value;
        }*/
        
        outputs.setBit(index, value);
    }

    public int getRegister(int index) {
        return registers[index].getValue();
    }

    public void setRegister(int index, int value) {
        registers[index].setValue(value);
    }
}
