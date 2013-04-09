/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee7.batch.test;

import org.glassfish.javaee7.batch.test.util.CommandUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.StringTokenizer;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BatchCommandsTest {

    private static final String VALID_TIMER_POOL_DATA_SOURCE_NAME = "jdbc/__TimerPool";

    private static final String VALID_DEFAULT_DATA_SOURCE_NAME = "jdbc/__default";

    private static final String VALID_EXECUTOR_NAME = "concurrent/__defaultManagedExecutorService";

    private static final String STAND_ALONE_INSTANCE_NAME = "batch-server";

    private static final String SET_BATCH_RUNTIME_COMMAND = "set-batch-runtime-configuration";

    private static final String SERVER_ES1 = "concurrent/__ES1";

    private static final String SERVER_ES2 = "concurrent/__ES2";

    private static final String BATCH_SERVER_ES1 = "concurrent/__BatchServer_ES1";

    private static final String BATCH_SERVER_ES2 = "concurrent/__BatchServer_ES2";

    @BeforeClass
    public static void setup() {
        CommandUtil cmd1 = CommandUtil.getInstance().executeCommandAndGetAsList("asadmin", "start-domain", "-d");
        CommandUtil cmd2 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "create-local-instance", STAND_ALONE_INSTANCE_NAME);
        CommandUtil cmd3 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "start-local-instance", STAND_ALONE_INSTANCE_NAME);
        CommandUtil cmd4 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "create-managed-executor-service", "--target", "server", SERVER_ES1);
        CommandUtil cmd5 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "create-managed-executor-service", SERVER_ES2);
        CommandUtil cmd6 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "create-managed-executor-service", "--target", STAND_ALONE_INSTANCE_NAME, BATCH_SERVER_ES1);
        CommandUtil cmd7 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "create-managed-executor-service", "--target", STAND_ALONE_INSTANCE_NAME, BATCH_SERVER_ES2);

        CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "list-managed-executor-services");
        CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "list-managed-executor-services", "--target", STAND_ALONE_INSTANCE_NAME);

