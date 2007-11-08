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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/oldconfig/OldConfigsMBean.java,v 1.2 2005/12/25 03:40:58 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:40:58 $
 */

package com.sun.enterprise.management.support.oldconfig;

/**
	Generated: Wed Apr 14 23:24:42 PDT 2004
	Generated from:
	com.sun.appserv:type=configs,category=config
*/

import java.util.Properties;
import javax.management.ObjectName;
import javax.management.AttributeList;

/**
	Implementing class was: com.sun.enterprise.admin.mbeans.ConfigsMBean
*/

public interface OldConfigsMBean 
{

// -------------------- Operations --------------------
        public void     addUser( final String param1, final String param2, final String[] param3, final String param4, final String param5 );
        public ObjectName       copyConfiguration( final String param1, final String param2, final Properties param3 );
        public ObjectName       createAuditModule( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createAuthRealm( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createConfig( final AttributeList attribute_list );
        public ObjectName       createEjbTimerService( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createHttpListener( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createIiopListener( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJaccProvider( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJmsHost( final AttributeList param1, final Properties param2, final String param3 );
        public String[] createJvmOptions( final String[] param1, final boolean param2, final String param3 );
        public ObjectName       createManagerProperties( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createMessageSecurityProvider( final String param1, final String param2, final String param3, final String param4, final String param5, final String param6, final String param7, final String param8, final boolean param9, final Properties param10, final String param11 );
        public ObjectName       createProfiler( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createSessionProperties( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createSsl( final AttributeList param1, final String param2, final String param3, final String param4 );
        public ObjectName       createStoreProperties( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createThreadPool( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createVirtualServer( final AttributeList param1, final Properties param2, final String param3 );
        public boolean  deleteAuditModule( final String param1, final String param2 );
        public boolean  deleteAuthRealm( final String param1, final String param2 );
        public void     deleteConfiguration( final String param1 );
        public boolean  deleteEjbTimerService( final String param1 );
        public boolean  deleteHttpListener( final String param1, final String param2 );
        public boolean  deleteIiopListener( final String param1, final String param2 );
        public boolean  deleteJaccProvider( final String param1, final String param2 );
        public boolean  deleteJmsHost( final String param1, final String param2 );
        public String[] deleteJvmOptions( final String[] param1, final boolean param2, final String param3 );
        public boolean  deleteManagerProperties( final String param1 );
        public boolean  deleteMessageSecurityProvider( final String param1, final String param2, final String param3 );
        public boolean  deleteProfiler( final String param1 );
        public boolean  deleteSessionConfig( final String param1 );
        public boolean  deleteSessionProperties( final String param1 );
        public boolean  deleteSsl( final String param1, final String param2, final String param3 );
        public boolean  deleteStoreProperties( final String param1 );
        public boolean  deleteThreadPool( final String param1, final String param2 );
        public boolean  deleteVirtualServer( final String param1, final String param2 );
        public boolean  destroyConfigElement();
        public ObjectName       getAuditModule( final String param1, final String param2 );
        public ObjectName       getAuthRealm( final String param1, final String param2 );
        public ObjectName       getChild( final String param1, final String[] param2, final String param3 );
        public javax.management.ObjectName[]    getConfig();
        public ObjectName       getConfigByName( final String key );
        public ObjectName       getEjbContainer( final String param1 );
        public ObjectName       getEjbTimerService( final String param1 );
        public String[] getGroupNames( final String param1, final String param2, final String param3 );
        public ObjectName       getHttpListener( final String param1, final String param2 );
        public ObjectName       getHttpService( final String param1 );
        public ObjectName       getIiopListener( final String param1, final String param2 );
        public ObjectName       getIiopService( final String param1 );
        public ObjectName       getJaccProvider( final String param1, final String param2 );
        public ObjectName       getJavaConfig( final String param1 );
        public ObjectName       getJmsHost( final String param1, final String param2 );
        public ObjectName       getJmsService( final String param1 );
        public String[] getJvmOptions( final boolean param1, final String param2 );
        public ObjectName       getLogService( final String param1 );
        public ObjectName       getManagerProperties( final String param1 );
        public ObjectName       getMdbContainer( final String param1 );
        public ObjectName       getModuleLogLevels( final String param1 );
        public ObjectName       getModuleMonitoringLevels( final String param1 );
        public ObjectName       getMonitoringService( final String param1 );
        public ObjectName       getOrb( final String param1 );
        public ObjectName       getProfiler( final String param1 );
        public ObjectName       getSecurityService( final String param1 );
        public ObjectName       getSessionProperties( final String param1 );
        public ObjectName       getSsl( final String param1, final String param2, final String param3 );
        public ObjectName       getStoreProperties( final String param1 );
        public ObjectName       getThreadPool( final String param1, final String param2 );
        public ObjectName       getTransactionService( final String param1 );
        public String[] getUserNames( final String param1, final String param2 );
        public ObjectName       getVirtualServer( final String param1, final String param2 );
        public ObjectName       getWebContainer( final String param1 );
        public javax.management.ObjectName[]    listAuditModules( final String param1 );
        public javax.management.ObjectName[]    listAuthRealms( final String param1 );
        public javax.management.ObjectName[]    listConfigurations( final String param1 );
        public javax.management.ObjectName[]    listConfigurations( final String param1, final boolean param2 );
        public javax.management.ObjectName[]    listHttpListeners( final String param1 );
        public javax.management.ObjectName[]    listIiopListeners( final String param1 );
        public javax.management.ObjectName[]    listJaccProviders( final String param1 );
        public javax.management.ObjectName[]    listJmsHosts( final String param1 );
        public javax.management.ObjectName[]    listMessageSecurityProviders( final String param1, final String param2 );
        public javax.management.ObjectName[]    listThreadPools( final String param1 );
        public javax.management.ObjectName[]    listVirtualServers( final String param1 );
        public void     removeConfigByName( final String key );
        public void     removeUser( final String param1, final String param2, final String param3 );
        public void     updateUser( final String param1, final String param2, final String[] param3, final String param4, final String param5 );
}