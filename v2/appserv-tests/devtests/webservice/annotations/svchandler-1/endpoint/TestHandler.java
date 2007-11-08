package endpoint;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.*;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;

public class TestHandler implements SOAPHandler<SOAPMessageContext> {
    
    public Set<QName> getHeaders() {
        return null;
    }
    
    String postConstString = "NOT_INITIALIZED";
    @PostConstruct
    public void init() {
        postConstString = "_HANDLER_PROPERLY_INITIALIZED";
    }

    @Resource(name="stringValue")
    String injectedString = "undefined";

    public boolean handleMessage(SOAPMessageContext context) {
        if ("_HANDLER_PROPERLY_INITIALIZED".equals(postConstString)) {
            System.out.println("postConstString = " + postConstString);
        } else {
            System.out.println("Handler PostConstruct not called property");
            System.out.println("postConstString = " + postConstString);
            return false;
        }            
        if ("undefined".equals(injectedString)) {
            System.out.println("Handler not injected property");
            return false;
        }
        System.out.println("injectedString = " + injectedString);
        try {
            SOAPMessageContext smc = (SOAPMessageContext) context;
            SOAPMessage message = smc.getMessage();
            SOAPBody body = message.getSOAPBody();
            
            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild().getFirstChild();
            int number = Integer.parseInt(paramElement.getValue());
            paramElement.setValue(String.valueOf(++number));
        } catch (SOAPException e) {
            e.printStackTrace();
        }
	System.out.println("VIJ's TEST HANDLER CALLED");
        return true;
    }
    
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }
    
    public void destroy() {}
    
    public void close(MessageContext context) {}
    
}
