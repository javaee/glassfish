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

import com.sun.appserv.management.config.ClusteredServerConfig;

import com.sun.enterprise.tools.admingui.handlers.ConfigurationHandlers;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

import com.sun.appserv.management.j2ee.J2EEServer;

import com.sun.jbi.jsf.bean.AlertBean;

import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;

import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.logging.Logger;

import javax.faces.model.SelectItem;

/**
 * Provides jsftemplating handlers for instance selection.
 */
public class InstanceSelectionHandlers
{

private static Logger sLog = JBILogger.getInstance();


    /**
     *  <p> This handler returns the values for runtime configuration instance list
     *  <p> Output value: "InstanceList" -- Type: <code>java.util.Array</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiGetRuntimeInstances",
             input={
                 @HandlerInput (name="componentType", type=String.class, required=true),
                 @HandlerInput (name="componentName", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="InstanceList", type=SelectItem[].class)} )

        public static void jbiGetRuntimeInstances(HandlerContext handlerCtx)
        {
            String componentType  = (String)handlerCtx.getInputValue("componentType");
            String componentName  = (String)handlerCtx.getInputValue("componentName");
            
            List instanceList = ClusterUtilities.findAllNonDomainTargets();
            instanceList.add(0,"domain");

            String[] instances = new String[0];
            instances = (String[]) instanceList.toArray(instances);
            SelectItem[] options = ConfigurationHandlers.getOptions(instances);

            handlerCtx.setOutputValue("InstanceList", options);
            sLog.fine("InstanceSelectionHandlers.jbiGetRuntimeInstances(), options.length=" + options.length);
    }


    /**
     *  <p> This handler returns the values for instances in configureComponent page
     *  <p> Output value: "InstanceList" -- Type: <code>java.util.Array</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiGetInstances",
             input={
                 @HandlerInput (name="componentType", type=String.class, required=true),
                 @HandlerInput (name="componentName", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="InstanceList", type=SelectItem[].class),
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)} )

        public static void jbiGetInstances(HandlerContext handlerCtx)
        {
            String componentType  = (String)handlerCtx.getInputValue("componentType");
            String componentName  = (String)handlerCtx.getInputValue("componentName");
            
            List installedInstanceList = new ArrayList();
            List installedTargetList = new ArrayList();
            List installedList = ClusterUtilities.findTargetsForNameByType (componentName, 
                                                                            componentType);
            // Construct the installedTargetList
            Iterator targetsIterator = installedList.iterator();
            while (targetsIterator.hasNext())
            {
                Properties targetProp = (Properties) targetsIterator.next();
                String target = targetProp.getProperty(SharedConstants.KEY_NAME);
                installedTargetList.add(target);
            }

            // Loop through all the installed targets to construct the
            // installedInstanceList
            Iterator targetIt = installedTargetList.iterator();
            while (targetIt.hasNext())
            {
                String target = (String) targetIt.next();
                // for each cluster target, add all of its instances (if any)
                if (ClusterUtilities.isCluster(target))
                {
                    sLog.fine("InstanceSelectionHandlers.jbiGetInstances(), cluster target=" + target);

                    Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(target).getClusteredServerConfigMap();
                    List instancesForOneCluster = new ArrayList();

                    if (null != serverMap)
                    {
                        for (String key : serverMap.keySet())
                        {
                            String name = serverMap.get(key).getName();
                            J2EEServer server = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name);
                            int state = server.getstate();
                            if (state == 1)
                            {
                                instancesForOneCluster.add(name);
                            }
                            sLog.fine("InstanceSelectionHandlers.jbiGetInstances(), cluster target=" + target + ", instanceName=" + name + ", state=" + state);
                        }
                    }

                    Iterator instanceIt = instancesForOneCluster.iterator();
                    while (instanceIt.hasNext())
                    {
                        String instance = (String) instanceIt.next();
                        installedInstanceList.add(instance);
                    }
                }
                // for each stand-alone server target, add it as an instance
                else
                {
                    J2EEServer server = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(target);
                    int state = server.getstate();
                    if (state == 1)
                    {
                        installedInstanceList.add(target);
                    }
                    sLog.fine("InstanceSelectionHandlers.jbiGetInstances(), stand-alone server target=" + target + ", state=" + state);
                }
            }

            // Construct the warning message if component is not installed on any target
            String alertType       = "";
            String alertDetails    = "";
            String alertSummary    = "";
            boolean isAlertNeeded  = false;
            if (installedInstanceList.size() == 0)
            {
                isAlertNeeded  = true;
                alertType      = "warning";
                alertSummary   = I18nUtilities.getResourceString ("jbi.component.not.installed.to.target.summary");
                Object[] args = {componentName};
                alertDetails = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.component.not.installed.to.target.details"), args);
                AlertBean alertBean = BeanUtilities.getAlertBean();
                alertBean.setAlertType(alertType);
            }

            // Return the options list
            String[] instances = new String[0];
            instances = (String[]) installedInstanceList.toArray(instances);
            SelectItem[] options = ConfigurationHandlers.getOptions(instances);
            
            handlerCtx.setOutputValue("InstanceList", options);
            handlerCtx.setOutputValue ("isAlertNeeded", isAlertNeeded);
            handlerCtx.setOutputValue ("alertSummary", alertSummary);
            handlerCtx.setOutputValue ("alertDetails", alertDetails);
            sLog.fine("InstanceSelectionHandlers.jbiGetInstances(), options.length=" + options.length);
        }


    /**
     *  <p> This handler sets the values for instances to be updated in configureComponent page
     *  <p> Input value: "fromInstance" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "toInstances" -- Type: <code>java.lang.String[]</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiSetDefaultSelectedInstances",
             input={
                 @HandlerInput(name="fromInstance", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="toInstances", type=String[].class)} )

        public static void jbiSetDefaultSelectedInstances(HandlerContext handlerCtx)
        {
            String[] result = new String[1];
            String fromInstance = (String) handlerCtx.getInputValue("fromInstance");
            result[0] = fromInstance;
            sLog.fine("InstanceSelectionHandlers.jbiSetDefaultSelectedInstances(" + fromInstance+ "), result.length=" + result.length);
            handlerCtx.setOutputValue ("toInstances", result);
        }


    /**
     *  <p> This converts an object array (containing strings) to a string array
     *  <p> Input value: "fromObjectArray" -- Type: <code>java.lang.Object[]</code></p>
     *  <p> Output value: "toStringArray" -- Type: <code>java.lang.String[]</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiObjectArrayToStringArray",
             input={
                 @HandlerInput(name="fromObjectArray", type=Object[].class, required=true)},
             output={
                 @HandlerOutput (name="toStringArray", type=String[].class)} )

        public static void jbiObjectArrayToStringArray(HandlerContext handlerCtx)
        {
            String[] result;
            Object[] fromObjectArray = (Object[]) handlerCtx.getInputValue("fromObjectArray");
            if (null != fromObjectArray)
            {
                result = new String[fromObjectArray.length];
                for (int i = 0; i < result.length; ++i)
                {
                    result[i] = (String) fromObjectArray[i];
                }
            }
            else
            {
                result = new String[0];
            }
            sLog.fine("InstanceSelectionHandlers.jbiObjectArrayToStringArray(" + fromObjectArray + "), result.length=" + result.length);
            handlerCtx.setOutputValue ("toStringArray", result);
        }


    /**
     *  <p> This handler converts an array of strings to a printable comma separated list in one string
     *  <p> Input value: "fromStringArray" -- Type: <code>java.lang.String[]</code></p>
     *  <p> Output value: "toStringWithCommas" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiListArrayStrings",
             input={
                 @HandlerInput(name="fromStringArray", type=Object[].class, required=true)},
             output={
                 @HandlerOutput (name="toStringWithCommas", type=String.class)} )

        public static void jbiListArrayStrings(HandlerContext handlerCtx)
        {
            String result = "list: [";
            Object[] fromStringArray = (Object[]) handlerCtx.getInputValue("fromStringArray");
            for (int i = 0; i < fromStringArray.length; ++i)
            {
                result += (String) fromStringArray[i];
                result += ", ";
            }
            result += " ]";

            sLog.fine("InstanceSelectionHandlers.jbiListArrayStrings(" + fromStringArray+ "), result=" + result);
            handlerCtx.setOutputValue ("toStringWithCommas", result);
        }


}






