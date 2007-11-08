package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.Hello")
@Stateless
@javax.jws.HandlerChain(name="some name", file="META-INF/myhandler.xml")
public class HelloEJB implements Hello {

    public String sayHello(String who) {
        return "WebSvcTest-Hello " + who;
    }
}
