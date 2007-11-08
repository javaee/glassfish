package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.annotation.PostConstruct;

@WebService(
    name="Hello",
    serviceName="HelloService",
    targetNamespace="http://example.com/Hello"
)
public class Hello {
        String str = "postconstruct NOT called";

	public Hello() {}

        @PostConstruct
        public void postConstMethod() { str = "postconstruct called"; }

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) {
		return "WebSvcTest-Hello " + who + " " + str;
	}
}
