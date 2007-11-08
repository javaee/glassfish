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
package com.sun.enterprise.admin.server.core.channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteStub;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.server.ss.ASSocketService;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * RMI server object for admin channel. 
 */
public class AdminChannelServer extends UnicastRemoteObject
        implements RemoteAdminChannel {

    private String localAddress = null;

    private byte[] myKey;

    /**
     * Server instance status. Its value is one of the constant <code>
     * kInstanceStartingCode, kInstanceRunningCode or kInstanceStoppingCode
     * </code> from the class <code>com.sun.enterprise.admin.common.Status</code>.
     */
    private int instanceStatus = Status.kInstanceStartingCode;

    /**
     * Is restart needed on this server instance to sync it up with persistent
     * configuration.
     */
    private boolean restartNeeded = false;

    private int conflictedPort = 0;

    /**
     * Create a new RMI server object for admin channel
     */
    public AdminChannelServer() throws RemoteException {
        super();

        // read from the persisted state
        restartNeeded = RRStateFactory.getState();
    }

    /**
     * Create a new RMI server object for admin channel that uses specified
     * socket factories.
     */
    public AdminChannelServer(int port, RMIClientSocketFactory csf,
            RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);

        // read from the persisted state
        restartNeeded = RRStateFactory.getState();
    }

    /**
     * Send event notification.
     */
    public AdminEventResult sendNotification(byte[] key, AdminEvent event)
            throws RemoteException {
        if (!checkAccess()) {
			String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
			String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        return AdminEventMulticaster.multicastEvent(event);
    }

    /**
     * Ping server. If the method call succeeds, notifications can be sent.
     */
    public boolean pingServer(byte[] key) throws RemoteException {
        if (!checkAccess()) {
			String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
			String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        return true;
    }

    /**
     * Get server status code. This method will return one of the following
     * constants from class <code>com.sun.enterprise.admin.common.Status</code>
     * -- <code>kInstanceStartingCode, kInstanceRunningCode or
     * kInstanceStoppingCode</code> representing starting, running and stopping
     * condition for the instance. 
     * @param key shared secret
     * @returns server status code denoting whether server is starting,
     *     running or stopping.
     */
    public int getServerStatusCode(byte[] key) throws RemoteException {
        if (!checkAccess()) {
			String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
			String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        return instanceStatus;
    }

    /**
     * Is restart needed to use persistent server configuration. After a
     * notification, the server may be in inconsistenet state with respect
     * to persistent configuration because all changes to configuration can
     * not be handled dynamically - A restart is needed in such cases to
     * synchronize server with persistent configuration.
     * @param key shared secret
     * @return true if restart is required, false otherwise.
     */
    public boolean isRestartNeeded(byte[] key) throws RemoteException {
        if (!checkAccess()) {
			String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
			String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        return restartNeeded;
    }

    /**
     * Set restart needed status on server instance.
     * @param key shared secret
     * @param needRestart true if the instance should be restarted to use
     *     changes in persistent configuration.
     */
    public void setRestartNeeded(byte[] key, boolean needRestart)
            throws RemoteException {
        if (!checkAccess()) {
			String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
			String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }

        try {
            // persists the state to a file
            RRStateFactory.saveState(needRestart);
        } catch (IOException ioe) {
			String msg = localStrings.getString(
                "admin.server.core.channel.unable_saving_state_file");
            throw new RuntimeException(msg, ioe);
        }

        restartNeeded = needRestart;
    }

    /**
     * Returns the port number that caused conflict. This could be 
     * 0, if port-conflict is not the cause of failure.
     * @param key shared secret
     * @return port number.
     */
    public int getConflictedPort(byte[] key) {
        if (!checkAccess()) {
                        String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
                        String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        return conflictedPort;
    }

    /**
     * Client will exit after calling this method. Notify the lock held 
     * in ASSocketService.
     * @param key shared secret
     */
    public void triggerServerExit(byte[] key) {
        if (!checkAccess()) {
                        String msg = localStrings.getString( "admin.server.core.channel.unauthorized_access" );
            throw new SecurityException( msg );
        }
        if (!keyMatches(key)) {
                        String msg = localStrings.getString( "admin.server.core.channel.invalid_key" );
            throw new IllegalArgumentException( msg );
        }
        ASSocketService.triggerServerExit();
    }

    /**
     * Set shared secret that clients must specify in every remote call. This
     * is set at startup.
     */
    void setSharedInfo(byte[] seed) {
        myKey = seed;
    }

    /**
     * Get remote stub for the server object.
     */
    RemoteStub getRemoteStub() throws NoSuchObjectException {
        return (RemoteStub)RemoteObject.toStub(this);
    }

    /**
     * Set the address that clients will be checked against. This is set at
     * startup.
     */
    void setLocalAddress(InetAddress address) {
        localAddress = address.getHostAddress();
    }

    /**
     * Set channel to starting state. 
     */
    void setChannelStarting() {
        this.instanceStatus = Status.kInstanceStartingCode;
    }

    /**
     * Set channel to ready (running) state.
     */
    void setChannelReady() {
        this.instanceStatus = Status.kInstanceRunningCode;
    }

    /**
     * Set channel to stopping state.
     */
    void setChannelStopping() {
        this.instanceStatus = Status.kInstanceStoppingCode;
    }

    /**
     * Set the channel to failed state.
     *
     * @conflictedPort Port that causing conflict. This could be 0
     * if the reason for failure is not port-conflict.
     */
    void setChannelAborting(int conflictedPort) {
        this.conflictedPort = conflictedPort;
        this.instanceStatus = Status.kInstanceFailedCode;
    }

    /**
     * Verify that client is coming from the same IP address as the server.
     */
    private boolean checkAccess() {
        boolean allowed = true;
        String addr = null;
        if (AdminChannel.LOCAL_ONLY_ACCESS.equals(AdminChannel.getAccessLevel())) {
            boolean matchAddress = true;
            try {
                addr = this.getClientHost();
                if (addr == null) {
                    AdminChannel.warn(CLIENT_HOST_NULL);
                    allowed = false;
                    matchAddress = false;
                }
            } catch (ServerNotActiveException snae) {
                AdminChannel.warn(LOCAL_ACCESS);
                AdminChannel.debug(snae);
                matchAddress = false;
            }
            if (matchAddress) {
                allowed = addressMatches(addr);
            }
        }
        if (!allowed) {
            AdminChannel.debug(ADDR_MISMATCH,
                    new Object[] {addr, getLocalAddress()});
        }
        return allowed;
    }

    /**
     * Check whether local address of the server object is same as specified
     * address.
     */
    private boolean addressMatches(String addr) {
        String localAddr = getLocalAddress();
        if (localAddr == null) {
            return false;
        }
        return addr.equals(localAddr);
    }

    /**
     * Get local address for the server object. If local address has not been
     * initialized, it is initialized using <code>InetAddress.getLocalHost()
     * </code>.
     */
    private String getLocalAddress() {
        if (localAddress == null) {
            InetAddress inetAddr = null;
            try {
                inetAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException uhe) {
                AdminChannel.warn(NO_LOCAL_HOST);
                AdminChannel.debug(uhe);
            }
            if (inetAddr != null) {
                localAddress = inetAddr.getHostAddress();
            }
        }
        return localAddress;
    }

    /**
     * Check whether shared secret for the server object matches the specified
     * key.
     */
    private boolean keyMatches(byte[] key) {
        boolean matches = true;
        if (AdminChannel.ENFORCE.equals(AdminChannel.getKeyCheckLevel())) {
            matches = checkKeyLength(key);
            for (int i = 0; matches && i < AdminChannel.SEED_LENGTH; i++) {
                if (key[i] != myKey[i]) {
                    matches = false;
                }
            }
        } else if (AdminChannel.REQUIRE_KEY.equals(AdminChannel.getKeyCheckLevel())) {
            matches = checkKeyLength(key);
        }
        if (!matches) {
            AdminChannel.debug(KEY_MISMATCH,
                    new Object[] {new String(key), new String(myKey)});
        }
        return matches;
    }

    /**
     * Check whether specified key is of correct length.
     */
    private boolean checkKeyLength(byte[] key) {
        return (key.length == AdminChannel.SEED_LENGTH);
    }

    private final static String CLIENT_HOST_NULL = "channel.client_host_null";
    private final static String LOCAL_ACCESS = "channel.local_access";
    private final static String ADDR_MISMATCH = "channel.addr_mismatch";
    private final static String NO_LOCAL_HOST = "channel.no_local_host";
    private final static String KEY_MISMATCH = "channel.key_mismatch";

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminChannelServer.class );
}
