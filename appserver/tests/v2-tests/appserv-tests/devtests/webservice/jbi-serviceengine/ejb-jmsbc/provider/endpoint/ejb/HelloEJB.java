package endpoint.ejb;

import javax.jws.WebService;
import javax.xml.ws.WebServiceRef;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.ejb.Hello", targetNamespace="http://endpoint/ejb")
@Stateless
public class HelloEJB implements Hello {

    public String sayHello(String who) {
	System.out.println("**** EJB Called");
        return "WebSvcTest-Hello " + who;
    }
}
