package org.jvnet.hk2.test;

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

    public void run() {
        File reportDir = null;
        if(context.getArguments().containsKey("-r"))
            reportDir = new File(context.getArguments().get("-r"));

        runTests(reportDir);
    }

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
