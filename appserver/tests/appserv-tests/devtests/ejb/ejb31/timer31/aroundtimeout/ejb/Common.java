/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;
import javax.ejb.Timer;

public class Common {

    static final String INTERCEPTORS_PROP = "aroundtimeout";
    static final String NOTHING_METHOD = "nothing";

    static final Set<String> calls = Collections.synchronizedSet(new HashSet<String>());

    static void aroundTimeoutCalled(InvocationContext ctx, String id) {

        List<String> aroundtimeout = (List<String>) 
            ctx.getContextData().get(INTERCEPTORS_PROP);
        
        if( aroundtimeout == null ) {
            aroundtimeout = new LinkedList<String>();
            ctx.getContextData().put(INTERCEPTORS_PROP, aroundtimeout);
        }

        aroundtimeout.add(id);
        
    }

    static void checkResults(InvocationContext ctx) {

        String methodName = ctx.getMethod().getName();

        if (methodName.equals("noaroundtimeout")) {
            throw new EJBException("AroundTimeout is called for method " + methodName);
        }

        String info = "" ;
        if( !(ctx.getTarget() instanceof SlessEJB7) ) {
            Timer t = (Timer) ctx.getTimer();
            if (t == null) {
                throw new EJBException("Timer is null for " + methodName);
            }
            info = "" + t.getInfo();

            String method_part = info.substring(info.indexOf('-') + 1);

            if (!methodName.equals(method_part)) {
                throw new EJBException("methodName: " + methodName + " vs. " + info);
            }
        }

        List<String> expected = null;

        if( !methodName.equals("nothing") ) {

            expected = new LinkedList<String>();

            String methodNameUpper = methodName.toUpperCase();

            for( char nextChar : methodNameUpper.toCharArray() ) {
                expected.add(nextChar + "");
            }
        } 

        List<String> actual = (List<String>) 
            ctx.getContextData().get(INTERCEPTORS_PROP);

        String msg = "Expected " + expected + " for method " +
            ctx.getMethod() + " actual = " + actual;

        if( (expected == null) && (actual == null) ) {
            System.out.println("Successful interceptor chain : " + msg);
        } else if( (expected == null) || (actual == null) ) {
            throw new EJBException(msg);
        } else if( !expected.equals(actual) ) {
            throw new EJBException(msg);
        } else {
            System.out.println("Successful interceptor chain : " + msg);
        }
        calls.add(info);
    }
   
    static void checkResults(String s0, int expected) {
        List<String> results = new ArrayList<String>();
        for (String s : calls) {
            if (s.startsWith(s0)) {
                results.add(s);
            }
        }
        if (results.size() != expected) {
             throw new RuntimeException("Expected for " + s0 + ": " + expected + " Got: " + results);
        }
        System.out.println("Verified " + expected + " aroundTimeout calls for " + s0);
    }

    static void storeResult(String s) {
        calls.add(s);
    }
}
