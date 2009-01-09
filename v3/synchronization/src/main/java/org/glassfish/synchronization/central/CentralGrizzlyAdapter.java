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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.client.SyncAdapter;
import org.glassfish.synchronization.client.SyncContext;
import org.glassfish.synchronization.filemanagement.FileServiceManager;
import org.glassfish.synchronization.loadbalancer.BitAndAddr;
import org.glassfish.synchronization.manifest.ManifestManager;
import org.glassfish.synchronization.message.Fin;
import org.glassfish.synchronization.message.FinACK;
import org.glassfish.synchronization.message.SyncInfoMessage;
import org.glassfish.synchronization.util.CookieManager;
import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.HttpHandler;
import org.glassfish.synchronization.util.ManifestCreator;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.glassfish.synchronization.util.ZipUtility;

import org.glassfish.api.container.Adapter;
import org.glassfish.server.ServerEnvironmentImpl;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

import java.io.File;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
/**
 * This class extends the basic synchronization adapter which runs on all the
 * instances that are attempting to synchronize. This adapter class is, however,
 * intended to run on only the source node of the synchronization process. Its
 * additional functionality handles synchronization completion messages.
 * 
 * @author Behrooz Khorashadi
 * 
 */
@Service(name="synchronization-service")
public class CentralGrizzlyAdapter extends SyncAdapter 
								   implements Adapter, PostConstruct {
    @Inject
    private ServerEnvironmentImpl env;
	public CentralGrizzlyAdapter() {
		super();
	}
	public CentralGrizzlyAdapter(SyncContext c) {
		super(c, new CentralLimitBalancer());
	}

	@Override
	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		Object req = getRequestObject(request);
		if (!(req instanceof Fin) && !(req instanceof SyncInfoMessage)) {
			super.handleRequestObject(req, request, response);
		} else if (req instanceof Fin) {
			super.handleRequestObject(req, request, response);
			ClientHeaderInfo cInfo = new ClientHeaderInfo(request);
			// System.out.println("recieved Complete");
			int numFiles = context.getManifestManager().getnumFiles();
			BitSet all = new BitSet(numFiles);
			all.set(0, numFiles);
			if (balancer instanceof CentralLimitBalancer
					&& cInfo.serverAddr != null) {
				CentralLimitBalancer b = (CentralLimitBalancer) balancer;
				b.addToFullySyncedList(cInfo);
			}
			try {
				HttpHandler
						.sendGrizzlyResponse((Object) new FinACK(), response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("Cache hit rate was: " +
			// context.getFileManager().getCacheHitRate());
		} else if (req instanceof SyncInfoMessage) {
			ClientHeaderInfo cInfo = new ClientHeaderInfo(request);
			SyncInfoMessage info = (SyncInfoMessage) req;
			// System.out.println(cInfo.toString() + " is connected to " +
			// info.connectedto);
			try {
				HttpHandler.sendGrizzlyResponse((Object) new SyncInfoMessage(
						"", 0), response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * This function is called by the hk2 framework automatically at startup of
	 * the glassfish server.
	 */
	public void postConstruct() {
		SyncContext c = new SyncContext();
		MainServerConfig server_config = 
                new MainServerConfig(env.getDomainRoot().getAbsolutePath()+
                                     File.separator);
		// Create objects needed for monitoring and managing sync process
		try {
			StaticSyncInfo syncInfo = new StaticSyncInfo(server_config, c);
			initContextObjects(c);
			createManifest(c);
			new ManifestManager(syncInfo.getManifestFilePath(), c);
			InetAddress addr = null;
			addr = InetAddress.getLocalHost();
			if (addr != null) {
				c.getStaticSyncInfo().setServerIP(addr.getHostAddress());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Logger logger = c.getLogger();
		if (logger.isLoggable(Level.FINE)) {
			String log = "Grizzly has Started on port 4848";
			c.getLogger().fine(log);
		}
		c.getStaticSyncInfo().setPort(4848);
		System.out.println("Grizzly has Started on port "
				+ c.getStaticSyncInfo().getServerAddress());
	}
	public void createManifest(SyncContext c) throws IOException {
		long t1=System.currentTimeMillis(), t2;
		ManifestCreator manC = new ManifestCreator(c);
		manC.start();
		t2 = System.currentTimeMillis();
		Logger logger = c.getLogger();
		if (logger.isLoggable(Level.FINE)) {
			c.getLogger().fine("Manifest created in " + (t2 - t1) + "msec");
		}
		
	}
	/**
	 * Initialize the basic context objects needed for running the 
	 * synchronization process
	 * @param c object that holds all the contexts
	 */
	private void initContextObjects(SyncContext c) {
		new CookieManager(c);
		new FileUtils(c);
		new ZipUtility(c);
		new FileServiceManager(c);
	}
	@Override 
	public String getContextRoot() {
		return CR;
	}
}
