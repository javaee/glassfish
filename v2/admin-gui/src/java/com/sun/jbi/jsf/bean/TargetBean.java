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
 *  TargetBean.java
 */

package com.sun.jbi.jsf.bean;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

									
/**
 * Provides properties used to populate stand-alone instance and cluster JBI view table
 */
public class TargetBean
{
	 /**
     * Controls printing of diagnostic messages to the log
     */
	private static Logger sLog = JBILogger.getInstance();
    /**
     * default result for queries when no data found
     */
    private final static String DEFAULT_RESULT = "";
    

    public TargetBean()
    {
        mJac = BeanUtilities.getClient();
    }

    /**
     * get the "target" stand-alone instance or cluster name
     * @return the "target" name
     */
    public String getName()
    {
		sLog.fine("TargetBean.getName(), mName=" + mName); 
        return mName;
    }

    public TableDataProvider getSingleTargetComponentsTableData()
    {
        sLog.fine("TargetBean.getSingleTargetComponentsTableData()2"); 

        sLog.fine("TargetBean.getSingleTargetComponentsTableData(): mComponentsList=" + mComponentsList); 

        TableDataProvider result =
	    new ObjectListDataProvider(mComponentsList);

        sLog.fine("TargetBean.getSingleTargetComponentsTableData(): result=" + result); 
        return result;
    }

    public TableDataProvider getSingleTargetDeploymentsTableData()
    {
        sLog.fine("TargetBean.getSingleTargetDeploymentsTableData()"); 

        TableDataProvider result =
	    new ObjectListDataProvider(mDeploymentsList);

        sLog.fine("TargetBean.getSingleTargetDeploymentsTableData(): result=" + result); 
        return result;
    }

    public TableDataProvider getSingleTargetLibrariesTableData()
    {
        sLog.fine("TargetBean.getSingleTargetLibrariesTableData()"); 

        TableDataProvider result =
	    new ObjectListDataProvider(mLibrariesList);

        sLog.fine("TargetBean.getSingleTargetLibrariesTableData(): result=" + result); 
        return result;
    }

    /**
     * get the components list
     * @return a List of zero or more components for this target. 
     */
    public List getComponentsList()
    {
		sLog.fine("TargetBean.getComponentsList(), mComponentsList=" + mComponentsList); 
        return mComponentsList;
    }

    /**
     * get the deployments list
     * @return a List of zero or more deployments for this target. 
     */
    public List getDeploymentsList()
    {
		sLog.fine("TargetBean.getDeploymentsList(), mDeploymentsList=" + mDeploymentsList); 
        return mDeploymentsList;
    }

    /**
     * get the libraries list
     * @return a List of zero or more libraries for this target. 
     */
    public List getLibrariesList()
    {
		sLog.fine("TargetBean.getLibrariesList(), mLibrariesList=" + mLibrariesList);
        return mLibrariesList;
    }

    /**
     * set the "target" stand-alone instance or cluster name
     * @param aName a "target" name
     */
    public void setName(String aName)
    {
	mName = aName;
    }

    /**
     * set the components list
     * @param aComponentsList a List of zero or more components for this target. 
     * An empty list implies installed/deployed to 'domain' only.
     */
    public void setComponentsList(List aComponentsList)
    {
		sLog.fine("TargetBean.setComponentsList(" + aComponentsList + ")"); 
        mComponentsList = aComponentsList;
    }
    /**
     * set the deployments list
     * @param aDeploymentsList a List of zero or more deployments for this target. 
     * An empty list implies installed/deployed to 'domain' only.
     */
    public void setDeploymentsList(List aDeploymentsList)
    {
		sLog.fine("TargetBean.setDeploymentsList(" + aDeploymentsList + ")"); 
        mDeploymentsList = aDeploymentsList;
    }
    /**
     * set the libraries list
     * @param aLibrariesList a List of zero or more libraries for this target. 
     * An empty list implies installed/deployed to 'domain' only.
     */
    public void setLibrariesList(List aLibrariesList)
    {
		sLog.fine("TargetBean.setLibrariesList(" + aLibrariesList + ")"); 
        mLibrariesList = aLibrariesList;
    }

    // private methods

	
    // member variables
    
    /**
     * cached JBI Admin Commands client
     */ 
    private JBIAdminCommands mJac;

    /**
     * The "target" stand-alone instance or cluster name
     */ 
    private String mName;

    /**
     * components for this target
     */ 
    private List mComponentsList = new ArrayList();

    /**
     * deployments for this target
     */ 
    private List mDeploymentsList = new ArrayList();

    /**
     * libraries for this target
     */ 
    private List mLibrariesList = new ArrayList();


}
