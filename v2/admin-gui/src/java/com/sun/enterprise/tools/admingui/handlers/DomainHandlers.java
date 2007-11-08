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
 * DomainHandlers.java
 *
 * Created on August 31, 2006, 4:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.NodeAgentConfig;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jennifer Chou
 */
public class DomainHandlers {
    
    /** Creates a new instance of DomainHandlers */
    public DomainHandlers() {
    }
    
    
    /**
     * <p> This handler returns the DomainConfig MBean </p>
     *
     * <p> Output value: "DomainConfig" -- Type: 
     *      <code>com.sun.appserv.management.config.DomainConfig</code>
     * @param  context The HandlerContext.
     */
    @Handler(id="getDomainConfigMBean",
        output={
            @HandlerOutput(name="DomainConfigMBean", 
                type=com.sun.appserv.management.config.DomainConfig.class)})
    public static void getDomainConfig(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("DomainConfigMBean", AMXUtil.getDomainConfig());
    }
    
 
    /**
     * <p> This handler returns value of domain property administrative.domain.name  </p>
     *
     * <p> Output value: "AdminDomainName" -- Type: <code>String</code>
     * @param  context The HandlerContext.
     */
    @Handler(id="getAdminDomainName",
        output={
            @HandlerOutput(name="AdminDomainName", type=String.class)})
    public static void getAdminDomainName(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue(
            "AdminDomainName",
            AMXUtil.getDomainConfig().getPropertyValue(ADMIN_DOMAIN_NAME_PROP_NAME));
    }
    
    /**
     * <p> This handler returns a Set of cluster names </p>
     *
     * <p> Output value: "Clusters" -- Type: <code>java.util.Set</code></P>
     * @param  context The HandlerContext.
     */
    @Handler(id="getClusters",
        output={
            @HandlerOutput(name="Clusters", type=Set.class)})
    public static void getClusters(HandlerContext handlerCtx) {
        Map<String,ClusterConfig> clusterMap = 
            (Map)AMXUtil.getDomainConfig().getClusterConfigMap();
        Set<String> clusters = clusterMap == null ? null : clusterMap.keySet();
        handlerCtx.setOutputValue("Clusters", clusters);
    }

    /**
     * <p> This handler returns a Set of instance names </p>
     *
     * <p> Output value: "Instances" -- Type: <code>java.util.Set</code>
     * @param  context The HandlerContext.
     */
    @Handler(id="getInstances",
        output={
            @HandlerOutput(name="Instances", type=Set.class)})
    public static void getInstances(HandlerContext handlerCtx) {
        Map<String,ServerConfig> instanceMap = 
                (Map)AMXUtil.getDomainConfig().getServerConfigMap();
        Set<String> instances = instanceMap == null ? null : instanceMap.keySet();
	handlerCtx.setOutputValue("Instances", instances);
    }
    
    /**
     * <p> This handler returns a Set of node agent names </p>
     *
     * <p> Output value: "NodeAgents" -- Type: <code>java.util.Set</code>
     * @param  context The HandlerContext.
     */
    @Handler(id="getNodeAgents",
        output={
            @HandlerOutput(name="NodeAgents", type=Set.class)})
    public static void getNodeAgents(HandlerContext handlerCtx) {
        Map<String,NodeAgentConfig> nodeAgentMap = 
            (Map)AMXUtil.getDomainConfig().getNodeAgentConfigMap();
        Set<String> nodeAgents = nodeAgentMap == null ?  null : nodeAgentMap.keySet();
	handlerCtx.setOutputValue("NodeAgents", nodeAgents);
    }
    
    public static final String ADMIN_DOMAIN_NAME_PROP_NAME = "administrative.domain.name";
}
