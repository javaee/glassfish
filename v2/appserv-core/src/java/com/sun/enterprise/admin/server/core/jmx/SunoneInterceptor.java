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
package com.sun.enterprise.admin.server.core.jmx;

//JDK imports
import java.util.Set;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports 
import javax.management.*;
import java.lang.reflect.InvocationTargetException;

import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
//server
import com.sun.enterprise.server.ApplicationServer;

//Admin imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.exception.AFRuntimeStoreException;
import com.sun.enterprise.admin.common.exception.AFRuntimeException;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.util.proxy.ProxyFactory;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

import javax.management.MBeanServerFactory;
import javax.management.loading.ClassLoaderRepository;

import com.sun.enterprise.config.serverbeans.ServerHelper;

/* for checking persistent store */
import com.sun.enterprise.admin.server.core.jmx.storage.PersistenceChecker;
import com.sun.enterprise.admin.server.core.jmx.storage.MBeanManufacturer;
import com.sun.enterprise.server.ondemand.entry.*;


import com.sun.enterprise.interceptor.DynamicInterceptorHook;
import com.sun.appserv.management.helper.AMXDebugHelper;


/** A class that is a poor man's interceptor. There are following reasons that led
 * to use this interceptor and not the standard JMX 1.2 interceptor.
 * <LI> Tight deadlines and smoke test has to work :( </LI>
 * <LI> We have lazy loading going on. (Again poor man's) This means that any 
 * MBean has to be registered first before it can be invoked. We did not have any
 * good solution at the architectural level for that, nor does JMX 1.2 have it. The
 * way we do it is to check that persistent medium and/or Java Beans derived from the dtd
 * and initialized from domain.xml (in memory) indicate that such an element should exist,
 * and then we register the mbean before invoking it. This solves the problem of
 * initialization meaning the MBeans for all the elements in configuration file (domain.xml)
 * do not have to be registered in order to invoke the operation. </LI>
 * <LI> Still, we want a JMX 1.2 implementation of the MBeanServer as some 
 * immediate future work may need it. </LI>
 * <LI> Ugliness to begin with gives birth to more ugliness. This class has
 * to handle the case of config files being hand edited. :((( </LI>
 * Hence this interceptor results. All it does is the following:
 * <LI> Implements the javax.management.MBeanServer as sincerely as possible. </LI>
 * <LI> Uses Lazy loading meaning that the MBeans will be registered if they
 * do not already exist in registry of the MBeanServer. An attempt will be made
 * to see that config bean already exists. (This is not a violation per se, but
 * an interpretation of the MBeanServer specification).
 * @see javax.management.MBeanServer
 * @since 8.0
*/

