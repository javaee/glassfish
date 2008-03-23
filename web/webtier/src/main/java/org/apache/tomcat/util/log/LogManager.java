

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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;


/**
 * Allows the control the log properties at runtime.
 * Normal applications will just use Log, without having to
 * deal with the way the log is configured or managed.
 *
 *
 * @author Alex Chaffee [alex@jguru.com]
 * @author Costin Manolache
 **/
public class LogManager {

    //static LogHandler defaultChannel=new LogHandler();
    static LogHandler defaultChannel=new CommonLogHandler();
    
    protected Hashtable loggers=new Hashtable();
    protected Hashtable channels=new Hashtable();

    public  Hashtable getLoggers() {
	return loggers;
    }

    public Hashtable getChannels() {
	return channels;
    }
    
    public static void setDefault( LogHandler l ) {
	if( defaultChannel==null)
	    defaultChannel=l;
    }

    public void addChannel( String name, LogHandler logH ) {
	if(name==null) name="";

	channels.put( name, logH );
	Enumeration enumeration=loggers.keys();
	while( enumeration.hasMoreElements() ) {
	    String k=(String)enumeration.nextElement();
	    Log l=(Log)loggers.get( k );
	    if( name.equals( l.getChannel( this ) )) {
		l.setProxy( this, logH );
	    }
	}
    }
    
    /** Default method to create a log facade.
     */
    public Log getLog( String channel, String prefix,
		       Object owner ) {
	if( prefix==null && owner!=null ) {
	    String cname = owner.getClass().getName();
	    prefix = cname.substring( cname.lastIndexOf(".") +1);
	}

	LogHandler proxy=(LogHandler)channels.get(channel);
	if( proxy==null ) proxy=defaultChannel;
	
	// user-level loggers
	Log log=new Log( channel, prefix, proxy, owner );
	loggers.put( channel + ":" + prefix, log );
	if( dL > 0 )
	    System.out.println("getLog facade " + channel + ":" + prefix);
	return log;
    }

    private static int dL=0;

}    
