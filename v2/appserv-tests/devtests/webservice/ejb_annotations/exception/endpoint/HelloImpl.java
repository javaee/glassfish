package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService
@Stateless
public class HelloImpl {

    public String sayHello(String who) {
	throw new RuntimeException("I am a bad bad ejb endpoint");
    }
}
