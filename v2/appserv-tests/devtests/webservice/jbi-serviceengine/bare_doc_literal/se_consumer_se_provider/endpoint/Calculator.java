package endpoint;

import javax.jws.*;
import common.IncomeTaxDetails;
import java.util.Hashtable;
import javax.jws.soap.SOAPBinding;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.web.service/Calculator"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class Calculator {
	public Calculator() {}

	@WebMethod
	public int add(
			/*
			@WebParam(name = "number1", targetNamespace = "http://example.web.service/Calculator", partName = "part1")
			int i , */
			@WebParam(name = "number2", targetNamespace = "http://example.web.service/Calculator", partName = "part2")
			int j
			) throws Exception {
				int i = 50;
                int k = i +j ;
                System.out.println("JBI Test :: bare-rpc-literal se_consumer_se_provider : " + i + "+" + j +" = " + k);
		return k;
	}

}
