package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService
public class Hello {

   @WebMethod
   @Oneway
   public void sayHello(String name) {
     System.out.println( "Hello, " + name + "!");
   }
}
