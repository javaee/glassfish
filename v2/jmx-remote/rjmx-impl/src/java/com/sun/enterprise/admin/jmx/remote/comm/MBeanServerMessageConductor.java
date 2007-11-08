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

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/MBeanServerMessageConductor.java,v 1.6 2007/03/28 02:37:33 ne110415 Exp $
 * $Revision: 1.6 $
 * $Date: 2007/03/28 02:37:33 $
 */

package com.sun.enterprise.admin.jmx.remote.comm;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.jmx.remote.internal.UrlConnectorFactory;
import java.io.IOException;
import java.net.URL;
import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;
import com.sun.enterprise.admin.jmx.remote.streams.StreamMBeanServerRequestMessage;
import com.sun.appserv.management.client.RedirectException;
/** A Class that uses an instance of {@link IConnection} to actually invoke
 * some operation on remote resource and read the response back. What is Serialized
 * and deserialized contains instances of {@link MBeanServerRequestMessage} and 
 * {@link MBeanServerResponseMessage} class. Note that all the objects travelling
 * back and forth have to be serializable. There is no (dynamic) class loader support
 * provided here. The classes and their versions have to be agreed upon by the
 * client and server sides.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class MBeanServerMessageConductor {
    
    private IConnection connection;
    private boolean autoRedirect = true;
    /** Creates a new instance of MessageConductor */
    public MBeanServerMessageConductor(IConnection connection) {
        this.connection = connection;
        String redirect = (String)System.getProperty(DefaultConfiguration.REDIRECTION);
        if ("false".equalsIgnoreCase(redirect)) autoRedirect = false;
    }
    
    public MBeanServerResponseMessage invoke(int methodId, Object[] params)
    throws Exception {
        final StreamMBeanServerRequestMessage request = 
            new StreamMBeanServerRequestMessage(methodId, params, null); 
                // delegationSubject to be considered: todo
        connection.send(request);
        MBeanServerResponseMessage response = null;        
        try {
            response = ((MBeanServerResponseMessage)connection.receive());
        } catch(RedirectException ex) {
            // if auto redirect is disabled throw the exception as is
            if (!autoRedirect) throw ex;
            // So we have received a redirect in response. So fetch the
            // redirection details 
            processRedirect(ex); // basically change existing connection
                                 // to reflect received redirect URL
            response = invoke(request);        
        }
        return response;
    }

    /**
     * This method is a second chance to invoke the 
     * operation again with the right redirected URL 
     */
    public MBeanServerResponseMessage invoke(
        StreamMBeanServerRequestMessage request) throws Exception {
        
        connection.send(request);
        return ((MBeanServerResponseMessage)connection.receive());
    }

    private void processRedirect(RedirectException ex) throws IOException {
        URL redirect = ex.getRedirectURL();

        // Check if redirected URL is valid in terms of 
        // 1. Validity of new redirect URL itself
        // 2. if redirection is not downgrading security unintentionally
        // If it is invalid rethrow the RedirectException to CLI 
        // for further processing - logging and user communciation
        if (isRedirectionInvalid(redirect, connection)) {
            throw new RedirectException(ex.getRedirectURLStr(), 
                "Invalid Redirect. " +
                "Security cannot be downgraded. Please try with --secure=false");
        }
        connection = getConnectionWithRedirectedURL(connection, redirect);
    }

    private IConnection getConnectionWithRedirectedURL(
        IConnection connection, URL redirect) throws IOException {
        
        ServletConnection sc = ((ServletConnection)connection);
        
        // create new connection details. 
          // 1. Use host from original connection and port from redirected URL
        HttpConnectorAddress hca = new HttpConnectorAddress(
            sc.getURL().getHost(), redirect.getPort());
          // 2. Use path from original connection
        hca.setPath(sc.getURL().getPath());
          // 3. Use authentication info from original connection
        hca.setAuthenticationInfo(
            sc.getHttpConnectorAddress().getAuthenticationInfo());
          // 4. Use Protocol from redirected URL to determine secure flag
        if (redirect.getProtocol().equalsIgnoreCase(
                HttpConnectorAddress.HTTPS_CONNECTOR)) {
            hca.setSecure(true);
        } else if (redirect.getProtocol().equalsIgnoreCase(
                HttpConnectorAddress.HTTP_CONNECTOR)) {
            hca.setSecure(false);
        }
        return ConnectionFactory.createConnection(hca);       
    }
    
    private boolean isRedirectionInvalid(URL redirect, IConnection origConn) {
        ServletConnection sc = ((ServletConnection)origConn);
        // check if redirect malformed i.e. it translates to a null. See 
        // RedirectException.getRedirectURL()
        if (redirect == null) return true;
        // check if the URL has been morphed by any evil interceptors
        String redirectedHost = redirect.getHost();
        String origHost = sc.getURL().getHost();
        if (redirectedHost == null) return true;
        if (!redirectedHost.equalsIgnoreCase(origHost)) return true;
        // check if security is being downgraded. For e.g. CLI specifies
        // --secure=true and redirect contains HTTP
        String newProtocol = redirect.getProtocol();
        String origProtocol = sc.getURL().getProtocol();
        if (origProtocol.equalsIgnoreCase(HttpConnectorAddress.HTTPS_CONNECTOR) 
            && newProtocol.equalsIgnoreCase(HttpConnectorAddress.HTTP_CONNECTOR)) 
            return true;
        // Everything seems to be fine. Return "no issues"
        return false;
    }
}
