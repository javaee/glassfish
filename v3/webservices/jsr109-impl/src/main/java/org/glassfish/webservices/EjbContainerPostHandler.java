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

import java.lang.reflect.Method;

import java.rmi.UnmarshalException;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This handler is inserted last in the handler chain for an
 * ejb web service endpoint.  It ensures that the application handlers
 * did not change anything in the soap message that would change the
 * dispatch method.
 *
 * @author Kenneth Saks
 */
public class EjbContainerPostHandler extends GenericHandler {

    private static Logger logger = 
        LogDomains.getLogger(EjbContainerPostHandler.class, LogDomains.EJB_LOGGER);
    private WsUtil wsUtil = new WsUtil();

    public EjbContainerPostHandler() {}

    public QName[] getHeaders() {
        return new QName[0];
    }

    public boolean handleRequest(MessageContext context) {
        /*
        Invocation inv = null;
        boolean continueProcessing = true;

        try {
            Switch theSwitch = Switch.getSwitch();
            InvocationManager invManager = theSwitch.getInvocationManager();
            inv = (Invocation) invManager.getCurrentInvocation();
            Container container = (Container) inv.container;

            Method webServiceMethodInPreHandler = inv.getWebServiceMethod();

            if( webServiceMethodInPreHandler != null ) {
                // Now that application handlers have run, do another method
                // lookup and compare the results with the original one.  This
                // ensures that the application handlers have not changed
                // the message context in any way that would impact which
                // method is invoked.
                Method postHandlerMethod = 
                    wsUtil.getInvMethod(inv.getWebServiceTie(), context);
                if( !webServiceMethodInPreHandler.equals(postHandlerMethod) ) {
                    inv.exception = new UnmarshalException
                        ("Original method " + webServiceMethodInPreHandler + 
                         " does not match post-handler method " + 
                         postHandlerMethod);
                }
            }
        } catch(Exception e) {
            String errorMsg = "Exception while getting method for " +
                ((inv != null ) ?
                 ((Container) inv.container).getEjbDescriptor().getName() : "");
            inv.exception = new UnmarshalException(errorMsg);
            inv.exception.initCause(e);
        }
        
        if( inv.exception != null ) {
            logger.log(Level.WARNING, "postEjbHandlerError", inv.exception);
            wsUtil.throwSOAPFaultException(inv.exception.getMessage(),
                                           context);
        } */
        return true;
    }
}
