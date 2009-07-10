package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.web.service/Calculator"
)
public class Calculator {
	public Calculator() {}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
 
		return k;
	}
}
