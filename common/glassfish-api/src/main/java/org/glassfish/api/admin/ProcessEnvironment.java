package org.glassfish.api.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

/**
 * Process environment allow access to information related to the execution or process.
 * This is a bit tricky to rely of @Contract/@Service service lookup for this API since
 * different implementations (server, clients, etc..) can be present of the classpath.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class ProcessEnvironment {

    /**
     * Default initialization is unkown process environment
     */
    public ProcessEnvironment() {
        type = ProcessType.Other;
    }

    /**
     * Enumeration of the supported process types
     * Server is the application server
     * ACC is the application client
     * Other is a standalone java.
     */
    public enum ProcessType { Server, ACC, Other }

    /**
     * Determine and return the modes in which the code is behaving, 
     * like application server or application client modes.
     * @return the process type
     */
    public ProcessType getProcessType() {
        return type;
    }

    /**
     * Creates a process environemnt for the inten
     * @param type of the execution environemnt
     */
    public ProcessEnvironment(ProcessType type) {
        this.type = type;
    }

    final private ProcessType type;

}
