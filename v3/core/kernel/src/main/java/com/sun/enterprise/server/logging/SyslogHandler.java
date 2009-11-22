/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.enterprise.server.logging;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.config.support.TranslatedConfigView;
import com.sun.enterprise.v3.common.BooleanLatch;

import java.util.logging.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import com.sun.logging.LogDomains;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Mar 11, 2009
 * Time: 1:41:30 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Scoped(Singleton.class)
@ContractProvided(java.util.logging.Handler.class)
public class SyslogHandler extends Handler implements PostConstruct, PreDestroy {

    @Inject
    ServerEnvironmentImpl env;

    private Syslog sysLogger;
    private Thread pump= null;
    private BooleanLatch done = new BooleanLatch();
    private BlockingQueue<LogRecord> pendingRecords = new ArrayBlockingQueue<LogRecord>(5000);
    
    

    public void postConstruct() {

        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        String systemLogging = TranslatedConfigView.getTranslatedValue(manager.getProperty(cname + ".useSystemLogging")).toString();
        if (systemLogging.equals("false"))
            return;

        //set up the connection
        try {
            sysLogger = new Syslog("localhost");  //for now only write to this host
        } catch ( java.net.UnknownHostException e) {
		   Logger.getAnonymousLogger().log(Level.SEVERE,"unknown host" );
		   return;
		}
        
        // start the Queue consummer thread.
        pump = new Thread() {
            public void run() {
                try {
                    while (!done.isSignalled()) {
                        log();
                    }
                } catch (RuntimeException e) {

                }
            }
        };
        pump.start();

    }
    public void preDestroy() {
        LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER).fine("SysLog Logger handler killed");
    }

    /**
     * Retrieves the LogRecord from our Queue and store them in the file
     *
     */
    public void log() {

        LogRecord record;

        try {
            record = pendingRecords.take();
        } catch (InterruptedException e) {
            return;
        }
        Level level= record.getLevel();
        long millisec = record.getMillis();
        int l;
        String slLvl;

        if (level.equals(Level.SEVERE)) {
            l = Syslog.CRIT;
            slLvl = "CRIT";
        }else if (level.equals(Level.WARNING)){
            l = Syslog.WARNING;
            slLvl = "WARNING";
        }else if (level.equals(Level.INFO)) {
            l = Syslog.INFO;
            slLvl = "INFO";
        }else   {
            l = Syslog.DEBUG;
            slLvl = "DEBUG";
        }
        
        //format the message
        String msg;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss");
        msg = formatter.format(millisec);
        msg = msg +" [ " + slLvl +" glassfish ] " +record.getMessage();

         //send message
        sysLogger.log(Syslog.DAEMON, Syslog.WARNING, msg);

    }

    /**
     * Publishes the logrecord storing it in our queue
     */
    public void publish( LogRecord record ) {
        if (pump == null)
            return;
            
        try {
            pendingRecords.add(record);
        } catch(IllegalStateException e) {
            // queue is full, start waiting.
            try {
                pendingRecords.put(record);
            } catch (InterruptedException e1) {
                // to bad, record is lost...
            }
        }
    }

    public void close() {

    }

    public void flush() {
        
    }
}

