package javax.security.jacc;

import javax.security.jacc.URLPattern;
import javax.security.jacc.URLPatternSpec;
import java.util.StringTokenizer;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestURLPatternSpec {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testURLPatternSpec ";

    private static boolean test_constructor(
            String p, boolean expectedToSucceed) {
        boolean result = expectedToSucceed;
       
        try {
            URLPatternSpec u = new URLPatternSpec(p);

            if (expectedToSucceed) {
                System.out.println("constructor( ): " + expectedToSucceed + 
                    " succeded " + u.toString());
            } else {
                System.out.println("constructor( ): " + expectedToSucceed + 
                    " failed   " + u.toString());
                result = false;
            }
        } catch (Throwable t) {
            if (!expectedToSucceed) { 
                System.out.println("constructor(e): " + expectedToSucceed + 
                     " succeded " + p);
            } else {
                result = false;
                System.out.println("constructor(e): " + expectedToSucceed + 
                     " failed   " + p);
                t.printStackTrace();
            }
        }
        return result;
    }

    private static void test_getURLPattern(String s) {
        URLPatternSpec u = new URLPatternSpec(s);

        String result = u.getURLPattern();

        String expected = s;
        int colon = s.indexOf(":");
        if (colon > 0) {
            expected = s.substring(0,colon);
        }

        if (result.equals(expected)) {
            System.out.println("getURLPattern: succeded " + expected + " " +
                    result + " " + s);
        } else {
            System.out.println("getURLPattern: failed  " + expected + " " + 
                    result + " " + s);
        }
    }

    private static void test_equals(
            String p1, String p2, boolean expected) {

        String description = "testEquals:" + p1 + "-" + p2 + "-" + expected;
        URLPatternSpec u1 = new URLPatternSpec(p1);
        URLPatternSpec u2 = new URLPatternSpec(p2);

        boolean result = u1.equals(u2);
        boolean inverse = u2.equals(u1);

        if (result == inverse) {
            if (result == true && (!u1.implies(u2) || !u2.implies(u1))) {
                System.out.println("equals(<->): " + expected + " " + 
                        result + " failed    " + 
                        "\t" + u1.implies(u2) + u2.implies(u1)+" "+
                        "\t" + u2 + "\t" + u1);
                stat.addStatus(description, stat.FAIL);
            } else if (result == expected) {
                System.out.println("equals(-->): " + expected + " " +
                        result + " succeded " + u1 + "\t" + u2);
                stat.addStatus(description, stat.PASS);
            } else { 
                System.out.println("equals(-->): " + expected + " " + 
                        result + " failed    " + u1 + "\t" + u2);
                stat.addStatus(description, stat.FAIL);
            }
        } else {
            System.out.println("equals(<--): " + result + " " + 
                    inverse + " failed    " + u2 + "\t" + u1);
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void test_hashCode(String s1) {
        String description = "testHashCode:" + s1;
        URLPatternSpec u1 = new URLPatternSpec(s1);

        int result = u1.hashCode();
        int again  = u1.hashCode();

        StringBuffer s2;

        int colon = s1.indexOf(":");

        if (colon > 0) {
            s2 = new StringBuffer(s1.substring(0,colon+1));

            String list = s1.substring(colon);

            StringTokenizer tokenizer = new StringTokenizer(list,":");

            int count = tokenizer.countTokens();
            String s2Array[] = new String[count];
            for (int i=count; i>0; i--) {
                s2Array[i-1] = tokenizer.nextToken();
            }

            for (int i=0; i<count; i++) {
                if (i!=0) {
                    s2.append(":");
                }
                s2.append(s2Array[i]);
            }
            //include empty string if it is there
            if (s1.startsWith(":") || s1.endsWith(":")) {
                s2.append(":");
            }
        } else {
            s2 = new StringBuffer(s1);
        }

        URLPatternSpec u2 = new URLPatternSpec(s2.toString());
        int other = u2.hashCode();
        if (result == again && result == other) { 
            System.out.println("hashCode: " + result + " " +
                    again + " " + other + " succeded " + s1 +
                    " " + u2.toString());
            stat.addStatus(description, stat.PASS);
        } else {
            System.out.println("hashCode: " + result + " " +
                again + " " + other + " failed " + s1 +
                " " + u2.toString());
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void test_implies(String p1, String p2,
            boolean expected, boolean expectedInverse) {
        String description = "testImplies:" +
            p1 + "-" + p2 + "-" + expected + "-" + expectedInverse;
        URLPatternSpec u1 = new URLPatternSpec(p1);
        URLPatternSpec u2 = new URLPatternSpec(p2);

        boolean result = u1.implies(u2);
        boolean inverse = u2.implies(u1);

        if (result != expected) {
     	    System.out.println("implies(->): " + expected + " " + result + 
                    " succeded " + u1 + "\t" + u2);
            stat.addStatus(description, stat.FAIL);
        } else if (inverse != expectedInverse) {
            System.out.println("implies(<-): " + expectedInverse + " " + 
                    inverse + " failed    " + u2 + "\t" + u1);
            stat.addStatus(description, stat.FAIL);
        } else {
            System.out.println("implies(->): " + expected + " " + result + 
                    " failed    " + u1 + "\t" + u2);
            stat.addStatus(description, stat.PASS);
        }
    }

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);

        String pArray[] = {
            "/a/c",
            "/a/b/c.jsp",
            "/a/c/*",
            "/a/*", 
            "/*",
            /* COMMENTED OUT! "//*",*/
            "*.jsp",
            "*.asp",
            "/",
            /* COMMENTED OUT! ,"//" */
            ""
        };

        int pTypeArray[] = {
            URLPattern.PT_EXACT,
            URLPattern.PT_EXACT,
            URLPattern.PT_PREFIX,
            URLPattern.PT_PREFIX,
            URLPattern.PT_PREFIX,
            /* COMMENTED OUT! URLPattern.PT_PREFIX, */
            URLPattern.PT_EXTENSION,
            URLPattern.PT_EXTENSION,
            URLPattern.PT_DEFAULT,
            /* COMMENTED OUT! ,URLPattern.PT_DEFAULT */
            URLPattern.PT_EXACT
        };

        for (int i=0; i<pArray.length; i++) {

            if (!test_constructor(pArray[i],true)) {
                break;
            }
            StringBuffer s = new StringBuffer(pArray[i]);

            test_getURLPattern(s.toString());
            test_hashCode(s.toString());

            URLPattern ui = new URLPattern(pArray[i]); 
 
            for (int j=0; j<pArray.length; j++) {

                boolean result = false;

                URLPattern uj = new URLPattern(pArray[j]);

                if (j==i || uj.implies(ui)) { 
                    result = test_constructor(s.toString() + ":" + 
                                     pArray[j],false);
                } else {
                    switch(pTypeArray[i]) {
                    case URLPattern.PT_EXACT:
                        result = test_constructor(s.toString() + ":" + 
                                pArray[j],false);
                        break;
                    case URLPattern.PT_DEFAULT:
                        result = test_constructor(s.toString() + ":" +
                                pArray[j],true);
                        break;
                    case URLPattern.PT_EXTENSION:
                        if (pTypeArray[j] == URLPattern.PT_PREFIX) {
                            result = test_constructor(s.toString() + ":" + 
                                    pArray[j],true);
                        } else if (pTypeArray[j] == URLPattern.PT_EXACT) {
                            if (pArray[j].endsWith(pArray[i].substring(1))) { 
                                result = test_constructor(s.toString() + ":" + 
                                        pArray[j],true);
                            } else {
                                result = test_constructor(s.toString() + ":" + 
                                        pArray[j],false);
                            }
                        } else { 
                            result = test_constructor(s.toString() + ":" + 
                                    pArray[j],false);
                        }
                        break;
                    case URLPattern.PT_PREFIX:
                        if (pTypeArray[j] == URLPattern.PT_EXACT ||
                                pTypeArray[j] == URLPattern.PT_PREFIX) {

                            if (ui.implies(uj)) {
                                result = test_constructor(s.toString() + ":" + 
                                        pArray[j],true); 
                            } else {
                                result = test_constructor(s.toString() + ":" + 
                                        pArray[j],false);
                            }
                        } else {
                            result = test_constructor(s.toString() + ":" + 
                                    pArray[j],false);
                        }
                        break;
                   }
               }

               if (result) {

                   String old = s.toString();
                   s.append(":" + pArray[j]);
                   String New = s.toString();

                   test_getURLPattern(New);

                   test_hashCode(New);

                   test_equals(old,old,true);
                   test_equals(New,New,true);

                   URLPatternSpec os = new URLPatternSpec(old);
                   URLPatternSpec ns = new URLPatternSpec(New);

                   if (os.toString().equals(ns.toString())) {
                       test_equals(New,old,true);
                       test_implies(New,old,true,true);
                   }
                   else {
                       test_equals(New,old,false);
                       test_implies(New,old,false,true);
                   }
                }
            }
        }
        stat.printSummary(testSuite);
    }
}










