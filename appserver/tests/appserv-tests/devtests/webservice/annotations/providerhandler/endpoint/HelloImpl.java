/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