public final class SunoneInterceptor
    implements DynamicInterceptorHook {
    
    protected final AMXDebugHelper    mDebug;

    // this stuff is probably all DEFUNCT
    private static final String  HOT_CONFIG_METHOD_NAME  = "canApplyConfigChanges";
    private static final String  FORCE_APPLY_METHOD_NAME = "overwriteConfigChanges";
    private static final String  APPLY_METHOD_NAME       = "applyConfigChanges";
    private static final String  USE_MANUAL_METHOD_NAME  = "useManualConfigChanges";
    private static final String  GET_HOST_AND_PORT_METHOD_NAME = "getHostAndPort";
   
    private static final Logger sLogger = 
            Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager localStrings =
		StringManager.getManager( SunoneInterceptor.class );
        
    private final AdminContext  adminContext;
    
    // MBeanServer to which requests ultimately get delegated
    private final MBeanServer   delegateMBeanServer;
    
    // MBeanServer which wraps 'delegateMBeanServer'
    private final MBeanServer   proxyMBeanServer;
    
    // MBeanServer to which MBeans should be registered;
    // the "real" MBeanServer enclosing this one
    private final MBeanServer   outerMBeanServer;

    private static SunoneInterceptor _Instance = null;
    
        protected final void
    debug( final Object... args)
    {
        mDebug.println( args );
    }
    
    /**
        @param adminContextIn the AdminContext
        @param outerMBeanServer  the MBeanServer to be used for registering MBeans
        @param delegateMBeanServer the MBeanServer to which requests are forwarded
    */
        private
    SunoneInterceptor(
        final AdminContext  adminContextIn,
        final MBeanServer   outerMBeanServerIn,
        final MBeanServer   delegateMBeanServerIn ) {
        
        mDebug  = new AMXDebugHelper( "__SunoneInterceptor__" );
        mDebug.setEchoToStdOut( true );
        debug( "SunoneInterceptor.SunoneInterceptor" );


        adminContext        = adminContextIn;
        delegateMBeanServer = delegateMBeanServerIn;
        
        outerMBeanServer = outerMBeanServerIn;
        proxyMBeanServer = (MBeanServer)ProxyFactory.createProxy(
                    MBeanServer.class, delegateMBeanServer,
                    adminContext.getMBeanServerInterceptor());
        logMBeanServerInfo();
        try {
            initialize(); 
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }
    
        public static synchronized SunoneInterceptor
    createInstance(
        final AdminContext adminContext,
        final MBeanServer  outerMBeanServer,
        final MBeanServer delegateMBeanServer) {
        if ( _Instance != null ) {
            throw new IllegalStateException();
        }
        _Instance = new SunoneInterceptor(
            adminContext, outerMBeanServer, delegateMBeanServer );
        return _Instance;
    }


        public static synchronized SunoneInterceptor
    getInstance() {
        if ( _Instance == null ) {
            throw new IllegalStateException();
        }
        return _Instance;
    }

    /**
        Initializes the MBeanServer. This method registers the System MBeans.
        The System MBeans are assumed to have default constructor.
       
        @throws InitException if any of the System MBeans can't be initialized.
    */

    private void initialize()  
        throws ClassNotFoundException, NoSuchMethodException,
                InstantiationException, IllegalAccessException,
                InvocationTargetException, InstanceAlreadyExistsException,
                MBeanRegistrationException, NotCompliantMBeanException
        {
        final ObjectName controllerObjectName 
                = ObjectNames.getControllerObjectName();
        final ObjectName configObjectName     
                = ObjectNames.getGenericConfiguratorObjectName();
        final ObjectName[] objectNames = { 
            controllerObjectName,
            configObjectName };
        final String controllerClassName = 
            "com.sun.enterprise.admin.server.core.mbean.config.ServerController";
        final String configClassName = 
            "com.sun.enterprise.admin.server.core.mbean.config.GenericConfigurator";
        final String[] clNames = {controllerClassName, configClassName};
            for (int i = 0 ; i < clNames.length ; i++) {
                createAndRegister( clNames[i], objectNames[ i ] );
            }
        registerDottedNameSupport();
     }
    
    /**
    	<LLC>
    	Load the MBeans which support dotted names.
     */
    
	// screwy build dependencies for this sort of lame instantation
	static private final String DottedMBeansIniterClassName	= 
			"com.sun.enterprise.admin.mbeans.DottedNameMBeansIniter";
			
		private void
	registerDottedNameSupport()
        throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException
	{
		final Class		initerClass	= Class.forName( DottedMBeansIniterClassName );
		
		// invoke new DottedNamesMBeanIniter( MBeanServer m )
		final Class []		signature	= new Class [] { MBeanServer.class };
		final java.lang.reflect.Constructor	constructor	= initerClass.getConstructor( signature );
		constructor.newInstance( new Object [] { outerMBeanServer } );
		// done--it will have done its job
	}
	
	private ObjectInstance createAndRegister( String className, ObjectName objectName )
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException
	{
        final Class  mbeanClass				= Class.forName( className );
        final Object mbeanImpl				= mbeanClass.newInstance();
        final ObjectInstance mbeanInstance	= this.registerMBean( mbeanImpl, objectName );
        
        sLogger.log(Level.FINE, "core.system_mbean_init_ok", objectName.toString() );
        return( mbeanInstance );
	}
	
    /* </LLC> */
    
    private ObjectInstance registerMBean(Object object, ObjectName objectName) 
        throws  InstanceAlreadyExistsException,
                MBeanRegistrationException,
                NotCompliantMBeanException {
        // !!! registration must be done with 'outerMBeanServer';
        // we are not delegating here and so the "outside world" must see
        // the registration
        return outerMBeanServer.registerMBean(object, objectName);
    }

    private void generateEntryContext(Object obj) {
        ServerEntryHelper.generateMbeanEntryContext((ObjectName) obj);
    }
    
    
    public Object invoke(ObjectName objectName, String operationName, 
        Object[] params, String[] signature) 
        throws ReflectionException, InstanceNotFoundException, MBeanException {
        generateEntryContext(objectName);
	  
        if(isInstanceMBean(objectName) && isConfigCheckRequired(operationName)) {
            checkHotConfigChanges(objectName);
        }
        
         if(FORCE_APPLY_METHOD_NAME.equals(operationName)) {
             // Manual changes to config will be overwritten, so persist the
             // state restart is required.
             String instanceName =
                     ObjectNameHelper.getServerInstanceName(objectName);
             AdminEventCache cache = AdminEventCache.getInstance(instanceName);
             cache.setRestartNeeded(true);
            //operationName = APPLY_METHOD_NAME;
         }
        registerWithPersistenceCheck(objectName);
        //logMBeanInfo(objectName);
        Object actualResult = proxyMBeanServer.invoke(objectName, operationName,
            params, signature );
        return ( actualResult );
    }
    
    public Object getAttribute(ObjectName objectName, String attributeName) 
        throws InstanceNotFoundException, AttributeNotFoundException, 
               MBeanException, ReflectionException {
        if(isInstanceMBean(objectName)) {
            checkHotConfigChanges(objectName);
        }
        registerWithPersistenceCheck(objectName);
        //logMBeanInfo(objectName);
        Object value = proxyMBeanServer.getAttribute(objectName, attributeName);
        return ( value );
    }
    
    public void setAttribute(ObjectName objectName, Attribute attribute) throws 
        InstanceNotFoundException, AttributeNotFoundException, 
        MBeanException, ReflectionException, InvalidAttributeValueException {
        if(isInstanceMBean(objectName)) {
            checkHotConfigChanges(objectName);
        }
        registerWithPersistenceCheck(objectName);
        //logMBeanInfo(objectName);
        proxyMBeanServer.setAttribute(objectName, attribute);
    }

    public AttributeList  getAttributes(ObjectName objectName, String[] attrNames) 
        throws InstanceNotFoundException, ReflectionException {//, RuntimeOperationsException
//        if(isInstanceMBean(objectName)) 
        {
            checkHotConfigChanges(objectName);
        }
        
        registerWithPersistenceCheck(objectName);
        return ( proxyMBeanServer.getAttributes(objectName, attrNames) );
    }

    public AttributeList setAttributes (ObjectName objectName, AttributeList attributeList) 
            throws InstanceNotFoundException, ReflectionException {
        if(isInstanceMBean(objectName)) {
            checkHotConfigChanges(objectName);
        }

        registerWithPersistenceCheck(objectName);
        return ( proxyMBeanServer.setAttributes(objectName, attributeList) );
    }

    /*
    private void unregisterMBean(ObjectName objectName) 
        throws InstanceNotFoundException, MBeanRegistrationException {
        proxyMBeanServer.unregisterMBean(objectName);
    }
	
    private Integer getMBeanCount() {
	return ( proxyMBeanServer.getMBeanCount() );
    }

    private Set queryMBeans(ObjectName name, QueryExp exp) {
        registerConfigMBeans();
        return ( proxyMBeanServer.queryMBeans(name, exp) );
    }

    private boolean isRegistered(ObjectName name) {
       return proxyMBeanServer.isRegistered(name); 
    }

    private void addNotificationListener(ObjectName objectName, 
        NotificationListener notificationListener, 
        NotificationFilter notificationFilter, Object obj) 
        throws InstanceNotFoundException {
        proxyMBeanServer.addNotificationListener(objectName, 
            notificationListener, notificationFilter, obj);
    }

    private void addNotificationListener (ObjectName objectName, 
        ObjectName objectName1, NotificationFilter notificationFilter, 
        Object obj) throws InstanceNotFoundException {
        proxyMBeanServer.addNotificationListener(objectName, objectName1,
        notificationFilter, obj);
    }

    private ObjectInstance createMBean (String str, ObjectName objectName) 
        throws ReflectionException, InstanceAlreadyExistsException, 
        MBeanRegistrationException, MBeanException, 
        NotCompliantMBeanException {
        return proxyMBeanServer.createMBean (str, objectName);
    }

    private ObjectInstance createMBean (String str, ObjectName objectName, 
        ObjectName objectName2) throws ReflectionException, 
        InstanceAlreadyExistsException, MBeanRegistrationException, 
        MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return ( proxyMBeanServer.createMBean (str, objectName, objectName2) );
    }

    private ObjectInstance createMBean (String str, ObjectName objectName, 
        Object[] obj, String[] str3) 
        throws ReflectionException, InstanceAlreadyExistsException, 
        MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return proxyMBeanServer.createMBean (str, objectName, obj, str3);
    }

    private ObjectInstance createMBean (String str, ObjectName objectName, 
        ObjectName objectName2, Object[] obj, String[] str4) 
        throws ReflectionException, InstanceAlreadyExistsException, 
        MBeanRegistrationException, MBeanException, 
        NotCompliantMBeanException, InstanceNotFoundException {
        return proxyMBeanServer.createMBean (str, objectName,
            objectName2, obj, str4);
    }

    private ObjectInputStream deserialize (String str, byte[] values) 
        throws OperationsException, ReflectionException {
        return proxyMBeanServer.deserialize (str, values);
    }

    private ObjectInputStream deserialize (ObjectName objectName, byte[] values) 
        throws InstanceNotFoundException, OperationsException {
        return proxyMBeanServer.deserialize (objectName, values);
    }

    private ObjectInputStream deserialize (String str, ObjectName objectName, 
        byte[] values) throws InstanceNotFoundException, OperationsException, 
        ReflectionException {
        return proxyMBeanServer.deserialize (str, objectName, values);
    }

    private String getDefaultDomain() {
        return proxyMBeanServer.getDefaultDomain();
    }
    
    private ObjectInstance getObjectInstance(ObjectName objectName)
        throws InstanceNotFoundException {
        return proxyMBeanServer.getObjectInstance(objectName);
    }
    
    private Object instantiate(String str) throws ReflectionException,
        MBeanException {
        return proxyMBeanServer.instantiate(str);
    }
    
    private Object instantiate(String str, ObjectName objectName)
    throws ReflectionException, MBeanException, InstanceNotFoundException {
        return proxyMBeanServer.instantiate(str, objectName);
    }
    
    private Object instantiate(String str, Object[] obj, String[] str2)
    throws ReflectionException, MBeanException {
        return proxyMBeanServer.instantiate(str, obj, str2);
    }
    
    private Object instantiate(String str, ObjectName objectName,
    Object[] obj, String[] str3)
    throws ReflectionException, MBeanException, InstanceNotFoundException {
        return proxyMBeanServer.instantiate(str, objectName, obj, str3);
    }

    private boolean isInstanceOf (ObjectName objectName, String str) 
    throws InstanceNotFoundException {
            return proxyMBeanServer.isInstanceOf(objectName, str);
    }

    private Set queryNames (ObjectName objectName, QueryExp queryExp) {
        registerConfigMBeans();
        return proxyMBeanServer.queryNames(objectName, queryExp);
    }

    private void removeNotificationListener (ObjectName objectName, 
            ObjectName objectName1) 
            throws InstanceNotFoundException, ListenerNotFoundException {
            proxyMBeanServer.removeNotificationListener (objectName, 
                objectName1);
    }

    private void removeNotificationListener (ObjectName objectName, 
            NotificationListener notificationListener) 
            throws InstanceNotFoundException, ListenerNotFoundException {
            proxyMBeanServer.removeNotificationListener (objectName, 
                notificationListener);
    }
    */

    public MBeanInfo getMBeanInfo(ObjectName objName) throws
        InstanceNotFoundException, IntrospectionException, ReflectionException {
		registerWithPersistenceCheck(objName);
        return ( proxyMBeanServer.getMBeanInfo(objName) );
    }
    
    
    // START BACKUP_HOT ISSUE FIX APIs
        
    private void checkHotConfigChanges(ObjectName mbeanName) {
        //different case
        try {
            //check whether the mbeanName pertains to a server instance.
//            String instanceName = ObjectNameHelper.getServerInstanceName(mbeanName);
String instanceName = ApplicationServer.getServerContext().getInstanceName();
            ObjectName instanceObjectName = ObjectNames.getServerInstanceObjectName(instanceName);
            Object canApply = this.invoke(instanceObjectName, HOT_CONFIG_METHOD_NAME,
                null, null);
            //debug("return value" + canApply);
            if (canApply.equals(Boolean.FALSE)) {
				String msg = localStrings.getString(
                       "admin.server.core.jmx.configuration_changed_apply_changes",
                       instanceName);
                throw new AFRuntimeStoreException( msg );
            }
        } catch(AFRuntimeStoreException af) { 
            throw af;
        } catch (Exception e) {
			String msg = localStrings.getString( "admin.server.core.jmx.bad_server_configuration" );
            sLogger.log(Level.INFO, msg, e);
            throw new AFRuntimeException( msg, e );
        }
    }

    private boolean isInstanceMBean(ObjectName mbeanName) {
        if(ObjectNameHelper.getServerInstanceName(mbeanName) != null) { 
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isConfigCheckRequired(String operationName) {
        //debug("Entering isConfigCheckRequired:" + operationName);
        
        if(GET_HOST_AND_PORT_METHOD_NAME.equals(operationName)) {
            //debug("getHostAndPort: returning FALSE");
            return false;
        }
        
        if(FORCE_APPLY_METHOD_NAME.equals(operationName)) {
                //debug("overwriteConfigChanges: returning FALSE");
                return false;
        }
        
        if(USE_MANUAL_METHOD_NAME.equals(operationName)) {
            //debug("useManualConfigChanges: returning FALSE");
            return false;
        }
        
       
        if(HOT_CONFIG_METHOD_NAME.equals(operationName)) {
            return false;
        }
        
        //START: Optimization to prevent multiple checks for changes
        if("getMBeanInfo".equals(operationName)) {
            return false;
        }
        //END: Optimization to prevent multiple checks for changes
         
        //debug("RETURNING true");
        return true;
    }
    
           
        public ClassLoader
    getClassLoader(final ObjectName objectName) 
        throws InstanceNotFoundException {
        debug( "SunoneInterceptor: getClassLoader: " + objectName );
        registerWithPersistenceCheck(objectName);
        return proxyMBeanServer.getClassLoader( objectName );
    }    
    
        public ClassLoader
    getClassLoaderFor(final ObjectName objectName) 
        throws InstanceNotFoundException {
        debug( "SunoneInterceptor: getClassLoaderFor: " + objectName );
        registerWithPersistenceCheck(objectName);
        return proxyMBeanServer.getClassLoaderFor( objectName );
    }
    
        private ClassLoaderRepository
    getClassLoaderRepository() {
        debug( "SunoneInterceptor: getClassLoaderRepository()" );
		return ( proxyMBeanServer.getClassLoaderRepository() );
    }
    
    /*
    private String[] getDomains() {
        return ( proxyMBeanServer.getDomains() );
    }
      
    private void removeNotificationListener(ObjectName objectName, 
        NotificationListener notificationListener, NotificationFilter 
        notificationFilter, Object obj) throws InstanceNotFoundException, 
        ListenerNotFoundException {
            proxyMBeanServer.
                removeNotificationListener(objectName, notificationListener,
                notificationFilter, obj);
    }
    
    private void removeNotificationListener(ObjectName objectName, 
        ObjectName objectName1, NotificationFilter notificationFilter, 
        Object obj) 
        throws InstanceNotFoundException, ListenerNotFoundException {
            proxyMBeanServer.removeNotificationListener(objectName, objectName1,
                notificationFilter, obj);
    }
    */
    
    /** Logs the MBeanServer information. It is logged to the server's output/error
     * log, to convey the information about MBeanServer implementation used.
     * @throws RuntimeException if there are some problems in invoking 
     * implementation methods.
    */
    private void logMBeanServerInfo() {
        try {
            final String        name  = "JMImplementation:type=MBeanServerDelegate";
            final ObjectName    oName = new ObjectName(name);
            
            sLogger.log(Level.FINE, "core.mbs_info");
            //log the implementation name
            String attrName     = "ImplementationName";
            String result       = (String) proxyMBeanServer.
                    getAttribute(oName, attrName);
            sLogger.log(Level.FINE, "core.mbs_implementation", result);
            //log the implementation vendor
            attrName    = "ImplementationVendor";
            result      = (String) proxyMBeanServer.getAttribute(oName, attrName);
            sLogger.log(Level.FINE, "core.mbs_vendor", result);
            //log the impl version
            attrName = "ImplementationVersion";
            result = (String) proxyMBeanServer.getAttribute(oName, attrName);
            sLogger.log(Level.FINE, "core.jmx_impl_version", result);
            //log the MBeanServerId
            attrName = "MBeanServerId";
            result = (String) proxyMBeanServer.getAttribute(oName, attrName);
            sLogger.log(Level.FINE, "core.mbs_id", result);
            result = proxyMBeanServer.getClass().getName();
            sLogger.log(Level.FINE, "core_mbs_classname", result);            
        }
        catch(Exception e) {
            throw new RuntimeException (e);
        }
    }
    /**
     * this method added to synchronize lazy loaded MBeans manufacturing and registering
     */
    synchronized private void manufactureAndRegisterMBean(ObjectName oName) throws Exception
    {
        //the "second" check inside synchronized area (it will perevent double manufacturing)
        if ( outerMBeanServer.isRegistered(oName) ) {
            //sLogger.log(Level.FINE, "core.mbean_exists", oName);
            debug( "manufactureAndRegisterMBean: already registered: " + oName );
            return;  //already registered
        }
        //call to lazy loading here
        final Object product = manufactureMBean(oName);
        if(product==null)
        {
            debug( "manufactureAndRegisterMBean: can't manufacture: " + oName );
            final String msg = localStrings.getString(
                   "admin.server.core.jmx.lazybean_not_found",
                   oName.toString());
            throw new InstanceNotFoundException(msg);
        }
        //register MBean.
        debug( "manufactureAndRegisterMBean: registering: " + oName );
        this.registerMBean(product, oName);
        debug( "manufactureAndRegisterMBean: registered: " + oName );
        sLogger.log(Level.FINE, "core.create_and_register", oName);
    }
    
    /** This method does the following in a sequential manner:
     * <LI> If the MBean with given ObjectName is registered, do nothing. </LI>
     * <LI> If the MBean does not exist, create the MBean by "manufacturing process". </LI>
     * <LI> If the MBean is thus manufactured, then register that MBean in MBeanServer. </LI>
     * @param oName ObjectName that corresponds to a single MBean - should not be a pattern.
     * @since 8.0.
     * @exception IllegalArgumentException if oName is a pattern.
     * @exception RuntimeException with actual exception embedded in case operation fails.
    */
    private void registerWithPersistenceCheck(final ObjectName oName)
        throws InstanceNotFoundException
    {
        if ( "ias".equals( oName.getDomain() ) ) {
            throw new RuntimeException( "JMX domain 'ias' not suppported" );
        }
        
        if (! outerMBeanServer.isRegistered(oName) ) {
    debug( "MBean NOT registered, will try to manufacture: " + oName );
            try {
                //Manufacture the MBean now.
                manufactureAndRegisterMBean(oName);
    debug( "Manufactured: " + oName );
            }
            catch (InstanceNotFoundException infe)
            {
    debug( "FAILED to manufacture: " + oName + " | " + infe );
                throw infe;
            }
            catch (RuntimeException re)
            {
    debug( "FAILED to manufacture: " + oName + " | " + re );
                throw re;
            }
            catch (Exception e) {
    debug( "FAILED to manufacture: " + oName + " | " + e );
                throw new RuntimeException(e);
            }
        }
    }
    
    private Object manufactureMBean(ObjectName oName) throws InstanceNotFoundException
    {
        final PersistenceChecker checker         = new PersistenceChecker();
        checker.setAdminContext(adminContext);
        final Object             storedObject	 = checker.findElement(oName);
        Object                   match           = null;
        if (storedObject != null) {
            MBeanManufacturer producer = new MBeanManufacturer(oName, storedObject);
            producer.setAdminContext(adminContext);
            match = producer.createMBeanInstance();
        }
        else {
            //this is severe - storage is not in sync with whatever is requested to be done.
            sLogger.log(Level.FINEST, "core.not_in_config", oName);
        }
        //Actually it should be asserted that match is NOT null.
        return ( match );
    }
        
    private void shutdown() {
        MBeanServerFactory.releaseMBeanServer(proxyMBeanServer);
        sLogger.log(Level.FINE, "core.release_mbs");
    }
    
    /* comment out this method later on */
    private void logMBeanInfo(ObjectName oName) {
        
        // if we are not logging at the finest level, just return
        if (!sLogger.isLoggable(Level.FINEST)) {
            return;            
        }
        
        //This method assumes that the mbean is registered.
	MBeanInfo info = null;
	try {
	    info = proxyMBeanServer.getMBeanInfo(oName);
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
	sLogger.log(Level.FINEST,"\nCLASSNAME: \t"+ info.getClassName());
	sLogger.log(Level.FINEST,"\nDESCRIPTION: \t"+ info.getDescription());
	sLogger.log(Level.FINEST,"\nATTRIBUTES");
	MBeanAttributeInfo[] attrInfo = info.getAttributes();
	if (attrInfo.length>0) {
	    for(int i=0; i<attrInfo.length; i++) {
		sLogger.log(Level.FINEST," ** NAME: \t"+ attrInfo[i].getName());
		sLogger.log(Level.FINEST,"    DESCR: \t"+ attrInfo[i].getDescription());
		sLogger.log(Level.FINEST,"    TYPE: \t"+ attrInfo[i].getType() +
		     "\tREAD: "+ attrInfo[i].isReadable() +
		     "\tWRITE: "+ attrInfo[i].isWritable());
	    }
	} else 
            sLogger.log(Level.FINEST," ** No attributes **");
	sLogger.log(Level.FINEST,"\nCONSTRUCTORS");
	MBeanConstructorInfo[] constrInfo = info.getConstructors();
	for(int i=0; i<constrInfo.length; i++) {
	    sLogger.log(Level.FINEST," ** NAME: \t"+ constrInfo[i].getName());
	    sLogger.log(Level.FINEST,"    DESCR: \t"+ constrInfo[i].getDescription());
	    sLogger.log(Level.FINEST,"    PARAM: \t"+ constrInfo[i].getSignature().length +" parameter(s)");
	}
	sLogger.log(Level.FINEST,"\nOPERATIONS");
	MBeanOperationInfo[] opInfo = info.getOperations();
	if (opInfo.length>0) {
	    for(int i=0; i<opInfo.length; i++) {
		sLogger.log(Level.FINEST," ** NAME: \t"+ opInfo[i].getName());
		sLogger.log(Level.FINEST,"    DESCR: \t"+ opInfo[i].getDescription());
		sLogger.log(Level.FINEST,"    PARAM: \t"+ opInfo[i].getSignature().length +" parameter(s)");
	    }
	} else 
            sLogger.log(Level.FINEST," ** No operations ** ");
	sLogger.log(Level.FINEST,"\nNOTIFICATIONS");
	MBeanNotificationInfo[] notifInfo = info.getNotifications();
	if (notifInfo.length>0) {
	    for(int i=0; i<notifInfo.length; i++) {
		sLogger.log(Level.FINEST," ** NAME: \t"+ notifInfo[i].getName());
		sLogger.log(Level.FINEST,"    DESCR: \t"+ notifInfo[i].getDescription());
	    }
	} else 
             sLogger.log(Level.FINEST," ** No notifications **");
    }
    
    private static boolean _alreadyCalled = false;

    public synchronized void registerConfigMBeans() {
        if(! _alreadyCalled ) {
            _alreadyCalled = true;
            try {
                final MBeanRegistry mr = MBeanRegistryFactory.getAdminMBeanRegistry();   
                mr.instantiateAndRegisterAllConfigMBeans(
                     adminContext.getAdminConfigContext(), 
                     ApplicationServer.getServerContext().getDefaultDomainName());
            }
            catch (Throwable t) {
                 sLogger.log(Level.WARNING, "Error in registering configMBeans", t);
            }
        }
    }
}







