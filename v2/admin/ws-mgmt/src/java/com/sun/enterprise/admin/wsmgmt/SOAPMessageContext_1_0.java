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
package com.sun.enterprise.admin.wsmgmt;

import javax.xml.soap.SOAPMessage;
import java.util.Iterator;

/**
 * SOAP Message Context. This encapsulates the required data for web services
 * management either from JAXWS 1.0 or 2.0 MessageContext.
 */
public class SOAPMessageContext_1_0 implements SOAPMessageContext {

    /**
     * Constructed from JAXWS 1.0 MessageContext
     *
     * @param   cause  the cause of this error
     */
    public SOAPMessageContext_1_0(
        com.sun.xml.rpc.spi.runtime.SOAPMessageContext smc) {
        if ( smc == null) {
            throw new IllegalArgumentException();
        }
        _smc = smc;
    }

    /**
     * Gets the SOAPMessage for this web service invocation.
     *
     * @return  SOAPMessage for this web service invocation
     */
    public SOAPMessage getMessage() {
        return _smc.getMessage();
    }

    /**
     * Gets the PropertyNames for this web service invocation.
     *
     * @return  PropertyNames for this web service invocation
     */
    public Iterator getPropertyNames() {
        return _smc.getPropertyNames();
    }

    /**
     * Gets the PropertyNames for this web service invocation.
     *
     * @return  PropertyNames for this web service invocation
     */
    public Object getProperty(String name) {
        return _smc.getProperty(name);
    }

    /**
     * Sets the SOAPMessage in the message context.
     *
     * @param msg the SOAPMessage to be set in the message context
     */
    public void setMessage(SOAPMessage msg) {
        _smc.setMessage(msg);
    }

    /**
     * Sets the message context.
     *
     * @param smc the new message context
     */
    public void setMessageContext(com.sun.xml.rpc.spi.runtime.SOAPMessageContext
    smc) {
        if ( smc == null) {
            throw new IllegalArgumentException();
        }
        _smc = smc;
    }

    /**
     * Gets the HTTP Request headers in the message.
     *
     * @return the HTTP Request headers in the message.
     */
    public String getHTTPRequestHeaders() {
        return null;
    }

    /**
     * Gets the HTTP Response headers in the message.
     *
     * @return the HTTP Response headers in the message.
     */
    public String getHTTPResponseHeaders() {
        return null;
    }

    // Private variables
    private com.sun.xml.rpc.spi.runtime.SOAPMessageContext _smc = null; 

}
