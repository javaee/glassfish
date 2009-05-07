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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.base.Singleton;


import java.util.Map;

/**
Configuration for the &lt;module-log-levels&gt; element.

Values are those defined in {@link LogLevelValues}.
 */
public interface ModuleLogLevels extends PropertiesAccess, ConfigElement, Singleton
{

    public static final String AMX_TYPE = "module-log-levels";

    public String getNodeAgent();

    public void setNodeAgent(String level);

    public String getUtil();

    public void setUtil(String level);

    public String getSelfManagement();

    public void setSelfManagement(String level);

    public String getSynchronization();

    public void setSynchronization(String level);

    public String getGroupManagementService();

    public void setGroupManagementService(String level);

    public String getManagementEvent();

    public void setManagementEvent(String level);

    public String getAdmin();

    public void setAdmin(String value);

    public String getClaoader();

    public void setClaoader(String value);

    public String getCMPContainer();

    public void setCMPContainer(String value);

    public String getCMP();

    public void setCMP(String value);

    public String getConfiguration();

    public void setConfiguration(String value);

    public String getConnector();

    public void setConnector(String value);

    public String getCORBA();

    public void setCORBA(String value);

    public String getDeployment();

    public void setDeployment(String value);

    public String getEJBContainer();

    public void setEJBContainer(String value);

    public String getJavamail();

    public void setJavamail(String value);

    public String getJAXR();

    public void setJAXR(String value);

    public String getJAXRPC();

    public void setJAXRPC(String value);

    public String getJDO();

    public void setJDO(String value);

    public String getJMS();

    public void setJMS(String value);

    public String getJTA();

    public void setJTA(String value);

    public String getJTS();

    public void setJTS(String value);

    public String getMDBContainer();

    public void setMDBContainer(String value);

    public String getNaming();

    public void setNaming(String value);

    public String getResourceAdapter();

    public void setResourceAdapter(String value);

    public String getRoot();

    public void setRoot(String value);

    public String getSAAJ();

    public void setSAAJ(String value);

    public String getSecurity();

    public void setSecurity(String value);

    public String getServer();

    public void setServer(String value);

    public String getVerifier();

    public void setVerifier(String value);

    public String getWebContainer();

    public void setWebContainer(String value);
}
