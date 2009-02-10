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

package org.glassfish.synchronization.loadbalancer;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.message.FileRequest;

/**
 * This class is responsible for balancing the incoming requests. The balancer
 * will attempt to redirect if requesting clients exceed the maximum limit.
 * NOTE: a redirect will not always be possible in which case the balancer will
 * service the request.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class LimitBalancer implements LoadBalancerInterface {
	Random r = new Random(2);
	protected SyncAgentManager sam = new SyncAgentManager();
	// protected HashMap<String, BitAndAddr> syncAgents = new HashMap<String,
	// BitAndAddr>();
	// protected HashMap<String, BitAndAddr> grizzlyServers = new
	// HashMap<String, BitAndAddr>();
	protected HashMap<String, BitAndAddr> redirectList = new HashMap<String, BitAndAddr>();
	protected HashMap<String, BitAndAddr> manifestServiceList = new HashMap<String, BitAndAddr>();

	public void fileReq(MessageContext msgContext) throws IOException {
		boolean test;
		synchronized (manifestServiceList) {
			manifestServiceList.remove(msgContext.cInfo.idKey);
		}
		if (sam.containsAgent(msgContext.cInfo.idKey)) {
			// System.out.println("Serving current child "
			// +msgContext.cInfo.toString());
			handleCurrentChild(msgContext);
			return;
		}
		synchronized (redirectList) {
			test = redirectList.containsKey(msgContext.cInfo.idKey);
		}
		if (test) {
			// System.out.println("Redirecting child " +
			// msgContext.cInfo.toString());
			handleScheduledRedirect(msgContext);
			return;
		} else {
			// System.out.println("Handeling new session " +
			// msgContext.cInfo.toString());
			handleNewSession(msgContext);
		}
	}

	public void joinReq(MessageContext msgContext) throws IOException {
		String syncAgentID;
		// System.out.println("new join request " +
		// msgContext.cInfo.toString());
		synchronized (manifestServiceList) {
			if (manifestServiceList.size() + sam.clientCount() >= MAX_SERVICE_NUM * 3
					&& redirectIsPossible(msgContext.cInfo)) {
				// System.out.println("redirecting join ");
				msgContext.syncCallBack.redirect(msgContext,
						getRedirectServer(msgContext.cInfo));// redirect to a
																// server
			} else {
				// System.out.println("serving join");
				syncAgentID = generateRandomSessionKey();
				msgContext.cInfo.idKey = syncAgentID;
				manifestServiceList.put(syncAgentID, null);
				msgContext.syncCallBack.sendManifest(msgContext);
			}
		}
	}

	/**
	 * gets a redirect server to redirect the request to. Client info has been
	 * passed in to avoid redirecting a request to the same machine. However
	 * this function will fail if there are no servers to redirect to and will
	 * throw an exception. When calling this function users must check to make
	 * sure other servers are available.
	 * 
	 * @param clientInfo
	 * @return redirect address
	 */
	protected String getRedirectServer(ClientHeaderInfo clientInfo) {
		BitAndAddr leastBusy = findLowestLoadServer(clientInfo);
		leastBusy.incrementLoad();
		// if(clientInfo.serverAddr.equals(leastBusy.address)){
		// System.err.println("crap ");
		// }
		return leastBusy.address;
	}

	protected BitAndAddr findLowestLoadServer(ClientHeaderInfo clientInfo) {
		return sam.findLowestLoadServer(clientInfo);
	}

	private void addClient(String newKey, ClientHeaderInfo cInfo,
			FileRequest req) {
		cInfo.idKey = newKey;
		sam.putSyncAgent(cInfo, req);
	}

	/**
	 * Handles the current children
	 * 
	 * @param clientInfo
	 * @param req
	 * @return
	 * @throws IOException
	 */
	private void handleCurrentChild(MessageContext msgContext)
			throws IOException {
		sam.updateServerInfo(msgContext.cInfo, msgContext.reqMsg);
		msgContext.syncCallBack.serviceRequest(msgContext);
	}

	/**
	 * Handles a new session. This method is called if the corresponding client
	 * id is neither in the redirect list or contained by SyncAgentManager
	 * 
	 * @param msgContext
	 * @throws IOException
	 */
	private void handleNewSession(MessageContext msgContext) throws IOException {
		String newKey = generateRandomSessionKey();
		if (sam.clientCount() >= MAX_SERVICE_NUM
				&& redirectIsPossible(msgContext.cInfo)) {
			// System.out.println("sending to overload"
			// +msgContext.cInfo.serverAddr);
			overLoaded(msgContext);
		} else {
			// System.out.println("serving " +msgContext.cInfo.serverAddr);
			addClient(newKey, msgContext.cInfo, msgContext.reqMsg);
			msgContext.syncCallBack.serviceRequest(msgContext);
		}
		// if(sam.clientCount() < MAX_SERVICE_NUM ||
		// !redirectIsPossible(msgContext.cInfo)){
		// addClient(newKey, msgContext.cInfo, msgContext.reqMsg);
		// msgContext.syncCallBack.serviceRequest(msgContext);
		// }
		// else {
		// overLoaded(msgContext);
		// }
	}

	/**
	 * Overloaded either redirects a child syncAgent and services this syncAgent
	 * or redirects this syncAgent
	 * 
	 * @param clientInfo
	 * @param req
	 * @return
	 * @throws IOException
	 */
	private void overLoaded(MessageContext msgContext) throws IOException {
		String newKey;
		int rank = msgContext.reqMsg.getHasBits().cardinality();
		String replace = getLowestRankChild(rank, msgContext.cInfo);
		if (replace == null) {// redirect this guy
			// System.out.println("redirecting same request " +
			// msgContext.cInfo.serverAddr);
			msgContext.syncCallBack.redirect(msgContext,
					getRedirectServer(msgContext.cInfo));
			return;
		} else {
			// System.out.println("swapping child " + replace);
			sam.removeSyncAgent(replace);
			synchronized (redirectList) {
				redirectList.put(replace, null);
			}
			newKey = generateRandomSessionKey();
			addClient(newKey, msgContext.cInfo, msgContext.reqMsg);
		}
		msgContext.syncCallBack.serviceRequest(msgContext);
	}

	/**
	 * Redirect those child nodes were scheduled to be redirected. If redirect
	 * is now not possible start serving this child again.
	 * 
	 * @param clientInfo
	 * @param req
	 * @return
	 * @throws IOException
	 */
	private void handleScheduledRedirect(MessageContext msgContext)
			throws IOException {
		synchronized (redirectList) {
			redirectList.remove(msgContext.cInfo.idKey);
		}
		if (redirectIsPossible(msgContext.cInfo)) {
			msgContext.syncCallBack.redirect(msgContext,
					getRedirectServer(msgContext.cInfo));
		} else {
			String newKey = generateRandomSessionKey();
			addClient(newKey, msgContext.cInfo, msgContext.reqMsg);
			msgContext.syncCallBack.serviceRequest(msgContext);
		}
	}

	public String[] serverList() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This check is to avoid the corner case that a node is redirected to
	 * itself
	 * 
	 * @param clientInfo
	 * @return
	 */
	protected boolean redirectIsPossible(ClientHeaderInfo cInfo) {
		return sam.redirectIsPossible(cInfo);
	}

	public int getLoad() {
		return sam.clientCount();
	}

	/**
	 * Generates a random id key using characters A-Z and 0-9 only
	 * 
	 * @return unique id identifier characters A-Z and 0-9 only
	 */
	private synchronized String generateRandomSessionKey() {
		String agentID = Long.toString(Math.abs(r.nextLong()), 36);
		while (sam.containsAgent(agentID) || redirectList.containsKey(agentID)) {
			agentID = Long.toString(Math.abs(r.nextLong()), 36);
		}
		return agentID;
	}

	/**
	 * finds and returns the lowest ranked SyncAgent in the group
	 * 
	 * @param rank
	 *            the rank of this requesting SyncAgent
	 * @param clientInfo
	 * @return
	 */
	private String getLowestRankChild(int rank, ClientHeaderInfo clientInfo) {
		return sam.getLowestRankChild(rank, clientInfo);
	}

	/**
	 * Process a sync complete message by removing all state information
	 * 
	 */
	public void syncCompleted(ClientHeaderInfo clientInfo) {
		synchronized (manifestServiceList) {
			manifestServiceList.remove(clientInfo.idKey);
		}
		// synchronized(grizzlyServers){
		// grizzlyServers.remove(clientInfo.serverAddr);
		// }
		sam.removeAllAgents(clientInfo.serverAddr);
		// synchronized(syncAgents){
		// Iterator<String> keys = syncAgents.keySet().iterator();
		// BitAndAddr removeChild;
		// String tempKey;
		// while(keys.hasNext()){
		// tempKey = keys.next();
		// removeChild = syncAgents.get(tempKey);
		// if(removeChild.address.equals(clientInfo.serverAddr)){
		// keys.remove();
		// }
		// }
		// }
	}

	/**
	 * The maximum number of SyncAgents that will be served. This is a soft
	 * number because if there is no redirect option then SyncAgent will be
	 * served
	 */
	public final int MAX_SERVICE_NUM = Integer.MAX_VALUE;

}
