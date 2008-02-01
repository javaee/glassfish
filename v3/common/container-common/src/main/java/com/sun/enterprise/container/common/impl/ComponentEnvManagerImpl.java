package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.JavaEEContainer;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;
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
    private Logger _logger;

    @Inject
    GlassfishNamingManager namingManager;

    @Inject
    private NamingUtils namingUtils;

    @Inject
    private InvocationManager invMgr;

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

    public Object getCurrentDescriptor() {
        Object desc = null;
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv != null) {
            JavaEEContainer cc = (JavaEEContainer) inv.getContainer();
            if (cc != null) {
                 desc = cc.getDescriptor();
            }
        }

        return desc;
    }

    public String bindToComponentNamespace(JndiNameEnvironment env)
        throws NamingException {
        String compEnvId = getComponentEnvId(env);
        Collection<JNDIBinding> bindings = getJNDIBindings(env);
        namingManager.bindToComponentNamespace(getApplicationName(env), compEnvId, bindings);
        return compEnvId;
    }

    public void unbindFromComponentNamespace(JndiNameEnvironment env)
        throws NamingException {
        namingManager.unbindObjects(getComponentEnvId(env));
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

/*
//TODO:
        for (Iterator itr = env.getEjbReferenceDescriptors().iterator();
             itr.hasNext();) {
            EjbReferenceDescriptor next = (EjbReferenceDescriptor) itr.next();
            EjbReferenceJNDIBinding binding = new EjbReferenceJNDIBinding(next);
            jndiBindings.add(binding);
        }

*/
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



         for (EntityManagerReferenceDescriptor next :
             env.getEntityManagerReferenceDescriptors()) {
            EntityManagerJNDIBinding binding =
                new EntityManagerJNDIBinding(next);
            jndiBindings.add(binding);
         }
*/

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
