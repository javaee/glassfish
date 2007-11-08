/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.synchronization;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import javax.management.ObjectName;

import javax.management.MBeanServerConnection;

import java.io.File;

import java.util.zip.CRC32;
import com.sun.enterprise.ee.util.zip.Unzipper;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Class SynchronizationTest
 *
 */
public class SynchronizationTest extends TestCase {
   
    protected static Thread _serverThread;    
    protected static MBeanServerConnection _serverConnection;
    protected static Server _server;
       
    static {
        initialize();
    }

    public SynchronizationTest(String name) {
        super(name);        
    }

    private static void initialize() {
        try {
            //Start up the server
            _server = new Server(Server.DEFAULT_CONNECTOR_PORT);            
            _serverThread = new Thread(_server);
            _serverThread.start();
            Thread.currentThread().sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
        
        try {            
            //Create a client connection to the newly created server. Requests 
            //will be sent over this.
            Client client = new Client("localhost", Server.DEFAULT_CONNECTOR_PORT);        
            _serverConnection = client.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    protected void setUp() {
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    }

    protected void tearDown() {
    }

    public void testEmptySync() {
        try {
            SynchronizationRequest[] requests = new SynchronizationRequest[1];              
            requests[0] = new SynchronizationRequest(
                "cf897yt5dfdb99iu",
                ".", 0, SynchronizationRequest.TIMESTAMP_NONE, null);            
            SynchronizationResponse result =
                (SynchronizationResponse)_serverConnection.invoke(
                    _server.getSynchronizationMBeanName(), "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            assertEquals(result.getZipBytes().length, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public void testEmptySync2() {
        try {
            SynchronizationRequest[] requests = new SynchronizationRequest[1];
            String[] files = (new File(".")).list();
            String fileName = files[0];
            File file = new File(fileName);
            requests[0] = new SynchronizationRequest(
                fileName, ".", file.lastModified(),
                SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);            
            SynchronizationResponse result =
                (SynchronizationResponse)_serverConnection.invoke(
                    _server.getSynchronizationMBeanName(), "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            assertEquals(result.getZipBytes().length, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    public void testEmptySync3() {
        try {
            SynchronizationRequest[] requests = new SynchronizationRequest[1];
            String[] files = (new File(".")).list();
            String fileName = files[0];
            File file = new File(fileName);
            requests[0] = new SynchronizationRequest(
                fileName, ".", System.currentTimeMillis(),
                SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE, null);            
            SynchronizationResponse result =
                (SynchronizationResponse)_serverConnection.invoke(
                    _server.getSynchronizationMBeanName(), "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            assertEquals(result.getZipBytes().length, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public void testSync() {
        try {
            String resultDirectory = Utils.getTemporaryDirectory("testSync");
            SynchronizationRequest[] requests = new SynchronizationRequest[1];
            String[] files = (new File(".")).list();
            String fileName = files[0];
            File file = new File(fileName);
            requests[0] = new SynchronizationRequest(
                fileName, ".", 0L,
                SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);            
            SynchronizationResponse result = (SynchronizationResponse)_serverConnection.invoke(
                    _server.getSynchronizationMBeanName(), "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            Unzipper z = new Unzipper(resultDirectory);
            long checksum = z.writeZipBytes(result.getZipBytes());
            //Check to make sure that the two match
            assertEquals(result.getChecksum(), checksum);
            assertTrue(Utils.areIdentical(fileName, 
                resultDirectory + File.separator + fileName));    
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public void testSync2() {
        try {
            String resultDirectory = Utils.getTemporaryDirectory("testSync2");
            SynchronizationRequest[] requests = new SynchronizationRequest[2];
            String[] files = (new File(".")).list();            
            System.out.println("files0 " + files[0]);
            System.out.println("files1 " + files[1]);
            requests[0] = new SynchronizationRequest(
                files[0], ".", 0L,
                SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);
            requests[1] = new SynchronizationRequest(
                files[1], ".", 0L,
                SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null); 
            SynchronizationResponse result =
                (SynchronizationResponse)_serverConnection.invoke(
                    _server.getSynchronizationMBeanName(), "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            Unzipper z = new Unzipper(resultDirectory);
            long checksum = z.writeZipBytes(result.getZipBytes());
            //Check to make sure that the two match
            assertEquals(result.getChecksum(), checksum);
            assertTrue(Utils.areIdentical(files[0], 
                resultDirectory + File.separator + files[0]));    
            assertTrue(Utils.areIdentical(files[1], 
                resultDirectory + File.separator + files[1]));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
   
    /*
    public static TestSuite suite() {
        //To run all tests
        return new TestSuite(SynchronizationTest.class);
        //To run a subset of the tests
        TestSuite suite = new TestSuite();
        suite.addTest(new SynchronizationTest("testEmptySync"));       
        suite.addTest(new SynchronizationTest("testEmptySync2"));       
        return suite;
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationTest.class);
    }
}
