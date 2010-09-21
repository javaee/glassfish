package org.glassfish.osgicdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.InjectionPoint;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.util.tracker.ServiceTracker;

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
        final OSGiService os = svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);
        debug("getService " + svcInjectionPoint.getType() + " OS:" + os);
        Object instance = createServiceProxy(svcInjectionPoint); 
        return instance;
    }

    private static Object createServiceProxy(
            final InjectionPoint svcInjectionPoint) throws ServiceUnavailableException {
        Type serviceType = svcInjectionPoint.getType();
        final OSGiService os = svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);

        //Get one service instance when the proxy is created
        final Object svcInstance = lookupService(svcInjectionPoint);
        InvocationHandler proxyInvHndlr = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                Object instanceToUse = svcInstance;
                if (os.dynamic()) {
                    //If the service is marked as dynamic, when a method is invoked on a 
                    //a service proxy, an attempt is made to get a reference to the service 
                    //and then the method is invoked on the newly obtained service.
                    //This scheme should work for statless and/or idempotent service 
                    //implementations that have a dynamic lifecycle that is not linked to
                    //the service consumer [service dynamism]
                    //TODO: we should track the lookedup service and 
                    //only if it goes away should we look up a service
                    debug ("looking a service as this is set to DYNAMIC=true");
                    instanceToUse =  lookupService(svcInjectionPoint);
                } else {
                    debug ("using the service that was looked up earlier" +
                    		" as this is set to DYNAMIC=false");
                }
                debug("calling Method " + method + " on proxy");
                return method.invoke(instanceToUse, args);
            }
        };
        
        Object instance =  Proxy.newProxyInstance(
                            Thread.currentThread().getContextClassLoader(), 
                            new Class[]{(Class)serviceType}, 
                            proxyInvHndlr);
        return instance;
    }

    private static Object lookupService(InjectionPoint svcInjectionPoint) 
                        throws ServiceUnavailableException {
        Type serviceType = svcInjectionPoint.getType();
        final OSGiService os = svcInjectionPoint.getAnnotated().getAnnotation(OSGiService.class);
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
        ServiceTracker st = 
            new ServiceTracker(bc, ((Class)(serviceType)).getName(), null);
        st.open();
        try {
            Object service = ((os.waitTimeout() == -1) 
                                    ? st.getService() 
                                    : st.waitForService(os.waitTimeout()));
            debug("service obtained from tracker" + service);
            if (service == null) {
                throw new ServiceUnavailableException();
            } 
            return service;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
            System.out.println("ServiceFactory:: " + string);
    }
    
    public static class ServiceUnavailableException extends Exception {
        private static final long serialVersionUID = 4055254962336930137L;
    }
    
}
