package com.sun.appserv.connectors.internal.api;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.api.ConnectorClassFinder;
import org.jvnet.hk2.config.types.Property;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.config.serverbeans.*;

import java.util.*;
import java.util.logging.Level;


@Service
public class AppSpecificConnectorClassLoaderUtil {

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Habitat habitat;

    /**
     * {@inheritDoc}
     */
    public void detectReferredRARs(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);

        //call to detectReferredRAs can be called only when appInfo is available
        if (appInfo == null) {
            throw new IllegalStateException("ApplicationInfo is not available for application [ " + appName + " ]");
        }
        Application app = appInfo.getMetaData(Application.class);

        // Iterate through all bundle descriptors, ejb-descriptors, managed-bean descriptors
        // for references to resource-adapters
        //
        // References can be via :
        // resource-ref
        // resource-env-ref
        // ra-mid
        //
        // Resource definition can be found in :
        // domain.xml
        // sun-ra.xml
        // default connector resource

        //handle application.xml bundle descriptor
        processDescriptorForRAReferences(app, app);

        Collection<BundleDescriptor> bundleDescriptors = app.getBundleDescriptors();

        //bundle descriptors
        for (BundleDescriptor bundleDesc : bundleDescriptors) {
            if (bundleDesc instanceof JndiNameEnvironment) {
                processDescriptorForRAReferences(app, bundleDesc);
            }
            // ejb descriptors
            if (bundleDesc instanceof EjbBundleDescriptor) {
                EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) bundleDesc;
                Set<EjbDescriptor> ejbDescriptors = ejbDesc.getEjbs();
                for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                    processDescriptorForRAReferences(app, ejbDescriptor);

                    if (ejbDescriptor instanceof EjbMessageBeanDescriptor) {
                        EjbMessageBeanDescriptor messageBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;
                        String raMid = messageBeanDesc.getResourceAdapterMid();
                        //there seem to be applications that do not specify ra-mid
                        if (raMid != null) {
                            app.addResourceAdapter(raMid);
                        }
                    }
                }
                //ejb interceptors
                Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
                for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                    processDescriptorForRAReferences(app, ejbInterceptor);
                }

            }
            // managed bean descriptors
            Set<ManagedBeanDescriptor> managedBeanDescriptors = bundleDesc.getManagedBeans();
            for (ManagedBeanDescriptor mbd : managedBeanDescriptors) {
                processDescriptorForRAReferences(app, mbd);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getRARsReferredByApplication(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            Application app = appInfo.getMetaData(Application.class);
            return app.getResourceAdapters();
        } else {
            return new HashSet<String>();
        }
    }

    private void processDescriptorForRAReferences(com.sun.enterprise.deployment.Application app,
                                                  Descriptor descriptor) {
        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment jndiEnv = (JndiNameEnvironment) descriptor;

            // resource-ref
            for (Object resourceRef : jndiEnv.getResourceReferenceDescriptors()) {
                ResourceReferenceDescriptor resRefDesc = (ResourceReferenceDescriptor) resourceRef;
                String jndiName = resRefDesc.getJndiName();
                //ignore refs where jndi-name is not available
                if(jndiName != null){
                    detectResourceInRA(app, jndiName);
                }
            }

            // resource-env-ref
            for (Object jmsDestRef : jndiEnv.getJmsDestinationReferenceDescriptors()) {
                JmsDestinationReferenceDescriptor jmsDestRefDesc = (JmsDestinationReferenceDescriptor) jmsDestRef;
                String jndiName = jmsDestRefDesc.getJndiName();
                //ignore refs where jndi-name is not available
                if(jndiName != null){
                    detectResourceInRA(app, jndiName);
                }
            }
        }
    }

    private void detectResourceInRA(Application app, String jndiName) {
        //domain.xml
        Resource res = getResources().getResourceByName(BindableResource.class, jndiName);
        //embedded ra's resources may not be created yet as they can be created only after .ear deploy
        //  (and .ear may refer to these resources in DD)
        if (res != null) {
            if (ConnectorResource.class.isAssignableFrom(res.getClass())) {
                String poolName = ((ConnectorResource) res).getPoolName();
                Resource pool = getResources().getResourceByName(ResourcePool.class, poolName);
                if (ConnectorConnectionPool.class.isAssignableFrom(pool.getClass())) {
                    String raName = ((ConnectorConnectionPool) pool).getResourceAdapterName();
                    app.addResourceAdapter(raName);
                }
            } else if (AdminObjectResource.class.isAssignableFrom(res.getClass())) {
                String raName = ((AdminObjectResource) res).getResAdapter();
                app.addResourceAdapter(raName);
            }
        } else {
            boolean found = false;
            //detect sun-ra.xml
            List<com.sun.enterprise.config.serverbeans.Application> applications =
                    getApplications().getApplications();
            Iterator itr = applications.iterator();
            while (itr.hasNext()) {
                com.sun.enterprise.config.serverbeans.Application application =
                        (com.sun.enterprise.config.serverbeans.Application) itr.next();
                List<Module> modules = application.getModule();
                if (modules.size() == 1) {
                    Module module = modules.get(0);
                    if ((module.getEngine("connector")) != null &&
                            (!Boolean.valueOf(application.getDeployProperties().getProperty("isComposite")))) {
                        String appName = application.getName();
                        ApplicationInfo appInfo = appRegistry.get(appName);
                        Application dolApp = appInfo.getMetaData(Application.class);
                        Collection<ConnectorDescriptor> rarDescriptors = dolApp.getRarDescriptors();
                        for (ConnectorDescriptor desc : rarDescriptors) {
                            SunConnector sunraDesc = desc.getSunDescriptor();
                            if (sunraDesc != null) {
                                String sunRAJndiName = (String) sunraDesc.getResourceAdapter().
                                        getValue(ResourceAdapter.JNDI_NAME);
                                if (jndiName.equals(sunRAJndiName)) {
                                    app.addResourceAdapter(desc.getName());
                                    found = true;
                                    break;
                                }
                            } else {
                                //check whether it is default resource in the connector
                                if (desc.getDefaultResourcesNames().contains(jndiName)) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!found) {
                DOLUtils.getDefaultLogger().log(Level.FINEST, "could not find resource by name : " + jndiName);
            }
        }
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() {
        try {
            return getConnectorsClassLoaderUtil().getSystemRARClassLoaders();
        } catch (ConnectorRuntimeException cre) {
            throw new RuntimeException(cre.getMessage(), cre);
        }
    }

    public boolean useGlobalConnectorClassLoader() {
        boolean flag = false;
        ConnectorService connectorService = habitat.getComponent(ConnectorService.class);
        //it is possible that connector-service is not yet defined in domain.xml
        if(connectorService != null){
            Property property = connectorService.getProperty("access-all-rars");
            if (property != null) {
                flag = Boolean.valueOf(property.getValue());
            }
        }
        return flag;
    }

    public Collection<String> getRequiredResourceAdapters(String appName) {
        List<String> requiredRars = new ArrayList<String>();
        if (appName != null) {
            ConnectorService connectorService = habitat.getComponent(ConnectorService.class);
            //it is possible that connector-service is not yet defined in domain.xml

            if (connectorService != null) {
                if (appName != null && appName.trim().length() > 0) {
                    Property property = connectorService.getProperty(
                            "required-resource-adapters-for-" + appName.trim());
                    if (property != null) {
                        String requiredRarsString = property.getValue();
                        StringTokenizer tokenizer = new StringTokenizer(requiredRarsString, ",");
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            requiredRars.add(token);
                        }
                    }
                }
            }
        }
        return requiredRars;
    }

    private ConnectorsClassLoaderUtil getConnectorsClassLoaderUtil() {
        return habitat.getComponent(ConnectorsClassLoaderUtil.class);
    }

    private Resources getResources() {
        return habitat.getComponent(Resources.class);
    }

    private Applications getApplications() {
        return habitat.getComponent(Applications.class);
    }
}
