package com.xkball.let_me_see_see;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class LMSMain {
    
    public static String JAR_PATH;
    
    public static void main(String[] args) {
        if(args.length == 2){
            var pid = Long.parseLong(args[0]);
            JAR_PATH = args[1];
            runAgent(pid);
        }
        else {
            System.out.println("Usage: java LMSMain <pid> <agent-path>");
        }
    }
    
    public static void runAgent(long pid){
        try {
            var virtualMachine = VirtualMachine.attach(String.valueOf(pid));
            virtualMachine.loadAgent(JAR_PATH);
            virtualMachine.detach();
        } catch (AttachNotSupportedException | IOException | AgentInitializationException | AgentLoadException e) {
            throw new RuntimeException(e);
        }
    }
}
