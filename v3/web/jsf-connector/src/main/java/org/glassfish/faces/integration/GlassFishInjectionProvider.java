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

package org.glassfish.faces.integration;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import com.sun.faces.util.FacesLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.InjectionInfo;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.ComponentInvocation;

/**
 * <p>This <code>InjectionProvider</code> is specific to the
 * GlassFish/SJSAS 9.x PE/EE application servers.</p>
 */
public class GlassFishInjectionProvider extends DiscoverableInjectionProvider {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private static final String HABITAT_ATTRIBUTE =
            "org.glassfish.servlet.habitat";
    private InjectionManager injectionManager;
    private InvocationManager invokeMgr;

    /**
     * <p>Constructs a new <code>GlassFishInjectionProvider</code> instance.</p>
     *
     * @param servletContext
     */
    public GlassFishInjectionProvider(ServletContext servletContext) {
        Habitat defaultHabitat = (Habitat)servletContext.getAttribute(
                HABITAT_ATTRIBUTE);
        invokeMgr = defaultHabitat.getByContract(InvocationManager.class);
        injectionManager = defaultHabitat.getByContract(InjectionManager.class);
    }
    
    /**
     * <p>The implementation of this method must perform the following
     * steps:
     * <ul>
     * <li>Inject the supported resources per the Servlet 2.5
     * specification into the provided object</li>    
     * </ul>
     * </p>
     *
     * @param managedBean the target managed bean
     */
    public void inject(Object managedBean) throws InjectionProviderException {
        try {
            injectionManager.injectInstance(managedBean,
                                            getNamingEnvironment(),
                                            false);
        } catch (InjectionException ie) {
            throw new InjectionProviderException(ie);
        }
    }

    /**
     * <p>The implemenation of this method must invoke any
     * method marked with the <code>@PreDestroy</code> annotation
     * (per the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     */
    public void invokePreDestroy(Object managedBean)
    throws InjectionProviderException {
        try {
            injectionManager.invokeInstancePreDestroy(managedBean);
        } catch (InjectionException ie) {
            throw new InjectionProviderException(ie);
        }
    }


    /**
     * <p>The implemenation of this method must invoke any
     * method marked with the <code>@PostConstruct</code> annotation
     * (per the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     *
     * @throws com.sun.faces.spi.InjectionProviderException
     *          if an error occurs when invoking
     *          the method annotated by the <code>@PostConstruct</code> annotation
     */
    public void invokePostConstruct(Object managedBean)
          throws InjectionProviderException {
            try {
                this.invokePostConstruct(managedBean, getNamingEnvironment());
            } catch (InjectionException ie) {
                throw new InjectionProviderException(ie);
            }

    }


    // --------------------------------------------------------- Private Methods

    /**
     * <p>This is based off of code in <code>InjectionManagerImpl</code>.</p>
     * @return <code>JndiNameEnvironment</code>
     * @throws InjectionException if we're unable to obtain the
     *  <code>JndiNameEnvironment</code>
     */
    private JndiNameEnvironment getNamingEnvironment()
         throws InjectionException {
        ComponentInvocation inv = invokeMgr.getCurrentInvocation();

        if (inv != null) {

            if (inv.getInvocationType()== ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION) {

                JndiNameEnvironment componentEnv = (JndiNameEnvironment)
                     inv.jndiEnvironment;

                if (componentEnv != null) {
                    return componentEnv;
                } else {
                    throw new InjectionException("No descriptor registered for " + " current invocation : " + inv);
                }
            } else {
                throw new InjectionException("Wrong invocation type");
            }
        } else {
            throw new InjectionException("null invocation context");
        }
    }


    /**
     * <p>This is based off of code in <code>InjectionManagerImpl</code>.</p>
     *
     * @param instance managed bean instance
     * @param envDescriptor JNDI environment
     * @throws InjectionException if an error occurs
     */
    private void invokePostConstruct(Object instance,
                                     JndiNameEnvironment envDescriptor)
    throws InjectionException {
        LinkedList<Method> postConstructMethods = new LinkedList<Method>();

        Class<? extends Object> nextClass = instance.getClass();

        // Process each class in the inheritance hierarchy, starting with
        // the most derived class and ignoring java.lang.Object.
        while ((!Object.class.equals(nextClass)) && (nextClass != null)) {

            InjectionInfo injInfo =
                 envDescriptor.getInjectionInfoByClass(nextClass);

            if (injInfo.getPostConstructMethodName() != null) {

                Method postConstructMethod = getPostConstructMethod
                     (injInfo, nextClass);

                // Invoke the preDestroy methods starting from
                // the least-derived class downward.
                postConstructMethods.addFirst(postConstructMethod);
            }

            nextClass = nextClass.getSuperclass();
        }

        for (Method postConstructMethod : postConstructMethods) {

            invokeLifecycleMethod(postConstructMethod, instance);

        }

    }


    /**
     * <p>This is based off of code in <code>InjectionManagerImpl</code>.</p>
     * @param injInfo InjectionInfo
     * @param resourceClass target class
     * @return a Method marked with the @PostConstruct annotation
     * @throws InjectionException if an error occurs
     */
    private Method getPostConstructMethod(InjectionInfo injInfo,
                                          Class<? extends Object> resourceClass)
        throws InjectionException {

        Method m = injInfo.getPostConstructMethod();

        if( m == null ) {
            String postConstructMethodName =
                injInfo.getPostConstructMethodName();

            // Check for the method within the resourceClass only.
            // This does not include super-classses.
            for(Method next : resourceClass.getDeclaredMethods()) {
                // InjectionManager only handles injection into PostConstruct
                // methods with no arguments.
                if( next.getName().equals(postConstructMethodName) &&
                    (next.getParameterTypes().length == 0) ) {
                    m = next;
                    injInfo.setPostConstructMethod(m);
                    break;
                }
            }
        }

        if( m == null ) {
            throw new InjectionException
                ("InjectionManager exception. PostConstruct method " +
                 injInfo.getPostConstructMethodName() +
                 " could not be found in class " +
                 injInfo.getClassName());
        }

        return m;
    }


    /**
     * <p>This is based off of code in <code>InjectionManagerImpl</code>.</p>
     * @param lifecycleMethod the method to invoke
     * @param instance the instanced to invoke the method against
     * @throws InjectionException if an error occurs
     */
     private void invokeLifecycleMethod(final Method lifecycleMethod,
                                       final Object instance)
        throws InjectionException {

        try {

            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Calling lifecycle method " +
                             lifecycleMethod + " on class " +
                             lifecycleMethod.getDeclaringClass());
            }

            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction() {
                    public java.lang.Object run() throws Exception {
                        if( !lifecycleMethod.isAccessible() ) {
                            lifecycleMethod.setAccessible(true);
                        }
                        lifecycleMethod.invoke(instance);
                        return null;
                    }
                });
        } catch( Exception t) {

                String msg = "Exception attempting invoke lifecycle "
                    + " method " + lifecycleMethod;
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, msg, t);
                }
                InjectionException ie = new InjectionException(msg);
                Throwable cause = (t instanceof InvocationTargetException) ?
                    t.getCause() : t;
                ie.initCause( cause );
                throw ie;

        }

        return;

    }


} // END GlassFishInjectionProvider
