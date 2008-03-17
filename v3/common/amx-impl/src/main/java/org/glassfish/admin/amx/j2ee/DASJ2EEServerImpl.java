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
package org.glassfish.admin.amx.j2ee;

import java.util.Set;
import java.util.Map;

import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.MBeanServerInvocationHandler;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.j2ee.StateManageable;
import static com.sun.appserv.management.j2ee.StateManageable.*;
import com.sun.appserv.management.j2ee.J2EEServer;

import org.glassfish.admin.amx.mbean.Delegate;
import org.glassfish.admin.amx.mbean.DummyDelegate;
import org.glassfish.admin.amx.mbean.QueryMgrImpl;

import com.sun.appserv.management.j2ee.J2EETypes;

import org.glassfish.admin.amx.util.Issues;

/**
	JSR 77 extension representing an Appserver standalone server (non-clustered)

	Server MBean which will reside on DAS
	for enabling state management including start() and stop()
 */
public class DASJ2EEServerImpl
	extends J2EEServerImpl
	implements NotificationListener
{
		public
	DASJ2EEServerImpl( final ObjectName parentObjectName )
	{
		super( "J2EEDomain", parentObjectName, DummyDelegate.INSTANCE );
	}

/*
	static private final Class[]	DOMAIN_STATUS_INTERFACES	= 
		new Class[] { DomainStatusMBean.class };
	
		protected DomainStatusMBean
	getDomainStatus()
	{
		DomainStatusMBean	domainStatus	= null;
		try {
			final MBeanServer	mbeanServer = getMBeanServer();
			final Set<ObjectName>	candidates	= QueryMgrImpl.queryPatternObjectNameSet( 
				mbeanServer, JMXUtil.newObjectNamePattern( 
					"*", DomainStatusMBean.DOMAIN_STATUS_PROPS ) );
			final ObjectName on = GSetUtil.getSingleton( candidates );
			domainStatus = (DomainStatusMBean)MBeanServerInvocationHandler.
				newProxyInstance( mbeanServer, on, DomainStatusMBean.class, false );
		} catch (Exception e) {
			final Throwable rootCause = ExceptionUtil.getRootCause( e );
			getMBeanLogger().warning( rootCause.toString() + "\n" +
				ExceptionUtil.getStackTrace( rootCause ) );
		}
		return( domainStatus );
	}
	
*/

		private boolean
	remoteServerIsRunning()
	{
		return (STATE_RUNNING == getstate());
	}

		private boolean
	remoteServerIsStartable()
	{
		final int cState = getstate();

		return  (STATE_STOPPED == cState) ||
                (STATE_FAILED == cState);
	}
	
		private boolean
	remoteServerIsStoppable()
	{
		int cState = getstate();

		if ((STATE_STARTING == cState) ||
		    (STATE_RUNNING == cState) ||
		    (STATE_FAILED == cState)) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
		public void
	handleNotification( final Notification notif , final Object ignore)
	{
		final String	notifType	= notif.getType();
		
/*
		if ( notifType.equals( DomainStatusMBean.SERVER_STATUS_NOTIFICATION_TYPE ) )
		{
			final String serverName = (String)
			    Util.getAMXNotificationValue( notif, DomainStatusMBean.SERVER_NAME_KEY );
			
//System.out.println( "########## DASJ2EEServerImpl.handleNotification: serverName = " + serverName  + ", state = " + getstate() + ": " + com.sun.appserv.management.util.jmx.stringifier.NotificationStringifier.toString(notif)  );


			if ( serverName.equals( getServerName() ) )
			{
				setDelegate();
			}
		}
*/
	}

	
		private synchronized void
	setDelegate()
	{
/*
		if ( remoteServerIsRunning() )
		{
//System.out.println( "########## DASJ2EEServerImpl.setDelegate(): remoteServerIsRunning=true");
		    try {
                // get the object name for the old jsr77 server mBean
                com.sun.enterprise.ManagementObjectManager mgmtObjManager =
                    com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();

                final String strON = mgmtObjManager.getServerBaseON(false, getServerName());
                
                final MBeanServerConnection remoteConn	=
                    getDomainStatus().getServerMBeanServerConnection( getServerName() );
                    
                final ObjectName onPattern = new ObjectName(strON + ",*");

                final Set<ObjectName> names = JMXUtil.queryNames( remoteConn, onPattern, null);

                assert( names.size() == 1 );

                final ObjectName serverON = GSetUtil.getSingleton( names );
                final Delegate	delegate = new DelegateToMBeanDelegate( remoteConn, serverON );
                
                setDelegate( delegate );
                setstartTime(System.currentTimeMillis());
//System.out.println( "########## DASJ2EEServerImpl.setDelegate(): set delegate to " + serverON);
		    }
            catch (Exception e) {
                final Throwable rootCause = ExceptionUtil.getRootCause( e );
                getMBeanLogger().warning(
                    rootCause.toString() + "\n" + ExceptionUtil.getStackTrace( rootCause ) );
                if ( getDelegate() == null )
                {
                    setDelegate( DummyDelegate.INSTANCE );
                    setstartTime(0);
                }
            }
		}
		else
		{
//System.out.println( "########## DASJ2EEServerImpl.setDelegate(): setting DummyDelegate" );
			setDelegate( DummyDelegate.INSTANCE );
			setstartTime(0);
		}
*/
	}
	
		public void
    preRegisterDone()
        throws Exception
	{
		super.preRegisterDone( );
		
		setstartTime( 0 );
		setDelegate();
	}
	
		protected String
	getServerName()
	{
		return( getSelfName() );
	}

		public boolean
	isstateManageable()
	{
		return true;
	}

/*
        final RuntimeStatus
    getRuntimeStatus(final String serverName )
    {
        final MBeanServer mbeanServer = getMBeanServer();
        
        final OldServersMBean oldServers =
            OldConfigProxies.getInstance( mbeanServer ).getOldServersMBean( );
            
        final RuntimeStatus status = oldServers.getRuntimeStatus( serverName );
        
        return status;
    }
*/
    
    /**
        Convert an internal status code to JSR 77 StateManageable state.
     *
        private static int
    serverStatusCodeToStateManageableState( final int statusCode )
    {
        int state = STATE_FAILED;
        switch( statusCode )
        {
            default: throw new IllegalArgumentException( "Uknown status code: " + statusCode );
            
            case Status.kInstanceStartingCode: state = STATE_STARTING; break;
            case Status.kInstanceRunningCode: state = STATE_RUNNING; break;
            case Status.kInstanceStoppingCode: state = STATE_STOPPING; break;
            case Status.kInstanceNotRunningCode: state = STATE_STOPPED; break;
        }
        
		return state;
    }
    */

 		public int
 	getstate()
 	{
        int state   = STATE_STOPPED;
        try
        {
            Issues.getAMXIssues().notDone( "DASJ2EEServerImpl.getRuntimeStatus: getRuntimeStatus" );
            //final int internalStatus = getRuntimeStatus(getServerName()).getStatus().getStatusCode();
            //state = serverStatusCodeToStateManageableState( internalStatus );
            state = STATE_RUNNING;
        }
        catch ( final Exception e )
        {
            // not available, must not be running
        }
                
		return state;
 	}

 		public void
 	start()
 	{
 		if ( remoteServerIsStartable() )
 		{
 			startRemoteServer();
 		}
 		else
 		{
			throw new RuntimeException("server is not in a startable state");
 		}
 	}
 	
 		public void
 	startRecursive()
 	{
 		start();
 	}
    
    
    /** The DAS is always named "server", or so inquiries suggest */
    static final String DAS_SERVER_NAME = "server";
    
    /**
        Does this particular J2EEServer represent the DAS?
     */
        private boolean
    isDASJ2EEServer()
    {
        return DAS_SERVER_NAME.equals( getName() );
    }
        
        public void
    stop()
    {
        if ( isDASJ2EEServer()  )
        {
            getDelegate().invoke( "stop", (Object[])null, (String[])null);
        }
        else if ( remoteServerIsStoppable() )
        {
            stopRemoteServer();
        }
        else
        {
            throw new RuntimeException("server is not in a stoppable state");
        }
    }

    private MBeanInfo  mMBeanInfo = null;
    
		public synchronized MBeanInfo
	getMBeanInfo()
	{
        // compute the MBeanInfo only once!
        if ( mMBeanInfo == null )
        {
            final MBeanInfo	superMBeanInfo	= super.getMBeanInfo();
            mMBeanInfo	= new MBeanInfo(
                superMBeanInfo.getClassName(),
                superMBeanInfo.getDescription(),
                mergeAttributeInfos(superMBeanInfo.getAttributes(), getMBeanAttributeInfo()),
                superMBeanInfo.getConstructors(),
                mergeOperationInfos(superMBeanInfo.getOperations(), getMBeanOperationInfo()),
                superMBeanInfo.getNotifications() );
        }
        return mMBeanInfo;
	}


	// attribute info

		private MBeanAttributeInfo[]
	mergeAttributeInfos(
		MBeanAttributeInfo[] infos1,
		MBeanAttributeInfo[] infos2 )
	{
		final MBeanAttributeInfo[] infos =
			new MBeanAttributeInfo[ infos1.length + infos2.length ];
		
		System.arraycopy( infos1, 0, infos, 0, infos1.length );
		System.arraycopy( infos2, 0, infos, infos1.length, infos2.length );
		
		return( infos );
	}


		private MBeanAttributeInfo[]
	getMBeanAttributeInfo()
	{
		MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[1];
		dAttributes[0] = new MBeanAttributeInfo("state",
                                                "java.lang.Integer",
                                                "server state",
                                                true,
                                                false,
                                                false);
		return dAttributes;
	}


	// operation info

		private MBeanOperationInfo[]
	mergeOperationInfos(
		MBeanOperationInfo[] infos1,
		MBeanOperationInfo[] infos2 )
	{
		final MBeanOperationInfo[] infos =
			new MBeanOperationInfo[ infos1.length + infos2.length ];
		
		System.arraycopy( infos1, 0, infos, 0, infos1.length );
		System.arraycopy( infos2, 0, infos, infos1.length, infos2.length );
		
		return( infos );
	}


		private MBeanOperationInfo[]
	getMBeanOperationInfo()
	{
		MBeanOperationInfo[] dOperations = new MBeanOperationInfo[3];
		dOperations[0] = new MBeanOperationInfo("start",
                                                "start server instance",
                                                null,
                                                "void",
                                                MBeanOperationInfo.ACTION);
		dOperations[1] = new MBeanOperationInfo("stop",
                                                "stop server instance",
                                                null,
                                                "void",
                                                MBeanOperationInfo.ACTION);
		dOperations[2] = new MBeanOperationInfo("startRecursive",
                                                "start server instance",
                                                null,
                                                "void",
                                                MBeanOperationInfo.ACTION);
		return dOperations;
	}


		private void 
	startRemoteServer() 
	{
    Issues.getAMXIssues().notDone( "DASJ2EEServerImpl.startRemoteServer" );
/*
		try {
			// get the object name for servers config mBean
			ObjectName on = DomainStatusHelper.getServersConfigObjectName();

			// invoke start method on servers config mBean

			// get mBean server
			final MBeanServer server = getMBeanServer();

			// form the parameters
			Object[] params = new Object[1];
                	params[0] = getServerName();
                	String[] signature = {"java.lang.String"};

			// invoke the start method
                	server.invoke(on, "startServerInstance", params, signature);
		} catch (javax.management.MalformedObjectNameException mfone) {
                        final Throwable rootCause       = ExceptionUtil.getRootCause( mfone );
                        getMBeanLogger().warning( rootCause.toString() + "\n" +
                                        ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(mfone);
		} catch (javax.management.InstanceNotFoundException infe) {
                        final Throwable rootCause       = ExceptionUtil.getRootCause( infe );
                        getMBeanLogger().warning( rootCause.toString() + "\n" +
                                        ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(infe);
		} catch (javax.management.MBeanException mbe) {
                        final Throwable rootCause       = ExceptionUtil.getRootCause( mbe );
                        getMBeanLogger().warning( rootCause.toString() + "\n" +
                                        ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(mbe);
		} catch (javax.management.ReflectionException rfe) {
                        final Throwable rootCause       = ExceptionUtil.getRootCause( rfe );
                        getMBeanLogger().warning( rootCause.toString() + "\n" +
                                        ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(rfe);
		}
*/
	}


		private void 
	stopRemoteServer() 
	{
    Issues.getAMXIssues().notDone( "DASJ2EEServerImpl.stopRemoteServer" );
/*
		try {
			// get the object name for servers config mBean
			ObjectName on = DomainStatusHelper.getServersConfigObjectName();

			// invoke stop method on servers config mBean

			// get mBean server
			final MBeanServer server = getMBeanServer();

			// form the parameters
			Object[] params = new Object[1];
                	params[0] = getServerName();
                	String[] signature = {"java.lang.String"};

			// invoke the start method
                	server.invoke(on, "stopServerInstance", params, signature);
		} catch (javax.management.MalformedObjectNameException mfone) {
                        final Throwable rootCause       = ExceptionUtil.getRootCause( mfone );
                        getMBeanLogger().warning( rootCause.toString() + "\n" +
                                        ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(mfone);
		} catch (javax.management.InstanceNotFoundException infe) {
			// in case of PE and DAS 
			// it is desit]rable that the instance be not stopped
			// hence the log level is fine
            final Throwable rootCause       = ExceptionUtil.getRootCause( infe );
            getMBeanLogger().fine( rootCause.toString() + "\n" +
                            ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(infe);
		} catch (javax.management.MBeanException mbe) {
            final Throwable rootCause       = ExceptionUtil.getRootCause( mbe );
            getMBeanLogger().warning( rootCause.toString() + "\n" +
                            ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(mbe);
		} catch (javax.management.ReflectionException rfe) {
            final Throwable rootCause       = ExceptionUtil.getRootCause( rfe );
            getMBeanLogger().warning( rootCause.toString() + "\n" +
                            ExceptionUtil.getStackTrace( rootCause ) );
			throw new RuntimeException(rfe);
		}
*/
	}
    
        public boolean
	getRestartRequired() {
        // Notification mechanism is broken (reason unknown).  Just set it explicitly if not
        // already set.
//System.out.println( "########## DASJ2EEServerImpl.getRestartRequired:" + getServerName() );
        if ( getDelegate() == null || getDelegate() == DummyDelegate.INSTANCE )
        {
            setDelegate();
        }
        
        // there might not be a Delegate.  Default to 'true', since a non-running server
        // must be restarted!
        boolean required    = true;
        
        final int state = getstate();
        // ensure that these states always return 'true'
        if ( state == STATE_STOPPED || state == STATE_FAILED || state == STATE_STOPPING )
        {
            required = true;
        }
        else
        {
            try {
                final Object result  = getDelegate().getAttribute( "restartRequired" );
                required    = Boolean.valueOf( "" + result );
            }
            catch( AttributeNotFoundException e ) {
                logWarning( ExceptionUtil.toString(e) );
                required    = true;
            }
        }
        
        return required;
	}
}





