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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.synchronization.central.MainSyncServer;
import org.glassfish.synchronization.filemanagement.FileServiceManager;
import org.glassfish.synchronization.manifest.ManifestManager;
import org.glassfish.synchronization.message.Fin;
import org.glassfish.synchronization.util.CookieManager;
import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.HttpHandler;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.glassfish.synchronization.util.ZipUtility;

import testing.Verify;

/**
 * Object in charge of a single instances synchronization. This will handle the
 * entire synchronization process.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncClient implements Runnable {
	private long startTime;
	SyncContext c;
	private StaticSyncInfo syncInfo;
	private JoinAgent joiner;
	private ClientSyncServer grizzly = null;
	
	public SyncClient(ClientConfig config) throws IOException {
		c = new SyncContext();
		syncInfo = new StaticSyncInfo(config, c);
		initContextObjects();
	}
//	public SyncClient(int id, String dasAddr, File config) {
//		c = new SyncContext();
//		String fs = File.separator;
//		String baseDir = "Instances" + fs + "I-" + id + fs;
//		try {
//			syncInfo = new StaticSyncInfo(baseDir, dasAddr, config, c);
//		} catch (Exception e) {
//			syncInfo = null;
//			e.printStackTrace();
//			System.exit(-1);
//		}
//		initContextObjects();
//		// setupLog(id);
//	}
	

//	public SyncClient(int id, File config) {
//		c = new SyncContext();
//		String fs = File.separator;
//		String baseDir = "Instances" + fs + "I-" + id + fs;
//		try {
//			syncInfo = new StaticSyncInfo(baseDir, config, c);
//		} catch (Exception e) {
//			syncInfo = null;
//			e.printStackTrace();
//			System.exit(-1);
//		}
//		initContextObjects();
//		// setupLog(id);
//	}

	public void run() {
		startTime = System.currentTimeMillis();
		Stack<ServerInfo> servers = null;
		try {
			servers = joiner.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		startGrizzlyServices();
		if (!servers.isEmpty()) {
			SyncAgentHandler syncDriverHandler = new SyncAgentHandler(c, this);
			syncDriverHandler.runSyncDrivers(servers);
		} else { // sync is complete
			syncCompleted();
		}
		// testingStopEndGrizzly();
	}

	private void startGrizzlyServices() {
		grizzly = new ClientSyncServer(c);
		grizzly.start();
	}

	/**
	 * Something went wrong reset all context objects and start sync again
	 */
	public void reset() {
		initContextObjects();
		grizzly.stopGrizzly();
		grizzly.start();
		run();
	}

	/**
	 * Do a full reset delete manifest and cookies
	 */
	public void fullReset() {
		c.getCookieManager().deleteCookie();
		c.getManifestManager().deleteManifest();
		grizzly.stopGrizzly();
		grizzly.start();
		reset();
	}

	private void initContextObjects() {
		System.setProperty("http.maxConnections", "20");
		new ManifestManager(c);
		new CookieManager(c);
		joiner = new JoinAgent(c);
		new FileUtils(c);
		new ZipUtility(c);
		new FileServiceManager(c);
	}

	public void syncCompleted() {
		HttpHandler.sendUpdate(new Fin(), c);
		if (grizzly == null) {
			c.getFileUtils().cleanUpTempFiles();
		}
		if (c.getLogger().isLoggable(Level.FINE)) {
			String log = "Time to Complete Total Synce was: "
					+ (System.currentTimeMillis() - startTime)
					+ " milliseconds";
			if (c.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			c.getLogger().fine(log);
		}
		System.out.println("Time to Complete Total Synce was: "
				+ (System.currentTimeMillis() - startTime) + " milliseconds");
	}

	private void testingStopEndGrizzly() {
		try { // clean up later just for testing purposes
			Thread.sleep(10);
			if (grizzly != null)
				grizzly.stopGrizzly();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void syncFailed() {
		// TODO: SOMETHING verification of sync failed
		System.out.println("sync failed do something");
	}
}
