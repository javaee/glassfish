/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * A POL (plain old logger).  
 *
 * @author bnevins
 */

public class GFLauncherLogger {

    public static void info(String msg, Object... objs)
    {
        logger.info(strings.get(msg, objs));
    }
    public static void severe(String msg, Object... objs)
    {
        logger.severe(strings.get(msg, objs));
    }
    public static void fine(String msg, Object... objs)
    {
        logger.fine(strings.get(msg, objs));
    }

    /////////////////////////  non-public below  //////////////////////////////
    
    static synchronized void setConsoleLevel(Level level) {
        Logger parent = logger;
        
        while(parent != null) {
            Handler[] handlers = parent.getHandlers();
            
            for(Handler h : handlers) {
                if(ConsoleHandler.class.isAssignableFrom(h.getClass())) {
                    h.setLevel(level);
                }
            }

            parent = parent.getParent();
        }
    }
    /**
     * IMPORTANT!  
     * The server's logfile is added to the *local* logger.  But it is never
     * removed.  The files are kept open by the logger.  One really bad result
     * is that Windows will not be able to delete that server after stopping it.
     * Solution: remove the file handler when done.
     * @param logFile The logfile
     * @throws GFLauncherException if the info object has not been setup
     */
    static synchronized void addLogFileHandler(String logFile, GFLauncherInfo info) throws GFLauncherException
    {
        try
        {
            if (logFile == null || logfileHandler != null) {
                return;
            }
            logfileHandler = new FileHandler(logFile, true);
            logfileHandler.setFormatter(new SimpleFormatter());
            logfileHandler.setLevel(Level.INFO);
            logger.addHandler(logfileHandler);
        }
        catch(IOException e)
        {
            // should be seen in verbose mode for debugging
            e.printStackTrace();
        }

    }
    static  synchronized void removeLogFileHandler()  {
        if(logfileHandler != null) {
            logger.removeHandler(logfileHandler);
            logfileHandler.close();
            logfileHandler = null;
        }          
    }
    
    private GFLauncherLogger() {
    }
    private final static Logger logger;
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncherLogger.class);
    private static FileHandler logfileHandler;
    
    static  {
        logger = Logger.getLogger(GFLauncherLogger.class.getName());
        logger.setLevel(Level.INFO);
    }
}
