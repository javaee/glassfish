/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author bhavanishankar@java.net
 */

@WebServlet(name="SecureWebAppTestServlet", urlPatterns = "/SecureWebAppTestServlet")
public class SecureWebAppTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();

        print("\n[OUTPUT from SecureWebAppTestServlet]", out);
        print("[Hi from SecureWebAppTestServlet]", out);

        String sysProp = System.getProperty("org.glassfish.embedded.greeting");
        print("[System property org.glassfish.embedded.greeting = " + sysProp + "]", out);
        if(!"Hi from BHAVANI".equals(sysProp)) {
            httpServletResponse.sendError(500,
                    "System property org.glassfish.embedded.greeting not found");
            return;
        }

        Boolean directClassLoading = Boolean.getBoolean("ANTLR_USE_DIRECT_CLASS_LOADING");
        print("[System property ANTLR_USE_DIRECT_CLASS_LOADING = " +
                System.getProperty("ANTLR_USE_DIRECT_CLASS_LOADING") + "]", out);
        if(!directClassLoading) {
            httpServletResponse.sendError(500,
                    "System property ANTLR_USE_DIRECT_CLASS_LOADING is not set");
            return;
        }
        print("[End of OUTPUT from SecureWebAppTestServlet]", out);
        
        out.flush();
        out.close();
    }

    private void print(String msg, PrintWriter out) {
        out.println(msg);
        System.out.println(msg);
    }
}
