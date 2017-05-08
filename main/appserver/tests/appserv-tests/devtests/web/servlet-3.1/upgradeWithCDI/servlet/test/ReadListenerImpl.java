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

package test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.WebConnection;

/**
 * The protocol is as follows:
 * Client:
 *  (a_isbn_to_be_verified CRLF)*| EXIT
 *   and tokens can be separated by " \t\n\r\f".
 * Server:
 *  (a_previous_isbn (true|false) CRLF)*
 */
class ReadListenerImpl implements ReadListener {
    private static final String EXIT = "EXIT";
    private static final String DELIMITER = " \t\n\r\f";
    private static final String SPACE = " ";
    private static final String CRLF = "\r\n";

    private String appName = null;
    private ServletInputStream input = null;
    private ServletOutputStream output = null;
    private WebConnection wc = null;
    private ISBNValidator isbnValidator = null;
    private boolean debug;

    private volatile String unprocessedData = "";

    ReadListenerImpl(String aName, ServletInputStream in, WebConnection c,
            ISBNValidator isbnV, boolean d) throws IOException {
        appName = aName;
        input = in;
        wc = c;
        isbnValidator = isbnV;
        debug = d;
        output = wc.getOutputStream();
    }

    @Override
    public void onDataAvailable() throws IOException {
        System.out.println("--> onDataAvailable");

        StringBuilder sb = new StringBuilder(unprocessedData);

        int len = -1;
        byte b[] = new byte[15];
        while (input.isReady()
                && (len = input.read(b)) != -1) {
            String data = new String(b, 0, len);
            if (debug) {
                System.out.println("--> " + data);
            }
            sb.append(data);
        }

        try {
            processData(sb.toString());
        } catch(IOException ioe) {
            throw ioe;
        } catch(RuntimeException re) {
            throw re;
        } catch(Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public void onAllDataRead() throws IOException {
        System.out.println("--> onAllDataRead");
        try {
            wc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onError(final Throwable t) {
        System.out.println("--> onError: " + t);
        t.printStackTrace();
        
        try {
            wc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void processData(String data) throws Exception {
        String lastToken = null;
        StringTokenizer tokens = new StringTokenizer(data, DELIMITER, true);
        boolean isExit = false;
        OutputStream output = wc.getOutputStream();
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (debug) {
                System.out.println("--> token: " + token);
            }
            if (DELIMITER.contains(token)) {
                if (lastToken != null) {
                    if (EXIT.equals(lastToken)) {
                        if (debug) {
                            System.out.println("--> found EXIT");
                        }
                        isExit = true;
                        break;
                    } else {
                        boolean result = isbnValidator.isValid(lastToken);
                        if (debug) {
                           System.out.println("--> " + lastToken + ": " + result);
                        }
                        output.write((lastToken + SPACE + result + CRLF).getBytes());
                        output.flush();
                   }
                }
                lastToken = null;
            } else {
                lastToken = token;
            }
        }

        unprocessedData = ((lastToken != null) ? lastToken : "");

        if (isExit) {
            if (debug) {
                System.out.println("--> WebConnection#close");
            }
            wc.close();
            return;
        }

        // testing checking
        InitialContext initialContext = new InitialContext();
        String aName = (String)initialContext.lookup("java:app/AppName");
        if (!appName.equals(aName)) {
            throw new IllegalStateException();
        }
    }
}
