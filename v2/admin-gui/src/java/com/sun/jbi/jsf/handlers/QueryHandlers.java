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

package com.sun.jbi.jsf.handlers;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.jbi.jsf.bean.ListBean;
import com.sun.jbi.jsf.bean.SelectableTargetInfo;
import com.sun.jbi.jsf.bean.ShowBean;
import com.sun.jbi.jsf.bean.TargetBean;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.jsf.component.TableRowGroup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides jsftemplating handlers for List/Show tables
 */
public class QueryHandlers
{
    public final static String TBL_PAGE_TYPE_BINDINGS_ENGINES  = "bindingsEngines";
    public final static String TBL_PAGE_TYPE_DEPLOYMENTS       = "deployments";
    public final static String TBL_PAGE_TYPE_LIBRARIES         = "libraries";

    private static Logger sLog = JBILogger.getInstance();

    /**
     *  <p> This method sets the binding components and service engines
     * component infos list into the TargetBean.
     *  <p> Input value: "targetName" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="jbiSetComponentsForTarget",
              input={
                  @HandlerInput (name="targetName",  type=String.class, required=true)})

    public static void  jbiSetComponentsForTarget(HandlerContext handlerCtx)
    {
        String targetName = (String)handlerCtx.getInputValue ("targetName");
        sLog.fine("QueryHandlers.jbiSetComponentsForTarget(...), targetName=" + targetName);
        List componentsList = ClusterUtilities.findBindingComponentsForTarget(targetName);
        componentsList.addAll(ClusterUtilities.findServiceEnginesForTarget(targetName));
        TargetBean targetBean = BeanUtilities.getTargetBean();
        sLog.fine("QueryHandlers.jbiSetComponentsForTarget(...), componentsList=" + componentsList);
        targetBean.setComponentsList(componentsList);
    }

    /**
     *  <p> This method sets the service assemblies
     *  infos list into the TargetBean.
     *  <p> Input value: "targetName" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="jbiSetDeploymentsForTarget",
              input={
                  @HandlerInput (name="targetName",  type=String.class, required=true)})

    public static void  jbiSetDeploymentsForTarget(HandlerContext handlerCtx)
    {
        String targetName = (String)handlerCtx.getInputValue ("targetName");
        sLog.fine("QueryHandlers.jbiSetDeploymentsForTarget(...), targetName=" + targetName);
        List deploymentsList = ClusterUtilities.findServiceAssembliesForTarget(targetName);
        TargetBean targetBean = BeanUtilities.getTargetBean();
        targetBean.setDeploymentsList(deploymentsList);
    }

    /**
     *  <p> This method sets the shared library component infos list
     *  into the TargetBean.
     *  <p> Input value: "targetName" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="jbiSetLibrariesForTarget",
              input={
                  @HandlerInput (name="targetName",  type=String.class, required=true)})

    public static void  jbiSetLibrariesForTarget(HandlerContext handlerCtx)
    {
        String targetName = (String)handlerCtx.getInputValue ("targetName");
        sLog.fine("QueryHandlers.jbiSetLibrariesForTarget(...), targetName=" + targetName);
        List librariesList = ClusterUtilities.findSharedLibrariesForTarget(targetName);
        TargetBean targetBean = BeanUtilities.getTargetBean();
        targetBean.setLibrariesList(librariesList);
    }

    /**
     *  <p> This method filters a list of targets* to just those that have the
     * "current" (ShowBean) component/deployment installed or deployed and sets
     * the ShowBean's filteredTargetsList property.  If this list is empty,
     * the component/deploymens is installed/deployed to the 'domain' target only.
     *  <p> Input value: "allTargetsList" -- Type: <code>java.util.List</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="jbiSetFilteredTargetsList",
              input={
                  @HandlerInput (name="pageName",  type=String.class, required=true),
                  @HandlerInput (name="clustersList",  type=java.util.List.class, required=true),
                  @HandlerInput (name="serversList",  type=java.util.List.class, required=true)})

    public static void  jbiSetFilteredTargetsList (HandlerContext handlerCtx)
    {
        List filteredTargetsList = new ArrayList();
        String pageName = (String)handlerCtx.getInputValue ("pageName");
        List clustersList = (List)handlerCtx.getInputValue ("clustersList");
        List serversList = (List)handlerCtx.getInputValue ("serversList");

        sLog.fine("QueryHandlers.setFilteredTargetsList, handlerCtx.getInputValue(\"clustersList\")=" +
                               clustersList + ", serversList=" + serversList + ", pageName=" + pageName);

        ShowBean showBean = BeanUtilities.getShowBean(); // implicit input

        List allTargetsList = new ArrayList();

        // add each cluster to the list in a property containing its name and type (cluster)
        if (null != clustersList)
        {
            Iterator clusters = clustersList.iterator();
            while (clusters.hasNext())
            {
                String clusterName = (String) clusters.next();
                Properties clusterProps = new Properties();
                clusterProps.setProperty("name", clusterName);
                clusterProps.setProperty("type", "cluster");
                sLog.fine("QueryHandlers.jbiSetFilteredTargetsList(...), adding clusterProps=" + clusterProps);
                allTargetsList.add(clusterProps);
            }
        }

        // add each server to the list in a property containing its name and type (server)
        if (null != serversList)
        {
            Iterator servers = serversList.iterator();
            while (servers.hasNext())
            {
                String serverName = (String) servers.next();
                Properties serverProps = new Properties();
                serverProps.setProperty("name", serverName);
                serverProps.setProperty("type", "server");
                sLog.fine("QueryHandlers.jbiSetFilteredTargetsList(...), adding serverProps=" + serverProps);
                allTargetsList.add(serverProps);
            }
        }


        // Depending on the page, set the filter state value
        String filterState = SharedConstants.DROP_DOWN_TYPE_SHOW_ALL;
        ArrayList filterStates = null;
        if (pageName.equalsIgnoreCase(TBL_PAGE_TYPE_BINDINGS_ENGINES))
        {
            filterState  = showBean.getFilterTargetComponentState();
            filterStates = showBean.getFilterTargetComponentStates();
        }
        else if (pageName.equalsIgnoreCase(TBL_PAGE_TYPE_DEPLOYMENTS))
        {
            filterState  = showBean.getFilterTargetAssemblyState();
            filterStates = showBean.getFilterTargetAssemblyStates();
        }

        Iterator possibleTargets = allTargetsList.iterator();
        while (possibleTargets.hasNext())
        {
            Properties targetProps = (Properties) possibleTargets.next();
            String target = (String) targetProps.getProperty("name");
            String type = (String) targetProps.getProperty("type");

            // TBD: instead of calling .check in this loop, pass in an array of target names
            // to move loop inside of common client

            String status = showBean.check(target);
            if (null != status)
            {
                sLog.fine("QueryHandlers.setFilteredTargetsList, target=" + target + ", status=" + status);

                SelectableTargetInfo selectableTarget =
                new SelectableTargetInfo(target, status, type);

                if (BeanUtilities.checkFilterOptions(selectableTarget.getStatus(),
                                                     filterState,
                                                     filterStates))
                {
                    filteredTargetsList.add(selectableTarget);
                }
            }
        }

        sLog.fine("QueryHandlers.setFilteredTargetsList, filteredTargetsList=" + filteredTargetsList);

        showBean.setTargetsList(filteredTargetsList);
    }


    /**
     *  sets the component name and type
     *  <p> Input value: "JBIComponentName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "JBIComponentType" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="setJBIComponentId",
             input={
                 @HandlerInput(name="JBIComponentName", type=String.class, required=true),
                 @HandlerInput(name="JBIComponentType", type=String.class, required=true)
             }
            )
    public static void setJBIComponentId(HandlerContext handlerCtx)
    {

        String componentName = (String) handlerCtx.getInputValue("JBIComponentName");
        String componentType = (String) handlerCtx.getInputValue("JBIComponentType");
        sLog.fine("QueryHandlers.setJBIComponentId(JBIComponentName=" + componentName +
                               ", JBIComponentType=" + componentType + ")");
        ShowBean showBean = BeanUtilities.getShowBean();
        showBean.setName(componentName);
        showBean.setType(componentType);
    }

    /**
     * <p> Gets jbi.xml descriptor  from client and sets in a variable
     * <p> Input  value: "compName" -- Type: <code> java.lang.String</code> Name of the component
     * <p> Input  value: "compType" -- Type: <code> java.lang.String</code> JBI type whose decriptor is requested
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="setMetaDataContents",
             input={
                 @HandlerInput(name="compName", type=String.class, required=true),
                 @HandlerInput(name="compType", type=String.class, required=true)})

    public static void setMetaDataContents(HandlerContext handlerCtx)
    {
        sLog.fine("DEPRECATED setMetaDataContents");
        String compName = (String)
                          handlerCtx.getInputValue("compName");
        String compType = (String)
                          handlerCtx.getInputValue("compType");

        ShowBean showBean = BeanUtilities.getShowBean();
        showBean.setName(compName);
        showBean.setType(compType);
    }

    /**
     * Returns a list of names and types for the selected rows.
     * @param aGroup <code>TableRowGroup</code> the table data with some rows selected.
     * @return <code>List</code> of <code>Properties</code> objects
     * <p> Each properties object has 'name' and 'type' keys and values.
     */
    private static List getSelectedRowProperties(TableRowGroup aGroup)
    {
        ArrayList result = new ArrayList();

        ObjectListDataProvider dp = (ObjectListDataProvider)
                                    aGroup.getSourceData();

        if (null != dp)
        {
            try
            {
                FieldKey fkName = dp.getFieldKey(SharedConstants.KEY_NAME);
                FieldKey fkType = dp.getFieldKey(SharedConstants.KEY_TYPE);

                RowKey[] rowKeys = aGroup.getSelectedRowKeys();

                for (int cnt = 0; cnt < rowKeys.length; cnt++)
                {
                    Properties selectedRowProperties =
                    new Properties();


                    String compName = (String)
                                      dp.getValue(fkName, rowKeys[cnt]);

                    selectedRowProperties.setProperty(SharedConstants.KEY_NAME, compName);

                    String compType = (String)
                                      dp.getValue(fkType, rowKeys[cnt]);

                    selectedRowProperties.setProperty(SharedConstants.KEY_TYPE, compType);

                    result.add(selectedRowProperties);
                }
            }
            catch (Exception ex)
            {
                sLog.fine("QueryHandlers.getSelectedRowProperties(), caught ex=" + ex);
                ex.printStackTrace(System.err);
            }
        }
        else
        {
            sLog.fine("QueryHandlers.getSelectedRowProperties(), cannot process dp=" + dp);
        }

        sLog.fine("QueryHandlers.getSelectedRowProperties(), result=" + result);
        return result;
    }


