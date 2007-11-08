package endpoint.jaxws;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.jaxws.Hi",targetNamespace="http://endpoint/jaxws")
@Stateless
public class HiEJB implements Hi {

    public String sayHi(String who) {
	System.out.println("In EJB2");
        return "WebSvcTest-Hi" + who;
    }
}
