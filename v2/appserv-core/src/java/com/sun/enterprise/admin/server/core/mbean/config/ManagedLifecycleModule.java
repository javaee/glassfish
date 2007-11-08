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

package com.sun.enterprise.admin.server.core.mbean.config;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
//JMX imports
import javax.management.Attribute;

//Admin Imports
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.EntityStatus;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.J2EEApplicationException;
import com.sun.enterprise.admin.common.exception.AFException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;


/**
    A class that represents LifecycleModule.
    It extends ConfigMBeanBase class which provides get/set attribute(s) and getMBeanInfo services according to text descriptions.
    ObjectName of this MBean is: 
		ias:type=J2EEApplication, name=<appName>, InstanceName=<instanceName>
*/

public class ManagedLifecycleModule extends ConfigMBeanBase implements ConfigAttributeName.LifecycleModule
{
    private static final String[][] MAPLIST    = 
    {
        {kName            , ATTRIBUTE + ServerTags.NAME},
//ms1        {kEnabled         , ATTRIBUTE + ServerTags.ENABLED},
        {kClassName       , ATTRIBUTE + ServerTags.CLASS_NAME},
        {kClasspath       , ATTRIBUTE + ServerTags.CLASSPATH},
        {kLoadOrder       , ATTRIBUTE + ServerTags.LOAD_ORDER},
        {kIsFailureFatal  , ATTRIBUTE + ServerTags.IS_FAILURE_FATAL},
        {kDescription     , ATTRIBUTE + PSEUDO_ATTR_DESCRIPTION},
    };
    private static final String[]   ATTRIBUTES  =
    {
        kName           + ", String,     R" ,
//ms1        kEnabled        + ", boolean,    RW" ,
        kClassName      + ", String,     RW" ,
        kClasspath      + ", String,     RW" ,
        kLoadOrder      + ", String,     RW" ,
        kIsFailureFatal + ", boolean,    RW" ,
        kDescription    + ", String,     RW" ,
    };
    

    private static final String[]   OPERATIONS  =
    {
        "enable(),      ACTION",
        "disable(),     ACTION"
    };  


    /**
        Default constructor sets MBean description tables
    */
    public ManagedLifecycleModule() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    /**
        Constructs Config MBean for lifecycle module.
        @param instanceName The server instance name.
        @param appName
    */
    public ManagedLifecycleModule(String instanceName, String appName)
        throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kLifecycleModule, new String[]{instanceName, appName});

    }

    /**
     * Disables this module.
     * @throws Exception if there is some error during 
     * disabling.
    */
    public void disable() throws AFException
    {
            return; //FIXME: for RI only
/*	    try{
            this.setAttribute(new Attribute(kEnabled, new Boolean(false)));
            super.getConfigContext().flush();
		}catch(Exception e){
			throw new AFException(e.getMessage());
		}
*/
    }   
    
    /**
     * Enables this module.
     * @throws Exception if there is some error during 
     * enablement.
    */
    public void enable() throws AFException
    {
        return; //FIXME: for RI only
/*	    try{
            this.setAttribute(new Attribute(kEnabled, new Boolean(true)));
            super.getConfigContext().flush();
		}catch(Exception e){
			throw new AFException(e.getMessage());
		}
*/
    }   
    

}
