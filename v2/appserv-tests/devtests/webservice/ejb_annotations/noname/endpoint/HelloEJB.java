package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.Hello")
@Stateless
public class HelloEJB implements Hello {

    public String sayHello(String who) {
        return "WebSvcTest-Hello " + who;
    }
}
