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
package org.glassfish.webservices;




/**
 * Runtime dispatch information about one ejb web service
 * endpoint.  This class must support concurrent access,
 * since a single instance will be used for all web
 * service invocations through the same ejb endpoint.
 *
 * @author Kenneth Saks
 */
public class Ejb2RuntimeEndpointInfo extends EjbRuntimeEndpointInfo {


   /* private Class tieClass;

    // Lazily instantiated and cached due to overhead
    // of initialization.
    private Tie tieInstance;

    private ServerAuthConfig serverAuthConfig;


    public Ejb2RuntimeEndpointInfo(WebServiceEndpoint webServiceEndpoint,
                                  StatelessSessionContainer ejbContainer, 
                                  Object servant, Class tie) {
                                  
        super(webServiceEndpoint, ejbContainer, servant);
        tieClass = tie;

	try {
	    // merge message security policy from domain.xml and sun-specific
	    // deployment descriptor
	    serverAuthConfig = ServerAuthConfig.getConfig
		(com.sun.enterprise.security.jauth.AuthConfig.SOAP,
		 endpoint.getMessageSecurityBinding(),
		 null);
	} catch (com.sun.enterprise.security.jauth.AuthException ae) {
            logger.log(Level.SEVERE, 
		       "EJB Webservice security configuration Failure", ae);
	}

    }

    public Handler getHandlerImplementor(MessageContext msgContext)
        throws Exception {

        // We need to split the preInvoke tasks into stages since handlers
        // need access to java:comp/env and method authorization must take
        // place before handlers are run.  Note that the application 
        // classloader was set much earlier when the invocation first arrived
        // so we don't need to set it here.
        Invocation inv = new Invocation();

        // Do the portions of preInvoke that don't need a Method object.
        inv.isWebService = true;
        inv.container = container;
        inv.messageContext = msgContext;
        inv.transactionAttribute = Container.TX_NOT_INITIALIZED;

        // If the endpoint has at least one handler, method
        // authorization will be performed by a container-provided handler
        // before any application handler handleRequest methods are called.
        // Otherwise, the ejb container will do the authorization.
	inv.securityPermissions =  Container.SEC_NOT_INITIALIZED;

        invManager.preInvoke(inv);

        // In all cases, the WebServiceInvocationHandler will do the
        // remaining preInvoke tasks : getContext, preInvokeTx, etc.

        // Create the tie and servant to pass to jaxrpc runtime system.
        // The servant is a dynamic proxy implementing the Service Endpoint
        // Interface.  Use endpoint address uri to disambiguate case where
        // an ejb implements more than one endpoint.
        //
        // NOTE : Tie instance MUST be created after InvManager.preInvoke,
        // since tie initialization could result in handler instance creation.
        // This also means ejb container handler cannot expect to access 
        // Invocation object from Handler.init()

        // Both tie and ejb container servant support concurrent access,
        // so lazily create tie and use the same instance for all invocations
        // through this ejb endpoint.  Tie instance is a heavyweight resource 
        // so it would be prohibitive to create one per thread.
        synchronized(this) {
            if( tieInstance == null ) {
                tieInstance = (Tie) tieClass.newInstance();
                tieInstance.setTarget((Remote) webServiceEndpointServant);
            }
        }

        inv.setWebServiceTie(tieInstance);

        return (Handler) tieInstance;
    }

    *//**
     * Called after attempt to handle message.  This is coded defensively
     * so we attempt to clean up no matter how much progress we made in
     * getImplementor.  One important thing is to complete the invocation
     * manager preInvoke().
     *//*
    public void releaseImplementor(Handler handler) {
        super.releaseImplementor();
    }

    public EjbMessageDispatcher getMessageDispatcher() {
        // message dispatcher is stateless, no need to synchronize, worse
        // case, we'll create too many.
        if (messageDispatcher==null) {
            messageDispatcher = new EjbWebServiceDispatcher();
        }
        return messageDispatcher;
    }

    public ServerAuthConfig getServerAuthConfig() {
        return serverAuthConfig;
    }*/
}
