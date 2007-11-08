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

package com.sun.enterprise.appclient.jws;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *The request wrapper object by which static document requests are delegated
 *to the default servlet implementation.
 *
 * @author tjquinn
 */
public class JWSSystemServletRequestWrapper extends HttpServletRequestWrapper {
  
    /** URI of the content requested, relative to the app server's installation directory */
    private URI relativeFileURI;
    
    /** Creates a new instance of JWSSystemServletRequestWrapper */
    public JWSSystemServletRequestWrapper(HttpServletRequest request, URI relativeFileURI) {
        super(request);
        this.relativeFileURI = relativeFileURI;
    }
    
    /**
     *Overrides the default implementation to provide the web container with
     *the correct path, relative to the actual doc base which is the installation
     *directory of the app server.
     *@return String containing the path information for the document
     */
    public String getPathInfo() {
        return "/" + relativeFileURI.toString();
    }
}
