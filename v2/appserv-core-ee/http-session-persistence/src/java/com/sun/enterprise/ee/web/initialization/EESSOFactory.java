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
 * EESSOFactory.java
 *
 * Created on August 24, 2004, 5:27 PM
 */

package com.sun.enterprise.ee.web.initialization;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.enterprise.ee.web.authenticator.*;
import com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationMessageRouter;
import com.sun.enterprise.ee.web.sessmgmt.StoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.StorePool;

import com.sun.appserv.ha.util.*;


/**
 *
 * @author lwhite
 */
public class EESSOFactory implements SSOFactory {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);     
    
    /** Creates a new instance of EESSOFactory */
    public EESSOFactory() {         
    }
    
    /**
     * Create a SingleSignOn valve
     * HASingleSignOnValve is created is global availability-enabled
     * is true and sso-failover-enabled
     */
    public SingleSignOn createSingleSignOnValve(String virtualServerName) {
        if(getAvailabilityEnabled() && getSsoFailoverEnabled()){
            return this.createHASingleSignOnValve(virtualServerName);
        } else {
            return new SingleSignOn();
        }
    }   
    
   /**
     * check global availability-enabled
     * if availability-enabled set global ha defaults
     * @return return true only if the value of availability-enabled is "true"
     * for this virtaul server. otherwise, return false.
     */
    private static boolean getAvailabilityEnabled() {
        // check global availability-enabled
        //if availability-enabled set global ha defaults
        ServerConfigLookup serverConfigLookup = new ServerConfigLookup();
        boolean isAvailabilityEnabled =
            serverConfigLookup.getAvailabilityEnabledFromConfig();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("EESSOFactory : AvailabilityGloballyEnabled = " + isAvailabilityEnabled);
        }
        return isAvailabilityEnabled; 
    }
    
   /**
     * check sso-failover-enabled in web-container-availability
     * @return return true only if the value of sso-failover-enabled is "true"
     * and availability-enabled in web-container-availability is "true"
     * and persistence-type is "HA" or "Replicated"
     * otherwise, return false.
     */
    private static boolean getSsoFailoverEnabled() {
        ServerConfigLookup serverConfigLookup = new ServerConfigLookup();
        boolean webContainerAvailabilityEnabled =
            serverConfigLookup.getWebContainerAvailabilityEnabledFromConfig();        
        boolean isSsoFailoverEnabled =
            serverConfigLookup.getSsoFailoverEnabledFromConfig();
        boolean isPersistenceTypeOkForFailover =
            isPersistenceTypeHAOrReplicated();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("EESSOFactory : WebContainerAvailabilityEnabled = " + webContainerAvailabilityEnabled);
            _logger.finest("EESSOFactory : SSOFailoverEnabled = " + isSsoFailoverEnabled);
            _logger.finest("EESSOFactory : PersistenceTypeOkForFailover = " + isPersistenceTypeOkForFailover);
        }      
        return isSsoFailoverEnabled 
                && webContainerAvailabilityEnabled
                && isPersistenceTypeOkForFailover; 
    }    
    
    /**
     * Create a SingleSignOn using the server.xml sso-failover-enabled property.
     */
    public SingleSignOn createHASingleSignOnValve(String virtualServerName) {
        //HASingleSignOn haSSO = new HASingleSignOn();
        //HASSO haSSO = new HASingleSignOn();
        HASSO haSSO = null;
        if(isPersistenceTypeHA()) {
            //haSSO = new HASingleSignOn();
            haSSO = getHASingleSignOnImpl();
        } else {
            haSSO = new ReplicationSingleSignOn(virtualServerName);
        }        
        initializeSSOvalve(haSSO);
        return (SingleSignOn)haSSO;
    }
    
    private final String EE_SSO_IMPL
        = "com.sun.enterprise.ee.web.authenticator.HASingleSignOn";

    public HASSO getHASingleSignOnImpl() {
        HASSO haSSO = null;
        try {
            haSSO =
                (HASSO) (Class.forName(EE_SSO_IMPL)).newInstance();
        } catch (Exception ex) {
        }
        return haSSO;
    }    

    //public void initializeSSOvalve(HASingleSignOn haSSO){
    public void initializeSSOvalve(HASSO haSSO){
        StoreFactory ssoStoreFactory = createSSOStoreFactoryImpl();
        //TODO: get these configuration from server.xml
        StorePool storePool = new StorePool(StorePool.DEFAULT_INITIAL_SIZE, StorePool.DEFAULT_UPPER_SIZE, StorePool.DEFAULT_POLL_TIME, ssoStoreFactory);
        haSSO.setSSOStorePool(storePool);
        haSSO.setPassedInPersistenceType(this.getPassedInPersistenceType());
    }
    
    //determine which kind of store factory to use
    //we have already checked and persistence-type = "HA" or "Replicated"
    public StoreFactory createSSOStoreFactoryImpl(){
        if(isPersistenceTypeHA()) {
            //return new SSOStoreFactoryImpl();
            return getHASSOStoreFactoryImpl();
        } else {
            return new ReplicationSSOStoreFactoryImpl();
        }
    }
    
    private final String EE_SSO_STORE_FACTORY_IMPL
        = "com.sun.enterprise.ee.web.authenticator.SSOStoreFactoryImpl";

    public StoreFactory getHASSOStoreFactoryImpl() {
        StoreFactory ssoStoreFactory = null;
        try {
            ssoStoreFactory =
                (StoreFactory) (Class.forName(EE_SSO_STORE_FACTORY_IMPL)).newInstance();
        } catch (Exception ex) {
        }
        return ssoStoreFactory;
    }    
    
    //this could be ha, replicated 
    //or a registered replication implementation
    private static boolean isPersistenceTypeHAOrReplicated() {    
        ServerConfigLookup lookup = new ServerConfigLookup();
        PersistenceType persistenceType = 
                lookup.getPersistenceTypeFromConfig();
        String persistenceTypeString = persistenceType.getType();
        return persistenceTypeString.equalsIgnoreCase("HA")
            || persistenceTypeString.equalsIgnoreCase("Replicated")
            || isPersistenceTypeRegistered();
    }
    
    private static boolean isPersistenceTypeRegistered() {          
        ServerConfigLookup lookup = new ServerConfigLookup();
        PersistenceType persistenceType = 
                lookup.getPersistenceTypeFromConfig();
        String persistenceTypeString = persistenceType.getType();
        EEPersistenceTypeResolver resolver = new EEPersistenceTypeResolver();
        return resolver.isRegisteredType(persistenceTypeString);
    }    
    
    private boolean isPersistenceTypeHA() {    
        ServerConfigLookup lookup = new ServerConfigLookup();
        PersistenceType persistenceType = 
                lookup.getPersistenceTypeFromConfig();
        String persistenceTypeString = persistenceType.getType();
        return persistenceTypeString.equalsIgnoreCase("HA");
    }
    
    private boolean isPersistenceTypeReplicated() {    
        ServerConfigLookup lookup = new ServerConfigLookup();
        PersistenceType persistenceType = 
                lookup.getPersistenceTypeFromConfig();
        String persistenceTypeString = persistenceType.getType();
        return persistenceTypeString.equalsIgnoreCase("REPLICATED");
    }
    
    private String getPassedInPersistenceType() {    
        ServerConfigLookup lookup = new ServerConfigLookup();
        PersistenceType persistenceType = 
                lookup.getPersistenceTypeFromConfig();
        //return persistenceType.getType();
        String passedInPersistenceTypeString = persistenceType.getType();
        /*
        PersistenceTypeResolver persistenceTypeResolver 
            = getPersistenceTypeResolver();
        String resolvedPersistenceType 
            = persistenceTypeResolver.resolvePersistenceType(passedInPersistenceTypeString);
        System.out.println("resolvedPersistenceType = " + resolvedPersistenceType);
         */

        String resolvedPersistenceType = passedInPersistenceTypeString; 
        return resolvedPersistenceType;
        
    }
    
    private PersistenceTypeResolver getPersistenceTypeResolver() {
        String resolverClassName 
            = "com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver";
    
        PersistenceTypeResolver persistenceTypeResolver = null;
        try {
            persistenceTypeResolver = 
                (PersistenceTypeResolver) (Class.forName(resolverClassName)).newInstance();
        } catch (Exception ex) {
            System.out.println("unable to create persistence type resolver");
        } 
        return persistenceTypeResolver;
    }    
    
}
