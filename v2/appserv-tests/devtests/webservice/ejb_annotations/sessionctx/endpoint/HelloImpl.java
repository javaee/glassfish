package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.ejb.SessionContext;
import java.security.Principal;

@WebService
@Stateless
public class HelloImpl {

    @Resource SessionContext ejbsc;

    public String sayHello(String who) {
	System.out.println("EJB WSCTXT wsc = " + ejbsc);
	if(ejbsc != null)
        	return "WebSvcTest-Hello " + ejbsc.getCallerPrincipal();
        return "EJB WebServiceContext injection failed";
    }
}
