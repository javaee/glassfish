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
 *  ClusterUtilties.java
 *
 */

package com.sun.jbi.jsf.util;

import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.jsf.bean.SelectableJBIComponentInfo;
import com.sun.jbi.jsf.bean.SelectableJBIServiceAssemblyInfo;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * Provides utilities for cluster profile related tables
 *
 **/

public final class ClusterUtilities
{

    //Get Logger to log fine mesages for debugging
    private static Logger sLog = JBILogger.getInstance();

    static
    {
        sJac = BeanUtilities.getClient();
    }

    public static List findBindingComponentsForTarget(String aTarget)
    {
        List result = new ArrayList(); // default to empty list
        List compInfoList = null;
        if (null != sJac)
        {
        try
            {
            String queryResult =
                sJac.listBindingComponents(aTarget);
            compInfoList = JBIComponentInfo.readFromXmlText(queryResult);

            }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            sLog.fine("ClusterUtilities.findBindingComponentsForTarget(" + aTarget +
                       "), caught jbiRemoteEx=" + jbiRemoteEx);
            jbiRemoteEx.printStackTrace(System.err);
            }
        }
        if (null != compInfoList)
        {
        Iterator components = compInfoList.iterator();
        while (components.hasNext())
            {
            JBIComponentInfo component = (JBIComponentInfo)
                components.next();
            SelectableJBIComponentInfo selectableComponent =
                new SelectableJBIComponentInfo(component);
            result.add(selectableComponent);
            }
        }

        sLog.fine("ClusterUtilities.findBindingComponentsForTarget(" + aTarget + "), result=" + result);

