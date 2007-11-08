package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService
public interface Hello {

        @WebMethod
        @Oneway
	public void sayHello(String who);
}
