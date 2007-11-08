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
package oracle.toplink.essentials.internal.ejb.cmp3;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.Constructor;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.PersistenceException;

import oracle.toplink.essentials.config.TopLinkProperties;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.essentials.internal.ejb.cmp3.base.PropertiesHandler;
import oracle.toplink.essentials.internal.weaving.TransformerFactory;
import oracle.toplink.essentials.jndi.JNDIConnector;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.sequencing.Sequence;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.threetier.ReadConnectionPool;
import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;
import oracle.toplink.essentials.tools.sessionmanagement.SessionManager;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.base.CMP3Policy;
import oracle.toplink.essentials.platform.server.CustomServerPlatform;
import oracle.toplink.essentials.platform.server.ServerPlatform;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.EJB30ConversionManager;
import javax.persistence.spi.PersistenceUnitTransactionType;
import oracle.toplink.essentials.ejb.cmp3.persistence.PersistenceUnitProcessor;

import oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base.DataSourceImpl;
import oracle.toplink.essentials.tools.sessionconfiguration.DescriptorCustomizer;
import oracle.toplink.essentials.tools.sessionconfiguration.SessionCustomizer;
import oracle.toplink.essentials.internal.security.SecurableObjectHolder;

import static oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider.*;

/**
 * INTERNAL:
 * A TopLink specific implementer of the EntityManagerInitializer interface.
 */
public class EntityManagerSetupImpl {
    /*
     * Design Pattern in use: Builder pattern
     * EntityManagerSetupImpl, MetadataProcessor and MetadataProject
     * play the role of director, builder and product respectively.
     * See processORMetadata which is the factory method.
     */

    protected MetadataProcessor processor = null;
    protected PersistenceUnitInfo persistenceUnitInfo = null;
    protected Map predeployProperties = null;
    // count a number of open factories that use this object.
    protected int factoryCount = 0;
    protected ServerSession session = null;
    // true if predeploy called by createContainerEntityManagerFactory; false - createEntityManagerFactory
    protected boolean isInContainerMode = false;
    // indicates whether weaving was used on the first run through predeploy (in STATE_INITIAL)
    protected boolean enableLazyForOneToOne = false;
    // indicates that classes have already been woven
    protected boolean isWeavingStatic = false;
    protected SecurableObjectHolder securableObjectHolder = new SecurableObjectHolder();

    // factoryCount==0; session==null
    public static final String STATE_INITIAL        = "Initial";
    
    // session != null
    public static final String STATE_PREDEPLOYED    = "Predeployed";
    
    // factoryCount>0; session != null; session stored in SessionManager
    public static final String STATE_DEPLOYED       = "Deployed";
   	
    // factoryCount==0; session==null
    public static final String STATE_PREDEPLOY_FAILED="PredeployFailed";
    
    // factoryCount>0; session != null
    public static final String STATE_DEPLOY_FAILED  = "DeployFailed";
    
    // factoryCount==0; session==null
    public static final String STATE_UNDEPLOYED     = "Undeployed";

    protected String state = STATE_INITIAL;

	/**
     *     Initial -----> PredeployFailed
     *           |         |
     *           V         V
     *       |-> Predeployed --> DeployFailed
     *       |   |         |        |
     *       |   V         V        V
     *       | Deployed -> Undeployed-->|
     *       |                          |
     *       |<-------------------------V
     */
    
    
    public static final String ERROR_LOADING_XML_FILE = "error_loading_xml_file";
	public static final String EXCEPTION_LOADING_ENTITY_CLASS = "exception_loading_entity_class";

    /**
     * This method can be used to ensure the session represented by emSetupImpl
     * is removed from the SessionManager.
     */
    protected void removeSessionFromGlobalSessionManager() {
        if (session != null){
            if(session.isConnected()) {
                session.logout();
            }
            SessionManager.getManager().getSessions().remove(session.getName());
        }
    }
    
    
    /**
     * Deploy a persistence session and return an EntityManagerFactory.
     * 
     * Deployment takes a session that was partially created in the predeploy call and makes it whole.
     * 
     * This means doing any configuration that requires the real class definitions for the entities.  In
     * the predeploy phase we were in a stage where we were not let allowed to load the real classes.
     * 
     * Deploy could be called several times - but only the first call does the actual deploying -
     * additional calls allow to update session properties (in case the session is not connected).
     * 
     * Note that there is no need to synchronize deploy method - it doesn't alter factoryCount
     * and while deploy is executed no other method can alter the current state
     * (predeploy call would just increment factoryCount; undeploy call would not drop factoryCount to 0).
     * However precautions should be taken to handle concurrent calls to deploy, because those may
     * alter the current state or connect the session.
     * 
     * @param realClassLoader The class loader that was used to load the entity classes. This loader
     *               will be maintained for the lifespan of the loaded classes.
     * @param additionalProperties added to predeployProperties for updateServerSession overriding existing properties.
     *              In JSE case it allows to alter properties in main (as opposed to preMain where preDeploy is called).
     * @return An EntityManagerFactory to be used by the Container to obtain EntityManagers
     */
    public ServerSession deploy(ClassLoader realClassLoader, Map additionalProperties) {       
        if(state != STATE_PREDEPLOYED && state != STATE_DEPLOYED) {
            throw new PersistenceException(EntityManagerSetupException.cannotDeployWithoutPredeploy(persistenceUnitInfo.getPersistenceUnitName(), state));
        }
        // state is PREDEPLOYED or DEPLOYED
        session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "deploy_begin", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
        try {                        
            Map deployProperties = mergeMaps(additionalProperties, predeployProperties);
            translateOldProperties(deployProperties, session);
            
            if(state == STATE_PREDEPLOYED) {
                synchronized(session) {
                    if(state == STATE_PREDEPLOYED) {
                        try {
                            // The project is initially created using class names rather than classes.  This call will make the conversion
                            session.getProject().convertClassNamesToClasses(realClassLoader);
                    
                            // listeners and queries require the real classes and are therefore built during deploy using the realClassLoader
                            processor.setClassLoader(realClassLoader);
                            processor.addEntityListeners();
                            processor.addNamedQueries();
                            // free the resouces that we don't need any more.
                            processor.cleanup();
                            processor = null;
                    
                            initServerSession(deployProperties);
                    
                            if (session.getIntegrityChecker().hasErrors()){
                                session.handleException(new IntegrityException(session.getIntegrityChecker()));
                            }
                    
                            session.getDatasourcePlatform().getConversionManager().setLoader(realClassLoader);
                            state = STATE_DEPLOYED;
                        } catch (RuntimeException ex) {
                            state = STATE_DEPLOY_FAILED;
                            // session not discarded here only because it will be used in undeploy method for debug logging.
                            throw new PersistenceException(EntityManagerSetupException.deployFailed(persistenceUnitInfo.getPersistenceUnitName(), ex));
                        }
                    }
                }
            }
            // state is DEPLOYED
            if(!session.isConnected()) {
                synchronized(session) {
                    if(!session.isConnected()) {
                        session.setProperties(deployProperties);
                        updateServerSession(deployProperties, realClassLoader);
                        if(isValidationOnly(deployProperties, false)) {
                            session.initializeDescriptors();
                        } else {
                            login(session, deployProperties);
                            generateDDLFiles(session, deployProperties, !isInContainerMode);
                        }
                    }
                }
            }
            return session;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new javax.persistence.PersistenceException(illegalArgumentException);
        } catch (oracle.toplink.essentials.exceptions.ValidationException exception) {
            throw new javax.persistence.PersistenceException(exception);
        } finally {
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "deploy_end", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
        }
    }


