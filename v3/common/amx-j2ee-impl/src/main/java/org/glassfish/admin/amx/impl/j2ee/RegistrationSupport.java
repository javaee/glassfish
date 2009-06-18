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
import java.util.HashMap;
import java.util.Map;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import javax.management.ObjectName;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.io.DescriptorConstants;




import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.j2ee.loader.J2EEInjectedValues;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.intf.config.AMXConfigUtil;
import org.glassfish.admin.amx.intf.config.ApplicationRef;
import org.glassfish.admin.amx.intf.config.ResourceRef;
import org.glassfish.admin.amx.intf.config.Server;
import org.glassfish.admin.amx.j2ee.EJB;
import org.glassfish.admin.amx.j2ee.EJBModule;
import org.glassfish.admin.amx.j2ee.EntityBean;
import org.glassfish.admin.amx.j2ee.J2EEApplication;
import org.glassfish.admin.amx.j2ee.J2EEManagedObject;

import org.glassfish.admin.amx.j2ee.MessageDrivenBean;
import org.glassfish.admin.amx.j2ee.Servlet;
import org.glassfish.admin.amx.j2ee.StatefulSessionBean;
import org.glassfish.admin.amx.j2ee.StatelessSessionBean;
import org.glassfish.admin.amx.j2ee.WebModule;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.MapUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;


import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.j2ee.AppClientModule;
import org.glassfish.admin.amx.j2ee.J2EEServer;
import org.glassfish.admin.amx.j2ee.ResourceAdapter;
import org.glassfish.admin.amx.j2ee.ResourceAdapterModule;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

/**
Handles registrations of resources and applications associated with a J2EEServer.
 */
final class RegistrationSupport
{
    private static void cdebug(Object o)
    {
        System.out.println("" + o);
    }

    /** Maps the ObjectName of a config MBean to that of a JSR 77 MBean */
    private static final Map<ObjectName, ObjectName> mConfigTo77 = new HashMap<ObjectName, ObjectName>();

    private final J2EEServer mJ2EEServer;

    private final MBeanServer mMBeanServer;

    private final ProxyFactory mProxyFactory;

    private final ResourceRefListener mResourceRefListener;

    public RegistrationSupport(final J2EEServer server)
    {
        mJ2EEServer = server;
        mMBeanServer = (MBeanServer) server.extra().mbeanServerConnection();
        mProxyFactory = server.extra().proxyFactory();

        final ObjectName test = mJ2EEServer.objectName();

        mResourceRefListener = new ResourceRefListener(mMBeanServer, mJ2EEServer.objectName(), getServerConfig());

        registerApplications(getServerConfig());
    }

    protected void cleanup()
    {
        mResourceRefListener.stopListening();
    }

    public void start()
    {
        mResourceRefListener.startListening();
    }

    /** Maps configuration MBean type to J2EE type */
    public static final Map<String, Class> RESOURCE_TYPES =
            MapUtil.toMap(new Object[]
            {
                "jdbc-resource", JDBCResourceImpl.class,
                "java-mail-resource", JavaMailResourceImpl.class,
                "jca-resource", JCAResourceImpl.class,
                "jms-resource", JMSResourceImpl.class,
                "jndi-resource", JNDIResourceImpl.class,
                "jta-resource", JTAResourceImpl.class,
                "rmi-iiop-resource", RMI_IIOPResourceImpl.class,
                "url-resource", URLResourceImpl.class
            },
            String.class, Class.class);

    private Domain getDomainConfig()
    {
        return AMXConfigUtil.getDomainConfig(mJ2EEServer);
    }

    private Server getServerConfig()
    {
        return getDomainConfig().getServers().getServer().get( mJ2EEServer.getName() );
    }

