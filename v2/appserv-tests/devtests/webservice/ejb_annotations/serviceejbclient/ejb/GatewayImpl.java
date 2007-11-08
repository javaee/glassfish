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
@WebServiceRef(name="service/helloservice", type=HelloImplService.class)
public class GatewayImpl {

    @WebServiceRef
    HelloImplService serviceField;

    HelloImplService serviceMethod=null;

    // method injection has to be private to avoid being part of the 
    // web service interface.
    @WebServiceRef()
    private void setService(HelloImplService service) {
	serviceMethod = service;
    }

    @WebMethod
    public String invokeMethod(String who) {
        HelloImpl port = serviceMethod.getPort(HelloImpl.class);       
	return "METHOD " + port.sayHello(who);
    }

    @WebMethod
    public String invokeField(String who) {
        HelloImpl port = serviceField.getPort(HelloImpl.class);        
        return "FIELD " + port.sayHello(who);
    }

    @WebMethod
    public String invokeDependency(String who) {
        String result = null;
	try {
		Context ic = new InitialContext();
		HelloImplService service = (HelloImplService) ic.lookup("java:comp/env/service/helloservice");
		HelloImpl port = service.getPort(HelloImpl.class);
                result = "JNDI " + port.sayHello(who);
		System.out.println(result);
	} catch(Throwable t) {
		t.printStackTrace();
		return "FAILED";
	}
        return result;
    }

}
