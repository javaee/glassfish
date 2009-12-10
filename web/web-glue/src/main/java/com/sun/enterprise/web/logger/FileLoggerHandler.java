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

package com.sun.enterprise.web.logger;

/**
 * An implementation of FileLoggerHandler which logs to virtual-server property 
 * log-file when enabled
 */

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.logging.*;
import com.sun.enterprise.server.logging.UniformLogFormatter;
import com.sun.enterprise.v3.services.impl.LogManagerService;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

//specify that the contract is provided by handler.class in the JDK
@Service
public class FileLoggerHandler extends Handler implements PostConstruct {
    

    @Inject(optional=true)
    LogManagerService logManager=null;

    private String webLogger = "javax.enterprise.system.container.web.com.sun.enterprise.web";
    private String catalinaLogger = "org.apache.catalina";
    
    private FileOutputStream fileOutputStream;
    private PrintWriter printWriter;

    
    public void postConstruct() {
        setLevel(Level.OFF);
        this.setFormatter(new UniformLogFormatter());
    }
    
    
    public void setLogFile(String logFile) {
        try {
            fileOutputStream = new FileOutputStream(logFile, true);
            printWriter = new PrintWriter(fileOutputStream);
            if (logManager!=null) {
                logManager.addHandler(this);
            }
    	} catch (IOException e) {
    	}
    }
 
    
    /**
     * Overridden method used to capture log entries   
     *
     * @param record The log record to be written out.
     */
    public void publish(LogRecord record) {
        // first see if this entry should be filtered out
        // the filter should keep anything
        if ( getFilter()!=null ) {
            if ( !getFilter().isLoggable(record) )
                return;
        }
        
        // log-file hasn't been set 
        if (fileOutputStream==null || printWriter==null) {
            return; 
        }

        if (webLogger.equals(record.getLoggerName()) || 
            catalinaLogger.equals(record.getLoggerName()) ) {      
            printWriter.write(getFormatter().format(record)); 
            printWriter.flush();
        }

    }

    
    /**
     * Called to close this log handler.
     */
    public void close() {
        printWriter.close();
    }
 
    
    /**
     * Called to flush any cached data that
     * this log handler may contain.
     */
    public void flush() {
        printWriter.flush();
    }
    
        
    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosityLevel The new verbosity level, as a string
     */
    public void setLevel(String logLevel) {
            
        if ("SEVERE".equalsIgnoreCase(logLevel)) {
            setLevel(Level.SEVERE);
        } else if ("WARNING".equalsIgnoreCase(logLevel)) {
            setLevel(Level.WARNING);
        } else if ("INFO".equalsIgnoreCase(logLevel)) {
           setLevel(Level.INFO);
        } else if ("CONFIG".equalsIgnoreCase(logLevel)) {
           setLevel(Level.CONFIG);
        } else if ("FINE".equalsIgnoreCase(logLevel)) {
            setLevel(Level.FINE);
        } else if ("FINER".equalsIgnoreCase(logLevel)) {
            setLevel(Level.FINER);
        } else if ("FINEST".equalsIgnoreCase(logLevel)) {
            setLevel(Level.FINEST);
        } else {
            setLevel(Level.INFO);
        }
        
    }
    
}
