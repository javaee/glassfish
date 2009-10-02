package endpoint;

import javax.jws.WebService ;

@WebService(
    portName="SubtractNumbersPortType",
    serviceName="SubtractNumbersService",
    targetNamespace="http://duke.org",
    wsdlLocation="WEB-INF/wsdl/SubtractNumbers.wsdl",
    endpointInterface="endpoint.SubtractNumbersPortType")

public class SubtractNumbersImpl  {
    
    public int subtractNumbers (int number1, int number2) {
        return number1 - number2;
    }
}
