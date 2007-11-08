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
 * MonitorHandlers.java
 *
 * Created on January 11, 2006, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Stack;

import javax.faces.model.SelectItem;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.LogServiceConfig;
import com.sun.appserv.management.config.ProfilerConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import com.sun.appserv.management.config.DASConfig;
import com.sun.appserv.management.config.AdminServiceConfig;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.transaction.monitor.JTSMonitorMBean;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import javax.faces.context.FacesContext;

import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.OptionsList;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.Separator;
import com.sun.webui.jsf.model.SingleSelectOptionsList;
import com.sun.webui.jsf.component.DropDown;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 *
 * @author Administrator
 */
public class MonitorHandlers {
     private boolean firstObjectSet = false;
    
    private boolean addOption(String objectName, ArrayList optionList, OptionGroup optionGroup, HandlerContext ctx) {
        boolean itemsAdded = false;
        try {
            // check to see if there will be anything to display...
            Object [] stats = (Object []) JMXUtil.invoke(objectName, "getStatistics", null, null);
            if (stats == null || stats.length == 0)
                return itemsAdded; //nothing to add
        } catch (Exception ex) {
            //System.out.println("exception: "+ex.getMessage());
            return itemsAdded;
        }
        // return the first object in the drop-down menu list.
        if (firstObjectSet == false) {
            firstObjectSet = true;
            ctx.setOutputValue("ObjectName",  objectName);
        }

     // add name and objectName to the drop-down menu list.
        String name = (String) JMXUtil.invoke(objectName, "getName", null, null);
        if (optionGroup != null) {
            Option option = new Option();
            option.setLabel(name);
            option.setValue(objectName);
            Option[] o = optionGroup.getOptions();
            if (o == null) {
                optionGroup.setOptions(new Option[] {option}); 
            } else {
                ArrayList list = new ArrayList();
                for (int i=0; i < o.length; i++) {
                    list.add(o[i]);
                }
                list.add(option);
                Option[] oNew = (Option[])list.toArray(new Option[list.size()]);
                optionGroup.setOptions(oNew);
            }
            itemsAdded = true;
        } else {
            optionList.add(new Option(objectName, name));
        }
        return itemsAdded;
    }
    
