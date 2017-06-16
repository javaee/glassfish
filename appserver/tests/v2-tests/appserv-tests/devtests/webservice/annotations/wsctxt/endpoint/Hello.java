package endpoint;

import javax.jws.WebService;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;


@WebService
public class Hello {
	
    @Resource WebServiceContext wsc;

    public String sayHello(String param) {
	System.out.println("wsctxt-servlet wsc = " + wsc);
	if(wsc != null) {
                 ServletContext sc =
(ServletContext)wsc.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
                String a = null;
                if (sc!= null ) {
                 a = sc.getServletContextName();
                }
		return "Hello " + param +a;
        }
	return "WebService Context injection failed";
    }
}
