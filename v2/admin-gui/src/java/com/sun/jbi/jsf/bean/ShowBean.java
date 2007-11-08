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
 *  ShowBean.java
 */

package com.sun.jbi.jsf.bean;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;


/**
 * Provides properties used to populate JBI Show view properties and
metadata
 */
public class ShowBean
{
   
    //Get logger to log fine, info level messages in server.log file
    private Logger sLog = JBILogger.getInstance();

    /**
     * default result for queries when no data found
     */
    private final static String DEFAULT_RESULT = "";


    public ShowBean()
    {
        mJac = BeanUtilities.getClient();
    }

    /**
     *
     * @return String - status (or null if not installed/deployed)
     */
    public String check(String aTarget)
    {
        final String NO_STATE_CHECK = null;
        final String NO_LIBRARY_CHECK = null;
        final String NO_COMPONENT_CHECK = null;
        final String NO_DEPLOYMENT_CHECK = null;
        
        sLog.fine("ShowBean.check(" + aTarget + ")");
       
        String result = null; // "not found"

        String queryResult = null;

        boolean isServiceAssembly = false;
        boolean isSharedLibrary = false;

        if (null != mJac)
        {
            try
            {

                if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(mType))
                {
                   queryResult = 
                    mJac.showBindingComponent(mName, 
                                              NO_STATE_CHECK,
                                              NO_LIBRARY_CHECK,
                                              NO_DEPLOYMENT_CHECK,
                                              aTarget);
                   }
                else if (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(mType))
                {
                    queryResult = 
                    mJac.showServiceEngine(mName, 
                                           NO_STATE_CHECK,
                                           NO_LIBRARY_CHECK,
                                           NO_DEPLOYMENT_CHECK,
                                           aTarget);
                   
                }
                else if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(mType))
                {
                    queryResult = 
                    mJac.showServiceAssembly(mName, 
                                             NO_STATE_CHECK,
                                             NO_COMPONENT_CHECK,
                                             aTarget);
                    isServiceAssembly = true;
                }
                else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(mType))
                {
                    queryResult = 
                    mJac.showSharedLibrary(mName, 
                                           NO_COMPONENT_CHECK,
                                           aTarget);
                    isSharedLibrary = true;
                }
                else
                {
                    sLog.fine("ShowBean.check--unrecognized mType=" + mType);
                }

