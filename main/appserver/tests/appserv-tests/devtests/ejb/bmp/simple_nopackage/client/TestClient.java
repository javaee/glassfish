import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestClient {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) throws Exception {
        try {
            stat.addDescription("Testing bmp simple_nopackage app.");
            InitialContext ic = new InitialContext();
            TestHome home = (TestHome) javax.rmi.PortableRemoteObject.narrow(ic.lookup("ejb/Test"), TestHome.class);
            System.out.println("Starting test");
            Test test = home.create(1);
            test.foo();
            System.out.println("Done");
            stat.addStatus("bmp simple_nopackage", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("bmp simple_nopackage", stat.FAIL);
        }
        stat.printSummary("simple_nopackage");
    }
}
