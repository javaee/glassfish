package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Hello",
    serviceName="HelloService",
    targetNamespace="http://example.com/Hello",
    wsdlLocation="http://localhost:8080/wsdl/HelloService.wsdl"
)
public class Hello {
	public Hello() {}

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) {
		return "WebSvcTest-Hello " + who;
	}
}
