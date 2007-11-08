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
package com.sun.enterprise.ee.synchronization;


/**
 * Represents the synchronization client interface. This synchronization
 * driver implementation will allow server instances and node agents to 
 * synchronize the configuration (and applications, etc.) to the local 
 * cache.
 * 
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public interface SynchronizationDriver {
    
    /** 
     * Returns true if this driver understands the subprotocol
     * specified in the URL.
     *
     * @param  url  the URL to the repository management system
     * @return      true if this driver understands the supprotocol
     */
    public boolean acceptsURL(String url);

    /**
     * Returns this driver's major version number.
     *
     * @return  major version number of this driver
     */
    public int getMajorVersion();

    /**
     * Returns this driver's minor version number.
     *
     * @return  minor version number of this driver
     */
    public int getMinorVersion();

    /**
     * Attempts to synchronize the client (server instance, node agent) 
     * with the central repository.
     *
     * @throws  SynchronizationException  if an error occurred during the 
     *                                    synchronization process
     */
    public void synchronize() throws SynchronizationException;
}
