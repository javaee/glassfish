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
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/test", asyncSupported=true)
public class TestServlet extends HttpServlet {
    private static final int MAX_TIME_MILLIS = 10 * 1000;
    private static final int LENGTH = 500000;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        AsyncContext ac = req.startAsync();
        ServletOutputStream output = res.getOutputStream();
        WriteListenerImpl writeListener = new WriteListenerImpl(output, ac);
        output.setWriteListener(writeListener);
    }

    static class WriteListenerImpl implements WriteListener {
        private ServletOutputStream output = null;
        private AsyncContext ac = null;
        private int count = 0;

        WriteListenerImpl(ServletOutputStream sos,
                AsyncContext c) {
            output = sos;
            ac = c;
        }

        public void onWritePossible() throws IOException {
            if (count == 0) {
                long startTime = System.currentTimeMillis();
                while (output.isReady()) {
                    writeData(output);
                    count++;    
                    if (System.currentTimeMillis() - startTime > MAX_TIME_MILLIS
                            || count > 10) {
                        throw new IOException("Cannot fill the write buffer");
                    }
                }
            } else if (count > 0) {
                String message = "onWritePossible";
                System.out.println("--> " + message);
                output.write(message.getBytes());
                ac.complete();
            }
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }

    static void writeData(ServletOutputStream output) throws IOException {
        System.out.println("--> calling writeData");
        byte[] b = new byte[LENGTH];
        Arrays.fill(b, 0, LENGTH, (byte)'a');
        output.write(b);
    }
}
