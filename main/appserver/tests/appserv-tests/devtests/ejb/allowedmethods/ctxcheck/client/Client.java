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

package com.sun.s1asdev.ejb.allowedmethods.ctxcheck.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.allowedmethods.ctxcheck.DriverHome;
import com.sun.s1asdev.ejb.allowedmethods.ctxcheck.Driver;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-allowedmethods-ctxcheck");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-allowedmethods-ctxcheck");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        localSlsbGetEJBObject();
        localSlsbGetEJBLocalObject();
        localSlsbGetEJBHome();
        localSlsbGetEJBLocalHome();
        localEntityGetEJBObject();
        localEntityGetEJBLocalObject();
        localEntityGetEJBHome();
        localEntityGetEJBLocalHome();
    }

    public void localSlsbGetEJBObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBObject();
            stat.addStatus("ejbclient localSlsbGetEJBObject", stat.FAIL);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBObject(-)" , stat.PASS);
        }
    }

    public void localSlsbGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBLocalObject();
            stat.addStatus("ejbclient localSlsbGetEJBLocalObject", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBLocalObject" , stat.FAIL);
        }
    }

    public void localSlsbGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBLocalHome();
            stat.addStatus("ejbclient localSlsbGetEJBLocalHome", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBLocalHome" , stat.FAIL);
        }
    }

    public void localSlsbGetEJBHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBHome();
            stat.addStatus("ejbclient localSlsbGetEJBHome" , stat.FAIL);
        } catch(Exception e) {
            stat.addStatus("ejbclient localSlsbGetEJBHome(-)", stat.PASS);
        }
    }


    public void localEntityGetEJBObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBObject();
            stat.addStatus("ejbclient localEntityGetEJBObject", stat.FAIL);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
        }
    }

    public void localEntityGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBLocalObject();
            stat.addStatus("ejbclient localEntityGetEJBLocalObject", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBLocalObject" , stat.FAIL);
        }
    }

    public void localEntityGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBLocalHome();
            stat.addStatus("ejbclient localEntityGetEJBLocalHome", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBLocalHome" , stat.FAIL);
        }
    }

    public void localEntityGetEJBHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBHome();
            stat.addStatus("ejbclient localEntityGetEJBHome" , stat.FAIL);
        } catch(Exception e) {
            stat.addStatus("ejbclient localEntityGetEJBHome(-)", stat.PASS);
        }
    }

}

