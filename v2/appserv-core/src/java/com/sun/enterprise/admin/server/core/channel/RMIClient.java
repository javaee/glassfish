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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.server.core.channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.ServerInstanceStatus;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * RMI client is used to send admin channel messages over RMI. 
 */
public class RMIClient implements Runnable {

    private File stubFile;
    private File seedFile;
    private long stubFileTs = 0;
    private byte[] key;
    private RemoteAdminChannel stub;
    private boolean autoRefresh;
    private long autoRefreshInterval;
    private Thread autoRefreshThread;

    /**
     * Create a new RMI client using specified stub file and use the specified
     * byte array (seed) for all calls to the remote object. To obtain an
     * instance of RMIClient please use the public method
     * <code>AdminChannel.getRMIClient()</code> which ensure that all calls to
     * a particular server instance go through the same object instance
     * (of RMIClient).
     * @param stubFile name of the RMI stub file
     * @param seedFile name of the shared secret file
     * @throws IllegalArgumentException if either stubFile or seedFile is null.
     */
    RMIClient(String stubFile, String seedFile) {
        if (stubFile == null || seedFile == null) {
            warn(CLIENT_NULLARGS_ERRCODE);
            throw new IllegalArgumentException(CLIENT_NULLARGS_ERRMSG);
        }
        this.stubFile = new File(stubFile);
        this.seedFile = new File(seedFile);
        if (this.stubFile.exists()) {
            stub = readStub();
        }
        if (AdminChannel.getClientAutoRefreshEnabled()) {
            startAutoRefreshThread(
                    AdminChannel.getClientAutoRefreshInterval());
        }
    }

    /**
     * Constructor for local mode
     */
    public RMIClient(boolean isDebug, String stubFile, String seedFile) {
        if (stubFile == null || seedFile == null) {
            throw new IllegalArgumentException(CLIENT_NULLARGS_ERRMSG);
        }
        this.stubFile = new File(stubFile);
        this.seedFile = new File(seedFile);
        if (this.stubFile.exists()) {
            stub = readStub();
        }
    }

