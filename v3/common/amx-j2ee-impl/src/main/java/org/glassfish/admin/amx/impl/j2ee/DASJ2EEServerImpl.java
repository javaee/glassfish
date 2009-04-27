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
package org.glassfish.admin.amx.impl.j2ee;

import java.lang.reflect.Constructor;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.j2ee.*;
import static org.glassfish.admin.amx.j2ee.StateManageable.*;

/**
JSR 77 extension representing an Appserver standalone server (non-clustered)

Server MBean which will reside on DAS
for enabling state management including start() and stop()
 */
public class DASJ2EEServerImpl extends J2EEServerImpl
        implements NotificationListener {

    public DASJ2EEServerImpl(final ObjectName parentObjectName) {
        super(parentObjectName);

        Issues.getAMXIssues().notDone("DASJ2EEServer needs to account for DAS/non-DAS");
    }

        protected <I extends J2EEManagedObject, C extends J2EEManagedObjectImplBase> ObjectName
    registerDummy(
        final ObjectName parent,
        final Class<I> intf,
        final Class<C>  clazz,
        final String    name)
    {
        ObjectName on = null;
        try
        {
            final Constructor<C> c = clazz.getConstructor(ObjectName.class);
            final J2EEManagedObjectImplBase impl = c.newInstance(parent);
            final String j2eeType = (String)intf.getField("J2EE_TYPE").get(c);
            on = new ObjectNameBuilder( getMBeanServer(), parent).buildChildObjectName( j2eeType, name);
            on = registerChild( impl, on);
        }
        catch( final Exception e )
        {
            e.printStackTrace();
        }

        return on;
    }
    
    private static final boolean DUMMY_HIERARCHY = false;
    
    void registerDummyHierarchy()
    {
        cdebug( "REGISTERING VARIOUS *DUMMY* MBeans in J2EEServer " + getObjectName() );
        Object impl;
        ObjectName on = null;

        final ObjectName self = getObjectName();
        registerDummy( self, JavaMailResource.class, JavaMailResourceImpl.class, "javamail1");
        registerDummy( self, RMI_IIOPResource.class, RMI_IIOPResourceImpl.class, "rmiiiop1");
        registerDummy( self, JMSResource.class, JMSResourceImpl.class, "jms1");
        registerDummy( self, URLResource.class, URLResourceImpl.class, "url1");
        registerDummy( self, JDBCResource.class, JDBCResourceImpl.class, "jdbc1");
        registerDummy( self, JTAResource.class, JTAResourceImpl.class, "jta1");
        registerDummy( self, JNDIResource.class, JNDIResourceImpl.class, "jndi1");
        registerDummy( self, JDBCDriver.class, JDBCDriverImpl.class, "jdbcdriver1");
        registerDummy( self, JCAManagedConnectionFactory.class, JCAManagedConnectionFactoryImpl.class, "jcamcf1");
        

        final ObjectName resam = registerDummy( self, ResourceAdapterModule.class, ResourceAdapterModuleImpl.class, "ra1");
        final ObjectName jca = registerDummy( resam, JCAResource.class, JCAResourceImpl.class, "jca1");
        registerDummy( jca, JCAConnectionFactory.class, JCAConnectionFactoryImpl.class, "jcacf1");
        
        final ObjectName appclient = registerDummy( self, AppClientModule.class, AppClientModuleImpl.class, "appclient1");
        
        final ObjectName wms = registerDummy( self, WebModule.class, WebModuleImpl.class, "wm1");
        registerDummy( wms, Servlet.class, ServletImpl.class, "servlet1");
        registerDummy( wms, Servlet.class, ServletImpl.class, "servlet2");

        final ObjectName ejbms = registerDummy( self, EJBModule.class, EJBModuleImpl.class, "ejbm1");
        registerDummy( ejbms, StatelessSessionBean.class, StatelessSessionBeanImpl.class, "slsb1");
        registerDummy( ejbms, StatefulSessionBean.class, StatefulSessionBeanImpl.class, "sfsb1");
        registerDummy( ejbms, EntityBean.class, EntityBeanImpl.class, "eb1");
        registerDummy( ejbms, MessageDrivenBean.class, MessageDrivenBeanImpl.class, "mdb1");
        
        final ObjectName app = registerDummy( self, J2EEApplication.class, J2EEApplicationImpl.class, "test-app");
        
        final ObjectName wminapp = registerDummy( app, WebModule.class, WebModuleImpl.class, "wm-in-app");
        registerDummy( wminapp, Servlet.class, ServletImpl.class, "appservlet1");
        registerDummy( wminapp, Servlet.class, ServletImpl.class, "appservlet2");

        final ObjectName ejbmapp = registerDummy( app, EJBModule.class, EJBModuleImpl.class, "ejbm-in-app");
        registerDummy( ejbmapp, StatelessSessionBean.class, StatelessSessionBeanImpl.class, "app-slsb1");
        registerDummy( ejbmapp, StatefulSessionBean.class, StatefulSessionBeanImpl.class, "app-sfsb1");
        registerDummy( ejbmapp, EntityBean.class, EntityBeanImpl.class, "app-eb1");
        registerDummy( ejbmapp, MessageDrivenBean.class, MessageDrivenBeanImpl.class, "app-mdb1");
    }
    

    @Override
        protected void
    registerChildren()
    {
        final ObjectNameBuilder builder = getObjectNames();

        final JVMImpl jvm = new JVMImpl( getObjectName() );
        final ObjectName jvmObjectName = builder.buildChildObjectName( JVM.J2EE_TYPE, null);
        registerChild( jvm, jvmObjectName );
        
        if ( DUMMY_HIERARCHY )
        {
            registerDummyHierarchy();
        }
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
    final ObjectName on = SetUtil.getSingleton( candidates );
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
    private boolean remoteServerIsRunning() {
        return (STATE_RUNNING == getstate());
    }

    private boolean remoteServerIsStartable() {
        final int cState = getstate();

        return (STATE_STOPPED == cState) ||
                (STATE_FAILED == cState);
    }

    private boolean remoteServerIsStoppable() {
        int cState = getstate();

        if ((STATE_STARTING == cState) ||
                (STATE_RUNNING == cState) ||
                (STATE_FAILED == cState)) {
            return true;
        } else {
            return false;
        }
    }

    public void handleNotification(final Notification notif, final Object ignore) {
        final String notifType = notif.getType();

    }

    protected String getServerName() {
        return Util.getNameProp(getObjectName());
    }

    public boolean isstateManageable() {
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
    public int getstate() {
        int state = STATE_STOPPED;
        try {
            Issues.getAMXIssues().notDone("DASJ2EEServerImpl.getRuntimeStatus: getRuntimeStatus");
            //final int internalStatus = getRuntimeStatus(getServerName()).getStatus().getStatusCode();
            //state = serverStatusCodeToStateManageableState( internalStatus );
            state = STATE_RUNNING;
        } catch (final Exception e) {
            // not available, must not be running
        }

        return state;
    }

    public void start() {
        if (remoteServerIsStartable()) {
            startRemoteServer();
        } else {
            throw new RuntimeException("server is not in a startable state");
        }
    }

    public void startRecursive() {
        start();
    }
    /** The DAS is always named "server", or so inquiries suggest */
    static final String DAS_SERVER_NAME = "server";

    /**
    Does this particular J2EEServer represent the DAS?
     */
    private boolean isDASJ2EEServer() {
        return DAS_SERVER_NAME.equals(getName());
    }

    public void stop() {
        if (isDASJ2EEServer()) {
            //getDelegate().invoke( "stop", (Object[])null, (String[])null);
        } else if (remoteServerIsStoppable()) {
            //stopRemoteServer();
        } else {
            throw new RuntimeException("server is not in a stoppable state");
        }
    }

    private void startRemoteServer() {
        Issues.getAMXIssues().notDone("DASJ2EEServerImpl.startRemoteServer");
    }

    private void stopRemoteServer() {
        Issues.getAMXIssues().notDone("DASJ2EEServerImpl.stopRemoteServer");
    }
}





