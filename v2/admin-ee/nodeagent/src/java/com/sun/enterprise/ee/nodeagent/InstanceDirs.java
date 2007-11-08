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
 * InstanceDirs.java
 *
 * Created on February 12, 2007, 10:31 PM
 *
 */

package com.sun.enterprise.ee.nodeagent;

import com.sun.enterprise.util.system.GFSystem;
import java.io.File;
import java.util.logging.*;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This class would be all package-private visibility -- not public.
 * BUT there is a caller that is outside of this package (but still inside the NA
 * family), so it has to be mostly public.
 *
 * What this class does is squirrel away a copy of essential System Properties that
 * are occasionally corrupted in some other unknown code during start-instance calls.
 * That leads to catastrophic results.  So after each such start-instance we reset
 * these Properties back to their virginal pristine state.
 *
 * @author bnevins
 */

public class InstanceDirs
{
    /* 
     * initialize with what WOULD have been the value of 
     * SystemPropertyConstants.INSTANCE_ROOT_PROPERTY
     * for NA.  This method is ONLY called by the NA constructor that is
     * running inside of DAS.
     * I.e. we are running inside DAS right now, and doing a create-instance.
     * We definitely don't want to use DAS' version of the System Properties!
     */
    static synchronized void initialize(RepositoryConfig config)
    {
        initialize
        (
            config.getRepositoryRoot() + "/" + 
            config.getRepositoryName() + "/" +
            config.getInstanceName()
        );
    }
        
    /* 
     * initialize using SystemPropertyConstants.INSTANCE_ROOT_PROPERTY
     * We are running inside the NA process.
     */
    static synchronized void initialize(String instanceRootString)
    {
        // only do this once, and early,  when we have a pristine set of System Properties!
    
        if(!verify(instanceRootString))
        {
            // we must be in create-node-agent 
            nodeAgentInstanceRoot = null;
            repName = null;
            repRoot = null;
        }
        else
        {
            nodeAgentInstanceRoot = instanceRootString;
            final File instanceRoot = new File(nodeAgentInstanceRoot);        
            final File repositoryDir = instanceRoot.getParentFile();
            final File repositoryRoot = repositoryDir.getParentFile();

            repName = repositoryDir.getName();
            repRoot = repositoryRoot.getAbsolutePath().replace('\\', '/');
        }
    }
    
    /*
     * reset the critical System Properties to their saved values
     */
    public static synchronized void resetSysProp()
    {
        // this method is called very rarely -- only after starting an instance.
        // the logic looks odd in order to get the log messages output correctly
        // I.e. without the logging and scrupulous checking, this method would be
        // one line long...
        StringManager sm = BaseNodeAgent._strMgr;
        Logger logger = Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER, "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
        boolean logOK = ( (sm != null) && (logger != null) );
        final String irSysProp = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        
        if(nodeAgentInstanceRoot == null)
        {
            if(logOK)
                logger.log(Level.WARNING, sm.getString("nodeAgent.InstanceDirs.noInstanceRoot"));
            return;
        }
        
        if(irSysProp == null)
        {
            if(logOK)
                logger.log(Level.WARNING, sm.getString("nodeAgent.InstanceDirs.noSysProp", 
                    nodeAgentInstanceRoot));
            
            GFSystem.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, nodeAgentInstanceRoot);
            return;
        }
        
        if(!nodeAgentInstanceRoot.equals(irSysProp))
        {
            if(logOK)
                logger.log(Level.WARNING, sm.getString("nodeAgent.InstanceDirs.funkyInstanceRoot", 
                    irSysProp, nodeAgentInstanceRoot));

            GFSystem.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, nodeAgentInstanceRoot);
            return;
        }

        if(logOK)
            logger.log(Level.INFO, sm.getString("nodeAgent.InstanceDirs.allOK", 
                nodeAgentInstanceRoot));
    }

    public static String getInstanceRoot() {
        return nodeAgentInstanceRoot;
    }
    
    public static String getRepositoryRoot() {
        return repRoot;
    }
    
    public static String getRepositoryName() {
        return repName;
    }
    
    private static boolean verify(String ir)
    {
        try
        {
            File grandParent = new File(ir).getParentFile().getParentFile();
            return grandParent.isDirectory();
        }
        catch(Exception e)
        {
            return false;
        }
    }    
    
    private static String nodeAgentInstanceRoot;
    private static String repName;
    private static String repRoot;
}
