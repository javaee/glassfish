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
package org.glassfish.admin.amx.impl.mbean;

import org.glassfish.admin.amx.base.*;

import org.glassfish.admin.amx.util.FeatureAvailability;
import com.sun.appserv.server.util.Version;

import org.glassfish.admin.amx.impl.loader.BootUtil;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import java.io.IOException;
import java.io.InputStreamReader;

import com.sun.enterprise.universal.io.SmartFile;

import com.sun.enterprise.universal.Duration;

import org.glassfish.server.ServerEnvironmentImpl;

import org.glassfish.admin.amx.impl.util.InjectedValues;

import java.io.InputStream;
import org.glassfish.admin.amx.impl.mbean.PathnamesImpl;
import org.glassfish.admin.amx.monitoring.MonitoringRoot;


/**
 */
public class DomainRootImpl extends AMXImplBase
	// implements DomainRoot
{
    private String	mAppserverDomainName;
    private volatile ComplianceMonitor  mCompliance;

        public
    DomainRootImpl()
    {
        super( null, DomainRoot.class );
        mAppserverDomainName	= null;
    }

    public void stopDomain()
    {
        getDomainRootProxy().getExt().getRuntimeMgr().stopDomain();
    }

    
    public ObjectName getQueryMgr()
    {
        return child(QueryMgr.class);
    }
    
    public ObjectName getUploadDownloadMgr()
    {
        return child(UploadDownloadMgr.class);
    }
    
    public ObjectName getPathnames()
    {
        return child(Pathnames.class);
    }
    
    public ObjectName getSystemStatus()
    {
        return child(SystemStatus.class);
    }
    
    public ObjectName getBulkAccess()
    {
        return child(BulkAccess.class);
    }
    
    
        public ObjectName
    preRegisterHook( final ObjectName selfObjectName )
        throws Exception
    {
        mAppserverDomainName	= BootUtil.getInstance().getAppserverDomainName();

        return selfObjectName;
    }


        public void
    preRegisterDone( )
        throws Exception
    {
        super.preRegisterDone();
    }
    
    @Override
		protected void
	postRegisterHook( final Boolean registrationSucceeded )
	{
        final boolean registeredOK = registrationSucceeded.booleanValue() ;
	    if ( registeredOK )
		{
            // start it listening
            mCompliance = ComplianceMonitor.getInstance( getDomainRootProxy() );
		}
        super.postRegisterHook(registrationSucceeded);
        
	    if ( registeredOK )
		{
            // validate ourself
            mCompliance.validate( getObjectName() );
            mCompliance.start();
		}
	}

        public String
    getAppserverDomainName()
    {
        return( mAppserverDomainName );
    }

    @Override
        protected final void
    registerChildren()
    {
        super.registerChildren();
        
        final ObjectName    self = getObjectName();
        final ObjectNameBuilder	objectNames	= new ObjectNameBuilder( getMBeanServer(), self );
        
        ObjectName childObjectName = null;
        Object mbean = null;
        final MBeanServer server = getMBeanServer();
                
        childObjectName	= objectNames.buildChildObjectName( Ext.class );
        mbean	= new ExtImpl(self);
        registerChild( mbean, childObjectName );
                        
        childObjectName	= objectNames.buildChildObjectName( Tools.class );
        mbean	= new ToolsImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildChildObjectName( QueryMgr.class);
        mbean	= new QueryMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildChildObjectName( BulkAccess.class);
        mbean	= new BulkAccessImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildChildObjectName( UploadDownloadMgr.class);
        mbean	= new UploadDownloadMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildChildObjectName( Sample.class);
        mbean	= new SampleImpl(self);
        registerChild( mbean, childObjectName );
             
        childObjectName	= objectNames.buildChildObjectName( Pathnames.class);
        mbean	= new PathnamesImpl(self);
        registerChild( mbean, childObjectName );

        childObjectName	= objectNames.buildChildObjectName( MonitoringRoot.class);
        mbean	= new MonitoringRootImpl(self);
        registerChild( mbean, childObjectName );
    }
    
    /*
    public Object loadInternal()
    {
        try
        {
            final ObjectName objectName = LoadSanityChecks.load(getMBeanServer());
            return "Loaded: " + objectName;
        }
        catch( final Throwable t )
        {
            t.printStackTrace();
            throw new RuntimeException("Failed to load SanityChecks: " + t, t);
        }
    }
    */

            
        public boolean
    getAMXReady()
    {
        // just block until ready, no need to support polling
        waitAMXReady();
        return true;
    }

        public void
    waitAMXReady( )
    {
        FeatureAvailability.getInstance().waitForFeature( FeatureAvailability.AMX_READY_FEATURE, this.getClass().getName() );
    }


    /*
    static private final Set<String>  OFFLINE_INCAPABLE_J2EE_TYPES =
        SetUtil.newUnmodifiableStringSet(
            XTypes.WEB_SERVICE_MGR
        );
        protected boolean
    isOfflineCapable( final TypeInfo childInfo )
    {
        final String    j2eeType    = childInfo.getJ2EEType();
        
        return (! OFFLINE_INCAPABLE_J2EE_TYPES.contains( j2eeType )) &&
                super.isOfflineCapable( childInfo );
    }

        protected void
    registerSelfMgrChild( final TypeInfo	childInfo )
        throws JMException, InstantiationException, IllegalAccessException
    {
        final String	childJ2EEType	= childInfo.getJ2EEType( );
        
        if ( getOffline() &&
                childJ2EEType.equals( XTypes.CONFIG_DOTTED_NAMES ) )
        {
            final OfflineConfigDottedNamesImpl   impl   = new OfflineConfigDottedNamesImpl();
            
            final ObjectName	childObjectName	=
                getObjectNames().buildChildObjectName( getObjectName(),
                    getFullType(), childJ2EEType );
                    
            registerMBean( impl, childObjectName );
        }
        else
        {
            super.registerSelfMgrChild( childInfo );
        }
    }
    */

    public String getDebugPort()
    {
        Issues.getAMXIssues().notDone( "DomainRootImpl.getDebugPort" );
        return "" + 9999;
    }


    public String getApplicationServerFullVersion()
    {
        return Version.getFullVersion();
    }


    public String getInstanceRoot()
    {
        return SmartFile.sanitize( "" + System.getProperty("com.sun.aas.instanceRoot")) ;
    }

    public String getDomainDir()
    {
        return SmartFile.sanitize( BootUtil.getInstance().getInstanceRoot().toString() );
    }

    public String getConfigDir()
    {
        return getDomainDir() + "/" + "config";
    }

    public String getInstallDir()
    {
        return SmartFile.sanitize( "" + System.getProperty("com.sun.aas.installRoot")) ;
    }

        public Object[]
    getUptimeMillis()
    {
        final ServerEnvironmentImpl env = InjectedValues.getInstance().getServerEnvironment();
        
        final long elapsed = System.currentTimeMillis() - env.getStartupContext().getCreationTime();
        final Duration duration = new Duration(elapsed);
        
        return new Object[] { elapsed, duration.toString() };
    }

    static String toString(final InputStream is)
         throws IOException
    {
       final StringBuffer sbuf = new StringBuffer();
       final char[] chars = new char[32 * 1024];

       final InputStreamReader reader = new InputStreamReader(is);
       do
       {
           final int len = reader.read(chars, 0, chars.length);
           if (len >= 1)
           {
               sbuf.append(chars, 0, len);
           }
       }
       while(reader.ready());

       return sbuf.toString();
    }
}












