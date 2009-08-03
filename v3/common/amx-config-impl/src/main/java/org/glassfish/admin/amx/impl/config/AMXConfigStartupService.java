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
package org.glassfish.admin.amx.impl.config;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.TimingDelta;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;


import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.glassfish.admin.amx.config.AMXConfigConstants;
import org.glassfish.admin.amx.impl.config.AMXConfigLoader;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;

import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.mbeanserver.PendingConfigBeans;
import org.jvnet.hk2.config.Transactions;

/**
    Startup service that loads support for AMX config MBeans.  How this is to be
    triggered is not yet clear.
 */
@Taxonomy( stability=Stability.NOT_AN_INTERFACE )
@Service
public final class AMXConfigStartupService
    implements  org.jvnet.hk2.component.PostConstruct,
                org.jvnet.hk2.component.PreDestroy,
                AMXConfigStartupServiceMBean
{
    private static void debug( final String s ) { System.out.println(s); }
    
    @Inject
    InjectedValues  mInjectedValues;
    
    @Inject//(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
    private MBeanServer mMBeanServer;
    
    @Inject
    private volatile PendingConfigBeans mPendingConfigBeans;
    
    @Inject
    private Transactions mTransactions;
    
    private volatile AMXConfigLoader mLoader;
    
    public AMXConfigStartupService()
    {
        //debug( "AMXStartupService.AMXStartupService()" );
    }
    
    public void postConstruct()
    {
        final TimingDelta delta = new TimingDelta();
        
        if ( mMBeanServer == null ) throw new Error( "AMXStartup: null MBeanServer" );
        if ( mPendingConfigBeans == null ) throw new Error( "AMXStartup: null mPendingConfigBeans" );
        
        try
        {
           final StandardMBean mbean = new StandardMBean(this, AMXConfigStartupServiceMBean.class);
           mMBeanServer.registerMBean( mbean, OBJECT_NAME );
        }
        catch( JMException e )
        {
            throw new Error(e);
        }
        //debug( "AMXStartupService.postConstruct(): registered: " + OBJECT_NAME);
        ImplUtil.getLogger().fine( "Initialized AMXConfig Startup service in " + delta.elapsedMillis() + " ms, registered as " + OBJECT_NAME );
    }

    public void preDestroy() {
        ImplUtil.getLogger().info( "AMXConfigStartupService.preDestroy(): stopping AMX" );
        unloadAMXMBeans();
    }
    
    public DomainRoot getDomainRoot()
    {
        return ProxyFactory.getInstance( mMBeanServer ).getDomainRootProxy(false);
    }
    
        public ObjectName
    getDomainConfig()
    {
        return getDomainRoot().child(Domain.class).extra().objectName();
    }
    
        public Domain
    getDomainConfigProxy()
    {
        return ProxyFactory.getInstance( mMBeanServer ).getProxy(getDomainConfig(), Domain.class);
    }
    
        public synchronized ObjectName
    loadAMXMBeans()
    {
        if ( mLoader == null )
        {
            //getDomainRootProxy().waitAMXReady();

            mLoader = new AMXConfigLoader(mMBeanServer, mPendingConfigBeans, mTransactions);
            mLoader.start();
            // asynchronous start, caller must wait for 
        }
        return getDomainConfig();
    }
    
    public synchronized void unloadAMXMBeans()
    {
        final Domain domainConfig = getDomainConfigProxy();
        if ( domainConfig!= null )
        {
            ImplUtil.unregisterAMXMBeans( domainConfig );
        }
        mLoader = null;
    }
}