    private ObjectName createAppMBeans(final Application application, final Metadata meta)
    {
        final String appLocation = "somewhere";

        final boolean isStandalone = application.isVirtual();
        ObjectName parentMBean = null;
        ObjectName top = null;
        if (isStandalone)
        {
            parentMBean = mJ2EEServer.objectName();
        }
        else
        {
            parentMBean = registerJ2EEChild(mJ2EEServer.objectName(), meta, J2EEApplication.class, J2EEApplicationImpl.class, application.getName());
            top = parentMBean;
        }

        for (final EjbBundleDescriptor desc : application.getEjbBundleDescriptors())
        {
            final ObjectName objectName = registerEjbModuleAndItsComponents(parentMBean, desc, appLocation);
            if (isStandalone)
            {
                assert (top != null);
                top = objectName;
            }
        }

        for (final WebBundleDescriptor desc : application.getWebBundleDescriptors())
        {
            final ObjectName objectName = registerWebModuleAndItsComponents(parentMBean, desc, appLocation);
            if (isStandalone)
            {
                assert (top != null);
                top = objectName;
            }
        }

        for (final ConnectorDescriptor desc : application.getRarDescriptors())
        {
            top = registerResourceAdapterModuleAndItsComponents(parentMBean, desc, appLocation);
        }

        for (final ApplicationClientDescriptor desc : application.getApplicationClientDescriptors())
        {
            top = registerAppClient(parentMBean, desc, appLocation);
        }

        ImplUtil.getLogger().info("Registered JSR 77 MBeans for application/module: " + top);
        return top;
    }

    /* Register ejb module and its' children ejbs which is part of an application */
    private ObjectName registerEjbModuleAndItsComponents(
            final ObjectName parentMBean,
            final EjbBundleDescriptor ejbBundleDescriptor,
            final String appLocation)
    {
        final ObjectName objectName = createEJBModuleMBean(parentMBean, ejbBundleDescriptor, appLocation);

        for (final EjbDescriptor desc : ejbBundleDescriptor.getEjbs())
        {
            createEJBMBean(parentMBean, desc);
        }
        return objectName;
    }

    /* Create ejb module mBean */
    private ObjectName createEJBModuleMBean(
            final ObjectName parentMBean,
            final EjbBundleDescriptor ejbBundleDescriptor,
            final String appLocation)
    {
        final String xmlDesc = getStringForDDxml(getModuleLocation(ejbBundleDescriptor, "EJBModule"));
        final String moduleName = getModuleName(ejbBundleDescriptor);
        final String applicationName = getApplicationName(ejbBundleDescriptor);

        final Metadata meta = new MetadataImpl();
        final ObjectName objectName = registerJ2EEChild(parentMBean, meta, EJBModule.class, EJBModuleImpl.class, moduleName);

        return objectName;
    }

    private ObjectName createEJBMBean(
            final ObjectName parentMBean,
            final EjbDescriptor ejbDescriptor)
    {
        final String ejbName = ejbDescriptor.getName();
        final String ejbType = ejbDescriptor.getType();
        final String ejbSessionType = ejbType.equals("Session") ? ((EjbSessionDescriptor) ejbDescriptor).getSessionType() : null;

        Class<? extends EJB> intf = null;
        Class<? extends EJBImplBase> impl = null;
        if (ejbType.equals("Entity"))
        {
            intf = EntityBean.class;
            impl = EntityBeanImpl.class;
        }
        else if (ejbType.equals("Message-driven"))
        {
            intf = MessageDrivenBean.class;
            impl = MessageDrivenBeanImpl.class;
        }
        else if (ejbType.equals("Session"))
        {
            if (ejbSessionType.equals("Stateless"))
            {
                intf = StatelessSessionBean.class;
                impl = StatelessSessionBeanImpl.class;
            }
            else if (ejbSessionType.equals("Stateful"))
            {
                intf = StatefulSessionBean.class;
                impl = StatefulSessionBeanImpl.class;
            }
        }

        final Metadata meta = new MetadataImpl();
        return registerJ2EEChild(parentMBean, meta, intf, impl, ejbName);
    }

    /* Register web module and its' children which is part of an application */
    private ObjectName registerWebModuleAndItsComponents(
            final ObjectName parentMBean,
            final WebBundleDescriptor webBundleDescriptor,
            final String appLocation)
    {
        final ObjectName webModuleObjectName = createWebModuleMBean(parentMBean, webBundleDescriptor, appLocation);

        for (final WebComponentDescriptor desc : webBundleDescriptor.getWebComponentDescriptors())
        {
            final String servletName = desc.getCanonicalName();
            final Metadata meta = new MetadataImpl();
            final ObjectName on = registerJ2EEChild(webModuleObjectName, meta, Servlet.class, ServletImpl.class, servletName);
        }

        return webModuleObjectName;
    }

