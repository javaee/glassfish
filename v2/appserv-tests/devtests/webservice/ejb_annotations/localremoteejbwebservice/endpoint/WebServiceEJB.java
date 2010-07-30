package endpoint;

import javax.jws.WebService;
import javax.ejb.*;

@WebService
@Stateless
@Local(NewSessionBeanLocal.class)
@Remote(NewSessionBeanRemote.class)

public class WebServiceEJB implements NewSessionBeanRemote, NewSessionBeanLocal {

    public String sayHello(String who) {
        return "WebSvcTest-Hello " + who;
    }
}
