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
 * EESupportHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */

package org.glassfish.admingui.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Vector;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.LayoutViewHandler;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.ConfigConfigKeys;
import com.sun.appserv.management.config.SystemPropertyConfig;
import com.sun.appserv.management.config.SystemPropertiesAccess;
import javax.faces.component.UIComponent;
import com.sun.webui.jsf.model.Option;


/**
 *
 * @author Anissa Lam
 */
public class EESupportHandlers {
    
    /** Creates a new instance of EESupportHandlers */
    public EESupportHandlers() {
    }
    
    /**
     *	<p> This handler checks if particular feature is supported  </p>
     *
     *  <p> Output value: "supportCluster" -- Type: <code>Boolean</code>/</p>
     *  <p> Output value: "supportHADB" -- Type: <code>Boolean</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="checkSupport",
    output={
        @HandlerOutput(name="supportCluster", type=Boolean.class),
        @HandlerOutput(name="supportHADB", type=Boolean.class)})
        public static void checkSupport(HandlerContext handlerCtx) {
            handlerCtx.setOutputValue("supportCluster", AMXRoot.getInstance().supportCluster());  
            handlerCtx.setOutputValue("supportHADB", false);
    }
    
    
     /**
     *	<p> This handler returns a list of Cluster in sorted order </p>
     *
     *  <p> Output value: "clusterList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getClusterList",
        output={
        @HandlerOutput(name="clusterList", type=java.util.List.class),
        @HandlerOutput(name="hasCluster", type=Boolean.class)})
    public static void getClustersList(HandlerContext handlerCtx) {
        Set clusterSet = AMXRoot.getInstance().getClustersConfig().getClusterConfigMap().keySet();
        ArrayList sortedClusterList = new ArrayList( new TreeSet(clusterSet));
        handlerCtx.setOutputValue("clusterList", sortedClusterList);
        handlerCtx.setOutputValue("hasCluster", (sortedClusterList.size()> 0) ? true : false);
    }
    
    
    /**
     *	<p> This handler returns a list of Standalone server in sorted order </p>
     *
     *  <p> Output value: "serverList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStandaloneServerList",
        output={
        @HandlerOutput(name="serverList", type=java.util.List.class)})
    public static void getStandaloneServerList(HandlerContext handlerCtx) {
        Set serverSet = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().keySet();
        ArrayList sortedServerList = new ArrayList( new TreeSet(serverSet));
        handlerCtx.setOutputValue("serverList", sortedServerList);
    }
    
    /**
     *	<p> This handler returns a list of all server instance, including both standalone 
     *  server and server of a cluster, in sorted order </p>
     *
     *  <p> Output value: "serverList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAllServerList",
        output={
        @HandlerOutput(name="serverList", type=java.util.List.class)})
    public static void getAllServerList(HandlerContext handlerCtx) {
        AMXRoot amxRoot = AMXRoot.getInstance();
        Set standaloneSet = amxRoot.getServersConfig().getStandaloneServerConfigMap().keySet();
        Set clusteredSet = amxRoot.getServersConfig().getClusteredServerConfigMap().keySet();
        
        ArrayList serverList = new ArrayList(standaloneSet);
        serverList.addAll(clusteredSet);
        handlerCtx.setOutputValue("serverList", serverList);
    }

    /**
     *	<p> This handler returns a list of virtual server for a give target.
     *  <p> Output value: "serverList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAllVSList",
        input={
        @HandlerInput(name="targetName", type=String.class, required=true ),
        @HandlerInput(name="PE", type=Boolean.class)},
        output={
        @HandlerOutput(name="serverList", type=java.util.List.class)})
    public static void getAllVSList(HandlerContext handlerCtx) {
	String targetName = (String)handlerCtx.getInputValue("targetName");
	Boolean PE = (Boolean)handlerCtx.getInputValue("PE");
        Set<String> vsList = TargetUtil.getVirtualServers(targetName);
        
        ArrayList<String> serverList = new ArrayList<String>(vsList);
	if(PE != null && PE) {
		//In the case of PE add one empty string, to reference to all VS
		String noneSelected = GuiUtil.getMessage("deploy.option.NoneSelected");	
		serverList.add(0, noneSelected);
	}
	//Removing system VS
	boolean remove = serverList.remove("__asadmin");
        handlerCtx.setOutputValue("serverList", serverList);
    }
    
     /**
     *	<p> This handler returns a list of all targets, including both standalone 
     *  server and cluster, in sorted order </p>
     *
     *  <p> Output value: "availableTargetList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAllTargetList",
        output={
        @HandlerOutput(name="allTargetList", type=java.util.List.class)})
    public static void getAllTargetList(HandlerContext handlerCtx) {
        AMXRoot amxRoot = AMXRoot.getInstance();
        Set<String> standaloneSet = amxRoot.getServersConfig().getStandaloneServerConfigMap().keySet();
        Set<String> clusterSet = amxRoot.getClustersConfig().getClusterConfigMap().keySet();
        
        Set<String> allTargets = new TreeSet<String>();
        allTargets.addAll(standaloneSet);
        allTargets.addAll(clusterSet);
        List targetList = new ArrayList(allTargets);
        handlerCtx.setOutputValue("allTargetList", targetList);
    }
    
    
    /**
     *	<p> This handler returns a list of all server instance of the specified cluster.
     *      If the cluster doesn't exist, an empty list will be returned. </p>
     *
     *  <p> Input value: "clusterName" -- Type: <code>String</code>/</p>
     *  <p> Output value: "serverList" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getServersOfCluster",
        input={
        @HandlerInput(name="clusterName", type=String.class, required=true )},
        output={
        @HandlerOutput(name="serverList", type=java.util.List.class)})
    public static void getServersOfCluster(HandlerContext handlerCtx) {
        String clusterName = (String)handlerCtx.getInputValue("clusterName");
        List servers = new ArrayList();
        if (GuiUtil.isEmpty(clusterName)){
            //TODO: Log warning
            System.out.println("getServerOfCluster:  clusterName is empty, no server is returned.");
            handlerCtx.setOutputValue("serverList", servers);
            return;
        }
        ClusterConfig cluster = AMXRoot.getInstance().getClustersConfig().getClusterConfigMap().get(clusterName);
        if (cluster == null){
             //TODO: Log warning of no such cluster
            System.out.println("getServerofCluster:  cluster does not exist --  " + clusterName);
            handlerCtx.setOutputValue("serverList", servers);
            return;
        }
        Set serverSet = cluster.getClusteredServerConfigMap().keySet();
        ArrayList sortedServerList = new ArrayList( new TreeSet(serverSet));
        handlerCtx.setOutputValue("serverList", sortedServerList);
    }
    
    /**
     * <p> This handler returns a list of configuration available.
     */
    @Handler(id="getConfigurationsList",
        output={
        @HandlerOutput(name="configList", type=java.util.List.class)})
    public static void getConfigurationsList(HandlerContext handlerCtx) {
        Set configSet = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().keySet();
        ArrayList sortedConfigList = new ArrayList( new TreeSet(configSet));
        handlerCtx.setOutputValue("configList", sortedConfigList);
    }

    
    /**
     *	<p> This Handler will attempt to build a [portion of a]
     *	    <code>UIComponent</code> tree.  It needs the <em>parent</em>
     *	    <code>UIComponent</code>, and a {@link LayoutElement} which
     *	    describes the <code>UIComponent</code> and all its children.  It
     *	    will walk the children to extract information and build the
     *	    cooresponding <code>UIComponent</code> tree.</p>
     *
     */
    @Handler(id="buildAllClusters",
	input={
            @HandlerInput(name="clusterList", type=java.util.List.class, required=true),
	    @HandlerInput(name="parent", type=UIComponent.class, required=true),
	    @HandlerInput(name="layoutElement", type=LayoutElement.class, required=true)
	})
    public static void buildAllClusters(HandlerContext handlerCtx) {
        List<String> clusterList = (List) handlerCtx.getInputValue("clusterList");
	UIComponent parent = (UIComponent) handlerCtx.getInputValue("parent");
	LayoutElement elt = (LayoutElement) handlerCtx.getInputValue("layoutElement");
        
        if (clusterList == null || clusterList.size() <=1){
            // There is only one cluster, it has been build already.
            return;
        }
        Map reqMap = handlerCtx.getFacesContext().getExternalContext().getRequestMap(); 
        for(int index=1; index < clusterList.size(); index++){
            reqMap.put("clusterId", "cluster"+index);
            reqMap.put("ignored", "false");
            reqMap.put("clusterName", clusterList.get(index));
            LayoutViewHandler.buildUIComponentTree(handlerCtx.getFacesContext(), parent, elt);
        }
    }
    
