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
package com.sun.enterprise.admin.server.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;

import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

import com.sun.enterprise.server.ApplicationServer;

//Admin imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/* for checking persistent store */
//import com.sun.enterprise.server.ondemand.entry.*;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.server.core.jmx.InitException;

import com.sun.enterprise.util.FeatureAvailability;
import com.sun.appserv.management.util.misc.RunnableBase;


/**
    Loads the com.sun.appserv configuration MBeans in a separate thread.
*/
final class ComSunAppservConfigMBeansIniter extends RunnableBase<String> {
    private static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);

    private final MBeanServer mServer;
    private final String      mJMXDomainName;
    private final AdminContext  mAdminContext;
    
    public ComSunAppservConfigMBeansIniter(final String jmxDomainName, final MBeanServer server, final AdminContext adminContext )
    {
        super( "ComSunAppservConfigMBeansIniter" );
        mServer         = server;
        mJMXDomainName  = jmxDomainName;
        mAdminContext    = adminContext;
        
        checkAlreadyLoaded();
    }
    
        private void
    checkAlreadyLoaded()
    {
        if ( mServer.isRegistered( ObjectNames.getControllerObjectName() ) )
        {
            throw new IllegalArgumentException();
        }
    }
    
        protected void
    doRun() throws Exception
    {
        checkAlreadyLoaded();
        
        initialize();
    }
    
        protected void
    startLoading( final boolean synchronous )
    {
        checkAlreadyLoaded();
        
        if ( synchronous )
        {
            run();
        }
        else
        {
            new Thread( this ).start();
        }
    }
    
    /**
        Initializes the MBeanServer. This method registers the System MBeans.
        The System MBeans are assumed to have default constructor.
       
        @throws InitException if any of the System MBeans can't be initialized.
    */

    private void initialize( )
        throws InitException {
        try {
            final ObjectName controllerObjectName = ObjectNames.getControllerObjectName();
            final ObjectName configObjectName     = ObjectNames.getGenericConfiguratorObjectName();
            final ObjectName[] objectNames        = { controllerObjectName, configObjectName };
            
            final String controllerClassName = 
                "com.sun.enterprise.admin.server.core.mbean.config.ServerController";
            final String configClassName = 
                "com.sun.enterprise.admin.server.core.mbean.config.GenericConfigurator";
                
            final String[] clNames = {controllerClassName, configClassName};
        
            for (int i = 0 ; i < clNames.length ; i++) {
                createAndRegister( clNames[i], objectNames[ i ] );
            }
            registerDottedNameSupport();
            registerConfigMBeans();
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "Error in initialize", e);
            throw new InitException(e.getMessage(), e );
        }
    }
    
	//  build dependencies force this form of instantiation
	static private final String DottedMBeansIniterClassName	= 
			"com.sun.enterprise.admin.mbeans.DottedNameMBeansIniter";
			
		private void
	registerDottedNameSupport()
		throws Exception
	{
		final Class		initerClass	= Class.forName( DottedMBeansIniterClassName );
		
		// invoke new DottedNamesMBeanIniter( MBeanServer m )
		final Class []		signature	= new Class [] { MBeanServer.class };
		final java.lang.reflect.Constructor	constructor	= initerClass.getConstructor( signature );
		constructor.newInstance( new Object [] { mServer } );
		// done--it will have done its job
	}
	
	private ObjectInstance createAndRegister( final String className, final ObjectName objectName )
        throws  InstanceAlreadyExistsException,
                MBeanRegistrationException,
                NotCompliantMBeanException,
                ClassNotFoundException,
                InstantiationException,
                IllegalAccessException
	{
        final Class  mbeanClass				= Class.forName( className );
        final Object mbeanImpl				= mbeanClass.newInstance();
        final ObjectInstance mbeanInstance	= this.registerMBean( mbeanImpl, objectName );
        
        return( mbeanInstance );
	}
	
    public ObjectInstance registerMBean( final Object object, final ObjectName objectName) 
        throws  InstanceAlreadyExistsException,
                MBeanRegistrationException,
                NotCompliantMBeanException {
        return mServer.registerMBean(object, objectName);
    }

    /**
     * OLD, possibly invalid comments below
     * NOTE: I am using runtime configcontext to instantiate configmbeans
     *
     * FIXME: The current implementation does not load all the mbeans
     * while creating elements. Hence, some mbeans may never be
     * loaded. This needs to be fixed.
     *
     * FIXME: Eventual plan is to move this method to AdminService where
     * it is initialized once and there is no need to call again.
     *
     * This method does not throw any exception and is best-effort.
     */
    private void registerConfigMBeans()
        throws com.sun.enterprise.admin.meta.MBeanMetaException
    {
        final long start = System.currentTimeMillis();
        
        final MBeanRegistry mr = MBeanRegistryFactory.getAdminMBeanRegistry();  
        final ConfigContext  configContext    = mAdminContext.getAdminConfigContext();
        final String jmxDomainName = ApplicationServer.getServerContext().getDefaultDomainName();
        
        mr.instantiateAndRegisterAllConfigMBeans(
             configContext, 
             jmxDomainName);
        
        final long elapsed = System.currentTimeMillis() - start;
        
        FeatureAvailability.getInstance().registerFeature( FeatureAvailability.COM_SUN_APPSERV_CONFIG_MBEANS_FEATURE, mServer );
    }
}






