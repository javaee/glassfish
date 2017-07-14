package ejb;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.ejb.Stateless;

import javax.xml.ws.WebServiceRef;
import javax.naming.Context;
import javax.naming.InitialContext;


import endpoint.HelloImplService;
import endpoint.HelloImpl;

@WebService
@Stateless
//@WebServiceRef(name="service/helloservice", type=HelloImplService.class)
@WebServiceRef(name="service/helloport", type=HelloImpl.class, value=HelloImplService.class)
public class GatewayImpl {

    @WebServiceRef(HelloImplService.class)
    HelloImpl portField;

    HelloImpl portMethod=null;

    // method injection has to be private to avoid being part of the 
    // web service interface.
    @WebServiceRef(HelloImplService.class)
    private void setPort(HelloImpl port) {
	portMethod = port;
    }

    @WebMethod
    public String invokeMethod(String who) {
	return "METHOD " + portMethod.sayHello(who);
    }

    @WebMethod
    public String invokeField(String who) {
        return "FIELD " + portField.sayHello(who);
    }

    @WebMethod
    public String invokeDependency(String who) {
	try {
		Context ic = new InitialContext();
//		HelloImplService service = (HelloImplService) ic.lookup("java:comp/env/service/helloservice");
//		HelloImpl port = service.getPort(HelloImpl.class);
//		System.out.println("From service... " + port.sayHello(who));
		HelloImpl port = (HelloImpl) ic.lookup("java:comp/env/service/helloport");
		return "JNDI " + port.sayHello(who);
	} catch(Throwable t) {
		t.printStackTrace();
		return "FAILED";
	}
    }

}
