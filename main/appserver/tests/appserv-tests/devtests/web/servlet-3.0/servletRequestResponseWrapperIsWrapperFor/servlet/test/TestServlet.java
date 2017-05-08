/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        ServletRequestWrapper reqWrapper1 = new SubFirstLevelRequestWrapper(req);
        ServletRequestWrapper reqWrapper2 = new SecondLevelRequestWrapper(reqWrapper1);
        ServletRequestWrapper reqWrapper3 = new ThirdLevelRequestWrapper(reqWrapper2);
       
        if (!reqWrapper3.isWrapperFor(reqWrapper1) ||
                !reqWrapper3.isWrapperFor(FirstLevelRequestWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        MyRequestWrapper myReqWrapper = new MyRequestWrapper(req);

        if (reqWrapper3.isWrapperFor(myReqWrapper) ||
                reqWrapper3.isWrapperFor(MyRequestWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        ServletResponseWrapper resWrapper1 = new SubFirstLevelResponseWrapper(res);
        ServletResponseWrapper resWrapper2 = new SecondLevelResponseWrapper(resWrapper1);
        ServletResponseWrapper resWrapper3 = new ThirdLevelResponseWrapper(resWrapper2);
       
        if (!resWrapper3.isWrapperFor(resWrapper1) ||
                !resWrapper3.isWrapperFor(FirstLevelResponseWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        MyResponseWrapper myResWrapper = new MyResponseWrapper(res);

        if (resWrapper3.isWrapperFor(myResWrapper) ||
                resWrapper3.isWrapperFor(MyResponseWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

    }

    static class FirstLevelRequestWrapper extends ServletRequestWrapper {
        public FirstLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class SubFirstLevelRequestWrapper extends FirstLevelRequestWrapper {
        public SubFirstLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class SecondLevelRequestWrapper extends ServletRequestWrapper {
        public SecondLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class ThirdLevelRequestWrapper extends ServletRequestWrapper {
        public ThirdLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class MyRequestWrapper extends ServletRequestWrapper {
        public MyRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class FirstLevelResponseWrapper extends ServletResponseWrapper {
        public FirstLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class SubFirstLevelResponseWrapper extends FirstLevelResponseWrapper {
        public SubFirstLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class SecondLevelResponseWrapper extends ServletResponseWrapper {
        public SecondLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class ThirdLevelResponseWrapper extends ServletResponseWrapper {
        public ThirdLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class MyResponseWrapper extends ServletResponseWrapper {
        public MyResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

}
