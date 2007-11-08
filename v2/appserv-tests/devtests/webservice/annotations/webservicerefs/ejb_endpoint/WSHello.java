package ejb_endpoint;

import javax.jws.WebService;

@WebService
public interface WSHello {

	public String sayEjbHello(String who);
}
