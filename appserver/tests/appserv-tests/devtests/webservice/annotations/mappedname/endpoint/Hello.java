package endpoint;

import javax.jws.WebService;

@WebService
public class Hello {
	
    public String sayHello(String param) {
	return "Hello " + param;
    }
}
