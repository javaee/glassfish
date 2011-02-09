/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import admin.AdminBaseDevTest;

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
        String configName = "server-config";
        startDomain();

        // if we started it -- make very sure that we at least attempt 
        // to stop it in all cases!
        // Also let's not forget disabling monitoring

        try {
            enableMonitoring(configName, true);
            runTestsThatNeedARunningDomain();
        }
        finally {
            try {
                enableMonitoring(configName, false);
                stopDomain();
            }
            catch (Exception e) {
                report("BigProblemInMonitoringTests", false);
                System.out.println("" + e);
            }
        }
    }

    private void runTestsThatNeedARunningDomain() {
        runGetTests();
    }

    private void runGetTests() {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param on if true it means turn on all levels to HIGH
     * if false it means set all to LOW
     * Also verify that they are currently set to the opposite
     */
    private void enableMonitoring(String configName, boolean high) {
        final String metName = "enableMonitoring";

        String prepend = MON_LEVEL_PREPEND + configName + MON_LEVEL_APPEND;
        String desiredValue = "=";
        desiredValue += high ? HIGH : OFF;
        String metFullName = metName + "-set-" + desiredValue + "-";

        for (String monItem : MON_LEVEL_ITEMS) {
            String fullitemname = prepend + monItem;
            String reportname = metFullName + monItem;
            report(reportname, asadmin("set", (fullitemname + desiredValue)));
        }
        // make sure they were all set properly
        verifyMonitoring(metName, high, configName);
    }

    /**
     * Verify that ALL levels are OFF or HIGH depending on the param
     * @param b
     */
    private void verifyMonitoring(String parentMethod, boolean high, String configName) {
        String prepend = MON_LEVEL_PREPEND + configName + MON_LEVEL_APPEND;
        String desiredValue = high ? HIGH : OFF;
        String metName = parentMethod + "-verify-" + desiredValue + "-";

        for (String monItem : MON_LEVEL_ITEMS) {
            String fullitemname = prepend + monItem;
            String reportname = metName + monItem;
            report(reportname, doesGetMatch(fullitemname, desiredValue));
        }
    }
    private static final String MON_LEVEL_PREPEND = "configs.config.";
    private static final String MON_LEVEL_APPEND = ".monitoring-service.module-monitoring-levels.";
    private static final String MON_LEVEL_ITEMS[] = new String[]{
        "http-service",
        "connector-connection-pool",
        "connector-service",
        "deployment",
        "ejb-container",
        "http-service",
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
}
