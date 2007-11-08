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
package com.sun.enterprise.web.connector.grizzly;

import org.apache.tomcat.util.net.ServerSocketFactory;

/**
 * Non blocking SSL interface secure instance of SelectorThread must implement.
 *
 * @author Jeanfrancois Arcand
 */
public interface SecureSelector<E> {
    
    public void setSSLImplementation(E sslImplementation);
    
    
    /**
     * Returns the list of cipher suites to be enabled when {@link SSLEngine}
     * is initialized.
     * 
     * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public String[] getEnabledCipherSuites();

    
    /**
     * Sets the list of cipher suites to be enabled when {@link SSLEngine}
     * is initialized.
     * 
     * @param cipherSuites <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public void setEnabledCipherSuites(String[] enabledCipherSuites);

   
    /**
     * Returns the list of protocols to be enabled when {@link SSLEngine}
     * is initialized.
     * 
     * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */  
    public String[] getEnabledProtocols();

    
    /**
     * Sets the list of protocols to be enabled when {@link SSLEngine}
     * is initialized.
     * 
     * @param protocols <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */    
    public void setEnabledProtocols(String[] enabledProtocols);   
    
    /**
     * Returns <tt>true</tt> if the SSlEngine is set to use client mode
     * when handshaking.
     */
    public boolean isClientMode() ;


    /**
     * Configures the engine to use client (or server) mode when handshaking.
     */    
    public void setClientMode(boolean clientMode) ;

    
    /**
     * Returns <tt>true</tt> if the SSLEngine will <em>require</em> 
     * client authentication.
     */   
    public boolean isNeedClientAuth() ;

    
    /**
     * Configures the engine to <em>require</em> client authentication.
     */    
    public void setNeedClientAuth(boolean needClientAuth) ;

    
    /**
     * Returns <tt>true</tt> if the engine will <em>request</em> client 
     * authentication.
     */   
    public boolean isWantClientAuth();

    
    /**
     * Configures the engine to <em>request</em> client authentication.
     */    
    public void setWantClientAuth(boolean wantClientAuth); 
    
    
    /**
     * Return the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */    
    public ServerSocketFactory getServerSocketFactory();

    
    /**
     * Set the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */
    public void setServerSocketFactory(ServerSocketFactory factory);

}
