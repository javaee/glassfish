import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.IOException;
import java.net.URL;

public class WebTest {
	private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

	public static void main(String args[]) {
		stat.addDescription("HTTP thread timeout");

		String host = args[0];
		String portS = args[1];
		String contextRoot = args[2];
		boolean noTimeout = args.length == 4;

		int port = new Integer(portS);

		goGet(host, port, contextRoot + "/ServletTest", noTimeout );

		stat.printSummary("web-thread-timeout");
	}

	private static void goGet(String host, int port, String contextPath, boolean noTimeout) {
		try {
			URL url = new URL("http://" + host + ":" + port + contextPath);
			url.getContent();
			stat.addStatus("web-thread-timeout", noTimeout ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
		} catch( IOException ex){
			stat.addStatus("web-thread-timeout", noTimeout ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS);
		} catch( Exception ex){
			stat.addStatus("web-thread-timeout", SimpleReporterAdapter.FAIL);
			ex.printStackTrace();
		}
	}
}
