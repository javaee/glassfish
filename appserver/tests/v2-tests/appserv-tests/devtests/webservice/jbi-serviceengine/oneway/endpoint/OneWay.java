package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService(
    name="OneWay",
    serviceName="OneWayService",
    targetNamespace="http://example.web.service/OneWay"
)
public class OneWay {
	public OneWay() {}

	@WebMethod(operationName="subtract", action="urn:Subtract")
	@Oneway
	public void subtract(int i, int j) {
		System.out.println("*** Inside subtract("+i+", "+j+")");
                int k = i -j ;
		if(i == 101)
			throw new RuntimeException("This is my exception in subtract ...");
	}

	@WebMethod(operationName="sayHi", action="urn:SayHi")
	@Oneway
	public void sayHi() {
                System.out.println("*** Hi from OneWay");
	}
}
