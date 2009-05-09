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
package org.glassfish.admin.amx.mbean;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.NotificationEmitterService;
import com.sun.appserv.management.base.NotificationEmitterServiceKeys;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Pathnames;
import com.sun.appserv.management.base.LoggingPropertiesMgr;
import com.sun.appserv.management.ext.realm.RealmsMgr;
import com.sun.appserv.management.ext.runtime.RuntimeMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.server.util.Version;
import org.glassfish.admin.amx.dotted.PathnamesImpl;
import org.glassfish.admin.amx.j2ee.DASJ2EEDomainImpl;
import org.glassfish.admin.amx.loader.BootUtil;
import com.sun.appserv.management.util.misc.FeatureAvailability;
import org.glassfish.admin.amx.util.Issues;
import org.glassfish.admin.amx.util.ObjectNames;

import org.glassfish.admin.amx.internal.SanityChecksMBean;
import org.glassfish.admin.amx.internal.LoadSanityChecks;

import javax.management.ObjectName;
import java.util.Set;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sun.enterprise.universal.io.SmartFile;

import com.sun.enterprise.universal.Duration;

import org.glassfish.server.ServerEnvironmentImpl;

import org.glassfish.admin.amx.util.InjectedValues;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;

import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
 */
public class DomainRootImplBase extends AMXNonConfigImplBase
	// implements DomainRoot
{
    private String	mAppserverDomainName;

        public String
    getGroup()
    {
        return( AMX.GROUP_UTILITY );
    }

        public
    DomainRootImplBase()
    {
        super( DomainRoot.J2EE_TYPE, DomainRoot.J2EE_TYPE, null, DomainRoot.class, null );
        mAppserverDomainName	= null;
    }

    @Override
        protected String
    _getPathnameType()
    {
        return "root";
    }

    @Override
        public String
    _getPathnameName()
    {
        return null;
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

    private static final Set<String> NOT_SUPERFLUOUS =
        GSetUtil.newUnmodifiableStringSet(
            "getDomainNotificationEmitterServiceObjectName" );
        protected final Set<String>
    getNotSuperfluousMethods()
    {
        return GSetUtil.newSet( super.getNotSuperfluousMethods(), NOT_SUPERFLUOUS );
    }


        public ObjectName
    getDomainNotificationEmitterServiceObjectName()
    {
        return( getContainerSupport().getContaineeObjectName( XTypes.NOTIFICATION_EMITTER_SERVICE,
            NotificationEmitterServiceKeys.DOMAIN_KEY ) );
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
        final ObjectNames	objectNames	= ObjectNames.getInstance( getJMXDomain() );
        
        ObjectName childObjectName = null;
        AMX mbean = null;
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                LoggingPropertiesMgr.J2EE_TYPE, AMX.NO_NAME, false );
        mbean	= new LoggingPropertiesMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.SYSTEM_STATUS, AMX.NO_NAME, false );
        mbean	= new SystemStatusImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.KITCHEN_SINK, AMX.NO_NAME, false );
        mbean	= new KitchenSinkImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.NOTIFICATION_EMITTER_SERVICE, NotificationEmitterServiceKeys.DOMAIN_KEY, false );
        mbean	= new NotificationEmitterServiceImpl(self);
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.NOTIFICATION_SERVICE_MGR, AMX.NO_NAME, false );
        mbean	= new NotificationServiceMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.QUERY_MGR, AMX.NO_NAME, false );
        mbean	= new QueryMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.BULK_ACCESS, AMX.NO_NAME, false );
        mbean	= new BulkAccessImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.UPLOAD_DOWNLOAD_MGR, AMX.NO_NAME, false );
        mbean	= new UploadDownloadMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.SAMPLE, AMX.NO_NAME, false );
        mbean	= new SampleImpl(self);
        registerChild( mbean, childObjectName );
        
        final String j2eeDomainName = getObjectName().getDomain();
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                J2EEDomain.J2EE_TYPE, j2eeDomainName, false );
        mbean	= new DASJ2EEDomainImpl( self );
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                Pathnames.J2EE_TYPE, AMX.NO_NAME, false );
        mbean	= new PathnamesImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                RealmsMgr.J2EE_TYPE, AMX.NO_NAME, false );
        mbean	= new RealmsMgrImpl(self);
        registerChild( mbean, childObjectName );
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                RuntimeMgr.J2EE_TYPE, AMX.NO_NAME, false );
        mbean	= new RuntimeMgrImpl(self);
        registerChild( mbean, childObjectName );
    }

        protected final void
    registerNotificationMgrService()
    {
        final ObjectNames	objectNames	= ObjectNames.getInstance( getJMXDomain() );
        final ObjectName	childObjectName	=
            objectNames.buildContaineeObjectName( getObjectName(),
                getFullType(), XTypes.NOTIFICATION_EMITTER_SERVICE,
                    NotificationEmitterServiceKeys.DOMAIN_KEY, false );
        
        final NotificationEmitterService	domainNES	= new NotificationEmitterServiceImpl(getObjectName());
        registerChild( domainNES, childObjectName );
    }
    
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

    static private final Set<String>  OFFLINE_INCAPABLE_J2EE_TYPES =
        GSetUtil.newUnmodifiableStringSet(
            XTypes.WEB_SERVICE_MGR
        );

    /*
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
                getObjectNames().buildContaineeObjectName( getObjectName(),
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

    public Object stopDomain()
    {
        // as this module is going away, just hard code to use AMX.new
        final ObjectName root = JMXUtil.newObjectName( "v3:pp=,type=DomainRoot,name=v3" );
        try
        {
            return getMBeanServer().invoke( root, "stopDomain", null, null);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
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

    private HTTPListenerConfig getAdminHttpListener()
    {
        final ConfigConfig config = getSelf(DomainRoot.class).getDomainConfig().getConfigsConfig().getConfigConfigMap().get("server-config");
        return config.getHTTPServiceConfig().getHTTPListenerConfigMap().get("admin-listener");
    }

    private int getRESTPort()
    {
        return getAdminHttpListener().resolveInteger("Port");
    }

    private String get_asadmin()
    {
        return getAdminHttpListener().resolveAttribute("DefaultVirtualServer");
    }

    public String getRESTBaseURL()
    {
        final String scheme = getAdminHttpListener().resolveBoolean("SecurityEnabled") ? "https" : "http";
        final String host = "localhost";
        
        return scheme + "://" + host + ":" + getRESTPort() + "/" + get_asadmin() + "/";
    }

    public String executeREST(final String cmd)
    {
        if ( DomainRoot.STOP_DOMAIN.equals(cmd) )
        {
            return "" + stopDomain();
        }
        
        String result = null;
        
        HttpURLConnection conn = null;
        try {
            final String url = getRESTBaseURL() + cmd;
            
            final URL invoke = new URL(url);
            //System.out.println( "Opening connection to: " + invoke );
            conn = (HttpURLConnection)invoke.openConnection();
            
            final InputStream is = conn.getInputStream();
            result = toString(is);
            is.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            result = ExceptionUtil.toString(e);
        }
        finally
        {
            if ( conn != null )
            {
                conn.disconnect();
            }
        }
        return result;
    }
}












