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
 * An implementation of FileLoggerHandler which logs to virtual-server property log-file when enabled.
 *
**/

import java.util.logging.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

//specify that the contract is provided by handler.class in the JDK
@Service
@ContractProvided(Handler.class) 
@Scoped(Singleton.class)
public class FileLoggerHandler extends Handler implements PostConstruct {
    
    @Inject
    Habitat habitat;
    
    static BufferedWriter f = null;
    String webLogger = "javax.enterprise.system.container.web";
    String catalinaLogger = "org.apache.catalina";
    
    public void postConstruct() {
        setLevel(Level.OFF);
    }
    
    public void setLogFile(String logFile) {
        try {
            f = new BufferedWriter(new FileWriter(logFile));       
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

        try {
            if (webLogger.equals(record.getLoggerName()) || 
                    catalinaLogger.equals(record.getLoggerName()) ) {
                f.write ("FileLoggerHandler output - ");
                f.write("logger name: "+record.getLoggerName());
                f.write(" source classname: "+record.getSourceClassName());
                f.write(" message: "+record.getMessage()); 
                f.newLine();
                f.flush();
            }
        } catch (IOException ex){
        }

    }

    /**
     * Called to close this log handler.
     */
    public void close() {
        try {
	    f.close();
 	} catch (IOException ex) {
        }
    }

 
    /**
     * Called to flush any cached data that
     * this log handler may contain.
     */
    public void flush() {
        // not used
    }
}
