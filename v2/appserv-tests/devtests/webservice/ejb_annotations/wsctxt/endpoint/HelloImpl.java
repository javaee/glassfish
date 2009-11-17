package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.ejb.SessionContext;

@WebService
@Stateless
public class HelloImpl {

    @Resource WebServiceContext ejbsc;
//@Resource SessionContext sc;


    public String sayHello(String who) {
	System.out.println("EJB WSCTXT wsc = " + ejbsc);
	if(ejbsc != null) {
System.out.println(ejbsc.getMessageContext());
if (ejbsc.getMessageContext() instanceof javax.xml.ws.handler.MessageContext){
System.out.println("YYYYY" +ejbsc.getMessageContext().getClass());
}
else {
System.out.println("NNN " + ejbsc.getMessageContext().getClass());

}
        	return "WebSvcTest-Hello " + who;
        }
/*	if(sc != null) {
System.out.println(sc.getContextData().getClass());
System.out.println(sc.getContextData());

}*/
        return "EJB WebServiceContext injection failed";
    }
}
