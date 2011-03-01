/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import admin.AdminBaseDevTest;
import java.io.*;

/**
 * Created on February 8, 2011
 * @author Byron Nevins
 */
public class MonitoringTests extends AdminBaseDevTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // top level try here!!
        MonitoringTests mt = new MonitoringTests();

        try {
            mt.runTests();
        }
        catch (Exception e) {
            mt.report("GotException-" + e.getClass().getName(), false);
        }
        mt.stat.printSummary();
    }

    public MonitoringTests() {
        earFile = new File("apps/webapp2.ear");
    }

    @Override
    protected String getTestDescription() {
        return "DevTests for Monitoring - Brought to you by\n"
                + "Jennifer Chou and Byron Nevins";
    }

    @Override
    public String getTestName() {
        return "Monitoring DevTests";
    }

    public void runTests() {
        String configName = "server";
        startDomainDebug();
        createClusterAndInstances();

        verifyMainFlags(configName);
        megaTestMainFlags(configName);
        // enable all the levels and verify on DAS
        enableMonitoringUsingSet(configName, true);
        test15397();
        enableMonitoringUsingSet(configName, false);
        // enable all the levels and verify on the cluster
        verifyMainFlags(CLUSTER_NAME);
        megaTestMainFlags(CLUSTER_NAME);
        enableMonitoringUsingSet(CLUSTER_NAME, true);
        enableMonitoringUsingSet(CLUSTER_NAME, false);
        // enable all the levels and verify on the stand-alone instance
        verifyMainFlags(STAND_ALONE_INSTANCE_NAME);
        megaTestMainFlags(STAND_ALONE_INSTANCE_NAME);
        enableMonitoringUsingSet(STAND_ALONE_INSTANCE_NAME, true);
        enableMonitoringUsingSet(STAND_ALONE_INSTANCE_NAME, false);

        enableMonitoringTest(configName);
        enableMonitoringTest(CLUSTER_NAME);
        enableMonitoringTest(STAND_ALONE_INSTANCE_NAME);
        report("leave-monitoring-enabled-for-other-devtests", asadmin("enable-monitoring"));
        deleteClusterAndInstances();
        stopDomain();
    }

    /**
     *
     * @param on if true it means turn on all levels to HIGH
     * if false it means set all to LOW
     * Also verify that they are currently set to the opposite
     */
    private void enableMonitoringUsingSet(String configName, boolean high) {
        final String metName = "enableMonitoringWithSet";
        String desiredValue = "=";
        desiredValue += high ? HIGH : OFF;
        String metFullName = metName + "-set-" + desiredValue + "-";

        for (String monItem : MON_CATEGORIES) {
            String fullitemname = createFullLevelName(configName, monItem);
            String reportname = metFullName + monItem;
            report(reportname, asadmin("set", (fullitemname + desiredValue)));
        }
        // make sure they were all set properly
        verifyMonitoringAllCategories(metName, high, configName);
    }

    /**
     * Verify that ALL levels are OFF or HIGH depending on the param
     * @param b
     */
    private void verifyMonitoringAllCategories(String parentMethod, boolean high, String configName) {
        String desiredValue = high ? HIGH : OFF;
        String metName = parentMethod + "-verify-" + desiredValue + "-";

        for (String monItem : MON_CATEGORIES) {
            String reportName = metName + monItem + "-";
            verifyMonitoringOneCategory(reportName, configName, monItem, desiredValue);
        }
    }

    private void verifyMonitoringOneCategory(String reportName, String configName, String category, String expect) {
        String fullitemname = createFullLevelName(configName, category);
        report(reportName, doesGetMatch(fullitemname, expect));
    }

    private void createClusterAndInstances() {
        verifyClusterAndInstances(false);
        report("verify-no-clusters", verifyNoClusters());
        report("verify-no-instances", verifyNoInstances());
        report("create-cluster", asadmin("create-cluster", CLUSTER_NAME));
        report("create-clustered-instance-1",
                asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                CLUSTERED_INSTANCE_NAME1));
        report("create-clustered-instance-2",
                asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                CLUSTERED_INSTANCE_NAME2));
        report("create-standalone-instance",
                asadmin("create-local-instance", STAND_ALONE_INSTANCE_NAME));

        verifyClusterAndInstances(true);
    }

    private void deleteClusterAndInstances() {
        // yes it is indeed overkill on the checking -- but these are devtests!
        verifyClusterAndInstances(true);

        report("delete-clustered-instance-1", asadmin("delete-local-instance", CLUSTERED_INSTANCE_NAME1));
        report("delete-clustered-instance-2", asadmin("delete-local-instance", CLUSTERED_INSTANCE_NAME2));
        report("delete-standalone-instance", asadmin("delete-local-instance", STAND_ALONE_INSTANCE_NAME));
        report("delete-cluster", asadmin("delete-cluster", CLUSTER_NAME));
        verifyClusterAndInstances(false);
        report("verify-no-clusters", verifyNoClusters());
        report("verify-no-instances", verifyNoInstances());
    }

    private void verifyClusterAndInstances(boolean exist) {
        String title = "verify-" + (exist ? "exists-" : "not-exists-");
        AsadminReturn clu = asadminWithOutput("list-clusters");
        AsadminReturn ins = asadminWithOutput("list-instances");

        if (exist) {
            report(title + CLUSTER_NAME, matchString(CLUSTER_NAME, clu.out));
            report(title + CLUSTERED_INSTANCE_NAME1, matchString(CLUSTERED_INSTANCE_NAME1, ins.out));
            report(title + CLUSTERED_INSTANCE_NAME2, matchString(CLUSTERED_INSTANCE_NAME2, ins.out));
            report(title + STAND_ALONE_INSTANCE_NAME, matchString(STAND_ALONE_INSTANCE_NAME, ins.out));
        }
        else {
            report(title + CLUSTER_NAME, !matchString(CLUSTER_NAME, clu.out));
            report(title + CLUSTERED_INSTANCE_NAME1, !matchString(CLUSTERED_INSTANCE_NAME1, ins.out));
            report(title + CLUSTERED_INSTANCE_NAME2, !matchString(CLUSTERED_INSTANCE_NAME2, ins.out));
            report(title + STAND_ALONE_INSTANCE_NAME, !matchString(STAND_ALONE_INSTANCE_NAME, ins.out));
        }
    }

    private void enableMonitoringTest(String configName) {
        // there are thousands (millions?) of permutations.  I selected a few to
        // exercise enable-monitoring
        // format --  category[=OFF|LOW|HIGH]:*
        // if there is no = then it will be set to high
        // only call this at the very end -- it will turn everything off

        final String em = "enable-monitoring";
        String s1 = MON_CATEGORIES[0] + ":" + MON_CATEGORIES[1] + "=LOW:" + MON_CATEGORIES[2] + "=OFF";
        String s2 = MON_CATEGORIES[3] + ":" + MON_CATEGORIES[4] + "=LOW:" + MON_CATEGORIES[5] + "=LOW";
        String s3 = MON_CATEGORIES[6] + "=HIGH:" + MON_CATEGORIES[7] + "=OFF:" + MON_CATEGORIES[8] + "=OFF";
        String s4 = MON_CATEGORIES[9] + "=LOW";
        String s5 = MON_CATEGORIES[10];

        String r1 = "enable-monitoring-sub-1-";
        String r2 = "enable-monitoring-sub-2-";
        String r3 = "enable-monitoring-sub-3-";
        String r4 = "enable-monitoring-sub-4-";
        String r5 = "enable-monitoring-sub-5-";

        report(r1, asadmin(em, "--target", configName, "--modules", s1));
        report(r2, asadmin(em, "--target", configName, "--modules", s2));
        report(r3, asadmin(em, "--target", configName, "--modules", s3));
        report(r4, asadmin(em, "--target", configName, "--modules", s4));
        report(r5, asadmin(em, "--target", configName, "--modules", s5));

        verifyMonitoringOneCategory(r1 + "-1-", configName, MON_CATEGORIES[0], "HIGH");
        verifyMonitoringOneCategory(r1 + "-2-", configName, MON_CATEGORIES[1], "LOW");
        verifyMonitoringOneCategory(r1 + "-3-", configName, MON_CATEGORIES[2], "OFF");

        verifyMonitoringOneCategory(r2 + "-1-", configName, MON_CATEGORIES[3], "HIGH");
        verifyMonitoringOneCategory(r2 + "-2-", configName, MON_CATEGORIES[4], "LOW");
        verifyMonitoringOneCategory(r2 + "-3-", configName, MON_CATEGORIES[5], "LOW");

        verifyMonitoringOneCategory(r3 + "-1-", configName, MON_CATEGORIES[6], "HIGH");
        verifyMonitoringOneCategory(r3 + "-2-", configName, MON_CATEGORIES[7], "OFF");
        verifyMonitoringOneCategory(r3 + "-3-", configName, MON_CATEGORIES[8], "OFF");

        verifyMonitoringOneCategory(r4, configName, MON_CATEGORIES[9], "LOW");
        verifyMonitoringOneCategory(r5, configName, MON_CATEGORIES[10], "HIGH");

        StringBuilder sb = new StringBuilder();
        boolean firstCat = true;

        for (String cat : MON_CATEGORIES) {
            if (firstCat)
                firstCat = false;
            else
                sb.append(':');

            sb.append(cat).append("=OFF");
        }
        // ALL categories off
        report("enable-monitoring-turn-off-all-", asadmin(em, "--target", configName, "--modules", sb.toString()));
        verifyMonitoringAllCategories("enable-monitoring-turn-off-all-verify-", false, configName);
    }

    private String createFullLevelName(String server, String what) {
        return MON_LEVEL_PREPEND + server + "-config" + MON_LEVEL_APPEND + what;
    }

    private String getDtraceEnabledName(String server) {
        return MON_LEVEL_PREPEND + server + "-config" + DTRACE_APPEND;
    }

    private String getMbeanEnabledName(String server) {
        return MON_LEVEL_PREPEND + server + "-config" + MBEAN_APPEND;
    }

    private String getMonEnabledName(String server) {
        return MON_LEVEL_PREPEND + server + "-config" + MON_APPEND;
    }

    private void verifyMainFlags(String configName) {
        // verify the defaults
        String reportName = "verify-main-flags-";

        report(reportName + "-dtrace-", doesGetMatch(getDtraceEnabledName(configName), "false"));
        report(reportName + "-mbean-", doesGetMatch(getMbeanEnabledName(configName), "true"));
        report(reportName + "-monitoring-", doesGetMatch(getMonEnabledName(configName), "true"));
    }

    private void megaTestMainFlags(String configName) {
        megaTestDtraceFlag(configName);
        megaTestMbeanFlag(configName);
        megaTestMonFlag(configName);
    }

    private void megaTestDtraceFlag(String configName) {
        // verify off, enable it, verify, disable, verify
        String reportName = "dtrace-enable-test-";
        report(reportName + "-verify-disabled-", doesGetMatch(getDtraceEnabledName(configName), "false"));
        report(reportName + "enable-", asadmin("enable-monitoring", "--target", configName, "--dtrace=true"));
        report(reportName + "-verify-enabled-", doesGetMatch(getDtraceEnabledName(configName), "true"));
        report(reportName + "disable-", asadmin("enable-monitoring", "--target", configName, "--dtrace=false"));
        report(reportName + "-verify-disabled-", doesGetMatch(getDtraceEnabledName(configName), "false"));
    }

    private void megaTestMbeanFlag(String configName) {
        // verify on, disable it, verify, enable, verify
        String reportName = "mbean-enable-test-";
        report(reportName + "-verify-enabled-", doesGetMatch(getMbeanEnabledName(configName), "true"));
        report(reportName + "disable-", asadmin("enable-monitoring", "--target", configName, "--mbean=false"));
        report(reportName + "-verify-disabled-", doesGetMatch(getMbeanEnabledName(configName), "false"));
        report(reportName + "enable-", asadmin("enable-monitoring", "--target", configName, "--mbean=true"));
        report(reportName + "-verify-enabled-", doesGetMatch(getMbeanEnabledName(configName), "true"));
    }

    private void megaTestMonFlag(String configName) {
        // verify on, disable it, verify, enable, verify
        String reportName = "mon-enabled-test-";
        report(reportName + "-verify-enabled-", doesGetMatch(getMonEnabledName(configName), "true"));
        report(reportName + "disable-", asadmin("disable-monitoring", "--target", configName));
        report(reportName + "-verify-disabled-", doesGetMatch(getMonEnabledName(configName), "false"));
        report(reportName + "enable-", asadmin("enable-monitoring", "--target", configName));
        report(reportName + "-verify-enabled-", doesGetMatch(getMonEnabledName(configName), "true"));
    }

    private void test15397() {
        String prepend = "15397-";
        report(prepend + "Ear File exists", earFile.isFile() && earFile.canRead());
        report(prepend + "deploy-earfile-with-dot", asadmin("deploy", earFile.getAbsolutePath()));
        report(prepend + "verify-deploy", asadminWithOutput("list-components").outAndErr.indexOf("webapp2") >= 0);
        report(prepend + "check-getm-1",
                checkForString(asadminWithOutput("get", "-m", "server.applications.webapp2.*"),
                MAGIC_NAME_IN_APP));
        report(prepend + "check-getm-2",
                checkForString(asadminWithOutput("get", "-m", "server.applications.webapp2.webapp2webmod1\\.war*"),
                MAGIC_NAME_IN_APP));
        report(prepend + "check-getm-3",
                checkForString(asadminWithOutput("get", "-m", "server.applications.webapp2.webapp2webmod1.war*"),
                MAGIC_NAME_IN_APP));
    }

    private boolean checkForString(AsadminReturn r, String s) {
        if(r.outAndErr == null)
            return false;

        System.out.println("QQQQQQ    " + r.outAndErr);
        return r.outAndErr.indexOf(s) >= 0;
    }

    private static final String CLUSTER_NAME = "moncluster";
    private static final String CLUSTERED_INSTANCE_NAME1 = "moninstance1";
    private static final String CLUSTERED_INSTANCE_NAME2 = "moninstance2";
    private static final String STAND_ALONE_INSTANCE_NAME = "moninstance3";
    private static final String MON_LEVEL_PREPEND = "configs.config.";
    private static final String MON_LEVEL_APPEND = ".monitoring-service.module-monitoring-levels.";
    private static final String DTRACE_APPEND = ".monitoring-service.dtrace-enabled";
    private static final String MBEAN_APPEND = ".monitoring-service.mbean-enabled";
    private static final String MON_APPEND = ".monitoring-service.monitoring-enabled";
    private static final String MAGIC_NAME_IN_APP = "webapp2webmod1_Servlet2";
    private static final String MON_CATEGORIES[] = new String[]{
        "http-service",
        "connector-connection-pool",
        "connector-service",
        "deployment",
        "ejb-container",
        "jdbc-connection-pool",
        "jersey",
        "jms-service",
        "jpa",
        "jvm",
        "orb",
        "security",
        "thread-pool",
        "transaction-service",
        "web-container",
        "web-services-container"
    };
    private final static String HIGH = "HIGH";
    private final static String OFF = "OFF";
    private final File earFile;
}
