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
 * ReplicatedWebmethodModifiedsessionStrategyBuilder.java
 *
 * Created on November 13, 2006, 12:03 PM
 *
 */

package com.sun.enterprise.ee.web.initialization;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import com.sun.enterprise.deployment.runtime.web.SessionManager;

import com.sun.enterprise.web.BasePersistenceStrategyBuilder;
import com.sun.enterprise.web.PersistenceStrategyBuilder;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver;
import com.sun.enterprise.ee.web.sessmgmt.HASessionStoreValve;
import com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationReceiver;
import com.sun.enterprise.ee.web.sessmgmt.ModifiedSessionFactory;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationWebEventPersistentManager;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationFullSessionStoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationMessageRouter;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationStore;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationState;
import com.sun.enterprise.ee.web.sessmgmt.SessionLockingStandardPipeline;
import com.sun.enterprise.ee.web.sessmgmt.StoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.StorePool;
import com.sun.enterprise.util.uuid.UuidGenerator;

/**
 *
 * @author lwhite
 */
public class ReplicatedWebmethodModifiedsessionStrategyBuilder 
        extends BasePersistenceStrategyBuilder implements PersistenceStrategyBuilder {
    
    final static String MODE_WEB = ReplicationState.MODE_WEB;
    
    /** Creates a new instance of ReplicatedWebmethodModifiedsessionStrategyBuilder */
    public ReplicatedWebmethodModifiedsessionStrategyBuilder() {
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
        if(_logger.isLoggable(Level.FINEST)) {        
            _logger.finest("IN ReplicatedWebmethodModifiedsessionStrategyBuilder");
        }
        System.out.println("IN ReplicatedWebmethodModifiedsessionStrategyBuilder NEW");
        String persistenceType = "replicated";
        String persistenceFrequency = "web-method";
        String persistenceScope = "modified-session";
        Object[] params = { ctx.getPath(), persistenceType, persistenceFrequency, persistenceScope };
        _logger.log(Level.INFO, "webcontainer.haPersistence", params);
        ReplicationWebEventPersistentManager mgr = new ReplicationWebEventPersistentManager();
        mgr.setPassedInPersistenceType(getPassedInPersistenceType());
        mgr.setMaxActiveSessions(maxSessions);  //FIXME: put this back
        //mgr.setCheckInterval(reapInterval);
        mgr.setMaxIdleBackup(0);           // FIXME: Make configurable

        ReplicationStore store = new ReplicationStore();
        //store.setCheckInterval(storeReapInterval);    //FIXME: put this back
        mgr.setStore(store);
        
        //in the future can set other implementations
        //of UuidGenerator in server.xml
        //even if not set it defaults to UuidGeneratorImpl
        ServerConfigLookup lookup = new ServerConfigLookup();
        UuidGenerator generator = lookup.getUuidGeneratorFromConfig();
        mgr.setUuidGenerator(generator);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("UUID_GENERATOR = " + generator); 
        }
        
        //for intra-vm session locking
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("sctx.restrictedSetPipeline(new SessionLockingStandardPipeline(sctx))");
        }
        StandardContext sctx = (StandardContext) ctx;
        sctx.restrictedSetPipeline(new SessionLockingStandardPipeline(sctx));
        
        //special code for Java Server Faces
        if(sctx.findParameter(JSF_HA_ENABLED) == null) {
            sctx.addParameter(JSF_HA_ENABLED, "true");
        }         
        
        ctx.setManager(mgr);      

        //this must be after ctx.setManager(mgr);
        if(!sctx.isSessionTimeoutOveridden()) {
           mgr.setMaxInactiveInterval(sessionMaxInactiveInterval); 
        }
        
        //add SessionFactory
        mgr.setSessionFactory(new ModifiedSessionFactory());
        
        //add HAStorePool
        ServerConfigReader configReader = new ServerConfigReader();

        int haStorePoolSize = configReader.getHAStorePoolSizeFromConfig();
        int haStorePoolUpperSize = configReader.getHAStorePoolUpperSizeFromConfig();
        int haStorePoolPollTime = configReader.getHAStorePoolPollTimeFromConfig();        
        
        StoreFactory haStoreFactory = new ReplicationFullSessionStoreFactory();
        StorePool storePool = 
            new StorePool(haStorePoolSize, haStorePoolUpperSize, 
                haStorePoolPollTime, haStoreFactory);
        mgr.setStorePool(storePool);
        mgr.setDuplicateIdsSemanticsAllowed(true);
                
        //add HASessionStoreValve
        HASessionStoreValve hadbValve = new HASessionStoreValve();
        StandardContext stdCtx = (StandardContext) ctx;
        stdCtx.addValve(hadbValve);
        
        //if we are doing replication
        //initialize jxta pipes if they haven't been already
        String passedInPersistenceType = getPassedInPersistenceType();
        if(EEPersistenceTypeResolver.REPLICATED_TYPE.equalsIgnoreCase(passedInPersistenceType)) {
            JxtaReplicationReceiver receiver
                = JxtaReplicationReceiver.createInstance();
            receiver.doPipeInitialization();
        }        
                
    }    
    
}
