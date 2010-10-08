package test.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import test.fwk.FrameworkService;
import test.fwk.SomeFwkServiceImpl;
import test.fwk.SomeFwkServiceInterface;

public class FrameworkServiceFactory {
    public static Object getService(final Type type, final FrameworkService fs){
        //NOTE:hard-coded for this test, but ideally should get the
        //service implementation from the service registry
        SomeFwkServiceInterface instance = 
            (SomeFwkServiceInterface) lookupService(type, fs.waitTimeout());
        
        if (fs.dynamic()) {
            InvocationHandler ih = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    System.out.println("****************** Method " + method + " called on proxy");
                    return method.invoke(lookupService(type, fs.waitTimeout()), args);
                }
            };
            instance = (SomeFwkServiceInterface) 
            Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
                    new Class[]{SomeFwkServiceInterface.class}, ih); 
        }
        return instance;
    }
    
    private static Object lookupService(Type type, int waitTimeout) {
        if (type.equals(SomeFwkServiceInterface.class)){ 
            return new SomeFwkServiceImpl("test");
        }
        return null;
    }

    public static void ungetService(Object serviceInstance){
        //unget the service instance from the service registry
    }

}
