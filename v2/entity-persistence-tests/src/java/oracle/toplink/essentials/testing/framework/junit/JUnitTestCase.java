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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.framework.junit;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import junit.framework.*;
import javax.persistence.*;

import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.config.TopLinkProperties;

/**
 * This is the superclass for all TopLink JUnit tests
 * Provides convenience methods for transactional access as well as to access
 * login information and to create any sessions required for setup.
 *
 * Assumes the existence of a titl.properties file on the classpath that defines the
 * following properties:
 *
 * login.databaseplatform
 * login.username
 * login.password
 * login.databaseURL
 * login.driverClass
 */
public abstract class JUnitTestCase extends TestCase {
    public static final String TEST_PROPERTIES_FILE_KEY = "test.properties";
    public static final String TEST_PROPERTIES_FILE_DEFAULT = "test.properties";

    public static Map propertiesMap = null;
    public static Map persistencePropertiesTestMap = new HashMap();
    private static EntityManagerFactory emf = null;
    private static Map emfNamedPersistenceUnits = null;
    
    static {
        // These following properties used for property processing testing.
        // Some (or all) of them may override persistence properties.
        // Used by EntityManagerJUnitTestSuite.testPersistenceProperties()
        persistencePropertiesTestMap.put(TopLinkProperties.JDBC_READ_CONNECTIONS_SHARED, "false");
        persistencePropertiesTestMap.put(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MIN, "4");
        persistencePropertiesTestMap.put(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MAX, "9");
        persistencePropertiesTestMap.put(TopLinkProperties.JDBC_READ_CONNECTIONS_MIN, "4");
        persistencePropertiesTestMap.put(TopLinkProperties.JDBC_READ_CONNECTIONS_MAX, "4");
        emfNamedPersistenceUnits = new Hashtable();
    }
    
    public JUnitTestCase() {
        super();
    }

    public JUnitTestCase(String name) {
        super(name);
    }
    
    public static void clearCache() {
         try {
            getServerSession().getIdentityMapAccessor().initializeAllIdentityMaps();
         } catch (Exception ex) {
            throw new  RuntimeException("An exception occurred trying clear the cache.", ex);
        }   
    }
    
    public static void clearCache(String persistenceUnitName) {
         try {
            getServerSession(persistenceUnitName).getIdentityMapAccessor().initializeAllIdentityMaps();
         } catch (Exception ex) {
            throw new  RuntimeException("An exception occurred trying clear the cache.", ex);
        }   
    }
    
    /**
     * Create an entity manager.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();       
    }
    
    public static EntityManager createEntityManager(String persistenceUnitName) {
        return getEntityManagerFactory(persistenceUnitName).createEntityManager();       
    }
    
    public static EntityManager createEntityManager(String persistenceUnitName, Map properties) {
        return getEntityManagerFactory(persistenceUnitName, properties).createEntityManager();       
    }

    /**
     * Read common properties (including database properties) from test.properties file.
     * The location of properties file can be given by system property <tt>test.properties</tt>.
     * The default location is "test.properties" file in current directory. 
     */
    public static Map getDatabaseProperties(){
        if (propertiesMap == null){
            Properties properties = new Properties();
            File testPropertiesFile 
                = new File(System.getProperty(TEST_PROPERTIES_FILE_KEY, TEST_PROPERTIES_FILE_DEFAULT));
            
            URL url = null;
            if (testPropertiesFile.exists()) {
                try {
                    url = testPropertiesFile.toURL();
                } catch (MalformedURLException exception) {
                    throw new RuntimeException("Error loading " + testPropertiesFile.getName() + ".", exception);
                }
            }
            propertiesMap = new HashMap();
            if (url != null){
                try{
                    properties.load(url.openStream());
                } catch (java.io.IOException exception){
                   throw new  RuntimeException("Error loading " + testPropertiesFile.getName() + ".", exception);
                }
                
                String dbDriver = (String) properties.get("db.driver");
                String dbUrl = (String) properties.get("db.url");
                String dbUser = (String) properties.get("db.user");
                String dbPwd = (String) properties.get("db.pwd");
                String logLevel = (String) properties.get("toplink.logging.level");
                
                if (dbDriver != null) {
                    propertiesMap.put("toplink.jdbc.driver", dbDriver);
                }
                if (dbUrl != null) {
                    propertiesMap.put("toplink.jdbc.url", dbUrl);
                }
                if (dbUser != null) {
                    propertiesMap.put("toplink.jdbc.user", dbUser);
                }
                if (dbPwd != null) {
                    propertiesMap.put("toplink.jdbc.password", dbPwd);
                }
                if (logLevel != null) {
                    propertiesMap.put("toplink.logging.level", logLevel);
                }
            }
            propertiesMap.putAll(persistencePropertiesTestMap);
        }
        return propertiesMap;
    }
    
    public static ServerSession getServerSession(){
        return ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)createEntityManager()).getServerSession();               
    }
    
    public static ServerSession getServerSession(String persistenceUnitName){
        return ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)createEntityManager(persistenceUnitName)).getServerSession();               
    }
    
    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName){
        return getEntityManagerFactory(persistenceUnitName,  getDatabaseProperties());
    }
    
    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName, Map properties){
        EntityManagerFactory emfNamedPersistenceUnit = (EntityManagerFactory)emfNamedPersistenceUnits.get(persistenceUnitName);
        if (emfNamedPersistenceUnit == null){
            emfNamedPersistenceUnit = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
            emfNamedPersistenceUnits.put(persistenceUnitName, emfNamedPersistenceUnit);
        }
        return emfNamedPersistenceUnit;
    }
    
    public static EntityManagerFactory getEntityManagerFactory(){
        if (emf == null){
            emf = Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        }
        return emf;
    }
    
    public static boolean doesEntityManagerFactoryExist() {
        return emf != null && emf.isOpen();
    }

    public static void closeEntityManagerFactory() {
        if(emf != null) {
            if(emf.isOpen()) {
                emf.close();
            }
            emf = null;
        }
    }

    public static void closeEntityManagerFactoryNamedPersistenceUnit(String persistenceUnitName) {
        EntityManagerFactory emfNamedPersistenceUnit = (EntityManagerFactory)emfNamedPersistenceUnits.get(persistenceUnitName);
        if(emfNamedPersistenceUnit != null) {
            if(emfNamedPersistenceUnit.isOpen()) {
                emfNamedPersistenceUnit.close();
            }
            emfNamedPersistenceUnits.remove(persistenceUnitName);
        }
    }

    public static Platform getDbPlatform() {
        return getServerSession().getDatasourcePlatform();
    }
   
    public void setUp() {
    }
    
    public void tearDown() {
    }
    
    /**
     * Launch the JUnit TestRunner UI.
     */
    public static void main(String[] args) {
        // Run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
