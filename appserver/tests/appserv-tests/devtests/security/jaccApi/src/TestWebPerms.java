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

package javax.security.jacc;

import java.lang.reflect.*;
import java.util.Enumeration;
import java.security.*; 
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.security.jacc.WebRoleRefPermission;

import javax.security.jacc.HttpMethodSpec;
import javax.security.jacc.URLPattern;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
 
public class TestWebPerms {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testWebPerms ";
    private static boolean isDebug = Boolean.getBoolean("debug");

    private static void debug(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }

    private static boolean testSerialization(Permission p1) {
        String description = "testSerialization:" + p1;
        boolean result = true;

        Permission p2 = p1;
        try {
            FileOutputStream fout = new 
                FileOutputStream("serial-test-file.tmp");
            ObjectOutputStream sout = new ObjectOutputStream(fout);
            sout.writeObject(p1);
            sout.flush();
            sout.close();
            fout.close();
        } catch( Throwable t ) { 
            t.printStackTrace();
            debug( "-- Serialization Test Failed(write)-" + p1.getName() + "," + p1.getActions())
;
        }

        try {
            FileInputStream fin = new FileInputStream("serial-test-file.tmp");
            ObjectInputStream sin = new ObjectInputStream(fin);
            p2 = (Permission) sin.readObject();
            sin.close();
            fin.close(); 
        } catch( Throwable t ) { 
            t.printStackTrace();
            debug( "-- Serialization Test Failed(read)-" + p1.getName() + "," + p1.getActions());
            result = false;
        }

        if (result == true) {
            if (p2.equals(p1)) { 
                debug( "-- Serialization Test Succeeded -----------" + p2.getName() + "," + p2.getActions());
            } else { 
                debug( "-- Serialization Test Failed-" + p1.getName() + "," + p1.getActions());
            }
        } else {
            debug( "-- Serialization Test Failed-" + p1.getName() + "," + p1.getActions());
        }

        return result;
    }

    private static void reportConstructResults(
            boolean result, boolean expectedToSucceed,Permission p) {
        if (result == expectedToSucceed) {
            if (expectedToSucceed) {
                debug( "-- Construct Test Succeeded -----------" + p.getName() + "," + p.getActions());
            } else {
                debug( "-- Construct Test Succeeded (negative)-" + p.getName() + "," + p.getActions());
            }
        } else {
            debug( "-- Construct Test Failed -------------");
        }
    }

    private static String makeWUDActions(String meth, String trans) {
        return meth == null ? (trans == null ? null : trans) :
            (trans == null ? meth : meth + trans);
    }

