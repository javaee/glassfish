/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

/**
 * This class makes it easier to print out debug statements.
 * The Debug.print statements are printed to System.err
 * if debuggingOn = true.
 */

package util;

public final class Debug {

    public static final boolean debuggingOn = false;

    public static final void print(String msg) {

        if (debuggingOn) {
           System.err.println("Debug: " + msg);
        }
    }

    public static final void print(String msg, Object object) {

        if (debuggingOn) {
           System.err.println("Debug: " + msg);
           System.err.println("       " + object.getClass().getName());
        }
    }

}  // Debug
