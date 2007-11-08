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
 * LogNotificationFilter.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.NotificationFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import com.sun.appserv.management.ext.logging.*;
import com.sun.appserv.management.base.Util;
import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * This is a NotificationFilter that will be instrumented to LogEvent to
 * filterout unwanted logEvents. Basically, it will filterout logs based on
 * LoggerName and Level settings.
 *
 * @author Sun Micro Systems, Inc
 */
public class LogEventFilter implements NotificationFilter {
    private static StringManager sm = StringManager.getManager(LogEventFilter.class);
    private boolean anyLogger = false;
    
    private ArrayList<String> loggerNames;
    
    private Level level;
    
    /** Creates a new instance of LogNotificationFilter */
    public LogEventFilter() {
    }
    
    public List getLoggerNames() {
        return loggerNames;
    }
    
    public void setLoggerNames(String loggers) {
        if( loggers == null ){
            return;
        }
        loggerNames = new ArrayList<String>( );
        StringTokenizer tokenizer = new StringTokenizer(loggers, ",");
        while( tokenizer.hasMoreTokens()) {
            String loggerName = tokenizer.nextToken();
            if ("*".equals(loggerName)) {
                anyLogger = true;
                loggerNames.add( loggerName );
                return;
            }
            loggerNames.add( loggerName );
        }
    }
    
    public String getLevel() {
        return level.toString();
    }
    
    public void setLevel(String level) {
        this.level = Level.parse(level);
    }
    
    // IMPORTANT: Do not put any logging statements in this method
    // This would cause infinite loop as the log statements would
    // generate another notification
    // Not even System.out or System.err
    
    public boolean isNotificationEnabled(
            javax.management.Notification notification ) {
        boolean loggerNameMatched = false;
        boolean logLevelMatched = false;
        
        if (anyLogger)
            loggerNameMatched = true;
        else {
            String loggerNameFromNotification = (String)Util.getAMXNotificationValue(notification,
                    LogRecordEmitter.LOG_RECORD_LOGGER_NAME_KEY);
            Iterator iterator = loggerNames.iterator( );
            while( iterator.hasNext( ) ) {
                String loggerNameFromList = (String) iterator.next( );
                if( loggerNameFromNotification.startsWith( loggerNameFromList )) {
                    loggerNameMatched = true;
                    break;
                }
            }
        }
        Level logLevelFromNotification = (Level)Util.getAMXNotificationValue(notification,
                LogRecordEmitter.LOG_RECORD_LEVEL_KEY);
        if( logLevelFromNotification.intValue() >= this.level.intValue() ) {
            logLevelMatched = true;
        }
        return loggerNameMatched && logLevelMatched;
    }
    
    
}
