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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *Wrapper around HttpServletRequest used for forwarding requests for the 
 *initial JNLP document for an app client - which arrives at the ad hoc 
 *servlet - off to the system servlet for processing.
 *
 * @author tjquinn
 */
public class JWSAdHocServletRequestWrapper extends HttpServletRequestWrapper {
    
    private String adjustedPathInfo;
    
    /** Creates a new instance of AdHocServletRequestWrapper */
    public JWSAdHocServletRequestWrapper(HttpServletRequest request, String contextRoot, String category) {
        super(request);
        setUserInfo(contextRoot, category);
    }
    
    private void setUserInfo(String contextRoot, String category) {

        adjustedPathInfo = "/" + removeLeadingSlash(category) + "/" + removeLeadingSlash(contextRoot);
    }
    
    /**
     *Returns the adjusted path information for use in the system web app.
     *@return path info reflecting the request category (appclient or application)
     *and the context root indicating which app client is of interest
     */
    public String getPathInfo() {
        return adjustedPathInfo;
    }
    
    /**
     *Removes any leading slash from the path segment.
     *@param path the path from which to remove any leading slash
     *@return the path with any leading slash removed
     */
    private String removeLeadingSlash(String path) {
        String result = null;
        /*
         *Exclude a slash at the beginning of the context root.
         */
        int slash = path.indexOf("/");
        if (slash == 0 && path.length() > 1) {
            result = path.substring(1);
        } else {
            result = path;
        }
        return result;
    }
}
