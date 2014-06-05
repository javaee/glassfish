/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.examples.configuration.webserver;

import java.io.File;
import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * This is a fake WebServer that is designed to show the features of
 * the HK2 configuration system.  This class is intended to have parameters
 * that might be useful for a real web server, some of which are dynamic
 * and some of which are not dynamic.  They will be configured using the
 * HK2 configuration system
 * <p>
 * 
 * @author jwells
 *
 */
@Contract 
public interface WebServer {
    /**
     * Gets the name of this web server
     * 
     * @return The name of this web server
     */
    public String getName();
    
    /**
     * Opens the admin port, and returns the number
     * of the port open
     * 
     * @return The admin port opened
     */
    public int openAdminPort();
    
    /**
     * Opens the SSL port, and returns the number
     * of the port open
     * 
     * @return The SSL port open
     */
    public int openSSLPort();
    
    /**
     * Opens the non-SSL port, and returns the number
     * of the port open
     * 
     * @return The non-SSL port open
     */
    public int openPort();
    
    /**
     * Gets the current admin port, or -1
     * if the port is not open
     * 
     * @return The current admin port, or -1
     */
    public int getAdminPort();
    
    /**
     * Gets the current SSL port, or -1
     * if the port is not open
     * 
     * @return The current SSL port, or -1
     */
    public int getSSLPort();
    
    /**
     * Gets the current HTTP port, or -1
     * if the port is not open
     * 
     * @return The current HTTP port, or -1
     */
    public int getPort();
    
    /**
     * Gets the list of certificates that are
     * used by this web server
     * 
     * @return A non-null but possibly empty set
     * of Files pointing to the public certificates
     * of the web server
     */
    public List<File> getCertificates();
    
    
}
