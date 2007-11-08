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
 * HaWebmethodSessionStrategyBuilder.java
 *
 * Created on September 30, 2002, 2:12 PM
 */

package com.sun.enterprise.ee.web.initialization;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import com.sun.enterprise.deployment.runtime.web.SessionManager;

import com.sun.enterprise.web.BasePersistenceStrategyBuilder;
import com.sun.enterprise.web.PersistenceStrategyBuilder;
//START OF 6364900
import com.sun.enterprise.web.PESessionLocker;
//END OF 6364900
import com.sun.enterprise.ee.web.sessmgmt.HAStore;
import com.sun.enterprise.ee.web.sessmgmt.HAContainerListener;
import com.sun.enterprise.ee.web.sessmgmt.HASessionStoreValve;
import com.sun.enterprise.ee.web.sessmgmt.HAWebEventPersistentManager;
//import com.sun.appserv.ee.web.sessmgmt.HAStorePool;
import com.sun.enterprise.ee.web.sessmgmt.StorePool;
import com.sun.enterprise.ee.web.sessmgmt.FullSessionFactory;
//import com.sun.appserv.ee.web.sessmgmt.HAStoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.StoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.HAFullSessionStoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.SessionLockingStandardPipeline;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.util.uuid.UuidGenerator;


/**
 *
 * @author  lwhite
 */
public class HaWebmethodSessionStrategyBuilder extends BasePersistenceStrategyBuilder implements PersistenceStrategyBuilder {
    
    /** Creates a new instance of HaWebmethodSessionStrategyBuilder */
    public HaWebmethodSessionStrategyBuilder() {
    }     

    /**
     * initialize & configure the correct persistence strategy
     * including manager, store, uuid generator
     *
     * @param ctx
     * @param smBean
     */      
    public void initializePersistenceStrategy(Context ctx, SessionManager smBean) {
        super.initializePersistenceStrategy(ctx, smBean);
        
        _logger.finest("IN HaWebmethodSessionStrategyBuilder");
        _logger.finest("IN HaWebmethodSessionStrategyBuilder NEW");
        System.out.println("IN HaWebmethodSessionStrategyBuilder NEW");
        String persistenceType = "ha";
        String persistenceFrequency = "web-method";
        String persistenceScope = "session";
        Object[] params = { ctx.getPath(), persistenceType, persistenceFrequency, persistenceScope };
        _logger.log(Level.INFO, "webcontainer.haPersistence", params);
        HAWebEventPersistentManager mgr = new HAWebEventPersistentManager();
        mgr.setMaxActiveSessions(maxSessions);  //FIXME: put this back
        //mgr.setCheckInterval(reapInterval);
        mgr.setMaxIdleBackup(0);           // FIXME: Make configurable

        HAStore store = new HAStore();
        //store.setCheckInterval(storeReapInterval);    //FIXME: put this back
        mgr.setStore(store);
        
        //in the future can set other implementations
        //of UuidGenerator in server.xml
        //even if not set it defaults to UuidGeneratorImpl
        ServerConfigLookup lookup = new ServerConfigLookup();
        UuidGenerator generator = lookup.getUuidGeneratorFromConfig();
        mgr.setUuidGenerator(generator);
        _logger.finest("UUID_GENERATOR = " + generator); 
        
        //for intra-vm session locking
        _logger.finest("sctx.restrictedSetPipeline(new SessionLockingStandardPipeline(sctx))");
        StandardContext sctx = (StandardContext) ctx;
        sctx.restrictedSetPipeline(new SessionLockingStandardPipeline(sctx));
        
        //special code for Java Server Faces
        if(sctx.findParameter(JSF_HA_ENABLED) == null) {
            sctx.addParameter(JSF_HA_ENABLED, "true");
        }         
        //START OF 6364900
        mgr.setSessionLocker(new PESessionLocker(ctx));
        //END OF 6364900        
        ctx.setManager(mgr);

        //this must be after ctx.setManager(mgr);
        if(!sctx.isSessionTimeoutOveridden()) {
           mgr.setMaxInactiveInterval(sessionMaxInactiveInterval); 
        }
        
        //add SessionFactory
        mgr.setSessionFactory(new FullSessionFactory());
        
        //add HAStorePool
        ServerConfigReader configReader = new ServerConfigReader();

        int haStorePoolSize = configReader.getHAStorePoolSizeFromConfig();
        int haStorePoolUpperSize = configReader.getHAStorePoolUpperSizeFromConfig();
        int haStorePoolPollTime = configReader.getHAStorePoolPollTimeFromConfig();        

        /*
        HAStoreFactory haStoreFactory = new HAFullSessionStoreFactory();
        HAStorePool storePool = 
            new HAStorePool(haStorePoolSize, haStorePoolUpperSize, 
                haStorePoolPollTime, haStoreFactory);
        mgr.setHAStorePool(storePool);
         */
        
        StoreFactory haStoreFactory = new HAFullSessionStoreFactory();
        StorePool storePool = 
            new StorePool(haStorePoolSize, haStorePoolUpperSize, 
                haStorePoolPollTime, haStoreFactory);
        mgr.setStorePool(storePool);        
                
        //add HASessionStoreValve
        HASessionStoreValve hadbValve = new HASessionStoreValve();
        StandardContext stdCtx = (StandardContext) ctx;
        stdCtx.addValve(hadbValve);
        
        //clear store
        //this was wrong
        //mgr.clearStore();
                
    }            
    
}
