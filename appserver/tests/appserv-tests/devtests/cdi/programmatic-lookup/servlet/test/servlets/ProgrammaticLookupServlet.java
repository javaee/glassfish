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

package test.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.artifacts.Asynchronous;
import test.artifacts.ChequePaymentProcessor;
import test.artifacts.MockPaymentProcessor;
import test.artifacts.PayBy;
import test.artifacts.PaymentMethod;
import test.artifacts.PaymentProcessor;
import test.artifacts.Synchronous;
import test.artifacts.TestApplicationScopedBean;
import test.artifacts.TestSessionScopedBean;
import test.artifacts.UnproxyableType;
import test.beans.BeanToTestProgrammaticLookup;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" }, initParams = {
        @WebInitParam(name = "n1", value = "v1"),
        @WebInitParam(name = "n2", value = "v2") })
public class ProgrammaticLookupServlet extends HttpServlet {

    @Inject
    BeanToTestProgrammaticLookup tb;

    @Inject
    @Synchronous
    Instance<PaymentProcessor> synchronousPaymentProcessor;

    @Inject
    @Asynchronous
    Instance<PaymentProcessor> asynchronousPaymentProcessor;

    @Inject
    @Any
    Instance<PaymentProcessor> anyPaymentProcessors;

    @Inject
    @Any
    Instance<UnproxyableType> unpnc;

    @Inject
    TestApplicationScopedBean tasb;

    @Inject
    TestSessionScopedBean tssb;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") + ", n2="
                + getInitParameter("n2");

        if (!tb.testInjection())
            msg += "Alternatives injection and test for availability of other "
                    + "beans via @Any Failed";

        boolean specifiedAtInjectionPoint = synchronousPaymentProcessor.get() instanceof MockPaymentProcessor
                && asynchronousPaymentProcessor.get() instanceof MockPaymentProcessor;
        if (!specifiedAtInjectionPoint)
            msg += "Qualifier based(specified at injection point) programmatic "
                    + "injection into Servlet Failed";

        PaymentProcessor pp1, pp2;
        // using anonymous inner classes
        pp1 = anyPaymentProcessors.select(
                new AnnotationLiteral<Asynchronous>() {
                }).get();
        pp2 = anyPaymentProcessors.select(
                new AnnotationLiteral<Synchronous>() {
                }).get();
        boolean specifiedQualifierDynamically = pp1 instanceof MockPaymentProcessor
                && pp2 instanceof MockPaymentProcessor;
        if (!specifiedQualifierDynamically)
            msg += "Qualifier based(specified dynamically through "
                    + "instance.select()) programmatic injection into Servlet Failed";

        // using concrete implementations of the annotation type
        pp1 = anyPaymentProcessors.select(new AsynchronousQualifier()).get();
        pp2 = anyPaymentProcessors.select(new SynchronousQualifier()).get();
        boolean specifiedQualifierDynamicallyThroughConcreteTypes = pp1 instanceof MockPaymentProcessor
                && pp2 instanceof MockPaymentProcessor;
        if (!specifiedQualifierDynamicallyThroughConcreteTypes)
            msg += "Qualifier based(specified dynamically through "
                    + "instance.select() through a concrete annotation type "
                    + "implementation) programmatic injection into Servlet Failed";

        PaymentProcessor chequePaymentProcessor = anyPaymentProcessors
                .select(new ChequeQualifier()).get();
        boolean specifiedQualifierWithMembersDynamicallyThroughConcreteTypes = chequePaymentProcessor instanceof ChequePaymentProcessor;
        if (!specifiedQualifierWithMembersDynamicallyThroughConcreteTypes)
            msg += "Qualifier with members based(specified dynamically through "
                    + "instance.select() through a concrete annotation type "
                    + "implementation) programmatic injection into Servlet Failed";

        // Ensure unproxabletypes are injectable
        for (Iterator iterator = unpnc.iterator(); iterator.hasNext();) {
            UnproxyableType type = (UnproxyableType) iterator.next();
            if (type == null) {
                System.out.println("UnproxyableType is null "
                        + type.getClass());
                msg += "Unproxyable type (class with non-public null constructor) Injection failed";
            }
        }

        // Ensure application-scoped bean has a reference to a session-scoped
        // bean
        // via a client proxy
        TestSessionScopedBean tsb = tasb.getSessionScopedBean();
        boolean isAClientProxy = testIsClientProxy(tsb,
                TestSessionScopedBean.class);
        if (!isAClientProxy)
            msg += "Session scoped bean is not a proxy class";

        writer.write("initParams: " + msg + "\n");
    }

    // Tests if the bean instance is a client proxy
    private boolean testIsClientProxy(Object beanInstance, Class beanType) {
        boolean isSameClass = beanInstance.getClass().equals(beanType);
        boolean isProxyAssignable = beanType.isAssignableFrom(beanInstance
                .getClass());
        System.out.println(beanInstance + "whose class is "
                + beanInstance.getClass() + " is same class of " + beanType
                + " = " + isSameClass);
        System.out.println(beanType + " is assignable from " + beanInstance
                + " = " + isProxyAssignable);
        boolean isAClientProxy = !isSameClass && isProxyAssignable;
        return isAClientProxy;
    }

    // concrete implementations of the qualifier annotation types to be used in
    // Instance.select()
    class AsynchronousQualifier extends AnnotationLiteral<Asynchronous>
            implements Asynchronous {
    }

    class SynchronousQualifier extends AnnotationLiteral<Synchronous>
            implements Synchronous {
    }

    class ChequeQualifier extends AnnotationLiteral<PayBy> implements PayBy {
        @Override
        public PaymentMethod value() {
            return PaymentMethod.CHEQUE;
        }

        @Override
        public String comment() {
            return "";
        }

    }

}
