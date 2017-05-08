/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin;

/**
 *
 * @author bnevins
 */
public class PortTests extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests Port Selection Algorithms for Instances.";
    }

    public static void main(String[] args) {
        PortTests tests = new PortTests();
        tests.runTests();
    }

    private void runTests() {
        report("01234567890123456789012345678901234567890123456789012345678901234567890123456789", true);
        startDomain();
        verifyUserSuppliedPortNumbersAreUnique();
        verifyPortsAreLegal();
        testConflictResolution();
        stopDomain();
        stat.printSummary();
    }

    private void verifyUserSuppliedPortNumbersAreUnique() {
        final int[] nums = new int[]{18080, 18181, 13800, 13700, 17676, 13801, 18686, 14848};
        String ports = assembleEnormousPortsString(nums);
        String iname = generateInstanceName();

        report("create-instance-" + iname + "-noPortsSpecified", asadmin("create-local-instance", iname));
        report("delete-instance-" + iname + "-noPortsSpecified", asadmin("delete-local-instance", iname));

        report("create-instance-" + iname + "-allGoodPortsSpecified", asadmin("create-local-instance", "--systemproperties", ports, iname));
        report("delete-instance-" + iname + "-allGoodPortsSpecified", asadmin("delete-local-instance", iname));

        for (int i = 0; i < 7; i++) {
            for (int j = i + 1; j < 8; j++) {
                ports = assembleEnormousPortsString(i, j, nums);
                AsadminReturn ret = asadminWithOutput("create-local-instance",
                        "--systemproperties",
                        ports,
                        iname);
                if (ret.returnValue) {
                    System.out.println("ERROR -- should have returned failure - it returned success!");
                    System.out.println(ret.outAndErr);
                    System.out.println(ports);
                    System.out.println("**** i,j = " + i + ", " + j);
                }
                report("create-instance-" + iname + "-duplicatePortsSpecified" + i + "-" + j, !ret.returnValue);
            }
        }
    }

    private void verifyPortsAreLegal() {
        final int[] nums = new int[]{18080, 18181, 13800, 13700, 17676, 13801, 18686, 14848};
        String iname = generateInstanceName();
        nums[3] = -100;
        report("create-instance-" + iname + "illegalPortsSpecified", !asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", !asadmin("delete-local-instance", iname));

        // UNIX -- if you are not superuser then we try incrementing 50 times and then quit.  Which is WAY under 1024.

        int newPort = 1000;

        if(isWindows())
            newPort = 0;

        nums[3] = newPort;
        report("create-instance-" + iname + "illegalPortsSpecified", asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", asadmin("delete-local-instance", iname));

        nums[3] = 65535;
        report("create-instance-" + iname + "illegalPortsSpecified", asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", asadmin("delete-local-instance", iname));

        nums[3] += 1;
        report("create-instance-" + iname + "illegalPortsSpecified", !asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", !asadmin("delete-local-instance", iname));
    }

    private void testConflictResolution() {
        //verifyCleanSlate();
        buildup();

        // check that the first instance is using the port that we expect
        report("byron1-uses-24848", doesGetMatch(
                "configs.config.byron1-config.system-property.ASADMIN_LISTENER_PORT.value",
                "24848"));

        report("instance-doesnt-use-22222", !doesGetMatch(
                "configs.config.byron1-config.system-property.ASADMIN_LISTENER_PORT.value",
                "22222"));

        // the **config** has 24848
        report("byron2-config-uses-24848", doesGetMatch(
                "configs.config.byron2-config.system-property.ASADMIN_LISTENER_PORT.value",
                "24848"));

        checkAndReportPort("byron2", 24849);
        checkAndReportPort("byron3", 24850);
        // clog up port 24851.  byron4 should automatically go to 24852
        report("Started-Fake-Server-Daemon-24851", true);
        runFakeServerDaemon(24851);
        report("create-byron4", asadminWithOutput("create-local-instance", "byron4"));
        checkAndReportPort("byron4", 24852);

        // if I delete byron2, then byron5 ought to re-use the port which is 24849
        report("delete-byron2", asadminWithOutput("delete-local-instance", "byron2"));
        report("create-byron5", asadminWithOutput("create-local-instance", "byron5"));
        checkAndReportPort("byron5", 24849);
        // bring byron2 up again -- teardown() will be looking for it
        report("delete-byron5", asadminWithOutput("delete-local-instance", "byron5"));
        report("create-byron2", asadminWithOutput("create-local-instance", "byron2"));
        checkAndReportPort("byron2", 24849);

        report("delete-byron4", asadmin("delete-local-instance", "byron4"));

        teardown();
        //verifyCleanSlate();
    }

    /*
    private void verifyCleanSlate() {
        // we are depending on there being ZERO instances and clusters!
        report("there-must-be-no-pre-existing-clusters", verifyNoClusters());
        report("there-must-be-no-pre-existing-instances", verifyNoInstances());
    }
     *
     */

    private void buildup() {
        report("create-cluster", asadmin("create-cluster", "c1"));
        report("create-byron1", asadmin("create-local-instance", "byron1"));
        report("create-byron2", asadminWithOutput("create-local-instance", "byron2"));
        report("create-byron3", asadminWithOutput("create-local-instance", "byron3"));
    }

    private void teardown() {
        report("delete-byron3", asadmin("delete-local-instance", "byron3"));
        report("delete-byron2", asadmin("delete-local-instance", "byron2"));
        report("delete-byron1", asadmin("delete-local-instance", "byron1"));
        report("delete-cluster", asadmin("delete-cluster", "c1"));
    }

    private String assembleEnormousPortsString(int index1, int index2, final int[] nums) {
        return assembleEnormousPortsString(makeDupes(index1, index2, nums));
    }

    private int[] makeDupes(int index1, int index2, int[] nums) {
        int[] copy = new int[8];
        System.arraycopy(nums, 0, copy, 0, 8);
        copy[index2] = nums[index1];
        return copy;
    }

    private String assembleEnormousPortsString(int[] nums) {
        if (nums == null || nums.length != 8)
            throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP_LISTENER_PORT").append("=" + nums[0]).append(":");
        sb.append("HTTP_SSL_LISTENER_PORT").append("=" + nums[1]).append(":");
        sb.append("IIOP_SSL_LISTENER_PORT").append("=" + nums[2]).append(":");
        sb.append("IIOP_LISTENER_PORT").append("=" + nums[3]).append(":");
        sb.append("JMX_SYSTEM_CONNECTOR_PORT").append("=" + nums[4]).append(":");
        sb.append("IIOP_SSL_MUTUALAUTH_PORT").append("=" + nums[5]).append(":");
        sb.append("JMS_PROVIDER_PORT").append("=" + nums[6]).append(":");
        sb.append("ASADMIN_LISTENER_PORT").append("=" + nums[7]);

        return sb.toString();

    }

    private void checkAndReportPort(String instance, int port) {
        report(instance + "-server-element-uses-" + port, doesGetMatch(
                "servers.server." + instance + ".system-property.ASADMIN_LISTENER_PORT.value",
                "" + port));
    }
}

/*
 * --systemproperties HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848 in1
 * --systemproperties
HTTP_LISTENER_PORT=18080:
HTTP_SSL_LISTENER_PORT=18181:
IIOP_SSL_LISTENER_PORT=13800:
IIOP_LISTENER_PORT=13700:
JMX_SYSTEM_CONNECTOR_PORT=17676:
IIOP_SSL_MUTUALAUTH_PORT=13801:
JMS_PROVIDER_PORT=18686:
ASADMIN_LISTENER_PORT=14848
configs.config.byron1-config.system-property.ASADMIN_LISTENER_PORT.name=ASADMIN_LISTENER_PORT
configs.config.byron1-config.system-property.ASADMIN_LISTENER_PORT.value=24848
configs.config.byron1-config.system-property.HTTP_LISTENER_PORT.name=HTTP_LISTENER_PORT
configs.config.byron1-config.system-property.HTTP_LISTENER_PORT.value=28080
configs.config.byron1-config.system-property.HTTP_SSL_LISTENER_PORT.name=HTTP_SSL_LISTENER_PORT
configs.config.byron1-config.system-property.HTTP_SSL_LISTENER_PORT.value=28181
configs.config.byron1-config.system-property.IIOP_LISTENER_PORT.name=IIOP_LISTENER_PORT
configs.config.byron1-config.system-property.IIOP_LISTENER_PORT.value=23700
configs.config.byron1-config.system-property.IIOP_SSL_LISTENER_PORT.name=IIOP_SSL_LISTENER_PORT
configs.config.byron1-config.system-property.IIOP_SSL_LISTENER_PORT.value=23820
configs.config.byron1-config.system-property.IIOP_SSL_MUTUALAUTH_PORT.name=IIOP_SSL_MUTUALAUTH_PORT
configs.config.byron1-config.system-property.IIOP_SSL_MUTUALAUTH_PORT.value=23920
configs.config.byron1-config.system-property.JMS_PROVIDER_PORT.name=JMS_PROVIDER_PORT
configs.config.byron1-config.system-property.JMS_PROVIDER_PORT.value=27676
configs.config.byron1-config.system-property.JMX_SYSTEM_CONNECTOR_PORT.name=JMX_SYSTEM_CONNECTOR_PORT
configs.config.byron1-config.system-property.JMX_SYSTEM_CONNECTOR_PORT.value=28686
 */