    private boolean fillMenuOptions(String objectName, ArrayList optionList, 
            OptionGroup optionGroup, Stack ogStack, boolean doGrouping, 
            HandlerContext ctx) {
        boolean itemsAdded = false;
        
        ObjectName[] childObjects =  null;
        try {
            childObjects = (ObjectName[])JMXUtil.invoke(objectName, "getChildren", null, null);
        } catch (Exception ex) {
            //System.out.println("exception: "+ex.getMessage());
            //return false;
        }
        if (childObjects != null && childObjects.length > 0) {
             OptionGroup og = optionGroup;
             if (doGrouping) {
                String name = (String) JMXUtil.invoke(objectName, "getName", null, null);
                og = new OptionGroup();
                og.setLabel(name);
                og.setValue(name);
            }
            // add the options of this node
            itemsAdded = addOption(objectName, optionList, og, ctx);
            // add options for any child nodes
            for (int i=0; i<childObjects.length; i++) {
                if (childObjects[i] != null) {
                    itemsAdded |= fillMenuOptions(childObjects[i].toString(), optionList, og, ogStack, doGrouping, ctx);
                }
            }
            if (itemsAdded && doGrouping) {
                // add the option group to the option list if anything was added
                ogStack.push(og); // add it after the parent.
                //optionList.add(og);
                return false;
            }
        } else {
            // just add an option of this objectName, if it has statistics.
            itemsAdded = addOption(objectName, optionList, optionGroup, ctx);
        }
        return itemsAdded;
    }
    
    
    /**
     *	<p> Returns the list of monitorable runtime components</p>
     * 
     *  <p> Input value: "MonitorObjects" -- Type: <code>java.util.List</code></p>
     *  <p> Input value: "DoGrouping" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "ObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MonitorList" -- Type: <code>com.sun.webui.jsf.model.Option[]</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="populateMonitorDropDown",
        input={
            @HandlerInput(name="MonitorObjects", type=List.class, required=true),
            @HandlerInput(name="DoGrouping", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="ObjectName", type=String.class),
            @HandlerOutput(name="MonitorList", type=Option[].class)})
    public void populateMonitorDropDown(HandlerContext handlerCtx) {
        ArrayList objs = (ArrayList) handlerCtx.getInputValue("MonitorObjects");
        Boolean doGrouping = (Boolean) handlerCtx.getInputValue("DoGrouping");
        ArrayList optionList  = new ArrayList();
        optionList.add(new Option("",""));
        Stack optGroupStack = new Stack();
        firstObjectSet = false;
        boolean itemsAdded = false;
        Option[] optionArr = new Option[0];
        if (objs != null) {
            for ( int i=0; i<objs.size(); i++) {
                String objectName = (String)objs.get(i);
                if (objectName != null) {
                    itemsAdded = fillMenuOptions(objectName, optionList, null,
                        optGroupStack, doGrouping.booleanValue(), handlerCtx);
                    while (optGroupStack.empty() == false) {
                        optionList.add((OptionGroup)optGroupStack.pop());
                    }
                }
            }
            optionArr = (Option[])optionList.toArray(new Option[optionList.size()]);
        }
        handlerCtx.setOutputValue("MonitorList", optionArr);
    }
    
    
       /**
     *	<p> Returns the list of applications for an applications object name</p>
     * 
     *  <p> Input value: "AppsObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ApplicationList" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="populateMonitorAppsDropDown",
        input={
            @HandlerInput(name="AppsObjectName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="ApplicationList", type=List.class)})
    public void populateMonitorAppsDropDown(HandlerContext handlerCtx) {
        String appsObject = (String) handlerCtx.getInputValue("AppsObjectName");
        ObjectName[] objs =  null;
        try {
            objs = (ObjectName[])JMXUtil.invoke(appsObject, "getChildren", null, null);
        } catch (Exception ex) {
            // ignore
        }
        ArrayList optionList = new ArrayList();
        optionList.add(new Option("",""));
        if (objs != null) {
            for ( int i=0; i<objs.length; i++) {
                String name = null;
                try {
                    name = (String) JMXUtil.invoke(objs[i], "getName", null, null);
                } catch (Exception ex) {
                    continue; // if can't get the name, skip it.
                }
                optionList.add(new Option(objs[i].toString(), name));
            }
        }
        handlerCtx.setOutputValue("ApplicationList", optionList);
    }
    
    /**
     *	<p> Returns the statistics data for the given monitorable object</p>
     * 
     *  <p> Input value: "MonitorObject" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "StatisticData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getStatisticData",
        input={
            @HandlerInput(name="MonitorObject", type=String.class, required=true)},
        output={
            @HandlerOutput(name="StatisticData", type=List.class)})
    public void getStatisticData(HandlerContext handlerCtx) {
        String obj = (String) handlerCtx.getInputValue("MonitorObject");
        List dataList = new ArrayList();
        if (!GuiUtil.isEmpty(obj)) {
            Statistic[] stats = (Statistic[]) JMXUtil.invoke(obj, "getStatistics", null, null);
            Locale locale = handlerCtx.getFacesContext().getViewRoot().getLocale();
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
            if (stats != null) {
                for (int i=0; i < stats.length; i++) {
                    Map statMap = new HashMap();
                    if (stats[i] instanceof BoundedRangeStatistic) {
                        BoundedRangeStatistic stat = (BoundedRangeStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", nf.format(stat.getCurrent()) + " " + stat.getUnit());
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details",
                                GuiUtil.getMessage("monitoring.HighWater")+ ": " + nf.format(stat.getHighWaterMark()) + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.LowWater")+": " + nf.format(stat.getLowWaterMark()) + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.UpperBound")+": " + nf.format(stat.getUpperBound()) + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.LowerBound")+": " + nf.format(stat.getLowerBound())+ " " + stat.getUnit()+ "<br/>");
                    } else if (stats[i] instanceof BoundaryStatistic) {
                        BoundaryStatistic stat = (BoundaryStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", "");
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details",
                                GuiUtil.getMessage("monitoring.UpperBound")+": " + stat.getUpperBound() + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.LowerBound")+": " + stat.getLowerBound()+ " " + stat.getUnit()+ "<br/>");
                    } else if (stats[i] instanceof RangeStatistic) {
                        RangeStatistic stat = (RangeStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", stat.getCurrent() + " " + stat.getUnit());
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details",
                                GuiUtil.getMessage("monitoring.HighWater")+": " + stat.getHighWaterMark() + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.LowWater")+": " + stat.getLowWaterMark() + " " + stat.getUnit()+ "<br/>");
                    } else if (stats[i] instanceof CountStatistic) {
                        CountStatistic stat = (CountStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", stat.getCount() + " " + stat.getUnit());
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details", "");
                    } else if (stats[i] instanceof TimeStatistic) {
                        TimeStatistic stat = (TimeStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", stat.getCount() + " " + stat.getUnit());
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details",
                                GuiUtil.getMessage("monitoring.MaxTime")+": " + stat.getMaxTime() + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.MinTime")+": " + stat.getMinTime() + " " + stat.getUnit() + "<br/>" +
                                GuiUtil.getMessage("monitoring.TotalTime")+": " + stat.getTotalTime() + " " + stat.getUnit()+ "<br/>");
                    } else if (stats[i] instanceof StringStatistic) {
                        StringStatistic stat = (StringStatistic)stats[i];
                        statMap.put("Name", stat.getName());
                        statMap.put("Value", stat.getCurrent());
                        statMap.put("Description", stat.getDescription());
                        statMap.put("StartTime", df.format(new Date(stat.getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stat.getLastSampleTime())));
                        statMap.put("Details", "");
                    } else if (stats[i] instanceof Statistic) {
                        statMap.put("Name", stats[i].getName());
                        statMap.put("Value", stats[i].getUnit());
                        statMap.put("Description", stats[i].getDescription());
                        statMap.put("StartTime", df.format(new Date(stats[i].getStartTime())));
                        statMap.put("LastSampleTime", df.format(new Date(stats[i].getLastSampleTime())));
                        statMap.put("Details", "");
                    }
                    dataList.add(statMap);
                }
            }
        }
        handlerCtx.setOutputValue("StatisticData", dataList);
    }
    
    /**
     *	<p> Returns the active transactions</p>
     * 
     *  <p> Input value: "ObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TransactionData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTransactionData",
        input={
            @HandlerInput(name="ObjectName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="TransactionData", type=List.class)})
    public void getTransactionData(HandlerContext handlerCtx) {
        String obj = (String) handlerCtx.getInputValue("ObjectName");
        List dataList = new ArrayList();
        if (obj != null) { 
            try {
                List transList = (List) JMXUtil.invoke(obj, "listActiveTransactions", null, null);
                for (Iterator iter = transList.iterator(); iter.hasNext();) {
                    Map transMap = (Map)iter.next();
                    transMap.put("Id", (String)transMap.get(JTSMonitorMBean.TRANSACTION_ID));
                    transMap.put("State", (String)transMap.get(JTSMonitorMBean.STATE));
                    transMap.put("Time", (String)transMap.get(JTSMonitorMBean.ELAPSED_TIME));
                    transMap.put("Component", (String)transMap.get(JTSMonitorMBean.COMPONENT_NAME));
                    transMap.put("Resources", (String)transMap.get(JTSMonitorMBean.RESOURCE_NAMES));
                    dataList.add(transMap);
                }
            } catch (Exception ex) {
                // ignore
            }
        }    
        handlerCtx.setOutputValue("TransactionData", dataList);
    }
    
    /**
     *	<p> Returns the active transactions</p>
     * 
     *  <p> Input value: "ObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TransactionData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="setTransactionPageTitle",
        input={
            @HandlerInput(name="State", type=String.class, required=true)},
        output={
            @HandlerOutput(name="Status", type=String.class)})
    public void setTransactionPageTitle(HandlerContext handlerCtx) {
        String state = (String)handlerCtx.getInputValue("State");
        String status = GuiUtil.getMessage("transactionId.Unknown");
        if (state != null) {
            status = state.equals("False")?GuiUtil.getMessage("transactionId.UnFreeze"):GuiUtil.getMessage("transactionId.Freeze");
        }
        handlerCtx.setOutputValue("Status", status);
    }
    
    /**
     *	<p> Returns the active transactions</p>
     * 
     *  <p> Input value: "ObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TransactionData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="setTransactionState",
        input={
            @HandlerInput(name="ObjectName", type=String.class, required=true),
            @HandlerInput(name="State", type=String.class, required=true)})
    public void setTransactionState(HandlerContext handlerCtx) {
        String objectName = (String)handlerCtx.getInputValue("ObjectName");
        String state = (String)handlerCtx.getInputValue("State");
        String methodName = state.equals("False")?"freeze":"unfreeze";
        JMXUtil.invoke(objectName, methodName, null, null);
    }

 }
