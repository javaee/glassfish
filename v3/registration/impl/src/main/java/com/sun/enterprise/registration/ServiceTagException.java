/*
 * ServiceTagException.java
 *
 * Created on October 17, 2007, 12:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.registration;

/**
 *
 * @author msiraj
 */
public class ServiceTagException extends Exception {
    
    private static final String LINE_SEP = System.getProperty("line.separator");

    /** Creates a new instance of ServiceTagException */
    public ServiceTagException(Throwable t) {        
        super(t.getMessage(), t);
    }

    public ServiceTagException(String message, Throwable t) {        
        super((message == null) ? "" : message + LINE_SEP + 
                "cause: " + (t==null ? "unknown" : t.getMessage()), t);
    }

 /*    
    public String getMessage() {
        return (getCause() != null) ? getCause().getMessage() : getMessage();
    }
 */
    
}
