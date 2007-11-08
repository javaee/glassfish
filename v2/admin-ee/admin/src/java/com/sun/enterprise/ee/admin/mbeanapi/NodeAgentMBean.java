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

package com.sun.enterprise.ee.admin.mbeanapi;

import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

import javax.management.MBeanException;
import javax.management.AttributeList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public interface NodeAgentMBean  {    
    public void startInstance(String instanceName) throws AgentException, MBeanException;
    
    public void stopInstance(String instanceName) throws AgentException, MBeanException;

    public void stopInstance(String instanceName, int timeout) 
    throws AgentException, MBeanException;
    
    public void synchronizeWithDAS() throws AgentException, MBeanException;
    
    public RuntimeStatus getRuntimeStatus() throws AgentException, MBeanException;
    
    public void clearRuntimeStatus() throws MBeanException;       

    public String getLogFilesDirectory(String componentName) throws AgentException, MBeanException;    
    
    public AttributeList getLogRecordsUsingQuery(String componentName, String logFile, Long fromRecord, Boolean next, Boolean forward,
        Integer requestedCount, Date fromDate, Date toDate, String logLevel, Boolean onlyLevel, List listOfModules,
        Properties nameValueMap) throws AgentException, MBeanException;
    
    
    public void rotateLog() throws MBeanException;

    public void dasHasMoved(String newHost, String newPort) 
    throws AgentException, MBeanException;
}
