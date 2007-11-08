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

package com.sun.enterprise.webservice;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.enterprise.Switch;
import com.sun.ejb.Container;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.webservice.EjbWebServiceDispatcher;
import com.sun.enterprise.webservice.EjbRuntimeEndpointInfo;
import com.sun.enterprise.webservice.WebServiceEjbEndpointRegistry;
import com.sun.enterprise.webservice.monitoring.WebServiceTesterServlet;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.EndpointType;
import com.sun.enterprise.webservice.monitoring.AuthenticationListener;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineImpl;
import com.sun.web.security.WebPrincipal;
import com.sun.web.security.RealmAdapter;
import com.sun.logging.LogDomains;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.audit.AuditManagerFactory;

import org.apache.catalina.Loader;
import org.apache.catalina.Globals;
import org.apache.catalina.util.Base64;

/**
 * Servlet responsible for invoking EJB webservice endpoint.
 *
 * Most of this code used to be in
 * com.sun.enterprise.webservice.EjbWebServiceValve.
 *
 * @author	Qingqing Ouyang
 * @author	Kenneth Saks
 * @author	Jan Luehe
 */
public class EjbWebServiceServlet extends HttpServlet {

    private static Logger logger
        = LogDomains.getLogger(LogDomains.EJB_LOGGER);
    private static final Base64 base64Helper = new Base64();
    private static final String AUTHORIZATION_HEADER = "authorization";

    private static AuditManager auditManager =
            AuditManagerFactory.getAuditManagerInstance();

    protected void service(HttpServletRequest hreq, HttpServletResponse hresp)
            throws ServletException, IOException {

        boolean dispatch = true;

        String requestUriRaw = hreq.getRequestURI();
        String requestUri = (requestUriRaw.charAt(0) == '/') ?
            requestUriRaw.substring(1) : requestUriRaw;
        String query = hreq.getQueryString();

        // check if it is a tester servlet invocation
        if ("Tester".equalsIgnoreCase(query)) {
            Endpoint endpoint = WebServiceEngineImpl.getInstance().getEndpoint(hreq.getRequestURI());
            if((endpoint.getDescriptor().isSecure()) ||
               (endpoint.getDescriptor().getMessageSecurityBinding() != null)) {
                String message = endpoint.getDescriptor().getWebService().getName() +
                    "is a secured web service; Tester feature is not supported for secured services";
                (new WsUtil()).writeInvalidMethodType(hresp, message);                
                return;
            }            
            if (endpoint!=null && Boolean.parseBoolean(endpoint.getDescriptor().getDebugging())) {
                dispatch = false;
                WebServiceTesterServlet.invoke(hreq, hresp,
                                               endpoint.getDescriptor());
            }
        }

        if (dispatch) {
            EjbRuntimeEndpointInfo ejbEndpoint = 
                WebServiceEjbEndpointRegistry.getRegistry().getEjbWebServiceEndpoint(requestUri, hreq.getMethod(), query);

            if (ejbEndpoint != null) {
                /*
                 * We can actually assert that ejbEndpoint is != null,
                 * because this EjbWebServiceServlet would not have been
                 * invoked otherwise
                 */
                dispatchToEjbEndpoint(hreq, hresp, ejbEndpoint);
            }
        }
    }


