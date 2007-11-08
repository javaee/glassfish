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

package com.sun.enterprise.admin.mbeans;

import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.EjbTimerEvent;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

import java.util.logging.Logger;
import java.util.logging.Level; 

/**
 * object name for this mbean: <domainName>:type=ejb-timer-management,category=config
 * EJBTimerManagementMBean exposes list timers
 *
 * @author sridatta
 *
 */
public class EJBTimerManagementMBean 
        extends BaseConfigMBean 
        implements com.sun.enterprise.admin.mbeanapi.IEJBTimerManagementMBean
{
   
	public EJBTimerManagementMBean()
	{
	    super();
	}	
    
    private Logger getLogger()
    {
        if (_logger == null) {
            _logger = Logger.getLogger(LogDomains.ADMIN_LOGGER);
        }
        return _logger;
    }

    /**
     * Lists ALL the ejb timers  
     *
     * @param target is ignored. It is always assumed to be the 
     * 	             PE instance. can also be null.
     *
     * exception InstanceException 
     *
     * @return returns the list of timers as a string array
     */
    public String[] listTimers(String target) 
			throws ConfigException, InstanceException {
       
	fine("Entering list Timers"); 
	String server = getServerName(); 
        
	fine("List Timers: Sending Notification to server" + server);
	 
        String[] resStr = sendListTimerEvent(server, new String[] {server});
	fine("RESULT: " + resStr);
	if(resStr == null || resStr.length == 0) {
	   resStr = new String[] {"There are no Ejb Timers."};
	} 
	resStr[0] = server + ": " + resStr[0];
	return resStr;
    }
   
     protected String[] sendListTimerEvent(String server, String[] allServers) {
         AdminEvent event = new EjbTimerEvent(server, 
                        EjbTimerEvent.ACTION_LISTTIMERS, 
                        server, 
                        allServers);
         
	 event.setTargetDestination(server); 
         AdminEventResult res = forwardEvent(event);

	return (String[]) res.getAttribute(server,
			EjbTimerEvent.EJB_TIMER_CALL_RESULT_ATTRNAME);
     }
       
     protected AdminEventResult forwardEvent(AdminEvent e) {

        AdminEventResult result = null;
        result = AdminEventMulticaster.multicastEvent(e);
        return result;
    }
     
     private void fine(String s) {
         getLogger().log(Level.INFO, s); //FIXME. change to fine
     }

    
    private String getServerName() {
	return ApplicationServer.getServerContext().getInstanceName();
    } 
	///////////////////////////////////////////////////////////////////////////
	
	private static final	StringManager	_strMgr = 
                StringManager.getManager(EJBTimerManagementMBean.class);
	private static 	        Logger			_logger;
	///////////////////////////////////////////////////////////////////////////
}
