/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.module.maven.sc;

/** Denotes an exceptional condition during the creation of the script.
 * @author Kedar Mhaswade (km@dev.java.net)
 */
public class ScriptCreationException extends Exception {

    /**
     * Creates a new instance of <code>ScriptCreationException</code> without detail message.
     */
    public ScriptCreationException() {
    }


    /**
     * Constructs an instance of <code>ScriptCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ScriptCreationException(String msg) {
        super(msg);
    }
    
    public ScriptCreationException(Exception e) {
        super(e);
    }
}
