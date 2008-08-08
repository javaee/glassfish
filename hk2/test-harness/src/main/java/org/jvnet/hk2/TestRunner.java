/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.istack.test.AntXmlFormatter;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class TestRunner implements ModuleStartup {
    StartupContext context;

    @Inject
    Habitat habitat;

    public void setStartupContext(StartupContext context) {
        // TODO: inject
        this.context = context;
    }

    public void start() {
        File reportDir = null;
        if(context.getArguments().containsKey("-r"))
            reportDir = new File(context.getArguments().get("-r"));

        runTests(reportDir);
    }

    public void stop() {}

    /**
     * Runs all the tests and returns the result.
     *
     * @param reportDir
     *      If non-null, XML reports of the test runs will be placed in this folder.
     */
    public TestResult runTests(final File reportDir) {
        TestSuite ts = new TestSuite();

        for(Inhabitant<? extends HK2TestCase> testCase : habitat.getInhabitants(HK2TestCase.class)) {
            ts.addTestSuite(testCase.type());
        }

        // custom TestRunner that can generate Ant format report
        junit.textui.TestRunner testRunner = new junit.textui.TestRunner() {
            private AntXmlFormatter formatter;

            protected TestResult createTestResult() {
                TestResult result = super.createTestResult();

                if(context.getArguments().containsKey("-r")) {
                    reportDir.mkdirs();
                    formatter = new AntXmlFormatter(XMLJUnitResultFormatter.class, reportDir);
                    result.addListener(formatter);
                }
                return result;
            }

            public TestResult doRun(Test test) {
                try {
                    return super.doRun(test);
                } finally {
                    if(formatter!=null)
                        formatter.close();
                }
            }
        };

        // run the test
        return testRunner.doRun(ts);
    }
}