    /**
     *	<p> This Handler will attempt to build a [portion of a]
     *	    <code>UIComponent</code> tree.  It needs the <em>parent</em>
     *	    <code>UIComponent</code>, and a {@link LayoutElement} which
     *	    describes the <code>UIComponent</code> and all its children.  It
     *	    will walk the children to extract information and build the
     *	    cooresponding <code>UIComponent</code> tree.</p>
     *
     */
    @Handler(id="buildAllConfiguration",
	input={
            @HandlerInput(name="configList", type=java.util.List.class, required=true),
	    @HandlerInput(name="parent", type=UIComponent.class, required=true),
	    @HandlerInput(name="layoutElement", type=LayoutElement.class, required=true)
	})
    public static void buildAllConfiguration(HandlerContext handlerCtx) {
        List<String> configList = (List) handlerCtx.getInputValue("configList");
	UIComponent parent = (UIComponent) handlerCtx.getInputValue("parent");
	LayoutElement elt = (LayoutElement) handlerCtx.getInputValue("layoutElement");
        
        if (configList == null || configList.size() <=1){
            // There is only one configuration, it has been build already.
            return;
        }
        Map reqMap = handlerCtx.getFacesContext().getExternalContext().getRequestMap(); 
        for(int index=1; index< configList.size(); index++){
            reqMap.put("configId", "configuration"+ index);
            reqMap.put("configName", configList.get(index));
            LayoutViewHandler.buildUIComponentTree(handlerCtx.getFacesContext(), parent, elt);
        }
    }