//        assert(cmd1.ranOK() && cmd2.ranOK() && cmd3.ranOK() && cmd4.ranOK() && cmd5.ranOK() && cmd6.ranOK() && cmd7.ranOK());
        assertTrue(true);
    }

    @AfterClass
     public static void unsetup() {
        CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "delete-managed-executor-service", "--target", STAND_ALONE_INSTANCE_NAME, BATCH_SERVER_ES1);
        CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "delete-managed-executor-service", "--target", STAND_ALONE_INSTANCE_NAME, BATCH_SERVER_ES2);
        CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "delete-managed-executor-service", SERVER_ES1);
        CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "delete-managed-executor-service", SERVER_ES2);
        CommandUtil cmd3 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "stop-local-instance", STAND_ALONE_INSTANCE_NAME);
        CommandUtil cmd2 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", "delete-local-instance", STAND_ALONE_INSTANCE_NAME);
        CommandUtil cmd1 = CommandUtil.getInstance().executeCommandAndGetAsList("asadmin", "stop-domain");
        assert(true);
    }


    @Test
    public void listRuntimeConfigurationServerTest() {
        String[] data = getConfigurationData("server");
        assertTrue(data.length == 2 && data[0] != null && data[1] != null);
    }

    @Test
    public void listRuntimeConfigurationBatchServerTest() {
        String[] data = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(data.length == 2 && data[0] != null && data[1] != null);
    }

    @Test
    public void setBatchRuntimeConfigurationWithNoArgsTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND);
        System.out.println("==> " + cmd.result().get(0));
        String expectedMessage = "remote failure: Either dataSourceLookupName or executorServiceLookupName must be specified.";
        assertTrue(!cmd.ranOK() && assertSameConfigurationData(null, origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setBatchRuntimeConfigurationWithNoArgsServerTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server");
        System.out.println("==> " + cmd.result().get(0));
        String expectedMessage = "remote failure: Either dataSourceLookupName or executorServiceLookupName must be specified.";
        assertTrue(!cmd.ranOK() && assertSameConfigurationData("server", origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setBatchRuntimeConfigurationWithNoArgsBatchServerTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME);
        System.out.println("==> " + cmd.result().get(0));
        String expectedMessage = "remote failure: Either dataSourceLookupName or executorServiceLookupName must be specified.";
        assertTrue(!cmd.ranOK() && assertSameConfigurationData(null, origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setInvalidBatchRuntimeConfigurationTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "-d", "foo");
        String expectedMessage = "remote failure: foo is not mapped to a DataSource";
        assertTrue(!cmd.ranOK() && assertSameConfigurationData(null, origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setInvalidBatchRuntimeConfigurationServerTest() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server", "-d", "foo");
        String expectedMessage = "remote failure: foo is not mapped to a DataSource";
        assertTrue(!cmd.ranOK() && assertSameConfigurationData("server", origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setInvalidBatchRuntimeConfigurationBatchServerTest() {
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-d", "foo2");
        String expectedMessage = "remote failure: foo2 is not mapped to a DataSource";

        System.out.println("** setInvalidBatchRuntimeConfigurationBatchServerTest ==> "
            + "ranOK: " + cmd.ranOK() + "; message: " + cmd.result().get(0).startsWith(expectedMessage));

        assertTrue(!cmd.ranOK() && assertSameConfigurationData(STAND_ALONE_INSTANCE_NAME, origEntries)
                && cmd.result().get(0).startsWith(expectedMessage));
    }

    @Test
    public void setValidExecutorConfigurationTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "-x", VALID_EXECUTOR_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData("server");
        assertTrue(newEntries[0].equals(origEntries[0]) && newEntries[1].equals(VALID_EXECUTOR_NAME));
    }

    @Test
    public void setValidExecutorConfigurationServerTest() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server", "-x", VALID_EXECUTOR_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData("server");
        assertTrue(newEntries[0].equals(origEntries[0]) && newEntries[1].equals(VALID_EXECUTOR_NAME));
    }

    @Test
    public void setValidExecutorConfigurationBatchServerTest() {
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-x", VALID_EXECUTOR_NAME);
        String[] output = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        if (!cmd.ranOK()) {
            cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                    "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-x", VALID_EXECUTOR_NAME);
            output = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        }
        System.out.println("**[##]** setValidExecutorConfigurationBatchServerTest ==> " + cmd.ranOK()
                + "; " + output[0] + "; " + output[1]);
        assertTrue(cmd.ranOK() && assertSameConfigurationData(STAND_ALONE_INSTANCE_NAME,
                new String[]{origEntries[0], VALID_EXECUTOR_NAME}));
    }

    @Test
    public void setValidDataSourceConfigurationTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "-d", VALID_TIMER_POOL_DATA_SOURCE_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData(null);
        assertTrue(newEntries[0].equals(VALID_TIMER_POOL_DATA_SOURCE_NAME) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setValidDataSourceConfigurationServerTest() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server", "-d", VALID_TIMER_POOL_DATA_SOURCE_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData("server");
        assertTrue(newEntries[0].equals(VALID_TIMER_POOL_DATA_SOURCE_NAME) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setValidButNonExistentDataSourceConfigurationBatchServerTest() {

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-d", VALID_DEFAULT_DATA_SOURCE_NAME);

        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        CommandUtil errorCmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-d", VALID_TIMER_POOL_DATA_SOURCE_NAME);
        assertTrue(!errorCmd.ranOK());
        String[] newEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(newEntries[0].equals(VALID_DEFAULT_DATA_SOURCE_NAME) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setValidDataSourceConfigurationBatchServerTest() {
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME, "-d", VALID_DEFAULT_DATA_SOURCE_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(newEntries[0].equals(VALID_DEFAULT_DATA_SOURCE_NAME) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setValidDataSourceAndExecutorConfigurationTest() {
        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND,
                "-d", VALID_DEFAULT_DATA_SOURCE_NAME, "-x", VALID_EXECUTOR_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData(null);
        System.out.println("**[setValidDataSourceAndExecutorConfigurationTest]: " +  cmd.ranOK()
                + " ; " + newEntries[0] + "    " + newEntries[1]);
        assertTrue(newEntries[0].equals(VALID_DEFAULT_DATA_SOURCE_NAME) && newEntries[1].equals(VALID_EXECUTOR_NAME) );

    }

    @Test
    public void setValidDataSourceAndExecutorConfigurationBatchServerTest() {
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-d", VALID_DEFAULT_DATA_SOURCE_NAME, "-x", VALID_EXECUTOR_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(newEntries[0].equals(VALID_DEFAULT_DATA_SOURCE_NAME) && newEntries[1].equals(VALID_EXECUTOR_NAME) );
    }

    @Test
    public void setValidDataSourceAndExecutorConfigurationServerTest() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-d", VALID_TIMER_POOL_DATA_SOURCE_NAME, "-x", VALID_EXECUTOR_NAME);
        assertTrue(cmd.ranOK());
        String[] newEntries = getConfigurationData("server");
        assertTrue(newEntries[0].equals(VALID_TIMER_POOL_DATA_SOURCE_NAME) && newEntries[1].equals(VALID_EXECUTOR_NAME) );
    }

    @Test
    public void setInvalidDataSourceAndExecutorConfigurationTest() {
        String[] origEntries = getConfigurationData(null);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND,
                "-x", VALID_DEFAULT_DATA_SOURCE_NAME, "-d", VALID_EXECUTOR_NAME);
        String[] newEntries = getConfigurationData(null);
        assertTrue(!cmd.ranOK()
                && newEntries[0].equals(origEntries[0]) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setInvalidDataSourceAndExecutorConfigurationBatchServerTest() {
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-x", VALID_DEFAULT_DATA_SOURCE_NAME, "-d", VALID_EXECUTOR_NAME);
        String[] newEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(!cmd.ranOK()
                && newEntries[0].equals(origEntries[0]) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setInvalidDataSourceAndExecutorConfigurationServerTest() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-x", VALID_TIMER_POOL_DATA_SOURCE_NAME, "-d", VALID_EXECUTOR_NAME);
        String[] newEntries = getConfigurationData("server");
        assertTrue(!cmd.ranOK()
                && newEntries[0].equals(origEntries[0]) && newEntries[1].equals(origEntries[1]) );
    }

    @Test
    public void setAvailableButInvalidExecNamesForServer() {
        String[] origEntries = getConfigurationData("server");

        CommandUtil cmd1 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-x", BATCH_SERVER_ES1);
        CommandUtil cmd2 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-x", BATCH_SERVER_ES2);
        String[] newEntries = getConfigurationData("server");
        assertTrue(!cmd1.ranOK() && !cmd2.ranOK()
                && newEntries[0].equals(origEntries[0]) && newEntries[1].equals(origEntries[1]) );

        CommandUtil cmd3 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-x", SERVER_ES1);
        CommandUtil cmd4 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", "server",
                "-x", SERVER_ES2);
        newEntries = getConfigurationData("server");
        assertTrue(cmd3.ranOK() && cmd4.ranOK()
                && newEntries[0].equals(origEntries[0]) && newEntries[1].equals(SERVER_ES2));

        CommandUtil cmd5 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "-x", SERVER_ES2);
        CommandUtil cmd6 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "-x", SERVER_ES1);
        String[] newEntries2 = getConfigurationData(null);
        assertTrue(cmd5.ranOK() && cmd6.ranOK()
                && newEntries2[0].equals(newEntries[0]) && newEntries2[1].equals(SERVER_ES1));

    }

    @Test
    public void setAvailableButInvalidExecNamesForBatchServer() {
        String[] origServerEntries = getConfigurationData(null);
        String[] origEntries = getConfigurationData(STAND_ALONE_INSTANCE_NAME);

        CommandUtil cmd1 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-x", SERVER_ES1);
        CommandUtil cmd2 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-x", SERVER_ES2);
        assertTrue(!cmd1.ranOK() && !cmd2.ranOK()
                && assertSameConfigurationData(STAND_ALONE_INSTANCE_NAME, origEntries)
                && assertSameConfigurationData(null, origServerEntries));


        CommandUtil cmd3 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-x", BATCH_SERVER_ES1);
        CommandUtil cmd4 = CommandUtil.getInstance().executeCommandAndGetAsList(
                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
                "-x", BATCH_SERVER_ES2);
        System.out.println("**##@@ => " + cmd3.ranOK() + "; " + cmd4.ranOK());
        for (String s : cmd3.result()) {
            System.out.println("cmd3 **=> " + s);
        }
        for (String s : cmd4.result()) {
            System.out.println("cmd4 **=> " + s);
        }
        assertTrue(cmd3.ranOK() && cmd4.ranOK()
                && assertSameConfigurationData(STAND_ALONE_INSTANCE_NAME, new String[]{origEntries[0], BATCH_SERVER_ES2})
                && assertSameConfigurationData(null, origServerEntries));
//
//        CommandUtil cmd5 = CommandUtil.getInstance().executeCommandAndGetAsList(
//                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
//                "-x", BATCH_SERVER_ES2);
//        CommandUtil cmd6 = CommandUtil.getInstance().executeCommandAndGetAsList(
//                "asadmin", SET_BATCH_RUNTIME_COMMAND, "--target", STAND_ALONE_INSTANCE_NAME,
//                "-x", BATCH_SERVER_ES1);
//        assertTrue(cmd5.ranOK() && cmd6.ranOK() && assertSameConfigurationData(null, origServerEntries)
//            && assertSameConfigurationData(STAND_ALONE_INSTANCE_NAME, new String[] {origEntries[0], BATCH_SERVER_ES1}));

    }

    @Test
    public void listBatchJobsTest() {
        getListBatchJobsData(null);
        assertTrue(true);
    }

    @Test
    public void listBatchJobsServerTest() {
        getListBatchJobsData("server");
        assertTrue(true);
    }

    @Test
    public void listBatchJobsBatchServerTest() {
        getListBatchJobsData(STAND_ALONE_INSTANCE_NAME);
        assertTrue(true);
    }

    @Test
    public void listRuntimeConfigurationTest() {
        String[] data = getConfigurationData(null);
        assertTrue(data.length == 2 && data[0] != null && data[1] != null);
    }

    @Test
    public void testListJobsWithJustJobNameHeader() {
        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "list-batch-jobs");
        int size = cmd.result().size();
        assertTrue(cmd.ranOK());

        CommandUtil cmdWithJustName = CommandUtil.getInstance().executeCommandAndGetAsList("asadmin",
                "list-batch-jobs", "-o", "jobname");
        assertTrue(cmdWithJustName.ranOK() && cmdWithJustName.result().size() == cmd.result().size());
    }


    @Test
    public void testListJobsWithInvalidHeader() {
        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetErrorOutput("asadmin",
                "list-batch-jobs", "-o", "abc");
        assertTrue(!cmd.ranOK() && "remote failure: Invalid header abc".equals(cmd.result().get(0)));
    }

    private String[] getConfigurationData(String target) {
        String[] command = target == null
                ? new String[] {"asadmin", "list-batch-runtime-configuration"}
                : new String[] {"asadmin", "list-batch-runtime-configuration", "--target", target};
        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(true, command);
        String output = cmd.result().get(1);
        String[] data = new String[2];
        StringTokenizer stok = new StringTokenizer(output, " \t\n\r");
        if (stok.hasMoreTokens()) data[0] = stok.nextToken();
        if (stok.hasMoreTokens()) data[1] = stok.nextToken();

        return data;
    }

    private boolean assertSameConfigurationData(String target, String[] orig) {
        String[] data = getConfigurationData(target);
        boolean result = orig[0].equals(data[0]) && orig[1].equals(data[1]);
        if (! result) {
            System.out.println("orig[0] == " + orig[0] + "             data[0] == " + data[0]);
            System.out.println("orig[1] == " + orig[1] + "             data[1] == " + data[1]);
        }

        return result;
    }

    private List<String> getListBatchJobsData(String target) {
        String[] command = target == null
                ? new String[] {"asadmin", "list-batch-jobs"}
                : new String[] {"asadmin", "list-batch-jobs", "--target", target};
        CommandUtil cmd = CommandUtil.getInstance().executeCommandAndGetAsList(command);
        return cmd.result();
    }

}
