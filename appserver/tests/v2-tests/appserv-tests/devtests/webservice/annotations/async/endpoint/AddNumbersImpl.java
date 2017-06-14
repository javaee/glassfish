package endpoint;

import javax.jws.WebService;

@WebService
public class AddNumbersImpl {
    public int addNumbers(int n1, int n2) {
        System.out.println ("Received add request for " + n1 + " and " + n2);
        return n1 + n2;
    }
}