    /**
     *	<p> This handler creates VS references for the given application/module name 
     *
     *  <p> Input value: "name" -- Type: <code>String</code>/</p>
     *  <p> Input value: "vsTargets" -- Type: <code>String[]</code>/</p>
     *  <p> Output value: "targetName" -- Type: <code>String</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="createVSReferences",
        input={
        @HandlerInput(name="name", type=String.class, required=true),
        @HandlerInput(name="targetName", type=String.class, required=true),
        @HandlerInput(name="vsTargets", type=String[].class, required=true )})
    public static void createVSReferences(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("name");
        String targetName = (String)handlerCtx.getInputValue("targetName");
		String[] selTargets = (String[])handlerCtx.getInputValue("vsTargets");
		String noneSelected = GuiUtil.getMessage("deploy.option.NoneSelected");
		String targets = null;
		if(selTargets.length > 0  && !noneSelected.equals(selTargets[0])) {
			targets = GuiUtil.arrayToString(selTargets, ",");
		}
		TargetUtil.setVirtualServers(name, targetName, targets);
	}

    /**
     *	<p> This handler creates references for the given resource name
     *
     *  <p> Input value: "name" -- Type: <code>String</code>/</p>
     *  <p> Input value: "targets" -- Type: <code>String[]</code>/</p>
     *  <p> Output value: "name" -- Type: <code>String</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="createResourceReferences",
        input={
        @HandlerInput(name="name", type=String.class, required=true),
        @HandlerInput(name="targets", type=String[].class, required=true )})
    public static void createResourceReferences(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("name");
        String[] selTargets = (String[])handlerCtx.getInputValue("targets");
        try{
            List<String> targets = Arrays.asList(selTargets);
            List<String> associatedTargets = TargetUtil.getDeployedTargets(name, false);

            for(String targetName:targets) {
                    if(!(associatedTargets.contains(targetName))) {
                            TargetUtil.createResourceRef(name, targetName);
                    }
            }
            //removes the old resource references
            for(String targetName:associatedTargets) {
                    if(!(targets.contains(targetName))) {
                            TargetUtil.removeResourceRef(name, targetName);
                    }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> Returns the options list to add in addRemove component
     *      in a new page    
     *  <p> Input value: "allTargets" -- Type: <code>List</code>
     *          - the list to be added to the field.</p>
     *          - <code>java.util.List</code> 
     *  <p> Output value: "optionsList" -- Type: <code>Option</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getTargetsOptionsList",
    input={
	    @HandlerInput(name="defaultTarget", type=String.class)},
    output={
        @HandlerOutput(name="AvailableTargets", type=Option[].class),
        @HandlerOutput(name="SelectedTargets", type=String[].class) })
    public static void getTargetsOptionsList(HandlerContext handlerCtx) {
        AMXRoot amxRoot = AMXRoot.getInstance();
        Set standaloneSet = amxRoot.getServersConfig().getStandaloneServerConfigMap().keySet();
        Set clusteredSet = amxRoot.getClustersConfig().getClusterConfigMap().keySet();
        ArrayList serverList = new ArrayList(standaloneSet);
        serverList.addAll(clusteredSet);
        Option[] availableOptions = null;
        if(serverList != null) {
            availableOptions = new Option[serverList.size()];
            List<String> strList = GuiUtil.convertListOfStrings(serverList);
// FIXME: 7-31-08 -- FIX by importing woodstock api's:
//            availableOptions = GuiUtil.getSunOptions(strList);
        }
        
        String defaultTarget = (String)handlerCtx.getInputValue("defaultTarget");
        if (defaultTarget == null) {
            defaultTarget = "server";
        }
        String [] selected = { defaultTarget };
        if (defaultTarget.trim().equals("")) {
            selected = new String[0];
        }
        handlerCtx.setOutputValue("SelectedTargets", selected);
        handlerCtx.setOutputValue("AvailableTargets", availableOptions);
    }

    
    /**
     *	<p> Returns the options list to add in addRemove component
     *
     *  <p> Input value: "allTargets" -- Type: <code>List</code>
     *          - the list to be added to the field.</p>
     *          - <code>java.util.List</code> 
     *      The type specified must be either:  application, resource, loadbalancer
     *  <p> Output value: "optionsList" -- Type: <code>Option</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getTargetOptions",
    	input={
	    @HandlerInput(name="allTargets", type=List.class, required=true),
	    @HandlerInput(name="type", type=String.class, required=true),
	    @HandlerInput(name="name", type=String.class, required=true)},
        output={
            @HandlerOutput(name="availableTargets", type=Option[].class),
            @HandlerOutput(name="selectedTargets", type=String[].class)})
    public static void getTargetOptions(HandlerContext handlerCtx) {
		String type = (String)handlerCtx.getInputValue("type");
		String name = (String)handlerCtx.getInputValue("name");
                List targets = (List)handlerCtx.getInputValue("allTargets");
                List<String> associatedTargets = new ArrayList();
                if(type.equals("loadBalancer")){
                    //TODO-V3
                    //String[] targetsArray = AMXRoot.getInstance().getLBConfigHelper().listTargets(name); 
                    //if (targetsArray != null){
                    //   for(int i=0; i < targetsArray.length; i++)
                    //      associatedTargets.add(targetsArray[i]);
                    //}
                }else{
                    boolean isApp = type.equals("application") ? true:false;
                    associatedTargets = TargetUtil.getDeployedTargets(name, isApp);
                }
                
		Option[] availableOptions = null; 
		String[] selectedOptions = null; 
		if(targets != null) {
			availableOptions = new Option[targets.size()];
			List<String> strList = GuiUtil.convertListOfStrings(targets);
// FIXME: 7-31-08 -- FIX by importing woodstock api's:
//			availableOptions = GuiUtil.getSunOptions(strList);
			selectedOptions = associatedTargets.toArray(new String[associatedTargets.size()]);
		}
        handlerCtx.setOutputValue("availableTargets", availableOptions);        
        handlerCtx.setOutputValue("selectedTargets", selectedOptions);        
    }

    /**
     *	<p> Returns the options list to add in addRemove component
     *
     *  <p> Input value: "allTargets" -- Type: <code>List</code>
     *          - the list to be added to the field.</p>
     *          - <code>java.util.List</code> 
     *  <p> Output value: "optionsList" -- Type: <code>Option</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getVSOptions", 
    input = {
        @HandlerInput(name = "vsList", type = List.class, required = true),
        @HandlerInput(name = "PE", type = Boolean.class),
        @HandlerInput(name = "targetName", type = String.class, required = true),
        @HandlerInput(name = "name", type = String.class, required = true)}, 
    output = {
        @HandlerOutput(name = "availableVS", type = List.class),
        @HandlerOutput(name = "selectedVS", type = String[].class)
    })
    public static void getVSOptions(HandlerContext handlerCtx) {
        String targetName = (String) handlerCtx.getInputValue("targetName");
        Boolean pe = (Boolean) handlerCtx.getInputValue("PE");
        String name = (String) handlerCtx.getInputValue("name");
        List<String> vsList = (List) handlerCtx.getInputValue("vsList");
        String associatedVS = TargetUtil.getAssociatedVS(name, targetName);
        String[] selectedOptions = null;
        if (vsList != null) {
            selectedOptions = GuiUtil.stringToArray(associatedVS, ",");
            if (pe != null && pe) {
                if (selectedOptions != null && !(selectedOptions.length > 0)) {
                    //None is selected by default
                    selectedOptions = new String[]{vsList.get(0)};
                }
            }
        }
        handlerCtx.setOutputValue("availableVS", vsList);
        handlerCtx.setOutputValue("selectedVS", selectedOptions);
    }

     /**
     * <p> This handler returns a list of configuration available for table list.
     */
    /**
     * TODO-V3
    @Handler(id="getConfigurationsTableList",
    input={
        @HandlerInput(name="selectedRows", type=List.class)},
    output={
        @HandlerOutput(name="Result", type=java.util.List.class) })
    public static void getConfigurationsTableList(HandlerContext handlerCtx) {
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
        Iterator iter = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().values().iterator();
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                ConfigConfig config = (ConfigConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = config.getName();
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? GuiUtil.isSelected(name, selectedList): false);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("Result", result);
    }
     * 
     */ 
    
