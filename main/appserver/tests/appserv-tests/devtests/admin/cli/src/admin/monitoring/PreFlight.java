/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin.monitoring;

import static admin.monitoring.Constants.*;

/**
 * Enforce PreFlight Assumptions
 * This should never fail on a Hudson build and can easily fail for a developer
 * environment (e.g. Oops I left a GlassFish server running on port 28080!)
 * @author Byron Nevins
 */
public class PreFlight extends MonTest {
    @Override
    void runTests(TestDriver driver) {
                new String();



        setDriver(driver);
        report(true, "PreFlight here!!!");
        boolean b1 = wget(8080, "");
        boolean b2 = wget(28080, "");
        boolean b3 = wget(28081, "");
        report(!b1, "Port 8080 Clear");
        report(!b2, "Port 28080 Clear");
        report(!b3, "Port 28081 Clear");
        // todo check that DB is **not** running

        if (b1 || b2 || b3) {
            report(false, "Monitoring Pre-Flight Failed::Aborted All Tests");
            System.out.println(SCREAMING_LOUD_MESSAGE);
            throw new RuntimeException("PreFlight");
        }
    }
    private static final String SCREAMING_LOUD_MESSAGE =
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "*****FATAL ERROR -- ABORTING MONITORING TESTS !!!!!***\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n";
}
