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

package com.sun.enterprise.ee.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;

import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

import java.io.File;

public class EEFileLayout extends PEFileLayout
{       
    public static final String EE_TEMPLATES_SUBDIR = "ee";   
        
    public EEFileLayout(RepositoryConfig config) {
        super(config);        
    }    
       
    protected String getTemplatesSubDir() {
        return EE_TEMPLATES_SUBDIR;
    }
    
    public void createConfigurationDirectories() throws RepositoryException
    {
        createDirectory(getConfigRoot());
        createDirectory(getLibDir()); 
        createDirectory(getExtLibDir());
        createDirectory(getDocRoot());
    }     
   
    
    public File getRepositoryDir()
    {
        if (getConfig().getInstanceName() != null) {
            return new File(super.getRepositoryDir(), getConfig().getInstanceName());
        } else {
            return super.getRepositoryDir();
        }
    }
    
    public File getConfigRoot() 
    {
        if (getConfig().getConfigurationName() != null) {
            return new File(super.getConfigRoot(), getConfig().getConfigurationName());
        } else {
            return super.getConfigRoot();
        }
    }
    
    public File getLibDir()
    {
        if (getConfig().getConfigurationName() != null) {
            return new File(getConfigRoot(), LIB_DIR);
        } else {
            return super.getLibDir();
        }
    }
    
    public File getDocRoot()
    {
        if (getConfig().getConfigurationName() != null) {
            return new File(getConfigRoot(), DOC_ROOT_DIR);
        } else {
            return super.getDocRoot();
        }
    }
    
    public void createNodeAgentDirectories() throws RepositoryException
    {              
        createDirectory(getRepositoryRootDir());
        createDirectory(getRepositoryDir());
        createDirectory(getConfigRoot());        
        createDirectory(getBinDir()); 
        createDirectory(getLogsDir());
    }
    
    public void createServerInstanceDirectories() throws RepositoryException
    {
        createDirectory(getRepositoryRootDir());
        createDirectory(getRepositoryDir());
        createDirectory(getConfigRoot());               
        createDirectory(getAddonRoot());               
        createDirectory(getBinDir());
        createDirectory(getLogsDir());
        createDirectory(getGeneratedDir());
        createDirectory(getJspRootDir());
        createDirectory(getEjbRootDir());
        createDirectory(getApplicationsRootDir());
        createDirectory(getJ2EEAppsDir());
        createDirectory(getJ2EEModulesDir());
        createDirectory(getLifecycleModulesDir());
        createDirectory(getSessionStore());

        // bug 5025253: auto deploy is not supported in remote server instance
        //createDirectory(getAutoDeployDir());
    }
        
    public File getStartAgent()
    {        
        return super.getStartServ();        
    }

    public static final String NSS_CERT_DB = "cert8.db";
    
    public File getNSSCertDBTemplate()
    {
        return new File(getTemplatesDir(), NSS_CERT_DB);
    }
    public File getNSSCertDBFile()
    {
        return new File(getConfigRoot(), NSS_CERT_DB);
    }
        
    public static final String NSS_KEY_DB = "key3.db";
    
    public File getNSSKeyDBTemplate()
    {
        return new File(getTemplatesDir(), NSS_KEY_DB);
    }
    public File getNSSKeyDBFile()
    {
        return new File(getConfigRoot(), NSS_KEY_DB);
    }
       
    
    public static final String START_AGENT_TEMPLATE_UNIX = "startagent.template";
    public static final String START_AGENT_TEMPLATE_WIN  = "startagent.bat.template";
    public static final String START_AGENT_TEMPLATE_OS = isWindows() ? START_AGENT_TEMPLATE_WIN :
        START_AGENT_TEMPLATE_UNIX;
       
    public File getStartAgentTemplate()
    {
        return new File(getTemplatesDir(), START_AGENT_TEMPLATE_OS);
    } 
    
    public File getStopAgent()
    {        
        return super.getStopServ();
    }

    public static final String STOP_AGENT_TEMPLATE_UNIX = "stopagent.template";
    public static final String STOP_AGENT_TEMPLATE_WIN  = "stopagent.bat.template";
    public static final String STOP_AGENT_TEMPLATE_OS = isWindows() ? STOP_AGENT_TEMPLATE_WIN :
        STOP_AGENT_TEMPLATE_UNIX;

    public File getStopAgentTemplate()
    {
        return new File(getTemplatesDir(), STOP_AGENT_TEMPLATE_OS);
    }
  
    public File getKillServ()
    {
        return new File(getBinDir(), KILL_SERV_OS);
    }

    public File getKillServTemplate()
    {
        return new File(getTemplatesDir(), KILL_SERV_OS);
    }

    public File getStartInstance()
    {        
        return super.getStartServ();        
    }

    public static final String START_INSTANCE_TEMPLATE_UNIX = "startinstance.tomcat.template";
    public static final String START_INSTANCE_TEMPLATE_WIN  = "startinstance.tomcat.bat.template";
    public static final String START_INSTANCE_TEMPLATE_OS = isWindows() ? START_INSTANCE_TEMPLATE_WIN :
        START_INSTANCE_TEMPLATE_UNIX;
    
    public File getStartInstanceTemplate()
    {        
        return new File(getTemplatesDir(), START_INSTANCE_TEMPLATE_OS);
    }
    
    public File getStopInstance()
    {
        return super.getStopServ();        
    }
    
    public File getStopInstanceTemplate() 
    {
        return super.getStopServTemplate();
    }    
   

 
}
