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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;

import org.glassfish.synchronization.filemanagement.FileServiceManagerV2;
import org.glassfish.synchronization.loadbalancer.LimitBalancer;
import org.glassfish.synchronization.loadbalancer.LoadBalancerInterface;
import org.glassfish.synchronization.loadbalancer.MessageContext;
import org.glassfish.synchronization.loadbalancer.ServeAllBalancer;
import org.glassfish.synchronization.message.*;
import org.glassfish.synchronization.util.HttpHandler;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;



/**
 * Client side servlet that is used by grizzly server
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncAdapter extends GrizzlyAdapter implements CallBackInterface {
	/** load balancer which handles queries and redirection */
	protected LoadBalancerInterface balancer;
	/** The glassfish v2 filemangement object */
	private FileServiceManagerV2 v2Server;
	// LoadBalancerInterface balancer = new SimpleRandomBalancer();
	protected SyncContext context;
	/** Context at which this adapter responds */
	protected final String CR = "/synchronization";
	 
	public SyncAdapter() {
		balancer = new LimitBalancer();
	}
	public SyncAdapter(LoadBalancerInterface b) {
		balancer = b;
	}
	public SyncAdapter(SyncContext c) {
		super();
		context = c;
		balancer = new LimitBalancer();
		context.setLoadBalancer(balancer);
		v2Server = new FileServiceManagerV2(c);
	}

	public SyncAdapter(SyncContext c, LoadBalancerInterface b) {
		super();
		context = c;
		balancer = b;
		context.setLoadBalancer(balancer);
		v2Server = new FileServiceManagerV2(c);
	}

	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		// System.out.println("incoming port is " + request.getRemoteAddr() +
		// ":"+request.getRemotePort());
		Object req = getRequestObject(request);
		handleRequestObject(req, request, response);
	}

	protected void handleRequestObject(Object req, GrizzlyRequest request,
			GrizzlyResponse response) {
		try {
			ClientHeaderInfo cInfo = new ClientHeaderInfo(request);
			setResponseHeader(response);
			if (req instanceof JoinRequest) {
				processJoin(cInfo, response);
			} else if (req instanceof FileRequest) {
				processFileRequest((FileRequest) req, cInfo, response, request
						.getHeader(ClientHeaderInfo.MANIFEST_VERSION_HEADER));
			} else if (req instanceof Fin) {
				handleSyncComplete((Fin) req, cInfo, response);
			} else if (req instanceof Ping) {
				handlePing(req, response);
			} else if (req instanceof VersionRequest) { // this is used to mimic
														// v2 behavior
				handleSimpleRequest((VersionRequest) req, response);
			} else {
				response
						.getWriter()
						.println(
								"Grizzly is soo cool..but you request cannot be found!");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleSimpleRequest(VersionRequest req,
			GrizzlyResponse response) throws IOException {
		HttpHandler.sendGrizzlyResponse((Object) v2Server
				.createReply(req.version), response);
	}

	private void handlePing(Object req, GrizzlyResponse response)
			throws IOException {
		setResponseHeader(response);
		HttpHandler.sendGrizzlyResponse(
				(Object) new PingACK(balancer.getLoad()), response);
	}

	protected Object getRequestObject(GrizzlyRequest request) {
		InputStream in = null;
		Object req = null;
		try {
			in = request.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(in);
			req = ois.readObject();
		} catch (Exception e) {
			return null;
		}
		return req;
	}

	protected void setResponseHeader(GrizzlyResponse response) {
		response.setHeader(ClientHeaderInfo.MANIFEST_VERSION_HEADER, String
				.valueOf(context.getManifestManager().getManVersion()));
	}

	protected void handleSyncComplete(Fin req, ClientHeaderInfo clientInfo,
			GrizzlyResponse response) {
		balancer.syncCompleted(clientInfo);
		try {
			HttpHandler.sendGrizzlyResponse((Object) new FinACK(), response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void processJoin(ClientHeaderInfo clientInfo,
			GrizzlyResponse response) throws IOException {
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "Recieved Join Request";
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().fine(log);
		}
		balancer.joinReq(new MessageContext(clientInfo, response, this));
		
	}

	protected void processFileRequest(FileRequest req,
			ClientHeaderInfo clientInfo, GrizzlyResponse response,
			String manifestV) throws IOException {
		if (!checkManifestHeaderOK(manifestV)) {
			// TODO: send a reset message
		}
		balancer.fileReq(new MessageContext(clientInfo, response, req, this));
		// if(action.contains("http://"))
		// sendRedirect(response, action);
		// else {
		// System.out.println("the this is " + action);
		// response.setHeader(ClientHeaderInfo.KEY_HEADER, action);
		// ZipMessage reply =
		// context.getFileManager().createReply(req.getNeedBits());
		// HttpHandler.sendGrizzlyResponse((Object)reply, response);
		// }
	}

	// private void sendRedirect(GrizzlyResponse response, String redirectAddr)
	// throws IOException {
	// RedirectMessage redirect = new RedirectMessage(redirectAddr,
	// context.getManifestManager().getManVersion());//use redirect manager
	// if (context.getLogger().isLoggable(Level.FINE)) {
	// String log = "Redirecting to: " + redirect.getRedirectAddress();
	// if(context.getStaticSyncInfo().getSysOut())
	// System.out.println(log);
	// context.getLogger().fine(log);
	// }
	// HttpHandler.sendGrizzlyResponse((Object)redirect, response);
	// }
	protected boolean checkManifestHeaderOK(String manV) {
		long manVer = Long.parseLong(manV);
		long currentManVersion = context.getManifestManager().getManVersion();
		if (currentManVersion < manVer) {
			context.getLogger().warning("My manifest version is out of date");
			System.exit(-1); // TODO: this would be very bad reset
		} else if (currentManVersion > manVer) {
			context.getLogger().warning("Source manifest is out of old");
			return false;
		}
		return true;
	}

	public void redirect(MessageContext msgContext, String redirectAddr)
			throws IOException {
		if (redirectAddr.equals(msgContext.cInfo.serverAddr))
			System.err.println("This shouldn't be happening " + redirectAddr);
		RedirectMessage redirect = new RedirectMessage(redirectAddr, context
				.getManifestManager().getManVersion());// use redirect manager
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "Redirecting " + msgContext.cInfo.serverAddr + " to: "
					+ redirect.getRedirectAddress();
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().fine(log);
		}
		HttpHandler.sendGrizzlyResponse((Object) redirect, msgContext.response);

	}

	public void serviceRequest(MessageContext msgContext) throws IOException {
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = msgContext.cInfo.toString()
					+ " Recieved file synce request with "
					+ msgContext.reqMsg.getNeedBits();
			System.out.println(log);
			context.getLogger().fine(log);
		}
		ZipMessage reply = context.getFileManager().createReply(
				msgContext.reqMsg.getNeedBits());
		msgContext.response.setHeader(ClientHeaderInfo.KEY_HEADER,
				msgContext.cInfo.idKey);
		try {
			HttpHandler
					.sendGrizzlyResponse((Object) reply, msgContext.response);
		} catch (IOException e) {
			System.err.println("Exception " + msgContext.cInfo.serverAddr);
			throw e;
		}
	}

	public void sendManifest(MessageContext msgContext) throws IOException {
		File manifest = context.getFileManager().getZippedManifest();
		msgContext.response.setHeader(ClientHeaderInfo.KEY_HEADER,
				msgContext.cInfo.idKey);
		AcceptJoinResponse reply = new AcceptJoinResponse(
				balancer.serverList(), manifest, context.getManifestManager()
						.getBitManifest());
		HttpHandler.sendGrizzlyResponse((Object) reply, msgContext.response);
		if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "Accepted and Served manifest";
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().fine(log);
		}

	}
	protected void setContext(SyncContext c) {
		context = c;
		context.setLoadBalancer(balancer);
		v2Server = new FileServiceManagerV2(c);
	}
	public String getContextRoot() {
		return CR;
	}
}
