/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package admin;

import admin.AdminBaseDevTest;

/**
 * This class is like the Ring of Power from LOTR.  It calls the other Rings (classes).
 * Created on February 8, 2011
 * @author Byron Nevins
 */
public class MonitoringTests extends AdminBaseDevTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new MonitoringTests().runTests();
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

    public void runTests(){
        startDomain();
        stopDomain();
        stat.printSummary();
    }
}
