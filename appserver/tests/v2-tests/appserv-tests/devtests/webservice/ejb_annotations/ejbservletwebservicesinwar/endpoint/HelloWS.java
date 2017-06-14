package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;



@WebService(
    name="Hello",
    serviceName="HelloService",
    targetNamespace="http://example.com/Hello"
)
public class HelloWS {

	public HelloWS() {}

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) {
		return "Servlet WS:" + who;
	}
}