    /**
     * <p> Sets the specified type of table row group data for a shared table
     * <p> Input  value: "tableType" -- Type: <code> java.lang.String</code>
     * Valid types: 'deployments,' 'bindingsEngines,' or 'libraries.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="setSharedTableData",
             input={
                 @HandlerInput(name="tableType", type=String.class, required=true)})

    public static void setSharedTableData(HandlerContext context)
    {
        String tableType = (String) context.getInputValue("tableType");
        sLog.fine("QueryHandlers.setSharedTableData(), tableType=" + tableType);

        ListBean  listBean = BeanUtilities.getListBean();

        if (TBL_PAGE_TYPE_BINDINGS_ENGINES.equals(tableType))
        {
            listBean.setBindingsEnginesTableData();
        }
        else if (TBL_PAGE_TYPE_DEPLOYMENTS.equals(tableType))
        {
            listBean.setDeploymentsTableData();
        }
        else if (TBL_PAGE_TYPE_LIBRARIES.equals(tableType))
        {
            listBean.setLibrariesTableData();
        }
    }

    @Handler(id="jbiGetWormholeLinkForName",
             input={
                 @HandlerInput(name="clusterOrServerName", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="wormholeLink", type=String.class)} )

    public static void jbiGetWormholeLinkForName(HandlerContext handlerCtx)
    {
        String result = "";
        String clusterOrServerName = (String)
                                     handlerCtx.getInputValue("clusterOrServerName");

        if (ClusterUtilities.isCluster(clusterOrServerName))
        {
            result = "cluster/clusterGeneral.jsf?clusterName=" + clusterOrServerName;
        }
        else if (ClusterUtilities.isServer(clusterOrServerName))
        {
            result = "standalone/standaloneInstanceGeneral.jsf?instanceName=" + clusterOrServerName;
        }

        handlerCtx.setOutputValue ("wormholeLink", result);

        sLog.fine("QueryHandlers.jbiGetWormholeLinkForName(...), " +
                               " clusterOrServerName=" + clusterOrServerName +
                               ", result=" + result);
        }

    /**
     *  <p> Writes a LogLevel.FINE record to the log
     *  <p> Input value: "diagnostic" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="jbiLogFine",
              input={
                  @HandlerInput(name="where", type=String.class, required=true),
                  @HandlerInput(name="diagnostic", type=String.class, required=true)})

    public static void  jbiLogFine(HandlerContext handlerCtx)
    {
        String where = (String)
            handlerCtx.getInputValue("where");
        String diagnostic = (String)
            handlerCtx.getInputValue("diagnostic");
        sLog.fine("QueryHandlers.jbiLogFine: where=[" + where + "], diagnostic=" + diagnostic);
    }

}
