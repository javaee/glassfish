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
 *  ListBean.java
 */

package com.sun.jbi.jsf.bean;

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.webui.jsf.model.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides properties used to populate JBI List/Show view tables
 */
public class ListBean
{

    private final static boolean IS_CLUSTER_PROFILE = ClusterUtilities.isClusterProfile();
    private final static String  LIST_TARGET = JBIAdminCommands.DOMAIN_TARGET_KEY;
    private final static String  NO_STATE_CHECK = null;
    private final static String  NO_LIBRARY_CHECK = null;
    private final static String  NO_COMPONENT_CHECK = null;
    private final static String  NO_DEPLOYMENT_CHECK = null;
     /**
     * Controls printing of diagnostic messages to the log
     */
    private static Logger sLog = JBILogger.getInstance();

    /**
     *
     */
    public static String findDescription(final String aName,
                                         List aComponentOrServiceAssemblyInfoList)
    {
        String result = "";
        sLog.fine("ListBean.findDescription(" + aName + ", (List)) size="
                               + aComponentOrServiceAssemblyInfoList.size());

        if ((null != aName)
            &&(null != aComponentOrServiceAssemblyInfoList))
        {
            String nextDescription = "";
            String nextName = "";

            for (int i = 0; i < aComponentOrServiceAssemblyInfoList.size(); ++i)
            {
                Object listItem = aComponentOrServiceAssemblyInfoList.get(i);

                sLog.fine("ListBean.findDescription(...), listItem=" + listItem);


                if (listItem instanceof SelectableJBIComponentInfo)
                {
                    SelectableJBIComponentInfo nextCompInfo =
                    (SelectableJBIComponentInfo) listItem;

                    nextDescription = nextCompInfo.getDescription();
                    nextName = nextCompInfo.getName();
                }
                else if (listItem instanceof SelectableJBIServiceAssemblyInfo)
                {
                    SelectableJBIServiceAssemblyInfo nextDepInfo =
                    (SelectableJBIServiceAssemblyInfo) listItem;

                    nextDescription = nextDepInfo.getDescription();
                    nextName = nextDepInfo.getName();
                }

                if (aName.equals(nextName))
                {
                    result = nextDescription;
                    break;
                }
            }
        }

        sLog.fine("ListBean.findDescription(" + aName +
                               ", " + aComponentOrServiceAssemblyInfoList +
                               "), result=" + result);

        return result;
    }


    /**
     *
     */
    List<SelectableJBIComponentInfo> getBindingComponentsInfoList()
    {
       sLog.fine("ListBean.getBindingComponentsInfoList()");
       return getBindingComponentsInfoList(mFilterComponentState,
                                            mFilterComponentStates);
    }


    /**
     *
     */
    List<SelectableJBIComponentInfo> getBindingComponentsInfoList(String aFilterState,
                                                                  ArrayList aFilterStates)
    {
        sLog.fine("ListBean.getBindingComponentsInfoList(" + aFilterState +
                   ", " + aFilterStates + ")");

        ArrayList<SelectableJBIComponentInfo> resultList =
        new ArrayList<SelectableJBIComponentInfo>();

    SelectableJBIComponentInfo[] bcInfoListArray =
        getCompStatus(JBIConstants.JBI_BINDING_COMPONENT_TYPE);

    for (int i = 0; i < bcInfoListArray.length; ++i)
        {
        if (BeanUtilities.checkFilterOptions(bcInfoListArray[i].getSummaryStatus(),
                             aFilterState,
                             aFilterStates))
            {
            sLog.fine("ListBean.getBindingComponentsInfoList() adding " + bcInfoListArray[i]);
            resultList.add(bcInfoListArray[i]);
            }
        }

        return resultList;
    }

    List<SelectableJBIServiceAssemblyInfo> getServiceAssembliesInfoList()
    {
        sLog.fine("ListBean.getServiceAssembliesInfoList()");
        return getServiceAssembliesInfoList(mFilterAssemblyState,
                                            mFilterAssemblyStates);

    }


    List<SelectableJBIServiceAssemblyInfo> getServiceAssembliesInfoList(String aFilterState,
                                                                        ArrayList aFilterStates)
    {
        sLog.fine("ListBean.getServiceAssmembliesInfoList(" + aFilterState +
                   ", " + aFilterStates + ")");
       ArrayList<SelectableJBIServiceAssemblyInfo> resultList =
        new ArrayList<SelectableJBIServiceAssemblyInfo>();

    SelectableJBIServiceAssemblyInfo[] saInfoListArray =
        getSaStatus();

    for (int i = 0; i < saInfoListArray.length; ++i)
        {
        if (BeanUtilities.checkFilterOptions(saInfoListArray[i].getSummaryStatus(),
                             aFilterState,
                             aFilterStates))
            {
            sLog.fine("ListBean.getServiceAssembliesInfoList() adding " + saInfoListArray[i]);
            resultList.add(saInfoListArray[i]);
            }
        }

        return resultList;
    }


