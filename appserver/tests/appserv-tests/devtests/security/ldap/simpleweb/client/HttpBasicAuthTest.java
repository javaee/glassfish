/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
import sun.misc.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class HttpBasicAuthTest implements Runnable {

    private int suxesCount = 0;
    private int failureCount = 0;
    private boolean result = true;

    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;
    private long avgTime;
    private double stdDev  = 0;
    private long totalTime = 0;
    private long indiTimes[];
    private int indiIndex = 0;

    private String url;
    private String username;
    private String password;
    private int threadCount;
    private int loopCount;

    public HttpBasicAuthTest(String url, String username, String password,
        int threadCount, int loopCount) {

        this.url =  url;
        this.username = username;
        this.password = password;
        this.threadCount = threadCount;
        this.loopCount = loopCount;
    }

    public void doTest() {

        indiTimes = new long[threadCount*loopCount];
        for(int i=0; i<indiTimes.length; i++) {
            indiTimes[i] = 0;
        }

        Thread tarray[] = new Thread[threadCount];

        for(int i=0; i<threadCount; i++) 
            tarray[i] = new Thread(this, "Http-request-thread-" + i);

        for(int i=0; i<threadCount; i++)
            tarray[i].start();

        for(int i=0; i<threadCount; i++) {
            try {
                tarray[i].join();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        avgTime = totalTime/suxesCount;

        for(int i=0; i<indiIndex; i++) {
            stdDev += (indiTimes[i]-avgTime)*(indiTimes[i]-avgTime);
        }
        if( indiIndex>1)
            stdDev = stdDev/(indiIndex-1);

        stdDev = Math.pow(stdDev, 0.5);

        System.out.println("Total requests: " + (suxesCount+failureCount) + 
                           ", success count: " + suxesCount + 
                           ", failure count: " + failureCount);
        System.out.println("Min/Max/Avg/StdDev: (milliseconds) " + 
                           minTime + "/" + 
                           maxTime + "/" + avgTime + "/" + stdDev);

        String testId = "Sec::LDAP BasicAuth";
        stat.addDescription("Security::LDAP BasicAuth");
        if (result) {
            stat.addStatus(testId, stat.PASS);
        } else {
            stat.addStatus(testId, stat.FAIL);
        }
        stat.printSummary(testId);
    }

    public void run() {
        long st,et;

        for(int i=0; i<loopCount; i++) {

            try {
                st = System.currentTimeMillis();
                run0();
                et = System.currentTimeMillis();
                synchronized(this) {

                    suxesCount++;
                    long tt = et-st;

                    totalTime += tt;
                    indiTimes[indiIndex++] = tt;

                    if( tt > maxTime )
                        maxTime = tt;
                    if( tt < minTime )
                        minTime = tt;

                }
            } catch(Exception e) {
                e.printStackTrace();
                synchronized(this) {
                    failureCount++;
                }
                result = false;
                continue;
            }

        }
    }

    protected void run0() throws Exception {

            System.out.println("running ...");
            URL u = new URL(url);
            URLConnection uconn = u.openConnection();

            String up = username + ":" + password;
            BASE64Encoder be = new BASE64Encoder();
            up = new String(be.encode(up.getBytes()));

            uconn.setRequestProperty("authorization", "Basic " + up);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                uconn.getInputStream()));
            String lineread;
            while((lineread=reader.readLine()) != null ) {
                System.out.println(Thread.currentThread() + " -- " + lineread);
            }
    }


    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");


    public static final String URL_OPTION = "-url";
    public static final String USER_OPTION = "-user";
    public static final String PASS_OPTION = "-pass";
    public static final String THREADS_OPTION = "-tc";
    public static final String LOOP_OPTION = "-lc";

    public static void usage() {
        System.out.println("usage: java HttpBasicAuthTest -url <url> -user <user> -pass <pass> -tc <thread-count> -lc <loop-count>");
    }

    public static void main(String[] args) {

        String url = null;
        String user = null;
        String pass = null;
        int tc=-1;
        int lc=-1;

        for(int i=0; i<args.length; i++) {
            if( args[i].intern() == URL_OPTION.intern() ) {
                url = args[++i];
            } else if( args[i].intern() == USER_OPTION.intern() ) {
                user = args[++i];
            } else if( args[i].intern() == PASS_OPTION.intern() ) {
                pass = args[++i];
            } else if( args[i].intern() == THREADS_OPTION.intern() ) {
                tc = Integer.parseInt(args[++i]);
            } else if( args[i].intern() == LOOP_OPTION.intern() ) {
                lc = Integer.parseInt(args[++i]);
            } else {
                usage();
                System.exit(1);
            }
        }

        if( url == null || user == null || pass == null ||
            tc==-1 || lc==-1 ) {
            usage();
            System.exit(1);
        }

        HttpBasicAuthTest test = new HttpBasicAuthTest(url, user, pass, tc, lc);
        test.doTest();


    }

}
