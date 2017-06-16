package ejb_endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="ejb_endpoint.WSHello")
@Stateless
public class WSHelloEJB implements WSHello {

    public String sayEjbHello(String who) {
        return "WebSvcTest-EJB-Hello " + who;
    }
}
