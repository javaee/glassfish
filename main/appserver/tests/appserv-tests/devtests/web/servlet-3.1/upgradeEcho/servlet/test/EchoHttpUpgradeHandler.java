/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2014 Oracle and/or its affiliates. All rights reserved.
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

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

public class EchoHttpUpgradeHandler implements HttpUpgradeHandler {
    private String delimiter = "\\";

    public EchoHttpUpgradeHandler() {
    }

    public void init(WebConnection wc) {
        System.out.println("EchoProtocolHandler.init");
        try {
            ServletInputStream input = wc.getInputStream();
            ReadListenerImpl readListener = new ReadListenerImpl(delimiter, input, wc);
            input.setReadListener(readListener);
            wc.getOutputStream().flush();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void destroy() {
        System.out.println("--> destroy");
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return delimiter;
    }

    static class ReadListenerImpl implements ReadListener {
        ServletInputStream input = null;
        ServletOutputStream output = null;
        WebConnection wc = null;
        String delimiter = null;

        ReadListenerImpl(String d, ServletInputStream in, WebConnection c)
                throws IOException {
            delimiter = d;
            input = in;
            wc = c;
            output = wc.getOutputStream();
        }

        public void onDataAvailable() throws IOException {
            StringBuilder sb = new StringBuilder();
            System.out.println("--> onDataAvailable");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            System.out.println("#### Thread.currentThread.getContextClassloader(): " + cl);
            if (cl instanceof org.glassfish.web.loader.WebappClassLoader) {
                System.out.println("Correct ClassLoader");
            } else {
                System.out.println("ERROR Wrong ClassLoader!!!");
                sb.append("WrongClassLoader"); 
            }

            int len = -1;
            byte b[] = new byte[1024];
            while (input.isReady()
                    && (len = input.read(b)) != -1) {
                String data = new String(b, 0, len);
                System.out.println("--> " + data);
                sb.append(data);
            }
            output.print(delimiter + sb.toString());
            output.flush();
        }

        public void onAllDataRead() throws IOException {
            System.out.println("--> onAllDataRead");
            try {
                wc.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        public void onError(final Throwable t) {
            System.out.println("--> onError");
            t.printStackTrace();
            try {
                wc.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
