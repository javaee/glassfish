package org.glassfish.flashlight.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author Mahesh Kannan
 *         Date: May 30, 2008
 */
public class ProbeAgentMain {

    private static Instrumentation _inst;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        _inst = inst;
    }
    
    public static void premain(String agentArgs, Instrumentation inst) {
        _inst = inst;
    }

    public static Instrumentation getInstrumentation() {
        return _inst;
    }

}
