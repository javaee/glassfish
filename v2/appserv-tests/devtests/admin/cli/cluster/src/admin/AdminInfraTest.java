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
package admin;


import com.sun.appserv.test.BaseDevTest;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

/*
 * Dev test for create/delete/list cluster
 * @author Bhakti Mehta
 */
public class AdminInfraTest extends BaseDevTest {

    private static final boolean DEBUG = false;

    public static void main(String[] args)  {
        AdminInfraTest test = new AdminInfraTest();
        test.run();
    }

    @Override
    protected String getTestName() {
        return "cluster";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for create/delete/list cluster";
    }

    public void run() {
        report("create-cluster", asadmin("create-cluster","cl1"));

        //create-cluster using existing config
        report("create-cluster-with-config", asadmin("create-cluster",
                "--config" ,"cl1-config",
                "cl2"));

        //check for duplicates
        report("create-cluster-duplicates", !asadmin("create-cluster","cl1"));

        //create-cluster using non existing config
        report("create-cluster-nonexistent-config", !asadmin("create-cluster",
                "--config" ,"junk-config",
                "cl3"));

        //create-cluster using systemproperties
        report("create-cluster-system-props", asadmin("create-cluster",
                "--systemproperties" ,"foo=bar",
                "cl4"));

        //evaluate using xpath that there are 3 elements in the domain.xml
        String xpathExpr = "count"+"("+"/domain/clusters/cluster"+")";

        Object o = evalXPath(xpathExpr, XPathConstants.NUMBER);
        System.out.println ("No of cluster elements in cluster: "+o);
        if (o instanceof Double) {
            report ("evaluation-xpath-create-cluster",o.equals(new Double("3")));
        } else {
            report ("evaluation-xpath-create-cluster",false);
        }

        //list-clusters
        report("list-clusters", asadmin("list-clusters"));
        cleanup();
        stat.printSummary();

    }

    @Override
    public void cleanup(){
        //Cleanup the code so that tests run successfully next time
        report("delete-cl1", asadmin("delete-cluster", "cl1"));
        report("delete-cl2", asadmin("delete-cluster", "cl2"));
        report("delete-cl3", !asadmin("delete-cluster", "cl3")); // should not have been created
        report("delete-cl4", asadmin("delete-cluster", "cl4"));
    }
}
