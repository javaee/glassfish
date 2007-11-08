/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package rpcencoded;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.xml.rpc.server.ServiceLifecycle;


// Service Implementation Class - as outlined in JAX-RPC Specification

public class HelloImpl implements javax.servlet.SingleThreadModel, ServiceLifecycle {

    private boolean gotInit = false;
   
    public void init(Object o) {
        System.out.println("Got ServiceLifecycle::init call " + o);
        gotInit = true;
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public String hello(String s) throws RemoteException {
        return "Hello, " + s + "!";
    }

    public void helloOneWay(String s) throws RemoteException {
        System.out.println("Hello one way, " + s + "!");
    }
}
