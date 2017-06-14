package endpoint;

import javax.jws.WebService;

@WebService
public interface Hello {

	public String sayHello(String who);
}
