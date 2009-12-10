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

import java.util.BitSet;
import java.util.LinkedList;

/**
 * A class that holds state information about each GF instance this is used to
 * maintain how much load each child node has and how many SyncAgents this
 * server is serving.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class BitAndAddr {
	/** A list of SyncAgents */
	LinkedList<String> agentList = new LinkedList<String>();
	/**
	 * the bits this server has (used when looking for rank and redirect
	 * candidates)
	 */
	public BitSet bits;
	/** the number of SyncAgents this child has connected to this server */
	private int syncAgentCount = 1;
	/** The number of SyncAgents served by this child node */
	private int serviceLoad = 0;
	/** the address of grizzly adapter (includes port) */
	public final String address;

	public BitAndAddr(BitSet b, String addr, int load, String agentID) {
		bits = b;
		address = addr;
		serviceLoad = load;
		agentList.add(agentID);
	}

	public BitAndAddr(BitSet b, String addr, int load) {
		this(b, addr, load, "notNeeded");
	}

	public synchronized void updateLoad(int newload) {
		serviceLoad = newload;
	}

	public synchronized int getServiceLoad() {
		return serviceLoad;
	}

	public synchronized void incrementLoad() {
		serviceLoad++;
	}

	public synchronized void decrementLoad() {
		serviceLoad--;
	}

	public synchronized void setLoad(int newLoad) {
		serviceLoad = newLoad;
	}

	public synchronized void incrementAgentCount(String agentID) {
		if (!agentList.contains(agentID)) {
			agentList.add(agentID);
			syncAgentCount++;
		}
	}

	public synchronized void decrementAgentCount(String agentID) {
		if (agentList.contains(agentID)) {
			agentList.remove(agentID);
			syncAgentCount--;
		}
	}

	public String getFirstAgent() {
		return agentList.getFirst();
	}

	public synchronized int getClientCount() {
		return syncAgentCount;
	}

	public synchronized void updatebitSet(BitSet set) {
		bits = (BitSet) set.clone();
	}
}
