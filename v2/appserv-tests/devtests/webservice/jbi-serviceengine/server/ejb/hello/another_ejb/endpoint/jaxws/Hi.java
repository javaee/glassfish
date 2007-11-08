package endpoint.jaxws;

import javax.jws.WebService;

@WebService(
targetNamespace="http://endpoint/jaxws")
public interface Hi {

	public String sayHi(String who);
}
