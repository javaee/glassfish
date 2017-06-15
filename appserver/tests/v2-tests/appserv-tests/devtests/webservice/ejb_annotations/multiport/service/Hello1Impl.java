/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * %W% %E%
 */

package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Service Implementation Class - as outlined in JAX-RPC Specification

import javax.jws.WebService;

@javax.ejb.Stateless
@javax.jws.WebService(
    serviceName="HttpTestService",
    endpointInterface="service.Hello1",
    portName="Hello1Port",
    targetNamespace="http://httptestservice.org/wsdl",
    wsdlLocation="META-INF/wsdl/HttpTestService.wsdl"
)
public class Hello1Impl implements Hello1 {

    public Hello1Response hello1(Hello1Request req) {
        System.out.println("Hello1, " + req.getString() + "!");
	Hello1Response resp = new Hello1Response();
	resp.setString("Hello1, " + req.getString() + "!");
        return resp;
    }
}
