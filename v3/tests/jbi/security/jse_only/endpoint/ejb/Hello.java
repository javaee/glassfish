package endpoint.ejb;

import javax.jws.WebService;

@WebService(
targetNamespace="http://endpoint/ejb")
public interface Hello {
	public String sayHello(String who);
}
