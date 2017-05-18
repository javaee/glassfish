/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package ear.earwithall2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestDeplOrder {

    public static void main (String[] args) {
        TestDeplOrder client = new TestDeplOrder();
        client.doTest(args);
    }
    
    public void doTest(String[] args) {

        String path = args[0];
        try {
            log("Test: devtests/deployment/ear/earwithall2");
            log("looking at " + path);
            boolean success = readFile(path, "Loading application WebNBean_ejb", "Loading application WebNBean_war");
            if (success) {
              pass();
            } else {
              fail();
            }
	} catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean readFile(String path, String first, String second)
            throws IOException, FileNotFoundException {
        int firstLine = -1;
        int secondLine = -1;
        BufferedReader reader =
          new BufferedReader(new FileReader(new File(path)));
        String line = reader.readLine();
        int totalLines = 0;
        while (line != null) {
            ++totalLines;
            if ((firstLine < 0) && (line.contains(first))) {
                firstLine = totalLines;
            }
            if ((secondLine < 0) && (line.contains(second))) {
                secondLine = totalLines;
            }
            line = reader.readLine();
        }
        reader.close();
        log("first line:  " + firstLine);
        log("second line:  " + secondLine);
        if ((firstLine < 0) ||
            (secondLine < 0))
          return false;
        if (firstLine < secondLine) {
          return true;
        }
        return false;
    }
    private void log(String message) {
        System.err.println("[ear.earwithall2.TestDeplOrder]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/ear/earwithall2");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/ear/earwithall2");
        System.exit(-1);
    }
}
