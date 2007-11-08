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
 * LogMgmtEventsNotificationListener.java
 *
 * Created on August 19, 2005, 1:18 PM
 */

package com.sun.enterprise.management.selfmanagement;

import java.util.Map;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
/**
 *
 * @author Harpreet Singh
 */
public final class LogMgmtEventsNotificationListener implements NotificationListener {
  
    /** Logger for self management service */
    private static final Logger _logger =  LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
  
    /** Local Strings manager for the class */
    private static final StringManager localStrings = StringManager.getManager(LogMgmtEventsNotificationListener.class);
    
    /**
     * Is logging turned on for this particular even. Key corresponds to 
     * the <i>record-event</i> from the event element in domain xml
     */
    public static final String RECORD_EVENT_KEY = ServerTags.RECORD_EVENT;
    
    /*
     * The logging level for logging events. An event is logged if the 
     * record-event is turned. Key corresponds to level in event element 
     * in domain xml.
     */
    public static final String RECORD_LOG_LEVEL_KEY = ServerTags.LEVEL;
        
    public static final String EVENT_TYPE_KEY = ServerTags.TYPE;
    
    public static final String EVENT_DESCRIPTION_KEY = ServerTags.DESCRIPTION;

    /** Creates a new instance of LogMgmtEventsNotificationListener */
    private LogMgmtEventsNotificationListener() {
        // do Nothing
    }

    public static LogMgmtEventsNotificationListener getInstance (){
        return new LogMgmtEventsNotificationListener ();
    }
    public void handleNotification(Notification notification, Object handback) {
        
        Map<String, String> property =
                (Map<String, String>) handback;
        
        if (property == null)
            return;
        
        String recordevent = null;;
        recordevent = (String)property.get(RECORD_EVENT_KEY);
	if (recordevent == null)
	   return;

        boolean recordEvent = false;
        if (recordevent != null){
            recordEvent = Boolean.valueOf(recordevent);
            
            if(recordEvent){
                try{
		    StringBuffer message  = 
			new StringBuffer(localStrings.
				getString("logMgmtEventsNotificationListener.prefix"));

                    String eventType = (String)property.get(EVENT_TYPE_KEY);

		    if (eventType != null){
			message.append (eventType);	
			message.append (":");
		    }
                    String logLevel = (String) property.get(RECORD_LOG_LEVEL_KEY);
	            if(logLevel == null){
			logLevel = Level.FINE.toString();	
		    }
		    message.append (logLevel);
		    message.append (":");
                    Level level = Level.parse(logLevel);

                    String description = (String) property.get(EVENT_DESCRIPTION_KEY);

		    if (description != null){
			message.append (description);
			message.append (":");
		   }
                    _logger.log(level, message.toString());
                } catch (IllegalArgumentException iae) {
                    _logger.log(Level.FINE, "Incorrect Log Level set for event. Cannot log event ", iae);
			iae.printStackTrace();
                } catch (NullPointerException npe){
                    _logger.log(Level.FINE, "Incorrect Log Level set for event. Cannot log event ", npe);
			npe.printStackTrace();
		}
            }
        }
    }
}
    
