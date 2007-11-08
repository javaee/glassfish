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

/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.enterprise.tools.admingui.bean;

import javax.servlet.http.HttpServletRequest;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;

import com.sun.webui.jsf.bean.HelpBackingBean;

/**
 * This class extends HelpBackingBean to set requestScheme to https 
 *
 */
public class HelpBean extends HelpBackingBean {
    private String requestScheme = null;
    
    /**
     * Creates a new instance of HelpBean.
     */
    public HelpBean() {
    
    }
    
    /**
     * <p>Get the scheme that will be used for help set requests.</p>
     *
     * <p>The default is "http".</p>
     *
     *@ param String The request scheme used for JavaHelp requests.
     */
    public String getRequestScheme() {
		HttpServletRequest request = getRequestObject();
		String requestScheme = null;
		if(super.getRequestScheme() == null) {
			requestScheme = request.getScheme();
			super.setRequestScheme(requestScheme);
		}
        return requestScheme;
    }

	private HttpServletRequest getRequestObject() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext extContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
		return request;
	}

}
    
