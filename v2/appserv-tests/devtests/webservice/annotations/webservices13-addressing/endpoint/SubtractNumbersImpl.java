package endpoint;

@javax.jws.WebService (serviceName="junkjunkjunk", portName="SubtractNumbersPortType", endpointInterface="endpoint.SubtractNumbersPortType")
public class SubtractNumbersImpl implements SubtractNumbersPortType {
    
    public int subtractNumbers (int number1, int number2) {
        return number1 - number2;
    }
}