    /**
     *  INTERNAL:
     *  Adds descriptors plus sequencing info found on the project to the session.
     */
    protected void addProjectToSession(ServerSession session, Project project) {
        DatasourcePlatform sessionPlatform = (DatasourcePlatform)session.getDatasourceLogin().getDatasourcePlatform();
        DatasourcePlatform projectPlatform = (DatasourcePlatform)project.getDatasourceLogin().getDatasourcePlatform();
        if (!sessionPlatform.hasDefaultSequence() && projectPlatform.hasDefaultSequence()) {
            sessionPlatform.setDefaultSequence(projectPlatform.getDefaultSequence());
        }
        if ((sessionPlatform.getSequences() == null) || sessionPlatform.getSequences().isEmpty()) {
            if ((projectPlatform.getSequences() != null) && !projectPlatform.getSequences().isEmpty()) {
                sessionPlatform.setSequences(projectPlatform.getSequences());
            }
        } else {
            if ((projectPlatform.getSequences() != null) && !projectPlatform.getSequences().isEmpty()) {
                Iterator itProjectSequences = projectPlatform.getSequences().values().iterator();
                while (itProjectSequences.hasNext()) {
                    Sequence sequence = (Sequence)itProjectSequences.next();
                    if (!sessionPlatform.getSequences().containsKey(sequence.getName())) {
                        sessionPlatform.addSequence(sequence);
                    }
                }
            }
        }
        session.addDescriptors(project);
    }
    
    /**
     * INTERNAL:
     * Put the given session into the session manager so it can be looked up later
     */
    protected void addSessionToGlobalSessionManager() {
        AbstractSession oldSession = (AbstractSession)SessionManager.getManager().getSessions().get(session.getName());
        if(oldSession != null) {
            throw new PersistenceException(EntityManagerSetupException.attemptedRedeployWithoutClose(session.getName()));
        }
        SessionManager.getManager().addSession(session);
    }

    /**
     *  INTERNAL:
     *  Assign a CMP3Policy to each descriptor
     */
    protected void assignCMP3Policy() {
        // all descriptors assigned CMP3Policy
        Project project = session.getProject();
        for (Iterator iterator = project.getDescriptors().values().iterator(); iterator.hasNext();){
            //bug:4406101  changed class cast to base class, which is used in projects generated from 904 xml
            ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
            
            if(descriptor.getCMPPolicy() == null) {
                descriptor.setCMPPolicy(new CMP3Policy());
            }
        }
    }

    /**
     * INTERNAL:
     * Updates the TopLink ServerPlatform class for use with this platform.
     * @returns true if the ServerPlatform has changed.
     */  
    protected boolean updateServerPlatform(Map m, ClassLoader loader) {
        String serverPlatformClassName = PropertiesHandler.getPropertyValueLogDebug(TopLinkProperties.TARGET_SERVER, m, session);
        if(serverPlatformClassName == null) {
            // property is not specified - nothing to do.
            return false;
        }

        // originalServerPlatform is always non-null - Session's constructor sets serverPlatform to NoServerPlatform
        ServerPlatform originalServerPlatform = session.getServerPlatform();
        String originalServerPlatformClassName = originalServerPlatform.getClass().getName();
        if(originalServerPlatformClassName.equals(serverPlatformClassName)) {
            // nothing to do - use the same value as before
            return false;
        }

        // the new serverPlatform
        ServerPlatform serverPlatform = null;
        // New platform - create the new instance and set it.
        Class cls = findClassForProperty(serverPlatformClassName, TopLinkProperties.TARGET_SERVER, loader);
        try {
            Constructor constructor = cls.getConstructor(new Class[]{oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl.class});
            serverPlatform = (ServerPlatform)constructor.newInstance(new Object[]{session});
        } catch (Exception ex) {
            if(ExternalTransactionController.class.isAssignableFrom(cls)) {
                // the new serverPlatform is CustomServerPlatform, cls is its ExternalTransactionController class
                if(originalServerPlatform.getClass().equals(CustomServerPlatform.class)) {
                    // both originalServerPlatform and the new serverPlatform are Custom,
                    // just set externalTransactionController class (if necessary) into
                    // originalServerPlatform
                    CustomServerPlatform originalCustomServerPlatform = (CustomServerPlatform)originalServerPlatform;
                    if(cls.equals(originalCustomServerPlatform.getExternalTransactionControllerClass())) {
                        // externalTransactionController classes are the same - nothing to do
                    } else {
                        originalCustomServerPlatform.setExternalTransactionControllerClass(cls);
                    }
                } else {
                    // originalServerPlatform is not custom - need a new one.
                    CustomServerPlatform customServerPlatform = new CustomServerPlatform(session);
                    customServerPlatform.setExternalTransactionControllerClass(cls);
                    serverPlatform = customServerPlatform;
                }
             } else {
                 throw EntityManagerSetupException.failedToInstantiateServerPlatform(serverPlatformClassName, TopLinkProperties.TARGET_SERVER, ex);
             }
         }
 
        if (serverPlatform != null){
            session.setServerPlatform(serverPlatform);
            return true;
        }    
        return false;
    }

