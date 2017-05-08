/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/test", asyncSupported=true)
public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String test = req.getParameter("testname");
        AsyncContext ac = req.startAsync();
        ac.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) {
                System.out.println("my asyncListener.onComplete");
            }
            public void onError(AsyncEvent event) {
                System.out.println("my asyncListener.onError");
            }
            public void onStartAsync(AsyncEvent event) {
                System.out.println("my asyncListener.onStartAsync");
            }
            public void onTimeout(AsyncEvent event) {
                System.out.println("my asyncListener.onTimeout");
            }
        });
        ServletOutputStream output = res.getOutputStream();
        ServletInputStream input = req.getInputStream();
        ReadListenerImpl readListener = new ReadListenerImpl(input, output, ac);
        input.setReadListener(readListener);
    }

    static class ReadListenerImpl implements ReadListener {
        private ServletInputStream input = null;
        private ServletOutputStream output = null;
        private AsyncContext ac = null;

        ReadListenerImpl(ServletInputStream in, ServletOutputStream out,
                AsyncContext c) {
            input = in;
            output = out;
            ac = c;
        }

        public void onDataAvailable() throws IOException {
            StringBuilder sb = new StringBuilder();
            System.out.println("--> onDataAvailable");
            int len = -1;
            byte b[] = new byte[1024];
            while (input.isReady() 
                    && (len = input.read(b)) != -1) {
                String data = new String(b, 0, len);
                System.out.println("--> " + data);
                sb.append('/' + data);
                //output.print('/' + data);
            }
            output.print(sb.toString());
        }

        public void onAllDataRead() throws IOException {
            try {
                System.out.println("--> onAllDataRead");
                output.println("-onAllDataRead");
            } finally {
                ac.complete();
            }
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }
}
