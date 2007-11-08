/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package wstoejb;

import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;

import wstoejb.WebServiceToEjbSEI;


/**
 * This is a appclient test
 */
public class Client {
    
    public static void main(String args[]) {
        
        boolean testPositive = (Boolean.valueOf(args[0])).booleanValue();
        try {
            Context ic = new InitialContext();        
        
            Service myWebService = (Service)
                ic.lookup("java:comp/env/service/WstoEjbService");        
            WebServiceToEjbSEI port = (WebServiceToEjbSEI) myWebService.getPort(WebServiceToEjbSEI.class);
            System.out.println(port.payload("APPCLIENT as client"));
        } catch(Throwable t) {
			if(testPositive) {
            	t.printStackTrace();
            	System.exit(-1);
			} else {
				System.out.println("Recd exception as expected");
			}
        }
		System.exit(0);
    }
    
}
