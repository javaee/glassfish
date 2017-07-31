/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.loadbalancing.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class EnrollerClientthreading extends Thread{

    private static  SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static int MAXTHREADS = 100;
    public static int count = 0;
    public static String ctxFactory;

    public static void main(String[] args) { 

        ctxFactory = args[0];
	System.out.println("Using " + ctxFactory);
	for (int i = 0; i < 300; i++) {
	    new EnrollerClientthreading().start();
	}
    }
  
    public void run() {
        try {
	    Properties env = new Properties();
	    env.put("java.naming.factory.initial", ctxFactory);	  
	    InitialContext ctx = new InitialContext(env);
		    
	    Object objref = ctx.lookup("ejb/MyStudent");
	    System.out.println("Thread #" + ++count + " looked up...ejb/MyStudent");
	    
	    StudentHome sHome = 
	      (StudentHome) PortableRemoteObject.narrow(objref, 
							StudentHome.class);
	    	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
