package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;

@WebService
@Stateless
public class AddNumbersImpl {
    public int addNumbers(int n1, int n2) {
        System.out.println ("Received ejb add request for " + n1 + " and " + n2);
        return n1 + n2;
    }
}

