package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService(
    name="AddNumber",
    serviceName="AddNumberService",
    targetNamespace="http://example.web.service/AddNumber"
)
public class AddNumber {
	public AddNumber() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) throws Exception {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
		//throw new java.lang.Exception("This is my exception"); 
		return k;
	}
}
