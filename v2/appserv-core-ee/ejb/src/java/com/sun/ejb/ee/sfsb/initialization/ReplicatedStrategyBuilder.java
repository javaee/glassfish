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
 * ReplicatedStrategyBuilder.java
 *
 * Created on May 31, 2006, 5:19 PM
 *
 */

package com.sun.ejb.ee.sfsb.initialization;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.ejb.spi.sfsb.initialization.PersistenceStrategyBuilder;
import com.sun.ejb.spi.sfsb.initialization.SFSBContainerInitialization;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.ejb.base.sfsb.util.EJBServerConfigLookup;
import com.sun.ejb.base.sfsb.initialization.AbstractPersistenceStrategyBuilder;
import com.sun.ejb.base.container.util.CacheProperties;

import com.sun.ejb.ee.sfsb.store.ReplicatedSFSBStoreManager;
import com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationReceiver;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationMessageRouter;


/**
 *
 * @author Larry White
 */
public class ReplicatedStrategyBuilder extends AbstractPersistenceStrategyBuilder implements PersistenceStrategyBuilder {
    
    private static final String REPLICATED_TYPE = "replicated";
    
    /** Creates a new instance of ReplicatedStrategyBuilder */
    public ReplicatedStrategyBuilder() {
    }
    
    public void initializePersistenceStrategy(
        SFSBContainerInitialization container, EjbDescriptor descriptor) {
        //put initialization code here
        //get containerId
        long containerId = descriptor.getUniqueId();
        
	try {
	    super.initializePersistenceStrategy(container, descriptor);

	    //SFSBStoreManager storeManager = new HASFSBStoreManager();
            ReplicatedSFSBStoreManager storeManager = new ReplicatedSFSBStoreManager();
            storeManager.setPassedInPersistenceType(getPassedInPersistenceType());
            initStoreManager(storeManager, descriptor);
	    container.setSFSBStoreManager(storeManager);
            
            //FIXME - register with health checker 
            //register with jxta receiver
            String contIdString = this.getContainerIDAsString(containerId);
            //System.out.println("ReplicatedEjbStrategyBuilder:registering container = " + contIdString);
            //System.out.println("ReplicatedEjbStrategyBuilder:registering containerId length = " + contIdString.length());            
            ReplicationMessageRouter router = ReplicationMessageRouter.createInstance();
            if(router != null) {
                router.addReplicationManager(contIdString, storeManager);
                //ReplicationMessageRouter.getReplicationAppIds(true);
            } 
            
	} catch (Throwable th) {
	    _logger.log(Level.SEVERE, "Could not initialize container "
                + "using ReplicatedStrategyBuilder", th);
	}
        
        //if we are doing replication
        //initialize jxta pipes if they haven't been already
        String passedInPersistenceType = getPassedInPersistenceType();
        if(REPLICATED_TYPE.equalsIgnoreCase(passedInPersistenceType)) {
            JxtaReplicationReceiver receiver
                = JxtaReplicationReceiver.createInstance();
            receiver.doPipeInitialization();
        }        
    }
    
    private void initStoreManager(SFSBStoreManager storeManager, 
        EjbDescriptor ejbDescriptor) {
        //get idleTimoutInSeconds
        CacheProperties desc = new CacheProperties(ejbDescriptor);
        int idleTimeoutInSeconds = desc.getRemovalTimeoutInSeconds();
        //get containerId
        long containerId = ejbDescriptor.getUniqueId();
        //get clusterId
        EJBServerConfigLookup configLookup = new EJBServerConfigLookup(ejbDescriptor);
        String clusterId = configLookup.getClusterName();
        Map initMap = new HashMap();
  
        initMap.put("idleTimeoutInSeconds", new Integer(idleTimeoutInSeconds));
        initMap.put("containerId", new Long(containerId));
        initMap.put("clusterId", clusterId);
        try {
            storeManager.initSessionStore(initMap);
        } catch (SFSBStoreManagerException ex) {}
    }
    
    public String getContainerIDAsString(long containerID) {
        Long longContId = new Long(containerID);
        return longContId.toString();
    }    
    
    public void setLogger(Logger logger) {
    }    
    
}
