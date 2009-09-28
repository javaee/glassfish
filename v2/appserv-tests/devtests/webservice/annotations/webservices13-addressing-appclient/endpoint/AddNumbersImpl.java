package endpoint;

@javax.xml.ws.BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
@javax.jws.WebService (wsdlLocation="WEB-INF/wsdl/AddNumbers.wsdl", serviceName="AddNumbersService", targetNamespace="http://duke.org", portName="AddNumbersPort", endpointInterface="endpoint.AddNumbersPortType")
public class AddNumbersImpl implements AddNumbersPortType {
    
    public int addNumbers (int number1, int number2) {
        return number1 + number2;
    }
}