    private static boolean doWebResourcePermission( boolean expectedToSucceed,
                                             String name, String actions) {
        boolean result = true;

        WebResourcePermission p1,p2;

        p1 = null;
        try {
            debug( "-- Construct Test --" + expectedToSucceed + " " + name + " " + actions);

            p1 = new WebResourcePermission(name,actions);

            if (expectedToSucceed) {

                p2 = new WebResourcePermission(p1.getName(),p1.getActions()); 

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }

    private static boolean doWebResourcePermission( boolean expectedToSucceed,
            String URLPattern, String[] methods) {
        boolean result = true;
        WebResourcePermission p1,p2;
        p1 = null;

        try {
            debug( "-- Construct Test --");
            p1 = new WebResourcePermission(URLPattern,methods);
            if (expectedToSucceed) {
                p2 = new WebResourcePermission(p1.getName(),p1.getActions()); 
                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);
                }
                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }
            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }
        } catch( Throwable t ) { 
            if (expectedToSucceed) {
                t.printStackTrace();
            }
            result = false;
        }
        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }

    private static boolean doWebResourcePermission( boolean expectedToSucceed,
            HttpServletRequest request) {
        boolean result = true;

        WebResourcePermission p1,p2;

        p1 = null;

        try {
            debug( "-- Construct Test -----------------------------------------------");

            p1 = new WebResourcePermission(request);

            if (expectedToSucceed) {

                p2 = new WebResourcePermission(p1.getName(),p1.getActions()); 

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }


    private static boolean doWebRoleRefPermission( boolean expectedToSucceed,
            String servletName, String roleRef) {
        boolean result = true;

        WebRoleRefPermission p1,p2;

        p1 = null;

        try {
            debug( "-- Construct Test -----------------------------------------------");

            p1 = new WebRoleRefPermission(servletName,roleRef);

            if (expectedToSucceed) {

                p2 = new WebRoleRefPermission(p1.getName(),p1.getActions()); 

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }

    private static boolean doWebUserDataPermission( boolean expectedToSucceed,
                                             String name, String actions) {
        boolean result = true;

        WebUserDataPermission p1,p2;

        p1 = null;
        try {
            debug( "-- Construct Test --" + expectedToSucceed + " " + name + " " + actions);

            p1 = new WebUserDataPermission(name,actions);

            if (expectedToSucceed) {

                p2 = new WebUserDataPermission(p1.getName(),p1.getActions()); 

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }

    private static boolean doWebUserDataPermission( boolean expectedToSucceed,
            String URLPattern, String methods, String transportType) {
        boolean result = true;
        WebUserDataPermission p1,p2;
        p1 = null;

        try {
            debug( "-- Construct Test --" + expectedToSucceed);
            p1 = new WebUserDataPermission
                (URLPattern,makeWUDActions(methods,transportType));

            if (expectedToSucceed) {
                p2 = new WebUserDataPermission(p1.getName(),p1.getActions()); 
                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);
                }
                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }
            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }
        } catch( Throwable t ) { 
            if (expectedToSucceed) {
                t.printStackTrace();
            }
            result = false;
        }
        reportConstructResults(result,expectedToSucceed,p1);
        return result;
    }

    private static String printP(Permission p) {
        return p.getName() + "," + p.getActions();
    }

    private static boolean doPermissionImplies (boolean expectedResult,
            Permission thisP, Permission p) { 
        boolean result = true;
        String description = "doPermissionImplies:" +
            expectedResult + "-" + thisP + "-" + p;
        try {

            debug( "-- Permission implies Test ----------------------------------------------");

            if (thisP.implies(p) != expectedResult) {
                debug(
                    (expectedResult ? "unexpected failure:" :"unexpected success:")
                     + printP(p) + (expectedResult ?" not implied by:":" implied by:") + 
                     printP(thisP));
                debug( "-- Permission implies Test failed(not implied)----------------------------");
                result = false;
            } else {
                debug(printP(p) + 
                    (expectedResult ? " implied by:":" not implied by:") + 
                    printP(thisP));
                debug( "-- Permission implies Test Succeeded -------------------------------------");
                result = true;
            }
        } catch( Throwable t ) { 
            debug("unexpected exception");
            t.printStackTrace();
            debug( "-- Permission implies Test failed(exception)---------------------------------");
            result = false;
        }
        return result;
    }

    private static boolean doHttpMethodSpecImplies ( boolean expectedResult,
            HttpMethodSpec thisS, HttpMethodSpec thatS) {
        boolean result = true;
        String description = "doHttpMethodSpecImplies:" +
                expectedResult + "-" + thisS + "-" + thatS;
        try {

            debug( "-- HttpMethodSpec implies Test ----------------------------------------------");
            if (thisS.implies(thatS) != expectedResult) {
                debug(
                    (expectedResult ? "unexpected failure:" :"unexpected success:")
                     + thatS + (expectedResult ?" not implied by:":" implied by:") + thisS);
                debug( "-- HttpMethodSpec implies Test failed(not implied)----------------------------");
                result = false;
            } else {
                debug(thatS + 
                    (expectedResult ? " implied by:":" not implied by:") + thisS);
                result = true;
            }

            if (!thisS.implies(thisS)) {
                debug( "unexpected failure:" 
                    + thisS + " not implied by: " +  thisS);
                debug( "-- HttpMethodSpec implies Test failed(not implied)----------------------------");
                result = false;
            }

            if (!thatS.implies(thatS)) {
                debug( "unexpected failure:" 
                    + thatS + " not implied by: " + thatS);
                debug( "-- HttpMethodSpec implies Test failed(not implied)----------------------------");
                result = false;
            }
        } catch( Throwable t ) { 
            debug("unexpected exception");
            t.printStackTrace();
            debug( "-- HttpMethodSpec implies Test failed(exception)---------------------------------");
        }
        return result;
    }

    public static void main( String[] args ) {
        boolean result = true;

        String tArray[] = { 
            null, 
            ":NONE", 
            ":INTEGRAL", 
            ":CONFIDENTIAL" 
        };

        String mArray[] = {
          "GET",
          "HEAD",
          "OPTIONS",
          "POST",
          "PUT",
          "TRACE",
          "FLY",
          "DELETE"
        };

        String msArray[] = new String[(1<<mArray.length)];
        for (int i=0; i<msArray.length; i++) {
            if (i==0) msArray[i] = null;
            else {
                StringBuffer s = null;
                int bitValue = 1;
                for (int j=0; j<mArray.length; j++) {
                    if ((i & bitValue) == bitValue) {
                        if (s == null) s = new StringBuffer(mArray[j]);
                        else s.append("," + mArray[j]);
                    }
                    bitValue = bitValue * 2;
                }
                msArray[i] = s.toString();
            }
        }

        String pArray[] = {
            "/a/c",
            "/a/b/c.jsp",
            "/a/c/*",
            "/a/*",
            "/*",
            "*.jsp",
            "*.asp",
            "/"
        };

        String qpArray[] = {
            "/a/c",
            "/a/b/c.jsp",
            "/a/c/*:/a/c",
            "/a/*:/a/c:/a/b/c.jsp", 
            "/*:/a/c:/a/b/c.jsp:/a/c/*:/a/*",
            "*.jsp:/a/c/*:/a/*:/a/b/c.jsp",
            "*.asp:/a/c/*:/a/*",
            "/:/a/c:/a/b/c.jsp:/a/c/*:/a/*:*.jsp:*.asp"
        };
 
        for (int i=0; i<msArray.length; i++) {

            HttpMethodSpec s = HttpMethodSpec.getSpec(msArray[i]);

            int hCode = s.hashCode();

            String actions = s.getActions();

            debug("input actions: " + msArray[i] + 
                 " hashCode: " + hCode + 
                 " actions:" + actions);
        }

        HttpMethodSpec h1 = HttpMethodSpec.getSpec((String) null);
        HttpMethodSpec h2 = HttpMethodSpec.getSpec("!PUT,SWIM");
        HttpMethodSpec h3 = HttpMethodSpec.getSpec("PUT");
        HttpMethodSpec h4 = HttpMethodSpec.getSpec("SWIM");
        HttpMethodSpec h5 = HttpMethodSpec.getSpec("!PUT");

        result = result && doHttpMethodSpecImplies(true,h1,h1);
        result = result && doHttpMethodSpecImplies(true,h1,h2);
        result = result && doHttpMethodSpecImplies(true,h1,h3);
        result = result && doHttpMethodSpecImplies(true,h1,h4);
        result = result && doHttpMethodSpecImplies(true,h1,h5);

        result = result && doHttpMethodSpecImplies(false,h2,h1);
        result = result && doHttpMethodSpecImplies(true,h2,h2);
        result = result && doHttpMethodSpecImplies(false,h2,h3);
        result = result && doHttpMethodSpecImplies(false,h2,h4);
        result = result && doHttpMethodSpecImplies(false,h2,h5);

        result = result && doHttpMethodSpecImplies(false,h3,h1);
        result = result && doHttpMethodSpecImplies(false,h3,h2);
        result = result && doHttpMethodSpecImplies(true,h3,h3);
        result = result && doHttpMethodSpecImplies(false,h3,h4);
        result = result && doHttpMethodSpecImplies(false,h3,h5);

        result = result && doHttpMethodSpecImplies(false,h4,h1);
        result = result && doHttpMethodSpecImplies(false,h4,h2);
        result = result && doHttpMethodSpecImplies(false,h4,h3);
        result = result && doHttpMethodSpecImplies(true,h4,h4);
        result = result && doHttpMethodSpecImplies(false,h4,h5);

        result = result && doHttpMethodSpecImplies(false,h5,h1);
        result = result && doHttpMethodSpecImplies(true,h5,h2);
        result = result && doHttpMethodSpecImplies(false,h5,h3);
        result = result && doHttpMethodSpecImplies(true,h5,h4);
        result = result && doHttpMethodSpecImplies(true,h5,h5);

        for (int i=0; i<pArray.length; i++) {
            for (int j=0; j<msArray.length; j++) {

                result = result && doWebResourcePermission(true,pArray[i],msArray[j]);

                result = result && doPermissionImplies
                    (true,new WebResourcePermission(pArray[i],msArray[j]),
                     new WebResourcePermission(pArray[i],msArray[j]));

                result = result && doWebResourcePermission(true,qpArray[i],msArray[j]);

                result = result && doPermissionImplies
                    (true,new WebResourcePermission(qpArray[i],msArray[j]),
                     new WebResourcePermission(qpArray[i],msArray[j]));

                result = result && doPermissionImplies
                    (true,new WebResourcePermission(pArray[i],msArray[j]),
                     new WebResourcePermission(qpArray[i],msArray[j]));

                result = result && doPermissionImplies
                    (qpArray[i].equals(pArray[i]) ? true : false,
                     new WebResourcePermission(qpArray[i],msArray[j]),
                     new WebResourcePermission(pArray[i],msArray[j]));

                result = result && testSerialization
                    (new WebResourcePermission(pArray[i],msArray[j]));
                result = result && testSerialization
                    (new WebResourcePermission(qpArray[i],msArray[j]));

            }
        }

        result = result && doWebRoleRefPermission(true,"servletName","customer");

        WebRoleRefPermission a1,a2,a3,a4;
        a1 = new WebRoleRefPermission("servletName1","roleRef1");
        result = result && testSerialization(a1);
        a2 = new WebRoleRefPermission("servletName1","roleRef2");
        result = result && testSerialization(a2);
        a3 = new WebRoleRefPermission("servletName2","roleRef1");
        result = result && testSerialization(a3);
        a4 = new WebRoleRefPermission("servletName2","roleRef2");
        result = result && testSerialization(a4);

        result = result && doPermissionImplies(true,a1,a1);
        result = result && doPermissionImplies(false,a1,a2);
        result = result && doPermissionImplies(false,a1,a3);
        result = result && doPermissionImplies(false,a1,a4);
        result = result && doPermissionImplies(false,a2,a1);
        result = result && doPermissionImplies(true,a2,a2);
        result = result && doPermissionImplies(false,a2,a3);
        result = result && doPermissionImplies(true,a3,a3);
        result = result && doPermissionImplies(false,a3,a4);
        result = result && doPermissionImplies(false,a4,a1);
        result = result && doPermissionImplies(false,a4,a2);
        result = result && doPermissionImplies(false,a4,a3);
        result = result && doPermissionImplies(true,a4,a4);

        for (int i=0; i<pArray.length; i++) {
            for (int j=0; j<msArray.length; j++) {
                for (int k=0; k<tArray.length; k++) {
                    result = result && doWebUserDataPermission(true,pArray[i],msArray[j],tArray[k]);

                    result = result && doPermissionImplies
                        (true,
                         new WebUserDataPermission
                         (pArray[i],makeWUDActions(msArray[j],tArray[k])),
                         new WebUserDataPermission
                         (pArray[i],makeWUDActions(msArray[j],tArray[k])));

                    result = result && doWebUserDataPermission(true,qpArray[i],msArray[j],tArray[k]);

                    result = result && doPermissionImplies
                        (true,
                         new WebUserDataPermission
                         (qpArray[i],makeWUDActions(msArray[j],tArray[k])),
                         new WebUserDataPermission
                         (qpArray[i],makeWUDActions(msArray[j],tArray[k])));

                    result = result && doPermissionImplies
                        (true,
                         new WebUserDataPermission
                         (pArray[i],makeWUDActions(msArray[j],tArray[k])),
                         new WebUserDataPermission
                         (qpArray[i],makeWUDActions(msArray[j],tArray[k])));

                    result = result && doPermissionImplies
                        (qpArray[i].equals(pArray[i]) ? true : false,
                         new WebUserDataPermission
                         (qpArray[i],makeWUDActions(msArray[j],tArray[k])),
                         new WebUserDataPermission
                         (pArray[i],makeWUDActions(msArray[j],tArray[k])));

                    result = result && testSerialization
                        (new WebUserDataPermission
                         (pArray[i],makeWUDActions(msArray[j],tArray[k])));

                    result = result && testSerialization
                        (new WebUserDataPermission
                         (qpArray[i],makeWUDActions(msArray[j],tArray[k])));

                }
            }
        }

        stat.addStatus(testSuite, (result)? stat.PASS : stat.FAIL);
        stat.printSummary();
    }
}
