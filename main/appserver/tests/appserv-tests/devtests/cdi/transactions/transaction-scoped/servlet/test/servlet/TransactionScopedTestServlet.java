/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import test.beans.Bean1;
import test.beans.Bean2;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import java.io.IOException;

@WebServlet(name = "TransactionScopedTestServlet", urlPatterns = {"/TransactionScopedTestServlet"})
public class TransactionScopedTestServlet extends HttpServlet {
    public static boolean bean1PreDestroyCalled = false;
    public static boolean bean2PreDestroyCalled = false;

    @Inject
    UserTransaction userTransaction;

    @Inject
    Bean1 bean1;

    @Inject
    Bean1 bean1_1;

    @Inject
    Bean2 bean2;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer msg = new StringBuffer();
        ServletOutputStream m_out = response.getOutputStream();

        msg.append( "@TransactionScoped Test");
        try {
            userTransaction.begin();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean1PreDestroyCalled = false;
        bean2PreDestroyCalled = false;
        msg.append(doActiveTransaction("First"));

        try {
            userTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        msg.append( checkPreDestroyCalled("First") );
        try {
            userTransaction.begin();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean1PreDestroyCalled = false;
        bean2PreDestroyCalled = false;
        msg.append(doActiveTransaction("Second"));
        try {
            userTransaction.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.append( checkPreDestroyCalled("Second") );

        try {
            bean1.foo();
            msg.append("Should have gotten a ContextNotActiveException.\n");
        } catch (ContextNotActiveException cnae) {
        }

        m_out.print( msg.toString() );
    }

    private String doActiveTransaction( String transNum ) {
        StringBuffer msg = new StringBuffer();
        String bean1Foo = bean1.foo();
        String bean1_1Foo = bean1_1.foo();
        String bean2Foo = bean2.foo();

        if ( bean1PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean1.preDestroyCalled initialized incorrectly.\n");
        }

        if ( bean2PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean2.preDestroyCalled initialized incorrectly.\n");
        }

        if (!bean1Foo.equals(bean1_1Foo)) {
            msg.append( transNum + " Transaction bean1 does not equal bean1_1.  It should.\n");
        }

        if (bean2Foo.equals(bean1Foo)) {
            msg.append(transNum + " Transaction bean2 equals bean1.  It should not.\n");
        }

        return msg.toString();
    }

    private String checkPreDestroyCalled( String transNum ) {
        StringBuffer msg = new StringBuffer();

        if ( ! bean1PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean1.preDestroyCalled not called.\n");
        }

        if ( ! bean2PreDestroyCalled ) {
            msg.append( transNum + " bean2.preDestroyCalled not called.\n");
        }

        return msg.toString();
    }

}
