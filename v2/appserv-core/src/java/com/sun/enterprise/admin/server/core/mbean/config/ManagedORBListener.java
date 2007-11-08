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

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.Ssl;


//Admin imports
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
    This Config MBean represents a ORB Listener.
    It extends ConfigMBeanBase class which provides get/set attribute(s) and getMBeanInfo services according to text descriptions.
    ObjectName of this MBean is:
        ias: type=orblistener, instance-name=<instance-name>, name=<listener-name>
*/
public class ManagedORBListener extends ConfigMBeanBase implements ConfigAttributeName.ORBListener
{
    /**
     * MAPLIST array defines mapping between "external" name and its location in XML relatively base node
     */
    private static final String[][] MAPLIST  =
    {
        {kId                    , ATTRIBUTE + ServerTags.ID},
        {kAddress               , ATTRIBUTE + ServerTags.ADDRESS},
        {kPort                  , ATTRIBUTE + ServerTags.PORT},
        {kEnabled               , ATTRIBUTE + ServerTags.ENABLED},
        //----- SSL -------- Ssl interface attributes appended here
    };
    /** 
     * ATTRIBUTES array specifies attributes descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   ATTRIBUTES  =
    {
        kId                    +", String,  R" ,
        kAddress               +", String,  RW" ,
        kPort                  +", int,     RW" ,
        kEnabled               +", Boolean, RW" ,
        //----- SSL -------- Ssl interface attributes appended here
    };
    /** 
     * OPERATIONS array specifies operations descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   OPERATIONS  =
    {
        "createSsl(String certNickname, Boolean ssl2Enabled, String ssl2Ciphers, Boolean ssl3Enabled, String ssl3TlsCiphers, Boolean tlsEnabled, Boolean tlsRollbackEnabled, Boolean clientAuthEnabled), ACTION",
        "deleteSsl(), ACTION",
        "isSslCreated(), INFO",
    };
    
    
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ManagedORBListener.class );
    
    /**
        Default constructor sets MBean description tables
    */
    public ManagedORBListener() throws MBeanConfigException
    {
        Object[] mergedAttrs = MergeAttributesWithAnotherMbean(
             MAPLIST, ATTRIBUTES, SslBase.MAPLIST, SslBase.ATTRIBUTES,
             ServerTags.SSL , null);
        this.setDescriptions((String[][])mergedAttrs[0], (String[])mergedAttrs[1], OPERATIONS);
    }

    /**
        Constructs Config MBean for ORB Listener.
        @param instanceName The server instance name.
        @param listenerId    Listener id for given resource
    */
    public ManagedORBListener(String instanceName, String listenerId) throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kOrbListenerType, new String[]{instanceName, listenerId});
    }
    
    /**
    This operation checks Ssl existance in current element;
     */
    public boolean isSslCreated() throws ConfigException
    {
        IiopListener listener = (IiopListener)getConfigBeanByXPath( getBasePath() );
        return (listener.getSsl()!=null);
    }

    /**
    This operation deletes Ssl sub-element from current element;
     */
    public void deleteSsl() throws ConfigException
    {
        IiopListener listener = (IiopListener)getConfigBeanByXPath( getBasePath() );
        listener.setSsl(null);
        
        getConfigContext().flush();
    }
    /**
     *    This operation creates Ssl ConfigBean according to attributes and adds(links) it to current config bean;
     *    If attribute is 'null' then default value will be set.
     */
    public void createSsl(String certNickname, Boolean ssl2Enabled, String ssl2Ciphers,
    Boolean ssl3Enabled, String ssl3TlsCiphers,
    Boolean tlsEnabled,  Boolean tlsRollbackEnabled, Boolean clientAuthEnabled) throws ConfigException
    {
        IiopListener listener = (IiopListener)getConfigBeanByXPath( getBasePath() );
        try
        {
            if(listener.getSsl()!=null)
            {
                listener = null;
				String msg = localStrings.getString( "admin.server.core.mbean.config.orblistener_has_ssl_created" );
                throw new ConfigException( msg );
            }
        }
        catch (ConfigException e)
        {
            if(listener==null)
                throw e;
        }
        
        Ssl ssl = new Ssl();
        //strings
        if(certNickname!=null)
            ssl.setCertNickname(certNickname);
        if(ssl2Ciphers!=null)
            ssl.setSsl2Ciphers(ssl2Ciphers);
        if(ssl3TlsCiphers!=null)
            ssl.setSsl3TlsCiphers(ssl3TlsCiphers);
        //Booleans
        if(ssl2Enabled!=null)
            ssl.setSsl2Enabled(ssl2Enabled.booleanValue());
        if(ssl3Enabled!=null)
            ssl.setSsl3Enabled(ssl3Enabled.booleanValue());
        if(tlsEnabled!=null)
            ssl.setTlsEnabled(tlsEnabled.booleanValue());
        if(tlsRollbackEnabled!=null)
            ssl.setTlsRollbackEnabled(tlsRollbackEnabled.booleanValue());
        if(clientAuthEnabled!=null)
            ssl.setClientAuthEnabled(clientAuthEnabled.booleanValue());
        
        listener.setSsl(ssl);
        
        getConfigContext().flush();
    }
    
}
