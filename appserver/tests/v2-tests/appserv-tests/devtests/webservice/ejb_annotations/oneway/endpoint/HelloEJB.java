package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService(endpointInterface="endpoint.Hello")
@Stateless
public class HelloEJB implements Hello {

    public void sayHello(String who) {
        System.out.println("WebSvcTest-Hello " + who);
    }
}