        return result;
    }

    public static List findServiceAssembliesForTarget(String aTarget)
    {
        List result = new ArrayList();
        List saInfoList = null;
        if (null != sJac)
        {
        try
            {
            String queryResult =
                sJac.listServiceAssemblies(aTarget);
            sLog.fine("ClusterUtilities.findServiceAssembliesForTarget(" + aTarget + "), queryResult=" + queryResult);


            saInfoList = ServiceAssemblyInfo.readFromXmlTextWithProlog(queryResult);
            sLog.fine("ClusterUtilities.findServiceAssembliesForTarget(" + aTarget + "), saInfoList=" + saInfoList);

            }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            sLog.fine("ClusterUtilities.findServiceAssembliesForTarget(" + aTarget +
                       "), caught jbiRemoteEx=" + jbiRemoteEx);
            jbiRemoteEx.printStackTrace(System.err);
            }
        }
        if (null != saInfoList)
        {
        Iterator deployments = saInfoList.iterator();
        while (deployments.hasNext())
            {
            ServiceAssemblyInfo deployment = (ServiceAssemblyInfo)
                deployments.next();
            SelectableJBIServiceAssemblyInfo selectableDeployment =
                new SelectableJBIServiceAssemblyInfo(deployment);
            result.add(selectableDeployment);
            }
        }

        sLog.fine("ClusterUtilities.findServiceAssembliesForTarget(" + aTarget + "), result=" + result);

        return result;
    }

    public static List findServiceEnginesForTarget(String aTarget)
    {
        List result = new ArrayList();
        List compInfoList = null;
        if (null != sJac)
        {
        try
            {
            String queryResult =
                sJac.listServiceEngines(aTarget);
            compInfoList = JBIComponentInfo.readFromXmlText(queryResult);

            }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            sLog.fine("ClusterUtilities.findServiceEnginesForTarget(" + aTarget +
                       "), caught jbiRemoteEx=" + jbiRemoteEx);
            jbiRemoteEx.printStackTrace(System.err);
            }
        }
        if (null != compInfoList)
        {
        Iterator components = compInfoList.iterator();
        while (components.hasNext())
            {
            JBIComponentInfo component = (JBIComponentInfo)
                components.next();
            SelectableJBIComponentInfo selectableComponent =
                new SelectableJBIComponentInfo(component);
            result.add(selectableComponent);
            }
        }

        sLog.fine("ClusterUtilities.findServiceEnginesForTarget(" + aTarget + "), result=" + result);

        return result;
    }

    public static List findSharedLibrariesForTarget(String aTarget)
    {
        List result = new ArrayList();
        List compInfoList = null;
        if (null != sJac)
        {
        try
            {
            String queryResult =
                sJac.listSharedLibraries(aTarget);
            compInfoList = JBIComponentInfo.readFromXmlText(queryResult);

            }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            sLog.fine("ClusterUtilities.findSharedLibrariesForTarget(" + aTarget +
                       "), caught jbiRemoteEx=" + jbiRemoteEx);
            jbiRemoteEx.printStackTrace(System.err);
            }
        }
        if (null != compInfoList)
        {
        Iterator components = compInfoList.iterator();
        while (components.hasNext())
            {
            JBIComponentInfo component = (JBIComponentInfo)
                components.next();
            SelectableJBIComponentInfo selectableComponent =
                new SelectableJBIComponentInfo(component);
            result.add(selectableComponent);
            }
        }

        sLog.fine("ClusterUtilities.findSharedLibrariesForTarget(" + aTarget + ") result=" + result);
        return result;
    }

    /**
     * returns a sorted list of all cluster and stand-alone instance targets
     */
    public static List findAllNonDomainTargets()
    {
        ArrayList result = null;

        Set clusterSet = AMXUtil.getDomainConfig().getClusterConfigMap().keySet();
        Set serverSet = AMXUtil.getDomainConfig().getStandaloneServerConfigMap().keySet();

        Set targetsSet = new TreeSet(clusterSet);
        targetsSet.addAll(serverSet);

        result = new ArrayList(targetsSet);
        sLog.fine("ClusterUtilities.findAllNonDomainTargets(), result=" +
                   result);

        return result;
    }

    public static List findTargetsForNameByType(String aName, String aType)
    {
        List result = new ArrayList();
        sLog.fine("ClusterUtilities.findTargetsForNameByType(" + aName +
                   ", " + aType + ")");

        List sortedTargetsList = findAllNonDomainTargets();

        Iterator targetsIt = sortedTargetsList.iterator();
        while (targetsIt.hasNext())
        {
        String nextTarget = (String)
            targetsIt.next();

        if (null != sJac)
            {
            try
                {
                String queryResult = "";

                boolean isServiceAssembly = false;
                if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aType))
                    {
                    
                    queryResult =
                        sJac.showBindingComponent(aName,
                                      SharedConstants.NO_STATE_CHECK,
                                      SharedConstants.NO_LIBRARY_CHECK,
                                      SharedConstants.NO_DEPLOYMENT_CHECK,
                                      nextTarget);
                    }
                else if (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(aType))
                    {
                    queryResult =
                        sJac.showServiceEngine(aName,
                                   SharedConstants.NO_STATE_CHECK,
                                   SharedConstants.NO_LIBRARY_CHECK,
                                   SharedConstants.NO_DEPLOYMENT_CHECK,
                                   nextTarget);
                    }
                else if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(aType))
                    {
                    isServiceAssembly = true;
                    queryResult =
                        sJac.showServiceAssembly(aName,
                                     SharedConstants.NO_STATE_CHECK,
                                     SharedConstants.NO_COMPONENT_CHECK,
                                     nextTarget);
                    }
                else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(aType))
                    {
                    
                    queryResult =
                        sJac.showSharedLibrary(aName,
                                   SharedConstants.NO_COMPONENT_CHECK,
                                   nextTarget);
                    }

                // get a list (empty or single element) from the query, based on type=SA or not.
                List list =
                    (isServiceAssembly) ? ServiceAssemblyInfo.readFromXmlTextWithProlog(queryResult)
                    : JBIComponentInfo.readFromXmlText(queryResult);

                // if the list is non-empty, the component or SA  is installed or deployed on this target
                if (1 == list.size())
                    {
                    Properties targetProperties = new Properties();
                    targetProperties.setProperty(SharedConstants.KEY_NAME, nextTarget);
                    result.add(targetProperties);

                    sLog.fine("ClusterUtilities.findTargetsForNameByType(" +
                                   aName + ", " + aType + "), nextTarget=" +
                                   nextTarget + ", queryResult=" + queryResult);

                    }
                }
            catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
                {
                sLog.fine("ClusterUtilities.findTargetsForNameByType(" + aName + ", " +
                           aType + "), caught jbiRemoteEx=" + jbiRemoteEx);
                jbiRemoteEx.printStackTrace(System.err);
                }
            }
        }

        sLog.fine("ClusterUtilities.findTargetsForNameByType(...). result=" + result);

        return result;
    }


    /**
     * Get the Cluster name for a cluster instance.  This method will
     * return the name of the cluster for a cluster instance.  If the 
     * instance specified is not a cluster instance, the instance name 
     * will be returned.
     *	@param	aInstanceName  The name of the instance
     *  @return the cluster name or the given aInstanceName
     */
    public static String getInstanceDomainCluster (String aInstanceName)
    {
        String clusterName = aInstanceName;
        List allNonDomainTargets = ClusterUtilities.findAllNonDomainTargets();
        List allTargets = new ArrayList(allNonDomainTargets);
        Iterator targetIt = allTargets.iterator();
        while (targetIt.hasNext())
        {
            String target = (String) targetIt.next();
            if (ClusterUtilities.isCluster(target))
            {
                Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(target).getClusteredServerConfigMap();
                List instancesForOneCluster = new ArrayList();
                if (null != serverMap)
                {
                    for (String key : serverMap.keySet())
                    {
                        String name = serverMap.get(key).getName();
                        if (name.equalsIgnoreCase(aInstanceName))
                        {
                            clusterName = target;
                            return clusterName;
                        }
                    }
                }
            }
        }
        return clusterName;
    }


    public static boolean isClusterProfile()
    {
        boolean result = AMXUtil.supportCluster();
        sLog.fine("ClusterUtilities.isClusterProfile(). result=" + result);
        return result;
    }

    public static boolean isCluster(String aClusterOrServerName)
    {
        Set clusterSet = AMXUtil.getDomainConfig().getClusterConfigMap().keySet();
        boolean result = clusterSet.contains(aClusterOrServerName);
        sLog.fine("ClusterUtilities.isCluster(" + aClusterOrServerName + "). result=" + result);
        return result;
    }

    public static boolean isServer(String aClusterOrServerName)
    {
        Set serverSet = AMXUtil.getDomainConfig().getStandaloneServerConfigMap().keySet();
        boolean result = serverSet.contains(aClusterOrServerName);
        sLog.fine("ClusterUtilities.isServer(" + aClusterOrServerName + "). result=" + result);
        return result;
    }

    public static SelectableJBIComponentInfo[] getCompStatus(String aType)
    {
        sLog.fine("ClusterUtilities.getCompStatus(" + aType + ")1");
        SelectableJBIComponentInfo[] resultDomainComponentsArray =
        new SelectableJBIComponentInfo[0];

    Map queryMapResult = getAllTargetCompInfosByType(aType);

    if (null != queryMapResult)
        {
        resultDomainComponentsArray = findDomainComponents(queryMapResult);

        // process all domain components
        for (int i = 0; i < resultDomainComponentsArray.length; ++i)
            {
            // process all targets, looking for this component

            String name = resultDomainComponentsArray[i].getName();

            int disabledCount = 0;
            int enabledCount = 0;

            Set targetNames = queryMapResult.keySet();
            Iterator targets = targetNames.iterator();

            processAllNonDomainTargets:
            while (targets.hasNext())
                {
                String nextTarget = (String)
                    targets.next();
                if (JBIAdminCommands.DOMAIN_TARGET_KEY.equals(nextTarget))
                    {
                    continue processAllNonDomainTargets;
                    }

                String info = (String)
                    queryMapResult.get(nextTarget);

                List list =
                    JBIComponentInfo.readFromXmlText(info);

                if (0 < list.size())
                    {
                    Iterator components = list.iterator();

                    processOnlyTargetComponent:
                    while (components.hasNext())
                        {
                        JBIComponentInfo targetCompInfo = (JBIComponentInfo)
                            components.next();

                        if (!name.equals(targetCompInfo.getName())) // skip "other" components
                            {
                            continue processOnlyTargetComponent;
                            }

                        if ("Started".equalsIgnoreCase(targetCompInfo.getState()))
                            {
                            ++ enabledCount;
                            }
                        else
                            {
                            ++ disabledCount;
                            }
                        }
                    }
                }

            // summarizes for this component (i.e. updates the summary state)
            summarizeStateForComponent(resultDomainComponentsArray[i], enabledCount, disabledCount);
            }

        }
    return resultDomainComponentsArray;
    }


    public static SelectableJBIServiceAssemblyInfo[] getSaStatus()
    {
        sLog.fine("ClusterUtilities.getSaStatus()1");

        SelectableJBIServiceAssemblyInfo[] resultDomainServiceAssembliesArray =
        new SelectableJBIServiceAssemblyInfo[0];

    Map queryMapResult = getAllTargetSaInfos();

    if (null != queryMapResult)
        {
        resultDomainServiceAssembliesArray = findDomainDeployments(queryMapResult);

        // process all domain deployments
        for (int i = 0; i < resultDomainServiceAssembliesArray.length; ++i)
            {
            // process all targets, looking for this saonent

            String name = resultDomainServiceAssembliesArray[i].getName();

            int disabledCount = 0;
            int enabledCount = 0;

            Set targetNames = queryMapResult.keySet();
            Iterator targets = targetNames.iterator();

            processAllNonDomainTargets:
            while (targets.hasNext())
                {
                String nextTarget = (String)
                    targets.next();
                if (JBIAdminCommands.DOMAIN_TARGET_KEY.equals(nextTarget))
                    {
                    continue processAllNonDomainTargets;
                    }

                String info = (String)
                    queryMapResult.get(nextTarget);

                List list =
                    ServiceAssemblyInfo.readFromXmlTextWithProlog(info);

                if (0 < list.size())
                    {
                    Iterator deployments = list.iterator();

                    processOnlyTargetServiceAssembly:
                    while (deployments.hasNext())
                        {
                        ServiceAssemblyInfo targetSaInfo = (ServiceAssemblyInfo)
                            deployments.next();

                        if (!name.equals(targetSaInfo.getName())) // skip "other" deployments
                            {
                            continue processOnlyTargetServiceAssembly;
                            }

                        if ("Started".equalsIgnoreCase(targetSaInfo.getState()))
                            {
                            ++ enabledCount;
                            }
                        else
                            {
                            ++ disabledCount;
                            }
                        }
                    }
                }

            // summarizes for this saonent (i.e. updates the summary state)
            summarizeStateForDeployment(resultDomainServiceAssembliesArray[i], enabledCount, disabledCount);
            }

        }
    return resultDomainServiceAssembliesArray;
    }


    /**
     * get all the JBIComponentInfos for all targets, including the domain target.
     * @returns a Map of targets to the ServiceAssemblies deployed on them
     */
    private static Map getAllTargetCompInfosByType(String aType)
    {
    Map queryMapResult = null;

    sLog.fine("ClusterUtilities.getAllTargetCompInfosByType(" + aType + ")");

    List allTargets = findAllNonDomainTargets();
    allTargets.add(JBIAdminCommands.DOMAIN_TARGET_KEY);

    String[] allTargetsArray = (String[])
        allTargets.toArray(new String[] {});

    sLog.fine("ClusterUtilities..getAllTargetCompInfosByType(" + aType + ")2, allTargetsArray=" +
                   allTargetsArray + ", .length=" + allTargetsArray.length);

    if (null != sJac)
        {
        try
            {
            // get all components of this request's component type, mapped by target name

            if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aType))
                {
                queryMapResult =
                    sJac.listBindingComponents(allTargetsArray);
                }
            else // service engines
                {
               queryMapResult =
                    sJac.listServiceEngines(allTargetsArray);
               }
            }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            // TBD use logging warning
            sLog.fine("ClusterUtilities.getAllTargetCompInfosByType(" + aType + "): caught jbiRemoteEx=" +
                       jbiRemoteEx);
            }

        }

    sLog.fine("ClusterUtilities.getAllTargetCompInfosByType(" + aType + "), queryMapResult=" + queryMapResult);


    return queryMapResult;
    }


    /**
     * get all the ServiceAssemblyInfos for all targets, including the domain target.
     * @returns a Map of targets to the ServiceAssemblies deployed on them
     */
    private static Map getAllTargetSaInfos()
    {
    Map queryMapResult = null;

    sLog.fine("ClusterUtilities.getAllTargetSaInfos()");


    List allTargets = findAllNonDomainTargets();
    allTargets.add(JBIAdminCommands.DOMAIN_TARGET_KEY);

    String[] allTargetsArray = (String[])
        allTargets.toArray(new String[] {});

    sLog.fine("ClusterUtilities..getAllTargetSaInfosByType()2, allTargetsArray=" +
                   allTargetsArray + ", .length=" + allTargetsArray.length);

    if (null != sJac)
        {
        try
            {
            // get all deployments, mapped by target name

           queryMapResult =
                sJac.listServiceAssemblies(allTargetsArray);
           }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
            {
            sLog.fine("ClusterUtilities.getAllTargetSaInfos(): caught jbiRemoteEx=" +
                       jbiRemoteEx);
            }

        }

    sLog.fine("ClusterUtilities.getAllTargetSaInfos(), queryMapResult=" + queryMapResult);

    return queryMapResult;
    }


    /**
     *
     */
    private static SelectableJBIComponentInfo[] findDomainComponents(Map aQueryMap)
    {
    SelectableJBIComponentInfo[] result
        = new SelectableJBIComponentInfo[0];

    sLog.fine("ClusterUtilities.findDomainComponents(" + aQueryMap + ")");

    Set targetNames = aQueryMap.keySet();
    Iterator targets = targetNames.iterator();

    findDomainComponents:
    while (targets.hasNext())
        {
        String nextTarget = (String)
            targets.next();
        if (JBIAdminCommands.DOMAIN_TARGET_KEY.equals(nextTarget))
            {
            String info = (String)
                aQueryMap.get(nextTarget); // domain target

            List list =
                JBIComponentInfo.readFromXmlText(info); // all "known" components

            result = new SelectableJBIComponentInfo[list.size()];

            for (int i = 0; i < result.length; ++i)
                {
                result[i] = new SelectableJBIComponentInfo((JBIComponentInfo)list.get(i));
                }

            break;
            }
        }

    sLog.fine("ClusterUtilities.findDomainComponents(" + aQueryMap + "), result=" + result + ", .length=" + result.length);

    return result;
    }



    /**
     *
     */
    private static SelectableJBIServiceAssemblyInfo[] findDomainDeployments(Map aQueryMap) {
    SelectableJBIServiceAssemblyInfo[] result
        = new SelectableJBIServiceAssemblyInfo[0];

    sLog.fine("ClusterUtilities.findDomainDeployments(" + aQueryMap + ")");

    Set targetNames = aQueryMap.keySet();
    Iterator targets = targetNames.iterator();

    findDomainDeployments:
    while (targets.hasNext())
        {
        String nextTarget = (String)
            targets.next();
        if (JBIAdminCommands.DOMAIN_TARGET_KEY.equals(nextTarget))
            {
            String info = (String)
                aQueryMap.get(nextTarget); // domain target

            List list =
                ServiceAssemblyInfo.readFromXmlTextWithProlog(info); // all "known" deployments

            result = new SelectableJBIServiceAssemblyInfo[list.size()];

            for (int i = 0; i < result.length; ++i)
                {
                result[i] = new SelectableJBIServiceAssemblyInfo((ServiceAssemblyInfo)list.get(i));
                }

            break;
            }
        }

    sLog.fine("ClusterUtilities.findDomainDeployments(" + aQueryMap + "), result=" + result + ", .length=" + result.length);

    return result;
    }



    /**
     *
     */
    private static void summarizeStateForComponent(SelectableJBIComponentInfo aComp, int anEnabledCount, int aDisabledCount)
    {

    sLog.fine("ClusterUtilities.summarizeStateForComponent(" + aComp +
                   ", " + anEnabledCount + ", " + aDisabledCount + ")");
    if (0 < anEnabledCount)
        {
        if (0 == aDisabledCount)
            {
            aComp.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.enabled.on.all.targets"));
            }
        else if (1 == anEnabledCount) // some disabled, but only 1 enabled
            {
            aComp.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.enabled.on.one.target"));
            }
        else if (1 < anEnabledCount) // some disabled, and more than 1 enabled
            {
            Object[] args = {anEnabledCount};
            String enabledOnNTargets =
                GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.operations.enabled.on.n.targets"), args);

            aComp.setSummaryStatus(enabledOnNTargets);
            }
        }
    else // not enabled anywhere
        {
        if (0 == aDisabledCount) // implies no targets
            {
            aComp.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.no.targets"));
            }
        else // disabled on all targets
            {
            aComp.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.disabled.on.all.targets"));
            }
        }
    sLog.fine("ClusterUtilities.summarizeStateForComponent(), summary=" + aComp.getSummaryStatus());

    }

    /**
     *
     */
    private static void summarizeStateForDeployment(SelectableJBIServiceAssemblyInfo anSa, int anEnabledCount, int aDisabledCount)
    {

    sLog.fine("ClusterUtilities.summarizeStateForDeployment(" + anSa +
                   ", " + anEnabledCount + ", " + aDisabledCount + ")");

    if (0 < anEnabledCount)
        {
        if (0 == aDisabledCount)
            {
            anSa.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.enabled.on.all.targets"));
            }
        else if (1 == anEnabledCount) // some disabled, but only 1 enabled
            {
            anSa.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.enabled.on.one.target"));
            }
        else if (1 < anEnabledCount) // some disabled, and more than 1 enabled
            {
            Object[] args = {anEnabledCount};
            String enabledOnNTargets =
                GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.operations.enabled.on.n.targets"), args);

            anSa.setSummaryStatus(enabledOnNTargets);
            }
        }
    else // not enabled anywhere
        {
        if (0 == aDisabledCount) // implies no targets
            {
            anSa.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.no.targets"));
            }
        else // disabled on all targets
            {
            anSa.setSummaryStatus(I18nUtilities.getResourceString ("jbi.operations.disabled.on.all.targets"));
            }
        }
    sLog.fine("ClusterUtilities.summarizeStateForDeployment(), summary=" + anSa.getSummaryStatus());


    }


    /**
     * cached JBI Admin Commands client
     */
    private static JBIAdminCommands sJac;
}
