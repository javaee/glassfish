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
import com.sun.enterprise.server.logging.Syslog;
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
        sysLogger.log(Syslog.DAEMON, l, msg);

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

