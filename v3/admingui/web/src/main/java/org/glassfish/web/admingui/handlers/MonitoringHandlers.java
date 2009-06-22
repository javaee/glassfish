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

package org.glassfish.web.admingui.handlers;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.flashlight.datatree.MethodInvoker;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 *
 * @author Jennifer
 */
public class MonitoringHandlers {
    
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getVirtualServers",
        input={
            @HandlerInput(name="serverName", type=String.class, required=true),
            @HandlerInput(name="application", type=String.class, required=true)},
        output={
            @HandlerOutput(name="virtualServers", type=java.util.List.class),
            @HandlerOutput(name="firstVirtualServer", type=String.class)}
    )
    public void getVirtualServers(HandlerContext handlerCtx) {
        String serverName = (String)handlerCtx.getInputValue("serverName");
        String application = (String)handlerCtx.getInputValue("application");
        List dataList = new ArrayList();
        if (application.equals("All")) {
            dataList.add("All");
        } else {
            if (serverRoot != null) {
                TreeNode server = serverRoot.getNode("applications."+application);
                if (server != null && server.isEnabled()) {
                    Collection<TreeNode> coll = server.getChildNodes();
                    for (TreeNode tn : coll) {
                        dataList.add(tn.getName());
                    }
                }
            }
        }
        handlerCtx.setOutputValue("virtualServers", dataList);
        if (!dataList.isEmpty()) {
            handlerCtx.setOutputValue("firstVirtualServer", (String)dataList.get(0));
        }
    }
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getWebStats",
        input={
            @HandlerInput(name="serverName", type=String.class, required=true),
            @HandlerInput(name="application", type=String.class, required=true),
            @HandlerInput(name="virtualServer", type=String.class, required=false),
            @HandlerInput(name="statNames", type=List.class, required=false), 
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="test", type=String.class, required=false)},
        output={
            @HandlerOutput(name="stats", type=java.util.List.class)}
    )
    public void getWebStats(HandlerContext handlerCtx) {
        String serverName = (String)handlerCtx.getInputValue("serverName");
        String application = (String)handlerCtx.getInputValue("application");
        String virtualServer = (String)handlerCtx.getInputValue("virtualServer");
        List<String> statNames = (List)handlerCtx.getInputValue("statNames");
        String type = (String)handlerCtx.getInputValue("type");
        String test = (String)handlerCtx.getInputValue("test");
        TreeNode statsNode = null;
        if (serverRoot != null) { 
            if (application == null || application.equals("All")) {
                statsNode = serverRoot.getNode("web."+type);            
            } else {
                statsNode = serverRoot.getNode("applications."+application+"."+virtualServer);
            }
        }
        
        Collection<TreeNode> coll = null;
        List dataList = new ArrayList();        
        if (statsNode != null  && statsNode.isEnabled()) {
            if (application == null || application.equals("All")) {
                coll = statsNode.getChildNodes();
            } else {
                if (type.equals("request")) {
                    coll = new ArrayList();
                    for (String s : statNames) {
                        TreeNode tn = serverRoot.getNode("applications."+application+"."+virtualServer+"."+s);
                        if (tn != null) {
                            coll.add(tn);
                        }
                    }
                } else {
                    String ype = type.substring(1);
                    String t = type.substring(0,1);
                    String T = t.toUpperCase();
                    coll = statsNode.getNodes("*applications."+application+"."+virtualServer+"."+"*"+"["+t+T+"]"+ype+"*");
                }
            }
            ArrayList list = new ArrayList();
            list.addAll(coll);
            Collections.sort(list);
            for (Object o : list) {
                TreeNode tn = (TreeNode)o;
                Map statMap = new HashMap();
                statMap.put("Name", tn.getName());
                statMap.put("Value", tn.getValue());
                statMap.put("ToolTip", "");
                if (tn instanceof Counter) {
                    statMap.put("ToolTip", ((Counter)tn).getDescription());
                }
                dataList.add(statMap);
            }
        }
        handlerCtx.setOutputValue("stats", dataList);
    }
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStats",
        input={
            @HandlerInput(name="serverName", type=String.class, required=true),
            @HandlerInput(name="node", type=String.class, required=true),
            @HandlerInput(name="test", type=String.class, required=false)},
        output={
            @HandlerOutput(name="stats", type=java.util.List.class)}
    )
    public void getStats(HandlerContext handlerCtx) {
        String serverName = (String)handlerCtx.getInputValue("serverName");
        String node = (String)handlerCtx.getInputValue("node");
        if (node != null && node.endsWith("All")) {
            node = node.replace("All", "request");
        }
        String test = (String)handlerCtx.getInputValue("test");
        List dataList = new ArrayList();        
        if (serverRoot != null) {
            TreeNode statsNode = serverRoot.getNode(node);
            Collection<TreeNode> coll = null;
            if (statsNode != null && statsNode.isEnabled()) {
                coll = statsNode.getChildNodes();
                ArrayList list = new ArrayList();
                list.addAll(coll);
                Collections.sort(list);
                for (Object o : list) {
                    TreeNode tn = (TreeNode)o;
                    Map statMap = new HashMap();
                    statMap.put("Name", tn.getName());
                    statMap.put("Value", tn.getValue());
                    statMap.put("ToolTip", "");
                    if (tn instanceof Counter) {
                        statMap.put("ToolTip", ((Counter)tn).getDescription());
                    }
                    dataList.add(statMap);
                }
            }
        }
        handlerCtx.setOutputValue("stats", dataList);
    }
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getJvmStats",
        input={
            @HandlerInput(name="serverName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="jvmStats", type=java.util.List.class)}
    )
    public void getJvmStats(HandlerContext handlerCtx) {
        String serverName = (String)handlerCtx.getInputValue("serverName");
        List dataList = new ArrayList();        
        if (serverRoot != null) {
            MethodInvoker tn = (MethodInvoker) (serverRoot.getNode("jvm")).getNode("committedHeapSize");
            MemoryUsage mu = (MemoryUsage) tn.getInstance();
            Map statMap = new HashMap();
            statMap.put("Name", "init");
            statMap.put("Value", mu.getInit());
            statMap.put("ToolTip", GuiUtil.getMessage(RESOURCE_NAME, "monitoring.jvm.init.tooltip"));
            dataList.add(statMap);
            statMap = new HashMap();
            statMap.put("Name", "used");
            statMap.put("Value", mu.getUsed());
            statMap.put("ToolTip", GuiUtil.getMessage(RESOURCE_NAME, "monitoring.jvm.used.tooltip"));
            dataList.add(statMap);
            statMap = new HashMap();
            statMap.put("Name", "committed");
            statMap.put("Value", mu.getCommitted());
            statMap.put("ToolTip", GuiUtil.getMessage(RESOURCE_NAME, "monitoring.jvm.committed.tooltip"));
            dataList.add(statMap);
            statMap = new HashMap();
            statMap.put("Name", "max");
            statMap.put("Value", mu.getMax());
            statMap.put("ToolTip", GuiUtil.getMessage(RESOURCE_NAME, "monitoring.jvm.max.tooltip"));
            dataList.add(statMap);
        }
        handlerCtx.setOutputValue("jvmStats", dataList);
    }
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getChildNodes",
        input={
            @HandlerInput(name="node", type=String.class, required=true),
            @HandlerInput(name="category", type=String.class, required=false),
            @HandlerInput(name="firstInList", type=String.class, required=false)},
        output={
            @HandlerOutput(name="childNodes", type=java.util.List.class),
            @HandlerOutput(name="firstItem", type=String.class)}
    )
    public void getChildNodes(HandlerContext handlerCtx) {
        String node = (String)handlerCtx.getInputValue("node");
        String category = (String)handlerCtx.getInputValue("category");
        String firstInList = (String)handlerCtx.getInputValue("firstInList");
        List dataList = new ArrayList();
        TreeNode treeNode = null;
        if (serverRoot != null) {
            treeNode = serverRoot.getNode(node);
        }
        if (treeNode != null && treeNode.isEnabled()) {
            Collection<TreeNode> coll = treeNode.getChildNodes();
            for (TreeNode tn : coll) {
                if (category != null) {
                    if (tn.getCategory().equals(category)) {
                        dataList.add(tn.getName());
                    }
                } else {
                    dataList.add(tn.getName());
                }
            }
            if (firstInList != null) {
                dataList.add(0, firstInList);
            }
        }
        handlerCtx.setOutputValue("childNodes", dataList);
        if (!dataList.isEmpty()) {
            handlerCtx.setOutputValue("firstItem", dataList.get(0));
        }
    }
    
    private TreeNode setupTree (){
        TreeNode serverRoot = TreeNodeFactory.createTreeNode ("server", this, "server");
        
        /* applications */
        
        TreeNode applications = TreeNodeFactory.createTreeNode("applications", this, "applications");
        
        TreeNode webApp1 = TreeNodeFactory.createTreeNode ("webApp1", this, "application");
        TreeNode webApp2 = TreeNodeFactory.createTreeNode ("webApp2", this, "application");
        TreeNode webApp3 = TreeNodeFactory.createTreeNode ("webApp3", this, "application");
        TreeNode server11 = TreeNodeFactory.createTreeNode ("server11", this, "server");
        TreeNode server12 = TreeNodeFactory.createTreeNode ("server12", this, "server");
        TreeNode server13 = TreeNodeFactory.createTreeNode ("server13", this, "server");
        TreeNode server21 = TreeNodeFactory.createTreeNode ("server21", this, "server");
        TreeNode server22 = TreeNodeFactory.createTreeNode ("server22", this, "server");
        TreeNode server23 = TreeNodeFactory.createTreeNode ("server23", this, "server");
        
        Counter counter111 = CounterFactory.createCount(10);        
        counter111.setName("11activesessionscurrent-count");
        server11.addChild((TreeNode)counter111);
        Counter counter112 = CounterFactory.createCount(9);        
        counter112.setName("11activesessionshigh-count");
        server11.addChild((TreeNode)counter112);
        Counter counter113 = CounterFactory.createCount(5);        
        counter113.setName("11expiredsessionstotal-count");
        server11.addChild((TreeNode)counter113);
        Counter counter114 = CounterFactory.createCount(6);        
        counter114.setName("11rejectedsessionstotal-count");
        server11.addChild((TreeNode)counter114);
        Counter counter115 = CounterFactory.createCount(7);        
        counter115.setName("11sessionstotal-count");
        server11.addChild((TreeNode)counter115);
        
        Counter reqcountjsp11 = CounterFactory.createCount(10);        
        reqcountjsp11.setName("11jsp1requestcount");
        server11.addChild((TreeNode)reqcountjsp11);
        Counter proctimejsp11 = CounterFactory.createCount(9);        
        proctimejsp11.setName("11jsp1processingtime");
        server11.addChild((TreeNode)proctimejsp11);
        Counter servicetimejsp11 = CounterFactory.createCount(5);        
        servicetimejsp11.setName("11jsp1servicingtime");
        server11.addChild((TreeNode)servicetimejsp11);
        
        webApp1.addChild(server11);
        
        Counter reqcountjsp12 = CounterFactory.createCount(10);        
        reqcountjsp12.setName("jsp12requestcount");
        server12.addChild((TreeNode)reqcountjsp12);
        Counter proctimejsp12 = CounterFactory.createCount(9);        
        proctimejsp12.setName("jsp12processingtime");
        server12.addChild((TreeNode)proctimejsp12);
        Counter servicetimejsp12 = CounterFactory.createCount(5);        
        servicetimejsp12.setName("jsp12servicingtime");
        server12.addChild((TreeNode)servicetimejsp12);
        
        webApp1.addChild(server12);
        
        Counter reqcountjsp23 = CounterFactory.createCount(10);        
        reqcountjsp23.setName("jsp23requestcount");
        server23.addChild((TreeNode)reqcountjsp23);
        Counter proctimejsp23 = CounterFactory.createCount(9);        
        proctimejsp23.setName("jsp23processingtime");
        server23.addChild((TreeNode)proctimejsp23);
        Counter servicetimejsp23 = CounterFactory.createCount(5);        
        servicetimejsp23.setName("jsp23servicingtime");
        server23.addChild((TreeNode)servicetimejsp23);
       
        webApp2.addChild(server23);
         
        Counter counter211 = CounterFactory.createCount(210);
        counter211.setName("21activesessionscurrent-count");
        server21.addChild((TreeNode)counter211);
        Counter counter212 = CounterFactory.createCount(29);        
        counter212.setName("21activesessionshigh-count");
        server21.addChild((TreeNode)counter212);
        Counter counter213 = CounterFactory.createCount(25);        
        counter213.setName("21expiredsessionstotal-count");
        server21.addChild((TreeNode)counter213);
        Counter counter214 = CounterFactory.createCount(26);
        counter214.setName("21rejectedsessionstotal-count");
        server21.addChild((TreeNode)counter214);
        Counter counter215 = CounterFactory.createCount(27);        
        counter215.setName("21sessionstotal-count");
        server21.addChild((TreeNode)counter215);
        
        webApp2.addChild(server21);
        
        applications.addChild(webApp1);
        applications.addChild(webApp2);
        applications.addChild(webApp3);
     
        serverRoot.addChild(applications);
        
        /* web */
        
        TreeNode web = TreeNodeFactory.createTreeNode("web", this, "web");
        TreeNode session = TreeNodeFactory.createTreeNode("session", this, "session");
        TreeNode servlet = TreeNodeFactory.createTreeNode("servlet", this, "servlet");
        TreeNode jsp = TreeNodeFactory.createTreeNode("jsp", this, "jsp");
        TreeNode request = TreeNodeFactory.createTreeNode("request", this, "request");
        
        
        Counter sessionStat1 = CounterFactory.createCount(210);
        sessionStat1.setName("Totalactivesessionscurrent-count");
        session.addChild((TreeNode)sessionStat1);
        Counter sessionStat2 = CounterFactory.createCount(29);        
        sessionStat2.setName("Totalactivesessionshigh-count");
        session.addChild((TreeNode)sessionStat2);
        Counter sessionStat3 = CounterFactory.createCount(25);        
        sessionStat3.setName("Totalexpiredsessionstotal-count");
        session.addChild((TreeNode)sessionStat3);
        
        Counter servletStat1 = CounterFactory.createCount(210);
        servletStat1.setName("Totalactiveservletscurrent-count");
        servlet.addChild((TreeNode)servletStat1);
        Counter servletStat2 = CounterFactory.createCount(29);        
        servletStat2.setName("Totalactiveservletshigh-count");
        servlet.addChild((TreeNode)servletStat2);
        Counter servletStat3 = CounterFactory.createCount(25);        
        servletStat3.setName("Totalexpiredservletstotal-count");
        servlet.addChild((TreeNode)servletStat3);
        
        Counter jspStat1 = CounterFactory.createCount(210);
        jspStat1.setName("Totalactivejspscurrent-count");
        jsp.addChild((TreeNode)jspStat1);
        Counter jspStat2 = CounterFactory.createCount(29);        
        jspStat2.setName("Totalactivejspshigh-count");
        jsp.addChild((TreeNode)jspStat2);
        Counter jspStat3 = CounterFactory.createCount(25);        
        jspStat3.setName("Totalexpiredjspstotal-count");
        jsp.addChild((TreeNode)jspStat3);
        
        Counter requestStat1 = CounterFactory.createCount(210);
        requestStat1.setName("Totalactiverequestscurrent-count");
        request.addChild((TreeNode)requestStat1);
        Counter requestStat2 = CounterFactory.createCount(29);        
        requestStat2.setName("Totalactiverequestshigh-count");
        request.addChild((TreeNode)requestStat2);
        Counter requestStat3 = CounterFactory.createCount(25);        
        requestStat3.setName("Totalexpiredrequestsstotal-count");
        request.addChild((TreeNode)requestStat3);
        
        web.addChild(session);
        web.addChild(servlet);
        web.addChild(jsp);
        web.addChild(request);
        serverRoot.addChild(web);
        
        /* http-service */
        TreeNode httpService = TreeNodeFactory.createTreeNode("http-service", this, "http-service");
        
        TreeNode virtualServer1 = TreeNodeFactory.createTreeNode("virtual-server1", this, "virtual-server");
        TreeNode virtualServer2 = TreeNodeFactory.createTreeNode("virtual-server2", this, "virtual-server");
        
        TreeNode hsrequest1 = TreeNodeFactory.createTreeNode("request", this, "request");
        TreeNode hsrequest2 = TreeNodeFactory.createTreeNode("request", this, "request");
        
        Counter hsrequestStat11 = CounterFactory.createCount(210);
        Counter hsrequestStat12 = CounterFactory.createCount(210);
        Counter hsrequestStat13 = CounterFactory.createCount(210);
        Counter hsrequestStat21 = CounterFactory.createCount(210);
        Counter hsrequestStat22 = CounterFactory.createCount(210);
        Counter hsrequestStat23 = CounterFactory.createCount(210);
        hsrequestStat11.setName("hsrequestStat11");
        hsrequestStat12.setName("hsrequestStat12");
        hsrequestStat13.setName("hsrequestStat13");
        hsrequestStat21.setName("hsrequestStat21");
        hsrequestStat22.setName("hsrequestStat22");
        hsrequestStat23.setName("hsrequestStat23");
        
        hsrequest1.addChild(hsrequestStat11);
        hsrequest1.addChild(hsrequestStat12);
        hsrequest1.addChild(hsrequestStat13);
        hsrequest2.addChild(hsrequestStat21);
        hsrequest2.addChild(hsrequestStat22);
        hsrequest2.addChild(hsrequestStat23);
        
        virtualServer1.addChild(hsrequest1);
        virtualServer2.addChild(hsrequest2);
        
        TreeNode httpListener11 = TreeNodeFactory.createTreeNode("http-listener11", this, "http-listener");
        TreeNode httpListener12 = TreeNodeFactory.createTreeNode("http-listener12", this, "http-listener");
        TreeNode httpListener21 = TreeNodeFactory.createTreeNode("http-listener21", this, "http-listener");
        TreeNode httpListener22 = TreeNodeFactory.createTreeNode("http-listener22", this, "http-listener");
        
        Counter hlrequestStat111 = CounterFactory.createCount(210);
        Counter hlrequestStat112 = CounterFactory.createCount(210);
        Counter hlrequestStat113 = CounterFactory.createCount(210);
        Counter hlrequestStat121 = CounterFactory.createCount(210);
        Counter hlrequestStat122 = CounterFactory.createCount(210);
        Counter hlrequestStat123 = CounterFactory.createCount(210);
        hlrequestStat111.setName("hlrequestStat111");
        hlrequestStat112.setName("hlrequestStat112");
        hlrequestStat113.setName("hlrequestStat113");
        hlrequestStat121.setName("hlrequestStat121");
        hlrequestStat122.setName("hlrequestStat122");
        hlrequestStat123.setName("hlrequestStat123");
        
        Counter hlrequestStat211 = CounterFactory.createCount(210);
        Counter hlrequestStat212 = CounterFactory.createCount(210);
        Counter hlrequestStat213 = CounterFactory.createCount(210);
        Counter hlrequestStat221 = CounterFactory.createCount(210);
        Counter hlrequestStat222 = CounterFactory.createCount(210);
        Counter hlrequestStat223 = CounterFactory.createCount(210);
        hlrequestStat211.setName("hlrequestStat211");
        hlrequestStat212.setName("hlrequestStat212");
        hlrequestStat213.setName("hlrequestStat213");
        hlrequestStat221.setName("hlrequestStat221");
        hlrequestStat222.setName("hlrequestStat222");
        hlrequestStat223.setName("hlrequestStat223");
        
        httpListener11.addChild(hlrequestStat111);
        httpListener11.addChild(hlrequestStat112);
        httpListener11.addChild(hlrequestStat113);
        httpListener12.addChild(hlrequestStat121);
        httpListener12.addChild(hlrequestStat122);
        httpListener12.addChild(hlrequestStat123);
        
        httpListener21.addChild(hlrequestStat211);
        httpListener21.addChild(hlrequestStat212);
        httpListener21.addChild(hlrequestStat213);
        httpListener22.addChild(hlrequestStat221);
        httpListener22.addChild(hlrequestStat222);
        httpListener22.addChild(hlrequestStat223);
        
        virtualServer1.addChild(httpListener11);
        virtualServer1.addChild(httpListener12);
        
        virtualServer2.addChild(httpListener21);
        virtualServer2.addChild(httpListener22);
        
        httpService.addChild(virtualServer1);
        httpService.addChild(virtualServer2);
        
        TreeNode threadPool = TreeNodeFactory.createTreeNode("thread-pool", this, "thread-pool");
        Counter threadPoolStat1 = CounterFactory.createCount(210);
        Counter threadPoolStat2 = CounterFactory.createCount(210);
        Counter threadPoolStat3 = CounterFactory.createCount(210);
        threadPoolStat1.setName("threadPoolStat1");
        threadPoolStat2.setName("threadPoolStat2");
        threadPoolStat3.setName("threadPoolStat3");
        threadPool.addChild(threadPoolStat1);
        threadPool.addChild(threadPoolStat2);
        threadPool.addChild(threadPoolStat3);
        
        httpService.addChild(threadPool);
        
        TreeNode connectionQueue = TreeNodeFactory.createTreeNode("connection-queue", this, "connection-queue");
        Counter connectionQueueStat1 = CounterFactory.createCount(210);
        Counter connectionQueueStat2 = CounterFactory.createCount(210);
        Counter connectionQueueStat3 = CounterFactory.createCount(210);
        connectionQueueStat1.setName("connectionQueueStat1");
        connectionQueueStat2.setName("connectionQueueStat2");
        connectionQueueStat3.setName("connectionQueueStat3");
        
        connectionQueue.addChild(connectionQueueStat1);
        connectionQueue.addChild(connectionQueueStat2);
        connectionQueue.addChild(connectionQueueStat3);
        
        httpService.addChild(connectionQueue);
        
        serverRoot.addChild(httpService);
        
        /* jvm */
        TreeNode jvm = TreeNodeFactory.createTreeNode("jvm", this, "jvm");
        
        Counter jvmStat1 = CounterFactory.createCount(210);
        Counter jvmStat2 = CounterFactory.createCount(210);
        Counter jvmStat3 = CounterFactory.createCount(210);
        jvmStat1.setName("jvmStat1");
        jvmStat2.setName("jvmStat2");
        jvmStat3.setName("jvmStat3");
        jvm.addChild(jvmStat1);
        jvm.addChild(jvmStat2);
        jvm.addChild(jvmStat3);
        serverRoot.addChild(jvm);
        
        return serverRoot;
    }
    
    //private TreeNode serverRoot = setupTree();
    private TreeNode serverRoot = GuiUtil.getHabitat().getComponent(MonitoringRuntimeDataRegistry.class).get("server");
    private static final String RESOURCE_NAME = "org.glassfish.web.admingui.Strings";
}
