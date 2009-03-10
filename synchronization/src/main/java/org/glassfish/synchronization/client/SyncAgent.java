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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.BitSet;
import java.util.logging.Level;

import org.glassfish.synchronization.message.FileRequest;
import org.glassfish.synchronization.message.Fin;
import org.glassfish.synchronization.message.RedirectMessage;
import org.glassfish.synchronization.message.SyncInfoMessage;
import org.glassfish.synchronization.message.ZipMessage;
import org.glassfish.synchronization.util.HttpHandler;

/**
 * This class is in charge of syncing with a parent instances or server. Each
 * node can potentially have multiple instances of SyncAgent running in order to
 * pipeline the synchronization process. SyncAgent has no need to be thread
 * safe, because it does not need to share any state with any other objects
 * (except for the syncAbort function).
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncAgent implements Runnable {
	private final Integer agentID;
	private HttpURLConnection syncConn = null;
	private URL url;
	ServerInfo serverInfo;
	private SyncContext context;
	/** sleep reset number */
	private int serverHasNothing = 0;
	private final Long manVersion;
	private boolean abortSync = false;
	private Exception failureStatus = null;
	private final SyncAgentHandler handler;

	public SyncAgent(int id, ServerInfo init, SyncContext c, 
														SyncAgentHandler d) {
		serverInfo = init;
		context = c;
		manVersion = new Long(context.getManifestManager().getManVersion());
		handler = d;
		agentID = new Integer(id);
	}

	/**
	 * Establishes a new connection with the given url string. This is called
	 * after a redirect message
	 * 
	 * @param urlString
	 *            the url representing the address of the new connection
	 */
	private void newConnection(String urlString) {
		serverInfo.has = null;
		serverInfo.id_key = "";
		serverInfo.address = urlString;
		if (syncConn != null)
			syncConn.disconnect();
		serverHasNothing = 0;
		try {
			url = new URL(serverInfo.address);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * This is used to reset the request-response phase (takes advantage of
	 * persistent HTTP conection). Sets appropriate headers and time out.
	 * 
	 * @throws IOException
	 */
	private void connect() throws IOException {
		syncConn = (HttpURLConnection) url.openConnection();
		syncConn.setRequestProperty(ClientHeaderInfo.KEY_HEADER,
				serverInfo.id_key);
		syncConn.setRequestProperty(ClientHeaderInfo.MANIFEST_VERSION_HEADER,
				manVersion.toString());
		syncConn.setRequestProperty(ClientHeaderInfo.SERVER_ADDRESS, context
				.getStaticSyncInfo().getServerAddress());
		syncConn.setRequestProperty(ClientHeaderInfo.SERVICE_LOAD, ""
				+ context.getBalancerLoad());
		syncConn.setRequestProperty(ClientHeaderInfo.SYNC_AGENT_ID, ""
				+ agentID.toString());
		syncConn.setRequestMethod("POST");
		syncConn.setDoInput(true);
		syncConn.setDoOutput(true);
		// syncConn.setReadTimeout(READ_TIMEOUT);
		syncConn.connect();
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "Connected to " + serverInfo.address;
			System.out.println(log);
			context.getLogger().fine(log);
		}
	}

	/**
	 * Attempts to get files. This function is called in a loop until exception
	 * is thrown or synchronization is completed.
	 * 
	 * @throws IOException
	 * @throws ManifestVersionException
	 * @throws ClassNotFoundException
	 *             throw if the response from the server is an unknown object
	 *             type
	 */
	private void getFiles() throws IOException, ManifestVersionException,
			ClassNotFoundException {
		connect();
		BitSet reqBits = context.getManifestManager().getnextDownloadBits(
				serverInfo.has);
		BitSet myManifestBits = context.getManifestManager().getBitManifest();
		// if(reqBits.isEmpty() &&
		// serverInfo.address.equals(context.getStaticSyncInfo().getDasUrl())){
		// System.out.println("no bits I have " + myManifestBits.cardinality());
		// }
		FileRequest req = new FileRequest(myManifestBits, reqBits);
		if (context.getLogger().isLoggable(Level.FINE)
				|| context.getStaticSyncInfo().getSysOut()) {
			String log = "Sending Sync Request with "
					+ req.getNeedBits().toString();
//			if (context.getStaticSyncInfo().getSysOut())
//				System.out.println(log);
			context.getLogger().fine(log);
		}
		HttpHandler.send(req, syncConn);
		handleResponse(req.getNeedBits());
		if (reqBits.isEmpty()) {// there is nothing to request sleep and wait
			handleNullRequest();
		}
	}

	/**
	 * Handles the two types of responses from a file request (Redirect or a Zip
	 * file).
	 * 
	 * @param reqstBits
	 *            the bits that correspond to the initial request. This is
	 *            passed in so these bits can be released. This is done mainly
	 *            so that on an error the bits are released so for future
	 *            requests.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ManifestVersionException
	 */
	private void handleResponse(BitSet reqstBits) throws IOException,
			ClassNotFoundException, ManifestVersionException {
		Object response = null;
		try {
			response = HttpHandler.receive(syncConn);
			checkManifestVersion(syncConn
					.getHeaderField(ClientHeaderInfo.MANIFEST_VERSION_HEADER));
			if (abortSync)
				return;
			if (response instanceof RedirectMessage) {
				serverInfo.id_key = "";
				RedirectMessage redResp = (RedirectMessage) response;
				if (context.getLogger().isLoggable(Level.FINE)) {
					String log = "I've been redierected to: "
							+ redResp.getRedirectAddress();
					if (context.getStaticSyncInfo().getSysOut())
						System.out.println(log);
					context.getLogger().fine(log);
				}
				if (redResp.getRedirectAddress().equals(
						context.getStaticSyncInfo().getServerAddress()))
					System.err.println("This shouldn't happen  going to "
							+ redResp.getRedirectAddress() + " from "
							+ context.getStaticSyncInfo().getServerAddress());
				// throw new SelfRedirectException();
				newConnection(redResp.getRedirectAddress());
				HttpHandler.sendUpdate(new SyncInfoMessage(redResp
						.getRedirectAddress(), 0), context);
			} else if (response instanceof ZipMessage) {
				serverInfo.id_key = syncConn
						.getHeaderField(ClientHeaderInfo.KEY_HEADER);
				ZipMessage zipMes = (ZipMessage) response;
				if (context.getLogger().isLoggable(Level.FINE)) {
					String log;
					if (zipMes.getContent() == null)
						log = "Zip message was empty";
					else
						log = "Got zip message with "
								+ zipMes.getContent().toString();
					if (context.getStaticSyncInfo().getSysOut())
						System.out.println(log);
					context.getLogger().fine(log);
				}
				serverInfo.has = zipMes.getHas();
				context.getFileManager().handleReceivedFile(
						zipMes.getZipAndContent());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			newConnection(context.getStaticSyncInfo().getDasUrl());
		} finally {
			context.getManifestManager().resetPendingBits(reqstBits);
		}
	}

	/**
	 * Checks for manifest version equality. You should only be syncing with
	 * instances that have the same manifest version. If the version is not
	 * equal throws exception.
	 * 
	 * @param headerField
	 * @throws ManifestVersionException
	 *             an exception specifically designed for manifest
	 *             inconsistency.
	 */
	private void checkManifestVersion(String headerField)
			throws ManifestVersionException {
		Long recievedVersionID = Long.parseLong(headerField);
		if (recievedVersionID.longValue() != manVersion.longValue()) {
			ManifestVersionException mException = new ManifestVersionException(
					"Manifest Version is unsynchronized");
			abortSync(mException);
			String log = "Manifest inconsistency, Recieved manifest with version "
					+ recievedVersionID
					+ " and possess version  "
					+ manVersion.longValue();
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().severe(log);
			throw mException;
		}
	}

	/**
	 * Starts Sync Agent
	 */
	public void run() {
		try {
			url = new URL(serverInfo.address);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			abortSync(e);
			handler.failed(this);
		}
		while (!context.getManifestManager().finished() && !abortSync) {
			try {
				getFiles();
			} catch (IOException e) {
				abortSync(e);
				handler.failed(this);
				e.printStackTrace();
			} catch (ManifestVersionException e) {
				abortSync(e);
				handler.manifestFailure(this);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				abortSync(e);
				handler.failed(this);
				e.printStackTrace();
			}
		}
		leaveParent();
		if (abortSync) {
			return;
		}
		if (syncConn != null)
			syncConn.disconnect();
		handler.ackDAS();
	}

	/**
	 * Signals the parent node that this instance is no longer a child node This
	 * occurs either when sync failed or is completed. NOTE: this function
	 * throws no exception on failure this fails we leave the cleanup to the
	 * parent.
	 */
	private void leaveParent() {
		try {
			syncConn = (HttpURLConnection) url.openConnection();
			syncConn.setRequestProperty(ClientHeaderInfo.KEY_HEADER,
					serverInfo.id_key);
			syncConn.setRequestProperty(
					ClientHeaderInfo.MANIFEST_VERSION_HEADER, manVersion
							.toString());
			syncConn.setRequestProperty(ClientHeaderInfo.SERVER_ADDRESS,
					context.getStaticSyncInfo().getServerAddress());
			syncConn.setRequestProperty(ClientHeaderInfo.SERVICE_LOAD, ""
					+ context.getBalancerLoad());
			syncConn.setRequestMethod("POST");
			syncConn.setDoInput(true);
			syncConn.setDoOutput(true);
			syncConn.connect();
			if (context.getLogger().isLoggable(Level.FINE)) {
				String log = "Sending Completion message to "
						+ serverInfo.address;
				if (context.getStaticSyncInfo().getSysOut())
					System.out.println(log);
				context.getLogger().fine(log);
			}
			HttpHandler.send(new Fin(), syncConn);
			Object response = HttpHandler.receive(syncConn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) { // Sync Completion failed
			e.printStackTrace();
		}
	}

	/**
	 * Abort the synchronization process. This can be called internally or
	 * externally by the SyncAgentHandler in order to stop the sync process
	 * 
	 * @param e
	 *            The reason for the abort.
	 */
	public synchronized void abortSync(Exception e) {
		failureStatus = e;
		abortSync = true;
	}

	/**
	 * Returns the reason for the agents failure
	 * 
	 * @return
	 */
	public Exception getFailException() {
		return failureStatus;
	}

	/**
	 * This function is called either if a generated request is empty. This can
	 * happen either if the parent node does not have the anything that this
	 * node needs or if this node has nothing to request due to pending
	 * requests. This function tries to establish which of these is the case and
	 * act accordingly
	 */
	private void handleNullRequest() {
		// TODO: if this method is called multiple times consider going back to
		// das
		BitSet myBits = context.getManifestManager().getBitManifest(); // bits
																		// I
																		// have
		myBits.flip(0, context.getManifestManager().getnumFiles()); // bits I
																	// need
		if (serverInfo.has != null)
			myBits.and(serverInfo.has);

		if (myBits.isEmpty() && serverInfo.has != null) { // server has
															// nothing to give
			serverHasNothing++;
		} else { // TODO: Server has content but I can't request anything
					// possibly request bits are locked

		}
		if (serverHasNothing > NO_CONTENT_THRESHOLD) {
			leaveParent();
			newConnection(context.getStaticSyncInfo().getDasUrl());
			context.getLogger().info(
					"Reverting back to das this server has nothing for me");
			if (context.getStaticSyncInfo().getSysOut()) {
				System.out
						.println("Reverting back to das this server has nothing for me");
			}
		}
		try {
			Thread.sleep(WAIT_FOR_CONTENT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** The wait time for a response to a request */
	private static final int READ_TIMEOUT = 300000;
	/**
	 * The number of times this agent will tolerate no being able to retrieve
	 * content from the parent node
	 */
	private static final int NO_CONTENT_THRESHOLD = 4;
	/**
	 * The sleep time idicating how long this node will sleep for when parent
	 * node has no available content
	 */
	private static final long WAIT_FOR_CONTENT = 10000;

	public Integer getID() {
		return agentID;
	}
}
