/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    
     private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
	    stat.addDescription("inheritedpk");

            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/PKA");
            pkvalidation.AHome ahome = (pkvalidation.AHome)PortableRemoteObject.narrow(objref, pkvalidation.AHome.class);

            pkvalidation.A abean = ahome.create(1, "A1", 2000.0);
            pkvalidation.APK pk1 = (pkvalidation.APK)abean.getPrimaryKey();
            System.out.println("CREATED A WITH PK: " + pk1.id);
            System.out.println("CREATED A WITH LASTNAME: " + abean.getLastname());

            pkvalidation.APK pk = new pkvalidation.APK();
            pk.id = 1;
            abean = ahome.findByPrimaryKey(pk);
            System.out.println("FOUND: " + abean.getLastname());

            objref = initial.lookup("java:comp/env/ejb/PKB");
            pkvalidation.BHome bhome = (pkvalidation.BHome)PortableRemoteObject.narrow(objref, pkvalidation.BHome.class);

            java.sql.Date d = new java.sql.Date(System.currentTimeMillis());

            pkvalidation.B bbean = bhome.create(d, "B1");
            System.out.println("CREATED B WITH PK: " + d);

            objref = initial.lookup("java:comp/env/ejb/PKC");
            pkvalidation.CHome chome = (pkvalidation.CHome)PortableRemoteObject.narrow(objref, pkvalidation.CHome.class);

            pkvalidation.C cbean = chome.create(1, "C1");
            pkvalidation.CPK pkc = (pkvalidation.CPK)cbean.getPrimaryKey();
            System.out.println("CREATED C WITH PK: " + pkc.id);
            System.out.println("CREATED C WITH NAME: " + cbean.getName());

            pkvalidation.CPK cpk = new pkvalidation.CPK();
            cpk.id = 1;
            cbean = chome.findByPrimaryKey(cpk);
            System.out.println("FOUND C: " + cbean.getName());

	    stat.addStatus("ejbclient inheritedpk", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient inheritedpk", stat.FAIL);
        }
            stat.printSummary("inheritedpk");
    }
    
}
