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

package eetimer;

import admin.AdminBaseDevTest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerTestBase extends AdminBaseDevTest {
    protected static final String ejb_jar_name = System.getProperty("ejb-jar-name");
    protected static final String ejb_jar_path = System.getProperty("ejb-jar-path");
    protected static final String cluster_name = System.getProperty("cluster-name");
    protected static final String instance_name_1 = System.getProperty("instance-name-1");
    protected static final String instance_name_2 = System.getProperty("instance-name-2");
    protected static final String instance_name_3 = System.getProperty("instance-name-3");

    protected static final Logger logger = Logger.getLogger(TimerTestBase.class.getName());

    @Override
    protected String getTestDescription() {
        return "devtests for ejb ee timer";
    }

    protected void deployEjbCreateTimers(String target) {
        AsadminReturn output = asadminWithOutput(
                "deploy", "--force", "true", "--target", target, ejb_jar_path);
        logger.log(Level.INFO, output.outAndErr);
    }

    protected void undeployEjb(String target) {
        String moduleName = ejb_jar_name.substring(0, ejb_jar_name.indexOf("."));
        AsadminReturn output = asadminWithOutput(
                "undeploy", "--target", target, moduleName);
        logger.log(Level.INFO, output.outAndErr);
    }

    /**
     * Parses list-timers output and save the result to a Map of instanceName:timerCount
     * @param output output from asadmin command list-timers
     * @return resultMap whose key is the instance name, and value is the number of timers.
     */
    protected Map<String, Integer> countInstanceTimers(String output) {
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        String[] lines = output.split(System.getProperty("line.separator"));
        //the first line is the asadmin command line itself.
        //If the output format changes in the future, need to change here too
        for(int i = 1; i < lines.length; i++) {
            String[] pair = lines[i].split(":");
            String k = pair[0].trim();
            String v = pair[1].trim();
            if(!k.isEmpty()) {
                resultMap.put(k, Integer.valueOf(v));
            }
        }
        logger.log(Level.INFO, "instance::timer map: {0}, from output: {1}",
                new Object[]{resultMap, output});
        return resultMap;
    }
}
