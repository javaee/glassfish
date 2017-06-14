package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService
@Stateless
public class WebServiceEJB {

    public String sayHello(String who) {
        return "WebSvcTest-Hello " + who;
    }
}
