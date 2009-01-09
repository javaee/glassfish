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

import com.sun.grizzly.tcp.http11.GrizzlyRequest;

/**
 * Parses and holds all the http header information that is transmitted by the
 * client
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class ClientHeaderInfo {
	public final long manV;
	public String idKey;
	public final String serverAddr;
	public final int serviceLoad;
	public int agentID;

	public ClientHeaderInfo(String version, String sessID, String add, int load) {
		manV = Long.parseLong(version);
		idKey = sessID;
		serverAddr = add;
		serviceLoad = load;
	}

	public ClientHeaderInfo(GrizzlyRequest request) {
		idKey = request.getHeader(KEY_HEADER);
		String manVersion = request.getHeader(MANIFEST_VERSION_HEADER);
		if (manVersion == null)
			manV = -1;
		else {
			long tempV = -1;
			try {
				tempV = Long.parseLong(manVersion);
			} catch (NumberFormatException e) {
				tempV = -1;
			}
			manV = tempV;
		}
		String servload = request.getHeader(SERVICE_LOAD);
		if (servload == null)
			serviceLoad = 0;
		else {
			int tempLoad = 0;
			try {
				tempLoad = Integer.parseInt(servload);
			} catch (NumberFormatException e) {
				tempLoad = 0;
			}
			serviceLoad = tempLoad;
		}
		serverAddr = request.getHeader(SERVER_ADDRESS);
		String stringAgentID = request.getHeader(SYNC_AGENT_ID);
		if (stringAgentID == null) {
			agentID = 0;
		} else {
			try {
				agentID = Integer.parseInt(stringAgentID);
			} catch (NumberFormatException e) {
				agentID = 0;
				e.printStackTrace();
			}
		}
	}

	@Override
	public int hashCode() {
		return idKey.hashCode();
	}

	public String toString() {
		String info = "idKey: " + idKey + " Server Address: " + serverAddr
				+ " Load: " + serviceLoad;
		return info;
	}

	public static final String SERVICE_LOAD = "Service_Load";
	public static final String KEY_HEADER = "ChildID";
	public static final String MANIFEST_VERSION_HEADER = "Manifest_Version";
	public static final String SERVER_ADDRESS = "Server_Address";
	public static final String SYNC_AGENT_ID = "Agent_ID";
}
