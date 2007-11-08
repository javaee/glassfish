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
import com.sun.enterprise.config.ConfigBean;

//Admin imports
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.ObjectNames;

public class ManagedMdbContainer extends ConfigMBeanBase implements ConfigAttributeName.MdbContainer
{
 private static final String[][] MAPLIST  =  {
   { kMinBeansInPool,                 ATTRIBUTE + ServerTags.STEADY_POOL_SIZE },
   { kBeanIncrementCount,             ATTRIBUTE + ServerTags.POOL_RESIZE_QUANTITY},
   { kMaxPoolSize,                    ATTRIBUTE + ServerTags.MAX_POOL_SIZE },
   { kIdleInPoolTimeoutInSeconds,     ATTRIBUTE + ServerTags.IDLE_TIMEOUT_IN_SECONDS },
//ms1   { kLogLevel,                       ATTRIBUTE + ServerTags.LOG_LEVEL },
   //{ kMonitoringEnabled,              ATTRIBUTE + ServerTags.MONITORING_ENABLED },
 };

 private static final String[]   ATTRIBUTES  = {
   kMinBeansInPool                 + ",  int,    RW",
   kBeanIncrementCount             + ",  int,    RW",
   kMaxPoolSize                    + ",  int,    RW",
   kIdleInPoolTimeoutInSeconds     + ",  int,    RW",
//ms1   kLogLevel                       + ",  String, RW",
   // kMonitoringEnabled              + ",  boolean,RW",
 };

 private static final String[]   OPERATIONS  = null;
 
 private final String     MDB_NODE_PATH ="/server/mdb-container";

    /**
        Default constructor sets MBean description tables
    */
    public ManagedMdbContainer() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    public ManagedMdbContainer(String instanceName) throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kMdbContainer, new String[]{instanceName});
    }

}
 
 
