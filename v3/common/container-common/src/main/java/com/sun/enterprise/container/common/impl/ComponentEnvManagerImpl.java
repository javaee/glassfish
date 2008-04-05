package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.JavaEEContainer;
import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.EjbNamingReferenceManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.NamingObjectProxy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import javax.naming.NamingException;
import javax.naming.Context;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ComponentEnvManagerImpl
    implements ComponentEnvManager {

    private static final String JAVA_COMP_STRING = "java:comp/env/";

    private static final String EIS_STRING = "/eis/";

    @Inject
    private Habitat habitat;

    @Inject
    private Logger _logger;

    @Inject
    GlassfishNamingManager namingManager;

    @Inject
    private NamingUtils namingUtils;

    @Inject
    private InvocationManager invMgr;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject(optional=true)
    private EjbNamingReferenceManager ejbRefMgr;

    private Map<String, JndiNameEnvironment> compId2Env =
            new ConcurrentHashMap<String, JndiNameEnvironment>();

    public void register(String componentId, JndiNameEnvironment env) {
        this.compId2Env.put(componentId, env);
    }

    public void unregister(String componentId) {
        this.compId2Env.remove(componentId);
    }

    public JndiNameEnvironment getJndiNameEnvironment(String componentId) {
        return this.compId2Env.get(componentId);
    }

    public JndiNameEnvironment getCurrentJndiNameEnvironment() {
        JndiNameEnvironment desc = null;
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv != null) {
            if (inv.componentId != null) {
                desc = compId2Env.get(inv.componentId);
            }
        }

        return desc;
    }

    public String bindToComponentNamespace(JndiNameEnvironment env)
        throws NamingException {
        String compEnvId = getComponentEnvId(env);
        Collection<JNDIBinding> bindings = getJNDIBindings(env);
        namingManager.bindToComponentNamespace(getApplicationName(env), compEnvId, bindings);
        this.register(compEnvId, env);
        return compEnvId;
    }

    public void unbindFromComponentNamespace(JndiNameEnvironment env)
        throws NamingException {

        String compEnvId = getComponentEnvId(env);
        namingManager.unbindObjects(compEnvId);
        this.unregister(compEnvId);
    }

    public Collection<JNDIBinding> getJNDIBindings(JndiNameEnvironment env) {

        Collection<JNDIBinding> jndiBindings = new ArrayList<JNDIBinding>();

        for (Iterator itr = env.getEnvironmentProperties().iterator();
             itr.hasNext();) {
            EnvironmentProperty next = (EnvironmentProperty) itr.next();
            // Only env-entries that have been assigned a value are
            // eligible for look up
            if (next.hasAValue()) {
                String name = JAVA_COMP_STRING + next.getName();
                Object value = next.getValueObject();
                jndiBindings.add(new CompEnvBinding(name,
                        namingUtils.createSimpleNamingObjectFactory(name, value)));
            }
        }

        for (Iterator itr =
             env.getJmsDestinationReferenceDescriptors().iterator();
             itr.hasNext();) {
            JmsDestinationReferenceDescriptor next =
                (JmsDestinationReferenceDescriptor) itr.next();
            jndiBindings.add(getCompEnvBinding(next));
        }

        for (Iterator itr = env.getEjbReferenceDescriptors().iterator();
             itr.hasNext();) {
            EjbReferenceDescriptor next = (EjbReferenceDescriptor) itr.next();
            String name = JAVA_COMP_STRING + next.getName();
            EjbReferenceProxy proxy = new EjbReferenceProxy(ejbRefMgr, next);
            jndiBindings.add(new CompEnvBinding(name, proxy));
        }


        for (Iterator itr = env.getMessageDestinationReferenceDescriptors().
                 iterator(); itr.hasNext();) {
            MessageDestinationReferenceDescriptor next =
                (MessageDestinationReferenceDescriptor) itr.next();
            jndiBindings.add(getCompEnvBinding(next));
        }

        for (Iterator itr = env.getResourceReferenceDescriptors().iterator();
            itr.hasNext();) {
            ResourceReferenceDescriptor resourceRef =
                (ResourceReferenceDescriptor) itr.next();
            String name = JAVA_COMP_STRING + resourceRef.getName();
            Object value = null;
            String physicalJndiName = resourceRef.getJndiName();
            if (resourceRef.isMailResource()) {
                value = new MailNamingObjectFactory(name,
                    physicalJndiName, namingUtils);
            } else if (resourceRef.isURLResource()) {
                Object obj = null;
                try {
                   obj  = new java.net.URL(physicalJndiName);
                }
                catch(MalformedURLException e) {
                    e.printStackTrace();
                }
                NamingObjectFactory factory = namingUtils.createSimpleNamingObjectFactory(name, obj);
                value = namingUtils.createCloningNamingObjectFactory(name, factory);
            } else if (resourceRef.isORB()) {
            } else if (resourceRef.isWebServiceContext()) {
            } else {
              value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, true);
            }
            jndiBindings.add(new CompEnvBinding(name, value));
        }

        for (EntityManagerFactoryReferenceDescriptor next :
                 env.getEntityManagerFactoryReferenceDescriptors()) {
            String name = JAVA_COMP_STRING + next.getName();
            Object value = new FactoryForEntityManagerFactoryWrapper(next.getUnitName(),
                    invMgr, this);
            jndiBindings.add(new CompEnvBinding(name, value));
         }

