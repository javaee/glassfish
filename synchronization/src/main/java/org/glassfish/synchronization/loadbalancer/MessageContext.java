/**
 * 
 */
package org.glassfish.synchronization.loadbalancer;

import org.glassfish.synchronization.client.CallBackInterface;
import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.message.FileRequest;

import com.sun.grizzly.tcp.http11.GrizzlyResponse;

/**
 * Message Context which is used by balancer to process a request and then call
 * back the servlet in response
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class MessageContext {
	public ClientHeaderInfo cInfo = null;
	public GrizzlyResponse response = null;
	public FileRequest reqMsg = null;
	public CallBackInterface syncCallBack = null;

	public MessageContext(ClientHeaderInfo clientInfo, GrizzlyResponse r,
			CallBackInterface s) {
		cInfo = clientInfo;
		response = r;
		syncCallBack = s;
	}

	public MessageContext(ClientHeaderInfo clientInfo, GrizzlyResponse r,
			FileRequest rmsg, CallBackInterface s) {
		cInfo = clientInfo;
		response = r;
		reqMsg = rmsg;
		syncCallBack = s;
	}
}
