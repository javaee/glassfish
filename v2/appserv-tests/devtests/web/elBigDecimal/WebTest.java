import javax.el.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for coverting long to BigDecimal in EL
 * issue 7479
 */

public class WebTest {

    private static final String testName = "EL-long-to-BigDecimal";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        stat.addDescription("Unit tests for logn to BigDecimal coersion");

        ExpressionFactory ef = ExpressionFactory.newInstance();
        Object o = ef.coerceToType(new Long("1234567890123456789"),
                                   java.math.BigDecimal.class);
        System.out.println("Result: " + o);
        if ("1234567890123456789".equals(o.toString()))
            stat.addStatus(testName, stat.PASS);                
        else
            stat.addStatus(testName, stat.FAIL);                

        stat.printSummary();
    }
}

