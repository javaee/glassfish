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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * HaStrategyBuilder.java
 *
 * Created on December 9, 2003, 3:55 PM
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

import com.sun.ejb.ee.sfsb.store.HASFSBStoreManager;
import com.sun.enterprise.ee.web.sessmgmt.EEHADBHealthChecker;

/**
 * Class to initialize Container with Ha based persistence
 *
 * @author lwhite
 */
public class HaStrategyBuilder extends AbstractPersistenceStrategyBuilder implements PersistenceStrategyBuilder{
    
    /** Creates a new instance of HaStrategyBuilder */
    public HaStrategyBuilder() {
    }
    
    public void initializePersistenceStrategy(
        SFSBContainerInitialization container, EjbDescriptor descriptor) {
        //put initialization code here
        //get containerId
        long containerId = descriptor.getUniqueId();
        
	try {
	    super.initializePersistenceStrategy(container, descriptor);

	    //SFSBStoreManager storeManager = new HASFSBStoreManager();
            HASFSBStoreManager storeManager = new HASFSBStoreManager();
            initStoreManager(storeManager, descriptor);
	    container.setSFSBStoreManager(storeManager);
            
            //register with health checker
            String contIdString = this.getContainerIDAsString(containerId);
            EEHADBHealthChecker.addHASFSBStoreManager(contIdString, storeManager);            

	} catch (Throwable th) {
	    _logger.log(Level.SEVERE, "Could not initialize container "
		    + "using HaStrategyBuilder", th);
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
    
    private void initStoreManagerPrevious(HASFSBStoreManager storeManager, 
        EjbDescriptor ejbDescriptor) {
        //get idleTimoutInSeconds
        CacheProperties desc = new CacheProperties(ejbDescriptor);
        int idleTimeoutInSeconds = desc.getRemovalTimeoutInSeconds();
        //get containerId
        long containerId = ejbDescriptor.getUniqueId();
        //get clusterId
        EJBServerConfigLookup configLookup = new EJBServerConfigLookup(ejbDescriptor);
        String clusterId = configLookup.getClusterName();
        storeManager.initSessionStore(clusterId, containerId,
                               idleTimeoutInSeconds);
    }
    
    public String getContainerIDAsString(long containerID) {
        Long longContId = new Long(containerID);
        return longContId.toString();
    }    
    
    public void setLogger(Logger logger) {
    }    
    
}
