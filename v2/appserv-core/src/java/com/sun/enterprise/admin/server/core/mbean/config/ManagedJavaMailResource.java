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
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.ConfigBeansFactory;

//Admin imports
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.StringValidator;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.ObjectNames;

/**
    Represents a JavaMail resource.

    ObjectName of this MBean is:
    ias:instance-name=<instance-name>, name=<jndiName>, component=mail-resource
*/
public class ManagedJavaMailResource extends ConfigMBeanBase implements ConfigAttributeName.MailResource
{
    private static final String[][] MAPLIST    = 
    {
        {kJndiName,                 ATTRIBUTE + ServerTags.JNDI_NAME},
//        {kEnabled,                  ATTRIBUTE + ServerTags.ENABLED}, 
        {kStoreProtocol,            ATTRIBUTE + ServerTags.STORE_PROTOCOL}, 
        {kStoreProtocolClass,       ATTRIBUTE + ServerTags.STORE_PROTOCOL_CLASS}, 
        {kTransportProtocol,        ATTRIBUTE + ServerTags.TRANSPORT_PROTOCOL}, 
        {kTransportProtocolClass,   ATTRIBUTE + ServerTags.TRANSPORT_PROTOCOL_CLASS}, 
        {kHost,                     ATTRIBUTE + ServerTags.HOST}, 
        {kUser,                     ATTRIBUTE + ServerTags.USER}, 
        {kFrom,                     ATTRIBUTE + ServerTags.FROM}, 
        {kDebug,                    ATTRIBUTE + ServerTags.DEBUG}, 
        {kDescription,              ATTRIBUTE + PSEUDO_ATTR_DESCRIPTION}

    };

    private static final String[]   ATTRIBUTES  = 
    {
        kJndiName               + " ,String,  R",
//        kEnabled                + " ,boolean, RW",
        kStoreProtocol          + " ,String, RW",
        kStoreProtocolClass     + " ,String, RW",
        kTransportProtocol      + " ,String, RW",
        kTransportProtocolClass + " ,String, RW",
        kHost                   + " ,String, RW",
        kUser                   + " ,String, RW",
        kFrom                   + " ,String, RW",
        kDebug                  + " ,boolean, RW",
        kDescription            + " ,String,  RW",
    };

    private static final String[]   OPERATIONS  = null;

    /**
        Default constructor sets MBean description tables
    */
    public ManagedJavaMailResource() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    /**
        Constructs Config MBean for java mail Resource.
        @param instanceName The server instance name.
        @param jndiName     JNDI name associated with given resource
    */
    public ManagedJavaMailResource(String instanceName, 
                                   String jndiName)
        throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kMailResourceType, new String[]{instanceName, jndiName});
    }

}