                if (null != queryResult)
                {
                    sLog.fine("ShowBean.check(" + aTarget + "), queryResult=" + queryResult); 
                    List list = null;
                    if (isServiceAssembly)
                    {
                        list =
                        ServiceAssemblyInfo.readFromXmlTextWithProlog(queryResult);
                        if (1 == list.size())
                        {
                            ServiceAssemblyInfo saInfo
                            = (ServiceAssemblyInfo) list.get(0);
                            String state = saInfo.getState();

                            if (SharedConstants.STATE_STARTED.equals(state))
                            {
                                result
                                =I18nUtilities.getResourceString("jbi.operations.comp.started");
                            }
                            else
                            {
                                result = "Disabled(" + state + ")";
                            }
                        }
                    }
                    else
                    {
                        list = JBIComponentInfo.readFromXmlText(queryResult);

                        if (1 == list.size())
                        {
                            JBIComponentInfo compInfo =
                            (JBIComponentInfo) list.get(0);

                            if (isSharedLibrary) // has no lifecycle state
                            {
                                result = I18nUtilities.getResourceString ("jbi.operations.start.enabled");
                            }
                            else // BC or SE (has lifecycle state)
                            {
                                String state = compInfo.getState();

                                if (SharedConstants.STATE_STARTED.equals(state))
                                {
                                    result =
                                    I18nUtilities.getResourceString("jbi.operations.comp.started");
                                }
                                else if (SharedConstants.STATE_STOPPED.equals(state))
                                {
                                    result =
                                    I18nUtilities.getResourceString("jbi.operations.comp.stopped");
                                }
                                else if (SharedConstants.STATE_SHUT_DOWN.equals(state))
                                {
                                    result =
                                    I18nUtilities.getResourceString("jbi.operations.comp.shutdown");
                                }
                                else
                                {
                                    result =
                                    I18nUtilities.getResourceString("jbi.operations.comp.state.unknown");
                                }
                            }
                        }
                    }
                }
            }
            catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {

            }
        }
        sLog.fine("check(" + aTarget + 
                           "), mName=" + mName + 
                           ", mType=" + mType + 
                           ", queryResult=" + queryResult +
                           ", result = " + result);
        return result;
    }


    // getters

    /**
     * get contents of /META-INF/jbi.xml for this "component"
     * @return the JBI deployment descriptor in a (validated) XML String
     */
    public String getDeploymentOrInstallationDescriptor()
    {
        String result = DEFAULT_RESULT;

        result = 
        queryDeploymentOrInstallationDescriptor();

        sLog.fine("ShowBean.getDeploymentOrInstallationDescriptor(),result=" + result);
        return result;
    }

    /**
     * get the "component" description
     * @return the JBI "component" description
     */
    public String getDescription()
    {
        String result = DEFAULT_RESULT;

        result = queryDescription();

        sLog.fine("ShowBean.getDescription(), result=" + result);
        return result;
    }

    /**
     * get the "component" name
     * @return the JBI "component" name
     */
    public String getName()
    {
        sLog.fine("ShowBean.getName(), mName=" + mName);
        return mName;
    }

    public TableDataProvider getSharedTargetsTableData()
    {
        sLog.fine("ShowBean.getSharedTargetsTableData()"); 
        TableDataProvider result =
        new ObjectListDataProvider(mTargetsList);

        sLog.fine("ShowBean.getSharedTargetsTableData(): result=" + result);
        return result;
    }

    /**
     * get the targets list
     * @return a List of zero or more targets for this component/deployment. 
     * An empty list implies installed/deployed to 'domain' only.
     */
    public List getTargetsList()
    {
        sLog.fine("ShowBean.getTargetsList(), mTargetsList=" + mTargetsList); 
        return mTargetsList;
    }

    /**
     * get the "component" type
     * @return the JBI "component" type (one of: <code>binding-component,
service-assembly, service-engine, shared-library</code>)
     */
    public String getType()
    {
        sLog.fine("ShowBean.getType(), mType=" + mType);
        return mType;
    }


    /**
     * initialize the member varible target name list, which is dynamic and
also
     * initialize the original target name list, which is static.
     */
    public void initTargetNames ()
    {
        List targets = getTargetsList();
        mTargetNames = new String[targets.size()];
        mOriginalTargetNames = new String[targets.size()];
        for (int i=0; i<targets.size(); i++)
        {
            SelectableTargetInfo tgtInfo =
            (SelectableTargetInfo)targets.get(i);
            mTargetNames[i] = tgtInfo.getName();
            mOriginalTargetNames[i] = tgtInfo.getName();
        }
    }


    /**
     * get the target names
     * @return an Array of zero or more target names for this
component/deployment. 
     */
    public String[] getTargetNames ()
    {
        return mTargetNames;
    }


    /**
     * get the target names
     * @return a Array of zero or more target names for this
component/deployment. 
     */
    public String[] getOriginalTargetNames ()
    {
        return mOriginalTargetNames;
    }

    // setters

    /**
     *
     */
    public void setDeploymentOrInstallationDescriptor(String ignored)
    {
        throw new
        RuntimeException("ShowBean.setDesploymnetOrInstallationDescriptor(String)not supported (read-only property)");
    }

    /**
     * set the "component" description
     * @param aDescription a JBI "component" description
     */
    public void setDescription(String ignored)
    {
        throw new RuntimeException("ShowBean.setDescription(String) not supported (read-only property)");
    }

    /**
     * set the "component" name
     * @param aName a JBI "component" name
     */
    public void setName(String aName)
    {
        mName = aName;
    }

    /**
     * set the targets list
     * @param aTargetsList a List of zero or more targets for this
component/deployment. 
     * An empty list implies installed/deployed to 'domain' only.
     */
    public void setTargetsList(List aTargetsList)
    {
        sLog.fine("ShowBean.setTargetsList(" + aTargetsList + ")");
        mTargetsList = aTargetsList;
    }

    /**
     * set the "component" type
     * @param aType a "component" type (one of: binding-component,
service-assembly, service-engine, shared-library)
     */
    public void setType(String aType)
    {
        mType = aType;
    }


    public void setTargetNames (String[] aTargetNames)
    {
        mTargetNames = aTargetNames;
    }


    // private methods

    /**
     * helper to initialize the "component" deployment/installation
metadata 
     * @return the contents of the /META-INF/jbi.xml in a validated XML
String
     */
    private String queryDeploymentOrInstallationDescriptor()
    {
        String result = DEFAULT_RESULT;
        String descriptor = DEFAULT_RESULT;
        try
        {

            JBIAdminCommands client = BeanUtilities.getClient();

            if ( null!= client )
            {

                sLog.fine("ShowBean.queryDeploymentOrInstallationDescriptor(), client=" + 
                                       client + ", mType=" + mType + ", mName=" + mName); 
                if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(mType) || 
                    JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(mType))
                {
                    descriptor = 
                    client.getComponentInstallationDescriptor(mName);
                }
                else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(mType))
                {
                    descriptor = 
                    client.getSharedLibraryInstallationDescriptor(mName);
                }
                else if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(mType))
                {
                    descriptor =
                    client.getServiceAssemblyDeploymentDescriptor(mName);
                }
                result = descriptor;
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("ShowBeanqueryDeploymentOrInstallationDescriptor(), caught jbiRemoteEx=" +
                               jbiRemoteEx);
            jbiRemoteEx.printStackTrace();
        }
        sLog.fine("ShowBean.queryDeploymentOrInstallationDescriptor(), result=" + result); 
        return result;
    }

    /**
     * initializes the "component" description 
     * @return the description (or an empty string if not found)
     */
    private String queryDescription()
    {
        String result = DEFAULT_RESULT;

        ListBean listBean = BeanUtilities.getListBean();

        sLog.fine("ShowBean.queryDeploymentOrInstallationDescriptor(), mType=" + 
                               mType); 
        
        if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(mType))
        {
            List<SelectableJBIComponentInfo> bindings =
            listBean.getBindingComponentsInfoList(mFilterTargetComponentState,  
                                                  mFilterTargetComponentStates);
            result = listBean.findDescription(mName, bindings);
        }
        else if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(mType))
        {
            List<SelectableJBIServiceAssemblyInfo> deployments =
            listBean.getServiceAssembliesInfoList(mFilterTargetAssemblyState, 
                                                  mFilterTargetAssemblyStates);
            result = listBean.findDescription(mName, deployments);
        }
        else if (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(mType))
        {
            List<SelectableJBIComponentInfo> engines =
            listBean.getServiceEnginesInfoList(mFilterTargetComponentState, 
                                               mFilterTargetComponentStates);
            result = listBean.findDescription(mName, engines);
        }
        else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(mType))
        {
            List<SelectableJBIComponentInfo> libraries =
            listBean.getSharedLibrariesInfoList();
            result = listBean.findDescription(mName, libraries);
        }

        sLog.fine("ShowBean.queryDescription(), result=" + result); 
        return result;
    }


    public void setFilterTargetComponentState (String filterState)
    {
        mFilterTargetComponentState = filterState;
    }


    public void setFilterTargetComponentState (String filterState, 
                                               String filterStates)
    {
        mFilterTargetComponentState  = filterState;
        filterStates = filterStates.replace('[',' ');
        filterStates = filterStates.replace(']',' ');
        String[] states = filterStates.split("\\,");
        for (int i=0; i<states.length; i++)
        {
            states[i] = states[i].trim();
        }
        mFilterTargetComponentStates = new
                                       ArrayList(Arrays.asList(states));
    }


    public String getFilterTargetComponentState ()
    {
        return mFilterTargetComponentState;
    }


    public ArrayList getFilterTargetComponentStates ()
    {
        return mFilterTargetComponentStates;
    }


    public void setFilterTargetAssemblyState (String filterState)
    {
        mFilterTargetAssemblyState = filterState;
    }


    public void setFilterTargetAssemblyState (String filterState, 
                                              String filterStates)
    {
        mFilterTargetAssemblyState  = filterState;
        filterStates = filterStates.replace('[',' ');
        filterStates = filterStates.replace(']',' ');
        String[] states = filterStates.split("\\,");
        for (int i=0; i<states.length; i++)
        {
            states[i] = states[i].trim();
        }
        mFilterTargetAssemblyStates = new ArrayList(Arrays.asList(states));
    }


    public String getFilterTargetAssemblyState ()
    {
        return mFilterTargetAssemblyState;
    }


    public ArrayList getFilterTargetAssemblyStates ()
    {
        return mFilterTargetAssemblyStates;
    }


    // member variables

    /**
     * cached JBI Admin Commands client
     */ 
    private JBIAdminCommands mJac;

    /**
     * JBI "component" name
     */ 
    private String mName;

    /**
     * JBI "component" type (one of: binding-component, service-assembly,
service-engine, shared-library)
     */ 
    private String mType;

    /**
     * targets for this component (zero or more cluster name Strings and
zero or more Stand-Alone Instance name Strings.
     */ 
    private List mTargetsList = new ArrayList();

    /** 
     * Holds the filter type for the components table 
     */
    private String mFilterTargetComponentState = SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;
    private ArrayList mFilterTargetComponentStates = null;

    /** 
     * Holds the filter type for the components table 
     */
    private String mFilterTargetAssemblyState = SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;
    private ArrayList mFilterTargetAssemblyStates = null;

    private String[] mTargetNames = null;
    private String[] mOriginalTargetNames = null;

}
