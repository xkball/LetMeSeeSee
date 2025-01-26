package com.xkball.let_me_see_see;

import java.lang.instrument.Instrumentation;

public class LMSAgent {

    public static Instrumentation INST;
    
    public static void agentmain(String args, Instrumentation inst) {
        INST = inst;
    }
    
}
