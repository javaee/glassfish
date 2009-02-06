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

package org.glassfish.synchronization.central;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.glassfish.api.Startup;
import org.glassfish.synchronization.client.SyncContext;
import org.glassfish.synchronization.filemanagement.FileServiceManager;
import org.glassfish.synchronization.manifest.ManifestManager;
import org.glassfish.synchronization.util.CookieManager;
import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.ManifestCreator;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.glassfish.synchronization.util.ZipUtility;
//import org.jvnet.hk2.annotations.Service;
//import org.jvnet.hk2.component.PostConstruct;


import com.sun.grizzly.http.embed.GrizzlyWebServer;

/**
 * This is the class that initializes the Grizzly server and Grizzly adapter
 * which runs the source synchronization logic.
 * 
 * @author Behrooz Khorashadi
 * 
 */
//@Service
public class MainSyncServer {//implements Startup, PostConstruct {
//public class MainSyncServer {
	private final MainServerConfig server_configs;
	public MainSyncServer(MainServerConfig config) {
		server_configs = config;
	}

	/**
	 * Starts grizzly server.
	 * 
	 * @param ws
	 * @throws IOException
	 */
	private void startGrizzly(GrizzlyWebServer ws) throws IOException {
		ws.start();
	}

//	public Lifecycle getLifecycle() {
//		return Startup.Lifecycle.SERVER;
//	}

	/**
	 * This function is called by the hk2 framework automatically at startup of
	 * the glassfish server.
	 */
	public void postConstruct() {
		SyncContext c = new SyncContext();
		// Create Grizzly webserver NOTE this should be removed later
		GrizzlyWebServer ws = new GrizzlyWebServer(DAS_GRIZZLY_PORT, "/var/www");
		ws.addGrizzlyAdapter(new CentralGrizzlyAdapter(c), contextRoot);
//		File config = new File(server_configs.base_dir + 
//			StaticSyncInfo.SYNC_FOLDER  + StaticSyncInfo.CONFIG_FILE_NAME);
		// Create objects needed for monitoring and managing sync process
		try {
			StaticSyncInfo syncInfo = new StaticSyncInfo(server_configs, c);
			initContextObjects(c);
			createManifest(c);
			new ManifestManager(syncInfo.getManifestFilePath(), c);
			startGrizzly(ws);
			InetAddress addr = null;
			addr = InetAddress.getLocalHost();
			if (addr != null) {
				c.getStaticSyncInfo().setServerIP(addr.getHostAddress());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			ws.stop();
			System.exit(-1);
		}
		Logger logger = c.getLogger();
		if (logger.isLoggable(Level.FINE)) {
			String log = "Grizzly has Started on port " + DAS_GRIZZLY_PORT;
			c.getLogger().fine(log);
		}
		c.getStaticSyncInfo().setPort(DAS_GRIZZLY_PORT);
		System.out.println("Grizzly has Started on port "
				+ c.getStaticSyncInfo().getServerAddress());
	}

	public void createManifest(SyncContext c) throws IOException {
		long t1=System.currentTimeMillis(), t2;
		ManifestCreator manC = new ManifestCreator(c);
		manC.start();
		t2 = System.currentTimeMillis();
		Logger logger = c.getLogger();
		if (logger.isLoggable(Level.FINE)) {
			c.getLogger().fine("Manifest created in " + (t2 - t1) + "msec");
		}
		
	}
	/**
	 * Initialize the basic context objects needed for running the 
	 * synchronization process
	 * @param c object that holds all the contexts
	 */
	private void initContextObjects(SyncContext c) {
		new CookieManager(c);
		new FileUtils(c);
		new ZipUtility(c);
		new FileServiceManager(c);
	}

	public static final int DAS_GRIZZLY_PORT = 4848;
	public static final String[] contextRoot = {"/synchronization"};
}
