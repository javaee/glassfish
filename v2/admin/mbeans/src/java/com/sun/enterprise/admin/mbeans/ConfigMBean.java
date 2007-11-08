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
 * $Id: ConfigMBean.java,v 1.3 2005/12/25 03:42:13 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

import java.lang.IllegalArgumentException;

//JMX imports
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

//event handling
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.DynamicReconfigEvent;

//config imports
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.admin.AdminContext;

// Logging
import java.util.logging.Level;

import com.sun.enterprise.admin.servermgmt.pe.PEInstancesManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;

public class ConfigMBean extends BaseConfigMBean
{
    
     /****************************************************************************************************************
     * Hook for standard setAttributes() to detect change for 
     * dynamic-reconfiguration-enabled attribute Sets the values of several MBean's attributes.
     * @param attrList A list of attributes: The identification of the attributes to be set and the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */
    public AttributeList setAttributes(AttributeList list) {
            
            boolean bEnabled = false;
            int reconfigIdx = -1;
            if(list!=null)
                for(int i=0; i<list.size(); i++)
                {
                    Attribute attr = (Attribute)list.get(i);
                    if(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED.equals(attr.getName()))
                    {
                        try {
                            bEnabled = validateDynamicReconfigEvent(attr.getValue());
                        } catch(Exception e) {
                            reconfigIdx = i;
                        }
                    }
                       
                }

            if (reconfigIdx != -1) {
                list.remove(reconfigIdx);
            }
            //then, call super to perform operation
            list = super.setAttributes(list);

            if (reconfigIdx != -1) {
                return list;
            }

            //now analyse if dynamic-reconfiguration-enabled attribute changed
            if(list!=null)
                for(int i=0; i<list.size(); i++)
                {
                    Attribute attr = (Attribute)list.get(i);
                    if(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED.equals(attr.getName()))
                    {
                        emitDynamicReconfigEvent(bEnabled);
                    }
                       
                }
            return list;
    }

    /**
     * Set the value of a specific attribute of this MBean.
     *
     * @param attr The identification of the attribute to be set
     *  and the new value
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public void setAttribute(Attribute attr)
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        boolean bEnabled = false;
        //first analyse if dynamic-reconfiguration-enabled attribute changed
        if(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED.equals(attr.getName()))
        {
            bEnabled = validateDynamicReconfigEvent(attr.getValue());
        }
        //next, call super to perform operation
        super.setAttribute(attr);
        if(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED.equals(attr.getName()))
        {
            emitDynamicReconfigEvent(bEnabled);
        }
    }
    
    private boolean validateDynamicReconfigEvent(Object value) throws IllegalArgumentException, MBeanException
    {
        boolean bEnabled = false;
        if(value instanceof Boolean)
            bEnabled = ((Boolean)value).booleanValue();
        else
            if("true".equalsIgnoreCase(value.toString()) || 
               "yes".equalsIgnoreCase(value.toString()) )
                bEnabled = true;

        boolean restartRequired = false;
        try {
            restartRequired = getRuntimeStatus().isRestartNeeded();
        } catch(InstanceException ie) {
            throw new MBeanException(ie);
        }

        if((bEnabled == true) && ( restartRequired == true)) {
            String msg = _strMgr.getString(
                "admin.mbeans.configMBean.serverRequiresRestart");
            Exception e = new Exception(msg);
            throw new MBeanException(e,msg);
        }

        return bEnabled;
    }
    private void emitDynamicReconfigEvent(boolean bEnabled)
    {
        try
        {
            AdminContext adminContext = MBeanRegistryFactory.getAdminContext();
            String instanceName = adminContext.getServerName();
            int action = bEnabled?DynamicReconfigEvent.ACTION_ENABLED:DynamicReconfigEvent.ACTION_DISABLED;
            DynamicReconfigEvent event = new DynamicReconfigEvent(instanceName, action);
            String configName = (String)getAttribute(ServerTags.NAME);
            event.setTargetDestination(configName);
            EventContext.addEvent(event);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();

            //throw new MBeanException(e.getMessage(), e);
        }
    }

    private RuntimeStatus getRuntimeStatus() throws InstanceException
    {        
        String serverName = 
            System.getProperty(SystemPropertyConstants.SERVER_NAME);
        PEInstancesManager manager = new PEInstancesManager(new RepositoryConfig());
        return RuntimeStatus.getRuntimeStatus(serverName, manager);        
    }
    ///////////////////////////////////////////////////////////////////////////
	
	private static final	StringManager	_strMgr = 
                StringManager.getManager(ConfigMBean.class);
	///////////////////////////////////////////////////////////////////////////

}
