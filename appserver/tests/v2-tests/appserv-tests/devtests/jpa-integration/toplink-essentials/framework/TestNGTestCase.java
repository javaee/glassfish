/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