    /**
     * INTERNAL:
     * Update loggers and settings for the singleton logger and the session logger. 
     * @param m the properties map
     * @param serverPlatformChanged the boolean that denotes a serverPlatform change in the session.
     * @param sessionNameChanged the boolean that denotes a sessionNameChanged change in the session.
     */
    protected void updateLoggers(Map m, boolean serverPlatformChanged, boolean sessionNameChanged, ClassLoader loader) {
        // Logger(SessionLog type) can be specified by the logger property or ServerPlatform.getServerLog().
        // The logger property has a higher priority to ServerPlatform.getServerLog().
        String loggerClassName = PropertiesHandler.getPropertyValueLogDebug(TopLinkProperties.LOGGING_LOGGER, m, session);

        // The sessionLog instance should be different from the singletonLog because they have 
        // different state.
        SessionLog singletonLog = null, sessionLog = null;
        if (loggerClassName != null) {
            SessionLog currentLog = session.getSessionLog();
            if (!currentLog.getClass().getName().equals(loggerClassName)) {
                // Logger class was specified and it's not what's already there.
                Class sessionLogClass = findClassForProperty(loggerClassName, TopLinkProperties.LOGGING_LOGGER, loader);
                try {
                    singletonLog = (SessionLog)sessionLogClass.newInstance();
                    sessionLog = (SessionLog)sessionLogClass.newInstance();
                } catch (Exception ex) {
                    throw EntityManagerSetupException.failedToInstantiateLogger(loggerClassName, TopLinkProperties.LOGGING_LOGGER, ex);
                }
            }
        } else if(serverPlatformChanged) {
            ServerPlatform serverPlatform = session.getServerPlatform();
            singletonLog = serverPlatform.getServerLog();
            sessionLog = serverPlatform.getServerLog();
        }
        
        // Don't change default loggers if the new loggers have not been created.
        if (singletonLog != null && sessionLog != null){
            AbstractSessionLog.setLog(singletonLog);
            session.setSessionLog(sessionLog);
        } else if (sessionNameChanged) {
            // In JavaLog this will result in logger name changes,
            // but won't affect DefaultSessionLog.
            // Note, that the session hasn't change, only its name.
            session.getSessionLog().setSession(session);
        }

        //Bug5389828.  Update the logging settings for the singleton logger.
        initOrUpdateLogging(m, AbstractSessionLog.getLog());
        initOrUpdateLogging(m, session.getSessionLog());
    }
    
