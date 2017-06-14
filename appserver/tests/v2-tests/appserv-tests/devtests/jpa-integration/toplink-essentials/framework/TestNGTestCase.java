/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2006, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.framework;

import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.threetier.ServerSession;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class holds creates an EntityManagerFactory and provides
 * convenience methods to access TopLink specific artifacts.  The
 * EntityManagerFactory is created by referencing the PersistenceUnit
 * "default", which is associated to the JavaDB bundled with the
 * application server.
 */
public abstract class TestNGTestCase {

    private Map propertiesMap = null;

    private EntityManagerFactory emf = null;

    public void clearCache() {
         try {
            getServerSession().getIdentityMapAccessor().initializeAllIdentityMaps();
         } catch (Exception ex) {
            throw new  RuntimeException("An exception occurred trying clear the cache.", ex);
        }   
    }
    
    /**
     * Create an entity manager.
     */
    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();       
    }
    
    public Map getDatabaseProperties(){
        if (propertiesMap == null){
            propertiesMap = new HashMap();
             propertiesMap.put("toplink.session.name", "default");
        }
        return propertiesMap;
    }
    
    public ServerSession getServerSession(){
        return ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)createEntityManager()).getServerSession();               
    }
    
    public EntityManagerFactory getEntityManagerFactory(){
        if (emf == null){
            emf = Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        }
        return emf;
    }
    
    public Platform getDbPlatform() {
        return getServerSession().getDatasourcePlatform();
    }

    @Configuration(beforeTestClass = true)
    public void setUp(){
        // Tables are created by Java2DB. Please see the option in persistence.xml!
    }

    @Configuration(afterTestClass = true)
    public void tearDown() {
        clearCache();
    }

}
