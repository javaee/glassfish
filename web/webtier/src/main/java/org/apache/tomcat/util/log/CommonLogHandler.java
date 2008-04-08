

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.tomcat.util.log;

import org.apache.tomcat.util.log.*;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.util.*;

import com.sun.org.apache.commons.logging.*;

/**
 *  Log using common-logging.
 *
 * @author Costin Manolache
 */
public  class CommonLogHandler extends LogHandler {

    private Hashtable loggers=new Hashtable();
    
    /**
     * Prints log message and stack trace.
     * This method should be overriden by real logger implementations
     *
     * @param	prefix		optional prefix. 
     * @param	message		the message to log. 
     * @param	t		the exception that was thrown.
     * @param	verbosityLevel	what type of message is this?
     * 				(WARNING/DEBUG/INFO etc)
     */
    public void log(String prefix, String msg, Throwable t,
		    int verbosityLevel)
    {
        if( prefix==null ) prefix="tomcat";

        com.sun.org.apache.commons.logging.Log l=(com.sun.org.apache.commons.logging.Log)loggers.get( prefix );
        if( l==null ) {
            l=LogFactory.getLog( prefix );
            loggers.put( prefix, l );
        }
        
	if( verbosityLevel > this.level ) return;

        if( t==null ) {
            if( verbosityLevel == Log.FATAL )
                l.fatal(msg);
            else if( verbosityLevel == Log.ERROR )
                l.error( msg );
            else if( verbosityLevel == Log.WARNING )
                l.warn( msg );
            else if( verbosityLevel == Log.INFORMATION)
                l.info( msg );
            else if( verbosityLevel == Log.DEBUG )
                l.debug( msg );
        } else {
            if( verbosityLevel == Log.FATAL )
                l.fatal(msg, t);
            else if( verbosityLevel == Log.ERROR )
                l.error( msg, t );
            else if( verbosityLevel == Log.WARNING )
                l.warn( msg, t );
            else if( verbosityLevel == Log.INFORMATION)
                l.info( msg, t );
            else if( verbosityLevel == Log.DEBUG )
                l.debug( msg, t );
        }
    }

    /**
     * Flush the log. 
     */
    public void flush() {
	// Nothing - commons logging doesn't have the notion
    }

    /**
     * Close the log. 
     */
    public synchronized void close() {
	// Nothing - commons logging doesn't have the notion
    }
    
}
