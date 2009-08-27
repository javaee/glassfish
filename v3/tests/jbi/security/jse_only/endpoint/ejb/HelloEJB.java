package endpoint.ejb;

import java.security.Principal;
import javax.jws.WebService;
import javax.xml.ws.WebServiceRef;
import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

@WebService(endpointInterface="endpoint.ejb.Hello", targetNamespace="http://endpoint/ejb")
@Stateless
public class HelloEJB implements Hello {

    @Resource private SessionContext ctx;

    public String sayHello(String who) {
	System.out.println("**** sayHello("+ who+")");
	Principal p = ctx.getCallerPrincipal();
	String principal = (p == null)? "NULL": p.getName(); //p.toString();
	System.out.println("****EJB: principal = " + principal);
 	return "JBI-SecurityTest " + who + " PrincipalGot="+principal;
    }
}
