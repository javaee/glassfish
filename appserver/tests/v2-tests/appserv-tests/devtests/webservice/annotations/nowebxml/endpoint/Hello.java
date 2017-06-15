package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Hello",
    serviceName="HelloService",
    targetNamespace="http://example.com/Hello"
)
public class Hello {
	public Hello() {}

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) {
		return "WebSvcTest-Hello " + who;
	}

	@WebMethod(operationName="sayDoubleHello", action="urn:SayHello")
	public String sayDoubleHello(String who) {
		return "WebSvcTest-Double-Hello " + who + " AND " + who;
	}
}
