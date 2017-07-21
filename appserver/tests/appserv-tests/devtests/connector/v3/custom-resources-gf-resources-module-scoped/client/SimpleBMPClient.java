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

package com.sun.s1asdev.jdbc.CustomResourceFactories.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    private static final String testSuite = "Custom Resource Factories Test - ";
    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();

	stat.addDescription(testSuite);

    InitialContext ic = new InitialContext();
    Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

    SimpleBMP simpleBMP = simpleBMPHome.create();

        String test = args[0];

        if(test.equalsIgnoreCase("javabean")){
            if ( simpleBMP.testJavaBean(args[1]) ) {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("primitivesandstring")){

            if ( simpleBMP.testPrimitives(args[1], args[2], args[3]) ) {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.FAIL);
            }

        }else if(test.equalsIgnoreCase("properties")){
            Properties properties = new Properties();
            for (int i=1; i<args.length-1;i++){
                properties.put(args[i],args[i+1]);
                i++;
            }

            if ( simpleBMP.testProperties(properties, args[args.length-1])) {
                stat.addStatus(testSuite+" Properties : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Properties : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("url")){
            if ( simpleBMP.testURL(args[1], args[2])) {
                stat.addStatus(testSuite+" URL : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" URL : ", stat.FAIL);
            }
        }



    stat.printSummary();
    }
}
