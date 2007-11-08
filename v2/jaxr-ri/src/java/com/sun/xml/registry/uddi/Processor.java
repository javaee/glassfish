/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


package com.sun.xml.registry.uddi;

import javax.xml.registry.*;
import java.lang.reflect.*;
import java.io.StringWriter;
import java.util.*;

import com.sun.xml.registry.uddi.*;
import com.sun.xml.registry.common.BulkResponseImpl;
import com.sun.xml.registry.uddi.*;
import com.sun.xml.registry.common.util.XMLUtil;
import com.sun.xml.registry.common.util.MarshallerUtil;

import org.w3c.dom.*;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import java.lang.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 
 * @version 1.4, 01/11/00
 * 
 * 
 */

public class Processor {
 
    RegistryServiceImpl service;
    UDDIMapper mapper;
    ResponseTransformer transformer;

     Logger logger = (Logger)
	AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		return Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".uddi");
	    }
	});

    public Processor (RegistryServiceImpl registryService, UDDIMapper mapper){
        this.service = registryService;
        this.mapper = mapper;
        this.transformer = new ResponseTransformer(mapper);
    }


    BulkResponse processRequestJAXB(Object obj, boolean secure, Collection keys, String type)
            throws JAXRException {
        SOAPMessage doc = null;
        try {
            doc = MarshallerUtil.getInstance().jaxbMarshalObject(obj);
        } catch (JAXBException jbe) {
            throw new JAXRException(jbe);
        }
        return processResponseJAXB(service.send(doc, secure), keys, type);
    }

     /**
     *
     * DOM Element response from UDDI Invocation is instantiated to
     * UDDI Object and passed to toBulkResponse method that will
     * perform Transformation of UDDI Object to JAXR Collection
     * of Objects returned via JAXR BulkResponse
     *
     * @param		response		DOM Element response from UDDI Invocation
     * @return		BulkResponse	Collection of JAXR Objects return
     * @exception	JAXRException
     *
     */
    BulkResponse processResponseJAXB(Node response, Collection keys, String type)
             throws JAXRException {

         BulkResponse bulkResponse = null;
         if (response == null)
             return null;

         Node resultNode = null;

         String name = response.getNodeName();
         

         if (((SOAPBody) response).hasFault()) {
             
             if (response instanceof Element) {
                 NodeList list = ((Element) response).getElementsByTagName("dispositionReport");
                 if (list != null) {
                     int listLength = list.getLength();
                     if (listLength > 0) {

                         for (int i = 0; i < listLength; i++) {
                             Node n = list.item(i);
                             if (n != null) {
                                 resultNode = n;
                                 name = "dispositionReport";
                                 break;
                             }
                         }
                     }
                 }
             }
         } else 
             resultNode = response.getFirstChild();

         logger.finest("Node name " + name);

         try {
             Object obj = MarshallerUtil.getInstance().jaxbUnmarshalObject(resultNode);
             logger.finest("Class name " + obj.getClass().getName());
             bulkResponse = invokeMethod(obj, keys, type);
         } catch (JAXBException jbe) {
             
             throw new JAXRException(jbe);
         }

         if (bulkResponse == null) {
             
             bulkResponse = new BulkResponseImpl();
         }
         

         return bulkResponse;
     }

    public BulkResponse invokeMethod( Object obj, Collection keys, String type)
      throws JAXRException {

       return transformer.transformResponse(obj, keys, type);
    }
    public BulkResponse invoke( Object obj, Collection keys, String type)
      throws InvocationTargetException,
             IllegalAccessException,
             JAXRException {

        Object args[] = new Object[3];
        args[0] = obj;
        args[1] = keys;
        args[2] = type;

        //get arg types
        Class[] classes = new Class[3];
        classes[0] = args[0].getClass();
        classes[1] = java.util.Collection.class;
        classes[2] = java.lang.String.class;

        //get helper class and invoke
        try {
            Class h = com.sun.xml.registry.uddi.UDDIMapper.class;
            Method m = h.getMethod("transformResponse", classes);
            return (BulkResponse)m.invoke(mapper, args);
        }
        catch (InvocationTargetException e) {
            Exception e1 = (Exception)e.getTargetException();
            throw new JAXRException(e1);
        }
        catch (NoSuchMethodException e) {
            throw new InvocationTargetException(e, ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Can't_find_method_"));
        }
    }
}

