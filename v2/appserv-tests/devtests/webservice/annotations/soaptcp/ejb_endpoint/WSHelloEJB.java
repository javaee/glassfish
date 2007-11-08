package ejb_endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService
@Stateless
public class WSHelloEJB {

    public String sayEjbHello(String who) {
        return "WebSvcTest-EJB-Hello " + who;
    }
}
