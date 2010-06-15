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
    protected String getTestName() {
        return "Ports Tests";
    }

    @Override
    protected String getTestDescription() {
        return "Tests Port Selection Algorithms for Instances.";
    }

    public static void main(String[] args) {
        PortTests tests = new PortTests();
        tests.justDoIt();
    }

    private void justDoIt() {
        String iname = generateInstanceName();
        report("OK-Port", asadmin("create-local-instance", "--systemproperties", "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181", iname));


        // currently broken!!!!
        report("OK-Port-delete", asadmin("delete-local-instance", iname));

        
        iname = generateInstanceName();
        AsadminReturn ret = asadminWithOutput("create-local-instance", "--systemproperties", "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18080", iname);
        report("Duplicated-Port", ret.outAndErr.indexOf("TransactionFailure:") >= 0);
        stat.printSummary();
    }


/*
 * --systemproperties HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848 in1

 */

}
