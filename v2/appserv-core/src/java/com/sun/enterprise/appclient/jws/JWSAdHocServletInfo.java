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

import com.sun.enterprise.web.AdHocServletInfo;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *Info describing the ad hoc servlet that is dynamically registered with the web 
 *container to receive Java Web Start's requests for the JNLP document.
 *<p>
 *The web container uses this information in setting up the dynamic mapping
 *from URLs to our code.
 *
 * @author tjquinn
 */
public class JWSAdHocServletInfo implements AdHocServletInfo {
    
    /** holds init parameter names and values that will be available to the servlet */
    private HashMap<String,String> initParams;
    
    /** holds the name that distinguishes ad hoc servlets within an app */
    private String servletName;
    
    /** Creates a new instance of JWSAdHocServletInfo */
    public JWSAdHocServletInfo(String virtualContextRoot, String category) {
        setInitParams(virtualContextRoot, category);
        servletName = virtualContextRoot;
    }

    /**
     *Reports which class implements the ad hoc servlet to be run.
     *@return Class for the ad hoc servlet class
     */
    public Class getServletClass() {
        return JWSAdHocServlet.class;
    }

    /**
     *Returns a map from servlet init param name to value.
     *@return Map<String,String> of init param names to values
     */
    public java.util.Map<String, String> getServletInitParams() {
        return initParams;
    }

    /**
     *Reports the servlet's name.
     *@return the name of the servlet for display purposes
     */
    public String getServletName() {
        return servletName;
    }
    
    /**
     *Sets up the map of init param names to values that will be retrieved
     *by the web container.
     *@param the context root by which the ad hoc servlet will be addressed
     *@param the category (basically either application or appclient)
     */
    private void setInitParams(String virtualContextRoot, String category) {
        initParams = new HashMap<String,String>();
        initParams.put(JWSAdHocServlet.CONTEXT_ROOT_PARAMETER_NAME, virtualContextRoot);
        initParams.put(JWSAdHocServlet.CATEGORY_PARAMETER_NAME, category);
    }
}
