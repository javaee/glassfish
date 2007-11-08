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

import java.io.File;
import java.util.Properties;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;

/**
 * Unit tests for timestamp command.
 *
 * @author Nazrul Islam
 */
public class TimestampCommandTest extends TestCase {
   
    public TimestampCommandTest(String name) {
        super(name);        
    }

    protected void setUp() {
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    }

    protected void tearDown() {
    }

    /**
     * Creates a synchronization response object.
     *
     * @return  synchronization response object
     */
    private SynchronizationResponse getResponseObj(long starttime) {

        SynchronizationRequest[] requests = new SynchronizationRequest[1];

        String fileName = ".";
        File file = new File(fileName);

        // synchronization request
        requests[0] = new SynchronizationRequest(
            fileName, ".", file.lastModified(),
            SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);            
        requests[0].setServerName("server");
        requests[0].setTimestampFileName("${com.sun.aas.instanceRoot}/t1");
        requests[0].setTargetDirectory("${config.name}/lib");
        requests[0].setCacheTimestampFile("/tmp/test/tsfile");

        // adds environment property
        requests[0].addEnvironmentProperty("com.sun.aas.instanceRoot", "/tmp");
        requests[0].addEnvironmentProperty("config.name", "server-config");

        // synchronization response
        SynchronizationResponse res = 
            new SynchronizationResponse(null, requests, 0, starttime, 0);

        return res;
    }

    /**
     * Tests time stamp command file update logic.
     */
    public void test() {
        System.out.println("--- TimestampCommand Test ---");
        try {
            SynchronizationResponse res = getResponseObj(2);
            SynchronizationRequest[] reqs = res.getReply();
            TimestampCommand tsc = new TimestampCommand(reqs[0], res);
            System.out.println("Command Name: " + tsc.getName());
            tsc.execute();

            assertTrue(tsc.getResult() == null);
            File f = reqs[0].getCacheTimestampFile();
            assertTrue( f.exists() );
            System.out.println("Timestamp saved to: " + f.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Tests time stamp command's validation logic. 
     */
    public void testFailure() {
        System.out.println("--- TimestampCommand Failure Test ---");
        try {
            SynchronizationResponse res = getResponseObj(1);
            SynchronizationRequest[] reqs = res.getReply();
            TimestampCommand tsc = new TimestampCommand(reqs[0], res);
            tsc.execute();

            // execute command should throw an exception 
            assertFalse( reqs[0].getCacheTimestampFile().exists() );

        } catch (Exception e) {
            System.out.println("New timestamp was smaller!");
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TimestampCommandTest.class);
    }
}
