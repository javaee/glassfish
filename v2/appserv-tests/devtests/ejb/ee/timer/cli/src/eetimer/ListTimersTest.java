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
package eetimer;

import java.util.Map;

public class ListTimersTest extends TimerTestBase {

    public static void main(String[] args) {
        (new ListTimersTest()).runTests();
    }

    @Override
    protected String getTestDescription() {
        return "devtests for list-timers";
    }

    public void runTests() {
        try {
            deployEjbCreateTimers(cluster_name);
            listTimersCluster();
            listTimers();
            listTimersInstance3Empty();
        } finally {
            undeployEjb(cluster_name);
        }

        try {
            deployEjbCreateTimers(instance_name_3);
            listTimersInstance3();
        } finally {
            undeployEjb(instance_name_3);
        }
        stat.printSummary();
    }

    public void listTimers() {
        String testName = "listTimers";
        AsadminReturn output = asadminWithOutput("list-timers");
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get("server") == 0);
    }

    //standalone instance
    public void listTimersInstance3Empty() {
        String testName = "listTimersInstance3Empty";
        AsadminReturn output = asadminWithOutput("list-timers", instance_name_3);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_3) == 0);
    }

    public void listTimersInstance3() {
        String testName = "listTimersInstance3";
        AsadminReturn output = asadminWithOutput("list-timers", instance_name_3);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_3) == 1);
    }

    public void listTimersCluster() {
        String testName = "listTimersCluster";
        AsadminReturn output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_1) == 1);
        report(testName, timerCounts.get(instance_name_2) == 1);
    }
}
