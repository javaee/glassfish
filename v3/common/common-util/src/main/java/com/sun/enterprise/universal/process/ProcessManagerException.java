/*
 * ProcessManagerException.java
 * Any errors in ProcessManager will cause this to be thrown
 * @since JDK 1.4
 * @author bnevins
 * Created on October 28, 2005, 10:08 PM
 */
package com.sun.enterprise.universal.process;

public class ProcessManagerException extends Exception {

    public ProcessManagerException(Throwable cause) {
        super(cause);
    }
}
