/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.ejb.cmp3;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import javax.persistence.*;
import javax.persistence.spi.*;

import oracle.toplink.essentials.config.TargetDatabase;
import oracle.toplink.essentials.config.TopLinkProperties;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerFactoryImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.JavaSECMPInitializer;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerSetupImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.PersistenceInitializationActivator;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.threetier.ServerSession;

import oracle.toplink.essentials.tools.schemaframework.SchemaManager;
import oracle.toplink.essentials.ejb.cmp3.persistence.SEPersistenceUnitInfo;
import oracle.toplink.essentials.exceptions.PersistenceUnitLoadingException;
import oracle.toplink.essentials.ejb.cmp3.persistence.PersistenceUnitProcessor;

/**
 * This is the TopLink EJB 3.0 provider
 * The default constructor can be used to build the provider by reflection, after which it can
 * be used to create EntityManagerFactories
 */

public class EntityManagerFactoryProvider implements javax.persistence.spi.PersistenceProvider, PersistenceInitializationActivator {

    // The following constants are used in persistence xml or in createSessionManagerFactory methods to specify properties.
    // Many property declarations were moved to oracle.toplink.essentials.config.TopLinkProperties class.

    public static final String TOPLINK_ORM_THROW_EXCEPTIONS = "toplink.orm.throw.exceptions";
    public static final String TOPLINK_VALIDATION_ONLY_PROPERTY = "toplink.validation-only";

    public static final String DDL_GENERATION   = "toplink.ddl-generation";
    
    public static final String CREATE_ONLY      = "create-tables";
    public static final String DROP_AND_CREATE  = "drop-and-create-tables";
    public static final String NONE             = "none";
    
    public static final String APP_LOCATION     = "toplink.application-location";
    
    public static final String CREATE_JDBC_DDL_FILE = "toplink.create-ddl-jdbc-file-name";
    public static final String DROP_JDBC_DDL_FILE   = "toplink.drop-ddl-jdbc-file-name";
    
    public static final String DEFAULT_APP_LOCATION = "." + File.separator;
    public static final String DEFAULT_CREATE_JDBC_FILE_NAME = "createDDL.jdbc";
    public static final String DEFAULT_DROP_JDBC_FILE_NAME = "dropDDL.jdbc";
    public static final String JAVASE_DB_INTERACTION = "INTERACT_WITH_DB";    
    
    public static final String DDL_GENERATION_MODE = "toplink.ddl-generation.output-mode";
    public static final String DDL_SQL_SCRIPT_GENERATION = "sql-script";
    public static final String DDL_DATABASE_GENERATION = "database";
    public static final String DDL_BOTH_GENERATION = "both";
    // This is the default for now to ensure we still play nicely with Glassfish.
    public static final String DEFAULT_DDL_GENERATION_MODE = DDL_SQL_SCRIPT_GENERATION;
    
    protected static final HashMap<String, EntityManagerSetupImpl> emSetupImpls = new HashMap<String, EntityManagerSetupImpl>();
    // TEMPORARY - WILL BE REMOVED.
    // Used to warn users about deprecated property name and suggest the valid name.
    // TEMPORARY the old property names will be translated to the new ones and processed.
    protected static final String oldPropertyNames[][] = {
        {TopLinkProperties.JDBC_WRITE_CONNECTIONS_MAX, "toplink.max-write-connections"},
        {TopLinkProperties.JDBC_WRITE_CONNECTIONS_MIN, "toplink.min-write-connections"},
        {TopLinkProperties.JDBC_READ_CONNECTIONS_MAX, "toplink.max-read-connections"},
        {TopLinkProperties.JDBC_READ_CONNECTIONS_MIN, "toplink.min-read-connections"},
        {TopLinkProperties.JDBC_BIND_PARAMETERS, "toplink.bind-all-parameters"},
        {TopLinkProperties.TARGET_DATABASE, "toplink.platform.class.name"},
        {TopLinkProperties.TARGET_SERVER, "toplink.server.platform.class.name"},
        {TopLinkProperties.CACHE_SIZE_DEFAULT, "toplink.cache.default-size"}
    };

    /**
     * A default constructor is required by all Providers accoring the the EJB 3.0 specification
     */
    public EntityManagerFactoryProvider() {      
    }

