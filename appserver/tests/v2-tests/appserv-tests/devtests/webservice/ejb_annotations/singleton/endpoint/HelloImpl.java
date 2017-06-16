package endpoint;

import javax.jws.WebService;
import javax.ejb.Singleton;
import outsidepkg.RetVal;

@WebService
@Singleton
public class HelloImpl {

    public RetVal sayHello(String who) {
        return new RetVal("WebSvcTest-Hello " + who);
    }
}