    /**
     * Start auto refresh thread. The auto refresh thread refreshes the remote
     * stub if the stub file has changed on the disk.
     */
    void startAutoRefreshThread(long interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException(INVALID_AUTO_REFRESH_INTERVAL);
        }
        autoRefresh = true;
        autoRefreshInterval = interval;
        if (autoRefreshThread != null && autoRefreshThread.isAlive()) {
            return;
        } else {
            autoRefreshThread = new Thread(this);
            autoRefreshThread.start();
        }
    }

    /**
     * Stop auto refresh thread.
     */
    void stopAutoRefreshThread() {
        autoRefresh = false;
    }

    /**
     * Auto refresh this rmi client.
     */
    public void run() {
        while (autoRefresh) {
            try {
                Thread.sleep(autoRefreshInterval);
            } catch (InterruptedException ie) {
                warn(AUTO_REFRESH_INTR);
                autoRefresh = false;
            }
            if (autoRefresh) {
                checkServerStatus();
            }
        }
    }

    /**
     * Send specified event notification over admin channel
     */
    public AdminEventResult sendNotification(AdminEvent event) {
        // Normal handling, send the event
        boolean doRetry = true;
        AdminEventResult result = null;
        if (stub != null) {
            try {
                result = stub.sendNotification(key, event);
                doRetry = false;
            } catch (ServerException re) {
                if ((re.detail != null) &&
                        (re.detail instanceof java.lang.IllegalArgumentException
                        || re.detail instanceof java.lang.SecurityException)) {
                    doRetry = false;
                    warn(EVENT_NOTIFY_ERROR);
                    debug(re.detail);
                } else {
                    if (re.detail != null) {
                        debug(re.detail);
                    }
                    debug(re);
                }
            } catch (RemoteException re) {
                if (re.detail != null) {
                    debug(re.detail);
                }
                debug(re);
            }
        }
        if (doRetry) {
            // Normal processing did not work, try to get stub again and then
            // attempt to send the event
            boolean gotNew = checkServerStatus();
            if (stub != null && gotNew) {
                try {
                    result = stub.sendNotification(key, event);
                } catch (RemoteException re) {
                    warn(EVENT_RENOTIFY_ERROR);
                    if (re.detail != null) {
                        debug(re.detail);
                    }
                    debug(re);
                }
            }
        }
        if (result == null) {
            // Still couldn't communicate, set result appropriately
            result = new AdminEventResult(event.getSequenceNumber());
            result.setResultCode(AdminEventResult.TRANSMISSION_ERROR);
            if (stub == null) {
                result.addMessage(event.getEffectiveDestination(),
                    "Remote Stub is null");
            }
        }
        return result;
    }

    /** Determines wheter the instance with given name is alive.
     *  Really speaking, checks whether the RMI channel is responsive.
     *  The method returns immediately with the status without any retries.
     *  @return boolean indicating whether the RMIServer Object
     *  that <code> this </code> server instance is alive.
     */
    public boolean isAlive() {
        return isAlive(false);
    }

    /**
     * Determines whether the instance represented by this object is alive.
     * Unless the parameter refreshStub is true, the responsiveness of
     * instance is checked by using cached stub (if any) and if there is no
     * cached stub the method returns false.
     * @param refreshStub if true, refresh remote stub if it has changed
     * @return true if the instance represented by <code>this</code> object
     *     is responding, false otherwise.
     */
    public boolean isAlive(boolean refreshStub) {

        if (refreshStub) {
            boolean gotNew = checkServerStatus();
        }

        boolean isAlive = true;

        if (stub != null) {
            try {
                stub.pingServer(key);
            }
            catch(RemoteException re) {
                debug(re);
                isAlive = false;
            }
        }
        else {
            isAlive = false;
        }
        return ( isAlive );
    }

    /**
     * Get server instance status code. If the instance can not be contacted
     * over rmi channel then the method reports that instance is not running.
     * The return value of this method is one of the constants <code>
     * kInstanceStartingCode, kInstanceRunningCode, kInstanceStoppingCode or
     * kInstanceNotRunningCode</code> from the class <code>
     * com.sun.enterprise.admin.common.Status</code>.
     * @return an int denoting the status of server starting, running, stopping
     *     or not running.
     */
    public int getInstanceStatusCode() {
        boolean gotNew = checkServerStatus();
        int statusCode = Status.kInstanceNotRunningCode;
        if (stub != null) {
            try {
                statusCode = stub.getServerStatusCode(key);
            } catch (RemoteException re) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                if (re.detail != null) {
                    trace(re.detail);
                }
                trace(re);
            } catch (IllegalArgumentException iae) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                trace(iae);
                // This means that the key did not match. Attempt to read
                // the key from the disk again to work around the race
                // condition when the file is read on the client before the
                // server has finished writing (and hence client has partial
                // key). Another read updates the shared key on the client.
                byte[] newKey = null;
                try {
                    newKey = readSeed();
                } catch (IOException ioe) {
                    debug(FILE_READ_ERROR, seedFile.getName());
                    trace(ioe);
                }
                if (newKey != null) {
                    key = newKey;
                }
                throw iae;
            }
        }
        return statusCode;
    }

    /**
     * Get the port where the conflict occurs.
     *
     * @return A valid port number.0 If there is any error in processing
     */
    public int getConflictedPort() {
        int conflictedPort = 0;
        boolean gotNew = checkServerStatus();
        if (stub != null) {
            try {
                conflictedPort = stub.getConflictedPort(key);
            } catch (RemoteException re) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                if (re.detail != null) {
                    trace(re.detail);
                }
                trace(re);
            }
        }
        return conflictedPort;
    }

    /**
     * Triggers exit of the server VM. Server VM will client 
     * that got the conflicted port number calls this method. 
     */
    public void triggerServerExit() {
        boolean gotNew = checkServerStatus();
        if (stub != null) {
            try {
                stub.triggerServerExit(key);
            } catch (RemoteException re) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                if (re.detail != null) {
                    trace(re.detail);
                }
                trace(re);
            }
        }
    }

    /**
     * Is restart needed on the instance. The status is set by admin server and
     * instance just keeps track of it across admin server restarts. If the
     * instance has already restarted since admin server set the status, the
     * instance does not require restart (as expected).
     * @return true if the instance requires restart, false otherwise.
     */
    public boolean isRestartNeeded() {
        boolean restartNeeded = false;
        boolean gotNew = checkServerStatus();
        if (stub != null) {
            try {
                restartNeeded = stub.isRestartNeeded(key);
            } catch (RemoteException re) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                if (re.detail != null) {
                    trace(re.detail);
                }
                trace(re);
            }
        }
        return restartNeeded;
    }

    /**
     * Set restart needed status for a server instance. If the instance is not
     * running or if there is an error in communicating, the instance restart
     * status will not be changed. The status set by this method is preserved
     * in the instance across admin server restarts.
     * @param restartNeeded true if instance restart is needed, false otherwise.
     */
    public void setRestartNeeded(boolean restartNeeded) {
        boolean gotNew = checkServerStatus();
        if (stub != null) {
            try {
                stub.setRestartNeeded(key, restartNeeded);
            } catch (RemoteException re) {
                debug(CHANNEL_COMM_ERROR, stubFile.getName());
                if (re.detail != null) {
                    trace(re.detail);
                }
                trace(re);
            }
        }
    }

    /**
     * Check status of server stub file and refresh if needed.
     */
    private boolean checkServerStatus() {
        boolean gotNew = false;
        if (stubFile.exists()) {
            long ts = stubFile.lastModified();
            if (ts > stubFileTs) {
                if (stubFile.canRead()) {
                    RemoteAdminChannel obj = readStub();
                    if (obj != null) {
                        gotNew = true;
                        stub = obj;
                    }
                } else {
                    warn(FILE_READ_ERROR, stubFile.getName());
                }
            }
        } else {
            if (stub != null) {
                stub = null;
            }
        }
        return gotNew;
    }

    /**
     * Read stub from stub file
     */
    private RemoteAdminChannel readStub() {
        RemoteAdminChannel obj = null;
        FileInputStream fis = null;
        try {
            stubFileTs = stubFile.lastModified();
            fis = new FileInputStream(stubFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = (RemoteAdminChannel)ois.readObject();
        } catch (IOException ioe) {
            warn(CLIENT_INIT_ERROR);
            debug(ioe);
        } catch (ClassNotFoundException cnfe) {
            warn(CLIENT_INIT_ERROR);
            debug(cnfe);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                }
            }
        }
        try {
            key = readSeed();
        } catch (IOException ioe) {
            warn(CLIENT_INIT_ERROR);
            debug(ioe);
            obj = null;
        }
        if (obj == null) {
            stubFileTs = 0;
        }
        return obj;
    }

    /**
     * Read shared secret from file.
     */
    private byte[] readSeed() throws IOException {
        byte[] seed = null;
        if (seedFile.exists() && seedFile.canRead()) {
            // Seed file is updated after creating stub file and therefore
            // it should only be read only if it has been modified since stub
            // file was modified. NOTE: This relies on the fact that the
            // server always (on every startup) writes seed file after stubfile.
            long seedFileTs = seedFile.lastModified();
            if (seedFileTs >= stubFileTs) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(seedFile);
                    seed = new byte[AdminChannel.SEED_LENGTH];
                    fis.read(seed);
                } catch (IOException ioe) {
                    warn(AdminChannel.KEY_READ_ERROR);
                    debug(ioe);
                    seed = null;
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
            } else {
                debug(SEED_FILE_OLDER,
                        new Long[] {new Long(seedFileTs), new Long(stubFileTs)});
            }
        } else {
            warn(AdminChannel.KEY_READ_ERROR);
            debug(FILE_READ_ERROR, seedFile.getName());
        }
        if (seed == null) {
			String msg = localStrings.getString( "admin.server.core.channel.unable_initializing_key", seedFile );
            throw new IOException( msg );
        }
        return seed;
    }

    /**
     * Has the server instance restarted since specified timestamp. If instance
     * is not running, the method will return false. The return value is
     * accurate within the timespan as specified in getClientRefreshInterval()
     * of AdminChannel -- meaning that if the time period since instance restart
     * is less than the specified timespan then the method may return false
     * instead of true.
     * @param ts Timestamp to check for
     * @return true if the instance has restarted since specified timestamp,
     *    false otherwise.
     */
    public boolean hasRestartedSince(long ts) {
        return hasRestartedSince(ts, false);
    }

    /**
     * Has the server instance restarted since specified timestamp. If instance
     * is not running, the method will return false. If refreshStub is true
     * then the method will attempt to read newer stub (if any) and will report
     * status using that. If refreshStub is false then the stub is not re-read
     * and return value is dependent on the cached stub (if any). 
     * @param ts Timestamp to check for
     * @param refreshStub if true, refresh remote stub if it has changed
     * @return true if the instance has restarted since specified timestamp,
     *    false otherwise.
     */
    public boolean hasRestartedSince(long ts, boolean refreshStub) {
        if (refreshStub) {
            boolean gotNew = checkServerStatus();
        }
        boolean restarted = false;
        if (stubFile != null) {
            if (stubFileTs > ts) {
                restarted = true;
            }
        }
        return restarted;
    }

    //
    // logger methods copied from AdminChannel
    //

    static void trace(Throwable t) {
        logger.log(Level.FINEST, t.getMessage(), t);
    }

    static void warn(String s) {
        logger.warning(s);
    }

    static void warn(String msgkey, String obj1) {
        logger.log(Level.WARNING, msgkey, obj1);
    }

    static void debug(String s) {
        logger.fine(s);
    }

    static void debug(String msgkey, String obj1) {
        logger.log(Level.FINE, msgkey, obj1);
    }

    static void debug(String msgkey, Object[] objarr) {
        logger.log(Level.FINE, msgkey, objarr);
    }

    static void debug(Throwable t) {
        logger.log(Level.FINE, t.getMessage(), t);
    }

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( RMIClient.class );

    private static final String CLIENT_NULLARGS_ERRCODE =
            "channel.client_nullargs";
    private static final String CLIENT_NULLARGS_ERRMSG =
			localStrings.getString( "admin.server.core.channel.attempt_initializing_channel_client_with_null_arguments" );
    private static final String CLIENT_INIT_ERROR = "channel.client_init_error";
    private static final String EVENT_NOTIFY_ERROR =
            "channel.event_notify_error";
    private static final String EVENT_RENOTIFY_ERROR =
            "channel.event_renotify_error";
    private static final String AUTO_REFRESH_INTR = "channel.auto_refresh_intr";
    private static final String CHANNEL_COMM_ERROR = "channel.comm_error";
    private static final String INVALID_AUTO_REFRESH_INTERVAL =
            localStrings.getString(
                    "admin.server.core.channel.invalid_auto_refresh_interval");

    static final String FILE_READ_ERROR = "channel.file_read_error";
    static final String SEED_FILE_OLDER = "channel.seed_file_older";

    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
}
