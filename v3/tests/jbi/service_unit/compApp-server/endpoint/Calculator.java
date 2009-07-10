package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.com/Calculator"
)
public class Calculator {
	public Calculator() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
 
		return k;
	}
}
