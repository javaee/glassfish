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
        return msgCtxt.get("intVal") + "WebSvcTest-Hello " + who + msgCtxt.get("longVal");
    }

    @AroundInvoke
    private Object interceptBusinessMethod(InvocationContext invCtx) {
	try {
           Map<String, Object> map = invCtx.getContextData();
           map.put("intVal", new Integer(45));
           map.put("longVal", new Long(1234));
           return invCtx.proceed();
	} catch(Throwable t) {}
        return null;
    }
}
