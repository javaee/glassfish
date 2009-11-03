package com.sun.appserv.test.util.results;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Main class used for Uniform reporting of results
 *
 * @author Ramesh.Mandava
 * @author Justin.Lee@sun.com
 */
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "StaticNonFinalField"})
public class Reporter extends Thread implements Serializable {
    private static Reporter reporterInstance = null;
    private String resultFile = "default.xml";
    private static final String ws_home = "sqe-pe";
    transient public PrintWriter out = new PrintWriter(System.out);
    private List<TestSuite> suites = new ArrayList<TestSuite>();

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(final String resultFile) {
        this.resultFile = resultFile;
    }

    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String id) {
        setTestSuite(id, ReporterConstants.NA, ReporterConstants.NA);
    }

    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String id, String name) {
        setTestSuite(id, name, ReporterConstants.NA);
    }

    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String id, String name, String description) {
        TestSuite suite = null;
        for (TestSuite test : suites) {
            if (id.equals(test.getId())) {
                suite = test;
            }
        }
        if (suite == null) {
            suite = new TestSuite(id, name, description);
            suites.add(suite);
        }
    }

    /**
     * After using setthing Test Suite info. We need to use this addTest method for adding information about particular
     * Test. We need to  pass both TestSuiteId and TestId
     */
    public void addTest(String testSuiteId, String testId) {
        addTest(testSuiteId, testId, ReporterConstants.NA, ReporterConstants.NA);
    }

    /**
     * After setting Test Suite info. suing setTestSuite, We need to use this addTest method for adding information
     * about particular Test. We need to  pass both TestSuiteId and TestId  along with othe info of Test
     */
    public void addTest(String testSuiteId, String testId, String testName) {
        addTest(testSuiteId, testId, testName, ReporterConstants.NA);
    }

    /**
     * After setting Test Suite info. suing setTestSuite, We need to use this addTest method for adding information
     * about particular Test. We need to  pass both TestSuiteId and TestId  along with othe info of Test
     */
    public void addTest(String testSuiteId, String testId, String testName, String testDescription) {
        TestSuite suite = findTestSuite(testSuiteId);
        if (suite == null) {
            return;
        }
        suite.addTest(new Test(testId, testName, testDescription));
    }

    /**
     * After adding a Test using addTest , We need to use this setTestStatus method for setting Test status(pass/fail)
     * information about particular Test. We need to  pass both TestSuiteId and TestId  along with status information.
     * This is optional as in some case only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status) {
        setTestStatus(testSuiteId, testId, status, ReporterConstants.NA);
    }

    /**
     * After adding a Test using addTest , We need to use this setTestStatus method for setting Test status(pass/fail)
     * information about particular Test. We need to  pass both TestSuiteId and TestId  along with status information.
     * This is optional as in some case only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status, String statusDescription) {
        TestSuite suite = findTestSuite(testSuiteId);
        if (suite == null) {
            return;
        }
        Test test = findTest(testId, suite);
        if (test == null) {
            return;
        }
        test.setStatus(status);
        test.setStatusDescription(statusDescription);
    }

    /**
     * After adding a Test using addTest , We need to use this setTestStatus method for setting Test status(pass/fail)
     * information about particular Test. We need to  pass both TestSuiteId and TestId  al ong with expected and actual
     * result. This is optional as in some case only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status, String expected, String actual) {
        TestSuite suite = findTestSuite(testSuiteId);
        if (suite == null) {
            return;
        }
        Test test = findTest(testId, suite);
        if (test == null) {
            return;
        }
        test.setStatus(status);
        test.setExpected(expected);
        test.setActual(actual);
    }

    /**
     * After adding a Test using addTest, We need to use this addTestCase method for adding information about particular
     * TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of
     * TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId) {
        TestSuite suite = findTestSuite(testSuiteId);
        if (suite == null) {
            return;
        }
        Test test = findTest(testId, suite);
        if (test == null) {
            return;
        }
        test.addTestCase(new TestCase(testCaseId.trim()));
    }

    /**
     * After adding a Test using addTest, We need to use this addTestCase method for adding information about particular
     * TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of
     * TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId, String testCaseName) {
        TestSuite testSuite = findTestSuite(testSuiteId);
        if (testSuite == null) {
            return;
        }
        Test test = findTest(testId, testSuite);
        if (test == null) {
            return;
        }
        TestCase testCase = new TestCase(testCaseId.trim(), testCaseName);
        test.addTestCase(testCase);
    }

    /**
     * After adding a Test using addTest, We need to use this addTestCase method for adding information about particular
     * TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of
     * TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId, String testCaseName,
        String testCaseDescription) {
        TestSuite testSuite = findTestSuite(testSuiteId);
        if (testSuite == null) {
            return;
        }
        Test test = findTest(testId, testSuite);
        if (test == null) {
            return;
        }
        TestCase testCase = new TestCase(testCaseId.trim(), testCaseName, testCaseDescription);
        test.addTestCase(testCase);
    }

    /**
     * After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method for setting TestCase
     * status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId
     * along with status information. only TestCases will have status
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status) {
        TestSuite suite = findTestSuite(testSuiteId);
        if (suite == null) {
            return;
        }
        Test test = findTest(testId, suite);
        if (test == null) {
            return;
        }
        TestCase testCase = findTestCase(testCaseId, test, suite);
        if (testCase == null) {
            return;
        }
        testCase.setStatus(status);

    }

    /**
     * After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method for setting TestCase
     * status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId
     * along with status information. Each TestCase will have status
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status,
        String statusDescription) {
        TestSuite testSuite = findTestSuite(testSuiteId);
        if (testSuite == null) {
            return;
        }
        Test test = findTest(testId, testSuite);
        if (test == null) {
            return;
        }
        TestCase testCase = findTestCase(testCaseId, test, testSuite);
        if (testCase == null) {
            return;
        }
        testCase.setStatus(status);
        testCase.setStatusDescription(statusDescription);

    }

    /**
     * After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method for setting TestCase
     * status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId
     * along with status information. We pass expected and actual information along with pass/fail here
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status, String expected,
        String actual) {
        TestSuite testSuite = findTestSuite(testSuiteId);
        if (testSuite == null) {
            return;
        }
        Test test = findTest(testId, testSuite);
        if (test == null) {
            return;
        }
        TestCase testCase = findTestCase(testCaseId, test, testSuite);
        if (testCase == null) {
            return;
        }
        testCase.setStatus(status);
        testCase.setExpected(expected);
        testCase.setActual(actual);

    }

    private TestSuite findTestSuite(final String testSuiteId) {
        for (TestSuite suite : suites) {
            if (testSuiteId.trim().equals(suite.getId())) {
                return suite;
            }
        }
        System.err.println("ERROR: setTestStatus might have called without setTestSuite.");
        System.err.println("Given suite ID: " + testSuiteId);
        System.err.println("suites: " + suites);
        return null;
    }

    private Test findTest(final String testId, final TestSuite suite) {
//        Test test = suite.getTests().get(testId);
        for (Test test : suite.getTests().values()) {
            if (testId.trim().equals(test.getId())) {
                return test;
            }
        }
        System.err.println("ERROR: setTestStatus might have called without addTest.");
        System.err.println(String.format("Given %s::%s", suite.getId(), testId));
        System.err.println("tests: " + suite.getTests());
        return null;
    }

    private TestCase findTestCase(final String testCaseId, final Test test, final TestSuite suite) {
//        TestCase testCase = test.getTestCases().get(testCaseId);
        for (TestCase testCase : test.getTestCases().values()) {
            if (testCase.getId().equals(testCaseId.trim())) {
                return testCase;
            }
        }
//        if (testCase == null) {
        System.err.println(String.format("ERROR: setTestCaseStatus might have called without addTestCase."
            + "  Given %s::%s::%s\ntest case: %s", suite.getId(), test.getId(), testCaseId, test.getTestCases()));
        Thread.dumpStack();
//        }
        return null;
    }

    /**
     * Change done on 7/10/02 by Deepa Singh Now Reporter will by default create results file in $EJTE_HOME if no
     * results file is specified So no need to pass the environment variable. Reporter.getInstance should create
     * test_results.xml at j2ee-test/
     */
    public static Reporter getInstance() {
        if (reporterInstance == null) {
            //reporterInstance = new Reporter( );
            String rootpath = new File(".").getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(ws_home));
            //ejte_home contains OS dependent path separator character without j2ee-test
            String outputDir = ejte_home + ws_home;
            reporterInstance = Reporter.getInstance(outputDir + File.separatorChar + "test_results.xml");
        }
        return reporterInstance;

    }

    public static Reporter getInstance(String wshome) {
        if (reporterInstance == null) {
            String rootpath = new File(".").getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(wshome));
            String outputDir = ejte_home + wshome;
            reporterInstance = new Reporter(outputDir + File.separatorChar + "test_results.xml");
        }
        return reporterInstance;
    }

    private Reporter(String resultFilePath) {
        try {
            resultFile = resultFilePath;
            Runtime.getRuntime().addShutdownHook(this);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void flushAll() {
        try {
            if ("default.xml".equals(resultFile)) {
                InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream();
                byte[] bytes = new byte[200];
                in.read(bytes);
                String file = "result_";
                String machineName = new String(bytes).trim();
                file += machineName;
                Calendar cal = Calendar.getInstance();
                String month = Integer.toString(cal.get(Calendar.MONTH));
                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                String year = Integer.toString(cal.get(Calendar.YEAR));
                file += "_" + month + day + year + ".xml";
                resultFile = file;
            }
            FileOutputStream foutput = new FileOutputStream(resultFile, true);
            Iterator<TestSuite> it = suites.iterator();
            while (it.hasNext()) {
                if (flush(it.next(), foutput)) {
                    it.remove();
                }
            }
            foutput.close();
            suites.clear();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
        }
    }

    /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite.
     *
     * @param testSuiteName the test suite's name.
     *
     * @return returns true if the file is succesfully created
     */
    public boolean flush(TestSuite testSuite) {
        boolean returnVal = false;
        try {
            if ("default.xml".equals(resultFile)) {
                InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream();
                byte[] bytes = new byte[200];
                in.read(bytes);
                String file = "result_";
                String machineName = new String(bytes).trim();
                file += machineName;
                Calendar cal = Calendar.getInstance();
                String month = Integer.toString(cal.get(Calendar.MONTH));
                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                String year = Integer.toString(cal.get(Calendar.YEAR));
                file += "_" + month + day + year + ".xml";
                resultFile = file;

            }
            FileOutputStream foutput = new FileOutputStream(resultFile, true);
            returnVal = flush(testSuite, foutput);
            foutput.close();
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
        }
        return returnVal;
    }

    /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite.
     *
     * @param testSuiteName the test suite's name.
     * @param foutput the FileOutputStream in which we need to write.
     *
     * @return returns true if the file is succesfully created
     */
    public boolean flush(TestSuite suite, FileOutputStream foutput) {
        try {
            if (suite != null && !suite.getWritten()) {
                suite.setWritten(writeXMLFile(suite.toXml(), foutput));
                return suite.getWritten();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean writeXMLFile(String xmlStringBuffer, FileOutputStream fout) {
        try {
            fout.write(xmlStringBuffer.getBytes());
            fout.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void run() {
        flushAll();
    }
}