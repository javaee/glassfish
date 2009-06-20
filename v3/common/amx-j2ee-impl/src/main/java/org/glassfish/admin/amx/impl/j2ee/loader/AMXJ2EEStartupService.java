/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.amx.impl.j2ee.loader;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.config.AMXConfigConstants;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.impl.j2ee.J2EEDomainImpl;
import org.glassfish.admin.amx.impl.j2ee.Metadata;
import org.glassfish.admin.amx.impl.j2ee.MetadataImpl;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.j2ee.J2EEDomain;
import org.glassfish.admin.amx.j2ee.J2EETypes;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.internal.data.ApplicationRegistry;


/**
    Startup service that loads support for AMX config MBeans.  How this is to be
    triggered is not yet clear.
 */
@Service
public final class AMXJ2EEStartupService
    implements  org.jvnet.hk2.component.PostConstruct,
                org.jvnet.hk2.component.PreDestroy,
                AMXJ2EEStartupServiceMBean
{
    private static void debug( final String s ) { System.out.println(s); }
    
    @Inject
    private MBeanServer mMBeanServer;
    
    @Inject
    InjectedValues mCore;
    public InjectedValues getCore() { return mCore; }

    @Inject
    private ApplicationRegistry mAppsRegistry;
    public ApplicationRegistry getApplicationRegistry() { return mAppsRegistry; }

    
    public AMXJ2EEStartupService()
    {
        //debug( "AMXStartupService.AMXStartupService()" );
    }
    
    public void postConstruct()
    {
        try
        {
        /*
           final StandardMBean mbean = new StandardMBean(this, AMXJ2EEStartupServiceMBean.class);
           mMBeanServer.registerMBean( mbean, OBJECT_NAME );
           */
           mMBeanServer.registerMBean( this, OBJECT_NAME );
        }
        catch( JMException e )
        {
            throw new Error(e);
        }
       //debug( "AMXJ2EEStartupService.postConstruct(): registered: " + OBJECT_NAME);
    }

    public void preDestroy() {
        //ImplUtil.getLogger().info( "AMXConfigStartupService.preDestroy(): stopping AMX" );
        unloadAMXMBeans();
    }
    
    private DomainRoot getDomainRootProxy()
    {
        return ProxyFactory.getInstance( mMBeanServer ).getDomainRootProxy();
    }
    
        public ObjectName
    getJ2EEDomain()
    {
        return getDomainRootProxy().child(J2EETypes.J2EE_DOMAIN).extra().objectName();
    }
    
        private J2EEDomain
    getJ2EEDomainProxy()
    {
        return ProxyFactory.getInstance( mMBeanServer ).getProxy(getJ2EEDomain(), J2EEDomain.class);
    }
    
        public synchronized ObjectName
    loadAMXMBeans()
    {
        FeatureAvailability.getInstance().waitForFeature( FeatureAvailability.AMX_CORE_READY_FEATURE, "" + this );
        FeatureAvailability.getInstance().waitForFeature( AMXConfigConstants.AMX_CONFIG_READY_FEATURE, "" + this );
        
        final DomainRoot domainRootProxy = ProxyFactory.getInstance( mMBeanServer ).getDomainRootProxy(false);
        final ObjectName domainRoot = domainRootProxy.objectName();
        final ObjectNameBuilder objectNames = new ObjectNameBuilder(mMBeanServer, domainRoot);
        final String domainName = Util.getNameProp(domainRoot);

        final Metadata metadata = new MetadataImpl();
        metadata.add( Metadata.CORRESPONDING_CONFIG, domainRootProxy.child(Domain.class).objectName());
        
        final J2EEDomainImpl impl = new J2EEDomainImpl(domainRoot, metadata);
        ObjectName objectName = objectNames.buildChildObjectName( J2EEDomain.class );
        try
        {
            objectName = mMBeanServer.registerMBean( impl, objectName ).getObjectName();
        }
        catch( JMException e )
        {
            throw new Error(e);
        }

        ImplUtil.getLogger().info( "J2EEDomain registered at " + objectName );
        return objectName;
    }
    
    public synchronized void unloadAMXMBeans()
    {
        final J2EEDomain j2eeDomain = getJ2EEDomainProxy();
        if ( j2eeDomain!= null )
        {
            ImplUtil.unregisterAMXMBeans( j2eeDomain );
        }
    }
}















