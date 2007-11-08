package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.web.service/Calculator"
)
public class Calculator {
	public Calculator() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) throws Exception {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
		throw new java.lang.Exception("This is my exception..."); 
		//return k;
	}
/*
	@WebMethod(operationName="subtract", action="urn:Subtract")
	@Oneway
	public void subtract(int i, int j) {
                int k = i -j ;
                System.out.println(i + "-" + j +" = " + k);
		if(i == 101)
			throw new RuntimeException("This is my exception in subtract ...");
	}
*/
}
