package endpoint;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.com/Calculator"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class Calculator {
	public Calculator() {}

	@WebMethod
	public int add(
			@WebParam(name = "number1", targetNamespace = "http://example.com/Calculator", partName = "part1")
			int j
			) throws Exception {
				int i = 500;
                int k = i +j ;
                System.out.println("JBI Test :: bare-rpc-literal bc_consumer_se_provider : " + i + "+" + j +" = " + k);
		return k;
	}
}
