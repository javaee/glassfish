/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.osgicdi.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.InjectionPoint;

import org.glassfish.osgicdi.OSGiService;
import org.glassfish.osgicdi.ServiceUnavailableException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A simple Service Factory class that provides the ability to obtain/get 
 * references to a service implementation (obtained from a service registry) 
 * and also provides a mechanism to unget or return a service after its usage
 * is completed.
 * 
 * @author Sivakumar Thyagarajan
 */
class OSGiServiceFactory {
    private static final boolean DEBUG_ENABLED = true;

    /**
     * Get a reference to the service of the provided <code>Type</code>
     * @throws ServiceUnavailableException 
     */
    public static Object getService(final InjectionPoint svcInjectionPoint) 
                                    throws ServiceUnavailableException{
        final OSGiService os = 
            svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);
        debug("getService " + svcInjectionPoint.getType() + " OS:" + os);
        Object instance = createServiceProxy(svcInjectionPoint); 
        return instance;
    }

    private static Object createServiceProxy(
                                    final InjectionPoint svcInjectionPoint) 
                                    throws ServiceUnavailableException {
        Type serviceType = svcInjectionPoint.getType();
        final OSGiService os = 
            svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);

        InvocationHandler proxyInvHndlr = os.dynamic() ? new DynamicInvocationHandler(os, svcInjectionPoint)
                : new StaticInvocationHandler(os, svcInjectionPoint);
        
        Object instance =  Proxy.newProxyInstance(
                            Thread.currentThread().getContextClassLoader(), 
                            new Class[]{(Class)serviceType}, 
                            proxyInvHndlr);
        return instance;
    }

    private static Object lookupService(InjectionPoint svcInjectionPoint) 
                        throws ServiceUnavailableException {
        Type serviceType = svcInjectionPoint.getType();
        final OSGiService os = 
            svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);
        debug("lookup service" + serviceType);
        
        //Get the bundle context from the classloader that loaded the annotation
        //element
        Class annotatedElt = svcInjectionPoint.getMember().getDeclaringClass();
        BundleContext bc = BundleReference.class
                            .cast(annotatedElt.getClassLoader())
                            .getBundle().getBundleContext();
        
        //Create the service tracker for this type.
        debug("creating service tracker for " + ((Class)(serviceType)).getName() 
                                            + " using bundle-context:" + bc);
        ServiceTracker st = null;
        try {
            Class svcTypeClazz = (Class) serviceType;
            if (os.serviceCriteria().trim().equals("")) {
                st = new ServiceTracker(bc, svcTypeClazz.getName(), null);
            } else {
                Filter f = bc.createFilter(getFilter(svcTypeClazz, os));
                st = new ServiceTracker(bc, f, null);
            }
            
            st.open();
            
            Object service = ((os.waitTimeout() == -1) 
                                    ? st.getService() 
                                    : st.waitForService(os.waitTimeout()));
            debug("service obtained from tracker" + service);
            if (service == null) {
                throw new ServiceUnavailableException(
                        "Service " + (((Class)serviceType).getName()) + " Unavailable", 
                        ServiceException.SUBCLASSED, null);
            } 
            return service;
        } catch (InvalidSyntaxException ise) {
            ise.printStackTrace();
            throw new ServiceUnavailableException("Invalid Filter specification", 
                    ServiceException.FACTORY_EXCEPTION, ise);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ServiceUnavailableException("" +
            		"Service " + (((Class)serviceType).getName()) + " Unavailable", 
            		ServiceException.SUBCLASSED, e);
        } finally {
			if (st != null) st.close();
        }
    }

    private static String getFilter(Class serviceType, OSGiService os) {
        String objectClassClause = "(" + Constants.OBJECTCLASS + "=" + serviceType.getName() + ")"; 
        String filter = "(&" +  objectClassClause +  os.serviceCriteria() + ")";
        debug("filter = " + filter);
        return filter;
    }

    /**
     * Unget the service
     */
    public static void ungetService(Object serviceInstance, 
                                    InjectionPoint svcInjectionPoint){
        //XXX: not implemented
    }
    
    private static void debug(String string) {
        if(DEBUG_ENABLED)
            System.out.println("OSGiServiceFactory:: " + string);
    }

    /**
     * If the service is marked as dynamic, when a method is invoked on a
     * a service proxy, an attempt is made to get a reference to the service
     * and then the method is invoked on the newly obtained service.
     * This scheme should work for statless and/or idempotent service
     * implementations that have a dynamic lifecycle that is not linked to
     * the service consumer [service dynamism]
     *
     */
    private static class DynamicInvocationHandler implements InvocationHandler {
        /*
         * TODO: we should track the lookedup service and
         * only if it goes away should we look up a service.
         */

        private final OSGiService os;
        private final InjectionPoint svcInjectionPoint;

        public DynamicInvocationHandler(OSGiService os, InjectionPoint svcInjectionPoint) {
            debug("In DynamicInvocationHandler");
            this.os = os;
            this.svcInjectionPoint = svcInjectionPoint;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            debug ("looking a service as this is set to DYNAMIC=true");
            final Object instanceToUse =  lookupService(svcInjectionPoint);
            debug("calling Method " + method + " on proxy");
            return method.invoke(instanceToUse, args);
        }
    }
    
    /**
     * If the service is marked as static, an attempt is made to get a reference to the service
     * when the injection point is resolved.
     */
    private static class StaticInvocationHandler implements InvocationHandler {
        private final OSGiService os;
        private final InjectionPoint svcInjectionPoint;
        
        private ServiceReference svcReference = null;
        private BundleContext bundleContext = null;

        public StaticInvocationHandler(OSGiService os, InjectionPoint svcInjectionPoint) {
            debug("In StaticInvocationHandler");
            this.os = os;
            this.svcInjectionPoint = svcInjectionPoint;
            
            //Get one service reference when the proxy is created
            this.bundleContext = getBundleContext(svcInjectionPoint);
            getServiceReference(svcInjectionPoint);
        }
        
        private BundleContext getBundleContext(InjectionPoint svcInjectionPoint) {
            Class annotatedElt = svcInjectionPoint.getMember().getDeclaringClass();
            BundleContext bc = BundleReference.class
                                .cast(annotatedElt.getClassLoader())
                                .getBundle().getBundleContext();
            return bc;
        }
        
        private void getServiceReference(InjectionPoint svcInjectionPoint){
            Type serviceType = svcInjectionPoint.getType();
            debug("lookup service" + serviceType);
            
            //Create the service tracker for this type.
            debug("creating service tracker for " + ((Class)(serviceType)).getName() 
                                                + " using bundle-context:" + this.bundleContext);
            ServiceTracker st = null;
            try {
                Class svcTypeClazz = (Class) serviceType;
                if (os.serviceCriteria().trim().equals("")) {
                    st = new ServiceTracker(this.bundleContext, svcTypeClazz.getName(), null);
                } else {
                    Filter f = this.bundleContext.createFilter(getFilter(svcTypeClazz, os));
                    st = new ServiceTracker(this.bundleContext, f, null);
                }
                st.open();

                //If wait timeout is specified wait for the specified timeout
                if (os.waitTimeout() != -1) {
                    st.waitForService(os.waitTimeout());
                } 
                this.svcReference = st.getServiceReference(); 
                if (this.svcReference == null) {
                    debug("ServiceReference obtained from ServiceTracker is " +
                    		"null. No matching services available at this point");
                    //No service at this point
                    throwServiceUnavailable();
                } 
                debug("ServiceReference obtained from tracker:" + this.svcReference);
            } catch (InvalidSyntaxException ise) {
                ise.printStackTrace();
                throw new ServiceUnavailableException("Invalid Filter specification", 
                        ServiceException.FACTORY_EXCEPTION, ise);
            } catch (InterruptedException e) {
                //Another thread interupted our wait for a service
                e.printStackTrace();
                throwServiceUnavailable();
            } finally {
                if (st != null) st.close();
                //close ServiceTracker as we are not going to use it anymore.
                //We track if the service is available manually during every
                //method invocation in the invoke method.
            }
            
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (this.svcReference == null) {
                //Earlier invocation has discovered that this service is unavailable
                //so throw a service unavailable
                throwServiceUnavailable();
            } else {
                //Attempt to get a service based on the original ServiceReference
                //obtained at instantiation time.
                Object instanceToUse = this.bundleContext.getService(this.svcReference);
                if (instanceToUse == null) {
                    //Service has vanished, so clear reference and throw svc 
                    //unavailable
                    
                    //clear service reference, so that subsequence invocations 
                    //continue to throw ServiceUnavailable without needing 
                    //to check status again.
                    this.svcReference = null;
                    throwServiceUnavailable();
                } 
                debug ("Using the service that was looked up earlier" +
                        " as this is set to DYNAMIC=false");
                debug("Calling Method " + method + " on Proxy");
                return method.invoke(instanceToUse, args);
            }
            return null;
        }

        private void throwServiceUnavailable() {
            Type serviceType = svcInjectionPoint.getType();
            throw new ServiceUnavailableException("Service "
                    + (((Class) serviceType).getName()) + " Unavailable",
                    ServiceException.SUBCLASSED, null);
        }

    }
    
}
