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

package com.sun.ejb.ee.sfsb.store;

import com.sun.enterprise.ee.web.sessmgmt.StorePoolElement;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;

/**
 *
 * <p>Company: Sun Microsystems Inc.</p>
 * @author Sridhar Satuloori <Sridhar.Satuloori@Sun.Com>
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 *
 */

public abstract class EJBStoreBase implements StorePoolElement {
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "EJBStoreBase/1.0";

    /**
     * Name to register for the background thread.
     * NOTE: subclasses modify this variable
     */
    protected String threadName = "EJBStoreBase";

    /**
     * Name to register for this Store, used for logging.
     */
    private static final String storeName = "EJBStoreBase";

    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    /**
     * The number of seconds to wait before timing out a transaction
     * Default is 5 minutes.
     * NOTE: subclasses modify this variable 
     */
    protected String timeoutSecs = new Long(5 * 60).toString();

    /**
     * This is the cluster id for which this instance belongs to
     */
    protected String clusterID = null;

    /** Manager associated with the store
     */
    private SFSBStoreManager manager;

    // ------------------------------------------------------------- Properties

    /** How long to wait in seconds before giving up on a transaction
     * @param timeoutSecs time in seconds
     */
    public void setTimeoutSecs(String timeoutSecs) {
        this.timeoutSecs = timeoutSecs;
    }

    /** Return the time to wait in seconcs
     * @return Returns the time in seconds
     */
    public String getTimeoutSecs() {

        return this.timeoutSecs;
    }

    /** Return the info for this Store.
     * @return returns info of this class
     */
    public String getInfo() {
        return (info);
    }

    /** Return the thread name for this Store.
     * @return Returns the thread name
     */
    public String getThreadName() {
        return (threadName);
    }

    /** Return the name for this Store, used for logging.
     * @return Returns the name of the store
     */
    public String getStoreName() {
        return (storeName);
    }

    /**
     * Set the debugging detail level for this Store.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    /** Return the debugging detail level for this Store.
     * @return return the value of debug
     */
    public int getDebug() {
        return (this.debug);
    }

    /** ClusterID for the application to which this store is attached to
     * @return returns the clusterid
     */
    public String getClusterID() {
        return clusterID;
    }

    /** Sets the cluserid
     * @param clusterid String clusterid
     */
    public void setClusterID(String clusterid) {
        this.clusterID = clusterid;
    }

    /** Constructor
     */
    public EJBStoreBase() {
    }

    /** Returns the manager for this store
     * @return returns the manager for this store
     */
    public SFSBStoreManager getSFSBStoreManager() {
        return manager;
    }

    /** Sets the manager for this store
     * @param mgr
     */
    public void setSFSBStoreManager(SFSBStoreManager mgr) {
        manager = mgr;
    }
}