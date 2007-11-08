/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.grizzly.async.javamail;


/**
 * The interface is used by the <code>JavaMailAsyncFilter</code> to 
 * asynchronously execute an HTTP request. This interface can be implemented
 * by a Servlet, a Java client etc.
 *
 * @author Jeanfrancois Arcand
 */
public interface JavaMailAsyncFilterHandler {
    
    /**
     * Handle a <code>JavaMailAsyncFilterEvent</code>. Return <code>true</code>
     * if the asynchnous execution must continue, <code>false</code> if the HTTP
     * request needs to be executed. 
     */
    public boolean handleEvent(JavaMailAsyncFilterEvent event);
    
    
    /**
     * The email account username
     */
    public String getUserName();

    
    /**
     * The email account password
     */
    public String getPassword();

    
    /**
     * The email account  mail server
     */
    public String getMailServer();
    
    
    /**
     * The mail server port.
     */
    public String getMailServerPort();
}
