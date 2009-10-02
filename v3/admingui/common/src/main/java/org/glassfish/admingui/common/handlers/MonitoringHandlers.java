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

package org.glassfish.admingui.common.handlers;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;

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

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import org.glassfish.admingui.common.util.V3AMX;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;


import javax.management.openmbean.TabularDataSupport;
import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;

/**
 *
 * @author Ana
 */
public class MonitoringHandlers {
    

    @Handler(id = "getMonitorLevels",
    input = {
        @HandlerInput(name = "objectName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "monitorCompList", type = List.class)
    })
    public static void getMonitorLevels(HandlerContext handlerCtx) {
        String objectName = (String) handlerCtx.getInputValue("objectName");
        List result = new ArrayList();
        try {
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectName));
            AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) amx);
            final Map<String, Object> attrs = helper.simpleAttributesMap();
            for (String oneMonComp : attrs.keySet()) {
                //if (oneMonComp.endsWith(".level")){
                if ((!oneMonComp.equals("Parent")) && (!oneMonComp.equals("Children")) && (!oneMonComp.equals("Name")) && (!oneMonComp.equals("Property"))) {
                    Map oneRow = new HashMap();
                    String name = null;
                    if(oneMonComp.equals("Jvm"))
                       name = JVM;
                    if(oneMonComp.equals("WebContainer"))
                        name = WEB_CONTAINER;
                    if(oneMonComp.equals("HttpService"))
                        name = HTTP_SERVICE;
                    if(oneMonComp.equals("ThreadPool"))
                        name = THREAD_POOL;
                    if(oneMonComp.equals("JdbcConnectionPool"))
                        name = JDBC_CONNECTION_POOL;
                    if(oneMonComp.equals("Connector Connection Pool"))
                        name = CONNECTOR_CONNECTION_POOL;
                    if(oneMonComp.equals("EjbContainer"))
                        name = EJB_CONTAINER;
                    if(oneMonComp.equals("TransactionService"))
                        name = TRANSACTION_SERVICE;
                    if(oneMonComp.equals("Orb"))
                        name = ORB;
                    if(oneMonComp.equals("ConnectorService"))
                        name = CONNECTOR_SERVICE;
                    if(oneMonComp.equals("JmsService"))
                        name = JMS_SERVICE;
                    if(oneMonComp.equals("WebServicesContainer"))
                        name = WEB_SERVICES_CONTAINER;
                    if(oneMonComp.equals("Jpa"))
                        name = JPA;
                    if(oneMonComp.equals("Security"))
                        name = SECURITY;
                    if(oneMonComp.equals("Jersey"))
                        name = JERSEY;
                    if(name == null)
                        name = oneMonComp;
                    oneRow.put("monCompName", name);
                    oneRow.put("level", attrs.get(oneMonComp));
                    oneRow.put("selected", false);
                    result.add(oneRow);
                //}
                }
            }
        } catch (Exception ex) {
        }
        handlerCtx.setOutputValue("monitorCompList", result);
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
                        String start = "--";
                        String last = "--";
                        String unit = "";
                        String current = "";
                        Object runtimes = null;
                        Object queuesize = null;
                        String thresholds = "--";
                        boolean nostatskey = true;
                        if (val instanceof CompositeDataSupport) {
                            CompositeDataSupport cds = ((CompositeDataSupport) val);
                            CompositeType ctype = cds.getCompositeType();
                            if (cds.containsKey("statistics")) {
                                nostatskey = false;
                                for (CompositeData cd : (CompositeData[]) cds.get("statistics")) {
                                    Map statsMap = new HashMap();
                                    if (cd.containsKey("name")) {
                                        val = cd.get("name");
                                    }
                                    if (cd.containsKey("description")) {
                                        desc = (String) cd.get("description");
                                    }
                                    if (cd.containsKey("lastSampleTime")) {
                                        last = df.format(new Date((Long) cd.get("lastSampleTime")));
                                    }
                                    if (cd.containsKey("startTime")) {
                                        start = df.format(new Date((Long) cd.get("startTime")));
                                    }
                                    statsMap.put("Name", monName);
                                    statsMap.put("StartTime", start);
                                    statsMap.put("LastTime", last);
                                    statsMap.put("Description", desc);
                                    statsMap.put("Value", (val == null) ? "" : val);
                                    statsMap.put("Details", "--");
                                    result.add(statsMap);
                                }

                            } else {
                                if (cds.containsKey("name")) {
                                    statMap.put("Name", cds.get("name"));
                                } else {
                                    statMap.put("Name", monName);
                                }
                                if (cds.containsKey("unit")) {
                                    unit = (String) cds.get("unit");
                                }
                                if (cds.containsKey("description")) {
                                    desc = (String) cds.get("description");
                                }
                                if (cds.containsKey("startTime")) {
                                    start = df.format(new Date((Long) cds.get("startTime")));
                                }
                                if (cds.containsKey("lastSampleTime")) {
                                    last = df.format(new Date((Long) cds.get("lastSampleTime")));
                                }
                                if (cds.containsKey("maxTime")) {
                                    details = (GuiUtil.getMessage("monitoring.MaxTime") + ": " + cds.get("maxTime") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("minTime")) {
                                    details = details + (GuiUtil.getMessage("monitoring.MinTime") + ": " + cds.get("minTime") + " " + unit + "<br/>");
                                }
                                if (cds.containsKey("totalTime")) {
                                    details = details + (GuiUtil.getMessage("monitoring.TotalTime") + ": " + cds.get("totalTime") + " " + unit + "<br/>");
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
                                    val = cds.get("current");
                                } else {
                                    val = "--";
                                }
                            }
                        } else if (val instanceof String[]) {
                            statMap.put("Name", monName);
                            String values = "";
                            for (String s : (String[]) val) {
                                values = values + s + "<br/>";

                            }
                            val = values;
                        } else if (val instanceof CompositeData[]) {
                            String apptype = "";
                            for (CompositeData cd : (CompositeData[]) val) {
                                if(cd.containsKey("appName")) {
                                    statMap.put("Name", cd.get("appName"));
                                }
                                if(cd.containsKey("applicationType")) {
                                    apptype = (String)cd.get("applicationType");
                                }
                                if(cd.containsKey("queueSize") && cd.containsKey("jrubyVersion")) {
                                    details = details + cd.get("environment") + " " + cd.get("jrubyVersion");
                                }
                            }
                            val = apptype;
                        } else {
                            statMap.put("Name", monName);
                        }
                        if (nostatskey) {
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
        @HandlerInput(name = "objectName", type = String.class)})
    public static void updateMonitorLevels(HandlerContext handlerCtx) {
        String objectNameStr = (String) handlerCtx.getInputValue("objectName");
        List<Map<String,String>> allRows = (List<Map<String,String>>) handlerCtx.getInputValue("allRows");
        for (Map<String, String> oneRow : allRows) {
            String name = oneRow.get("monCompName");
            String value = null;
            if (name.equals(JVM))
                value = "Jvm";
            if (name.equals(WEB_CONTAINER)) 
                value = "WebContainer";
            if (name.equals(HTTP_SERVICE))
                value = "HttpService";
            if (name.equals(THREAD_POOL))
                value = "ThreadPool";
            if (name.equals(JDBC_CONNECTION_POOL)) 
                value = "JdbcConnectionPool";
            if (name.equals(CONNECTOR_CONNECTION_POOL)) 
                value = "Connector Connection Pool";
            if (name.equals(EJB_CONTAINER)) 
                value = "EjbContainer";
            if (name.equals(TRANSACTION_SERVICE)) 
                value = "TransactionService";
            if (name.equals(ORB)) 
                value = "Orb";
            if (name.equals(CONNECTOR_SERVICE)) 
                value = "ConnectorService";
            if (name.equals(JMS_SERVICE))
                value = "JmsService";
            if (name.equals(WEB_SERVICES_CONTAINER))
                value = "WebServicesContainer";
            if (name.equals(JPA))
                value = "Jpa";
            if (name.equals(SECURITY))
                value = "Security";
            if (name.equals(JERSEY)) 
                value = "Jersey";
            if(value == null)
                value = name;
            V3AMX.setAttribute(objectNameStr, new Attribute(value, oneRow.get("level")));
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
    

    final private static List<String> levels= new ArrayList();
    static{
        levels.add("OFF");
        levels.add("LOW");
        levels.add("HIGH");
    }
    //monitoring component names
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



}
