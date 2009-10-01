package endpoint;

@javax.jws.WebService 
public class SubtractNumbersImpl  {
    
    public int subtractNumbers (int number1, int number2) {
        return number1 - number2;
    }
}
