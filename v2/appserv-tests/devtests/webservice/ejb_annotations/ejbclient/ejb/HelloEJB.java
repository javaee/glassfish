
package ejb;

import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;

import endpoint.WebServiceEJBService;
import endpoint.WebServiceEJB;

@Stateless 
public class HelloEJB implements Hello {


   @WebServiceRef
   WebServiceEJBService webService;
 
    public String invoke(String string) {
        System.out.println("invoked with " + string); 
	System.out.println("getting the port now from " + webService);
	WebServiceEJB ejb = webService.getWebServiceEJBPort();
	System.out.println("got " + ejb);
	return ejb.sayHello(string);
   }
}
