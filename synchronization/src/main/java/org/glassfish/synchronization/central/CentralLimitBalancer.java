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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.loadbalancer.BitAndAddr;
import org.glassfish.synchronization.loadbalancer.LimitBalancer;

/**
 * This class extends the LimitBalancer class. It has the added functionality of
 * dealing with completely synced instances. This balancer is intended to
 * function on the source of the synchronization process, in the case of
 * GlassFish this would be the DAS.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class CentralLimitBalancer extends LimitBalancer {
	private List<BitAndAddr> completed = new LinkedList<BitAndAddr>();

	public CentralLimitBalancer() {
		Thread a1 = new Thread(new Pinger(completed));
		a1.start();
	}

	/**
	 * Adds information to completed list which contains all the fully synced
	 * 
	 * @param cInfo
	 */
	public void addToFullySyncedList(ClientHeaderInfo cInfo) {
		synchronized (completed) {
			completed.add(new BitAndAddr(null, cInfo.serverAddr,
					cInfo.serviceLoad));
		}
	}

	@Override
	protected String getRedirectServer(ClientHeaderInfo clientInfo) {
		BitAndAddr leastBusy = findLowestLoadServer(clientInfo);
		leastBusy.incrementLoad();
		return leastBusy.address;
	}

	@Override
	protected BitAndAddr findLowestLoadServer(ClientHeaderInfo clientInfo) {
		BitAndAddr leastbusy = super.findLowestLoadServer(clientInfo);
		synchronized (completed) {
			for (BitAndAddr temp : completed) {
				if (temp.getServiceLoad() < leastbusy.getServiceLoad()
						&& clientInfo.serverAddr != temp.address) {
					leastbusy = temp;
				}
			}
		}
		return leastbusy;
	}

	/**
	 * This check is to avoid the corner case that a node is redirected to
	 * itself
	 * 
	 * @param clientInfo
	 * @return
	 */
	@Override
	protected boolean redirectIsPossible(ClientHeaderInfo clientInfo) {
		return (sam.redirectIsPossible(clientInfo) || !completed.isEmpty());
		// return ((grizzlyServers.size() > 0 &&
		// !grizzlyServers.containsKey(clientInfo.serverAddr)) ||
		// (grizzlyServers.containsKey(clientInfo.serverAddr) &&
		// grizzlyServers.size()>1) ||
		// !completed.isEmpty());
	}

	public List<BitAndAddr> getCompleted() {
		return completed;
	}
}
