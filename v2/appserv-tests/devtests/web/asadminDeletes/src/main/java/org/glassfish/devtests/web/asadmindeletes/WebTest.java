/*
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
package org.glassfish.devtests.web.asadmindeletes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
 * Unit test for asadmin deletes.
 */
public class WebTest {
    private static final String TEST_NAME = "asadmin-deletes";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private final String name = System.currentTimeMillis() + "";
    private final PrintWriter writer;

    public WebTest() throws FileNotFoundException {
        writer = new PrintWriter("test.out");
    }

    public static void main(String[] args) throws FileNotFoundException {
        stat.addDescription("Unit test for deleting referenced domain.xml entities");
        new WebTest().run();
    }

    public void run() {
        asadmin2("create-threadpool", false, "create-threadpool", name);
        asadmin2("create-transport", false, "create-transport", name);
        asadmin2("create-protocol", false, "create-protocol", name);
        asadmin2("create-http", false, "create-http", "--default-virtual-server", "server", name);
        asadmin2("create-network-listener", false, "create-network-listener",
            "--listenerport", "10000",
            "--protocol", name,
            "--threadpool", name,
            "--transport", name,
            name);
        asadmin2("delete-threadpool", true, "delete-threadpool", name);
        asadmin2("delete-transport", true, "delete-transport", name);
        asadmin2("delete-protocol", true, "delete-protocol", name);
        asadmin2("delete-network-listener", false, "delete-network-listener", name);
        asadmin2("delete-protocol-2", false, "delete-protocol-2", name);
        asadmin2("delete-threadpool-2", false, "delete-threadpool", name);
        asadmin2("delete-transport-2", false, "delete-transport", name);
        stat.printSummary();
    }

    private void asadmin2(String step, boolean shouldFail, final String... args) {
        List<String> command = new ArrayList<String>();
        command.add("asadmin");
//        command.add("--echo=true");
//        command.add("--terse=true");
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        String status = SimpleReporterAdapter.FAIL;
        Process process = null;
        boolean failed = false;
        try {
            process = builder.start();
            InputStream inStream = process.getInputStream();
            InputStream errStream = process.getErrorStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            try {
                final byte[] buf = new byte[1000];
                int read;
                while ((read = inStream.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
                while ((read = errStream.read(buf)) != -1) {
                    err.write(buf, 0, read);
                }
            } finally {
                errStream.close();
                inStream.close();
            }
            String outString = new String(out.toByteArray()).trim();
            String errString = new String(err.toByteArray()).trim();
            failed = outString.matches("Command.*failed\\.");
//            if (failed) {
                print(outString, "out");
                print(errString, "err");
//            }
            if(failed) {
                status = shouldFail ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL;
            } else {
                status = shouldFail ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (shouldFail) {
                status = SimpleReporterAdapter.PASS;
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        write(String.format("*** %s (shouldFail=%b, failed=%b) ==> %s", step, shouldFail, failed, status));
        stat.addStatus(step, status);
    }

    private void print(final String string, final String name) {
        if (string.length() != 0) {
            write(String.format("*** %s = \"%s\"", name, string));
        }
    }

    private void write(final String out) {
        writer.println(out);
        writer.flush();
    }

}
