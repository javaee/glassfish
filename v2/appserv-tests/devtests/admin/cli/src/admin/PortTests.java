/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
        startDomain();
        verifyUserSuppliedPortNumbersAreUnique();
        verifyPortsAreLegal();
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
        nums[3]= -100;
        report("create-instance-" + iname + "illegalPortsSpecified", !asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", !asadmin("delete-local-instance", iname));

        nums[3] = 0;
        report("create-instance-" + iname + "illegalPortsSpecified", asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", asadmin("delete-local-instance", iname));

        nums[3] = 65535;
        report("create-instance-" + iname + "illegalPortsSpecified", asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", asadmin("delete-local-instance", iname));

        nums[3] += 1;
        report("create-instance-" + iname + "illegalPortsSpecified", !asadmin("create-local-instance", "--systemproperties", assembleEnormousPortsString(nums), iname));
        report("delete-instance-" + iname + "legalPortsSpecified", !asadmin("delete-local-instance", iname));
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
     */
}
