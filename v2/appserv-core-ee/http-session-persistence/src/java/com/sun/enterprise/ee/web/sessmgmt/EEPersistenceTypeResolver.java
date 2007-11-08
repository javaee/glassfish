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
 * EEPersistenceTypeResolver.java
 *
 * Created on November 15, 2006, 1:04 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.appserv.ha.spi.*;
import com.sun.enterprise.web.ServerConfigLookup;

import com.sun.appserv.ha.util.PersistenceTypeResolver;

/**
 *
 * @author lwhite
 */
public class EEPersistenceTypeResolver implements PersistenceTypeResolver {

    public static final String FILE_TYPE = "file";
    public static final String MEMORY_TYPE = "memory";
    public static final String HA_TYPE = "ha";
    public static final String REPLICATED_TYPE = "replicated";
    
    private static final List PREDEFINED_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, MEMORY_TYPE, HA_TYPE, REPLICATED_TYPE);
    private static final List PREDEFINED_NO_HADB_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, MEMORY_TYPE, REPLICATED_TYPE);
    private static final List PREDEFINED_EJB_NO_HADB_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, REPLICATED_TYPE);    
    
    private static final List NON_REPLICATED_PREDEFINED_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, MEMORY_TYPE, HA_TYPE);
    private static final List NON_REPLICATED_NO_HADB_PREDEFINED_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, MEMORY_TYPE);    
    
    private static final List NON_REPLICATED_EJB_PREDEFINED_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE, HA_TYPE);
    private static final List NON_REPLICATED_NO_HADB_EJB_PREDEFINED_PERSISTENCE_TYPES
        = Arrays.asList(FILE_TYPE);
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    /** Creates a new instance of EEPersistenceTypeResolver */
    public EEPersistenceTypeResolver() {
    }
    
    private boolean isReplicatedType(String type) {
        return REPLICATED_TYPE.equals((String)type);
    }    
    
    private boolean isPredefinedType(String type) {
        return PREDEFINED_PERSISTENCE_TYPES.contains((String)type);
    }
    
    private boolean isNonReplicatedPredefinedType(String type) {
        return NON_REPLICATED_PREDEFINED_PERSISTENCE_TYPES.contains((String)type);
    } 
    
    private boolean isNonReplicatedEjbPredefinedType(String type) {
        return NON_REPLICATED_EJB_PREDEFINED_PERSISTENCE_TYPES.contains((String)type);
    }   
    
    private boolean checkGMS() {
        JxtaStarter jxtaStarter = JxtaStarter.createInstance();
        return jxtaStarter.checkGMS();
    }
    
    public boolean isRegisteredType(String type) {
        Collection<String> registeredTypes = this.getRegisteredTypes();
        /*
        System.out.println("ourType=" + type);
        System.out.println("registeredTypes size=" + registeredTypes.size());
        int i=0;
        Iterator it = registeredTypes.iterator();
        while(it.hasNext()) {
             System.out.println("registeredType[i]=" + it.next()); 
             i++;
        }
         */
        return registeredTypes.contains(type);
    }    
    
    private Collection<String> getRegisteredTypes() {
        BackingStoreRegistry backingStoreRegistry
            = BackingStoreRegistry.getInstance();
        Collection<String> registeredTypes 
            = backingStoreRegistry.getRegisteredTypes();
        //for now have to add REPLICATED_TYPE
        String[] regTypesArray = (String[]) registeredTypes.toArray(new String[0]);
        ArrayList regTypesList = new ArrayList();
        for(int i=0; i<regTypesArray.length; i++) {
            regTypesList.add(regTypesArray[i]);
        }
        regTypesList.add(REPLICATED_TYPE);
        /* for testing only
        for(int i=0; i<regTypesList.size(); i++) {
            System.out.println("regType[" + i + "]= " + regTypesList.get(i));
        }
         */
        return regTypesList;
    }    
    
    public String resolvePersistenceType(String persistenceType) {
        /*test begin
        List webTypes = getWebDefinedPersistenceTypes();
        for(int i=0; i<webTypes.size(); i++) {
            System.out.println("webType[" + i + "]= " + webTypes.get(i));
        }        
        List ejbTypes = this.getEjbDefinedPersistenceTypes();
        for(int i=0; i<ejbTypes.size(); i++) {
            System.out.println("ejbType[" + i + "]= " + ejbTypes.get(i));
        }
        //test end         
        */
        
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Resolving persistenceType:" + persistenceType);
        }
        //do not allow "ha" if HADB is not installed
        if(HA_TYPE.equalsIgnoreCase(persistenceType) 
            && !ServerConfigLookup.isHADBInstalled()) {
            return MEMORY_TYPE;
        }
        //no change for these types
        if(isNonReplicatedPredefinedType(persistenceType)) {
            return persistenceType;
        }
        //if GMS not enabled/running default to MEMORY_TYPE
        if(!this.isNativeReplicationEnabled()) {
            return MEMORY_TYPE;
        }
        if(this.isReplicatedType(persistenceType)) {
            return persistenceType;
        }
        /* leave this out for now - can add later
        if(!checkGMS()) {
            return MEMORY_TYPE;
        }
         */
        if(isRegisteredType(persistenceType)) {
            return REPLICATED_TYPE;
        } else {
            return MEMORY_TYPE;
        }
    }
    
    public String resolvePersistenceTypeForEjb(String persistenceType) {
        //do not allow "ha" if HADB is not installed
        if(HA_TYPE.equalsIgnoreCase(persistenceType) 
            && !ServerConfigLookup.isHADBInstalled()) {
            return FILE_TYPE;
        }        
        //no change for these types
        if(isNonReplicatedEjbPredefinedType(persistenceType)) {
            return persistenceType;
        }
        //if GMS not enabled/running default to FILE_TYPE
        if(!this.isNativeReplicationEnabled()) {
            return FILE_TYPE;
        }
        if(this.isReplicatedType(persistenceType)) {
            return persistenceType;
        }        
        /* leave this out for now - can add later
        if(!checkGMS()) {
            return FILE_TYPE;
        }
         */
        if(isRegisteredType(persistenceType)) {
            return REPLICATED_TYPE;
        } else {
            return FILE_TYPE;
        }
    }
    
     private boolean isNativeReplicationEnabled() {
         ServerConfigLookup lookup = new ServerConfigLookup();         
         if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("GMS ENABLED:" + lookup.isGMSEnabled());
            _logger.finest("NATIVE REPLICATION ENABLED:" + lookup.isNativeReplicationEnabledFromConfig());
         }
         return lookup.isGMSEnabled() && lookup.isNativeReplicationEnabledFromConfig();
     }
     
     public List getWebDefinedPersistenceTypes() {
         List result = new ArrayList();
         result.addAll(getNonReplicatedPredefinedPersistenceTypes());
         //result.addAll(NON_REPLICATED_PREDEFINED_PERSISTENCE_TYPES);
         result.addAll(this.getRegisteredTypes());
         return result;
     }
     
     private List getNonReplicatedPredefinedPersistenceTypes() {
         if(ServerConfigLookup.isHADBInstalled()) {
            return NON_REPLICATED_PREDEFINED_PERSISTENCE_TYPES;
         } else {
            return NON_REPLICATED_NO_HADB_PREDEFINED_PERSISTENCE_TYPES;
         }
     }
     
     public List getEjbDefinedPersistenceTypes() {
         List result = new ArrayList();
         result.addAll(getNonReplicatedEjbPredefinedPersistenceTypes());
         //result.addAll(NON_REPLICATED_EJB_PREDEFINED_PERSISTENCE_TYPES);
         result.addAll(this.getRegisteredTypes());
         return result;
     }
     
      private List getNonReplicatedEjbPredefinedPersistenceTypes() {
         if(ServerConfigLookup.isHADBInstalled()) {
            return NON_REPLICATED_EJB_PREDEFINED_PERSISTENCE_TYPES;
         } else {
            return NON_REPLICATED_NO_HADB_EJB_PREDEFINED_PERSISTENCE_TYPES;
         }
     }    
    
}