    /**
     *
     */
    List<SelectableJBIComponentInfo> getServiceEnginesInfoList()
    {
       sLog.fine("ListBean.getServiceEnginesInfoList()");
       return getServiceEnginesInfoList(mFilterComponentState,
                                         mFilterComponentStates);
    }


    List<SelectableJBIComponentInfo> getServiceEnginesInfoList(String aFilterState,
                                                               ArrayList aFilterStates)
    {
       sLog.fine("ListBean.getServiceEnginesInfoList(" + aFilterState +
                   ", " + aFilterStates + ")");
       ArrayList<SelectableJBIComponentInfo> resultList =
        new ArrayList<SelectableJBIComponentInfo>();

    SelectableJBIComponentInfo[] seInfoListArray =
        getCompStatus(JBIConstants.JBI_SERVICE_ENGINE_TYPE);

    for (int i = 0; i < seInfoListArray.length; ++i)
        {
        if (BeanUtilities.checkFilterOptions(seInfoListArray[i].getSummaryStatus(),
                             aFilterState,
                             aFilterStates))
            {
            sLog.fine("ListBean.getServiceEnginesInfoList() adding " + seInfoListArray[i]);
            resultList.add(seInfoListArray[i]);
            }
        }

        return resultList;
    }


    List<SelectableJBIComponentInfo> getSharedLibrariesInfoList()
    {
        String xmlQueryResults = getListSharedLibraries();

        ArrayList<SelectableJBIComponentInfo> result =
        new ArrayList<SelectableJBIComponentInfo>();

        List sharedLibrariesComponentInfoList =
        JBIComponentInfo.readFromXmlText(xmlQueryResults);
        for ( Iterator it = sharedLibrariesComponentInfoList.iterator(); it.hasNext();)
        {
            SelectableJBIComponentInfo next =
            new SelectableJBIComponentInfo((JBIComponentInfo) it.next());
            // shared libraries have no state to summarize
            result.add(next);
        }
        sLog.fine("ListBean.getSharedLibrariesInfoList(), result=" + result);
        return result;
    }

