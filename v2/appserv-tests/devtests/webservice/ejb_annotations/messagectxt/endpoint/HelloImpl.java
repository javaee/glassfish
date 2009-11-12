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


    public String sayHello(String who) {
	if(ejbsc != null) {
           System.out.println(ejbsc.getMessageContext());
           if (ejbsc.getMessageContext() instanceof javax.xml.ws.handler.MessageContext){
              //System.out.println("YYYYY" +ejbsc.getMessageContext().getClass());
        	return "WebSvcTest-Hello " + who;
}
           else {
             // System.out.println("NNN " + ejbsc.getMessageContext().getClass());
              return "EJB MSGContext injection failed";

}
        }
        return "EJB MSGContext injection failed";
    }
}
