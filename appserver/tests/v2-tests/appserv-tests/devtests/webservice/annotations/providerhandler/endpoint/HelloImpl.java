package endpoint;

import java.io.*;
import java.util.Map;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.Service	;	
import javax.xml.soap.SOAPMessage;
import javax.ejb.Stateless;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;


import endpoint.jaxws.*;

@WebServiceProvider(serviceName="HelloImplService", portName="HelloImpl", targetNamespace="http://endpoint/jaxws", wsdlLocation="HelloImplService.wsdl")
@javax.jws.HandlerChain(name="some name", file="WEB-INF/myhandler.xml")
public class HelloImpl implements Provider<Source> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int combo;
    private int bodyIndex;
    
    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }
    
    
    public Source invoke(Source request) {
        try {
            recvBean(request);
            return sendBean();
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);            
        }
    }
    
    private void recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        System.out.println(jaxbContext.createUnmarshaller().unmarshal(source));
        JAXBElement element = (JAXBElement) jaxbContext.createUnmarshaller().unmarshal(source);
        System.out.println("name="+element.getName()+ " value=" + element.getValue());        
        if (element.getValue() instanceof SayHello) {
            SayHello hello = (SayHello) element.getValue(); 
            System.out.println("Say Hello from " + hello.getArg0());
        }
        
    }

    private Source sendBean() throws Exception {
        System.out.println("**** sendBean ******");
        SayHelloResponse resp = new SayHelloResponse();
        resp.setReturn("WebSvcTest-Hello");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectFactory factory = new ObjectFactory(); 
        jaxbContext.createMarshaller().marshal(factory.createSayHelloResponse(resp), bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }
    
}
