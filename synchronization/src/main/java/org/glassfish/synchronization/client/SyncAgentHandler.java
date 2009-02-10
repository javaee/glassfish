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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import org.glassfish.synchronization.message.Fin;
import org.glassfish.synchronization.util.HttpHandler;

import testing.Verify;

/**
 * Initializes the SyncAgents and starts them. Handles SyncAgents and failures
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncAgentHandler {
	private SyncContext context;
	private boolean syncComplete = false;
	private long startTime;
	private final SyncClient client;
	private Map<Integer, SyncAgent> drivers = new HashMap<Integer, SyncAgent>();
	private int agent_num = 1;

	public SyncAgentHandler(SyncContext c, SyncClient clt) {
		context = c;
		client = clt;
		agent_num = c.getStaticSyncInfo().getSAT();
	}

	public void runSyncDrivers(Stack<ServerInfo> serverURLs) {
		startTime = System.currentTimeMillis();
		context.getFileUtils().cleanUpTempFiles();
		// TODO Create a thread for as many urls you want to sync with
		// but for now just redirect to the person that gave you a manifest
		ServerInfo serverInfo = serverURLs.pop();
		SyncAgent syncDriver;
		for (int i = 0; i < agent_num; i++) {
			syncDriver = new SyncAgent(i + 1, serverInfo, context, this);
			Thread a1 = new Thread(syncDriver);
			drivers.put(syncDriver.getID(), syncDriver);
			a1.start();
		}

	}

	public synchronized void ackDAS() {
		if (syncComplete)
			return;
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "File Sync Completed in  "
					+ (System.currentTimeMillis() - startTime)
					+ " milliseconds";
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().fine(log);
		}
		syncComplete = true;
		boolean hash_verify = context.getStaticSyncInfo().getHashVerify();
		if (!Verify.verify(context.getManifestManager().getManifest(), context
				.getStaticSyncInfo().getBasePath(), hash_verify, context
				.getLogger()))
			client.syncFailed();
		else {
			client.syncCompleted();
			try {
				context.getCookieManager().createCookie();
			} catch (IOException e) {
				// TODO Do nothing but send a warning
				e.printStackTrace(); // for testing only
			}
		}
	}

	public synchronized void manifestFailure(SyncAgent sd) {
		Iterator<SyncAgent> syncDrivers = drivers.values().iterator();
		SyncAgent syncDriver;
		while (syncDrivers.hasNext()) {
			syncDriver = syncDrivers.next();
			syncDriver.abortSync(null);
		}
		context.getFileUtils().cleanUpTempFiles();
		client.reset();
	}

	public synchronized void failed(SyncAgent sd) {
		drivers.remove(sd.getID());
		if (drivers.size() == 0 && !syncComplete) {
			context.getFileUtils().cleanUpTempFiles();
			client.reset();
		}
	}

}
