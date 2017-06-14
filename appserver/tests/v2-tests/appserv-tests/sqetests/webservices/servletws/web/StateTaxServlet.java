/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1peqe.webservices.servlet.taxcal;

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

public class StateTaxServlet implements 
			SingleThreadModel, ServiceLifecycle {

    public StateTaxServlet() {
        System.out.println("StateTaxServlet() instantiated");
    }

    public void init(Object context) {
        System.out.println("Got ServiceLifecycle::init call " + context);
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public double getStateTax(double income, double deductions) {
	System.out.println("getStateTax invoked from servlet endpoint");
	 return ((income -  deductions) * 0.3);
    }
}
