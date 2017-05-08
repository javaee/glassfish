/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.sfsb.stress.client;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class LoadGenerator {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");    
    private static String propsFileName = 
	"/export/home/s1as/cts/ws/appserv-tests/devtests/ejb/sfsb/stress/client/jndi.properties";

    private Context ctx;
    private StressSFSBHome home;

    public LoadGenerator(String[] args)
	throws Exception
    {
	String jndiName = args[0];
	ctx = getContext(args[1]);    

	Object ref = ctx.lookup(jndiName);
	this.home = (StressSFSBHome) 
	    PortableRemoteObject.narrow(ref, StressSFSBHome.class);
	System.out.println("LoadGenerator got home: " + home.getClass());
    }

    private InitialContext getContext(String propsFileName)
	throws Exception
    {
        InitialContext ic;

        if( propsFileName == null ) {
            ic = new InitialContext();
        } else {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFileName);
            props.load(fis);
            ic = new InitialContext(props);
        }

        return ic;
    }


    public void doTest() {
	for (int i=0; i<10; i++) {
	    System.out.println("Creating StressSFSBClient[" + i + "]");
	    String clientName = "client-"+i;
	    StressSFSBClient client = new StressSFSBClient(clientName,
		    home, 10);
	}
    }


    public static void main(String[] args) {
        try {
	    stat.addDescription("ejb-sfsb-stress");
	    LoadGenerator generator = new LoadGenerator(args);
	    generator.doTest();
	    stat.addStatus("ejb-sfsb-stress main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejb-sfsb-stress main", stat.FAIL);
        }
    }

}
