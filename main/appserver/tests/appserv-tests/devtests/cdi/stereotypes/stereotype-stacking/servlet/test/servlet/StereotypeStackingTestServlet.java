/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestSecureBean;
import test.beans.TestTxBean;
import test.beans.TestTxSecBean;
import test.beans.interceptors.SecurityInterceptor;
import test.beans.interceptors.TransactionInterceptor;
import test.beans.qualifiers.Preferred;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class StereotypeStackingTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestTxBean tb_tx;

    @Inject
    @Preferred
    TestSecureBean tb_sec;

    @Inject
    @Preferred
    TestTxSecBean tb_tx_sec;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // Just Transactional Interceptor
        if (tb_tx == null)
            msg += "Injection of transactional bean failed";

        tb_tx.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb_tx.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor [TransactionInterceptor] " +
            		"invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;

        if (SecurityInterceptor.aroundInvokeCalled)
            msg += "Security Interceptor called when "
                    + "it shouldn't have been called";

        clearInterceptors();
        
        //Just security interceptor
        if (tb_sec == null)
            msg += "Injection of @secure bean failed";

        tb_sec.m1();
        if (!SecurityInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb_sec.m2();
        if (SecurityInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation [SecurityInterceptor]" +
            		"on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + SecurityInterceptor.aroundInvokeInvocationCount;
        if (!SecurityInterceptor.errorMessage.trim().equals(""))
            msg += SecurityInterceptor.errorMessage;

        if (TransactionInterceptor.aroundInvokeCalled)
            msg += "Security Interceptor called when "
                    + "it shouldn't have been called";

        clearInterceptors();

        //Both transaction and security interceptors
        if (tb_tx_sec == null)
            msg += "Injection of @transactional and @secure bean failed";

        tb_tx_sec.m1();
        if (!SecurityInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor [Security Interceptor] aroundInvoke not called";
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor [Transaction Interceptor] aroundInvoke not called";
        
        tb_tx_sec.m2();
        if (SecurityInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation [SecurityInterceptor]" +
                    "on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + SecurityInterceptor.aroundInvokeInvocationCount;
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor [TransactionInterceptor] " +
                    "invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        
        if (!SecurityInterceptor.errorMessage.trim().equals(""))
            msg += SecurityInterceptor.errorMessage;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;


        writer.write(msg + "\n");
    }

    private void clearInterceptors() {
        //clear interceptors
        TransactionInterceptor.clear();
        SecurityInterceptor.clear();
    }

}