    /**
    * Called by Persistence class when an EntityManagerFactory
    * is to be created.
    *
    * @param emName The name of the persistence unit
    * @param map A Map of properties for use by the
    * persistence provider. These properties may be used to
    * override the values of the corresponding elements in
    * the persistence.xml file or specify values for
    * properties not specified in the persistence.xml.
    * @return EntityManagerFactory for the persistence unit,
    * or null if the provider is not the right provider
    */
	public EntityManagerFactory createEntityManagerFactory(String emName, Map properties){
        Map nonNullProperties = (properties == null) ? new HashMap() : properties;
        String name = emName;
        if (name == null){
            name = "";
        }

        JavaSECMPInitializer initializer = JavaSECMPInitializer.getJavaSECMPInitializer();
        EntityManagerSetupImpl emSetupImpl = null;
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = currentLoader.getResources("META-INF/persistence.xml");
            boolean initialized = false;
            while (resources.hasMoreElements()) {
                URL url = PersistenceUnitProcessor.computePURootURL(resources.nextElement());
                String urlAndName = url + name;
            
                synchronized (EntityManagerFactoryProvider.emSetupImpls){
                    emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(urlAndName);
                    if (emSetupImpl == null || emSetupImpl.isUndeployed()){
                        if (!initialized) {
                            initializer.initialize(nonNullProperties, this);
                            initialized = true;
                        }
                        
                        emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(urlAndName);
                    }
                }

                // We found a match, stop looking.
                if (emSetupImpl != null) {
                    break;
                }
            }
        } catch (Exception e){
            throw PersistenceUnitLoadingException.exceptionSearchingForPersistenceResources(currentLoader, e);
        }

        //gf bug 854  Returns null if EntityManagerSetupImpl for the name doesn't exist (e.g. a non-existant PU)
        if (emSetupImpl == null) {
            return null;
        }
        
        if (!isPersistenceProviderSupported(emSetupImpl.getPersistenceUnitInfo().getPersistenceProviderClassName())){
            return null;
        }

        // synchronized to prevent overriding of the class loader
        // and also calls to predeploy and undeploy by other threads -
        // the latter may alter result of shouldRedeploy method.
        synchronized(emSetupImpl) {
            if(emSetupImpl.shouldRedeploy()) {
                SEPersistenceUnitInfo persistenceInfo = (SEPersistenceUnitInfo)emSetupImpl.getPersistenceUnitInfo();
                persistenceInfo.setClassLoader(JavaSECMPInitializer.getMainLoader());
                // if we are in undeployed state, this is a redeploy of an initial deployment that worked
                // so if weaving were going to occur, it already occured.  Therefore we can use the main classloader
                if (emSetupImpl.isUndeployed()){
                    persistenceInfo.setNewTempClassLoader(JavaSECMPInitializer.getMainLoader());
                }
            }
            // call predeploy
            // this will just increment the factory count since we should already be deployed
            emSetupImpl.predeploy(emSetupImpl.getPersistenceUnitInfo(), nonNullProperties);
        }
        
