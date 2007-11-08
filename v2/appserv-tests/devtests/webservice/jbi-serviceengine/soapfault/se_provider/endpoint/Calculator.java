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

    
    @WebMethod(operationName="throwRuntimeException", action="urn:ThrowRuntimeException")
    public String throwRuntimeException(String name) {
        String exceptionMsg = "Calculator :: Threw Runtime Exception";
        System.out.println(exceptionMsg);
        throw new RuntimeException(exceptionMsg);
    }

    @WebMethod(operationName="throwApplicationException", action="urn:ThrowApplicationException")
    public String throwApplicationException(String name) throws Exception {
        String exceptionMsg = "Calculator :: Threw Application Exception";
        System.out.println(exceptionMsg);
        throw new Exception(exceptionMsg);
    }
}
