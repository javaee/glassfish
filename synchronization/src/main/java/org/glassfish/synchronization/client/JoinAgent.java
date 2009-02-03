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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.synchronization.message.AcceptJoinResponse;
import org.glassfish.synchronization.message.Fin;
import org.glassfish.synchronization.message.JoinRequest;
import org.glassfish.synchronization.message.RedirectMessage;
import org.glassfish.synchronization.message.SyncInfoMessage;
import org.glassfish.synchronization.util.HttpHandler;

/**
 * Used to retrieve the manifest from the server
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class JoinAgent {
	private HttpURLConnection syncConn = null;
	private String contactUrl;
	private String[] serverURLList;
	private ServerInfo acceptingServer = null;
	// private boolean connectedToTree = false;
	URL url;
	private SyncContext context;

	public JoinAgent(SyncContext c) {
		context = c;
	}

	/**
	 * Handles the new connection. This is called initially to establish a
	 * connection with DAS, and then again on each redirect.
	 * 
	 * @param urlString
	 *            the string representation of the contact URL
	 * @throws MalformedURLException
	 *             thrown if the string url is malformed
	 * @throws ConnectException
	 * @throws IOException
	 */
	private void newConnection(String urlString) throws MalformedURLException,
			ConnectException, IOException {
		if (syncConn != null)
			syncConn.disconnect();
		contactUrl = urlString;
		url = new URL(contactUrl);
		syncConn = (HttpURLConnection) url.openConnection();
		syncConn.setRequestMethod("POST");
		syncConn.setDoInput(true);
		syncConn.setDoOutput(true);
		syncConn.connect();
	}

	/**
	 * Reuses the persistent connection rather than re connecting
	 * 
	 * @throws ConnectException
	 * @throws IOException
	 */
	private void connect() throws ConnectException, IOException {
		syncConn = (HttpURLConnection) url.openConnection();
		syncConn.setRequestProperty(ClientHeaderInfo.KEY_HEADER,
				acceptingServer.id_key);
		syncConn.setRequestMethod("POST");
		syncConn.setDoInput(true);
		syncConn.setDoOutput(true);
		syncConn.connect();
	}

	/**
	 * Handles the two possible responses from the server (Redirect or ziped
	 * manifest).
	 * 
	 * @throws ConnectException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void handleJoinResponse() throws ConnectException, IOException,
			ClassNotFoundException {
		Object joinRes = null;

		joinRes = HttpHandler.receive(syncConn);

		if (joinRes instanceof AcceptJoinResponse) {
			AcceptJoinResponse acceptResp = (AcceptJoinResponse) joinRes;
			if (context.getLogger().isLoggable(Level.FINE)) {
				String log = "Received Response: Manifest is "
						+ acceptResp.getFile().getAbsolutePath();
				System.out.println(log);
				context.getLogger().fine(log);
			}
			String manifestPath = context.getStaticSyncInfo()
					.getManifestFilePath();
			context.getZipUtility().unZipFile(acceptResp.getFile(),
					manifestPath);
			context.getManifestManager().setManifest(manifestPath);
			acceptResp.getFile().deleteOnExit();
			serverURLList = acceptResp.getServerList();
			acceptingServer = new ServerInfo(syncConn
					.getHeaderField(ClientHeaderInfo.KEY_HEADER), contactUrl,
					acceptResp.getHasBits());
			// FileServerManager.setManifestZipFile(acceptResp.getFile());
		} else if (joinRes instanceof RedirectMessage) {
			RedirectMessage redResp = (RedirectMessage) joinRes;
			if (context.getLogger().isLoggable(Level.FINE)) {
				String log = "Redirected to " + redResp.getRedirectAddress();
				System.out.println(log);
				context.getLogger().fine(log);
			}
			newConnection(redResp.getRedirectAddress());
			sendJoinRequest();
			HttpHandler.sendUpdate(new SyncInfoMessage(redResp
					.getRedirectAddress(), 0), context);
		} else {
			throw new RuntimeException("Unkown Server Response on Join Request");
		}
	}

	/**
	 * Trys to join the group MAX_RETRYS number of times.
	 * 
	 * @throws Exception
	 *             throws the last exception that was retrieved when a join was
	 *             attempted
	 */
	private void tryConnection() throws Exception {
		Exception lastException = null;
		for (int i = 0; i < MAX_RETRYS; i++) {
			try {
				sendJoinRequest();
				return;
			} catch (ConnectException e) {
				Thread.sleep(HALF_SECOND);
				newConnection(context.getStaticSyncInfo().getDasUrl());
				lastException = e;
				if (context.getLogger().isLoggable(Level.SEVERE)) {
					String log = "Failed to Join will retry attempt #"
							+ (i + 1);
					System.out.println(log);
					context.getLogger().fine(log);
				}
			} catch (IOException ex) {
				throw ex;
			}
		}
		throw lastException;
	}

	/**
	 * The function that starts the join process
	 * 
	 * @return a stack with all the possible options for retrieving files it is
	 *         up to the handler to decide how many of those servers it will
	 *         connect to.
	 * @throws Exception
	 */
	public Stack<ServerInfo> run() throws Exception {
		newConnection(context.getStaticSyncInfo().getDasUrl());
		try {
			tryConnection();
		} catch (Exception e) {
			throw new Exception("Join failed with following message: "
					+ e.getMessage(), e);
		}
		Stack<ServerInfo> serverList = createURLStack();
		if (serverList.isEmpty()) {
			try {
				completeSync();
			} catch (Exception e) {
				// do nothing I don't care if I could ack I'm still synced
				e.printStackTrace();
			}
		}
		return serverList;
	}

	/**
	 * If the sync is complted at this point than the joinAgent must signal the
	 * contacted server that it is done. This is done because the contacted
	 * server, after serving the manifest, is expecting that it will then be
	 * queried by the syncAgents.
	 * 
	 * @throws ConnectException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void completeSync() throws ConnectException, IOException,
			ClassNotFoundException {
		connect();
		HttpHandler.send(new Fin(), syncConn);
		Object joinRes = null;
		joinRes = HttpHandler.receive(syncConn);
		syncConn.disconnect();
	}

	/**
	 * Simply handles sending the join requests
	 * 
	 * @throws ConnectException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void sendJoinRequest() throws ConnectException, IOException,
			ClassNotFoundException {
		// connect();
		Object req = (Object) new JoinRequest();
		HttpHandler.send(req, syncConn);
		context.getLogger().fine("Sending Join Request");
		handleJoinResponse();
	}

	/**
	 * resets this join agent
	 */
	public void reset() {
		// TODO handle a reset message
	}

	/**
	 * Creates the list of possible contacts for file service. This is returned
	 * and used by the SyncAgentHandler when creating SyncAgents.
	 * 
	 * @return
	 */
	private Stack<ServerInfo> createURLStack() {
		if (context.getManifestManager().finished())
			return new Stack<ServerInfo>(); // sync is already complete
		Stack<ServerInfo> servers = new Stack<ServerInfo>();
		ServerInfo serverInfo;
		if (serverURLList != null) {
			for (int i = 0; i < serverURLList.length; i++) {
				serverInfo = new ServerInfo(serverURLList[i]);
				servers.push(serverInfo);
			}
		}
		servers.push(acceptingServer);
		return servers;
	}

	private final long HALF_SECOND = 3000;
	private final int MAX_RETRYS = 5;
}
