/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package org.glassfish.synchronization.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.glassfish.synchronization.message.FileVersionResponse;
import org.glassfish.synchronization.message.VersionRequest;
import org.glassfish.synchronization.util.HttpHandler;


/**
 * This class is used to mimic V2 behavior with a few fixes.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncClientV2 extends SyncClient {
	
	public SyncClientV2(ClientConfig config) throws IOException {
		super(config);
	}
//	public SyncClientV2(int id, File config) {
//		super(id, config);
//	}
//
//	public SyncClientV2(int id, String dasAddr, File config) {
//		super(id, dasAddr, config);
//	}

	public void run() {
		long t1 = System.currentTimeMillis();
		try {
			URL url = new URL(c.getStaticSyncInfo().getDasUrl());
			HttpURLConnection syncConn = (HttpURLConnection) url
					.openConnection();
			syncConn.setRequestMethod("POST");
			syncConn.setDoOutput(true);
			syncConn.setDoInput(true);
			syncConn.connect();
			HttpHandler.send(new VersionRequest(c.getCookieManager()
					.getCookie()), syncConn);
			handleReply(HttpHandler.receive(syncConn));
		} catch (MalformedURLException e) {
			// TODO DAS ack failed but don't care this shouldn't happen though
			e.printStackTrace(); // remove after testing
		} catch (IOException e) {
			// TODO reply to das failed to I care?
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Sync time took " + (t2 - t1));
	}

	private void createCookie(long timestamp) throws IOException {
		File cook = new File(c.getStaticSyncInfo().getCookieFilePath());
		if (!cook.exists()) {
			cook.createNewFile();
		}
		cook.delete();
		BufferedWriter out = new BufferedWriter(new FileWriter(c
				.getStaticSyncInfo().getCookieFilePath()));
		out.write("" + timestamp);
		out.close();
	}

	private void handleReply(Object o) throws FileNotFoundException,
			IOException {
		FileVersionResponse reply = (FileVersionResponse) o;
		if (reply.getFile() != null) {
			c.getZipUtility().unzipit(reply.getFile(),
					c.getStaticSyncInfo().getBasePath());
		}
		createCookie(reply.version);
	}
}
