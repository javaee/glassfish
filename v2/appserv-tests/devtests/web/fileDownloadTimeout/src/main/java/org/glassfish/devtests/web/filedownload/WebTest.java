package org.glassfish.devtests.web.filedownload;

import com.sun.appserv.test.BaseDevTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author emiddio
 * @author justin.d.lee@oracle.com
 */
public class WebTest extends BaseDevTest {
	private static final String TEST_NAME = "default-response-type";

	byte[] ba = new byte[1024];

	@Override
	protected String getTestDescription() {
		return "file download time out test";
	}

	@Override
	protected String getTestName() {
		return TEST_NAME;
	}


	/**
	 * @param args the command line arguments
	 */
	public void run() throws Exception {
		asadmin("set", "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.timeout-seconds=300");
		// TODO code application logic here
		URL u = new URL("http://localhost:" + antProp("http.port") + "/web-file-download-timeout/webservices-osgi.jar");
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET");
		huc.setReadTimeout(0);

		InputStream is = huc.getInputStream();

		File base = new File("src/main/webapp/webservices-osgi.jar");
		File tmp = new File("/tmp");
		if(!tmp.exists()) {
			tmp = new File(System.getProperty("java.io.tmpdir"));
		}
		File file = File.createTempFile("webservices-osgi", ".jar", tmp);
		file.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(file);

		int c;
		Long start = System.currentTimeMillis();
		try {
			while ((c = is.read(ba)) != -1) {
				fos.write(ba, 0, c);
				Thread.sleep(10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
			fos.close();
			asadmin("set", "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.timeout-seconds=");
		}
		Long end = System.currentTimeMillis();

		report(TEST_NAME, 30 <= (end - start)/1000);
		boolean same = base.length() == file.length();
		report(TEST_NAME, same);
	}

    public static void main(String[] args) throws Exception {
        new WebTest().run();
    }
}
