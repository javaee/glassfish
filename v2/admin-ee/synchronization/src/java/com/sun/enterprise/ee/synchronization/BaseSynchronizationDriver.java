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
package com.sun.enterprise.ee.synchronization;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.audit.AuditMgr;
import com.sun.enterprise.ee.synchronization.tx.Transaction;
import com.sun.enterprise.ee.synchronization.tx.TransactionManager;
import com.sun.enterprise.ee.synchronization.cleaner.CacheRepositoryCleanerMain;
import com.sun.enterprise.admin.server.core.channel.RRStateFactory;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

// for TLS while talking to DAS
import com.sun.enterprise.admin.server.core.jmx.ssl.AsTlsClientEnvSetter;

import com.sun.enterprise.ee.synchronization.audit.AuditException;
import com.sun.enterprise.ee.synchronization.NonMatchingDASContactedException;
/**
 * This synchronization driver implementation synchronizes the 
 * client cache with central repository. It executes the synchronization
 * requests in a multi-threaded environment.
 * 
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public abstract class BaseSynchronizationDriver 
        implements SynchronizationDriver {
    
    protected final static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);

    static final StringManager _localStrMgr = 
            StringManager.getManager(BaseSynchronizationDriver.class);

    /** 
     * Returns true if this driver understands the subprotocol
     * specified in the URL.
     *
     * @param  url  the URL to the repository management system
     * @return      true if this driver understands the supprotocol
     */
    public boolean acceptsURL(String url) {

        if (SynchronizationDriverFactory.INSTANCE_CONFIG_URL.equals(url)
            || SynchronizationDriverFactory.NODE_AGENT_CONFIG_URL.equals(url)
            || SynchronizationDriverFactory.
                NODE_AGENT_STARTUP_CONFIG_URL.equals(url) ) {

            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns this driver's major version number.
     *
     * @return  major version number of this driver
     */
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Returns this driver's minor version number.
     *
     * @return  minor version number of this driver
     */
    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    /**
     * Attempts to synchronize the client (server instance, node agent) 
     * with the central repository.
     *
     * @throws  SynchronizationException  if an error occurred during the 
     *                                    synchronization process
     */
    public void synchronize() throws SynchronizationException {

        try {

            synchronizeInternal(); 

        } catch (DASCommunicationException dce) {
            throw dce;

        } catch (SynchronizationException e) {

            _logger.log(Level.INFO, 
                "synchronization.retry_synchronization");

            // synchronization failed, re-try
            synchronizeInternal(); 
        }
    }
    
        private String
    secondsString( final double d, final int numDecimalPlaces ) {
        final String format = "%." + numDecimalPlaces + "f";
        return String.format( format, d );
    }

    /**
     * Attempts to synchronize the client (server instance, node agent) 
     * with the central repository. If DAS does not respond to the ping, 
     * it throws a communication exception. When synchronization is 
     * complete, it activates the repository cache cleaner. An audit 
     * is performed when client is a server instance.
     *
     * @throws  SynchronizationException  if an error occurred during the 
     *                                    synchronization process
     */
    void synchronizeInternal() throws SynchronizationException {

        final long startTime = System.currentTimeMillis();
        try {
            TransactionManager txMgr=TransactionManager.getTransactionManager();

            // pings the central repository admin
            Ping pc = getPingCommand();
            pc.execute();

            if ( pc.isAlive() ) { // central repository admin is responding
                SynchronizationRequest[] reqs = getAllRequests(pc);

                assert(reqs.length != 0);
                assert(_dpr != null);

                // begin a transaction for the synchronization
                Transaction tx = txMgr.begin(reqs.length); 

                // requests from the meta data
                RequestMediator[] mReqs=new RequestMediator[reqs.length];

                // total synchronization request threads
                Thread[] reqThreads = new Thread[reqs.length];

                // starts all the synchronization request threads
                for (int i=0; i<reqs.length; i++) {

                    mReqs[i] = getRequestMediator(reqs[i], tx); 
                    reqThreads[i] = new Thread(mReqs[i], THREAD_NAME+i);
                    reqThreads[i].start();
                }

                // waits for all the threads to finish
                for (int i=0; i<reqThreads.length; i++) {
                    reqThreads[i].join();
                }

                String configDir = System.getProperty("com.sun.aas.instanceRoot") +
                    File.separator + "config";
                java.io.File nssFile = new File(configDir, "key3.db");
                if (!nssFile.exists()) {
                    if (System.getProperty("javax.net.ssl.keyStore") == null) {
                        System.setProperty("javax.net.ssl.keyStore", configDir +
                            File.separator + "keystore.jks");
                    }
                    if (System.getProperty("javax.net.ssl.trustStore") == null) {
                        System.setProperty("javax.net.ssl.trustStore", configDir +
                            File.separator + "cacerts.jks");
                    }
                }

                for (int i=0; i<reqThreads.length; i++) {
                    // check for exceptions
                    if ( mReqs[i].isException() ) {
                        throw mReqs[i].getException();
                    }
                    else {
                        _logger.log(Level.FINE, 
                            "synchronization.done_request",
                            mReqs[i].getRequest().getMetaFileName());                
                    }
                }

                // logs the total time spent for synchronization
                final double elapsedSeconds =
                    (System.currentTimeMillis()-startTime)/1000.0;
                _logger.log(Level.INFO, "synchronization.time_taken", 
                        secondsString(elapsedSeconds, 3));

                // calls repository cleaner if dealing with a server instance
                if (SynchronizationDriverFactory.INSTANCE_CONFIG_URL.
                        equals(_metaFile)) {
                    
                    final long cleanerStartMillis = System.currentTimeMillis();

                    CacheRepositoryCleanerMain cleanerMain = 
                                CacheRepositoryCleanerMain.getInstance();
                    //cleanerMain.start();

                    // waits for the cleaner to finish
                    cleanerMain.run(mReqs);

                    // remove the restart required state file
                    RRStateFactory.removeStateFile();

                    final double cleanerSeconds =
                        (System.currentTimeMillis()-cleanerStartMillis)/1000.0;
                    _logger.log(Level.INFO,"synchronization.cleaner.time_taken",
                            secondsString(cleanerSeconds,3));

                    try {
                        AuditMgr aMgr = new AuditMgr(reqs);
                        aMgr.auditServer(_dpr);
                    } catch (AuditException aEx) {
                        _logger.log(Level.SEVERE,
                            "synchronization.audit_fail", aEx);
                    }
                }

            } else {

                String msg = _localStrMgr.getString("dasCommunicationError");
                // include the original exception from ping
                if (pc != null) {
                    Object pingResult = pc.getResult();
                    if (pingResult instanceof NonMatchingDASContactedException) {
                        NonMatchingDASContactedException oriEx = 
                            (NonMatchingDASContactedException) pingResult;
                        throw oriEx;
                    }
                    _logger.log(Level.INFO, 
                        "synchronization.skipping_synchronization");
                    if (pingResult instanceof java.lang.Exception) {
                        Exception oriEx = (Exception) pingResult;
                        throw new DASCommunicationException(msg, oriEx);
                    }
                    throw new DASCommunicationException(msg);
                }
            }
        } catch (DASCommunicationException dce) {
            throw dce;
        } catch (NonMatchingDASContactedException nmde) {
            throw nmde;
        } catch (Exception e) {
            _logger.log(Level.FINE,"synchronization.sync_fail", e);

            // throw the exception for client to handle business logic
            throw new SynchronizationException(e);
        }
    }

    /**
     * Returns a concrete implementation of Ping interface that 
     * can determine if DAS is alive.
     *
     * @return concrete implementation of Ping interface
     */
    protected abstract Ping getPingCommand();

    /**
     * Returns a concrete implementation of RequestMediator interface.
     * 
     * @param  req  synchronization request object
     * @param  tx   a synch transaction 
     */
    protected abstract RequestMediator getRequestMediator(
            SynchronizationRequest req, Transaction tx);

    private SynchronizationRequest[] getAllRequests(Ping pc) 
            throws IOException {

        // synchronization requests
        SynchronizationRequest[] reqs = getRequests();

        ArrayList list = new ArrayList();
        list.addAll( Arrays.asList(reqs) );

        // application requests
        Object pr = (SynchronizationPingResponse) pc.getResult();

        if ( (pr != null) && (pr instanceof SynchronizationPingResponse)) {
            SynchronizationPingResponse pingResponse = 
                            (SynchronizationPingResponse) pr;

            List appReqs = pingResponse.getApplicationSynchRequests();
            Iterator iter = appReqs.iterator();
            while (iter.hasNext()) {
               ApplicationSynchRequest aReq =
                    (ApplicationSynchRequest) iter.next();
               list.addAll( aReq.toSynchronizationRequest() );
            }
        }

        SynchronizationRequest[] allReqs = 
            new SynchronizationRequest[list.size()];
        return ((SynchronizationRequest[]) list.toArray(allReqs));
    }

    /**
     * Returns the synchronization requests objects from the meta file.
     *
     * @return  an array of synchronization requests
     * @throws  IOException  if an i/o error while persing the meta file
     */
    private SynchronizationRequest[] getRequests() throws IOException {

        assert(_metaFile != null);
        SynchronizationConfig sConfig = new SynchronizationConfig(_metaFile);
        SynchronizationRequest[] requests = sConfig.getSyncRequests();

        return requests;
    }

    // ---- VARIABLE(S) - PRIVATE ----------------------------
    protected String _metaFile       = null;
    protected String _instanceRoot   = null;
    protected DASPropertyReader _dpr = null;
    static final int MAJOR_VERSION   = 9;
    static final int MINOR_VERSION   = 1;
    static final String THREAD_NAME  = "sync-";
}
