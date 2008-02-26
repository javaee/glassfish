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

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.JMException;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.NotificationEmitterServiceKeys;
import com.sun.appserv.management.base.NotificationEmitterService;

import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import org.glassfish.admin.amx.support.BootUtil;
import org.glassfish.admin.amx.util.ObjectNames;


//import org.glassfish.admin.amx.types.TypeInfo;


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
		
	    final CheckStartedThread    t   = new CheckStartedThread();
	    t.start();
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
                XTypes.NOTIFICATION_EMITTER_SERVICE, NotificationEmitterServiceKeys.DOMAIN_KEY, false );
		mbean	= new NotificationEmitterServiceImpl();
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.NOTIFICATION_SERVICE_MGR, AMX.NO_NAME, false );
		mbean	= new NotificationServiceMgrImpl();
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.QUERY_MGR, AMX.NO_NAME, false );
		mbean	= new QueryMgrImpl();
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.BULK_ACCESS, AMX.NO_NAME, false );
		mbean	= new BulkAccessImpl();
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.UPLOAD_DOWNLOAD_MGR, AMX.NO_NAME, false );
		mbean	= new UploadDownloadMgrImpl();
        registerChild( mbean, childObjectName );
        
        
        childObjectName	= objectNames.buildContaineeObjectName( self, getFullType(),
                XTypes.SAMPLE, AMX.NO_NAME, false );
		mbean	= new SampleImpl();
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
		
		final NotificationEmitterService	domainNES	= new NotificationEmitterServiceImpl();
        registerChild( domainNES, childObjectName );
	}
	
	
	static private final long  AMX_READY_SLEEP_DURATION  = 100;
	/**
	    Notice when AMX has finished loading, the exit.
	 */
	private final class CheckStartedThread extends Thread
	{
	    public void CheckStartedThread()    {}
	    
	        public void
	    run()
	    {
	        waitAMXReady();
            amxNowReady();
	    }
	}
	
	    private void
	amxNowReady()
	{
	    if ( ! getAMXReady() )
	    {
	        throw new IllegalStateException();
	    }
	    sendNotification( DomainRoot.AMX_READY_NOTIFICATION_TYPE );
	}
	
	    public boolean
	getAMXReady()
	{
	    return BootUtil.getInstance().getAMXReady();
	}
	
	    public void
	waitAMXReady( )
	{
        while ( ! getAMXReady() )
        {
            sleepMillis( AMX_READY_SLEEP_DURATION );
        }
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
}