    protected static Class findClass(String className, ClassLoader loader) throws ClassNotFoundException, PrivilegedActionException {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            return (Class)AccessController.doPrivileged(new PrivilegedClassForName(className, true, loader));
        } else {
            return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(className, true, loader);
        }
    }
    
    protected static Class findClassForProperty(String className, String propertyName, ClassLoader loader) {
        try {
            return findClass(className, loader);
        } catch (PrivilegedActionException exception1) {
            throw EntityManagerSetupException.classNotFoundForProperty(className, propertyName, exception1.getException());
        } catch (ClassNotFoundException exception2) {
            throw EntityManagerSetupException.classNotFoundForProperty(className, propertyName, exception2);
        }
    }
    
    protected void updateDescriptorCacheSettings(Map m, ClassLoader loader) {
        Map typeMap = PropertiesHandler.getPrefixValuesLogDebug(TopLinkProperties.CACHE_TYPE_, m, session);
        Map sizeMap = PropertiesHandler.getPrefixValuesLogDebug(TopLinkProperties.CACHE_SIZE_, m, session);
        Map sharedMap = PropertiesHandler.getPrefixValuesLogDebug(TopLinkProperties.CACHE_SHARED_, m, session);
        if(typeMap.isEmpty() && sizeMap.isEmpty() && sharedMap.isEmpty()) {
            return;
        }

        boolean hasDefault = false;
        
        String defaultTypeName = (String)typeMap.remove(TopLinkProperties.DEFAULT);
        Class defaultType = null;
        if(defaultTypeName != null) {
            defaultType = findClassForProperty(defaultTypeName, TopLinkProperties.CACHE_TYPE_DEFAULT, loader);
            hasDefault = true;
        }
        
        String defaultSizeString = (String)sizeMap.remove(TopLinkProperties.DEFAULT);
        Integer defaultSize = null;
        if(defaultSizeString != null) {
            defaultSize = Integer.parseInt(defaultSizeString);
            hasDefault = true;
        }
        
        String defaultSharedString = (String)sharedMap.remove(TopLinkProperties.DEFAULT);
        Boolean defaultShared = null;
        if(defaultSharedString != null) {
            defaultShared = Boolean.parseBoolean(defaultSharedString);
            hasDefault = true;
        }
        
        Iterator it = session.getDescriptors().values().iterator();
        while (it.hasNext() && (hasDefault || !typeMap.isEmpty() || !sizeMap.isEmpty() || !sharedMap.isEmpty())) {
            ClassDescriptor descriptor = (ClassDescriptor)it.next();
            
            if(descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
                continue;
            }
            
            String entityName = descriptor.getAlias();
            String className = descriptor.getJavaClass().getName();
            String name;
            
            Class type = defaultType;
            name = entityName;
            String typeName = (String)typeMap.remove(name);
            if(typeName == null) {
                name = className;
                typeName = (String)typeMap.remove(name);
            }
            if(typeName != null) {
                type = findClassForProperty(typeName, TopLinkProperties.CACHE_TYPE_ + name, loader);
            }
            if(type != null) {
                descriptor.setIdentityMapClass(type);
            }

            Integer size = defaultSize;
            name = entityName;
            String sizeString = (String)sizeMap.remove(name);
            if(sizeString == null) {
                name = className;
                sizeString = (String)sizeMap.remove(name);
            }
            if(sizeString != null) {
                size = Integer.parseInt(sizeString);
            }
            if(size != null) {
                descriptor.setIdentityMapSize(size.intValue());
            }

            Boolean shared = defaultShared;
            name = entityName;
            String sharedString = (String)sharedMap.remove(name);
            if(sharedString == null) {
                name = className;
                sharedString = (String)sharedMap.remove(name);
            }
            if(sharedString != null) {
                shared = Boolean.parseBoolean(sharedString);
            }
            if(shared != null) {
                descriptor.setIsIsolated(!shared.booleanValue());
            }
        }
    }

    /**
     * Perform any steps necessary prior to actual deployment.  This includes any steps in the session
     * creation that do not require the real loaded domain classes.
     * 
     * The first call to this method caches persistenceUnitInfo which is reused in the following calls.
     * 
     * Note that in JSE case factoryCount is NOT incremented on the very first call 
     * (by JavaSECMPInitializer.callPredeploy, typically in preMain).
     * That provides 1 to 1 correspondence between factoryCount and the number of open factories.
     * 
     * In case factoryCount > 0 the method just increments factoryCount.
     * factory == 0 triggers creation of a new session.
     * 
     * This method and undeploy - the only methods altering factoryCount - should be synchronized.
     *
     * @return A transformer (which may be null) that should be plugged into the proper
     *         classloader to allow classes to be transformed as they get loaded.
     * @see #predeploy(javax.persistence.spi.PersistenceUnitInfo, java.util.Map)
     */
    public synchronized ClassTransformer predeploy(PersistenceUnitInfo info, Map extendedProperties) {
        if(state == STATE_DEPLOY_FAILED) {
            throw new PersistenceException(EntityManagerSetupException.cannotPredeploy(persistenceUnitInfo.getPersistenceUnitName(), state));
        }
        if(state == STATE_PREDEPLOYED || state == STATE_DEPLOYED) {
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "predeploy_begin", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
            factoryCount++;
            if(session != null) {
                session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "predeploy_end", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
            }
            return null;
        } else if(state == STATE_INITIAL || state == STATE_UNDEPLOYED) {
            persistenceUnitInfo = info;
        }
        // state is INITIAL, PREDEPLOY_FAILED or UNDEPLOYED
        try {
            ClassLoader privateClassLoader = persistenceUnitInfo.getNewTempClassLoader();

            // create server session (it should be done before initializing ServerPlatform)
            // (also before translateOldProperties - it requires session to log warnings)
            session = new ServerSession(new Project(new DatabaseLogin()));
            
            predeployProperties = mergeMaps(extendedProperties, persistenceUnitInfo.getProperties());
    
            // translate old properties
            // this should be done before using properties (i.e. ServerPlatform)
            translateOldProperties(predeployProperties, null);
    
            ClassLoader realClassLoader = persistenceUnitInfo.getClassLoader();
            // ServerSession name  and ServerPlatform must be set prior to setting the loggers.
            setServerSessionName(predeployProperties);
            updateServerPlatform(predeployProperties, realClassLoader);
    
            // Update loggers and settings for the singleton logger and the session logger.
            updateLoggers(predeployProperties, true, false, realClassLoader);
            
            warnOldProperties(predeployProperties, session);
    
            session.getPlatform().setConversionManager(new EJB30ConversionManager());
        
            PersistenceUnitTransactionType transactionType=null;
            //find and override the transaction type
            String transTypeString = getConfigPropertyAsStringLogDebug(TopLinkProperties.TRANSACTION_TYPE, predeployProperties, session);
            if ( transTypeString != null ){
                transactionType=PersistenceUnitTransactionType.valueOf(transTypeString);
            }else if (persistenceUnitInfo!=null){
                transactionType=persistenceUnitInfo.getTransactionType();
            }
            if(!isValidationOnly(predeployProperties, false) && persistenceUnitInfo != null && transactionType == PersistenceUnitTransactionType.JTA) {
                if( predeployProperties.get(TopLinkProperties.JTA_DATASOURCE) == null && persistenceUnitInfo.getJtaDataSource() == null ) {
                    throw new PersistenceException(EntityManagerSetupException.jtaPersistenceUnitInfoMissingJtaDataSource(persistenceUnitInfo.getPersistenceUnitName()));
                }
            }
            
            // this flag is used to disable work done as a result of the LAZY hint on OneToOne mappings
            if(state == STATE_INITIAL || state == STATE_UNDEPLOYED) {
                enableLazyForOneToOne = true;
                isWeavingStatic = false;
                String weaving = getConfigPropertyAsString(TopLinkProperties.WEAVING);
                if (weaving != null && weaving.equalsIgnoreCase("false")) {
                    enableLazyForOneToOne = false;
                }else if (weaving != null && weaving.equalsIgnoreCase("static")) {
                    isWeavingStatic = true;
                }
            }
            
            boolean throwExceptionOnFail = "true".equalsIgnoreCase(
                    EntityManagerFactoryProvider.getConfigPropertyAsStringLogDebug(EntityManagerFactoryProvider.TOPLINK_ORM_THROW_EXCEPTIONS, predeployProperties, "true", session));                
    
            // Create an instance of MetadataProcessor for specified persistence unit info
            processor = new MetadataProcessor(persistenceUnitInfo, session, privateClassLoader, enableLazyForOneToOne);
            
            // Process the Object/relational metadata from XML and annotations.
            PersistenceUnitProcessor.processORMetadata(processor,privateClassLoader, session,throwExceptionOnFail);
    
            
            // The connector will be reconstructed when the session is actually deployed
            session.getProject().getLogin().setConnector(new DefaultConnector());
    
            if (session.getIntegrityChecker().hasErrors()){
                session.handleException(new IntegrityException(session.getIntegrityChecker()));
            }
    
            // The transformer is capable of altering domain classes to handle a LAZY hint for OneToOne mappings.  It will only
            // be returned if we we are mean to process these mappings
            ClassTransformer transformer = null;
            if (enableLazyForOneToOne){
                // build a list of entities the persistence unit represented by this EntityManagerSetupImpl will use
                Collection entities = PersistenceUnitProcessor.buildEntityList(processor,privateClassLoader);
                transformer = TransformerFactory.createTransformerAndModifyProject(session, entities, privateClassLoader);
            }
            
            // factoryCount is not incremented only in case of a first call to preDeploy
            // in non-container mode: this call is not associated with a factory
            // but rather done by JavaSECMPInitializer.callPredeploy (typically in preMain).
            if((state != STATE_INITIAL && state != STATE_UNDEPLOYED) || this.isInContainerMode()) {
                factoryCount++;
            }
            state = STATE_PREDEPLOYED;
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "predeploy_end", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
            //gf3146: if static weaving is used, we should not return a transformer.  Transformer should still be created though as it modifies descriptros 
            if (isWeavingStatic) {
                return null;
            } else {
                return transformer;
            }
        } catch (RuntimeException ex) {
            state = STATE_PREDEPLOY_FAILED;
            session = null;
            throw new PersistenceException(EntityManagerSetupException.predeployFailed(persistenceUnitInfo.getPersistenceUnitName(), ex));
        }
    }

    /**
     * Check the provided map for an object with the given key.  If that object is not available, check the
     * System properties.  If it is not available from either location, return the default value.
     * @param propertyKey 
     * @param defaultValue
     * @return 
     */
    public String getConfigPropertyAsString(String propertyKey, String defaultValue){
        return getConfigPropertyAsStringLogDebug(propertyKey, predeployProperties, defaultValue, session);
    }

    public String getConfigPropertyAsString(String propertyKey){
        return getConfigPropertyAsStringLogDebug(propertyKey, predeployProperties, session);
    }

    /**
     * Return the name of the session this SetupImpl is building. The session name is only known at deploy
     * time and if this method is called prior to that, this method will return null.
     * @return 
     */
    public String getDeployedSessionName(){
        return session != null ? session.getName() : null;
    }
    
    public PersistenceUnitInfo getPersistenceUnitInfo(){
        return persistenceUnitInfo;
    }
    
    public boolean isValidationOnly(Map m) {
        return isValidationOnly(m, true);
    }
    
    protected boolean isValidationOnly(Map m, boolean shouldMergeMap) {
        if(shouldMergeMap) {
            m = mergeWithExistingMap(m);
        }
        String validationOnlyString = getConfigPropertyAsStringLogDebug(TOPLINK_VALIDATION_ONLY_PROPERTY, m, session);
        if(validationOnlyString != null) {
            return Boolean.parseBoolean(validationOnlyString);
        } else {
            return false;
        }
    }
    
    public boolean shouldGetSessionOnCreateFactory(Map m) {
        m = mergeWithExistingMap(m);
        return isValidationOnly(m, false);
    }
    
    protected Map mergeWithExistingMap(Map m) {
        if(predeployProperties != null) {
            return mergeMaps(m, predeployProperties);
        } else if(persistenceUnitInfo != null) {
            return mergeMaps(m, persistenceUnitInfo.getProperties());
        } else {
            return m;
        }
    }

    public boolean isInContainerMode(){
        return isInContainerMode;
    }

    /**
   * Override the default login creation method.
   * If persistenceInfo is available, use the information from it to setup the login
   * and possibly to set readConnectionPool.
   * @param m
   */
    protected void updateLogins(Map m){
        DatasourceLogin login = session.getLogin();
    
        // Note: This call does not checked the stored persistenceUnitInfo or extended properties because
        // the map passed into this method should represent the full set of properties we expect to process

        String user = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_USER, m, session);
        String password = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_PASSWORD, m, session);
        if(user != null) {
            login.setUserName(user);
        }
        if(password != null) {
            login.setPassword(securableObjectHolder.getSecurableObject().decryptPassword(password));
        }

        String toplinkPlatform = (String)PropertiesHandler.getPropertyValueLogDebug(TopLinkProperties.TARGET_DATABASE, m, session);
        if (toplinkPlatform != null) {
            login.setPlatformClassName(toplinkPlatform);
        }
        PersistenceUnitTransactionType transactionType = persistenceUnitInfo.getTransactionType();
        //find and override the transaction type
        String transTypeString = getConfigPropertyAsStringLogDebug(TopLinkProperties.TRANSACTION_TYPE, m, session);
        if ( transTypeString != null ){
            transactionType = PersistenceUnitTransactionType.valueOf(transTypeString);
        }
        //find the jta datasource
        javax.sql.DataSource jtaDatasource = getDatasourceFromProperties(m, TopLinkProperties.JTA_DATASOURCE, persistenceUnitInfo.getJtaDataSource());

        //find the non jta datasource  
        javax.sql.DataSource nonjtaDatasource = getDatasourceFromProperties(m, TopLinkProperties.NON_JTA_DATASOURCE, persistenceUnitInfo.getNonJtaDataSource());

        if (isValidationOnly(m, false) && transactionType == PersistenceUnitTransactionType.JTA && jtaDatasource == null){
            updateLoginDefaultConnector(login, m);
            return;
        }
        
        login.setUsesExternalTransactionController(transactionType == PersistenceUnitTransactionType.JTA);

        javax.sql.DataSource mainDatasource = null;
        javax.sql.DataSource readDatasource = null;
        if(login.shouldUseExternalTransactionController()) {
            // JtaDataSource is guaranteed to be non null - otherwise exception would've been thrown earlier
            mainDatasource = jtaDatasource;
            // only define readDatasource if there is jta mainDatasource
            readDatasource = nonjtaDatasource;
        } else {
            // JtaDataSource will be ignored because transactionType is RESOURCE_LOCAL
            if(jtaDatasource != null) {
                session.log(SessionLog.WARNING, SessionLog.TRANSACTION, "resource_local_persistence_init_info_ignores_jta_data_source", persistenceUnitInfo.getPersistenceUnitName());
            }
            if(nonjtaDatasource != null) {
                mainDatasource = nonjtaDatasource;
            } else {
                updateLoginDefaultConnector(login, m);
                return;
            }
        }

        // mainDatasource is guaranteed to be non null
        if(!(login.getConnector() instanceof JNDIConnector)) {
             JNDIConnector jndiConnector;
            if (mainDatasource instanceof DataSourceImpl) {
                //Bug5209363  Pass in the datasource name instead of the dummy datasource
                jndiConnector = new JNDIConnector(((DataSourceImpl)mainDatasource).getName());                
            } else {
                jndiConnector = new JNDIConnector(mainDatasource);                                
            }
            login.setConnector(jndiConnector);
            login.setUsesExternalConnectionPooling(true);
        }

        // set readLogin
        if(readDatasource != null) {
            DatasourceLogin readLogin = (DatasourceLogin)login.clone();
            readLogin.dontUseExternalTransactionController();
            JNDIConnector jndiConnector;
            if (readDatasource instanceof DataSourceImpl) {
                //Bug5209363  Pass in the datasource name instead of the dummy datasource
                jndiConnector = new JNDIConnector(((DataSourceImpl)readDatasource).getName());
            } else {
                jndiConnector = new JNDIConnector(readDatasource);                    
            }
            readLogin.setConnector(jndiConnector);
            session.setReadConnectionPool(readLogin);
        }
        
    }
    
  /**
   * This is used to return either the defaultDatasource or, if one exists, a datasource 
   * defined under the property from the Map m.  This method will build a DataSourceImpl
   * object to hold the url if the property in Map m defines a string instead of a datasource.
   */
    protected javax.sql.DataSource getDatasourceFromProperties(Map m, String property, javax.sql.DataSource defaultDataSource){
        Object datasource = getConfigPropertyLogDebug(property, m, session);
        if ( datasource == null ){
            return defaultDataSource;
        } 
        if ( datasource instanceof String){
            // Create a dummy DataSource that will throw an exception on access
            return new DataSourceImpl((String)datasource, null, null, null);
        }
        if ( !(datasource instanceof javax.sql.DataSource) ){
            //A warning should be enough.  Though an error might be better, the properties passed in could contain anything
            session.log(SessionLog.WARNING, SessionLog.PROPERTIES, "invalid_datasource_property_value", property, datasource);
            return defaultDataSource;
        }
        return (javax.sql.DataSource)datasource;
    }
    
  /**
   * In cases where there is no data source, we will use properties to configure the login for
   * our session.  This method gets those properties and sets them on the login.
   * @param login 
   * @param m 
   */
    protected void updateLoginDefaultConnector(DatasourceLogin login, Map m){
        if((login.getConnector() instanceof DefaultConnector)) {
            DatabaseLogin dbLogin = (DatabaseLogin)login;
            // Note: This call does not checked the stored persistenceUnitInfo or extended properties because
            // the map passed into this method should represent the full set of properties we expect to process
            String jdbcDriver = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_DRIVER, m, session);
            String connectionString = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_URL, m, session);
            if(connectionString != null) {
                dbLogin.setConnectionString(connectionString);
            }
            if(jdbcDriver != null) {
                dbLogin.setDriverClassName(jdbcDriver);
            }
        }
    }

    protected void updatePools(Map m) {
        // Sizes are irrelevant for external connection pool
        if(!session.getDefaultConnectionPool().getLogin().shouldUseExternalConnectionPooling()) {
            String strWriteMin = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MIN, m, session);
            if(strWriteMin != null) {
                session.getDefaultConnectionPool().setMinNumberOfConnections(Integer.parseInt(strWriteMin));
            }
            String strWriteMax = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MAX, m, session);
            if(strWriteMax != null) {
                session.getDefaultConnectionPool().setMaxNumberOfConnections(Integer.parseInt(strWriteMax));
            }
        }
        
        // Sizes and shared option are irrelevant for external connection pool
        if(!session.getReadConnectionPool().getLogin().shouldUseExternalConnectionPooling()) {
            String strReadMin = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_READ_CONNECTIONS_MIN, m, session);
            if(strReadMin != null) {
                session.getReadConnectionPool().setMinNumberOfConnections(Integer.parseInt(strReadMin));
            }
            String strReadMax = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_READ_CONNECTIONS_MAX, m, session);
            if(strReadMax != null) {
                session.getReadConnectionPool().setMaxNumberOfConnections(Integer.parseInt(strReadMax));
            }
            String strShouldUseShared = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_READ_CONNECTIONS_SHARED, m,session);
            if(strShouldUseShared != null) {
                boolean shouldUseShared = Boolean.parseBoolean(strShouldUseShared);
                boolean sessionUsesShared = session.getReadConnectionPool() instanceof ReadConnectionPool;
                if(shouldUseShared != sessionUsesShared) {
                    Login readLogin = session.getReadConnectionPool().getLogin();
                    int nReadMin = session.getReadConnectionPool().getMinNumberOfConnections();
                    int nReadMax = session.getReadConnectionPool().getMaxNumberOfConnections();
                    if(shouldUseShared) {
                        session.useReadConnectionPool(nReadMin, nReadMax);
                    } else {
                        session.useExclusiveReadConnectionPool(nReadMin, nReadMax);
                    }
                    // keep original readLogin
                    session.getReadConnectionPool().setLogin(readLogin);
                }
            }
        }
    }
    
  /**
   * Normally when a property is missing nothing should be applied to the session.
   * However there are several session attributes that defaulted in EJB3 to the values
   * different from TopLink defaults (for instance, in TopLink defaults binding to false,
   * EJB3 - to true).
   * This function applies defaults for such properties and registers the session.
   * All other session-related properties are applied in updateServerSession.
   * Note that updateServerSession may be called several times on the same session
   * (before login), but initServerSession is called just once - before the first call
   * to updateServerSession.
   * @param m
   */
    protected void initServerSession(Map m) {
        assignCMP3Policy();

        // Register session that has been created earlier.
        addSessionToGlobalSessionManager();
        
        // shouldBindAllParameters is true by default - set it if no property provided
        if (EntityManagerFactoryProvider.getConfigPropertyAsString(TopLinkProperties.JDBC_BIND_PARAMETERS, m) == null) {
//            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_default", new Object[]{TopLinkProperties.JDBC_BIND_PARAMETERS, "true"});
            session.getPlatform().setShouldBindAllParameters(true);
        }
        
        // set default descriptor cache size - set it to all descriptors if CACHE_SIZE_DEFAULT not provided
        if (PropertiesHandler.getPrefixedPropertyValue(TopLinkProperties.CACHE_SIZE_, TopLinkProperties.DEFAULT, m) == null) {
//            int defaultCacheSize = Integer.parseInt(PropertiesHandler.getDefaultPropertyValueLogDebug(TopLinkProperties.CACHE_SIZE_, session));
            int defaultCacheSize = Integer.parseInt(PropertiesHandler.getDefaultPropertyValue(TopLinkProperties.CACHE_SIZE_));
            Iterator descriptorsIterator = session.getDescriptors().values().iterator();
            while (descriptorsIterator.hasNext()) {
                 ((ClassDescriptor)descriptorsIterator.next()).setIdentityMapSize(defaultCacheSize);
            }
        }
    }

  /**
   * Set ServerSession name but do not register the session.
   * The session registration should be done in sync
   * with increment of the deployment counter, as otherwise the
   * undeploy will not behave correctly in case of a more
   * than one predeploy request for the same session name.
   * @param m the combined properties map.
   */
    protected void setServerSessionName(Map m) {
        // use default session name if none is provided
        String name = EntityManagerFactoryProvider.getConfigPropertyAsString(TopLinkProperties.SESSION_NAME, m);
        if(name == null) {
            if (persistenceUnitInfo.getPersistenceUnitRootUrl() != null){
                name = persistenceUnitInfo.getPersistenceUnitRootUrl().toString() + "-" + persistenceUnitInfo.getPersistenceUnitName();
            } else {
                name = persistenceUnitInfo.getPersistenceUnitName();
            }
        } 

        session.setName(name);
    }

  /**
   * Make any changes to our ServerSession that can be made after it is created.
   * @param m
   */
    protected void updateServerSession(Map m, ClassLoader loader) {
        if (session == null || session.isConnected()) {
            return;
        }

        // In deploy Session name and ServerPlatform could've changed which will affect the loggers.
        boolean serverPlatformChanged = updateServerPlatform(m, loader);
        boolean sessionNameChanged = updateSessionName(m);

        updateLoggers(m, serverPlatformChanged, sessionNameChanged, loader);

        String shouldBindString = getConfigPropertyAsStringLogDebug(TopLinkProperties.JDBC_BIND_PARAMETERS, m, session);
        if (shouldBindString != null) {
            session.getPlatform().setShouldBindAllParameters(Boolean.parseBoolean(shouldBindString));
        }

        updateLogins(m);
        if(!session.getLogin().shouldUseExternalTransactionController()) {
            session.getServerPlatform().disableJTA();
        }
        

        updatePools(m);
        
        updateDescriptorCacheSettings(m, loader);

        // Customizers should be processed last
        processDescriptorCustomizers(m, loader);
        processSessionCustomizer(m, loader);
    }

  /** 
   * This sets the isInContainerMode flag.
   * "true" indicates container case, "false" - SE.
   * @param isInContainerMode 
   */ 
    public void setIsInContainerMode(boolean isInContainerMode) {
        this.isInContainerMode = isInContainerMode;
   }

   protected void processSessionCustomizer(Map m, ClassLoader loader) {
        String sessionCustomizerClassName = getConfigPropertyAsStringLogDebug(TopLinkProperties.SESSION_CUSTOMIZER, m, session);
        if(sessionCustomizerClassName == null) {
            return;
        }
        
        Class sessionCustomizerClass = findClassForProperty(sessionCustomizerClassName, TopLinkProperties.SESSION_CUSTOMIZER, loader);
        SessionCustomizer sessionCustomizer;
        try {
            sessionCustomizer = (SessionCustomizer)sessionCustomizerClass.newInstance();
            sessionCustomizer.customize(session);
        } catch (Exception ex) {
            throw EntityManagerSetupException.failedWhileProcessingProperty(TopLinkProperties.SESSION_CUSTOMIZER, sessionCustomizerClassName, ex);
        }
   }

    protected void initOrUpdateLogging(Map m, SessionLog log) {
        String logLevelString = PropertiesHandler.getPropertyValueLogDebug(TopLinkProperties.LOGGING_LEVEL, m, session);
        if(logLevelString != null) {
            log.setLevel(AbstractSessionLog.translateStringToLoggingLevel(logLevelString));
        }
        // category-specific logging level
        Map categoryLogLevelMap = PropertiesHandler.getPrefixValuesLogDebug(TopLinkProperties.CATEGORY_LOGGING_LEVEL_, m, session);
        if(!categoryLogLevelMap.isEmpty()) {
            Iterator it = categoryLogLevelMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String category = (String)entry.getKey();
                String value = (String)entry.getValue();
                log.setLevel(AbstractSessionLog.translateStringToLoggingLevel(value), category);
            }
        }
        
        String tsString = getConfigPropertyAsStringLogDebug(TopLinkProperties.LOGGING_TIMESTAMP, m, session);
        if (tsString != null) {
            log.setShouldPrintDate(Boolean.parseBoolean(tsString));
        }
        String threadString = getConfigPropertyAsStringLogDebug(TopLinkProperties.LOGGING_THREAD, m, session);
        if (threadString != null) {
            log.setShouldPrintThread(Boolean.parseBoolean(threadString));
        }
        String sessionString = getConfigPropertyAsStringLogDebug(TopLinkProperties.LOGGING_SESSION, m, session);
        if (sessionString != null) {
            log.setShouldPrintSession(Boolean.parseBoolean(sessionString));
        }
        String exString = getConfigPropertyAsStringLogDebug(TopLinkProperties.LOGGING_EXCEPTIONS, m, session);
        if (exString != null) {
            log.setShouldLogExceptionStackTrace(Boolean.parseBoolean(exString));
        }
    }

    /**
     * Updates server session name if changed.
     * @return true if the name has changed.
     */
    protected boolean updateSessionName(Map m) {
        String newName = getConfigPropertyAsStringLogDebug(TopLinkProperties.SESSION_NAME, m, session);
        if(newName == null || newName.equals(session.getName())) {
            return false;
        }

        removeSessionFromGlobalSessionManager();
        session.setName(newName);
        addSessionToGlobalSessionManager();

        return true;
    }
    
    protected void processDescriptorCustomizers(Map m, ClassLoader loader) {
        Map customizerMap = PropertiesHandler.getPrefixValuesLogDebug(TopLinkProperties.DESCRIPTOR_CUSTOMIZER_, m, session);
        if(customizerMap.isEmpty()) {
            return;
        }

        Iterator it = customizerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            
            ClassDescriptor descriptor = session.getDescriptorForAlias(name);
            if(descriptor == null) {
                try {
                    Class javaClass = findClass(name, loader);
                    descriptor = session.getDescriptor(javaClass);
                } catch (Exception ex) {
                    // Ignore exception
                }
            }
            if(descriptor != null) {
                String customizerClassName = (String)entry.getValue();
                Class customizerClass = findClassForProperty(customizerClassName, TopLinkProperties.DESCRIPTOR_CUSTOMIZER_ + name, loader);
                try {
                    DescriptorCustomizer customizer = (DescriptorCustomizer)customizerClass.newInstance();
                    customizer.customize(descriptor);
                } catch (Exception ex) {
                    throw EntityManagerSetupException.failedWhileProcessingProperty(TopLinkProperties.DESCRIPTOR_CUSTOMIZER_ + name, customizerClassName, ex);
                }
            }
        }
    }
    
    
    public boolean isInitial() {
        return state == STATE_INITIAL;
    }

    public boolean isPredeployed() {
        return state == STATE_PREDEPLOYED;
    }

    public boolean isDeployed() {
        return state == STATE_DEPLOYED;
    }

    public boolean isUndeployed() {
        return state == STATE_UNDEPLOYED;
    }

    public boolean isPredeployFailed() {
        return state == STATE_PREDEPLOY_FAILED;
    }

    public boolean isDeployFailed() {
        return state == STATE_DEPLOY_FAILED;
    }

    public int getFactoryCount() {
        return factoryCount;
    }

    public boolean shouldRedeploy() {
        return state == STATE_UNDEPLOYED || state == STATE_PREDEPLOY_FAILED;
    }
    
    /**
     * Undeploy may be called several times, but only the call that decreases
     * factoryCount to 0 disconnects the session and removes it from the session manager.
     * This method and predeploy - the only methods altering factoryCount - should be synchronized.
     * After undeploy call that turns factoryCount to 0:
     *   session==null;
     *   PREDEPLOYED, DEPLOYED and DEPLOYED_FAILED states change to UNDEPLOYED state.
     */
    public synchronized void undeploy() {
        if(state == STATE_INITIAL || state == STATE_PREDEPLOY_FAILED || state == STATE_UNDEPLOYED) {
            // must already have factoryCount==0 and session==null
            return;
        }
        // state is PREDEPLOYED, DEPLOYED or DEPLOY_FAILED
        session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "undeploy_begin", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
        try {
            factoryCount--;
            if(factoryCount > 0) {
                return;
            }
            state = STATE_UNDEPLOYED;
            removeSessionFromGlobalSessionManager();
        } finally {
            session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "undeploy_end", new Object[]{getPersistenceUnitInfo().getPersistenceUnitName(), state, factoryCount});
            if(state == STATE_UNDEPLOYED) {
                session = null;
            }
        }
    }    
}