    private ObjectName createWebModuleMBean(
            final ObjectName parentMBean,
            final WebBundleDescriptor webBundleDescriptor,
            final String appLocation)
    {
        final String xmlDesc = getStringForDDxml(getModuleLocation(webBundleDescriptor, "WebModule"));
        final String moduleName = getModuleName(webBundleDescriptor);

        final Metadata meta = new MetadataImpl();
        return registerJ2EEChild(parentMBean, meta, WebModule.class, WebModuleImpl.class, moduleName);
    }

    public ObjectName registerResourceAdapterModuleAndItsComponents(
            final ObjectName parentMBean,
            final ConnectorDescriptor bundleDesc,
            final String appLocation)
    {
        final ObjectName objectName = createRARModuleMBean(parentMBean, bundleDesc, appLocation);

        final Metadata meta = new MetadataImpl();
        final ObjectName rarObjectName = registerJ2EEChild(objectName, meta, ResourceAdapter.class, ResourceAdapterImpl.class, bundleDesc.getName());

        return objectName;
    }

    private ObjectName createRARModuleMBean(
            final ObjectName parentMBean,
            final ConnectorDescriptor bundleDesc,
            final String appLocation)
    {
        // get the string for deployment descriptor file
        // fix for CTS bug# 6411637
        // if resource adapter module name is one of connector system apps
        // then set the location of deployment descriptor to the original
        // location and not the generated directory. These system apps are
        // directly loaded without generating descriptors
        String modLocation = "";
        if (bundleDesc.getModuleDescriptor().isStandalone())
        {
            modLocation = appLocation + File.separator + DescriptorConstants.RAR_DD_ENTRY;
        }
        else
        {
            final String moduleName = bundleDesc.getUniqueFriendlyId();
            modLocation = appLocation + File.separator + moduleName + File.separator + DescriptorConstants.RAR_DD_ENTRY;
        }

        final String xmlDesc = getStringForDDxml(modLocation);
        final String resAdName = getModuleName(bundleDesc);

        final Metadata meta = new MetadataImpl();
        final ObjectName objectName = registerJ2EEChild(parentMBean, meta, ResourceAdapterModule.class, ResourceAdapterModuleImpl.class, resAdName);

        return objectName;
    }

    /* Register application client module */
    public ObjectName registerAppClient(
            final ObjectName parentMBean,
            final ApplicationClientDescriptor bundleDesc,
            final String appLocation)
    {
        final String xmlDesc = getStringForDDxml(getModuleLocation(bundleDesc, "AppClientModule"));

        String applicationName = null;
        if (bundleDesc.getApplication() != null)
        {
            if (!bundleDesc.getModuleDescriptor().isStandalone())
            {
                applicationName = bundleDesc.getApplication().getRegistrationName();
            }
        }
        if (applicationName == null)
        {
            applicationName = bundleDesc.getName();
        }

        final Metadata meta = new MetadataImpl();
        return registerJ2EEChild(parentMBean, meta, AppClientModule.class, AppClientModuleImpl.class, applicationName);
    }

