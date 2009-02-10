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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.message.FileRequest;

/**
 * This class manages the SyncAgents that are directly connected to this Server.
 * It also maintains a list of possible redirection servers that can be used.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncAgentManager {
	private static final long serialVersionUID = 1L;
	protected Map<String, String> syncAgents = new HashMap<String, String>();
	protected Map<String, BitAndAddr> grizzlyServers = new HashMap<String, BitAndAddr>();
	protected Map<String, BitAndAddr> servers = new HashMap<String, BitAndAddr>();

	/**
	 * Adds a syncAgent to the manager
	 * 
	 * @param agentID
	 *            the unique id that identifies this agent to this server
	 * @param serverAddress
	 *            the address on which this agent runs its sync server
	 * @param cInfo
	 *            the clients header information
	 * @param req
	 *            the request from this SyncAgent
	 */
	public void putSyncAgent(ClientHeaderInfo cInfo, FileRequest req) {
		String agentID = cInfo.idKey;
		BitAndAddr bitaddr;
		synchronized (syncAgents) {
			syncAgents.put(agentID, cInfo.serverAddr);
		}
		synchronized (grizzlyServers) {
			if (grizzlyServers.containsKey(cInfo.serverAddr)) {
				grizzlyServers.get(cInfo.serverAddr).incrementAgentCount(
						agentID);
			} else {
				bitaddr = new BitAndAddr(req.getHasBits(), cInfo.serverAddr,
						cInfo.serviceLoad, agentID);
				grizzlyServers.put(cInfo.serverAddr, bitaddr);
				// System.out.println("put " + cInfo.serverAddr);
			}
		}
		updateServerInfo(cInfo, req);
	}

	/**
	 * Removes the agent with this particular agentID it will also reduce the
	 * agent count on the global counter
	 * 
	 * @param agentID
	 */
	public void removeSyncAgent(String agentID) {
		String address;
		synchronized (syncAgents) {
			address = syncAgents.remove(agentID);
		}
		BitAndAddr bitaddr;
		synchronized (grizzlyServers) {
			bitaddr = grizzlyServers.get(address);
			// if(bitaddr == null)
			// System.out.println("adgent ID is " + agentID);
			bitaddr.decrementAgentCount(agentID);
			if (bitaddr.getClientCount() < 1) {
				grizzlyServers.remove(address);
				// System.out.println("removed " +address);
			}
		}
	}

	/**
	 * Return whether this agent is currently being servered by this server
	 * 
	 * @param agentID
	 * @return
	 */
	public synchronized boolean containsAgent(String agentID) {
		return syncAgents.containsKey(agentID);
	}

	/**
	 * Removes all agents from the agent list and from the server list that are
	 * associated with the server address.
	 * 
	 * @param serverAddress
	 */
	public void removeAllAgents(String serverAddress) {
		synchronized (syncAgents) {
			Iterator<String> values = syncAgents.values().iterator();
			String tempaddress;
			while (values.hasNext()) {
				tempaddress = values.next();
				if (tempaddress.equals(serverAddress)) {
					values.remove();
				}
			}
		}
		synchronized (grizzlyServers) {
			grizzlyServers.remove(serverAddress);
			// System.out.println("removed " +serverAddress);
		}
	}

	/**
	 * Updates the agent server information
	 * 
	 * @param clientInfo
	 * @param req
	 */
	public void updateServerInfo(ClientHeaderInfo clientInfo, FileRequest req) {
		BitAndAddr baddr;
		synchronized (grizzlyServers) {
			baddr = grizzlyServers.get(clientInfo.serverAddr);
		}
		if (baddr != null) {
			baddr.updateLoad(clientInfo.serviceLoad);
			baddr.updatebitSet(req.getHasBits());
		}
	}

	/**
	 * finds the server with the lowest load
	 * 
	 * @param clientInfo
	 * @return
	 */
	public BitAndAddr findLowestLoadServer(ClientHeaderInfo clientInfo) {
		Iterator<BitAndAddr> values = grizzlyServers.values().iterator();
		BitAndAddr baddr;
		int minLoad = Integer.MAX_VALUE, tempLoad;
		BitAndAddr leastBusy = null;
		while (values.hasNext()) {
			baddr = values.next();
			if (baddr.address.equals(clientInfo.serverAddr))
				continue; // don't want to redirect to self
			tempLoad = baddr.getServiceLoad();
			if (tempLoad == 0) {
				return baddr;
			} else if (tempLoad < minLoad) {
				leastBusy = baddr;
				minLoad = tempLoad;
			}
		}
		return leastBusy;
	}

	/**
	 * finds and returns the lowest ranked SyncAgent in the group
	 * 
	 * @param rank
	 *            the rank of this requesting SyncAgent
	 * @param clientInfo
	 * @return
	 */
	public String getLowestRankChild(int rank, ClientHeaderInfo clientInfo) {
		BitAndAddr replace = null;
		synchronized (syncAgents) {
			Iterator<BitAndAddr> values = grizzlyServers.values().iterator();
			BitAndAddr bAddr;
			int minRank = rank;
			while (values.hasNext()) {
				bAddr = values.next();
				int tempRank = bAddr.bits.cardinality();
				if (tempRank < minRank) {
					minRank = tempRank;
					replace = bAddr;
				}
			}
		}
		if (replace == null)
			return null;
		return replace.getFirstAgent();
	}

	/**
	 * This check is to avoid the corner case that a node is redirected to
	 * itself
	 * 
	 * @param clientInfo
	 * @return
	 */
	public boolean redirectIsPossible(ClientHeaderInfo cInfo) {
		synchronized (grizzlyServers) {
			boolean b1 = grizzlyServers.size() == 0;
			boolean b2 = grizzlyServers.size() == 1
					&& grizzlyServers.containsKey(cInfo.serverAddr);
			// System.out.println("for " + cInfo.serverAddr + " b1: " + b1 + "
			// b2: " +b2);
			return !(b1 || b2);
		}
	}

	/**
	 * Returns the number of agents currently connected to this server
	 * 
	 * @return
	 */
	public int clientCount() {
		return syncAgents.size();
	}
}
