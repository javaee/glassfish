/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.SecondTransactionInterceptor;
import test.beans.TestBean;
import test.beans.TestRequestScopedBean;
import test.beans.ThirdTransactionInterceptor;
import test.beans.TransactionInterceptor;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class MultipleInterceptorImplementationsTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;

    @Inject 
    TestRequestScopedBean trsb;
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (tb == null)
            msg += "Injection of bean (that is being intercepted) failed";

        tb.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;

        
        if (!SecondTransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke in the second " +
            		"transaction interceptor not called";
        tb.m2(); //calling m2 again
        if (SecondTransactionInterceptor.aroundInvokeInvocationCount != 3)
            msg += "Business method second interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =3, actual="
                    + SecondTransactionInterceptor.aroundInvokeInvocationCount;
        
        //test ordering of interceptors in the system
        boolean expectedOrdering = 
            ((trsb.interceptorInvocationOrder.get(0).
                    equals(SecondTransactionInterceptor.class.getCanonicalName()))
                && (trsb.interceptorInvocationOrder.get(1).
                        equals(TransactionInterceptor.class.getCanonicalName())));
        if (!expectedOrdering) 
            msg += "Interceptor invocation order does not match with the expected order";
        
        //test disabling of interceptors via beans.xml 
        if (ThirdTransactionInterceptor.aroundInvokeCalled) 
            msg += "Disabled interceptor called";
        
        writer.write(msg + "\n");
    }

}