    /** Utility routine somewhere? */
    private String getStringForDDxml(String fileName)
    {
        if (!(new File(fileName)).exists())
        {
            ImplUtil.getLogger().fine("Descriptor does not exist " + fileName);
            return null;
        }

        FileReader fr = null;
        try
        {
            fr = new FileReader(fileName);
            StringWriter sr = new StringWriter();

            char[] buf = new char[8192];
            int len = 0;
            while (len != -1)
            {
                try
                {
                    len = fr.read(buf, 0, buf.length);
                }
                catch (EOFException eof)
                {
                    break;
                }
                if (len != -1)
                {
                    sr.write(buf, 0, len);
                }
            }

            fr.close();
            sr.close();
            return sr.toString();

        }
        catch (final IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        finally
        {
            if (fr != null)
            {
                try
                {
                    fr.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }
    }

    private String getModuleName(final BundleDescriptor bd)
    {
        String moduleName = null;

        if (bd.getModuleDescriptor().isStandalone())
        {
            moduleName = bd.getApplication().getRegistrationName();
        }
        else
        {
            moduleName = bd.getModuleDescriptor().getArchiveUri();
        }

        return moduleName;
    }

    private String getApplicationName(final BundleDescriptor bd)
    {
        String applicationName = "null";

        if (bd.getModuleDescriptor().isStandalone())
        {
            return applicationName;
        }
        else
        {
            if (bd.getApplication() != null)
            {
                applicationName = bd.getApplication().getRegistrationName();
                if (applicationName == null)
                {
                    applicationName = "null";
                }
            }
        }
        return applicationName;
    }

    /* get module location */
    private String getModuleLocation(
            final BundleDescriptor bd,
            final String j2eeType)
    {
        String modLocation = null;
        final String ddRoot = bd.getApplication().getGeneratedXMLDirectory();
        final String fs = File.separator;

        if (bd.getModuleDescriptor().isStandalone())
        {
            final String moduleName = bd.getApplication().getRegistrationName();

            if (j2eeType.equals("AppClientModule"))
            {
                modLocation = ddRoot + fs + DescriptorConstants.APP_CLIENT_DD_ENTRY;
            }
            else if (j2eeType.equals("EJBModule"))
            {
                modLocation = ddRoot + fs + DescriptorConstants.EJB_DD_ENTRY;
            }
            else if (j2eeType.equals("WebModule"))
            {
                modLocation = ddRoot + fs + DescriptorConstants.WEB_DD_ENTRY;
            }
            else if (j2eeType.equals("ResourceAdapterModule"))
            {
                modLocation = ddRoot + fs + DescriptorConstants.RAR_DD_ENTRY;
            }
            else
            {
                throw new IllegalArgumentException(j2eeType);
            }
        }
        else
        {
            final String moduleName = bd.getUniqueFriendlyId();

            if (j2eeType.equals("AppClientModule"))
            {
                modLocation = ddRoot + fs + moduleName + fs + DescriptorConstants.APP_CLIENT_DD_ENTRY;
            }
            else if (j2eeType.equals("EJBModule"))
            {
                modLocation = ddRoot + fs + moduleName + fs + DescriptorConstants.EJB_DD_ENTRY;
            }
            else if (j2eeType.equals("WebModule"))
            {
                modLocation = ddRoot + fs + moduleName + fs + DescriptorConstants.WEB_DD_ENTRY;
            }
            else if (j2eeType.equals("ResourceAdapterModule"))
            {
                modLocation = ddRoot + fs + moduleName + fs + DescriptorConstants.RAR_DD_ENTRY;
            }
            else
            {
                throw new IllegalArgumentException(j2eeType);
            }
        }

        return modLocation;
    }

    protected void registerApplications(final Server serverConfig)
    {
        // find all applications
        final Map<String, ApplicationRef> appRefConfigs = serverConfig.getApplicationRef();
        final ApplicationRegistry appRegistry = J2EEInjectedValues.getInstance().getApplicationRegistry();

        final MetadataImpl meta = new MetadataImpl();
        for (final ApplicationRef ref : appRefConfigs.values())
        {
            meta.setCorrespondingRef(ref.objectName());
            
            final String appName = ref.getName();
            
            final ApplicationInfo appInfo = appRegistry.get(appName);
            if (appInfo == null)
            {
                ImplUtil.getLogger().info("Unable to get ApplicationInfo for application: " + appName);
                continue;
            }
            final Application app = appInfo.getMetaData(Application.class);
            
            final org.glassfish.admin.amx.intf.config.Application appConfig = AMXConfigUtil.getApplicationByName(ref, appName);
            if ( appConfig == null )
            {
                ImplUtil.getLogger().warning("Unable to get Application config for: " + appName);
                continue;
            }
            
            meta.setConfig( appConfig.objectName() );
            final ObjectName objectName = createAppMBeans(app, meta);
        }
    }

    protected <I extends J2EEManagedObject, C extends J2EEManagedObjectImplBase> ObjectName registerJ2EEChild(
            final ObjectName parent,
            final Metadata metadata,
            final Class<I> intf,
            final Class<C> clazz,
            final String name)
    {
        ObjectName objectName = null;
        
        final String j2eeType = Util.deduceType(intf);
            
        try
        {
            final Constructor<C> c = clazz.getConstructor(ObjectName.class, Metadata.class);
            final J2EEManagedObjectImplBase impl = c.newInstance(parent, metadata);
            objectName = new ObjectNameBuilder(mMBeanServer, parent).buildChildObjectName(j2eeType, name);
            objectName = mMBeanServer.registerMBean( impl, objectName ).getObjectName();
        }
        catch (final Exception e)
        {
            throw new RuntimeException( "Cannot register " + j2eeType + "=" + name + " as child of " + parent, e);
        }

        return objectName;
    }

    /**
    Listen for registration/unregistration of {@link ResourceRef},
    and associate them with JSR 77 MBeans for this J2EEServer.
    Resources belong to a J2EEServer via ResourceRefs.  So we can stay in the AMX
    world by tracking registration and unregistration of AMX config MBeans of
    type ResourceRef.
     */
    private final class ResourceRefListener implements NotificationListener
    {
        private final MBeanServer mServer;

        private final ObjectName mJ2EEServer;

        /** the parent MBeans for config resources MBeans */
        private final Server mServerConfig;

        private final String mResourceRefType;

        public ResourceRefListener(final MBeanServer server, final ObjectName j2eeServer, final Server serverConfig)
        {
            mServer = server;
            mJ2EEServer = j2eeServer;
            mServerConfig = serverConfig;

            mResourceRefType = Util.deduceType(ResourceRef.class);
        }

        /**
        Consider the MBean to see if it is a ResourceRef that should be manifested under this server.
         */
        public void register(final ResourceRef ref)
        {
            if (!mResourceRefType.equals(ref.type()))
            {
                throw new IllegalArgumentException("Not a resource-ref: " + ref.objectName());
            }

            if (!mServerConfig.objectName().equals(ref.parent().objectName()))
            {
                cdebug("ResourceRef is not a child of server " + mServerConfig.objectName());
                return;
            }

            // find the referenced resource
            final AMXConfigProxy amxConfig = AMXConfigUtil.getResourceByName(getDomainConfig().getResources(), ref.getName());
            if (amxConfig == null)
            {
                throw new IllegalArgumentException("ResourceRef refers to non-existent resource: " + ref);
            }

            final String configType = amxConfig.type();
            final Class<J2EEManagedObjectImplBase> implClass = RESOURCE_TYPES.get(configType);
            if (implClass == null)
            {
                ImplUtil.getLogger().info("Unrecognized resource type for JSR 77 purposes: " + amxConfig.objectName());
                return;
            }
            final Class<J2EEManagedObject> intf = (Class) ClassUtil.getFieldValue(implClass, "INTF");
            final String j2eeType = Util.deduceType(intf);

            try
            {
                final MetadataImpl meta = new MetadataImpl();
                meta.setCorrespondingRef(ref.objectName());
                meta.setConfig(amxConfig.objectName());
                
                final ObjectName mbean77 = registerJ2EEChild(mJ2EEServer, meta, intf, implClass, amxConfig.getName());
                synchronized (mConfigTo77) // prevent race condition where MBean is unregistered before we can put it into our Map
                {
                    mConfigTo77.put(ref.objectName(), mbean77);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        //cdebug( "Registered " + child + " for  config resource " + amx.objectName() );
        }

        public void handleNotification(final Notification notifIn, final Object handback)
        {
            if (!(notifIn instanceof MBeanServerNotification))
            {
                return;
            }

            final MBeanServerNotification notif = (MBeanServerNotification) notifIn;
            final ObjectName objectName = notif.getMBeanName();
            if (!mJ2EEServer.getDomain().equals(objectName.getDomain()))
            {
                return;
            }

            if (notif.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
            {
                final ResourceRef ref = mProxyFactory.getProxy(objectName, ResourceRef.class);
                register(ref);
            }
            else if (notif.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
            {
                // determine if it's a config for which a JSR 77  MBean is registered
                synchronized (mConfigTo77)
                {
                    final ObjectName mbean77 = mConfigTo77.remove(objectName);
                    if (mbean77 != null)
                    {
                        try
                        {
                            mMBeanServer.unregisterMBean(mbean77);
                        }
                        catch (final Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void startListening()
        {
            // important: register a listener *first* so that we don't miss anything 
            try
            {
                mServer.addNotificationListener(JMXUtil.getMBeanServerDelegateObjectName(), this, null, null);
            }
            catch (final JMException e)
            {
                throw new RuntimeException(e);
            }

            // register all existing ResourceRefs
            final Map<String, ResourceRef> resourceRefs = mServerConfig.getResourceRef();
            for (final ResourceRef ref : resourceRefs.values())
            {
                register(ref);
            }
        }

        public void stopListening()
        {
            try
            {
                mServer.removeNotificationListener(JMXUtil.getMBeanServerDelegateObjectName(), this);
            }
            catch (final JMException e)
            {
                throw new RuntimeException(e);
            }
        }

    }
}





