    /**
     *	<p> This handler returns the list of targets for populating the table.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getConfigurationTargets",
           input={
            @HandlerInput(name="ConfigName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getConfigurationTargets(HandlerContext handlerCtx){
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        List result = new ArrayList();
        AMXRoot amxRoot = AMXRoot.getInstance();
        try{
            if(configName != null){
                Vector targets = getReferencedInstances(configName);
                for(int i=0; i<targets.size(); i++){
                    HashMap oneRow = new HashMap();
                    ServerConfig servConfig = (ServerConfig)targets.get(i);
                    String name = servConfig.getName();
                    oneRow.put("targetname", name);
                    oneRow.put("image", amxRoot.getStatusForDisplay(
                                amxRoot.getJ2EEDomain().getJ2EEServerMap().get(name), false));
                    if(servConfig instanceof StandaloneServerConfig){
                        oneRow.put("isCluster", false);
                    }else{
                        oneRow.put("isCluster", true);
                    }    
                    result.add(oneRow);
                }
            }
            handlerCtx.setOutputValue("result", result);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static Vector getReferencedInstances(String configName){
        Vector targets = new Vector();
        AMXRoot amxRoot = AMXRoot.getInstance();
        Map standaloneMap = amxRoot.getServersConfig().getStandaloneServerConfigMap(); 
        Map clusteredMap = amxRoot.getServersConfig().getClusteredServerConfigMap();
        for(Iterator it = standaloneMap.values().iterator(); it.hasNext();){
            StandaloneServerConfig server = (StandaloneServerConfig) it.next();
            String config = server.getConfigRef();
            if(config.equalsIgnoreCase(configName)){
                targets.add(server);
            }
        }
        for(Iterator it = clusteredMap.values().iterator(); it.hasNext();){
            ClusteredServerConfig server = (ClusteredServerConfig) it.next();
            String config = server.getConfigRef();
            if(config.equalsIgnoreCase(configName)){
                targets.add(server);
            }
        }
        
        return targets;
    }
    
    /**
        Return a Map<String,String> of all system properties in the specified config.
     */
    private static Map<String,String> getSystemProperties( final SystemPropertiesAccess config ) {
        final Map<String,SystemPropertyConfig> systemPropertyConfigMap = config.getSystemPropertyConfigMap();
        
        Map<String,String> props = null;
        if ( systemPropertyConfigMap.size() != 0 ) {
            props = new HashMap<String,String>();
            for( final SystemPropertyConfig prop : systemPropertyConfigMap.values() ) {
                props.put( prop.getName(), prop.getValue() );
            }
        }
        return props;
    }
    
