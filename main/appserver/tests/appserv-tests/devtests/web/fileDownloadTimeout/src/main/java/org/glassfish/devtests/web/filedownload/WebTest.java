/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
