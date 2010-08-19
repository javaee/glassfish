
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import sun.misc.BASE64Encoder;

class TestRoleAssignment {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private boolean result = true;
    private final String url;
    private final String username;
    private final String password;
    private final String role;
    private final boolean positiveTest;

    public TestRoleAssignment(String url, String username, String password, String role, boolean positiveTest) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.role = role;
        this.positiveTest = positiveTest;
    }

    public void doTest() {
        try {
            URL u = new URL(url);
            URLConnection uconn = u.openConnection();

            String up = username + ":" + password;
            BASE64Encoder be = new BASE64Encoder();
            up = be.encode(up.getBytes());
            uconn.setRequestProperty("authorization", "Basic " + up);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    uconn.getInputStream()));
            while (reader.readLine() != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        stat.addDescription("Weblogic Role Assignment test for role: " + role);
        String testId = "Weblogic Role Assignment test for role: " + role;
        if (positiveTest) {
            if (result) {
                stat.addStatus(testId, stat.PASS);
            } else {
                stat.addStatus(testId, stat.FAIL);
            }
        } else { // negative test
            if (result) {
                stat.addStatus(testId, stat.FAIL);
            } else {
                stat.addStatus(testId, stat.PASS);
            }
        }
        stat.printSummary(testId);
    }
    public static final String URL_OPTION = "-url";
    public static final String USER_OPTION = "-user";
    public static final String PASS_OPTION = "-pass";
    public static final String ROLE_OPTION = "-role";
    public static final String NEGATIVE_TEST_OPTION = "-negative";

    public static void usage() {
        System.out.println("usage: java TestRoleAssignment -url <url> -user <user> -pass <pass> -role <role>");
    }

    public static void main(String[] args) {

        String url = null;
        String user = null;
        String pass = null;
        String role = null;
        boolean positiveTest = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].intern() == URL_OPTION.intern()) {
                url = args[++i];
            } else if (args[i].intern() == USER_OPTION.intern()) {
                user = args[++i];
            } else if (args[i].intern() == PASS_OPTION.intern()) {
                pass = args[++i];
            } else if (args[i].intern() == ROLE_OPTION.intern()) {
                role = args[++i];
            } else if (args[i].intern() == NEGATIVE_TEST_OPTION.intern()) {
                positiveTest = false;
            } else {
                usage();
                System.exit(1);
            }
        }

        if (url == null || user == null || pass == null || role == null) {
            usage();
            System.exit(1);
        }

        TestRoleAssignment test =
                new TestRoleAssignment(url, user, pass, role, positiveTest);
        test.doTest();
    }
}
