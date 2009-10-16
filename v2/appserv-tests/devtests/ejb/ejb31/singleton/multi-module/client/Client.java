/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.devtest.client;

//import com.sun.mod1.SingletonBean;

import java.util.Map;
import java.util.HashMap;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.glassfish.devtest.ejb31.singleton.multimodule.servlet.RemoteInitTracker;

public class Client {

	private static String[] BEAN_NAMES = new String[] {
		"org.glassfish.devtest.ejb31.singleton.multimodule.servlet.InitOrderTrackerBean",
		"org.glassfish.devtest.ejb31.singleton.multimodule.mod1.BeanA_Mod1",
		"org.glassfish.devtest.ejb31.singleton.multimodule.mod1.RootBean_Mod1",
		"org.glassfish.devtest.ejb31.singleton.multimodule.mod2.BeanA_Mod2",
		"org.glassfish.devtest.ejb31.singleton.multimodule.mod2.RootBean_Mod2"
	};

	private static final String INIT_ORDER_BEAN = BEAN_NAMES[0];

	private static final String BEAN_MOD1 = BEAN_NAMES[1];

	private static final String ROOT1 = BEAN_NAMES[2];

	private static final String BEAN_MOD2 = BEAN_NAMES[3];

	private static final String ROOT2 = BEAN_NAMES[4];

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    RemoteInitTracker tracker;

    public static void main(String[] args) {
        appName = args[0];
        stat.addDescription(appName);
        Client t = new Client();
        t.test();
        stat.printSummary(appName + "ID");
    }

    private void test() {
	try {
 
		String lookupName = "java:global/ejb-ejb31-singleton-multimoduleApp/ejb-ejb31-singleton-multimodule-ejb/InitOrderTrackerBean!org.glassfish.devtest.ejb31.singleton.multimodule.servlet.RemoteInitTracker";
		System.out.println("*****************************************************");
		System.out.println("*** " + lookupName + " ***");
		System.out.println("*****************************************************");
		tracker = (RemoteInitTracker) new InitialContext().lookup(lookupName);
	    
		Map<String, Integer> initOrder = tracker.getInitializedNames();
		boolean result = initOrder.size() == BEAN_NAMES.length;

		int b1m1 = initOrder.get(BEAN_MOD1);
		int b2m2 = initOrder.get(BEAN_MOD2);
		int root1 = initOrder.get(ROOT1);
		int root2 = initOrder.get(ROOT2);

		boolean test1 = b1m1 < root1;
		boolean test2 = root1 < root2;
		boolean test3 = b2m2 < root2;
		for (String key : initOrder.keySet()) {
			System.out.println(key + ": " + initOrder.get(key));
		}

		result = result && test1 && test2 && test3;

        	stat.addStatus("EJB singleton-cross-module-dependency",
			(result ? stat.PASS : stat.FAIL));
	} catch (Throwable th) {
		th.printStackTrace();
        	stat.addStatus("EJB singleton-cross-module-dependency", stat.FAIL);
	}
    }

}
