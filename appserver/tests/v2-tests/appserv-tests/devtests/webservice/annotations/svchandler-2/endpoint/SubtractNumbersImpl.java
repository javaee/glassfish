package endpoint;

@javax.jws.HandlerChain(name="some name", file="myhandler.xml")
@javax.jws.WebService (serviceName="junkjunkjunk", endpointInterface="endpoint.SubtractNumbersPortType")
public class SubtractNumbersImpl implements SubtractNumbersPortType {
    
    public int subtractNumbers (int number1, int number2) {
System.out.println("I got n1 = " + number1 + " and n2 = " + number2);
        return number1 - number2;
    }
}
