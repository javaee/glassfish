package endpoint;

import java.util.Map;

import javax.jws.WebService;
import javax.ejb.Stateless;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@WebService(endpointInterface="endpoint.Hello")
@Stateless
public class HelloEJB implements Hello {

    @Resource WebServiceContext wsc;

    public String sayHello(String who) {
        Map<String, Object> msgCtxt = wsc.getMessageContext();
        return msgCtxt.toString();
    }

    @AroundInvoke
    private Object interceptBusinessMethod(InvocationContext invCtx) {
	try {
            System.out.println("ContextData" + invCtx.getContextData());
            //This is just to get the invocation trace
            //remove once bug is fixed
            Exception e = new Exception();
            e.printStackTrace();
            if (invCtx.getContextData() instanceof javax.xml.ws.handler.MessageContext){
                System.out.println("ContextDataMap is an instance of javax.xml.ws.handler.MessageContext ");

                return invCtx.proceed();
           } else {
                  return null;
           }
	} catch(Throwable t) { t.printStackTrace();}
        return null;
    }
}
