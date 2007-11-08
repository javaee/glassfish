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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.RRPersistenceHelper;

import com.sun.enterprise.admin.runtime.BaseRuntimeMBean;

import com.sun.enterprise.server.logging.FileandSyslogHandler;

import java.util.logging.Logger;
import java.util.logging.Level; 

public class ServerRuntimeMBean extends BaseRuntimeMBean 
    implements com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean {
    
    private static final StringManager _strMgr = 
        StringManager.getManager(ServerRuntimeMBean.class);

    private static Logger _logger = null;
    
    public ServerRuntimeMBean() {
        super();
    }    
    
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

    public RuntimeStatus getRuntimeStatus() throws InstanceException
    {        
        String serverName = 
            System.getProperty(SystemPropertyConstants.SERVER_NAME);
        EEInstancesManager manager = new EEInstancesManager(new RepositoryConfig());
        return RuntimeStatus.getRuntimeStatus(serverName, manager);        
    }
    
    public void clearRuntimeStatus() 
    {
        RuntimeStatus.clearRuntimeStatus();
    }        

    public AdminEventResult forwardEvent(AdminEvent e) {

        AdminEventResult result = null;
        result = AdminEventMulticaster.multicastEvent(e);
        return result;
    }
        
    public void setRestartRequired(boolean restartFlag) 
    {
        RRPersistenceHelper helper = new RRPersistenceHelper();
        helper.setRestartRequired(restartFlag);
    }        

}