        EntityManagerFactoryImpl factory = null;
        try {
            factory = new EntityManagerFactoryImpl(emSetupImpl, nonNullProperties);
    
            // This code has been added to allow validation to occur without actually calling createEntityManager
            if (emSetupImpl.shouldGetSessionOnCreateFactory(nonNullProperties)) {
                factory.getServerSession();
            }
            return factory;
        } catch (RuntimeException ex) {
            if(factory != null) {
                factory.close();
            } else {
                emSetupImpl.undeploy();
            }
            throw ex;
        }
    }
    
    /**
    * Called by the container when an EntityManagerFactory
    * is to be created.
    *
    * @param info Metadata for use by the persistence provider
    * @return EntityManagerFactory for the persistence unit
    * specified by the metadata
    * @param map A Map of integration-level properties for use
    * by the persistence provider.
    */
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties){
        Map nonNullProperties = (properties == null) ? new HashMap() : properties;
        
        EntityManagerSetupImpl emSetupImpl = null;
        synchronized (EntityManagerFactoryProvider.emSetupImpls) {
            String urlAndName = info.getPersistenceUnitRootUrl() + info.getPersistenceUnitName();
            emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(urlAndName);
            if (emSetupImpl == null){
                emSetupImpl = new EntityManagerSetupImpl();
                emSetupImpl.setIsInContainerMode(true);        
                EntityManagerFactoryProvider.addEntityManagerSetupImpl(urlAndName, emSetupImpl);
            }
        }
        
        ClassTransformer transformer = null;
        if(!emSetupImpl.isDeployed()) {
            transformer = emSetupImpl.predeploy(info, nonNullProperties);
        }
        if (transformer != null){
            info.addTransformer(transformer);
        }
        // When EntityManagerFactory is created, the session is only partially created
        // When the factory is actually accessed, the emSetupImpl will be used to complete the session construction
        EntityManagerFactoryImpl factory = new EntityManagerFactoryImpl(emSetupImpl, nonNullProperties);

        // This code has been added to allow validation to occur without actually calling createEntityManager
        if (emSetupImpl.shouldGetSessionOnCreateFactory(nonNullProperties)) {
        	factory.getServerSession();
        }
        return factory;
    }

    /**
     * Returns whether the given persistence provider class is supported by this implementation
     * @param providerClassName
     * @return
     */
    public boolean isPersistenceProviderSupported(String providerClassName){
        return (providerClassName == null) || providerClassName.equals("") || providerClassName.equals(EntityManagerFactoryProvider.class.getName());
    }
    
    /**
     * Logs in to given session. If user has not specified  <codeTARGET_DATABASE</code>
     * the plaform would be auto detected
     * @param session The session to login to.
     * @param properties User specified properties for the persistence unit
     */
    public static void login(ServerSession session, Map properties) {
        String toplinkPlatform = (String)properties.get(TopLinkProperties.TARGET_DATABASE);
        if (!session.isConnected()) {
            if (toplinkPlatform == null || toplinkPlatform.equals(TargetDatabase.Auto)) {
                // if user has not specified a database platform, try to detect
                session.loginAndDetectDatasource();
            } else {
                session.login();
            }
        }
    }

    public static void generateDDLFiles(ServerSession session, Map props,
            boolean inSEmode) {
        boolean createTables = false, shouldDropFirst = false;
        String appLocation; 
        String createDDLJdbc;
        String dropDDLJdbc;
        String ddlGeneration = NONE;
        
        if(null == props){
            return;
        }

        ddlGeneration = (String)getConfigPropertyAsString(DDL_GENERATION, props, NONE);
        ddlGeneration = ddlGeneration.toLowerCase();
        if(ddlGeneration.equals(NONE)) {
            return;
        }

        if(ddlGeneration.equals(CREATE_ONLY) || 
            ddlGeneration.equals(DROP_AND_CREATE)) {
            createTables = true;
            if(ddlGeneration.equals(DROP_AND_CREATE)) {
                shouldDropFirst = true;
            }
        } 
        
        if (createTables) {
            String ddlGenerationMode = (String) getConfigPropertyAsString(DDL_GENERATION_MODE, props, DEFAULT_DDL_GENERATION_MODE);
            // Optimize for cases where the value is explicitly set to NONE 
            if (ddlGenerationMode.equals(NONE)) {                
                return;
            }
            
            appLocation = (String)getConfigPropertyAsString( APP_LOCATION, props, DEFAULT_APP_LOCATION);
            createDDLJdbc = (String)getConfigPropertyAsString( CREATE_JDBC_DDL_FILE, props, DEFAULT_CREATE_JDBC_FILE_NAME);
            dropDDLJdbc = (String)getConfigPropertyAsString( DROP_JDBC_DDL_FILE, props,  DEFAULT_DROP_JDBC_FILE_NAME);
            
            SchemaManager mgr = new SchemaManager(session);
            
            // The inSEmode checks here are only temporary to ensure we still 
            // play nicely with Glassfish.
            if (ddlGenerationMode.equals(DDL_DATABASE_GENERATION) || inSEmode) {
                runInSEMode(mgr, shouldDropFirst);                
                
                if (inSEmode) {
                    writeDDLsToFiles(mgr, appLocation,  createDDLJdbc,  dropDDLJdbc);      
                }
            } else if (ddlGenerationMode.equals(DDL_SQL_SCRIPT_GENERATION)) {
                writeDDLsToFiles(mgr, appLocation,  createDDLJdbc,  dropDDLJdbc);                
            } else if (ddlGenerationMode.equals(DDL_BOTH_GENERATION)) {
                runInSEMode(mgr, shouldDropFirst);
                writeDDLsToFiles(mgr, appLocation,  createDDLJdbc,  dropDDLJdbc);
            }
        }
    }
   
    public static void runInSEMode(SchemaManager mgr, boolean shouldDropFirst) {
        String str = getConfigPropertyAsString(JAVASE_DB_INTERACTION, null ,"true");
        boolean interactWithDB = Boolean.valueOf(str.toLowerCase()).booleanValue();
        if (!interactWithDB){
            return;
        }
        createOrReplaceDefaultTables(mgr, shouldDropFirst);
    }
    
  /**
   * Check the provided map for an object with the given key.  If that object is not available, check the
   * System properties.  If it is not available from either location, return the default value.
   * @param propertyKey 
   * @param map 
   * @param defaultValue 
   * @return 
   */
    public static String getConfigPropertyAsString(String propertyKey, Map overrides, String defaultValue){
        String value = getConfigPropertyAsString(propertyKey, overrides);
        if (value == null){
            value = defaultValue;
        }
        return value;
    }
    
    public static String getConfigPropertyAsString(String propertyKey, Map overrides){
        String value = null;
        if (overrides != null){
            value = (String)overrides.get(propertyKey);
        }
        if (value == null){
            value = System.getProperty(propertyKey);
        }
        
        return value;
    }

    public static String getConfigPropertyAsStringLogDebug(String propertyKey, Map overrides, String defaultValue, AbstractSession session){
        String value = getConfigPropertyAsStringLogDebug(propertyKey, overrides, session);
        if (value == null){
            value = defaultValue;
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_default", new Object[]{propertyKey, value});
        }
        return value;
    }
    
    public static String getConfigPropertyAsStringLogDebug(String propertyKey, Map overrides, AbstractSession session){
        String value = null;
        if (overrides != null){
            value = (String)overrides.get(propertyKey);
        }
        if (value == null){
            value = System.getProperty(propertyKey);
        }
        if(value != null && session !=  null) {
            String overrideValue = TopLinkProperties.getOverriddenLogStringForProperty(propertyKey);;           
            String logValue = (overrideValue == null) ? value : overrideValue;
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_specified", new Object[]{propertyKey, logValue});
        }
        
        return value;
    }
    
    public static Object getConfigPropertyLogDebug(String propertyKey, Map overrides, AbstractSession session){
        Object value = null;
        if (overrides != null){
            value = overrides.get(propertyKey);
        }
        if (value == null){
            value = System.getProperty(propertyKey);
        }
        if(value != null && session !=  null) {
            String overrideValue = TopLinkProperties.getOverriddenLogStringForProperty(propertyKey);;           
            Object logValue = (overrideValue == null) ? value : overrideValue;
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_specified", new Object[]{propertyKey, logValue});
        }
        
        return value;
    }

    public static void createOrReplaceDefaultTables(
        SchemaManager mgr, boolean shouldDropFirst) {          
        if (shouldDropFirst){
            mgr.replaceDefaultTables(true); 
        } else { 
            mgr.createDefaultTables(); 
        }
    }

    public static void writeDDLsToFiles(SchemaManager mgr,  String appLocation,
        String createDDLJdbc, String dropDDLJdbc) {
        // Ensure that the appLocation string ends with  File.seperator 
        appLocation = addFileSeperator(appLocation);
        if (null != createDDLJdbc) {
            String createJdbcFileName = appLocation + createDDLJdbc;
            mgr.outputCreateDDLToFile(createJdbcFileName);
        }

        if (null != dropDDLJdbc) {
            String dropJdbcFileName = appLocation + dropDDLJdbc;              
            mgr.outputDropDDLToFile(dropJdbcFileName);
        }

        mgr.setCreateSQLFiles(false);
        // When running in the application server environment always ensure that
        // we write out both the drop and create table files.
        createOrReplaceDefaultTables(mgr, true);
        mgr.closeDDLWriter();
    }
    
    public static String addFileSeperator(String appLocation) {
        int strLength = appLocation.length();
        if (appLocation.substring(strLength -1, strLength).equals(File.separator)) {
            return appLocation;
        } else {
            return appLocation + File.separator;
        }
    }

  /**
   * Merge the properties from the source object into the target object.  If the property
   * exists in both objects, use the one from the target
   * @param target 
   * @param source 
   * @return the target object
   */
    public static Map mergeMaps(Map target, Map source){
        Map map = new HashMap();
        if (source != null){
            map.putAll(source);
        }

        if (target != null){
            map.putAll(target);
        }
        return map;
    }

  /**
   * This is a TEMPORARY method that will be removed.
   * DON'T USE THIS METHOD - for internal use only.
   * @param Map m
   * @param AbstractSession session
   */
    public static void translateOldProperties(Map m, AbstractSession session) {
        for(int i=0; i < oldPropertyNames.length; i++) {
            Object value = getConfigPropertyAsString(oldPropertyNames[i][1], m);
            if(value != null) {
                if(session != null){
                    session.log(SessionLog.INFO, SessionLog.TRANSACTION, "deprecated_property", oldPropertyNames[i]);
                }
                m.put(oldPropertyNames[i][0], value);
            }
        }
    }

    public static void warnOldProperties(Map m, AbstractSession session) {
        for(int i=0; i < oldPropertyNames.length; i++) {
            Object value = m.get(oldPropertyNames[i][1]);
            if(value != null) {
                session.log(SessionLog.INFO, SessionLog.TRANSACTION, "deprecated_property", oldPropertyNames[i]);
            }
        }
    }
    
    /**
     * Return the setup class for a given entity manager name 
     * @param emName 
     */
      public static EntityManagerSetupImpl getEntityManagerSetupImpl(String emName){
          if (emName == null){
              return (EntityManagerSetupImpl)emSetupImpls.get("");
          }
          return (EntityManagerSetupImpl)emSetupImpls.get(emName);
      }
      
      /**
       * Add an EntityManagerSetupImpl to the cached list
       * These are used to ensure all persistence units that are the same get the same underlying session
       * @param name
       * @param setup
       */
      public static void addEntityManagerSetupImpl(String name, EntityManagerSetupImpl setup){
          if (name == null){
              emSetupImpls.put("", setup);
          }
          emSetupImpls.put(name, setup);
      }
}
