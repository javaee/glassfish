package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Subtractor",
    serviceName="SubtractorService",
    targetNamespace="http://example.com/Subtractor"
)
public class Subtractor {
	static int count = 0;
	public Subtractor() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) {
                int k = i -j ;
		count++;
                System.out.println(i + "-" + j +" = " + k);
 		System.out.println("Count is: "+count);
		return k;
	}
}
