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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        URL[] urls = new URL[2];
        Enumeration<URL> e = getClass().getClassLoader().getResources("test.txt");
        int i = 0;
        while (e.hasMoreElements()) {
            if (i == 2) {
                throw new ServletException(
                    "Wrong number of resource URLs, expected 2");
            }
            urls[i++] = e.nextElement();            
        }

        if (i != 2) {
            throw new ServletException(
                "Wrong number of resource URLs, expected 2");
        }

        getServletContext().log("urls[0]=" + urls[0]);
        getServletContext().log("urls[1]=" + urls[1]);

        if (!urls[0].toString().endsWith("test.txt") ||
                !urls[1].toString().endsWith("test.txt")) {
            throw new ServletException("Wrong resource URL(s)");
        }

        /*
         * Since delegate is set to false, we expect the first URL in the
         * returned enum:
         * 
         * jar:file:/space/luehe/ws/v3/distributions/web/target/glassfish\
	 *     /domains/domain1/applications\
         *     /web-classloader-get-resources-delegate-false-web/WEB-INF/lib\
         *     /mytest.jar!/test.txt
         * 
         * to be longer than the second:
         * 
         * jar:file:/space/luehe/ws/v3/distributions/web/target/glassfish\
         *     /lib/test.jar!/test.txt|#]
         */
        if (urls[0].toString().length() < urls[1].toString().length()) {
            throw new ServletException("Delegate flag not honored");
        }
    }
}



