package test.webservice;

import java.rmi.*;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

public class WebServiceTestImpl implements ServiceLifecycle, WebServiceTest {
 
    WebServiceTest delegate;
    
    public void destroy() {
        System.out.println("Driver servlet destroyed");
    }
    
    public void init(Object context) {
        ServletEndpointContext seContext = (ServletEndpointContext) context;
        String testClassName = seContext.getServletContext().getInitParameter("testclassname");
        if (testClassName==null) {
            System.out.println("Error : no delegate servlet provided for test");
            return;
        }
        try {
            Class clazz = Class.forName(testClassName);
            if (clazz==null) {
                System.out.println("Error : cannot load delegate " + testClassName);
                return;
            }
            Object o = clazz.newInstance();
            if (o instanceof WebServiceTest) {
             delegate = (WebServiceTest) o;
            } else {
             System.out.println("Error : delegate not of type WebServiceTest");
            }
        } catch(ClassNotFoundException cnfe) {
            System.out.println("Error : cannot load delegate " + testClassName);
        } catch(InstantiationException ie) {
            System.out.println("Error : cannot instantiate " + testClassName);
        } catch(Exception e) {
            System.out.println("Error : cannot load delegate " + testClassName + " " + e.getMessage());
        }
    }
    
    public String doTest(String[] params) throws RemoteException {
        
        if (delegate!=null) {
            return delegate.doTest(params);
        } else {
            throw new RemoteException("No delegate for test harness");
        }
    }    
}