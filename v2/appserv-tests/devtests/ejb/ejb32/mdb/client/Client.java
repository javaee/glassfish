package com.sun.s1asdev.ejb.ejb32.mdb.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.mdb.ejb.ResultsRemote;

import javax.naming.InitialContext;
import java.util.List;

/**
 * Modern MDB test
 *
 * Verifies that the resource adapter:
 *  - has access to the beanClass via the activation spec
 *  - can obtain a LocalBean-like view
 *
 * @author David Blevins
 */
public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
        stat.addDescription("ejb32-mdb");

        try {
            ResultsRemote resultsRemote = (ResultsRemote) new InitialContext().lookup("java:global/ejb32-mdb/ejb32-mdb-ejb/ResultsBean!com.sun.s1asdev.ejb.ejb32.mdb.ejb.ResultsRemote");

//            System.out.println("awaitInvocations - start");
            assertStatus("ejb32-mdb: awaitInvocations", resultsRemote.awaitInvocations());

            final List<String> invoked = resultsRemote.getInvoked();
            assertStatus("ejb32-mdb: method one", invoked.contains("one - intercepted"));
            assertStatus("ejb32-mdb: method two", invoked.contains("two - intercepted"));
            assertStatus("ejb32-mdb: method three", invoked.contains("three - intercepted"));
            assertStatus("ejb32-mdb: total invocations", invoked.size() == 3);

        } catch (Exception e) {
            stat.addStatus("ejb32-mdb: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-mdb");
    }

    private static void assertStatus(final String message, final boolean condition) {
        System.out.println(message + " : " + condition);
        stat.addStatus(message, condition ? stat.PASS : stat.FAIL);
    }
}
