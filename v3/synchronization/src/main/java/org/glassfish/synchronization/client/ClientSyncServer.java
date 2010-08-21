/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import java.net.InetAddress;
import java.net.ServerSocket;

import org.glassfish.synchronization.util.StaticSyncInfo;

import com.sun.grizzly.http.embed.GrizzlyWebServer;

/**
 * Object which initializes the client side server on a random port
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class ClientSyncServer {
	private SyncContext context;
	private GrizzlyWebServer ws;
	private int freePort;

	public ClientSyncServer(SyncContext c) {
		context = c;
		freePort = findFreePort();
		ws = new GrizzlyWebServer(freePort, "/var/www");
	}

	public void start() {
		ws.addGrizzlyAdapter(new SyncAdapter(context));
		try {
			ws.start();
			InetAddress addr = InetAddress.getLocalHost();
			StaticSyncInfo syncInfo = context.getStaticSyncInfo();
			if (addr != null) {
				syncInfo.setServerIP(addr.getHostAddress());
			}
			syncInfo.setPort(freePort);
			// System.out.println("Grizzly has Started on port " +
			// context.getStaticSyncInfo().getServerAddress());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopGrizzly() {
		ws.stop();
		context.getFileUtils().cleanUpTempFiles();
	}

	private int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}
}
