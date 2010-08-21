/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admingui.common.handlers;

import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Date;

import java.util.ListIterator;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import org.glassfish.admingui.common.util.V3AMX;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;


import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admingui.common.util.RestResponse;

/**
 *
 * @author Ana
 */
public class MonitoringHandlers {
    

    @Handler(id = "getMonitorLevels",
    input = {
        @HandlerInput(name = "endpoint", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "monitorCompList", type = List.class)
    })
    public static void getMonitorLevels(HandlerContext handlerCtx) {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        List result = new ArrayList();
        try {
            String monitoringServiceEndPoint = endpoint + "/monitoring-service";
            Map attrs = RestApiHandlers.getEntityAttrs(monitoringServiceEndPoint, "entitiy");

            ObjectName[] pnames = (ObjectName[]) attrs.get("ContainerMonitoring");
            if (pnames != null) {
                for (int i = 0; i < pnames.length; i++) {
                    Map oneRow = new HashMap();
                    String cname = null;
                    String pname = pnames[i].getKeyProperty("name");
                    ListIterator ci = containerDispList.listIterator();
                    ListIterator vi = containerNameList.listIterator();
                    while (ci.hasNext() && vi.hasNext()) {
                        String dispName = (String) ci.next();
                        String value = (String) vi.next();
                        if (pname.equals(value)) {
                            cname = dispName;
                        }
                    }
                    oneRow.put("monCompName", (cname == null) ? pname : cname);
                    oneRow.put("level", V3AMX.getAttribute(pnames[i], "Level"));
                    oneRow.put("selected", false);
                    result.add(oneRow);
                }
            }

            String monitoringLevelsEndPoint = endpoint + "/module-monitoring-levels";
            attrs = RestApiHandlers.getEntityAttrs(monitoringLevelsEndPoint, "entity");
            for (Object oneMonComp : attrs.keySet()) {
                Map oneRow = new HashMap();
                String name = null;
                ListIterator ni = monDisplayList.listIterator();
                ListIterator vi = monNamesList.listIterator();
                while (ni.hasNext() && vi.hasNext()) {
                    String dispName = (String) ni.next();
                    String value = (String) vi.next();
                    if ((oneMonComp.equals(value))) {
                        name = dispName;
                    }
                }
                if (name == null) {
                    name = (String) oneMonComp;
                }
                oneRow.put("monCompName", name);
                oneRow.put("level", attrs.get(oneMonComp));
                oneRow.put("selected", false);
                result.add(oneRow);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue( "monitorCompList", result);
    }

    /*
     * This handler returns a list of statistical data for an endpoint.
     * Useful for populating table
     */
    @Handler(id="getStats",
    input={
        @HandlerInput(name="endpoint",   type=String.class, required=true),
        @HandlerInput(name="statType",   type=String.class),
        @HandlerInput(name="type",   type=String.class)},
    output={
        @HandlerOutput(name="result",        type=List.class),
        @HandlerOutput(name="hasStats",        type=Boolean.class)})

        public static void getStats(HandlerContext handlerCtx) {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String statType = (String) handlerCtx.getInputValue("statType");
        String type = (String) handlerCtx.getInputValue("type");
        Locale locale = GuiUtil.getLocale();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        List result = new ArrayList();

        try {
            //This check is to get the correct type of statistics.
            if ((type == null || statType == null) || type.equals(statType)) {
                if (doesProxyExist(endpoint)) {
                    Map<String, Map> stats = RestApiHandlers.getMonitoringStatInfo(
                            RestApiHandlers.get(endpoint).getResponseBody());
                    for (String statName : stats.keySet()) {
                        Map<String, String> monAttrs = (Map<String, String>) stats.get(statName);
                        Map<String, String> statMap = new HashMap();
                        String val = "";
                        String details = "--";
                        String desc = "--";
                        String start = "--";
                        String last = "--";
                        String unit = "";
                        String current = "";
                        String mname = null;
                        String runtimes = null;
                        String queuesize = null;
                        String thresholds = "--";

                        Map statsMap = new HashMap();
                        if (monAttrs.size() != 0) {

                            if (monAttrs.containsKey("name")) {
                                mname = (String) monAttrs.get("name");
                            } else if (monAttrs.containsKey("appname")) {
                                mname = (String) monAttrs.get("appname");
                            }
                            unit = (String) monAttrs.get("unit");
                            desc = (String) monAttrs.get("description");

                            last = (String) monAttrs.get("lastsampletime");
                            if (Long.valueOf(last) != -1) {
                                last = df.format(new Date(Long.valueOf(last)));
                            }
                            start = (String) monAttrs.get("starttime");
                            if (Long.valueOf(start) != -1) {
                                start = df.format(new Date(Long.valueOf(start)));
                            }
                            if (monAttrs.containsKey("count")) {
                                val = (String) monAttrs.get("count") + " " + unit;
                            } else if (monAttrs.containsKey("current")) {
                                if (statName.equals("transaction-service")) {
                                    String str = (String) monAttrs.get("current");
                                    String formatStr = formatActiveIdsForDisplay(str);
                                    if (!formatStr.isEmpty() && !formatStr.equals("")) {
                                        val = formatStr;
                                    }
                                } else {
                                    val = (String) monAttrs.get("current");
                                    if (unit != null && !(unit.equals("String"))) {
                                        val = val + unit;
                                    }
                                }
                            } else if (monAttrs.containsKey("applicationtype")) {
                                val = (String) monAttrs.get("applicationtype");
                            }

                            //Update the details
                            if (monAttrs.containsKey("appName")) {
                                details = (GuiUtil.getMessage("msg.AppName") + ": " + monAttrs.get("appName") + "<br/>");
                            }
                            if (monAttrs.containsKey("appname")) {
                                details = (GuiUtil.getMessage("msg.AppName") + ": " + monAttrs.get("appname") + "<br/>");
                            }
                            if (monAttrs.containsKey("jrubyversion")) {
                                details = details + (GuiUtil.getMessage("msg.JrubyVersion") + ": " + monAttrs.get("jrubyversion") + "<br/>");
                            }
                            if (monAttrs.containsKey("rubyframework")) {
                                details = details + (GuiUtil.getMessage("msg.Framework") + ": " + monAttrs.get("rubyframework") + "<br/>");
                            }
                            if (monAttrs.containsKey("environment")) {
                                details = details + (GuiUtil.getMessage("msg.Environment") + ": " + monAttrs.get("environment") + "<br/>");
                            }
                            if (monAttrs.containsKey("address")) {
                                details = details + (GuiUtil.getMessage("msg.Address") + ": " + monAttrs.get("address") + "<br/>");
                            }
                            if (monAttrs.containsKey("deploymenttype")) {
                                details = details + (GuiUtil.getMessage("msg.DepType") + ": " + monAttrs.get("deploymenttype") + "<br/>");
                            }
                            if (monAttrs.containsKey("endpointname")) {
                                details = details + (GuiUtil.getMessage("msg.EndPointName") + ": " + monAttrs.get("endpointname") + "<br/>");
                            }
                            if (monAttrs.containsKey("classname")) {
                                details = (GuiUtil.getMessage("msg.ClassName") + ": " + monAttrs.get("classname") + "<br/>");
                            }
                            if (monAttrs.containsKey("impltype")) {
                                details = details + (GuiUtil.getMessage("msg.ImplClass") + ": " + monAttrs.get("implclass") + "<br/>");
                            }
                            if (monAttrs.containsKey("implclass") && monAttrs.containsKey("impltype")) {
                                details = details + (GuiUtil.getMessage("msg.ImplType") + ": " + monAttrs.get("impltype") + "<br/>");
                            }

                            if (monAttrs.containsKey("namespace")) {
                                details = details + (GuiUtil.getMessage("msg.NameSpace") + ": " + monAttrs.get("namespace") + "<br/>");
                            }
                            if (monAttrs.containsKey("portname")) {
                                details = details + (GuiUtil.getMessage("msg.PortName") + ": " + monAttrs.get("portname") + "<br/>");
                            }
                            if (monAttrs.containsKey("servicename")) {
                                details = details + (GuiUtil.getMessage("msg.ServiceName") + ": " + monAttrs.get("servicename") + "<br/>");
                            }
                            if (monAttrs.containsKey("tester")) {
                                details = details + (GuiUtil.getMessage("msg.Tester") + ": " + monAttrs.get("tester") + "<br/>");
                            }
                            if (monAttrs.containsKey("wsdl")) {
                                details = details + (GuiUtil.getMessage("msg.WSDL") + ": " + monAttrs.get("wsdl") + "<br/>");
                            }

                            if (monAttrs.containsKey("maxtime")) {
                                details = (GuiUtil.getMessage("msg.MaxTime") + ": " + monAttrs.get("maxtime") + " " + unit + "<br/>");
                            }
                            if (monAttrs.containsKey("mintime")) {
                                details = details + (GuiUtil.getMessage("msg.MinTime") + ": " + monAttrs.get("mintime") + " " + unit + "<br/>");
                            }
                            if (monAttrs.containsKey("totaltime")) {
                                details = details + (GuiUtil.getMessage("msg.TotalTime") + ": " + monAttrs.get("totaltime") + " " + unit + "<br/>");
                            }
                            if (monAttrs.containsKey("highwatermark")) {
                                details = (GuiUtil.getMessage("msg.HWaterMark") + ": " + monAttrs.get("highwatermark") + " " + unit + "<br/>");
                            }
                            if (monAttrs.containsKey("lowwatermark")) {
                                details = details + (GuiUtil.getMessage("msg.LWaterMark") + ": " + monAttrs.get("lowwatermark") + " " + unit + "<br/>");
                            }
                            if (monAttrs.containsKey("activeruntimes")) {
                                runtimes = (String) monAttrs.get("activeruntimes");
                            }
                            if (monAttrs.containsKey("queuesize")) {
                                queuesize = (String) monAttrs.get("queuesize");
                            }
                            if (monAttrs.containsKey("hardmaximum") && monAttrs.get("hardmaximum") != null) {
                                val = monAttrs.get("hardmaximum") + " " + "hard max " + "<br/>" + monAttrs.get("hardminimum") + " " + "hard min";
                            }
                            if (monAttrs.containsKey("newthreshold") && monAttrs.get("newThreshold") != null) {
                                thresholds = monAttrs.get("newthreshold") + " " + "new " + "<br/>" + monAttrs.get("queuedownthreshold") + " " + "queue down";
                            }
                            if (monAttrs.containsKey("queuesize") && monAttrs.containsKey("jrubyversion")) {
                                details = details + monAttrs.get("environment") + " " + monAttrs.get("jrubyversion");
                            }

                            statMap.put("Name", mname);
                            statMap.put("StartTime", start);
                            statMap.put("LastTime", last);
                            statMap.put("Description", desc);
                            statMap.put("Value", (val == null) ? "" : val);
                            statMap.put("Details", (details == null) ? "--" : details);
                            statMap.put("Thresholds", (thresholds == null) ? "--" : thresholds);
                            statMap.put("QueueSize", (queuesize == null) ? "--" : queuesize);
                            statMap.put("Runtimes", (runtimes == null) ? "--" : runtimes);
                            result.add(statMap);
                        }
                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("hasStats", (result.size() == 0) ? false : true);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
            
      /*
     * This handler returns a list of statistical data for type and name of component.
     * Useful for populating table
     */
    @Handler(id="getStatsbyTypeName",
    input={
        @HandlerInput(name="type",   type=String.class, required=true),
        @HandlerInput(name="name",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=List.class),
        @HandlerOutput(name="hasStats",        type=Boolean.class)})

        public static void getStatsbyTypeName(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        String name = (String) handlerCtx.getInputValue("name");
        Locale locale = GuiUtil.getLocale();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        List result = new ArrayList();
        
        try {
            Query query = V3AMX.getInstance().getDomainRoot().getQueryMgr();
            Set amxproxy = (Set) query.queryTypeName(type, name);
            Iterator iter = amxproxy.iterator();
            while (iter.hasNext()) {
                Map<String, Object> monattrs = ((AMXProxy) iter.next()).attributesMap();
               for (String monName : monattrs.keySet()) {
                   if ((!monName.equals("Parent")) && (!monName.equals("Children"))&& (!monName.equals("Name"))) {
                        Map statMap = new HashMap();
                        Object val = monattrs.get(monName);
                        String details = "--";
                        String desc = "--";
                        Object start = "--";
                        Object last = "--";
                        String unit = "";
                        String current = "";
                        String mname = null;
                        Object runtimes = null;
                        Object queuesize = null;
                        String thresholds = "--";
                        boolean nostatskey = true;
                        if (val instanceof CompositeDataSupport) {
                            CompositeDataSupport cds = ((CompositeDataSupport) val);
                            CompositeType ctype = cds.getCompositeType();
                            if (cds.containsKey("statistics")) {
                                Object statistics = cds.get("statistics");
                               if (statistics instanceof CompositeData[]) {
                                    CompositeData[] mycd = (CompositeData[])cds.get("statistics");
                                    if(((CompositeData[])cds.get("statistics")).length == 0){
                                        val = "--";
                                    }
                                    for (CompositeData cd : (CompositeData[]) cds.get("statistics")) {
                                        String statname = null;
                                        nostatskey = false;
                                        Map statsMap = new HashMap();
                                        if (cd.containsKey("name")&& type.equals("web-service-mon")) {
                                            val = cd.get("name");
                                        }
                                        if (cd.containsKey("name")&& cd.containsKey("count")) {
                                            statname = (String)cd.get("name");
                                        }
                                        if (cd.containsKey("name") && !cd.containsKey("count")) {
                                            val = cd.get("name");
                                        }
                                        if (cd.containsKey("unit")) {
                                            unit = (String) cd.get("unit");
                                        }
                                        if (cd.containsKey("count")) {
                                            val = cd.get("count") + " " + unit;
                                        }
                                        if (cd.containsKey("description")) {
                                            desc = (String) cd.get("description");
                                        }
                                        if (cd.containsKey("lastSampleTime")) {
                                            if ((Long)cd.get("lastSampleTime") == -1) {
                                                last = cd.get("lastSampleTime");
                                            } else {
                                                last = df.format(new Date((Long) cd.get("lastSampleTime")));
                                            }
                                        }
                                        if (cd.containsKey("startTime")) {
                                            if ((Long) cd.get("startTime") == -1) {
                                                start = cd.get("startTime");
                                            } else {
                                                start = df.format(new Date((Long) cd.get("lastSampleTime")));
                                            }
                                        }
                                        if (cd.containsKey("appName")) {
                                            details = (GuiUtil.getMessage("msg.AppName") + ": " + cd.get("appName") + "<br/>");
                                        }
                                        if (cd.containsKey("appname")) {
                                            details = (GuiUtil.getMessage("msg.AppName") + ": " + cd.get("appname") + "<br/>");
                                        }
                                        if (cd.containsKey("jrubyversion")) {
                                            details = details + (GuiUtil.getMessage("msg.JrubyVersion") + ": " + cd.get("jrubyversion") + "<br/>");
                                        }
                                        if (cd.containsKey("rubyframework")) {
                                            details = details + (GuiUtil.getMessage("msg.Framework") + ": " + cd.get("rubyframework") + "<br/>");
                                        }
                                        if (cd.containsKey("environment")) {
                                            details = details + (GuiUtil.getMessage("msg.Environment") + ": " + cd.get("environment") + "<br/>");
                                        }
                                        if (cd.containsKey("address")) {
                                            details = details + (GuiUtil.getMessage("msg.Address") + ": " + cd.get("address") + "<br/>");
                                        }
                                        if (cd.containsKey("deploymentType")) {
                                            details = details + (GuiUtil.getMessage("msg.DepType") + ": " + cd.get("deploymentType") + "<br/>");
                                        }
                                        if (cd.containsKey("endpointName")) {
                                            details = details + (GuiUtil.getMessage("msg.EndPointName") + ": " + cd.get("endpointName") + "<br/>");
                                        }
                                        if (cd.containsKey("classname")) {
                                            details = (GuiUtil.getMessage("msg.ClassName") + ": " + cd.get("classname") + "<br/>");
                                        }
                                        if (cd.containsKey("implType")) {
                                            details = details + (GuiUtil.getMessage("msg.ImplClass") + ": " + cd.get("implClass") + "<br/>");
                                        }
                                        if (cd.containsKey("implClass") && cd.containsKey("implType")) {
                                            details = details + (GuiUtil.getMessage("msg.ImplType") + ": " + cd.get("implType") + "<br/>");
                                        }
                                        
                                        if (cd.containsKey("namespace")) {
                                            details = details + (GuiUtil.getMessage("msg.NameSpace") + ": " + cd.get("namespace") +  "<br/>");
                                        }
                                        if (cd.containsKey("portName")) {
                                            details = details + (GuiUtil.getMessage("msg.PortName") + ": " + cd.get("portName") +  "<br/>");
                                        }
                                        if (cd.containsKey("serviceName")) {
                                            details = details + (GuiUtil.getMessage("msg.ServiceName") + ": " + cd.get("serviceName") +  "<br/>");
                                        }
                                        if (cd.containsKey("tester")) {
                                            details = details + (GuiUtil.getMessage("msg.Tester") + ": " + cd.get("tester") +  "<br/>");
                                        }
                                        if (cd.containsKey("wsdl")) {
                                            details = details + (GuiUtil.getMessage("msg.WSDL") + ": " + cd.get("wsdl") +  "<br/>");
                                        }
                                        statsMap.put("Name", (statname == null) ? monName : statname);
                                        statsMap.put("StartTime", start);
                                        statsMap.put("LastTime", last);
                                        statsMap.put("Description", desc);
                                        statsMap.put("Value", (val == null) ? "" : val);
                                        statsMap.put("Details", details);
                                        result.add(statsMap);
                                    }
                                }
                                
                            } else {
                                if (cds.containsKey("name")) {
                                    mname = (String)cds.get("name");
                                } else {
                                    mname = (String)monName;
                                }
                                if (cds.containsKey("unit")) {
                                    unit = (String) cds.get("unit");
                                }
                                if (cds.containsKey("description")) {
                                    desc = (String) cds.get("description");
                                }
                                if (cds.containsKey("startTime")) {
                                    if ((Long) cds.get("startTime") == -1) {
                                        start = cds.get("startTime");
                                    } else {
                                        start = df.format(new Date((Long) cds.get("lastSampleTime")));
                                    }
                                }
                                if (cds.containsKey("lastSampleTime")) {
                                    if ((Long) cds.get("lastSampleTime") == -1) {
                                        last = cds.get("lastSampleTime");
                                    } else {
                                        last = df.format(new Date((Long) cds.get("lastSampleTime")));
                                    }
                                }
                                if (cds.containsKey("maxTime")) {
                                    details = (GuiUtil.getMessage("msg.MaxTime") + ": " + cds.get("maxTime") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("minTime")) {
                                    details = details + (GuiUtil.getMessage("msg.MinTime") + ": " + cds.get("minTime") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("totalTime")) {
                                    details = details + (GuiUtil.getMessage("msg.TotalTime") + ": " + cds.get("totalTime") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("highWaterMark")) {
                                    details = (GuiUtil.getMessage("msg.HWaterMark") + ": " + cds.get("highWaterMark") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("lowWaterMark")) {
                                    details = details + (GuiUtil.getMessage("msg.LWaterMark") + ": " + cds.get("lowWaterMark") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("activeRuntimes")) {
                                    runtimes = (Integer) cds.get("activeRuntimes");
                                }
                                if (cds.containsKey("queueSize")) {
                                    queuesize = cds.get("queueSize");
                                }
                                if (cds.containsKey("hardMaximum") && cds.get("hardMaximum") != null) {
                                    val = cds.get("hardMaximum") + " " + "hard max " + "<br/>" + cds.get("hardMinimum") + " " + "hard min";
                                }
                                if (cds.containsKey("newThreshold") && cds.get("newThreshold") != null) {
                                    thresholds = cds.get("newThreshold") + " " + "new " + "<br/>" + cds.get("queueDownThreshold") + " " + "queue down";
                                }
                                if (cds.containsKey("count")) {
                                    val = cds.get("count") + " " + unit;
                                } else if (cds.containsKey("current")) {
                                    if (name.equals("transaction-service")) {
                                        String str = (String) cds.get("current");
                                        String formatStr = formatActiveIdsForDisplay(str);
                                        if(!formatStr.isEmpty() && !formatStr.equals(""))
                                            val = formatStr;
                                    } else {
                                        val = cds.get("current");
                                    }
                                } else {
                                    val = "--";
                                }
                            }
                        } else if (val instanceof String[]) {
                            mname = (String)monName;
                            String values = "";
                            for (String s : (String[]) val) {
                                values = values + s + "<br/>";

                            }
                            val = values;
                        } else if (val instanceof CompositeData[]) {
                            String apptype = "";
                            for (CompositeData cd : (CompositeData[]) val) {
                                if(cd.containsKey("appName")) {
                                    mname = (String)cd.get("appName");
                                }
                                if(cd.containsKey("applicationType")) {
                                    apptype = (String)cd.get("applicationType");
                                }
                                if(cd.containsKey("queueSize") && cd.containsKey("jrubyVersion")) {
                                    details = details + cd.get("environment") + " " + cd.get("jrubyVersion");
                                }
                            }
                            val = apptype;
                        }
                        if (nostatskey) {
                            statMap.put("Name", (mname != null) ? mname : monName);
                            statMap.put("Thresholds", (thresholds == null) ? "--" : thresholds);
                            statMap.put("QueueSize", (queuesize == null) ? "--" : queuesize);
                            statMap.put("Runtimes", (runtimes == null) ? "--" : runtimes);
                            statMap.put("Current", current);
                            statMap.put("StartTime", start);
                            statMap.put("LastTime", last);
                            statMap.put("Description", desc);
                            statMap.put("Value", (val == null) ? "" : val);
                            statMap.put("Details", (details == null) ? "--" : details);

                            result.add(statMap);
                        }

                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("hasStats", (amxproxy.isEmpty()) ? false : true);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
    @Handler(id = "updateMonitorLevels",
    input = {
        @HandlerInput(name = "allRows", type = List.class, required = true),
        @HandlerInput(name = "endpoint", type = String.class),
        @HandlerInput(name = "containerEndpoint", type = String.class)})
    public static void updateMonitorLevels(HandlerContext handlerCtx) {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String containerEndpoint = (String) handlerCtx.getInputValue("containerEndpoint");
        List<Map<String,String>> allRows = (List<Map<String,String>>) handlerCtx.getInputValue("allRows");
        String objectNameStr = null;
        for (Map<String, String> oneRow : allRows) {
            String name = oneRow.get("monCompName");
            String value = null;
            ListIterator ni = monDisplayList.listIterator();
            ListIterator vi = monNamesList.listIterator();
            while (ni.hasNext() && vi.hasNext()) {
                String dispName = (String) ni.next();
                String mvalue = (String) vi.next();
                if (name.equals(dispName)) {
                    value = mvalue;
                    objectNameStr = endpoint;
                }
            }
            if (value == null) {
                ListIterator ci = containerDispList.listIterator();
                ListIterator cni = containerNameList.listIterator();
                while (ci.hasNext() && cni.hasNext()) {
                    String cDispName = (String) ci.next();
                    String cName = (String) cni.next();
                    if (name.equals(cDispName)) {
                        value = "Level";
                        objectNameStr = containerEndpoint+"/"+cName;
                    }
                }
            }
            Map<String,Object> attrMap = new HashMap<String,Object>();
            attrMap.put((value == null) ? name : value, oneRow.get("level"));
            String entityUrl = (objectNameStr == null) ? containerEndpoint+"/"+name : objectNameStr ;
            RestResponse response = RestApiHandlers.sendUpdateRequest(entityUrl, attrMap, null, null, null);
            if (!response.isSuccess()) {
                GuiUtil.getLogger().severe("Update monitor level failed.  parent=" + endpoint + "; attrsMap =" + attrMap);
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
                return;
            }
        }
     }

    /**
     *	<p> Add list to new list
     */
    @Handler(id = "addToMonitorList",
        input = {
            @HandlerInput(name = "oldList", type = List.class),
            @HandlerInput(name = "newList", type = List.class)},
        output = {
            @HandlerOutput(name = "result", type = List.class)
            })
    public static void addToMonitorList(HandlerContext handlerCtx) {
       List<String> oldList = (List) handlerCtx.getInputValue("oldList");
       List<String> newList = (List) handlerCtx.getInputValue("newList");
        if (newList == null){
            newList = new ArrayList();
        }
        if (oldList != null) {
                for (String sk : oldList) {
                    newList.add(sk);
                }
            }
        handlerCtx.setOutputValue("result", newList);
    }
    
    @Handler(id = "getValidMonitorLevels",
    output = {
        @HandlerOutput(name = "monitorLevelList", type = List.class)
    })
    public static void getValidMonitorLevels(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("monitorLevelList",  levels);
     }
    
    @Handler(id = "getFirstValueFromList",
    input={
        @HandlerInput(name="values",   type=List.class, required=true)},
    output = {
        @HandlerOutput(name = "firstValue", type = String.class)
    })
    public static void getFirstValueFromList(HandlerContext handlerCtx) {
        List values = (List) handlerCtx.getInputValue("values");
        String firstval = "";
        if ((values != null) && (values.size()!=0)){
            firstval = (String)values.get(0);

        }
        handlerCtx.setOutputValue("firstValue",  firstval);
     }

    @Handler(id = "getAppName",
      input={
        @HandlerInput(name="endpoint",   type=String.class, required=true),
        @HandlerInput(name="name",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "appName", type = String.class),
        @HandlerOutput(name = "appFullName", type = String.class)
       })
    public static void getAppName(HandlerContext handlerCtx) {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String name = (String) handlerCtx.getInputValue("name");
        String appName = name;
        String fullName = name;
        try {
            List<String> applications = RestApiHandlers.getChildList(endpoint);
            for (String oneApp : applications) {
                List<String> modules = RestApiHandlers.getChildList(endpoint + "/" + oneApp + "/module");
                if (modules.contains(name)) {
                    appName = oneApp;
                    break;
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
        if (fullName != null && !(name.equals(appName))) {
            fullName = appName + "/" + name;
        }

        handlerCtx.setOutputValue("appName",  appName);
        handlerCtx.setOutputValue("appFullName",  fullName);
    }

    @Handler(id = "getNameforMbean",
      input={
        @HandlerInput(name="appName",   type=String.class, required=true),
        @HandlerInput(name="end",   type=String.class, required=true),
        @HandlerInput(name="compVal",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "mbeanName", type = String.class)
       })
    public static void getNameforMbean(HandlerContext handlerCtx) {
        String app = (String) handlerCtx.getInputValue("appName");
        String comp = (String) handlerCtx.getInputValue("compVal");
        String end = (String) handlerCtx.getInputValue("end");
        String mbeanName = "EMPTY";
        try {
            Query query = V3AMX.getInstance().getDomainRoot().getQueryMgr();
            Set data = (Set) query.queryType("server-mon");
            Iterator iter = data.iterator();
            while (iter.hasNext()) {
                Map attrs = ((AMXProxy) iter.next()).attributesMap();
                ObjectName[] pnames = (ObjectName[]) attrs.get("Children");
                for (int i = 0; i < pnames.length; i++) {
                    String pname = pnames[i].getKeyProperty("name");
                    if(pname != null){
                        if (end.equals("true")) {
                            if (pname.endsWith(app + "/" + comp)) {
                                mbeanName = pname;
                                break;
                            }
                        } else {
                            if (pname.startsWith(app + "/" + comp)) {
                                mbeanName = pname;
                                break;
                            }
                        }
                    }

                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("mbeanName", mbeanName);

    }

        @Handler(id = "getNameforMbeanByType",
      input={
        @HandlerInput(name="appName",   type=String.class, required=true),
        @HandlerInput(name="end",   type=String.class, required=true),
        @HandlerInput(name="type",   type=String.class, required=true),
        @HandlerInput(name="compVal",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "mbeanName", type = String.class)
       })
    public static void getNameforMbeanByType(HandlerContext handlerCtx) {
        String app = (String) handlerCtx.getInputValue("appName");
        String comp = (String) handlerCtx.getInputValue("compVal");
        String end = (String) handlerCtx.getInputValue("end");
        String type = (String) handlerCtx.getInputValue("type");
        String mbeanName = "EMPTY";
        List proxyList = V3AMX.getProxyListByType(type);
        if (proxyList.size() != 0) {
            ListIterator li = proxyList.listIterator();
            while (li.hasNext()) {
                String pname = (String) li.next();
                if (end.equals("true")) {
                    if (pname.endsWith(app + "/" + comp)) {
                        mbeanName = pname;
                        break;
                    }
                } else {
                    if (pname.startsWith(app + "/" + comp)) {
                        mbeanName = pname;
                        break;
                    }
                }
            }
        }
        handlerCtx.setOutputValue("mbeanName", mbeanName);

    }

    @Handler(id = "getWebStatsUrl",
      input={
        @HandlerInput(name="app",   type=String.class, required=true),
        @HandlerInput(name="compVal",   type=String.class, required=true),
        @HandlerInput(name="vsList",   type=List.class, required=true),
        @HandlerInput(name="moduleProps",   type=Map.class, required=true)},
       output = {
        @HandlerOutput(name = "webStatUrl", type = String.class),
        @HandlerOutput(name = "webStatType", type = String.class)
       })
    public static void getWebStatsUrl(HandlerContext handlerCtx) {
        String app = (String) handlerCtx.getInputValue("app");
        List<String> vsList = (List<String>) handlerCtx.getInputValue("vsList");
        String compVal = (String) handlerCtx.getInputValue("compVal");
        Map<String, String> moduleProps = (Map<String, String>) handlerCtx.getInputValue("moduleProps");
        String webStatUrl = "EMPTY";
        String statType = "EMPTY";
        String monitorEndpoint = GuiUtil.getSessionValue("MONITOR_URL") + "/server/applications/" + app;

        if (compVal == null || compVal.equals("")) {
            for (String vs : vsList) {
                monitorEndpoint = monitorEndpoint + "/" + vs;
                if (doesProxyExist(monitorEndpoint)) {
                    webStatUrl = monitorEndpoint;
                    statType = "Web";
                    break;
                }
            }
        } else {
            String[] compStrs = compVal.split("/");
            if (vsList.contains(compStrs[0])) {
                if (moduleProps.containsKey(compStrs[1]) && moduleProps.get(compStrs[1]).equals("Servlet")) {
                    monitorEndpoint = monitorEndpoint + "/" + compVal;
                    if (doesProxyExist(monitorEndpoint)) {
                        webStatUrl = monitorEndpoint;
                        statType = "ServletInstance";
                    }
                }
            }
        }
        handlerCtx.setOutputValue("webStatUrl", webStatUrl);
        handlerCtx.setOutputValue("webStatType", statType);
    }

    @Handler(id = "getStatsUrl",
      input={
        @HandlerInput(name="app",   type=String.class, required=true),
        @HandlerInput(name="moduleProps",   type=Map.class, required=true),
        @HandlerInput(name="compVal",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "statUrl", type = String.class),
        @HandlerOutput(name = "statType", type = String.class)
       })
    public static void getStatsUrl(HandlerContext handlerCtx) {
        String app = (String) handlerCtx.getInputValue("app");
        String comp = (String) handlerCtx.getInputValue("compVal");
        Map<String, String> moduleProps = (Map<String, String>) handlerCtx.getInputValue("moduleProps");
        String statUrl = "EMPTY";
        String statType = "";
        String monitorUrl = (String) GuiUtil.getSessionValue("MONITOR_URL");

        statUrl = monitorUrl + "/server/applications/" + app + "/" + comp;

        if (comp != null && doesProxyExist(statUrl)) {
            String[] compStrs = comp.split("/");
            if (compStrs.length == 1) {
                statType = (String) moduleProps.get(compStrs[0]);
            } else {
                statType = modifyStatType(compStrs[1]);
            }
        }

        handlerCtx.setOutputValue("statUrl", statUrl);
        handlerCtx.setOutputValue("statType", statType);
    }
    /*
     * Filter the request,session,jsp and servlets. 
     * Filed an issue :12687.
     * Once this issue is resolved, we can remove this handler.
     */
    @Handler(id = "filterWebStats",
      input={
        @HandlerInput(name="webStats",   type=List.class, required=true),
        @HandlerInput(name="statType",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "stats", type = List.class)
       })
    public static void filterWebStats(HandlerContext handlerCtx) {
        List<Map> webStats = (List<Map>) handlerCtx.getInputValue("webStats");
        String statType = (String) handlerCtx.getInputValue("statType");
        List<String> requestStatNames = java.util.Arrays.asList("MaxTime", "ProcessingTime", "RequestCount", "ErrorCount");
        List stats = new ArrayList();
        if (webStats != null) {
            for (Map webStat : webStats) {
                String statName = (String) webStat.get("Name");
                if (requestStatNames.contains(statName) && statType.equals("Request")) {
                    stats.add(webStat);
                } else if (statName.contains(statType) && !(statType.equals("Request"))) {
                    stats.add(webStat);
                }
            }
        }

        handlerCtx.setOutputValue("stats", stats);
    }
    
    /*
     * Returns true if the given pool name is the child of an entity
     * (jdbc connection pools or connector connection pools).
     * This is used in monitoringResourceStats.jsf.
     */
    @Handler(id = "isPool",
      input={
        @HandlerInput(name="poolName",   type=String.class, required=true),
        @HandlerInput(name="endpoint",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "result", type = Boolean.class)
       })
    public static void isPool(HandlerContext handlerCtx) {
        String poolName = (String) handlerCtx.getInputValue("poolName");
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        Boolean result = false;
        try {
            List<String> poolNames = RestApiHandlers.getChildList(endpoint);
            if (poolNames.contains(poolName)) {
                result = true;
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
        
        handlerCtx.setOutputValue("result", result);

    }
    
    /*
     * Returns the jdbc connection pools, connector connection pools,
     * first jdbc element and first connector element for the given set of
     * pool names  and resources endpoint. 
     */
    @Handler(id = "getMonitoringPools",
      input={
        @HandlerInput(name="poolNames",   type=List.class, required=true),
        @HandlerInput(name="endpoint",   type=String.class, required=true)},
       output = {
        @HandlerOutput(name = "jdbcList", type = List.class),
        @HandlerOutput(name = "firstJdbc", type = String.class),
        @HandlerOutput(name = "connectorList", type = List.class),
        @HandlerOutput(name = "firstConnector", type = String.class)
       })
    public static void getMonitoringPools(HandlerContext handlerCtx) {
        List<String> poolNames = (List<String>) handlerCtx.getInputValue("poolNames");
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        List<String> jdbcMonitorList = new ArrayList<String>();
        List<String> connectorMonitorList = new ArrayList<String>();
        String fisrtJdbc = null;
        String firstConnector = null;

        try {
            List<String> jdbcPools = RestApiHandlers.getChildList(endpoint + "/jdbc-connection-pool");
            List<String> connectorPools = RestApiHandlers.getChildList(endpoint + "/connector-connection-pool");
            for (String poolName : poolNames) {
                if (jdbcPools.contains(poolName)) {
                    jdbcMonitorList.add(poolName);
                } else if (connectorPools.contains(poolName)) {
                    connectorMonitorList.add(poolName);
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

        handlerCtx.setOutputValue("jdbcList", jdbcMonitorList);
        handlerCtx.setOutputValue("firstJdbc", fisrtJdbc);
        handlerCtx.setOutputValue("connectorList", connectorMonitorList);
        handlerCtx.setOutputValue("firstConnector", firstConnector);
    }


    public static Boolean doesAppProxyExist(String name, String type) {
        List proxyList = V3AMX.getProxyListByType(type);
        boolean proxyexist = false;
        if (proxyList != null && proxyList.size() != 0) {
            ListIterator li = proxyList.listIterator();
            while (li.hasNext()) {
                String pname = (String) li.next();
                if (pname.contains(name+"/")) {
                    proxyexist = true;
                }
            }
        }
        return proxyexist;
    }

    public static Boolean doesProxyExist(String endpoint) {
        if (RestApiHandlers.get(endpoint).isSuccess()) {
            return true;
        }
        return false;
    }

    public static String modifyStatType(String name) {
        String[] nameStrs = name.split("-");
        String modifiedName = "";
        for (int i = 0; i < nameStrs.length; i++) {
            String tmp = nameStrs[i].substring(0, 1).toUpperCase() + nameStrs[i].substring(1);
            modifiedName = modifiedName + tmp;
        }
        return modifiedName;
    }

    public static List servletInstanceValues(String name, String type, String instance) {
        List proxyList = V3AMX.getProxyListByType(type);
        List servlets = new ArrayList();
        if (proxyList.size() != 0) {
            ListIterator li = proxyList.listIterator();
            while (li.hasNext()) {
                String pname = (String) li.next();
               if (pname.contains(name)) {
                    String vs = "";
                    if (pname.contains(".war")) {
                        vs = pname.substring(pname.lastIndexOf(".war") + 5, pname.lastIndexOf("/"));
                    } else {
                        vs = pname.substring(pname.indexOf("/") + 1, pname.lastIndexOf("/"));
                    }
                    if (instance.equals(vs)) {
                        servlets.add(pname.substring(pname.lastIndexOf("/") + 1, pname.length()));
                    }
                }
            }
        }
        return servlets;
    }

    public static List getAllEjbComps(String appname, String type, String state) {
        //List ejblist = new ArrayList();
        List menuList = new ArrayList();
        List bstate = getEjbComps(appname, type, "");
        if (!bstate.isEmpty()) {
            ListIterator bi = bstate.listIterator();
            while (bi.hasNext() && bi.hasNext()) {
                List ejblist = new ArrayList();
                String name = (String) bi.next();
                ejblist.add(name);
                List bcache = getEjbComps(appname, "bean-cache-mon", name);
                List bpool = getEjbComps(appname, "bean-pool-mon", name);
                List timers = getEjbComps(appname, "ejb-timed-object-mon", name);
                if (!bcache.isEmpty()) {
                    ejblist.addAll(bcache);
                }
                if (!bpool.isEmpty() && bpool.size() > 0) {
                    ejblist.addAll(bpool);
                }
                if (!timers.isEmpty()) {
                    ejblist.addAll(timers);
                }
                if(!ejblist.isEmpty()){
                    menuList.add(ejblist);
                }
            }

        }
        return menuList;
    }

    public static List getEjbComps(String name, String type, String ejbstate) {
        List proxyList = V3AMX.getProxyListByType(type);
        List comps = new ArrayList();
        if (proxyList.size() != 0) {
            ListIterator li = proxyList.listIterator();
            while (li.hasNext()) {
                String pname = (String) li.next();
                if (!ejbstate.isEmpty() || !ejbstate.equals("")) {
                    if ((pname.startsWith(name) || pname.contains("/"+name+"/")) && pname.contains(ejbstate)) {
                        comps.add(pname.substring(pname.lastIndexOf("/") + 1, pname.length()));
                    }
                } else {
                    if (pname.startsWith(name) || pname.contains("/"+name+"/") ) {
                        comps.add(pname.substring(pname.lastIndexOf("/") + 1, pname.length()));

                    }
                }
            }
        }
        return comps;
    }
        private static String formatActiveIdsForDisplay(String str) {
        String values = " ";
        String[] strArray = str.split("%%%EOL%%%");
        if (strArray != null && strArray.length > 0) {
            values = values + "<table>";
            for (String s : (String[]) strArray) {
                if (s.startsWith("Transaction")) {
                    String sh = s.replaceFirst(" ", "_");
                    String[] strHeaders = sh.split(" ");
                    if (strHeaders != null && strHeaders.length > 0) {
                        values = values + "<tr>";
                        for (String h : (String[]) strHeaders) {
                            if (!h.isEmpty()) {
                                values = values + "<td>" + h + "</td>";
                            }

                        }
                        values = values + "</tr>";
                    }
                } else {
                    String[] strData = s.split(" ");
                    if (strData != null && strData.length > 0) {
                        values = values + "<tr>";
                        for (String d : (String[]) strData) {
                            if (!d.isEmpty()) {
                                values = values + "<td>" + d + "</td>";
                            }

                        }
                        values = values + "</tr>";
                    }

                }
            }
            values = values + "</table>";
        }
        return values;
    }
      
    final private static List<String> levels= new ArrayList();
    static{
        levels.add("OFF");
        levels.add("LOW");
        levels.add("HIGH");
    }
    //monitoring component names
    public static final String JRUBY = "JRuby Container";
    public static final String JVM = "JVM";
    public static final String WEB_CONTAINER = "Web Container";
    public static final String HTTP_SERVICE = "HTTP Service";
    public static final String THREAD_POOL = "Thread Pool";
    public static final String JDBC_CONNECTION_POOL = "JDBC Connection Pool";
    public static final String CONNECTOR_CONNECTION_POOL = "Connector Connection Pool";
    public static final String EJB_CONTAINER = "EJB Container";
    public static final String TRANSACTION_SERVICE = "Transaction Service";
    public static final String ORB = "ORB";
    public static final String CONNECTOR_SERVICE = "Connector Service";
    public static final String JMS_SERVICE = "JMS Service";
    public static final String WEB_SERVICES_CONTAINER = "Web Services Container";
    public static final String JPA = "JPA";
    public static final String SECURITY = "Security";
    public static final String JERSEY = "Jersey";

    final private static List monDisplayList= new ArrayList();
    static{
        monDisplayList.add(JVM);
        monDisplayList.add(WEB_CONTAINER);
        monDisplayList.add(HTTP_SERVICE);
        monDisplayList.add(THREAD_POOL);
        monDisplayList.add(JDBC_CONNECTION_POOL);
        monDisplayList.add(CONNECTOR_CONNECTION_POOL);
        monDisplayList.add(EJB_CONTAINER);
        monDisplayList.add(TRANSACTION_SERVICE);
        monDisplayList.add(ORB);
        monDisplayList.add(CONNECTOR_SERVICE);
        monDisplayList.add(JMS_SERVICE);
        monDisplayList.add(WEB_SERVICES_CONTAINER);
        monDisplayList.add(JPA);
        monDisplayList.add(SECURITY);
        monDisplayList.add(JERSEY);
    }
    
    final private static List monNamesList = new ArrayList();
    static{
        monNamesList.add("Jvm");
        monNamesList.add("WebContainer");
        monNamesList.add("HttpService");
        monNamesList.add("ThreadPool");
        monNamesList.add("JdbcConnectionPool");
        monNamesList.add("ConnectorConnectionPool");
        monNamesList.add("EjbContainer");
        monNamesList.add("TransactionService");
        monNamesList.add("Orb");
        monNamesList.add("ConnectorService");
        monNamesList.add("JmsService");
        monNamesList.add("WebServicesContainer");
        monNamesList.add("Jpa");
        monNamesList.add("Security");
        monNamesList.add("Jersey");
    }

    final private static List containerDispList= new ArrayList();
    static{
        containerDispList.add(JRUBY);
    }

    final private static List containerNameList= new ArrayList();
    static{
        containerNameList.add("jruby-container");
    }
}
