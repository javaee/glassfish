import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that <parameter-encoding> subelement of <sun-web-app>
 * takes precedence over <parameter-encoding> subelement of
 * <locale-charset-info>, which has been deprecated.
 *
 * In its sun-web.xml, this web module specifies two parameter-encoding
 * elements, each with a form-hint-field attribute: the value of the 
 * form-hint-field attribute of the <parameter-encoding> subelement of 
 * <sun-web-app> is 'sunWebAppFromHintField', whereas the value of the
 * form-hint-field attribute of the <parameter-encoding> subelement of
 * <locale-charset-info> is 'localeCharsetInfoFormHintField'.
 *
 * Client appends to the request URI two query parameters named after the
 * form-hint-field attributes. The two query parameters, which would normally
 * represent hidden form fields, specify different request charsets as their
 * values.
 *
 * Container is supposed to set the request encoding to the value of the query
 * parameter named 'sunWebAppFromHintField', which is supposed to take
 * precedence over the query parameter named 'localeCharsetInfoFormHintField'.
 *
 * JSP that is the target of the request retrieves the request encoding and
 * assigns it as the response encoding, which is checked by this client by
 * parsing the Content-Type response header.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "form-hint-field-precedence";

    private static final String LOCALE_CHARSET_INFO_FORM_HINT_FIELD
        = "localeCharsetInfoFormHintField";
    private static final String SUN_WEB_APP_FORM_HINT_FIELD
        = "sunWebAppFormHintField";

    private static final String LOCALE_CHARSET_INFO_FORM_HINT_FIELD_CHARSET
        = "GB18030";
    private static final String SUN_WEB_APP_FORM_HINT_FIELD_CHARSET
        = "Shift_JIS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/jsp/formHintField.jsp?"
                          + LOCALE_CHARSET_INFO_FORM_HINT_FIELD
                          + "="
                          + LOCALE_CHARSET_INFO_FORM_HINT_FIELD_CHARSET
                          + "&"
                          + SUN_WEB_APP_FORM_HINT_FIELD
                          + "="
                          + SUN_WEB_APP_FORM_HINT_FIELD_CHARSET);
        System.out.println("Invoking URL: " + url.toString());

        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        System.out.println("Response Content-Type: " + contentType);

        if (contentType != null) {
            System.out.println(contentType);
            int index = contentType.indexOf(
                        "charset=" + SUN_WEB_APP_FORM_HINT_FIELD_CHARSET);
            if (index != -1) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME,
                               stat.FAIL);
            }
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
