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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.iiop;

import java.util.logging.Level;

import com.sun.ejb.containers.SFSBVersionManager;
import com.sun.enterprise.util.Utility;

import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;

import org.omg.PortableInterceptor.*;

/**
 * This is the implementation of the JTS PI-based client/server interceptor.
 * This will be called during request/reply invocation path.
 * 
 */
public class SFSBServerRequestInterceptor
    extends SFSBAbstractInterceptor
    implements ServerRequestInterceptor {

    private ThreadLocal<SFSBServiceContextInfo> scInfo =
        new ThreadLocal<SFSBServiceContextInfo>();
    
    public SFSBServerRequestInterceptor(Codec codec) {
        super("com.sun.enterprise.iiop.SFSBServerRequestInterceptor", codec);
    }

    // implementation of the ServerInterceptor interface.

    public void receive_request_service_contexts(ServerRequestInfo ri)
            throws ForwardRequest {
        try {
            ServiceContext serviceContext =
                ri.get_request_service_context(SFSBVersionConstants.SFSB_VERSION_SERVICE_CONTEXT_ID);
            if (serviceContext != null) {
                byte[] data = serviceContext.context_data;
                long version = Utility.bytesToLong(data, 0);
                SFSBVersionManager.setRequestClientVersion(version);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "SFSBServerReqInterceptor:: "
                            + "receive_request_service_context: " + version);
                }
                SFSBServiceContextInfo info = new SFSBServiceContextInfo();
                info.requestVersion= version;
                info.valid = true;
                scInfo.set(info);
            } else {
                scInfo.set(null);
            }
        } catch (Exception ex) {
            scInfo.set(null);
            _logger.log(Level.INFO, "SFSBServerReqInterceptor:: receive_request_service_coventext: ", ex);
        }
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest {
    }

    private void sendSFSBVersionNumber(String callName, ServerRequestInfo ri) {
        try {
            SFSBServiceContextInfo info =
                SFSBVersionManager.getServiceContext();
            if (info != null) {
                long version = SFSBVersionManager.getResponseClientVersion();
                byte[] data = new byte[8];
                Utility.longToBytes(version, data, 0);
                ri.add_reply_service_context(new ServiceContext(
                        SFSBVersionConstants.SFSB_VERSION_SERVICE_CONTEXT_ID, data), false);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "SFSBServerReqInterceptor:: "
                            + callName + ": " + version);
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.INFO, "SFSBServerReqInterceptor:: "
                    + callName + ": ", ex);
        } finally {
            SFSBVersionManager.clearServiceContextInfo();
        }
    }

    public void send_reply(ServerRequestInfo ri) {
        sendSFSBVersionNumber("send_reply", ri);
    }
    
    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest {
        sendSFSBVersionNumber("send_exception", ri);
    }

    public void send_other(ServerRequestInfo ri)
        throws ForwardRequest {
        try {
            SFSBServiceContextInfo info = SFSBVersionManager.getServiceContext();
            if (info != null) {
                _logger.log(Level.WARNING, "SFSBServerReqInterceptor:: send_other: ");
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "SFSBServerReqInterceptor:: send_other: ", ex);
        }
    }

}
