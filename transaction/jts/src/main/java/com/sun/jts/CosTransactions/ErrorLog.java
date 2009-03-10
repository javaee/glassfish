/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
//----------------------------------------------------------------------------
//
// Module:      ErrorLog.java
//
// Description: Error logging facility.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//
// Copyright (c):   1995-1997 IBM Corp.
//
//   The source code for this program is not published or otherwise divested
//   of its trade secrets, irrespective of what has been deposited with the
//   U.S. Copyright Office.
//
//   This software contains confidential and proprietary information of
//   IBM Corp.
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

// Import required classes.

import java.io.*;
import java.util.*;
import java.text.DateFormat;

/**
 * Provides a log of error messages.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

public class ErrorLog extends Object {
    /**The message format strings.
     */
    private static Messages messages = null;

    /**The default error log file name.
     */
    static final String DEFAULT_LOGFILE = "jts.log"/*#Frozen*/;

    /**The path of the log file.
     */
    private static String errorLogPath = null;

    /**The name of the server.
     */
    private static String serverName = null;

    /**Sets up the error log path.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    private static final void setup() {
        // Get the error log file path.

        int[] result = new int[1];
        errorLogPath = Configuration.getDirectory(Configuration.TRACE_DIRECTORY,
                                                  Configuration.JTS_SUBDIRECTORY,
                                                  result);

        // If a default was used, display a message.

        if( result[0] == Configuration.DEFAULT_USED ||
            result[0] == Configuration.DEFAULT_INVALID ) {

            // In the case where the SOMBASE default is used, only display a message if
            // an invalid value was specified in the environment value.

            boolean loggingOn =
                Configuration.getPropertyValue(Configuration.ERR_LOGGING)
                != null;

            if (errorLogPath != null && loggingOn) {
                System.err.println(
                    ErrorLog.getMessage(Messages.INVALID_LOG_PATH,
                                        new java.lang.Object[]
                                            { errorLogPath })
                                  );
            }

            // In the case where the SOMBASE default is invalid, the value returned is
            // the invalid default. We then default to the current directory.

            if( result[0] == Configuration.DEFAULT_INVALID ) {
                if (loggingOn) {
                    System.err.println(
                        ErrorLog.getMessage(Messages.INVALID_DEFAULT_LOG_PATH)
                                      );
                }
                errorLogPath = "."/*#Frozen*/;
            }
        }

        // Get the server name too.

        serverName = Configuration.getServerName();
        if( serverName == null )
            serverName = "Anonymous transient server"/*#Frozen*/;

        // Get the ResourceBundle contents for message formats.

        messages = (Messages)ResourceBundle.getBundle("com.sun.jts.CosTransactions.Messages");
    }

    /**Writes an error message to the error log.
     *
     * @param message  The error message.
     *
     * @return
     *
     * @see
     */
    private static final void fileWrite( String message ) {
        if (Configuration.getPropertyValue(Configuration.ERR_LOGGING) == null) {
            return;
        }

        // Open the error log file and append the message.

        try {
            File errFileHandle = new File(errorLogPath, DEFAULT_LOGFILE);
            RandomAccessFile fileOutput = new RandomAccessFile(errFileHandle,"rw"/*#Frozen*/);
            fileOutput.seek(fileOutput.length());
            fileOutput.writeBytes(message);
            fileOutput.close();
        } catch( Throwable e ) {
            System.err.println(
                ErrorLog.getMessage(Messages.LOG_FILE_WRITE_ERROR)
                              );
        }
    }

    /**
     * Writes an error message to the error log and to the screen.
     * <p>
     * Note that the inserts should be Strings, Integers or Dates.
     * Exceptions should be converted to strings before calling this method.
     *
     * @param message  The error message number.
     * @param inserts  The error message inserts.
     * @param fatal    Indicates whether the error is fatal.
     *
     * @return
     *
     * @see
     */
    public static final void error(int message, Object[] inserts,
                                    boolean fatal) {
        String messageStr = getMessage(message, inserts);

        // First display the message to the screen.

        System.err.println(
            ErrorLog.getMessage(Messages.MSG_JTS_ERROR,
                                new java.lang.Object[] { messageStr })
                          );
        (new Exception()).printStackTrace();

        // Write the message to the log file.
        /*
        messageStr = new Date().toString() + " : " + serverName + " : JTS" +
            messages.getMessageNumber(message)+
            (fatal ? "F " : "E ") +
            messageStr + "\n";
        */
        String dateString = DateFormat.getDateTimeInstance().format(new Date());
        messageStr = ErrorLog.getMessage(Messages.LOG_MESSAGE,
                                         new java.lang.Object[] {
                                            dateString, serverName,
                                            messages.getMessageNumber(message),
                                            (fatal ? "F"/*#Frozen*/ : "E"/*#Frozen*/),
                                            messageStr,
                                         });

        fileWrite(messageStr);

        // If the error is fatal, then end the process.

        if (fatal) {
            // CHANGED(Ram J) - fatal errors should not cause VM crash.
            //System.exit(1);

            // throw a system exception, so that the app or the app server
            // may catch it. Note: This may leave the tx objects in an
            // inconsistent state, and may result in a memory leak (?).
            throw new org.omg.CORBA.INTERNAL(messageStr);
        }
    }

    /**
     * Writes a warning message to the error log and to the screen.
     * <p>
     * Note that the inserts should be Strings, Integers or Dates.
     * Exceptions should be converted to strings before calling this method.
     *
     * @param message  The warning message number.
     * @param inserts  The warning message inserts.
     *
     * @return
     *
     * @see
     */
    public static final void warning(int message, Object[] inserts) {
        String messageStr = getMessage(message, inserts);

        // First display the message to the screen.

        System.err.println(
            ErrorLog.getMessage(Messages.MSG_JTS_WARNING,
                                new java.lang.Object[] { messageStr })
                          );

        // Write the message to the log file.
        /*
        messageStr = new Date().toString() + " : " + serverName + " : JTS" +
            messages.getMessageNumber(message)+"W "+
            messageStr + "\n";
        */
        String dateString = DateFormat.getDateTimeInstance().format(new Date());
        messageStr = ErrorLog.getMessage(Messages.LOG_MESSAGE,
                                         new java.lang.Object[] {
                                            dateString, serverName,
                                            messages.getMessageNumber(message),
                                            "W"/*#Frozen*/, messageStr,
                                         });
        fileWrite(messageStr);
    }

    /**
     * Writes an informational message to the error log and to the screen.
     * <p>
     * Note that the inserts should be Strings, Integers or Dates.
     * Exceptions should be converted to strings before calling this method.
     *
     * @param message  The informational message number.
     * @param inserts  The informational message inserts.
     *
     * @return
     *
     * @see
     */
    public static final void info(int message, Object[] inserts) {
        String messageStr = getMessage(message, inserts);

        // First display the message to the screen.

        System.err.println(
            ErrorLog.getMessage(Messages.MSG_JTS_INFO,
                                new java.lang.Object[] { messageStr })
                          );

        // Write the message to the log file.
        /*
        messageStr = new Date().toString() + " : " + serverName + " : JTS" +
            messages.getMessageNumber(message)+"I "+
            messageStr + "\n";
        */
        String dateString = DateFormat.getDateTimeInstance().format(new Date());
        messageStr = ErrorLog.getMessage(Messages.LOG_MESSAGE,
                                         new java.lang.Object[] {
                                            dateString, serverName,
                                            messages.getMessageNumber(message),
                                            "I"/*#Frozen*/, messageStr,
                                         });
        fileWrite(messageStr);
    }

    /**
     * Returns a formatted message given the message number and inserts.
     * <p>
     * Note that the inserts should be Strings, Integers or Dates.
     * Exceptions should be converted to strings before calling this method.
     *
     * @param message  The message number.
     * @param inserts  The message inserts.
     *
     * @return  The formatted string.
     *
     * @see
     */
    static final String getMessage(int message, Object[] inserts) {
        String result = null;

        // Get the error log file path, and the message formats.

        if (errorLogPath == null) {
            setup();
        }

        // Format the message.

        if (inserts == null) {
            inserts = new Object[0];
        }

        return messages.getMessage(message, inserts);
    }

    /**
     * Returns an unformatted message given the message number.
     *
     * @param messageNum  The message number.
     *
     * @return  The unformatted string.
     *
     * @see
     */
    public static final String getMessage(int messageNum) {
        String result = null;

        // Get the error log file path, and the message formats.

        if (errorLogPath == null) {
            setup();
        }

        return messages.getMessage(messageNum);
    }
}
