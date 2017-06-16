package endpoint.jaxws;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.jaxws.Hello", targetNamespace="http://endpoint/jaxws")
@Stateless
public class HelloEJB implements Hello {

    public String sayHello(String who) {
        return "WebSvcTest-Hello " + who;
    }
}
