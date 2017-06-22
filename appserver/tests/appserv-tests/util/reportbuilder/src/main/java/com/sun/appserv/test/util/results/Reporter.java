/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Main class used for Uniform reporting of results
 *
 * @author Ramesh.Mandava
 * @author Justin.Lee@sun.com
 */
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "StaticNonFinalField"})
public class Reporter implements Serializable {
    private static Reporter reporterInstance = null;
    private String resultFile = "default.xml";
    transient public PrintWriter out = new PrintWriter(System.out);
    private List<TestSuite> suites = new ArrayList<TestSuite>();

    public String getResultFile() {
        return resultFile;
    }

    public void setTestSuite(TestSuite suite) {
        if (suite != null) {
            suites.add(suite);
        }
    }

    public static Reporter getInstance(String home) {
        if (reporterInstance == null) {
            String path = new File(".").getAbsolutePath();
            String outputDir = path.substring(0, path.indexOf(home)) + home;
            reporterInstance = new Reporter(outputDir + "/test_results.xml");
        }
        return reporterInstance;
    }

    private Reporter(String resultFilePath) {
        try {
            resultFile = resultFilePath;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    flushAll();
                }
            }));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void flushAll() {
        try {
            if ("default.xml".equals(resultFile)) {
                InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream();
                byte[] bytes = new byte[200];
                in.read(bytes);
                String file = "result_";
                String machineName = new String(bytes).trim();
                file += machineName;
                Calendar cal = Calendar.getInstance();
                String month = Integer.toString(cal.get(Calendar.MONTH));
                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                String year = Integer.toString(cal.get(Calendar.YEAR));
                file += "_" + month + day + year + ".xml";
                resultFile = file;
            }
            FileOutputStream output = new FileOutputStream(resultFile, true);
            Iterator<TestSuite> it = suites.iterator();
            while (it.hasNext()) {
                if (flush(it.next(), output)) {
                    it.remove();
                }
            }
            output.close();
            suites.clear();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
        }
    }

    /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite.
     *
     * @param suite the test suite
     * @param output the FileOutputStream in which we need to write.
     *
     * @return returns true if the file is successfully created
     */
    public boolean flush(TestSuite suite, FileOutputStream output) {
        try {
            if (suite != null && !suite.getWritten()) {
                suite.setWritten(writeXMLFile(suite.toXml(), output));
                return suite.getWritten();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean writeXMLFile(String xmlStringBuffer, FileOutputStream out) {
        try {
            out.write(xmlStringBuffer.getBytes());
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