/*
//TODO:
        for (Iterator itr = env.getServiceReferenceDescriptors().iterator();
             itr.hasNext();) {
            ServiceReferenceDescriptor next =
                (ServiceReferenceDescriptor) itr.next();
            DefaultJNDIBinding binding = new DefaultJNDIBinding(next);
            jndiBindings.add(binding);
        }

*/

         for (EntityManagerReferenceDescriptor next :
             env.getEntityManagerReferenceDescriptors()) {
             String name = JAVA_COMP_STRING + next.getName();
             FactoryForEntityManagerWrapper value =
                new FactoryForEntityManagerWrapper(next, habitat);
            jndiBindings.add(new CompEnvBinding(name, value));
         }

        return jndiBindings;
    }

    private CompEnvBinding getCompEnvBinding(JmsDestinationReferenceDescriptor next) {
        String name = JAVA_COMP_STRING + next.getName();
            Object value = null;
            if (next.isEJBContext()) {
                //value = visitor.visitEJBContext(next);
            } else {
                value = namingUtils.createLazyNamingObjectFactory(name, next.getJndiName(), true);
            }

        return new CompEnvBinding(name, value);
    }

    private CompEnvBinding getCompEnvBinding(MessageDestinationReferenceDescriptor next) {
        String name = JAVA_COMP_STRING + next.getName();
        String physicalJndiName = null;
        if (next.isLinkedToMessageDestination()) {
            physicalJndiName = next.getMessageDestination().getJndiName();
        } else {
            physicalJndiName = next.getJndiName();
        }

        Object value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, true);
            return new CompEnvBinding(name, value);
    }


    /**
     * Generate the name of an environment property in the java:comp/env
     * namespace.  This is the lookup string used by a component to access
     * its environment.
     */
    private String descriptorToLogicalJndiName(Descriptor descriptor) {
        return JAVA_COMP_STRING + descriptor.getName();
    }


    private static final String ID_SEPARATOR = "_";

    /**
     * Generate a unique id name for each J2EE component.
     */
    private String getComponentEnvId(JndiNameEnvironment env) {
	    String id = null;

        if (env instanceof EjbDescriptor) {
            // EJB component
	        EjbDescriptor ejbEnv = (EjbDescriptor) env;

            // Make jndi name flat so it won't result in the creation of
            // a bunch of sub-contexts.
            String flattedJndiName = ejbEnv.getJndiName().replace('/', '.');

            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
	        id = ejbEnv.getApplication().getName() + ID_SEPARATOR +
                ejbBundle.getModuleDescriptor().getArchiveUri()
                + ID_SEPARATOR +
                ejbEnv.getName() + ID_SEPARATOR + flattedJndiName +
                ejbEnv.getUniqueId();
        } else if(env instanceof WebBundleDescriptor) {
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
	    id = webEnv.getApplication().getName() + ID_SEPARATOR +
                webEnv.getContextRoot();
        } else if (env instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appEnv =
		(ApplicationClientDescriptor) env;
	    id = "client" + ID_SEPARATOR + appEnv.getName() +
                ID_SEPARATOR + appEnv.getMainClassName();
        }

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, getApplicationName(env)
                + "Component Id: " + id);
        }
        return id;
    }

    private String getApplicationName(JndiNameEnvironment env) {
        String appName = "";
        String moduleName = "";

        if (env instanceof EjbDescriptor) {
            // EJB component
	    EjbDescriptor ejbEnv = (EjbDescriptor) env;
            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
	    appName = "ejb ["+
                ejbEnv.getApplication().getRegistrationName();
            moduleName = ejbEnv.getName();
            if (moduleName == null || moduleName.equals("")) {
                appName = appName+"]";
            }
            else {
                appName = appName+":"+ejbEnv.getName()+"]";
            }
        } else if (env instanceof WebBundleDescriptor) {
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
	    appName = "web module ["+
                webEnv.getApplication().getRegistrationName();
            moduleName = webEnv.getContextRoot();
            if (moduleName == null || moduleName.equals("")) {
                appName = appName+"]";
            }
            else {
                appName = appName+":"+webEnv.getContextRoot()+"]";
            }
        } else if (env instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appEnv =
		(ApplicationClientDescriptor) env;
	    appName =  "client ["+appEnv.getName() +
                ":" + appEnv.getMainClassName()+"]";
        }
        return appName;
    }

    private static boolean isConnector(String logicalJndiName){
        return (logicalJndiName.indexOf(EIS_STRING) != -1);
    }

    private class FactoryForEntityManagerWrapper
        implements NamingObjectProxy {

        private EntityManagerReferenceDescriptor refDesc;

        private Habitat habitat;
        
        FactoryForEntityManagerWrapper(EntityManagerReferenceDescriptor refDesc,
            Habitat habitat) {
            this.refDesc = refDesc;
            this.habitat = habitat;
        }

        public Object create(Context ctx) {
            EntityManagerWrapper emWrapper =
                    habitat.getComponent(EntityManagerWrapper.class);
            emWrapper.initializeEMWrapper(refDesc.getUnitName(),
                    refDesc.getPersistenceContextType(),
                    refDesc.getProperties());

            return emWrapper;
        }
    }

    private static class EjbReferenceProxy
        implements NamingObjectProxy {
        
        private EjbNamingReferenceManager ejbRefMgr;
        private EjbReferenceDescriptor ejbRef;
        private boolean isCacheable;
        private transient Object cachedValue;

        EjbReferenceProxy(EjbNamingReferenceManager ejbRefMgr, EjbReferenceDescriptor ejbRef) {
            this.ejbRefMgr = ejbRefMgr;
            this.ejbRef = ejbRef;
        }

        public Object create(Context ctx)
                throws NamingException {

            Object result = null;

            if (ejbRefMgr != null) {
                if (ejbRefMgr.isEjbReferenceCacheable(ejbRef)) {
                    if (cachedValue == null) {
                        cachedValue = ejbRefMgr.resolveEjbReference(ejbRef, null);
                    }

                    result = cachedValue;
                } else {
                    result = ejbRefMgr.resolveEjbReference(ejbRef, null);
                }
            }

            return result;
        }
    }

    private static class CompEnvBinding
        implements JNDIBinding {

        private String name;
        private Object value;

        CompEnvBinding(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

    }

}
