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

/*
 * DefaultEnvironment.java
 *
 * Created on January 2, 2004, 2:35 PM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;

/**
 *
 * @author  sridatta
 */
public class ConfigEnvironmentImpl extends com.sun.enterprise.config.impl.ConfigEnvironmentImpl {
    
    /** Creates a new instance of DefaultEnvironment */
    public ConfigEnvironmentImpl() {
    }
    
    private static final String _HANDLER =
      "com.sun.enterprise.config.serverbeans.ServerValidationHandler";
     private static final String _ROOT_CLASS =
      "com.sun.enterprise.config.serverbeans.Domain";
     
    public String getHandler() {
        return _HANDLER;
    }
       
    public String getRootClass() {
        return _ROOT_CLASS;
    }
    
    public synchronized ConfigBeanInterceptor getConfigBeanInterceptor() {
        ConfigBeanInterceptor cbi = super.getConfigBeanInterceptor();
        if (null != cbi) {
            return cbi;
        }
        cbi = new ServerBeanInterceptor();
        setConfigBeanInterceptor(cbi);
        return cbi;
    }
}
