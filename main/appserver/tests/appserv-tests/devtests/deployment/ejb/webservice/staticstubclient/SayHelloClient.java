/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package staticstubclient;

import javax.xml.rpc.Stub;
import helloservice.*;

public class SayHelloClient {

    public static void main(String[] args) {

        System.out.println("Endpoint address = " + args[0]);
		boolean testPositive = (Boolean.valueOf(args[1])).booleanValue();
        try {
            Stub stub = createProxy();
            stub._setProperty
              (javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, args[0]); 
			SayHello hello = (SayHello)stub;
            System.out.println(hello.sayHello("Hi There ejb endpoint !!!"));
        } catch (Exception ex) {
	    	if(testPositive) {
				ex.printStackTrace();
				System.exit(-1);
	    	} else {
				System.out.println("Exception recd as expected");
	    	}
		}
		System.exit(0);
    }    

    private static Stub createProxy() {
        return (Stub) (new SayHelloService_Impl().getSayHelloPort());
    }
} 
