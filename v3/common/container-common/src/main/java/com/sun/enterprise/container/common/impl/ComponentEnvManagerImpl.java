/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.JavaEEContainer;
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
import javax.naming.NameNotFoundException;
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

    // TODO: container-common shouldn't depend on EJB stuff, right?
    // this seems like the abstraction design failure.
    @Inject
    private NamingUtils namingUtils;

    @Inject
    private InvocationManager invMgr;

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
                // TODO handle non-default ORBs
                value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, false);
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
                value = new EjbContextProxy(next.getRefType());
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

    private class EjbContextProxy
        implements NamingObjectProxy {

        private volatile EjbNamingReferenceManager ejbRefMgr;
        private String contextType;

        EjbContextProxy(String contextType) {
            this.contextType = contextType;
        }

        public Object create(Context ctx)
                throws NamingException {
            Object result = null;

            if (ejbRefMgr==null) {
                ejbRefMgr = habitat.getByContract(EjbNamingReferenceManager.class);
            }

            if (ejbRefMgr != null) {
                result = ejbRefMgr.getEJBContextObject(contextType);    
            }

            if( result == null ) {
                throw new NameNotFoundException("Can not resolve EJB context of type " +
                    contextType);
            }

            return result;
        }

    }
    private class EjbReferenceProxy
        implements NamingObjectProxy {

        private volatile EjbNamingReferenceManager ejbRefMgr;
        private EjbReferenceDescriptor ejbRef;

        // Note : V2 had a limited form of ejb-ref caching.  It only applied
        // to EJB 2.x Home references where the target lived in the same application
        // as the client.  It's not clear how useful that even is and it's of limited
        // value given the behavior is different for EJB 3.x references.  For now,
        // all ejb-ref caching is turned off.

        EjbReferenceProxy(EjbNamingReferenceManager ejbRefMgr, EjbReferenceDescriptor ejbRef) {
            this.ejbRefMgr = ejbRefMgr;
            this.ejbRef = ejbRef;
        }

        public Object create(Context ctx)
                throws NamingException {

            Object result = null;

            if (ejbRefMgr==null) {
                ejbRefMgr = habitat.getByContract(EjbNamingReferenceManager.class);
            }

            if (ejbRefMgr != null) {

                result = ejbRefMgr.resolveEjbReference(ejbRef, ctx);

            }

            if( result == null ) {
                throw new NameNotFoundException("Can not resolve ejb reference " + ejbRef.getName() +
                    " : " + ejbRef);
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
