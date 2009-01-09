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

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.glassfish.synchronization.client.ServerInfo;
import org.glassfish.synchronization.loadbalancer.BitAndAddr;
import org.glassfish.synchronization.message.Ping;
import org.glassfish.synchronization.message.PingACK;
import org.glassfish.synchronization.util.HttpHandler;

/**
 * Class that loops through all the completely synchronized servers and makes
 * sure that the all the servers in the list object are up and running. This is
 * done through periodic pings. If server does not respond then remove it from
 * the list.
 * 
 * @author Behrooz Khorashadi
 */
public class Pinger implements Runnable {
	private List<BitAndAddr> serverList;

	public Pinger(List<BitAndAddr> m) {
		serverList = m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(true) {
			pingServers();
			try {
				if (serverList.isEmpty())
					Thread.sleep(2 * SLEEP_INTERVAL);
				else
					Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private void pingServers() {
		HttpURLConnection syncConn = null;
		URL url;
		List<BitAndAddr> failedList = new LinkedList<BitAndAddr>();
		LinkedList<BitAndAddr> servers = new LinkedList<BitAndAddr>();
		synchronized(serverList) {
			servers.addAll(serverList);
		}
		for (BitAndAddr temp : servers) {
			if (syncConn != null)
				syncConn.disconnect();
			try {
				url = new URL(temp.address);
				syncConn = (HttpURLConnection) url.openConnection();
				syncConn.setRequestMethod("POST");
				syncConn.setReadTimeout(READ_TIMEOUT);
				syncConn.setDoInput(true);
				syncConn.setDoOutput(true);
				syncConn.connect();
				HttpHandler.send((Object) new Ping(), syncConn);
				PingACK reply = (PingACK) HttpHandler.receive(syncConn);
				temp.setLoad(reply.load);
//				 System.out.println("Got a ping response from " + temp.address
//				 + " with load " + reply.load);
			} catch (ConnectException e) {
				failedList.add(temp);
			} catch (MalformedURLException e) {
				failedList.add(temp);
			} catch (IOException e) {
				failedList.add(temp);
			} catch (ClassNotFoundException e) {
				failedList.add(temp);
			}
		}
		updateList(failedList);
	}
	/**
	 * Removes all the nodes which failed the ping test.
	 * 
	 * @param failedList
	 *            the list of all the failed nodes
	 */
	private void updateList(List<BitAndAddr> failedList) {
		synchronized (serverList) {
			for (BitAndAddr remove : failedList) {
				Iterator<BitAndAddr> servers = serverList.iterator();
				BitAndAddr temp;
				while (servers.hasNext()) {
					temp = servers.next();
					if (temp.address.equals(remove.address)) {
						servers.remove();
//						System.out.println("Server remved" + temp.address);
					}
				}
			}
		}
	}

	/** Sleeping interval in milliseconds */
	private long SLEEP_INTERVAL = 10000;
	/** The time to wait before failure in milliseconds */
	private static final int READ_TIMEOUT = 10000;
}
