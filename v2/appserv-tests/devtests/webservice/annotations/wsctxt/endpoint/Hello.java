package endpoint;

import javax.jws.WebService;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

@WebService
public class Hello {
	
    @Resource WebServiceContext wsc;

    public String sayHello(String param) {
	System.out.println("wsctxt-servlet wsc = " + wsc);
	if(wsc != null)
		return "WebSvcTest-Hello " + param;
	return "WebService Context injection failed";
    }
}
