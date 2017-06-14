package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.ejb.SessionContext;
import javax.annotation.Resource;



@javax.ejb.Stateless
@WebService(serviceName = "HelloService", portName = "HelloPort", targetNamespace = "http://example.com/Hello", endpointInterface = "com.example.hello.Hello", wsdlLocation = "WEB-INF/wsdl/HelloService.wsdl")

public class Hello {
@Resource
   private SessionContext sc;

	public Hello() {}

	public String sayHello(String who) {
		return "WebSvcTest-Hello " + who;
	}
}