    private void dispatchToEjbEndpoint(HttpServletRequest hreq,
                                       HttpServletResponse hresp,
                                       EjbRuntimeEndpointInfo ejbEndpoint) {

        String scheme = hreq.getScheme();
        String expectedScheme = ejbEndpoint.getEndpoint().isSecure() ?
            "https" : "http";

        if( !expectedScheme.equalsIgnoreCase(scheme) ) {
            logger.log(Level.WARNING, "Invalid request scheme for Endpoint " +
                       ejbEndpoint.getEndpoint().getEndpointName() + ". " +
                       "Expected " + expectedScheme + " . Received " + scheme);
            return;
        }

        Switch theSwitch = Switch.getSwitch();
        Container container = ejbEndpoint.getContainer();
        
        boolean authenticated = false;
        try {
            // Set context class loader to application class loader
            container.externalPreInvoke();

            // compute realmName
            String realmName = null;
            Application app = ejbEndpoint.getEndpoint().getBundleDescriptor().getApplication();
            if (app != null) {
                realmName = app.getRealm();
            }
            if (realmName == null) {
                realmName = ejbEndpoint.getEndpoint().getRealm();
            }

            if (realmName == null) {
                // use the same logic as BasicAuthenticator
                realmName = hreq.getServerName() + ":" + hreq.getServerPort();
            }
            
            try {
                authenticated = doSecurity(hreq, ejbEndpoint, realmName);
            } catch(Exception e) {
                sendAuthenticationEvents(false, hreq.getRequestURI(), null);
                logger.log(Level.WARNING, "authentication failed for " +
                           ejbEndpoint.getEndpoint().getEndpointName(),
                           e);
            }

   	        if (auditManager.isAuditOn()){

	            auditManager.ejbAsWebServiceInvocation(
                    ejbEndpoint.getEndpoint().getEndpointName(),authenticated);
            }



            if (!authenticated) {
                hresp.setHeader("WWW-Authenticate",
                        "Basic realm=\"" + realmName + "\"");
                hresp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // depending on the jaxrpc or jax-ws version, this will return the
            // right dispatcher.
            EjbMessageDispatcher msgDispatcher = ejbEndpoint.getMessageDispatcher();
            msgDispatcher.invoke(hreq, hresp, getServletContext(), ejbEndpoint);
            
        } catch(Throwable t) {
            logger.log(Level.WARNING, "", t);
        } finally {
            if( authenticated ) {
                // remove any security context from the thread before returning
                SecurityContext.setCurrent(null);
            }
            
            // Restore context class loader
            container.externalPostInvoke();
        }
        return;
    }
    
    private boolean doSecurity(HttpServletRequest hreq,
                               EjbRuntimeEndpointInfo epInfo,
                               String realmName) throws Exception {

        //BUG2263 - Clear the value of UserPrincipal from previous request
        //If authentication succeeds, the proper value will be set later in
        //this method.
        WebServiceContextImpl context = (WebServiceContextImpl)
                ((EjbRuntimeEndpointInfo)epInfo).getWebServiceContext();
        if (context != null) {
            context.setUserPrincipal(null);        
        }
        
        WebServiceEndpoint endpoint = epInfo.getEndpoint();
        boolean authenticated = false;
        
        String method = hreq.getMethod();
        if( method.equals("GET") || !endpoint.hasAuthMethod() ) {
            return true;
        }
        
        WebPrincipal webPrincipal = null;
        String endpointName = endpoint.getEndpointName();
        
        if( endpoint.hasBasicAuth() ) {
            String rawAuthInfo = hreq.getHeader(AUTHORIZATION_HEADER);
            if (rawAuthInfo==null) {
                sendAuthenticationEvents(false, hreq.getRequestURI(), null);
                return false;
            }
            
            String[] usernamePassword =
                    parseUsernameAndPassword(rawAuthInfo);
            if( usernamePassword != null ) {
                webPrincipal = new WebPrincipal
                        (usernamePassword[0], usernamePassword[1], SecurityContext.init());
            } else {
                logger.log(Level.WARNING, "BASIC AUTH username/password " +
                           "http header parsing error for " + endpointName);
            }
        } else {

            X509Certificate certs[] =  (X509Certificate[]) hreq.getAttribute(Globals.CERTIFICATES_ATTR);
            if ((certs == null) || (certs.length < 1)) {
                certs = (X509Certificate[])
                    hreq.getAttribute(Globals.SSL_CERTIFICATE_ATTR);
            }

            if( certs != null ) {
                webPrincipal = new WebPrincipal(certs, SecurityContext.init());
            } else {
                logger.log(Level.WARNING, "CLIENT CERT authentication error for " + endpointName);
            }

        }

        if (webPrincipal==null) {
            sendAuthenticationEvents(false, hreq.getRequestURI(), null);           
            return authenticated;
        }
        
        RealmAdapter ra = new RealmAdapter(realmName);
        authenticated = ra.authenticate(webPrincipal);
        if( authenticated == false ) {
            sendAuthenticationEvents(false, hreq.getRequestURI(), webPrincipal);
            logger.fine("authentication failed for " +  endpointName);
        }

        sendAuthenticationEvents(true, hreq.getRequestURI(), webPrincipal);
        if(epInfo instanceof Ejb2RuntimeEndpointInfo) {
            // For JAXRPC based EJb endpoints the rest of the steps are not needed
            return authenticated;
        }
        //Setting if userPrincipal in WSCtxt applies for JAXWS endpoints only
        epInfo.prepareInvocation(false);
        WebServiceContextImpl ctxt = (WebServiceContextImpl)
                ((EjbRuntimeEndpointInfo)epInfo).getWebServiceContext();
        ctxt.setUserPrincipal(webPrincipal);
        return authenticated;
    }       

    private String[] parseUsernameAndPassword(String rawAuthInfo) {

        String[] usernamePassword = null;
        if ( (rawAuthInfo != null) && 
             (rawAuthInfo.startsWith("Basic ")) ) {
            String authString = rawAuthInfo.substring(6).trim();
            // Decode and parse the authorization credentials
            String unencoded =
                new String(base64Helper.decode(authString.getBytes()));
            int colon = unencoded.indexOf(':');
            if (colon > 0) {
                usernamePassword = new String[2];
                usernamePassword[0] = unencoded.substring(0, colon).trim();
                usernamePassword[1] = unencoded.substring(colon + 1).trim();
            }
        }
        return usernamePassword;
    }

    private void sendAuthenticationEvents(boolean success,         
            String url, WebPrincipal principal) {
        
        Endpoint endpoint = WebServiceEngineImpl.getInstance().getEndpoint(url);
        if (endpoint==null) {
            return;
        }
        
        for (AuthenticationListener listener : WebServiceEngineImpl.getInstance().getAuthListeners()) {
            if (success) {
                listener.authSucess(endpoint.getDescriptor().getBundleDescriptor(),
                        endpoint, principal);
            } else {
                listener.authFailure(endpoint.getDescriptor().getBundleDescriptor(),
                        endpoint, principal);
            }
        }
    }
}
