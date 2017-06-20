package endpoint.jaxws;

import javax.jws.WebService;

@WebService(
targetNamespace="http://endpoint/jaxws")
public interface Hello {

	public String sayHello(String who);
}
