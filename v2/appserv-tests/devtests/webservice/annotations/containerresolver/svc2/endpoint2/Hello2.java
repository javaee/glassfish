package endpoint2;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Hello2",
    serviceName="Hello2Service",
    targetNamespace="http://example.com/Hello2"
)
public class Hello2 {
	public Hello2() {}

	@WebMethod(operationName="sayHello2", action="urn:SayHello2")
	public String sayHello2(String who) {
		return "WebSvcTest-Hello2 " + who;
	}
}
