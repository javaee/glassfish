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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
 * Unit test for comet: echo.
 */
public class WebTest {

    private static final String TEST_NAME = "comet-echo";

    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for comet: echo");
        WebTest webTest = new WebTest(args);
        webTest.run();

    }

    public void run() {
        try {
            String urlStr = "http://" + host + ":" + port + contextRoot + "/echo";
            int numOfClients = 10;
            CountDownLatch startSignal = new CountDownLatch(numOfClients);
            CountDownLatch endSignal = new CountDownLatch(numOfClients);
            ExecutorService executorService = Executors.newFixedThreadPool(numOfClients);
            List<Future<String>> futures = new ArrayList<Future<String>>();

            for (int i = 0; i < numOfClients; i++) {
                futures.add(executorService.submit(
                        new Worker(urlStr, startSignal, endSignal)));
            }

            System.out.println("Wait to start ...");
            boolean ss = startSignal.await(numOfClients * 2000, TimeUnit.MILLISECONDS);
            System.out.println(ss);

            String message = "abc";
            System.out.println("Sending message: " + message);
            writeMessage(urlStr, message);

            System.out.println("Wait to end ...");
            boolean es = endSignal.await(numOfClients * 1000, TimeUnit.MILLISECONDS);
            System.out.println(es);
            boolean valid = true;
            for (Future<String> future : futures) {
                String returnedMessage = future.get(1000, TimeUnit.MILLISECONDS);
                System.out.println("Got message : " + returnedMessage);
                valid = valid && (message.equals(returnedMessage));
            }
            executorService.shutdown();

            stat.addStatus(TEST_NAME, ((valid)? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private void writeMessage(String urlStr, String message) throws Exception {
        OutputStream os = null;
        BufferedWriter bw = null;
        String line = null;

        try{
            URL url = new URL(urlStr);
            String data = "msg=" + URLEncoder.encode(message);
            HttpURLConnection urlConnection =  (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            os = urlConnection.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(data);
            bw.flush();

            int statusCode = urlConnection.getResponseCode();
            if (HttpURLConnection.HTTP_OK != statusCode) {
                throw new IllegalStateException("Incorrect return code: " + statusCode);
            } 
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }
    }

    private static class Worker implements Callable<String> {
        private String urlStr;
        private CountDownLatch startSignal;
        private CountDownLatch endSignal;

        public Worker(String urlStr, CountDownLatch startSignal,
                CountDownLatch endSignal) throws Exception {

            this.urlStr = urlStr;
            this.startSignal = startSignal;
            this.endSignal = endSignal;
        }

        public String call() {
            String listenMessage;
            InputStream is = null;
            BufferedReader br = null;

            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection =  (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                is = urlConnection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                // OK message
                System.out.println(br.readLine());

                startSignal.countDown();
                listenMessage = br.readLine();
                endSignal.countDown();
            } catch( Exception ex){
                ex.printStackTrace();   
                throw new IllegalStateException("Test UNPREDICTED-FAILURE");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
            return listenMessage;
        }
    }
}