    public String getListBindingComponents()
    {
        String result = "";
        try
        {
            if (null != mJac)
            {
                if (mJac.isJBIRuntimeEnabled())
                {
                    result =
                    mJac.listBindingComponents(LIST_TARGET);
                }
                else
                {
                    sLog.fine("ListBean.getListBindingComponents() scaffolding false");
                }
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("ListBean.getListBindingComponents(): caught jbiRemoteEx=" +
                               jbiRemoteEx);
        }
        catch (Exception unexpectedEx)
        {
            logSevereI18n(unexpectedEx);
        }
        return result;
    }

    public String getListServiceAssemblies()
    {
        String result = "";
        try
        {
            if (null != mJac)
            {
                if (mJac.isJBIRuntimeEnabled())
                {
                  result =
                    mJac.listServiceAssemblies(LIST_TARGET);
                }
                else
                {
                    sLog.fine("ListBean.getListServiceAssemblies() scaffolding false");
                 }
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("ListBean.getListServiceAssemblies(): caught jbiRemoteEx=" +
                               jbiRemoteEx);
        }
        catch (Exception unexpectedEx)
        {
            logSevereI18n(unexpectedEx);
        }
        return result;
    }
    public String getListServiceEngines()
    {
        String result = "";
        try
        {
            if (null != mJac)
            {
                if (mJac.isJBIRuntimeEnabled())
                {
                    result =
                    mJac.listServiceEngines(LIST_TARGET);
                }
                else
                {
                    sLog.fine("ListBean.getListServiceEngines() scaffolding false");
                 }
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
           sLog.fine("ListBean.getListServiceEngines(): caught jbiRemoteEx=" +
                               jbiRemoteEx);
        }
        catch (Exception unexpectedEx)
        {
            logSevereI18n(unexpectedEx);
        }
        return result;
    }
    public String getListSharedLibraries()
    {
        String result = "";
        try
        {
            if (null != mJac)
            {
                if (mJac.isJBIRuntimeEnabled())
                {
                    result =
                    mJac.listSharedLibraries(LIST_TARGET);
                }
                else
                {
                    sLog.fine("ListBean.getListSharedLibraries() scaffolding false");
                 }
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("ListBean.getListSharedLibraries(): caught jbiRemoteEx=" +
                               jbiRemoteEx);
        }
        catch (Exception unexpectedEx)
        {
            logSevereI18n(unexpectedEx);
        }

        sLog.fine("ListBean.getListShaerdLibraries(), result=" + result);
        return result;
    }

    public TableDataProvider getSharedTableData()
    {
        sLog.fine("ListBean.getSharedTableData()");
       TableDataProvider result =
        mCachedTableData;

       sLog.fine("ListBean.getSharedTableData(): result=" + result);
       return result;
    }


    public void setBindingsEnginesTableData()
    {
        List<SelectableJBIComponentInfo> combinedList = null;

        sLog.fine("ListBean.setBindingsEnginesTableData()");

        if (SharedConstants.DROP_DOWN_TYPE_SHOW_ALL.equals(mFilterType))
        {
            combinedList = getBindingComponentsInfoList();
            combinedList.addAll(getServiceEnginesInfoList());
        }
        else if (SharedConstants.DROP_DOWN_TYPE_BINDING.equals(mFilterType))
        {
            combinedList = getBindingComponentsInfoList();
        }
        else if (SharedConstants.DROP_DOWN_TYPE_ENGINE.equals(mFilterType))//not i18n
        {
            combinedList = getServiceEnginesInfoList();
        }

        TableDataProvider result = new ObjectListDataProvider (combinedList);
        mCachedTableData = result;
        sLog.fine("ListBean.setBindingsEnginesTableData(): result=" + result);
        }


    public void setDeploymentsTableData()
    {
       sLog.fine("ListBean.setDeploymentsTableData()");
       TableDataProvider result =
        new ObjectListDataProvider (getServiceAssembliesInfoList());
        mCachedTableData = result;
        sLog.fine("ListBean.setDeploymentsTableData(): result=" + result);
    }


    public void setLibrariesTableData()
    {
        sLog.fine("ListBean.setLibrariesTableData()");
        TableDataProvider result =
        new ObjectListDataProvider (getSharedLibrariesInfoList());
        mCachedTableData = result;
        sLog.fine("ListBean.setLibrariesTableData(): result=" + result);
     }


    public void setFilterType (String filterType)
    {
        mFilterType = filterType;
    }


    public String getFilterType ()
    {
        return mFilterType;
    }


    public void setFilterComponentState (String filterState)
    {
        mFilterComponentState = filterState;
    }


    public void setFilterComponentState (String filterState, String filterStates)
    {
        mFilterComponentState  = filterState;
        filterStates = filterStates.replace('[',' ');
        filterStates = filterStates.replace(']',' ');
        String[] states = filterStates.split("\\,");
        for (int i=0; i<states.length; i++)
        {
            states[i] = states[i].trim();
        }
        mFilterComponentStates = new ArrayList(Arrays.asList(states));
    }


    public String getFilterComponentState ()
    {
        return mFilterComponentState;
    }


    public void setFilterAssemblyState (String filterState)
    {
        mFilterAssemblyState = filterState;
    }


    public void setFilterAssemblyState (String filterState, String filterStates)
    {
        mFilterAssemblyState  = filterState;
        filterStates = filterStates.replace('[',' ');
        filterStates = filterStates.replace(']',' ');
        String[] states = filterStates.split("\\,");
        for (int i=0; i<states.length; i++)
        {
            states[i] = states[i].trim();
        }
        mFilterAssemblyStates = new ArrayList(Arrays.asList(states));
    }


    public String getFilterAssemblyState ()
    {
        return mFilterAssemblyState;
    }


    /**
     * determines the summary status for either all binding components or all service engines
     * using the server target for developer-profile, or using all targets for cluster-profile
     * @param a type
     * @returns an array of selectable component infos (for the requested type)
     * with updated summary status for each component
     */
    private SelectableJBIComponentInfo[] getCompStatus(String aType)
    {
    SelectableJBIComponentInfo[] result = new SelectableJBIComponentInfo[0];

        if (IS_CLUSTER_PROFILE)
        {
       sLog.fine("ListBean.getCompStatus(" + aType + ") cluster-profile");
       result = ClusterUtilities.getCompStatus(aType); // summarize across all targets
        }
        else // developer-profile
        {
        sLog.fine("ListBean.getCompStatus(" + aType + ") developer-profile");

        List compInfoList = null;

        if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aType)) // binding components
        {
            String listBcXmlQueryResults = getListBindingComponents();
            compInfoList =
            JBIComponentInfo.readFromXmlText(listBcXmlQueryResults);
        }
        else // service engines
        {
            String listSeXmlQueryResults = getListServiceEngines();
            compInfoList =
            JBIComponentInfo.readFromXmlText(listSeXmlQueryResults);
        }

        result = new SelectableJBIComponentInfo[compInfoList.size()];

        int index = 0;
        for ( Iterator it = compInfoList.iterator(); it.hasNext();)
        {
            SelectableJBIComponentInfo next =
            new SelectableJBIComponentInfo((JBIComponentInfo) it.next());
            sLog.fine("ListBean.getCompStatus(" + aType + ") next=" + next);
                    result[index] = next;
            result[index].setSummaryStatus(result[index].getState());
            sLog.fine("ListBean.getCompStatus(" + aType + "), result[" + index +
                           "].getState()=" + result[index].getState() + ")");
                    ++index;
        }
    }

    sLog.fine("ListBean.getCompStatus(" + aType + "), result.length=" + result.length);
    return result;
    }


    /**
     * determines the summary status for all service assemblies
     * using the server target for developer-profile, or using all targets for cluster-profile
     * @returns an array of selectable service assembly infos
     * with updated summary status for each deployment
     */
    private SelectableJBIServiceAssemblyInfo[] getSaStatus()
    {
    SelectableJBIServiceAssemblyInfo[] result = new SelectableJBIServiceAssemblyInfo[0];

        if (IS_CLUSTER_PROFILE)
        {
       sLog.fine("ListBean.getSaStatus() cluster-profile");
       result = ClusterUtilities.getSaStatus(); // summarize across all targets
        }
        else // developer-profile
        {
        sLog.fine("ListBean.getSaStatus() developer-profile");

        List saInfoList = null;

        String listSaXmlQueryResults = getListServiceAssemblies();
        saInfoList =
        ServiceAssemblyInfo.readFromXmlTextWithProlog(listSaXmlQueryResults);

        result = new SelectableJBIServiceAssemblyInfo[saInfoList.size()];

        int index = 0;
        for ( Iterator it = saInfoList.iterator(); it.hasNext();)
        {
            SelectableJBIServiceAssemblyInfo next =
            new SelectableJBIServiceAssemblyInfo((ServiceAssemblyInfo) it.next());
            sLog.fine("ListBean.getSaStatus() next=" + next);
            result[index] = next;
            result[index].setSummaryStatus(result[index].getState());
            sLog.fine("ListBean.getSaStatus(), result[" + index +
                           "].getState()=" + result[index].getState() + ")");
                    ++index;
        }
    }

    sLog.fine("ListBean.getSaStatus(), result.length=" + result.length);


    return result;
    }

    public void setUpdateInstancesList(String[] list) {
        mUpdateList = list;
    }

    public String[] getUpdateInstancesList() {
        return mUpdateList;
    }

    private void logSevereI18n(Exception anUnexpectedException)
    {
        sLog.fine("ListBean.logSevereI18n anUnexpectedException=" +
                  anUnexpectedException);

        // use localized message, if available (may get nonlocalized msg)
        String exMsg = anUnexpectedException.getLocalizedMessage();
        sLog.fine("ListBean.logSevereI18n getLocalizedMessage=" + exMsg);

        // log type of exception if messages are not available
        if (null == exMsg)
        {
            exMsg = anUnexpectedException.getClass().getName();
            sLog.fine("ListBean.logSevereI18n exMsg=" + exMsg);
        }

        Object[] args = { exMsg };

        String msg = null;
        try
        {
            // log localized common client exception with insert
            msg=
                GuiUtil.getMessage(I18nUtilities.getResourceString(
                    "jbi.common.client.unexpected.exception"), args);
        }
        catch (Exception unexpectedI18nEx)
        {
            sLog.fine("ListBean.logSevereI18n caught unexpectedI18nEx=" +
                      unexpectedI18nEx);

            // log localized i18n exception (if insert fails)
            msg = I18nUtilities.getResourceString(
                "jbi.internal.i18n.error");
        }

        sLog.severe(msg);

        // in all cases log the original stack trace
        // (this is a common client or JBI runtime escaped exception
        // and prevents the console from displaying information correctly)
        anUnexpectedException.printStackTrace(System.err);
    }

    private String[] mUpdateList;

    /**
     * cached data for table row group
     */
    private TableDataProvider mCachedTableData = null;

    /**
     * provides JBI common client administration interface.
     */
    private JBIAdminCommands mJac = BeanUtilities.getClient();

    /** Holds value of property testCaseOptions. */
    private Option[] testCaseOptions = null;

    /**
     * Holds the filter type for the components table
     */
    private String mFilterType = SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;

    /**
     * Holds the filter type for the components table
     */
    private String mFilterComponentState =SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;
    private ArrayList mFilterComponentStates = null;

    /**
     * Holds the filter type for the components table
     */
    private String mFilterAssemblyState = SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;
    private ArrayList mFilterAssemblyStates = null;

}
