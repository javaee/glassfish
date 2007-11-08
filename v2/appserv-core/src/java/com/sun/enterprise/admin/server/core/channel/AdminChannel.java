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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.server.Constants;
import com.sun.appserv.server.ServerLifecycleException;

import com.sun.enterprise.util.SystemPropertyConstants;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Admin channel is used for communication between admin service agents
 * running in different server instances.
 */
public class AdminChannel {

    /**
     * A reference to logger object
     */
    static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    static volatile String instanceRoot = null;
    
    static final String fileSeparator = "/";

    static final int SEED_LENGTH = 16;

    private static volatile AdminChannelServer server = null;

    private static final Map<String,RMIClient> rmiClientMap = new HashMap<String,RMIClient>();

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminChannel.class );

    /**
     * Create a RMI channel. This method creates a server object and exposes
     * the stub on local filesystem.
     */
    public static synchronized void createRMIChannel() throws ServerLifecycleException {
        try {
            server = createServerObject();
            saveStubToFile(server.getRemoteStub());
        } catch (Exception e) {
            warn(SERVER_CREATION_ERRCODE);
            debug(e);
            throw new ServerLifecycleException(e);
        }
    }

    /**
     * Remove RMI channel. Remove the server object from JVM (do not accept
     * any more calls and abort in process calls) and clean up the stub
     * exposed on filesystem.
     */
    public static synchronized void destroyRMIChannel() throws ServerLifecycleException {
        if (server != null) {
            server.setChannelStopping();
            try {
                UnicastRemoteObject.unexportObject(server, true);
            } catch (NoSuchObjectException nsoe) {
                throw new ServerLifecycleException(nsoe);
            }
        }
        deleteStubFile();
    }

    /**
     * Create a shared secret. The shared secret is saved on the filesystem
     * and is verified during every call on admin channel.
     */
    public static synchronized void createSharedSecret() throws ServerLifecycleException {        
        assertAdminServerChannelNotNull();
        String fileName = getSeedFileName();
        File seedFile = new File(fileName);
        byte[] prevSeed = getPreviousSeed(seedFile);
        SecureRandom sr = new SecureRandom(prevSeed);
        byte[] seed = new byte[SEED_LENGTH];
        sr.nextBytes(seed);
        saveSeedToFile(seed, seedFile);
        server.setSharedInfo(seed);
        server.setChannelStarting();
    }

    /**
     * Enable reconfiguration of Sun ONE Web Server core. 
     */
    public static void enableWebCoreReconfig() {
        try {
            ReconfigHelper.enableWebCoreReconfig();
        } catch (Throwable t) {
            // If reconfiguration could not be enabled log a warning
            // message and continue
            warn(RECONFIG_ENABLE_ERROR );
            debug(t);
        }
    }

    /**
     * Get RMI client for specified instance.
     */
    public static synchronized RMIClient getRMIClient(String instanceName) {
        //KE FIXME: All of this code is obsolete whent the stub file 
        //is removed.
        RMIClient client = (RMIClient)rmiClientMap.get(instanceName);
        if (client == null) {
            client = new RMIClient(getStubFileName(),
                    getSeedFileName());
            rmiClientMap.put(instanceName, client);
        }
        return client;
    }

    /**
     * Set channel to ready state. This means that the server instance that
     * initialized the channel is ready to serve client requests.
     * @throws RuntimeException if the channel has not been initialized
     */
    public static synchronized void setRMIChannelReady() {
        assertAdminServerChannelNotNull();
        server.setChannelReady();
    }

    public static synchronized void setRMIChannelStopping() {
        assertAdminServerChannelNotNull();
        server.setChannelStopping();
    }

    /**
     * Set the channel to failed state. If the client detects this state, then
     * it will try to get the port number that caused failure from the channel.
     * @param port port number.
     */
    public static void setRMIChannelAborting(int port) {
        assertAdminServerChannelNotNull();
        server.setChannelAborting(port);
    }
    
    static final String stubFileName = "admch";

    //Begin EE: 4921345 instanceRoot cannot be statically initialized since it relies on
    //a system property which may not be set until startup time. This removes the
    //dependency ond AdminService.
    static String getInstanceRoot() {
        if (instanceRoot == null) {            
            instanceRoot = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        }
        return instanceRoot;
    }
    //End EE: 4921345 instanceRoot cannot be statically initialized since it relies on
    //a system property which may not be set until startup time. This removes the
    //dependency ond AdminService.
    
    static String getStubFileName() {
        return getInstanceRoot() + fileSeparator 
                + Constants.CONFIG_DIR_NAME + fileSeparator
                + stubFileName;
    }

    static final String seedFileName = "admsn";

    static String getSeedFileName() {
        return getInstanceRoot() + fileSeparator 
                + Constants.CONFIG_DIR_NAME + fileSeparator
                + seedFileName;
    }

    /**
     * Create server object that serves RMI client. If local loopback address
     * can be determined the server object listens on only local loopback
     * address, otherwise it listens on all interfaces (default RMI behavior).
     */
    private static AdminChannelServer createServerObject()
            throws RemoteException {
        AdminChannelServer server = null;
        InetAddress localAddress = getLocalLoopbackAddress();
        if (localAddress == null) {
            server = new AdminChannelServer();
        } else {
            LocalRMIClientSocketFactory csf =
                    new LocalRMIClientSocketFactory(localAddress);
            LocalRMIServerSocketFactory ssf =
                    new LocalRMIServerSocketFactory(localAddress);
            int port = 0;
            port = Integer.getInteger(
                PROP_SERVER_PORT, new Integer(port)).intValue();
            server = new AdminChannelServer(port, csf, ssf);            
            server = new AdminChannelServer(0, csf, ssf);
            server.setLocalAddress(localAddress);
        }
        return server;
    }

    /**
     * Get local loopback address.
     * @return local loopback address, if it can be determined, null otherwise
     */
    private static InetAddress getLocalLoopbackAddress() {
        InetAddress localAddr = null;
        try {
            localAddr = InetAddress.getByName(null);
            if (!localAddr.isLoopbackAddress()) {
                localAddr = null;
            }
        } catch (Throwable t) {
            // Catch all exceptions and return null to the caller
            localAddr = null;
        }
        return localAddr;
    }

    /**
     * Get previous seed. This seed is read either from previous session's
     * shared secret file or initialized using SecureRandom.getSeed (if the
     * shared secret file does not exist). This seed is then used with
     * SecureRandom to generate next key value.
     */
    private static byte[] getPreviousSeed(File seedFile) {
        boolean haveSeed = false;
        byte[] prevSeed = new byte[SEED_LENGTH];

        // Using secure.seed bits to mix in a few extra bits of randomness
        // since we cannot use SecureRandoms built-in seeding.
        // Read bugs 4703002 and 4709460 for some background on this.
        SecureRandom sr = 
            com.sun.enterprise.server.J2EEServer.secureRandom;
        assert (sr != null);    // was initialized early on startup
        sr.setSeed(System.currentTimeMillis());
        
        if (seedFile.exists() && seedFile.canRead()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(seedFile);
                fis.read(prevSeed);
                sr.setSeed(prevSeed);
                sr.nextBytes(prevSeed);
                haveSeed = true;
            } catch (IOException ioe) {
                warn(KEY_READ_ERROR);
                debug(ioe);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        if (!haveSeed) {
            sr.nextBytes(prevSeed);
        }
        return prevSeed;
    }

    /**
     * Save shared secret in file (so that it becomes shared)
     */
    private static void saveSeedToFile(byte[] seed, File seedFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(seedFile);
            fos.write(seed);
        } catch (IOException ioe) {
            warn(KEY_WRITE_ERROR);
            debug(ioe);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * Save remote stub for admin channel server to file.
     */
    private static void saveStubToFile(RemoteStub stub) {        
        String fileName = getStubFileName();
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(stub);
            fos.close();
        } catch (Exception e) {
			String msg = localStrings.getString( "admin.server.core.channel.unable_saving_stub_to_file", fileName );	
            throw new RuntimeException( msg, e );
        }
    }

    /**
     * Cleanup stub file (is invoked on shutdown)
     */
    private static void deleteStubFile() {
        String fileName = getStubFileName();
        new File(fileName).delete();
    }

    /**
     * Assert than Admin server channel is not null.
     * @throws RuntimeException if admin server channel is null.
     */
    private static final void assertAdminServerChannelNotNull() {
        if (server == null) {
			String msg = localStrings.getString( "admin.server.core.channel.admin_server_channel_not_initialized" ); 
            throw new RuntimeException( msg );
        }
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

    static void trace(Throwable t) {
        logger.log(Level.FINEST, t.getMessage(), t);
    }

    static final String LOCAL_ONLY_ACCESS = "high";
    static final String ALLOW_ALL_ACCESS = "none";

    /**
     * Get access level. FIX to use config parameter
     */
    static String getAccessLevel() {
        return LOCAL_ONLY_ACCESS;
    }

    static final String ENFORCE = "high";
    static final String REQUIRE_KEY = "medium";
    static final String NO_ENFORCE = "low";

    /**
     * Get key check level. FIX to use config parameter.
     */
    static String getKeyCheckLevel() {
        return ENFORCE;
    }

    /**
     * Is Auto Refresh enabled for RMIClient objects (so they will keep on
     * scanning file system for changes to stub file and reset themselves).
     * FIX to use config parameter
     */
    static boolean getClientAutoRefreshEnabled() {
        return true;
    }

    /**
     * How frequently should the RMI client objects refresh themselves (in
     * milliseconds). FIX to use config parameter
     */
    static long getClientAutoRefreshInterval() {
        // 1 minute
        return (1 * 60 * 1000);
    }

    static final String RECONFIG_ENABLE_ERROR = "channel.reconfig_enable_error";
    static final String SERVER_CREATION_ERRCODE = "channel.creation_error";
    static final String KEY_READ_ERROR = "channel.key_read_error";
    static final String KEY_WRITE_ERROR = "channel.key_write_error";
    
    static final String PROP_SERVER_PORT =
        "com.sun.enterprise.admin.server.core.channel.port";     
}
