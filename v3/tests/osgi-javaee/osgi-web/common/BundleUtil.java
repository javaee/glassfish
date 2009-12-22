
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author mohit
 */
public class BundleUtil {

//    String installerUrl = "http://localhost:8080/testOSGI/BundleInstaller";
//    String uninstallerUrl = "http://localhost:8080/testOSGI/BundleUninstaller";

    String installerUrl = "http://localhost:8080/testREST/web/bundleinstaller";
    String uninstallerUrl = "http://localhost:8080/testREST/web/bundleuninstaller";

    private static Reporter reporter;
    private static String testName;
    int failCode = 2;//code for deployment failure [see Reporter.java]

    // args[0] contains testname
    // args[1] contains testResultFile
    // args[2] if there, will contain installUrl.
    public static void main(String args[]) {
        BundleUtil bundleUtil = new BundleUtil();
        if(args.length == 2) {
	    testName = args[0];
            reporter = new Reporter(args[1]);	    
            //uninstall the previously installed bundle
            bundleUtil.uninstall();
        } else if(args.length == 3) {
            testName = args[0];
            reporter = new Reporter(args[1]);
            //install the bundle using given arguments.
            bundleUtil.install(args[2]);
        } else {
            System.out.println("Invalid Number of Arguments.");
        }
    }

    public void install(String installUrl) {
        //installerUrl = installerUrl + "?installType=" + installType;
        //installerUrl = installerUrl + "&bundlePath=" + bundlePath;
        String [] parameters = {"installUrl", installUrl};
        invokeURL(installerUrl, parameters);
    }

    public void uninstall() {
        invokeURL(uninstallerUrl, new String [] {});
    }

    public void invokeURL(String url, String[] parameters) {
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream out = connection.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            for (int i = 0; i < parameters.length; i++) {
                writer.write(parameters[i++]);//first wtire param name, then value
                writer.write("=");
                writer.write(URLEncoder.encode(parameters[i], "UTF-8"));
                writer.write("&");
            }
            writer.close();
            out.close();

            int code = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                log(line);
		if(line.contains("FAIL")){
 		    fail();
  		}
            }

            if (code != 200) {
                log("Error invoking " + url);
                fail();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void log(String message) {
        System.out.println("[BundleUtil]:: " + message);
    }

    private void fail() {
        System.out.println("[BundleUtil]:: TestFailed");
        reporter.printStatus(testName, failCode);
    }
}

