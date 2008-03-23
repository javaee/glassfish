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

package com.sun.enterprise.loader;

import java.net.URL;

/**
 * Represents an interface that must be implemented by classloaders
 * that don't extend URLClassLoader and are installed as the parent
 * classloader for web applications.
 *
 * This is used by the JSP engine to construct the classpath to pass to
 * javac during JSP compilation.
 */
public interface JasperAdapter {

    /**
     * Returns the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs();
}
