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
    PROPRIETARY/CONFIDENTIAL. Use of this product is subject
    to license terms. Copyright (c) 2002 Sun Microsystems, Inc.
        All rights reserved.
 
    $Id: ConfigMBeanNamingInfo.java,v 1.4 2005/12/25 04:14:34 tcfujii Exp $
 */

package com.sun.enterprise.admin.server.core.mbean.config.naming;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.MalformedNameException;

import com.sun.enterprise.admin.server.core.mbean.config.ConfigMBeanBase;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Provides naming support for ConfigMbeans
 */
public class ConfigMBeanNamingInfo
{
    private AdminContext m_AdminContext;
    String[] m_ids = null;
    MBeanNamingDescriptor m_descr = null;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ConfigMBeanNamingInfo.class );

    public ConfigMBeanNamingInfo(String type, String[] params) throws MBeanConfigException
    {
       this(type, params, true);
    }
    public ConfigMBeanNamingInfo(String type, String[] params, boolean bTestParamSize) throws MBeanConfigException
    {
        m_descr = ConfigMBeansNaming.findNamingDescriptorByType(type);
        if(m_descr==null) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_type", type );
            throw new MBeanConfigException( msg );
		}

        if(bTestParamSize)
        {
            int parmSize = (params==null)?0:params.length;
            if(m_descr.getParmListSize()!=parmSize) {
				String msg = localStrings.getString( "admin.server.core.mbean.config.naming.wrong_parameters_array_size", type );
                throw new MBeanConfigException( msg );
			}
        }
        m_ids   = params;
    }
    
    public ConfigMBeanNamingInfo(String dottedName) throws MBeanConfigException, MalformedNameException
    {
        m_descr = ConfigMBeansNaming.findNamingDescriptor(dottedName);
        if(m_descr==null) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_dotted_name", dottedName );
            throw new MBeanConfigException( msg );
		}
        m_ids   = m_descr.extractParmList(dottedName);
    }
    
    public ConfigMBeanNamingInfo(ObjectName objectName) throws MBeanConfigException
    {
        m_descr = ConfigMBeansNaming.findNamingDescriptor(objectName);
        if(m_descr==null) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_object_name", objectName );
            throw new MBeanConfigException( msg );
		}
        m_ids   = m_descr.extractParmList(objectName);
    }

    public void setAdminContext(AdminContext adminContext) {
        m_AdminContext = adminContext;
    }
    
    //******************************M A P P I N G S***************************
    public ObjectName getObjectName() throws MalformedObjectNameException
    {
        return m_descr.createObjectName(m_ids);
    }

    public String[] getLocationParams() 
    {
        return m_ids;
    }

    public String getXPath() 
    {
        return m_descr.createXPath(m_ids);
    }

    public int getMode() 
    {
        return m_descr.getMode();
    }

    public boolean isModeConfig() 
    {
        int mode = m_descr.getMode();
        return ((mode&MBeansDescriptions.MODE_CONFIG)!=0);
    }

    public boolean isModeMonitorable() 
    {
        int mode = m_descr.getMode();
        return ((mode&MBeansDescriptions.MODE_MONITOR)!=0);
    }

    public ConfigMBeanBase constructConfigMBean() throws MBeanConfigException
    {
        String className = MBeansDescriptions.CONFIG_MBEANS_BASE_CLASS_PREFIX + m_descr.getMBeanClassName();
        Class cl;
        ConfigMBeanBase configMBean;
        try
        {
            cl = Class.forName(className);
            //create configMBean by defaul constructor;
            configMBean  = (ConfigMBeanBase)cl.newInstance();
        }
        catch (Exception e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_couldnot_create_mbean_class", className );
            throw new MBeanConfigException( msg );
        }
        configMBean.setAdminContext(m_AdminContext);
        configMBean.initialize(this);
        return configMBean;
    }

    public String getServerInstanceName()  throws MBeanConfigException
    {
        if(m_ids==null || m_ids.length==0)
        {
            String msg = localStrings.getString( "admin.server.core.mbean.config.naming.wrong_parameters_array_size", m_descr.getType() );
            throw new MBeanConfigException( msg );
        }
        return m_ids[0];
    }

}
