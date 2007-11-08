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

import com.sun.enterprise.util.Utility;

import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;

import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;

import java.util.logging.Level;

/**
 * This is the implementation of the JTS PI-based client/server interceptor.
 * This will be called during request/reply invocation path.
 * 
 */
public class SFSBClientRequestInterceptor
    extends SFSBAbstractInterceptor
    implements ClientRequestInterceptor {

    private static final Level TRACE_LEVEL = Level.FINE;
    private static final Level TRACE_LEVEL_FINE = Level.FINE;
    
    public SFSBClientRequestInterceptor(Codec codec) {
        super("com.sun.enterprise.iiop.SFSBClientRequestInterceptor", codec);
    }

    // implementation of the ClientInterceptor interface.

    public void send_request(ClientRequestInfo ri) throws ForwardRequest {
        if (doesSFSBVersionPolicyExist(ri)) {
            EJBTargetKeyInfo oidInfo = new EJBTargetKeyInfo(ri
                    .effective_target());
            if (_logger.isLoggable(TRACE_LEVEL_FINE)) {
                _logger.log(TRACE_LEVEL_FINE, "**SFSBClientInterceptor.send_request...: "
                    + ri.operation() + " " + oidInfo);
            }
            
            try {
                //Any anyData = ri.get_slot(getSlotID());
                long version = SFSBClientVersionManager.getClientVersion(oidInfo.getContainerId(),
                        oidInfo.getInstanceKey());
                //version = ++versionCounter;
                //anyData.insert_longlong(version);
                byte[] data = new byte[8];
                Utility.longToBytes(version, data, 0);
                ri.add_request_service_context(new ServiceContext(
                        SFSBVersionConstants.SFSB_VERSION_SERVICE_CONTEXT_ID, data),
                        false);
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "SFSBClientInterceptor.send_request. anyData: " + version);
                }
            } catch (Exception e) {
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "SFSBClientInterceptor.send_request. Exception", e);
                }
            }
        }

    }

    public void send_poll(ClientRequestInfo ri) {
        // do nothing.
    }

    private void receiveSFSBVersionNumber(String callName, ClientRequestInfo ri) {
        try {
            if (doesSFSBVersionPolicyExist(ri)) {
                EJBTargetKeyInfo oidInfo = new EJBTargetKeyInfo(ri.effective_target());
                ServiceContext ctx = ri.get_reply_service_context(
                        SFSBVersionConstants.SFSB_VERSION_SERVICE_CONTEXT_ID);
                if (ctx != null) {
                    long version = Utility.bytesToLong(ctx.context_data, 0);
                    SFSBClientVersionManager.setClientVersion(oidInfo.getContainerId(),
                        oidInfo.getInstanceKey(), version);
                    if (_logger.isLoggable(TRACE_LEVEL)) {
                        _logger.log(TRACE_LEVEL, "SFSBClientInterceptor." + callName
                                + " " + ri.operation() + " " + oidInfo
                                + " version: " + version);
                    }
                }
            }
        } catch (Throwable th) {
            _logger.log(Level.FINE, "SFSBClientInterceptor::receiveSFSBVersionNumber"
                    + " Got exception: ", th);
        }
    }

    public void receive_reply(ClientRequestInfo ri) {
        receiveSFSBVersionNumber("receive_reply", ri);
    }
    
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
        receiveSFSBVersionNumber("receive_exception", ri);
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest {
        if (doesSFSBVersionPolicyExist(ri)) {
            EJBTargetKeyInfo oidInfo = new EJBTargetKeyInfo(ri
                    .effective_target());
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.log(Level.WARNING, "**SFSBClientInterceptor.receive_other...: "
                    + oidInfo);
            }
        }
    }

    public boolean doesSFSBVersionPolicyExist(ClientRequestInfo ri) {
        boolean result = false;
        try {
            TaggedComponent sfsbVersionTag = ri
                    .get_effective_component(POARemoteReferenceFactory.SFSB_VERSION_POLICY_TYPE);
            result = (sfsbVersionTag != null);
        } catch (BAD_PARAM e) {
            // ignore
        }
        return result;
    }
    
}
