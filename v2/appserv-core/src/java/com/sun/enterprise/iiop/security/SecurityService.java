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

package com.sun.enterprise.iiop.security;

import com.sun.enterprise.iiop.security.SecurityContext;

/*
 * This is an interface between the CSIV2 interceptors 
 * and the rest of the J2EE RI.
 *
 */

public interface SecurityService {
    public static final int STATUS_PASSED = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_RETRY = 2;

    /**
     * This is called by the CSIv2 interceptor on the client before
     * sending the IIOP message. 
     * @param the effective_target field of the PortableInterceptor 
     * ClientRequestInfo object.
     * @return a SecurityContext which is marshalled into the IIOP msg
     * by the CSIv2 interceptor.
     */
    SecurityContext getSecurityContext(org.omg.CORBA.Object effective_target)
	throws InvalidMechanismException, InvalidIdentityTokenException;
    /**
     * This is called by the CSIv2 interceptor on the client after
     * a reply is received.
     * @param the reply status from the call. The reply status field
     * could indicate an authentication retry.
     * The following is the mapping of PI status to the reply_status field
     * PortableInterceptor::SUCCESSFUL -> STATUS_PASSED
     * PortableInterceptor::SYSTEM_EXCEPTION -> STATUS_FAILED
     * PortableInterceptor::USER_EXCEPTION -> STATUS_PASSED
     * PortableInterceptor::LOCATION_FORWARD -> STATUS_RETRY
     * PortableInterceptor::TRANSPORT_RETRY -> STATUS_RETRY
     * @param the effective_target field of the PI ClientRequestInfo object.
     */
    void receivedReply(int reply_status, org.omg.CORBA.Object effective_target);

    /**
     * This is called by the CSIv2 interceptor on the server after
     * receiving the IIOP message. If authentication fails a FAILED status
     * is returned. If a FAILED status is returned the CSIV2 interceptor will
     * marshall the MessageError service context and throw the NO_PERMISSION 
     * exception.
     * @param the SecurityContext which arrived in the IIOP message.
     * @return the status
     */
    int setSecurityContext(SecurityContext context, byte[] object_id, String method);

    /**
     * This is called by the CSIv2 interceptor on the server before
     * sending the reply.
     * @param the SecurityContext which arrived in the IIOP message.
     */
    void sendingReply(SecurityContext context);
    
    /**
     * This is called on the server to unset the security context 
     * this is introduced to prevent the re-use of the thread
     * security context on re-use of the thread.
     */
    public void unsetSecurityContext();
}

