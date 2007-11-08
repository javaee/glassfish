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
 * $Id: IConfigsMBean.java,v 1.5 2005/12/25 03:42:09 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeanapi;

import java.util.Properties;


import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanException;

import com.sun.enterprise.admin.util.InvalidJvmOptionException;


public interface IConfigsMBean {
    
    public ObjectName createHttpListener(AttributeList   attrList,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    
    public boolean deleteHttpListener(String listenerId, String targetName)
        throws MBeanException;
    
    public ObjectName[] listHttpListeners(String targetName)
        throws MBeanException;
    
    public ObjectName createIiopListener(AttributeList   attrList,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    public boolean deleteIiopListener(String listenerId, String targetName)
        throws MBeanException;
    
    public ObjectName[] listIiopListeners(String targetName)
        throws MBeanException;
    
    public ObjectName createSsl(AttributeList   al,
        String          id,
        String          type,
        String          targetName)
        throws Exception;
    
    public boolean deleteSsl(String id, String type, String targetName)
        throws MBeanException;
    
    public ObjectName createVirtualServer(AttributeList al,
        Properties props,
        String targetName)
        throws MBeanException;

    public boolean deleteVirtualServer(String id, String targetName)
        throws MBeanException;
    
    public ObjectName[] listVirtualServers(String targetName)
        throws MBeanException;
    
    
    public ObjectName createAuthRealm(AttributeList attrs,
        Properties    props,
        String        targetName)
        throws Exception;
    
    public boolean deleteAuthRealm(String name, String targetName)
        throws MBeanException;
    
    public ObjectName[] listAuthRealms(String targetName)
        throws MBeanException;
    
    public void addUser(String      user,
        String      password,
        String[]    grps,
        String      realmName,
        String      targetName)
        throws MBeanException;
    
    public void updateUser(String      user,
        String      password,
        String[]    grps,
        String      realmName,
        String      targetName)
        throws MBeanException;
    
    public void removeUser(String      user,
        String      realmName,
        String      targetName)
        throws MBeanException;
    
    public String[] getUserNames(String realmName, String targetName)
        throws MBeanException;
    
    public String[] getGroupNames(String user,
        String realmName,
        String targetName)
        throws MBeanException;
        
    public ObjectName createProfiler(AttributeList  al,
        Properties     props,
        String         targetName)
        throws MBeanException;
    
    public boolean deleteProfiler(String targetName) 
        throws MBeanException;
    
    public String[] getJvmOptions(boolean isProfiler, String targetName)
        throws MBeanException;
    
    public String[] createJvmOptions(String[]   options,
        boolean    isProfiler,
        String     targetName)
        throws MBeanException, InvalidJvmOptionException;

    
    public String[] deleteJvmOptions(String[]   options,
        boolean    isProfiler,
        String     targetName)
        throws MBeanException, InvalidJvmOptionException;
    
    public ObjectName createAuditModule(AttributeList   attrs,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    public ObjectName[] listAuditModules(String targetName)
        throws MBeanException;
    
    public boolean deleteAuditModule(String name, String targetName)
        throws MBeanException;
    
    public ObjectName createJmsHost(AttributeList   attrs,
        Properties      props,
        String          targetName)
        throws MBeanException;

    public boolean deleteJmsHost(String name, String targetName)
        throws MBeanException;
    
    public ObjectName[] listJmsHosts(String targetName) 
        throws MBeanException;
    
    public ObjectName createJaccProvider(AttributeList   attrs,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    public boolean deleteJaccProvider(String name, String targetName)
        throws MBeanException;
    
    public ObjectName[] listJaccProviders(String targetName)
        throws MBeanException;
    
    public ObjectName createThreadPool(AttributeList   attrs,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    public boolean deleteThreadPool(String threadPoolId, String targetName)
        throws MBeanException;
    
    public ObjectName[] listThreadPools(String targetName)
        throws MBeanException;
    
    public ObjectName createEjbTimerService(AttributeList   al,
        Properties      props,
        String          targetName)
        throws MBeanException;
    
    public boolean deleteEjbTimerService(String targetName)
        throws MBeanException;
    
    public ObjectName createSessionProperties(AttributeList   al,
        Properties      props,
        String          targetName)
        throws MBeanException;

    public boolean deleteSessionProperties(String targetName)
        throws MBeanException;    
    
    public ObjectName createManagerProperties(AttributeList   al,
        Properties      props,
        String          targetName)
        throws MBeanException;

    public boolean deleteManagerProperties(String targetName)
        throws MBeanException;
    
    public ObjectName createStoreProperties(AttributeList   al,
            Properties      props,
            String          targetName)
            throws MBeanException;
    
    
    public boolean deleteStoreProperties(String targetName)
        throws MBeanException;
    
    public boolean deleteSessionConfig(String targetName)
        throws MBeanException;
    
    public ObjectName getHttpService(String targetName) 
        throws MBeanException;
    
    public ObjectName getIiopService(String targetName) 
        throws MBeanException;
    
    public ObjectName getEjbContainer(String targetName) 
        throws MBeanException;
    
    
    public ObjectName getWebContainer(String targetName) 
        throws MBeanException;
    
    public ObjectName getMdbContainer(String targetName) 
        throws MBeanException;
    
    public ObjectName getJmsService(String targetName) 
        throws MBeanException;
    
    public ObjectName getLogService(String targetName) 
        throws MBeanException;
    
    public ObjectName getSecurityService(String targetName) 
        throws MBeanException;
    
    public ObjectName getTransactionService(String targetName)
        throws MBeanException;
    
    public ObjectName getMonitoringService(String targetName)
        throws MBeanException;
    
    public ObjectName getJavaConfig(String targetName) 
        throws MBeanException;
    
    public ObjectName getHttpListener(String id, String targetName)
    throws MBeanException;
    
    public ObjectName getVirtualServer(String id, String targetName)
        throws MBeanException;
    
    public ObjectName getIiopListener(String id, String targetName)
        throws MBeanException;
    
    public ObjectName getOrb(String targetName) 
        throws MBeanException;
    
    public ObjectName getJmsHost(String name, String targetName)
        throws MBeanException;
    
    public ObjectName getAuthRealm(String name, String targetName)
        throws MBeanException;
    
    public ObjectName getAuditModule(String name, String targetName)
        throws MBeanException;
    
    public ObjectName getJaccProvider(String name, String targetName)
        throws MBeanException;
    
    public ObjectName getModuleLogLevels(String targetName)
        throws MBeanException;
    
    public ObjectName getModuleMonitoringLevels(String targetName)
        throws MBeanException;
    
    public ObjectName getThreadPool(String threadPoolId, String targetName)
        throws MBeanException;
    
    public ObjectName getEjbTimerService(String targetName)
        throws MBeanException;
    
    public ObjectName getProfiler(String targetName) 
        throws MBeanException;
    
    public ObjectName getSsl(String type, String id, String targetName)
        throws MBeanException;
    
    public ObjectName getSessionProperties(String targetName)
        throws MBeanException;
    
    public ObjectName getManagerProperties(String targetName)
        throws MBeanException;
    
    public ObjectName getStoreProperties(String targetName)
        throws MBeanException;
    
    public ObjectName getChild(String      type,
        String[]    location,
        String      targetName) throws MBeanException;
}
