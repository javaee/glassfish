package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

import outsidepkg.RetVal;

@WebService(
    name="Hello",
    serviceName="HelloService",
    targetNamespace="http://example.com/Hello"
)
public class Hello {
	public Hello() {}

	public RetVal sayHello(String who) {
		return new RetVal("WebSvcTest-Hello " + who);
	}
}
