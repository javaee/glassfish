/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package myweb;

import java.util.Iterator;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.naming.*;
import javax.xml.rpc.Service;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.namespace.QName;
import javax.servlet.SingleThreadModel;

public class FindInterestServletImpl implements 
			SingleThreadModel, ServiceLifecycle {

    public FindInterestServletImpl() {
        System.out.println("FindInterestServletImpl() instantiated");
    }

    public void init(Object context) {
        System.out.println("Got ServiceLifecycle::init call " + context);
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public double calculateInterest(double balance, double period) {
	System.out.println("calculateInterest invoked from servlet endpoint");
	return 0.2*balance*period;
    }
}
