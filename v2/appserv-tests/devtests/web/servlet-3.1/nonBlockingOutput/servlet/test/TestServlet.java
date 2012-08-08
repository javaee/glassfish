/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    private static final int MAX_TIME_MILLIS = 10 * 1000;
    
    private static final int LENGTH = 587952;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        CountDownLatch latch = new CountDownLatch(1);

        ServletOutputStream output = res.getOutputStream();
        WriteListenerImpl writeListener = new WriteListenerImpl(output, latch);
        output.setWriteListener(writeListener);

        output.write("START\n".getBytes());    
        output.flush();

        long count = 0;
        System.out.println("--> Begin for loop");
        boolean prevCanWrite = true;
        final long startTimeMillis = System.currentTimeMillis();

        while ((prevCanWrite = output.canWrite())) {
            writeData(output, count);
            count++;

            if (System.currentTimeMillis() - startTimeMillis > MAX_TIME_MILLIS) {
                System.out.println("Error: can't overload output buffer");
                return;
            }
        }
        //output.flush();
        System.out.println("--> prevCanWriite = " + prevCanWrite + 
                ", count = " + count);

         try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                System.out.println("SUCCESS");
            } else {
                System.out.println("TIMEOUT");
            }
        } catch (InterruptedException e) {
        }
    }

    class WriteListenerImpl implements WriteListener {
        private ServletOutputStream output = null;
        private CountDownLatch latch = null;

        WriteListenerImpl(ServletOutputStream sos,
                CountDownLatch l) {
            output = sos;
            latch = l;
        }

        public void onWritePossible() {
            try {
                String message = "onWritePossible";
                System.out.println("--> " + message);
                output.write(message.getBytes());
            } catch(Exception ex) {
                throw new IllegalStateException(ex);
            } finally {
                latch.countDown();
            }
        }

        public void onError(final Throwable t) {
            t.printStackTrace();
        }
    }

    void writeData(ServletOutputStream output, long count) throws IOException {
        System.out.println("--> calling writeData " + count);
        char[] cs = String.valueOf(count).toCharArray();
        byte[] b = new byte[LENGTH];
        for (int i = 0; i < cs.length; i++) {
            b[i] = (byte)cs[i];
        }
        Arrays.fill(b, cs.length, LENGTH, (byte)'a');
        output.write(b);
    }
}
