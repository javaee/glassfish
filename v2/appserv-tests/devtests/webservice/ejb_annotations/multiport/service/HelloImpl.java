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
    endpointInterface="service.Hello",
    portName="HelloPort",
    targetNamespace="http://httptestservice.org/wsdl",
    wsdlLocation="META-INF/wsdl/HttpTestService.wsdl"
)
public class HelloImpl implements Hello {

    public HelloResponse hello(HelloRequest req) {
        System.out.println("Hello, " + req.getString() + "!");
	HelloResponse resp = new HelloResponse();
	resp.setString("Hello, " + req.getString() + "!");
        return resp;
    }
}
