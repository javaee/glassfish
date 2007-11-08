package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

@WebService
@Stateless
public class HelloImpl {

    @Resource WebServiceContext ejbsc;

    public String sayHello(String who) {
	System.out.println("EJB WSCTXT wsc = " + ejbsc);
	if(ejbsc != null)
        	return "WebSvcTest-Hello " + who;
        return "EJB WebServiceContext injection failed";
    }
}
