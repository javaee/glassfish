/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import java.util.Map;
import java.util.HashMap;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        stat.addDescription(appName);
        Client t = new Client();
        t.test();
        stat.printSummary(appName + "ID");

    }

    private void test() {

        //Map<String, Object> p = new HashMap<String, Object>();
        //p.put(EJBContainer.MODULES, "sample");

        EJBContainer c = EJBContainer.createEJBContainer();
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
	    System.out.println("Sleeping for a second...");
	    Thread.sleep(1000);

	    SingletonBean sb = (SingletonBean) ic.
		lookup("java:global/classes/SingletonBean!com.acme.SingletonBean");
	    sb.hello();

	    StatefulBean sfTimeout = (StatefulBean) ic.
		lookup("java:global/classes/StatefulBean");
	    StatefulBean2 sfNoTimeout = (StatefulBean2) ic.
		lookup("java:global/classes/StatefulBean2");
	    sfTimeout.hello();
	    sfNoTimeout.hello();

	    System.out.println("Sleeping to wait for sf bean to be removed ...");
	    Thread.sleep(7000);
	    System.out.println("Waking up , checking sf bean existence");

	    try {
		sfTimeout.hello();
		throw new RuntimeException("StatefulTimeout(0) bean should have timed out");
	    } catch(EJBException e) {
		System.out.println("Stateful bean successfully timed out");
	    }

	    sfNoTimeout.hello();
	    System.out.println("Stateful bean with longer timeout is still around");

	    /**
	    HelloRemote hr = (HelloRemote) ic.
		lookup("java:global/classes/SingletonBean!com.acme.HelloRemote");
	    hr.hello();
	    */

	    if( sb.getPassed() ) {
		System.out.println("getPassed() returned true");
		stat.addStatus("embedded async test", stat.PASS);
	    } else {
		throw new EJBException("getPassed() returned false");
	    }
        } catch (Exception e) {
            stat.addStatus("embedded async test", stat.FAIL);
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }

        System.out.println("Closing container");
	
	c.close();
        System.out.println("Done Closing container");

    }

}
