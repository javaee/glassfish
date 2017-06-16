package endpoint;

import javax.jws.WebService;

@WebService(
    name="Hello",
    serviceName="HelloService",
    portName="Hello"
)
public class Hello2 {
	
    public String sayHello(String param) {
	return "WebSvcTest-Hello " + param;
    }
}
