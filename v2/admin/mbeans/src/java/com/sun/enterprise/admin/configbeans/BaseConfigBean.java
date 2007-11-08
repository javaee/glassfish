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
 * EEBaseConfigMBean.java
 *
 * Created on September 18, 2003, 11:49 AM
 */

package com.sun.enterprise.admin.configbeans;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContextImpl;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.AdminService;

import javax.management.MBeanException;

/**
 *
 * @author  kebbs
 */
public class BaseConfigBean {        
       
    public static final boolean OVERWRITE = BaseConfigMBean.OVERWRITE;
    
    private ConfigContext _configContext;
    
    public BaseConfigBean(ConfigContext configContext)
    {
        _configContext = configContext;
    }       
    
    //FIXTHIS: This needs to be refactored out since it used in a number of places when
    //we make the decision whether this is the correct approach.
    /**
     * Persist config changes to disk. Clear all config changes (so that no notification will be
     * fired) by the ConfigInterceptor. Overlay the runtime config context with the modified
     * admin context.
     */
    public static void flushAll(ConfigContext ctx) throws ConfigException
    {       
        ctx.flush(OVERWRITE);
        ctx.resetConfigChangeList();
        //((ServerContextImpl)ApplicationServer.getServerContext()).setConfigContext(ctx);        
    }
    
    //FIXTHIS: This needs to be refactored out since it used in a number of places when
    //we make the decision whether this is the correct approach.
    protected void flushAll() throws ConfigException
    {
        flushAll(getConfigContext());
    }
    
    //We must subclass this method, because the base case seems to return a null config context if 
    //the mbean does not have a corresponding xpath (or something). The properties MBean seems
    //to have this problem.
    protected ConfigContext getConfigContext()
    {
        return _configContext;
    }
}
