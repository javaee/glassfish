package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.ejb.SessionContext;
import javax.annotation.Resource;



@javax.ejb.Stateless
@WebService(
    name="Hello",
    serviceName="HelloEjbService",
    targetNamespace="http://example.com/Hello"
)
public class HelloEjb {
@Resource
   private SessionContext sc;

	public HelloEjb() {}

	@WebMethod(operationName="sayHello", action="urn:SayHello")
	public String sayHello(String who) {
		return "EJB WS:" + who;
	}
}
