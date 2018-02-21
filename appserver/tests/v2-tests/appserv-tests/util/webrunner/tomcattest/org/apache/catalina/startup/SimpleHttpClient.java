/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.startup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple client for unit testing. It isn't robust, it isn't secure and
 * should not be used as the basis for production code. Its only purpose
 * is to do the bare minimum for the unit tests.
 */
public abstract class SimpleHttpClient {
    public static final String TEMP_DIR =
        System.getProperty("java.io.tmpdir");

    public static final boolean DEBUG = Boolean.getBoolean("debug");

    public static final String CRLF = "\r\n";

    public static final String INFO_100 = "HTTP/1.1 100";
    public static final String OK_200 = "HTTP/1.1 200";
    public static final String REDIRECT_302 = "HTTP/1.1 302";
    public static final String FAIL_400 = "HTTP/1.1 400";
    public static final String FAIL_404 = "HTTP/1.1 404";
    public static final String FAIL_413 = "HTTP/1.1 413";
    public static final String FAIL_50X = "HTTP/1.1 50";
    public static final String FAIL_500 = "HTTP/1.1 500";
    public static final String FAIL_501 = "HTTP/1.1 501";

    private static final String SESSION_COOKIE_HEADER_PREFIX =
        "Set-Cookie: JSESSIONID=";

    private Socket socket;
    private Writer writer;
    private BufferedReader reader;
    private String host = "localhost";
    private int port = 8080;

    private String[] request;
    private boolean useContinue = false;
    private int requestPause = 1000;

    private String responseLine;
    private List<String> responseHeaders = new ArrayList<String>();
    private String responseBody;
    private boolean useContentLength;

    protected void setHost(String theHost) {
        host = theHost;
    }

    protected void setPort(int thePort) {
        port = thePort;
    }

    public void setRequest(String[] theRequest) {
        request = theRequest;
    }

    public void setUseContinue(boolean theUseContinueFlag) {
        useContinue = theUseContinueFlag;
    }

    public boolean getUseContinue() {
        return useContinue;
    }

    public void setRequestPause(int theRequestPause) {
        requestPause = theRequestPause;
    }

    public String getResponseLine() {
        return responseLine;
    }

    public List<String> getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setUseContentLength(boolean b) {
        useContentLength = b;
    }

    public String getSessionId() {
        for (String header : responseHeaders) {
            if (header.startsWith(SESSION_COOKIE_HEADER_PREFIX)) {
                header = header.substring(SESSION_COOKIE_HEADER_PREFIX.length());
                header = header.substring(0, header.indexOf(';'));
                return header;
            }
        }
        return null;
    }

    public void connect(int connectTimeout, int soTimeout) throws UnknownHostException, IOException {
        final String encoding = "ISO-8859-1";
        SocketAddress addr = new InetSocketAddress(host, port);
        socket = new Socket();
        socket.setSoTimeout(soTimeout);
        socket.connect(addr,connectTimeout);
        OutputStream os = socket.getOutputStream();
        writer = new OutputStreamWriter(os, encoding);
        InputStream is = socket.getInputStream();
        Reader r = new InputStreamReader(is, encoding);
        reader = new BufferedReader(r);
    }
    public void connect() throws UnknownHostException, IOException {
        connect(0,0);
    }

    public void processRequest() throws IOException, InterruptedException {
        processRequest(true);
    }

    public void processRequest(boolean readBody) throws IOException, InterruptedException {
        sendRequest();

        readResponse(readBody);

    }

    public void sendRequest() throws InterruptedException, IOException {
        // Send the request
        boolean first = true;
        for (String requestPart : request) {
            if (first) {
                first = false;
            } else {
                Thread.sleep(requestPause);
            }
            writer.write(requestPart);
            writer.flush();
            debug(requestPart);
        }
    }

    public void readResponse(boolean readBody) throws IOException {
        // Reset fields use to hold response
        responseLine = null;
        responseHeaders.clear();
        responseBody = null;

        // Read the response
        responseLine = readLine();

        // Is a 100 continue response expected?
        if (useContinue) {
            if (isResponse100()) {
                // Skip the blank after the 100 Continue response
                readLine();
                // Now get the final response
                responseLine = readLine();
            } else {
                throw new IOException("No 100 Continue response");
            }
        }

        // Put the headers into the map
        String line = readLine();
        int cl = -1;
        while (line!=null && line.length() > 0) {
            responseHeaders.add(line);
            line = readLine();
            if (line != null && line.startsWith("Content-Length: ")) {
                cl = Integer.parseInt(line.substring(16));
            }
        }

        // Read the body, if any
        StringBuilder builder = new StringBuilder();
        if (readBody) {
            if (cl > -1 && useContentLength) {
                char[] body = new char[cl];
                reader.read(body);
                builder.append(body);
            } else {
                line = readLine();
                while (line != null) {
                    builder.append(line);
                    line = readLine();
                }
            }
        }
        responseBody = builder.toString();
    }

    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            debug(line);
        }
        return line;
    }

    public void disconnect() throws IOException {
        if (writer != null) {
            try {
                writer.close();
            } catch(Throwable t) {
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch(Throwable t) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch(Throwable t) {
            }
        }
    }

    public void reset() {
        socket = null;
        writer = null;
        reader = null;

        request = null;
        requestPause = 1000;

        useContinue = false;

        responseLine = null;
        responseHeaders = new ArrayList<String>();
        responseBody = null;
    }

    public boolean isResponse100() {
        return getResponseLine().startsWith(INFO_100);
    }

    public boolean isResponse200() {
        return getResponseLine().startsWith(OK_200);
    }

    public boolean isResponse302() {
        return getResponseLine().startsWith(REDIRECT_302);
    }

    public boolean isResponse400() {
        return getResponseLine().startsWith(FAIL_400);
    }

    public boolean isResponse404() {
        return getResponseLine().startsWith(FAIL_404);
    }

    public boolean isResponse413() {
        return getResponseLine().startsWith(FAIL_413);
    }

    public boolean isResponse50x() {
        return getResponseLine().startsWith(FAIL_50X);
    }

    public boolean isResponse500() {
        return getResponseLine().startsWith(FAIL_500);
    }

    public boolean isResponse501() {
        return getResponseLine().startsWith(FAIL_501);
    }

    public Socket getSocket() {
        return socket;
    }

    public abstract boolean isResponseBodyOK();

    private void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
