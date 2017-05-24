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

import test.beans.TestBeanInterface;
import test.beans.artifacts.AnotherQualifier;
import test.beans.artifacts.Preferred;
import test.beans.artifacts.RequiresNewTransactionInterceptor;
import test.beans.artifacts.TransactionInterceptor;
import test.beans.mock.MockBean;
import test.beans.mock.MockShoppingCart;
import test.beans.mock.MockTestBeanForAnotherQualifier;
import test.beans.nonmock.ShoppingCart;
import test.beans.nonmock.TestBean;
import test.beans.nonmock.TestBeanForAnotherQualifier;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class SimpleSpecializationTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBeanInterface tb;
    
    @Inject
    @AnotherQualifier
    TestBeanInterface tb_another;
    
    @Inject
    @Preferred
    ShoppingCart sc;
    

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        //TestBean uses normal interceptors placed on it
        if (tb == null)
            msg += "Injection of request scoped bean failed";

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
        
        if (RequiresNewTransactionInterceptor.aroundInvokeCalled)
            msg += "RequiresNew TransactionInterceptor called when " +
            		"it shouldn't have been called";
        
        if (TestBean.testBeanInvoked)
            msg += "Test Bean invoked when actually mock bean should " +
            		"have been invoked";
        
        if (!MockBean.mockBeanInvoked)
            msg += "Mock bean not invoked";
        
        TransactionInterceptor.clear();
        //invoke shopping cart bean. ShoppingCart bean uses a Stereotype to
        //assign the requires new transaction interceptor
        //This should result in an invocation on
        //the RequiresNewTransactional
        
        //check that the mock shopping cart bean inherits the qualifiers when the alternative 
        //bean extends the actual bean and uses @Specializes to inherit the 
        //Qualifier 
        
        sc.addItem("Test Item");
        if (!RequiresNewTransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke in requires new " +
            		"transaction interceptor not called";
        if (RequiresNewTransactionInterceptor.aroundInvokeInvocationCount != 1)
            msg += "Business method requires new interceptor invocation on " +
            		"method-level interceptor annotation count not expected. "
                    + "expected =1, actual="
                    + RequiresNewTransactionInterceptor.aroundInvokeInvocationCount;
        if (!RequiresNewTransactionInterceptor.errorMessage.trim().equals(""))
            msg += RequiresNewTransactionInterceptor.errorMessage;
        
        //TransactionInterceptor should not be called
        if (TransactionInterceptor.aroundInvokeCalled)
            msg += "TranscationInterceptor aroundInvoke called when a requiresnew" +
            		"transaction interceptor should have been called";
        
        //test that the mocks are called instead of the actual beans
        if (ShoppingCart.shoppingCartInvoked)
            msg += "Test shopping cart invoked when actually mock shopping cart " +
            		"should have been invoked";
        
        if (!MockShoppingCart.mockShoppingCartInvoked)
            msg += "Mock shopping cart not invoked";
        
        //check that the mock bean inherits the qualifiers when the bean is 
        //produced through a Specializes producer method
        if (tb_another == null)
            msg += " bean with another qualifier was not injected";
        
        if (!(tb_another instanceof MockTestBeanForAnotherQualifier))
            msg += "bean with another qualifier is not an instance of TestBeanWithAnotherQualifier";
        

        writer.write(msg + "\n");
    }

}
