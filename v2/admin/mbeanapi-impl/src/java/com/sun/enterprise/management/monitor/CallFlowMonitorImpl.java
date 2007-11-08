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

package com.sun.enterprise.management.monitor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.monitor.CallFlowMonitor;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.Switch;
/**
    @see CallFlowMonitor
*/
public final class CallFlowMonitorImpl extends MonitoringImplBase
	//implements CallFlowMonitor
{
    private boolean             mCallFlowEnabled;
    
	public CallFlowMonitorImpl()
	{
	    super( XTypes.CALL_FLOW_MONITOR );
	    mCallFlowEnabled    = false;        
            
	}
		      
    public boolean getEnabled() {
        return mCallFlowEnabled;
    }

    public void setEnabled(final boolean enabled) {
        final Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        callflowAgent.setEnable(enabled);
        mCallFlowEnabled = callflowAgent.isEnabled ();
    }
    

    public void clearData (){
        final Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();        
        callflowAgent.clearData();
    }
    
    public boolean deleteRequestIDs (String[] requestId){
        final Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();        
        return callflowAgent.deleteRequestIds(requestId);
    }
    public List<Map<String, String>> queryRequestInformation (){
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();        
        return callflowAgent.getRequestInformation ();
    }

    public List<Map<String, String>> queryCallStackForRequest(String rid){
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent(); 
        return callflowAgent.getCallStackForRequest (rid);
    }
    

    public Map<String, String> queryPieInformation (String rid){
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        return callflowAgent.getPieInformation(rid);        
    }

    public String getCallerIPFilter (){
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        return callflowAgent.getCallerIPFilter();
    }
    
    public void setCallerIPFilter(String filter) {
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        callflowAgent.setCallerIPFilter(filter);        
    }
    public String getCallerPrincipalFilter() {
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        return callflowAgent.getCallerPrincipalFilter();
    }
    
    public void setCallerPrincipalFilter(String filter) {
        Agent callflowAgent = Switch.getSwitch().getCallFlowAgent();
        callflowAgent.setCallerPrincipalFilter(filter);
    }
    
    public String[] queryRequestTypeKeys (){
        String[] requestType = 
            {
            CallFlowMonitor.REMOTE_ASYNC_MESSAGE, 
            CallFlowMonitor.REMOTE_EJB,
            CallFlowMonitor.REMOTE_WEB,
            CallFlowMonitor.REMOTE_WEB_SERVICE,
            CallFlowMonitor.TIMER_EJB
            };
            
            return requestType;
    }
    public String[] queryComponentTypeKeys (){
        String[] componentType = 
        {
            CallFlowMonitor.BEAN_MANAGED_PERSISTENCE,
            CallFlowMonitor.CONTAINER_MANAGED_PERSISTENCE,
            CallFlowMonitor.MESSAGE_DRIVEN_BEAN,
            CallFlowMonitor.SERVLET,
            CallFlowMonitor.SERVLET_FILTER,
            CallFlowMonitor.STATEFUL_SESSION_BEAN,
            CallFlowMonitor.STATELESS_SESSION_BEAN
        };
        return componentType;
    }
    public String[] queryContainerTypeOrApplicationTypeKeys (){
        String[] containerType = 
        {
            CallFlowMonitor.WEB_CONTAINER,
            CallFlowMonitor.EJB_CONTAINER,
            CallFlowMonitor.ORB,
            CallFlowMonitor.WEB_APPLICATION,
            CallFlowMonitor.EJB_APPLICATION,     
            CallFlowMonitor.OTHER        
        };
        return containerType;
    }
}

