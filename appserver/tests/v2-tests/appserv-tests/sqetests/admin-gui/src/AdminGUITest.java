import com.meterware.httpunit.*;
import util.Util;
import util.JSSE;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class AdminGUITest {
    
    private final String host = Util.getAdminHost();
    private final String port = Util.getAdminPort();
    private final String user = Util.getAdminUser();
    private final String passwd = Util.getAdminPassword();
    private final String installType = Util.getInstallType();
    private String URL = null;
    private WebConversation sr = null;
    private SimpleReporterAdapter status = null;
    private int pass = 0;
    private int fail = 0;
    private int total = 4;
    private final int DEBUG = 0;
    private String[][] summary = {{"Login Page Test        : ", ""},
    {"Home Page Test         : ", ""},
    {"PropertySheet Page Test: ", ""},
    {"TablePage Test         : ", ""}};
    
    public static void main( String args[] ) {
        try {
            AdminGUITest test = new AdminGUITest();
            test.runTest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public AdminGUITest() throws Exception {
        if(host == null || port == null) {
            URL = "http://localhost:4848";
        } else {
            if(installType.equals("pe")){
                System.out.println("GOING TO EXECUTE PE TEST CASE");
                URL = "http://"+host+":"+port;
            } else {
                System.out.println("GOING TO EXECUTE EE TEST CASE");
                JSSE jse = new JSSE(new URL("https", host, Integer.parseInt(port), "/index.jsf"));
                jse.trustAnyServerCertificate();
                URL = "https://"+host+":"+port;
            }
        }
        System.out.println("INSTALL_TYPE ="+installType);
        sr = new WebConversation( );
        status = new SimpleReporterAdapter("appserv-tests");
        status.addDescription("Testing adminGUI..");
        
    }
    
    private void runTest() {
        HttpUnitOptions.setScriptingEnabled(false);
        testLoginPage();
        authorize();
        testHomePage();
        testPropertySheetPage();
        testTablePage();
        printSummary();
        
    }
    
    private void testLoginPage()  {
        String loginPage = "/index.jsf";
        
        try {
            WebResponse client = sr.getResponse(URL+loginPage);
            client = sr.getResponse(URL+loginPage);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("Copyright") >= 0) && (s.indexOf("j_username") >= 0)) {
                    status.addStatus("admin-gui loginPage:", status.PASS);
                    pass++;
                    summary[0][1] = "Passed";
                } else {
                    status.addStatus("admin-gui loginPage:", status.FAIL);
                    summary[0][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus("admin-gui loginPage:", status.FAIL);
                summary[0][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus("admin-gui loginPage:", status.FAIL);
            summary[0][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
        
    }
    
    private void testHomePage() {
        String homePage = "/commonTask.jsf";
        try {
            WebResponse client = sr.getResponse(URL+homePage);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("Deployment") >= 0) && (s.indexOf("Monitoring") >= 0)) {
                    status.addStatus("admin-gui homePage:", status.PASS);
                    summary[1][1] = "Passed";
                    pass++;
                } else {
                    status.addStatus("admin-gui homePage:", status.FAIL);
                    summary[1][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus("admin-gui homePage:", status.FAIL);
                summary[1][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus("admin-gui homePage:", status.FAIL);
            summary[1][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
    }
    
    private void authorize()  {
        String loginPage = "/index.jsf";
        try {
            WebResponse client = sr.getResponse(URL+loginPage);
            WebForm form = client.getFormWithName("loginform");
            form.setParameter("j_username", new String[]{user});
            form.setParameter("j_password", new String[]{passwd});
            SubmitButton sb = form.getSubmitButton("loginButton");
            form.submit(sb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void testPropertySheetPage()  {
        String propertyPage = "/configuration/transactionService.jsf?configName=server-config";
        
        try {
            WebResponse client = sr.getResponse(URL+propertyPage);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("transaction service settings") >= 0) && (s.indexOf("Heuristic") >= 0)) {
                    status.addStatus("admin-gui propertysheetPage:", status.PASS);
                    summary[2][1] = "Passed";
                    pass++;
                } else {
                    status.addStatus("admin-gui propertysheetPage:", status.FAIL);
                    summary[2][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus("admin-gui propertysheetPage:", status.FAIL);
                summary[2][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus("admin-gui propertysheetPage:", status.FAIL);
            summary[2][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
    }
    
    private void testTablePage() {
        String tablePage = "/applications/webApplications.jsf";
        
        try {
            WebResponse client = sr.getResponse(URL+tablePage);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("deployButton") >= 0) && (s.indexOf("Deployed") >= 0)) {
                    status.addStatus("admin-gui tablePage:", status.PASS);
                    summary[3][1] = "Passed";
                    pass++;
                } else {
                    status.addStatus("admin-gui tablePage:", status.FAIL);
                    summary[3][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus("admin-gui tablePage:", status.FAIL);
                summary[3][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus("admin-gui tablePage:", status.FAIL);
            fail++;
            summary[3][1] = "Failed";
            ex.printStackTrace();
        }
    }
    private void printSummary() {
        System.out.println("**********************************************");
        System.out.println("Total Tests :"+total);
        System.out.println("Passed      :"+pass);
        System.out.println("Failed      :"+fail);
        System.out.println("**********************************************");
        System.out.println("Details of Test Run:\n");
        for(int i=0; i<summary.length; i++) {
            System.out.print(summary[i][0]);
            System.out.println(summary[i][1]);
        }
        status.printSummary("admin-guiID");
    }
    
}
