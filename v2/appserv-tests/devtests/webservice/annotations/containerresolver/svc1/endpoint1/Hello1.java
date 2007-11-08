package endpoint1;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Hello1",
    serviceName="Hello1Service",
    targetNamespace="http://example.com/Hello1"
)
public class Hello1 {
	public Hello1() {}

	@WebMethod(operationName="sayHello1", action="urn:SayHello1")
	public String sayHello1(String who) {
		return "WebSvcTest-Hello1 " + who;
	}
}
