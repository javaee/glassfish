package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="MyHello",
    serviceName="MyService",
    targetNamespace="http://example.com/Hello"
)
public class Hello {
	public Hello() {}

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) throws MyException {
		if(who == null)
			throw new MyException("INPUT IS NULL");
		else
			return "WebSvcTest-Hello " + who;
	}
}
