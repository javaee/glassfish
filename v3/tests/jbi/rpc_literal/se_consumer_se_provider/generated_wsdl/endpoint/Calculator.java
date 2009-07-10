package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;
import common.IncomeTaxDetails;
import java.util.Hashtable;
import javax.jws.soap.SOAPBinding;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.web.service/Calculator"
)
@SOAPBinding(style=SOAPBinding.Style.RPC, use=SOAPBinding.Use.LITERAL)
public class Calculator {

	public static final String testName = "\nTest :: rpc-literal-generated-wsdl : ";
	public Calculator() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) throws Exception {
                int k = i +j ;
                System.out.println(testName + i + "+" + j +" = " + k);
		//throw new java.lang.Exception("This is my exception"); 
		return k;
	}

	@WebMethod(operationName="calculateIncomeTax", action="urn:CalculateIncomeTax")
	public long calculateIncomeTax(IncomeTaxDetails details
			 , IncomeTaxDetails details2
			 , IncomeTaxDetails details3
			 , IncomeTaxDetails details4
			 , IncomeTaxDetails details5
			 , IncomeTaxDetails details6
			 , IncomeTaxDetails details7
			 , IncomeTaxDetails details8
			 , IncomeTaxDetails details9
			 , IncomeTaxDetails details10
			) {
		long income = details.annualIncome;
		System.out.println(testName + "Annual income = " + income);
		long taxRate = 30; // 30%
		long taxToBePaid = income / taxRate;
		System.out.println(testName +"Tax to be paid = " + taxToBePaid);
		return taxToBePaid;
	}

	@WebMethod(operationName="sayHi", action="urn:SayHi")
	public String sayHi() {
		return testName + "Hi from sayHi()";
	}

	@WebMethod(operationName="printHi", action="urn:PrintHi")
	public void printHi() {
		System.out.println(testName +"Hi from printHi()");
	}

	@WebMethod(operationName="printHiToMe", action="urn:PrintHiToMe")
	public void printHiToMe(String name) {
		System.out.println(testName +"Hi to " + name + " from printHiToMe()");
	}
}
