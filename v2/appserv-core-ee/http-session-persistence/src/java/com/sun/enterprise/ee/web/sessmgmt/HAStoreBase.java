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
 * HAStoreBase.java
 *
 * Created on November 22, 2005, 12:34 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import org.apache.catalina.*;
import org.apache.catalina.session.*;
import org.apache.catalina.session.StoreBase;
//import org.apache.catalina.util.*;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;
import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author Larry White
 */
public abstract class HAStoreBase extends StoreBase {
    
    /** Creates a new instance of HAStoreBase */
    public HAStoreBase() {
    }
    
    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the level is one of FINE, FINER
     * or FINEST.
     * 
     */
    protected int _debug = 0;
    
    protected void debug(String message) {
        log(message);
        System.out.println(message);
    }
    
    /**
     * The current level of logging verbosity for this object.
     */
    protected Level _logLevel = null;    
    
    /**
     * Set _debug flag and _logLevel based on the log level.
     */
    protected void setLogLevel() {
        Level level = _logger.getLevel();
        _logLevel = level;

        // Determine the appropriate value our debug level
        if (level.equals(Level.FINE))
            _debug = 1;
        else if (level.equals(Level.FINER))
            _debug = 2;
        else if (level.equals(Level.FINEST))
            _debug = 5;
        else
            _debug = 0;
    }    
    
    /**
    * The application id
    */  
    protected String applicationId = null;
  
    public String getApplicationId() {
        if(applicationId != null)
            return applicationId;
        Container container = manager.getContainer();
        //System.out.println("getApplicationId():container = " + container);
        StringBuffer sb = new StringBuffer(50);
        sb.append(this.getClusterId());
        ArrayList list = new ArrayList();
        while (container != null) {
            if(container.getName() != null) {
                list.add(":" + container.getName());
            }
            container = container.getParent();
        }
        for(int i=(list.size() -1); i>-1; i--) {
            String nextString = (String) list.get(i);
            sb.append(nextString);
        }
        applicationId = sb.toString();
        return applicationId;
        /*
        String result = sb.toString();
        applicationId = result;
        //remove after test
        //extractShortApplicationIdFromApplicationId(result);
        return result;
         */
    }
    
    /**
    * Return the cluster id for this Store as defined in server.xml.
    */  
    protected String getClusterIdFromConfig() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getClusterIdFromConfig();
    }

    /**
    * The cluster id
    */  
    protected String clusterId = null;
  
    /**
    * Return the cluster id for this Store
    */ 
    protected String getClusterId() {
        if(clusterId == null)
            clusterId = getClusterIdFromConfig();
        return clusterId;
    }    
    
    //possible generic methods begin
    /**
    * Create an byte[] for the session that we can then pass to
    * the HA Store.
    *
    * @param session
    *   The session we are serializing
    *
    */
    protected byte[] getByteArray(Session session)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        //FIXME removed after review
        //((StandardSession) session).notifyHttpSessionActivationListenersSessionWillPassivate();

        IOUtilsCaller utilsCaller = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
              //oos = EJBUtils.getOutputStream(new BufferedOutputStream(bos), true);	
            //end - Bug 4832603 
            
            if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                try {
                    oos = utilsCaller.createObjectOutputStream(new BufferedOutputStream(bos), true);
                } catch (Exception ex) {}
            }
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }            

            writeSession(session, oos);
            oos.close();
            oos = null;

            obs = bos.toByteArray();
            //for monitoring
            boolean isMonitoringEnabled = ServerConfigReader.isMonitoringEnabled();
            if(isMonitoringEnabled) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN HAStoreBase>>getInputStream for monitoring");
                }
                ReplicationManagerBase mgr = (ReplicationManagerBase)this.getManager();
                WebModuleStatistics stats = mgr.getWebModuleStatistics();
                stats.processSessionSize(obs.length);
            }
            //end for monitoring
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    }
    
    //SJSAS 6406580 START
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void remove(String id) throws IOException  {

        if (_debug > 0) {
            debug("in remove");
        }

        if ( id == null )  {
            if (_debug > 0) {
                debug("In remove, got a null id");
            }
            return;
        }
        Manager mgr = this.getManager();
        if(mgr instanceof ReplicationManagerBase) {
            ReplicationManagerBase pMgr = (ReplicationManagerBase)mgr;
            pMgr.doRemove(id);
        } else {
            this.removeSynchronized(id);
        }
    }
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action. (This is the non-synchronized version of remove
    * called by a store element from the pool, not the singleton store
    * which must use removeSynchronized
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void doRemove(String id) throws IOException  {
        //over-ridden in sub-classes
    }    
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public synchronized void removeSynchronized(String id) throws IOException {
        //over-ridden in sub-classes
    }

    /**
    * return the size of the store cache
    * will be over-ridden in subclasses
    */    
    public int getSize() throws IOException {
        //FIXME
        return 0; 
    }    
    
    /**
     * get the utility class used to call into services from IOUtils
     */
    protected IOUtilsCaller getWebUtilsCaller() {
        if(webUtilsCaller == null) {
            WebIOUtilsFactory factory = new WebIOUtilsFactory();
            webUtilsCaller = factory.createWebIOUtil();            
        }
        return webUtilsCaller;
    }
          
    //possible generic methods end
    
    /**
     * A utility class used to call into services from IOUtils
     */
    private IOUtilsCaller webUtilsCaller = null;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
}
