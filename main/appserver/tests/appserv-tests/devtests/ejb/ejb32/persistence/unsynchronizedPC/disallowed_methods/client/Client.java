package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.client;

import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "SlessBean")
    private static Tester slessBean;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-disallowed_methods");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-disallowed_methods");
    }

    public void doTest() {
        System.out.println("I am in client");
        
        try {
            Map<String, Boolean> resultMap = slessBean.doTest();
            for (Entry<String, Boolean> entry : resultMap.entrySet()) {
                stat.addStatus(entry.getKey(), entry.getValue() ? stat.PASS : stat.FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
