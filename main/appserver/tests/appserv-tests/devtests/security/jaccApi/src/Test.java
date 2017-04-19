package javax.security.jacc;

import javax.security.jacc.URLPattern;
import javax.security.jacc.URLPatternSpec;
import java.util.StringTokenizer;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Test {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API test ";

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);
        String description = null;

        description = testSuite + "test1";
        String s = new String("/a/*:/a/b/joe.jsp:/a/b/c:/a/b/*");
        URLPatternSpec ups = new URLPatternSpec(s);
        System.out.println("s:   " + s);
        System.out.println("ups: " + ups);
        if ("/a/*:/a/b/*".equals(ups.toString())) {
            stat.addStatus(description, stat.PASS);
        } else {
            stat.addStatus(description, stat.FAIL);
        }

        description =  testSuite + "test2";
        s = new String("/:/a/b/joe.jsp:/a/b/c:/a/b/*:*.jsp:/a/*");
        ups = new URLPatternSpec(s);
        System.out.println("s:   " + s);
        System.out.println("ups: " + ups);
        if ("/:*.jsp:/a/*".equals(ups.toString())) {
            stat.addStatus(description, stat.PASS);
        } else {
            stat.addStatus(description, stat.FAIL);
        }

        stat.printSummary(testSuite);
    }
}
