package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;
import outsidepkg.RetVal;

@WebService
@Stateless
public class HelloImpl {

    public RetVal sayHello(String who) {
        return new RetVal("WebSvcTest-Hello " + who);
    }
}
