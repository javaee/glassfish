/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package org.glassfish.admin.payload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.Payload.Inbound;
import org.glassfish.api.admin.Payload.Outbound;
import org.glassfish.api.admin.Payload.Part;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class PayloadFilesManagerTest {

    private Logger defaultLogger = Logger.getAnonymousLogger();
    private PayloadFilesManager tempMgr;

    public PayloadFilesManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            tempMgr = new PayloadFilesManager.Temp(defaultLogger);
        } catch (Exception e) {
            fail("Could not set up class for test run" + e.getLocalizedMessage());
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getOutputFileURI method, of class PayloadFilesManager.
     */
    @Test
    public void testGetOutputFileURI() throws Exception {
        System.out.println("getOutputFileURI");

        PayloadFilesManager instance = new PayloadFilesManager.Temp(Logger.getAnonymousLogger());
        String originalPath = "way/over/there/myApp.ear";
        Part testPart = PayloadImpl.Part.newInstance("text/plain", originalPath, null, "random content");
        URI result = instance.getOutputFileURI(testPart, testPart.getName());
        System.out.println("  " + originalPath + " -> " + result);
        assertTrue(result.toASCIIString().endsWith("/myApp.ear"));
    }

    @Test
    public void testDiffFilesFromSamePath() throws Exception {
        new CommonTest() {

            @Override
            protected void addParts(Outbound ob,
                    PayloadFilesManager instance) throws Exception {
                ob.addPart("text/plain", "dir/x.txt", null, "sample data");
                ob.addPart("text/plain", "dir/y.txt", null, "y content in same temp dir as dir/x.txt");
            }

            @Override
            protected void checkResults(Inbound ib, PayloadFilesManager instance) throws Exception {
                List<File> files = instance.extractFiles(ib);
                File parent = null;
                boolean success = true;
                // Make sure all files have the same parent - since in this test
                // they all came from the same path originally.
                for (File f : files) {
                    System.out.println("  " + f.toURI().toASCIIString());
                    if (parent == null) {
                        parent = f.getParentFile();
                    } else {
                        success &= (parent.equals(f.getParentFile()));
                    }
                }
                assertTrue("Failed because the temp files should have had the same parent", success);
            }

        }.run("diffFilesFromSamePath");
    }

    @Test
    public void testSameFilesInDiffPaths() throws Exception {
        new CommonTest() {

            @Override
            protected void addParts(Outbound ob, PayloadFilesManager instance) throws Exception {
                ob.addPart("text/plain", "here/x.txt", null, "data from here");
                ob.addPart("text/plain", "elsewhere/x.txt", null, "data from elsewhere");
            }

            @Override
            protected void checkResults(Inbound ib, PayloadFilesManager instance) throws Exception {
                List<File> files = instance.extractFiles(ib);
                boolean success = true;
                String fileName = null;
                List<File> parents = new ArrayList<File>();
                for (File f : files) {
                    if (fileName == null) {
                        fileName= f.getName();
                    } else {
                        success &= (f.getName().equals(fileName)) && ( ! parents.contains(f.getParentFile()));
                    }
                    System.out.println("  " + f.toURI().toASCIIString());
                }
                assertTrue("Failed because temp file names did not match or at least two had a parent in common", success);
            }

        }.run("sameFilesInDiffPaths");
    }

    @Test
    public void testLeadingSlashes() throws Exception {


        new CommonTest() {
            private static final String originalPath = "/here/x.txt";
            private final File originalFile = new File(originalPath);

            @Override
            protected void addParts(Outbound ob, PayloadFilesManager instance) throws Exception {
                ob.addPart("application/octet-stream", originalPath, null, "data from here");
            }

            @Override
            protected void checkResults(Inbound ib, PayloadFilesManager instance) throws Exception {
                List<File> files = instance.extractFiles(ib);
                System.out.println("  Original: " + originalFile.toURI().toASCIIString());

                for (File f : files) {
                    System.out.println("  Temp file: " + f.toURI().toASCIIString());
                    if (f.equals(originalFile)) {
                        fail("Temp file was created at original top-level path; should have been in a temp dir");
                    }
                }
            }
        }.run("testLeadingSlashes");
    }

    @Test
    public void testPathlessFile() throws Exception {
        new CommonTest() {

            @Override
            protected void addParts(Outbound ob, PayloadFilesManager instance) throws Exception {
                ob.addPart("application/octet-stream", "flat.txt", null, "flat data");
                ob.addPart("text/plain", "x/other.txt", null, "one level down");
            }

            @Override
            protected void checkResults(Inbound ib, PayloadFilesManager instance) throws Exception {
                List<File> files = instance.extractFiles(ib);
                boolean success = true;
                for (File f : files) {
                    if (f.getName().equals("flat.txt")) {
                        success &= (f.getParentFile().equals(instance.getTargetDir()));
                    }
                    System.out.println("  " + f.toURI().toASCIIString());
                }
                System.out.println("  Done");
                assertTrue("Flat file was not deposited in top-level temp directory", success);
            }
        }.run("testPathlessFile");
    }

    @Test
    public void testWindowsPath() throws Exception {
        System.out.println("testWindowsPath");
        testForBadChars("C:\\Program Files\\someDir");
    }

    @Test
    public void testNonWindowsPath() throws Exception {
        System.out.println("testNonWindowsPath");
        testForBadChars("/Users/whoever/someDir");

    }

    private void testForBadChars(String initialPath) {
        URI uri = null;
        URI targetDirURI = null;
        try {
            PayloadFilesManager.Temp instance = new PayloadFilesManager.Temp(Logger.getAnonymousLogger());
            uri = instance.getTempSubDirForPath(initialPath);
            targetDirURI = instance.getTargetDir().toURI();

            System.out.println("  " + initialPath + " -> " + uri.toASCIIString());
            String uriString = targetDirURI.relativize(uri).toASCIIString();
            
            // trim the trailing slash for the directory
            uriString = uriString.substring(0, uriString.length() - 1);
            assertFalse("path " + uriString + " still contains bad character(s)",
                    uriString.contains("/") ||
                    uriString.contains("\\") ||
                    uriString.contains(":"));        } catch (Exception e) {
            fail("unexpected exception " + e.getLocalizedMessage());
        }
    }
    private abstract class CommonTest {

        private String payloadType = "application/zip";

        protected abstract void addParts(final Payload.Outbound ob,
                final PayloadFilesManager instance) throws Exception;

        protected abstract void checkResults(final Payload.Inbound ib,
                final PayloadFilesManager instance) throws Exception;

        public void run(String testName) throws Exception {
            File tempZipFile = null;

            System.out.println(testName);


            try {
                PayloadFilesManager.Temp instance = new PayloadFilesManager.Temp(Logger.getAnonymousLogger());
                tempZipFile = File.createTempFile("testzip", ".zip");
                Payload.Outbound ob = PayloadImpl.Outbound.newInstance();

                addParts(ob, instance);

                OutputStream os;
                ob.writeTo(os = new BufferedOutputStream(new FileOutputStream(tempZipFile)));
                os.close();

                Payload.Inbound ib = PayloadImpl.Inbound.newInstance(payloadType, new BufferedInputStream(new FileInputStream(tempZipFile)));

                checkResults(ib, instance);

                instance.cleanup();
            } finally {
                if (tempZipFile != null) {
                    tempZipFile.delete();
                }
            }

        }
    }

}