   /**
     * <p> This handler returns the configs attributes.
     * <p> Input value: "ConfigName"               -- Type: <code>java.lang.String</code></p>
     * <p> Output value: "DynamicReconfig"         -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "Properties"              -- Type: <code>java.util.Map</code></p>
     * <p> Output value: "TableList"               -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="getSystemProperties",
    input={
        @HandlerInput(name="ConfigName",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="DynamicReconfig",  type=Boolean.class),
        @HandlerOutput(name="Properties",       type=java.util.Map.class),
        @HandlerOutput(name="TableList",        type=java.util.List.class) })
    public static void getSystemProperties(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        
        handlerCtx.setOutputValue("DynamicReconfig", config.getDynamicReconfigurationEnabled());
        List data = new ArrayList();
        final Map<String, String> props = getSystemProperties(config);	
        if(props != null ){
            for(String key : props.keySet()){
                HashMap oneRow = new HashMap();
                Object value = props.get(key);
                String valString = (value==null)? "" : value.toString();
                oneRow.put("name", key);
                oneRow.put("value", valString);
                oneRow.put("selected", false);
                data.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("Properties", props);
        handlerCtx.setOutputValue("TableList", data);
    }
    
    /**
     * <p> This handler returns the configs attributes.
     * <p> Input value: "ConfigName"               -- Type: <code>java.lang.String</code></p>
     * <p> Output value: "DynamicReconfig"         -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "AddProps"                -- Type: <code>java.util.Map</code></p>
     * <p> Output value: "RemoveProps"             -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="saveSystemProperties",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),
        @HandlerInput(name="DynamicReconfig",   type=Boolean.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
    public static void saveSystemProperties(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        config.setDynamicReconfigurationEnabled("" + handlerCtx.getInputValue("DynamicReconfig"));
        Map<String,String> addProps = (Map)handlerCtx.getInputValue("AddProps");
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        if(removeProps != null){
            String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
            for(int i=0; i<remove.length; i++){
                config.removeSystemPropertyConfig(remove[i]);
            }
        }
        if(addProps != null ){
            for(String key: addProps.keySet()){
                String value = addProps.get(key);
                if (GuiUtil.isEmpty(value))
                    continue;
                if (config.getSystemPropertyConfigMap().containsKey(key))
                    config.getSystemPropertyConfigMap().get(key).setValue(value);
                else
                    config.createSystemPropertyConfig(key, value);
            }
        }
    }
    
    /**
     * <p> This handler returns the configs attributes.
     * <p> Input value: "ConfigName"               -- Type: <code>java.lang.String</code></p>
     * <p> Output value: "DynamicReconfig"         -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "SystemProperties"        -- Type: <code>java.util.Map</code></p>
     */
    @Handler(id="getInstanceValues",
    input={
        @HandlerInput(name="ConfigName",   type=String.class, required=true),
        @HandlerInput(name="PropertyName",     type=String.class, required=true)},
    output={
        @HandlerOutput(name="InstancesList",        type=java.util.List.class) })
    public static void getInstancevalues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        String propName = (String) handlerCtx.getInputValue("PropertyName");
        Vector instances = getReferencedInstances(configName);
        List data = new ArrayList();
        for(int i=0; i<instances.size(); i++){
            HashMap oneRow = new HashMap();
            ServerConfig servConfig = (ServerConfig)instances.get(i);
            String name = servConfig.getName();
            String propValue = ""; //NOI18N
            if (servConfig.getSystemPropertyConfigMap().containsKey(propName))
                propValue = servConfig.getSystemPropertyConfigMap().get(propName).getValue();
            if(servConfig instanceof ClusteredServerConfig){
                String clusterName = getClusterForServer(name);
                oneRow.put("clusterName", clusterName);
                oneRow.put("isCluster", true);
            } else {
                oneRow.put("clusterName", "");
                oneRow.put("isCluster", false);                
            }
            oneRow.put("name", name);
            oneRow.put("value", propValue);
            data.add(oneRow);
        }
        handlerCtx.setOutputValue("InstancesList", data);
    }
    
    /**
     * <p> This handler saves the system properties
     * <p> Input value: "ConfigName"           -- Type: <code>java.lang.String</code></p>
     * <p> Input value: "PropertyName"         -- Type: <code>java.lang.String</code></p>
     * <p> Input value: "PropsList"            -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="saveInstanceValues",
    input={
        @HandlerInput(name="ConfigName",       type=String.class, required=true),
        @HandlerInput(name="PropertyName",     type=String.class, required=true),
        @HandlerInput(name="PropsList",          type=List.class, required=true) })
    public static void saveInstanceValues(HandlerContext handlerCtx) {
        String propName = (String) handlerCtx.getInputValue("PropertyName");
        AMXRoot amxRoot = AMXRoot.getInstance();
        try{
            List obj = (List) handlerCtx.getInputValue("PropsList");
            List<Map> newRows = (List) obj;            
            for(Map oneRow : newRows){
                String instanceName = (String)oneRow.get("name");
                boolean isCluster = ((Boolean)oneRow.get("isCluster")).booleanValue();
                String propValue = (String)oneRow.get("value");
                if(isCluster){
                    ClusteredServerConfig clusterConfig = amxRoot.getServersConfig().getClusteredServerConfigMap().get(instanceName);
                    if (clusterConfig.getSystemPropertyConfigMap().containsKey(propName)){
                        clusterConfig.getSystemPropertyConfigMap().get(propName).setValue(propValue);
                    }else{
                        clusterConfig.createSystemPropertyConfig(propName, propValue);
                    }
                }else{
                    StandaloneServerConfig serverConfig = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(instanceName);
                    if (serverConfig.getSystemPropertyConfigMap().containsKey(propName)){
                        serverConfig.getSystemPropertyConfigMap().get(propName).setValue(propValue);
                    }else{
                        serverConfig.createSystemPropertyConfig(propName, propValue);
                    }
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
    /**
     *	<p> This handler creates a new configuration
     *	@param	handlerCtx	The HandlerContext.
     *  <p> Input value: "Name"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Config"     -- Type: <code>java.lang.String</code></p>
     */
    @Handler(id="createConfiguration",
    input={
        @HandlerInput(name="Name",       type=String.class, required=true),
        @HandlerInput(name="Config",     type=String.class, required=true) })
        public static void createConfiguration(HandlerContext handlerCtx) {
        String configurationName = (String)handlerCtx.getInputValue("Name");
        String copyFromConfigName = (String)handlerCtx.getInputValue("Config");
        AMXRoot amxRoot = AMXRoot.getInstance();
        try {
            HashMap configMap = new HashMap();
            if("default-config".equals(copyFromConfigName)) {
                amxRoot.getConfigsConfig().createConfigConfig(configurationName, configMap);
            }else{
                configMap.put(ConfigConfigKeys.SRC_CONFIG_NAME_KEY, copyFromConfigName);
                amxRoot.getConfigsConfig().createConfigConfig(configurationName, configMap);
            }
         }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler deletes all selected clustered server instances.
     *	@param	handlerCtx	The HandlerContext.
     *  <p> Input value: "selectedRows"       -- Type: <code>java.util.</code></p>
     */
    @Handler(id="deleteConfigurations",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true)}
    )
    public static void deleteConfigurations(HandlerContext handlerCtx){
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                /*Vector targets = getReferencedInstances(name);
                for(int i=0; i<targets.size(); i++){
                    ServerConfig servConfig = (ServerConfig)targets.get(i);
                    AMXRoot.getInstance().getDomainConfig().
                }*/
                AMXRoot.getInstance().getConfigsConfig().removeConfigConfig(name);
            }    
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    public static String getClusterForServer(String clusteredInstance){
        Map clusterConfMap = AMXRoot.getInstance().getClustersConfig().getClusterConfigMap();
        String clusterName = "";
        for(Iterator it = clusterConfMap.values().iterator(); it.hasNext();){
            ClusterConfig clusterConf = (ClusterConfig) it.next();
            clusterName = clusterConf.getName();
            if(clusterConf.getClusteredServerConfigMap().containsKey(clusteredInstance)){
                return clusterName;
            }
        }
        return clusterName;
    }
}
