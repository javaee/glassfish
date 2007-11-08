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
 * LogHandlers.java
 *
 * Created on Sept 21, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

//import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.MSVValidator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.Random;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.handlers.NavigationHandlers;
import com.sun.jsftemplating.component.ComponentUtil;

import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.CallFlowMonitor;

import com.sun.appserv.management.ext.logging.Logging;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

/**
 *
 * @author Anissa Lam
 */
public class LogHandlers {
    
    /** Creates a new instance of LogHandlers */
    public LogHandlers() {
    }
   
    @Handler(id="getErrorInfoList",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="doCharting", type=Boolean.class),
            @HandlerInput(name="demo", type=String.class)},
        output={
            @HandlerOutput(name="chartValues", type=java.util.List.class),
            @HandlerOutput(name="chartLabels", type=java.util.List.class),
            @HandlerOutput(name="chartTitle", type=String.class),
            @HandlerOutput(name="hasChart", type=Boolean.class),
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="hasResult", type=Boolean.class)}
     )
     public static void getErrorInfoList(HandlerContext handlerCtx){
        String instanceName = (String )handlerCtx.getInputValue("instanceName");
        Map infoMap[] = null;
        Logging logging = getLoggingBean(instanceName);
        
        if (logging != null)
            infoMap = logging.getErrorInfo();
        
        
        List result = new ArrayList();
        
        if (infoMap == null || infoMap.length <= 0){
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("hasResult", false);
            handlerCtx.setOutputValue("hasChart", false);
            return;
        }
        
        
        //We want to filter out the -1 data from the returned information map, otherwise, if the user has set a big
        //retain error attribute, most of the data will be N/A on screen.
        ArrayList smaller = new ArrayList();
        
        for(int i=0; (i< 5) && (i < infoMap.length); i++){
            smaller.add(infoMap[i]);
        }
        for(int i=5; i < infoMap.length; i++){
            Map attrs = infoMap[i];
            Integer severe = (Integer) attrs.get(Logging.SEVERE_COUNT_KEY);
            if (severe.intValue()<0){
                break;
            }
            smaller.add(infoMap[i]);
        }

        List severeList = new ArrayList();
        List warningList = new ArrayList();
        List labelList = new ArrayList();
        
        //DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
        int size = smaller.size() -1;
        int mod = 0;
        if ( size > 50) 
            mod = 10;
        else
        if (size > 20){
            mod = 5;
        }
        
        int countLabel = 0;
        for( ; size >= 0; size--){
            Map attrs = (Map) smaller.get(size);
            Map oneRow = new HashMap();
            Long ms = (Long) attrs.get(Logging.TIMESTAMP_KEY);
            oneRow.put ("timeStamp", ms);
            
            
            //String formattedTime = dateFormat.format(new Date(ms.longValue()));
            
            oneRow.put("timeStampFormatted", getFormattedTime(ms.toString()));
            
            Integer severe = (Integer) attrs.get(Logging.SEVERE_COUNT_KEY);
            oneRow.put( "severeString", (severe.intValue() < 0) ? GuiUtil.getMessage("common.NA") : severe.toString());
            oneRow.put( "severeCount", (severe.intValue() < 0) ? Integer.parseInt("-1") : severe);
            
            Integer warning = (Integer) attrs.get(Logging.WARNING_COUNT_KEY);
            oneRow.put( "warningString", (warning.intValue() < 0) ? GuiUtil.getMessage("common.NA") : warning.toString());
            oneRow.put( "warningCount", (warning.intValue() < 0) ? Integer.parseInt("-1") : warning);
            
            oneRow.put("selected", false);
            if ((severe.intValue() <=0) && (warning.intValue() <= 0))
                oneRow.put("disabled", true);
            else
                oneRow.put("disabled", false);
            result.add(oneRow);
            
            
            //For charting
            if (severe.intValue() < 0){
                severe=new Integer(0);
            }
            if (warning.intValue() < 0){
                warning=new Integer(0);
            }
            severeList.add(severe);
            warningList.add(warning);
            
            DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, GuiUtil.getLocale());
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, GuiUtil.getLocale());
            
            boolean putLabel = true;
            if (mod != 0){
                putLabel = ((countLabel++ % mod) == 0);
            }
            Date mm = new Date(ms);
            if(putLabel){
                Map sMap = new HashMap();
                sMap.put("title", dateTimeFormat.format(mm)+ "(" + severe + ", " + warning + ")");
                sMap.put("label", timeFormat.format(mm));
                labelList.add(sMap);
            }else{
                Map sMap = new HashMap();
                sMap.put("label", " ");
                sMap.put("title", " ");
                labelList.add(sMap);
            }
        }
        
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("hasResult", true);
        
        //By default, charting info will be generated. 
        Boolean doCharting = (Boolean )handlerCtx.getInputValue("doCharting");
        
        //TODO need optimization
        /*
        if (doCharting != null && !doCharting){
            handlerCtx.setOutputValue("hasChart", false);
            return;
        }
         */
        
        
        String demo = (String )handlerCtx.getInputValue("demo");
        if (!GuiUtil.isEmpty(demo)){
            labelList = new ArrayList();
            int ct = Integer.parseInt(demo);
            int countLabel2 = 0;
            int mod2 = 0;
            if (ct > 50) 
                mod2 = 10;
            else
            if (ct > 20){
                mod2 = 5;
            }
            Random random = new Random();
            severeList = new ArrayList();
            warningList = new ArrayList();
            Date current = new Date(System.currentTimeMillis());
            DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, GuiUtil.getLocale());
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, GuiUtil.getLocale());
            for(int i=0; i<ct; i++){
                Integer severe = new Integer(Math.abs(random.nextInt()%30));
                severeList.add(severe);
                Integer warning = new Integer(Math.abs(random.nextInt()%30));
                warningList.add(warning);
                
                boolean putLabel = true;
                if (mod2!= 0){
                    putLabel = ((countLabel2 ++ % mod2) == 0);
                }
                if(putLabel){
                    Map sMap = new HashMap();
                    sMap.put("title", dateTimeFormat.format(current)+ "(" + severe + ", " + warning + ")");
                    sMap.put("label", timeFormat.format(current));
                    labelList.add(sMap);
                }else{
                    Map sMap = new HashMap();
                    sMap.put("label", " ");
                    sMap.put("title", " ");
                    labelList.add(sMap);
                }
            }
        }
        
        Map sMap = new HashMap();
        sMap.put("color", "red" );
        sMap.put("label", GuiUtil.getMessage("logAnalyzer.chart.legend.severe") );
        sMap.put("values", severeList);
        
        Map wMap = new HashMap();
        wMap.put("color", "pink");
        wMap.put("label", GuiUtil.getMessage("logAnalyzer.chart.legend.warning") );
        wMap.put("values", warningList);
        
        List valueList = new ArrayList();
        valueList.add(sMap);
        valueList.add(wMap);
        Long start = (Long) ((Map) result.get(0)).get("timeStamp");
        Long end = (Long) ((Map) result.get(result.size()-1)).get("timeStamp");
        String startStr = getFormattedTime(start.toString() ); 
        String endStr = getFormattedTime(end.toString() );
        String titleStr = GuiUtil.getMessage("logAnalyzer.chart.title", new Object[]{startStr, endStr});
        
        
        handlerCtx.setOutputValue("chartValues", valueList);
        handlerCtx.setOutputValue("chartLabels", labelList);
        handlerCtx.setOutputValue("chartTitle", titleStr);
        handlerCtx.setOutputValue("hasChart", doCharting);  //TODO optimization
    }
    
    
     @Handler(id="getErrorLoggersList",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="timeStamp", type=String.class, required=true),
            @HandlerInput(name="doCharting", type=Boolean.class, required=false),
            @HandlerInput(name="demo", type=String.class)},
        output={
            @HandlerOutput(name="hasResults", type=Boolean.class),
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="severeChartValues", type=java.util.List.class),
            @HandlerOutput(name="severeChartLabels", type=java.util.List.class),
            @HandlerOutput(name="hasSevereChart", type=Boolean.class),
            @HandlerOutput(name="warningChartValues", type=java.util.List.class),
            @HandlerOutput(name="warningChartLabels", type=java.util.List.class),
            @HandlerOutput(name="hasWarningChart", type=Boolean.class)}
     )
     public static void getErrorLoggersList(HandlerContext handlerCtx){
        String instanceName = (String )handlerCtx.getInputValue("instanceName");
        String ts = (String )handlerCtx.getInputValue("timeStamp");
        Long timeStamp = Long.parseLong(ts);
        Logging logging = getLoggingBean(instanceName);
        List result = new ArrayList();
        
        if (logging == null ){
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("hasResults", false);
            handlerCtx.setOutputValue("hasSevereChart", false);
            handlerCtx.setOutputValue("hasWarningChart", false);
            return;
        }
        
        Map<String, Integer> warningMap = logging.getErrorDistribution(timeStamp, ""+Level.WARNING);
        Map<String, Integer> severeMap = logging.getErrorDistribution(timeStamp, ""+Level.SEVERE);
        Map<String, Integer[]>  combinedMap = new HashMap();
        String demo = (String )handlerCtx.getInputValue("demo");
        
        // combine date from both map
        
        if (GuiUtil.isEmpty(demo)){
            for(String module : severeMap.keySet()){
                Integer[] counts = {severeMap.get(module), Integer.parseInt("0")};
                combinedMap.put(module, counts);
            }
            for(String module: warningMap.keySet()){
                Integer[] counts = combinedMap.get(module);
                if (counts == null){
                    Integer[] newCounts = {Integer.parseInt("0"), warningMap.get(module)};
                    combinedMap.put(module, newCounts);
                }else{
                    counts[1] = warningMap.get(module);
                }
            }

            for(String module: combinedMap.keySet()){
                Map oneRow = new HashMap();
                oneRow.put("loggerName", module);
                Integer counts[] = combinedMap.get(module);
                oneRow.put("severe", counts[0]);
                oneRow.put("warning", counts[1]);
                result.add(oneRow);
            }
        }
        else {
            int ct = Integer.parseInt(demo);
            String module="com.abc.demo.aa";
            Random random = new Random();
            result = new ArrayList();
            severeMap = new HashMap();
            warningMap = new HashMap();
            for(int i=0; i<ct; i++){
                Map oneRow = new HashMap();
                oneRow.put("loggerName", module+i);
                Integer sev = new Integer(Math.abs(random.nextInt()%20));
                Integer war = new Integer(Math.abs(random.nextInt()%20));
                severeMap.put( module+i, sev);
                //warningMap.put(module+i, war);
                oneRow.put("severe", sev);
                oneRow.put("warning", war);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("hasResults", (result.size()>0));
        
        //By default, charting info will be generated. 
        Boolean doCharting = (Boolean )handlerCtx.getInputValue("doCharting");
        if (doCharting != null && !doCharting){
            handlerCtx.setOutputValue("hasSevereChart", false);
            handlerCtx.setOutputValue("hasWarningChart", false);
            //TODO need optimization
            // return;
        }
        
        List[] sList= createChartData(severeMap, "red");
        List[] wList = createChartData(warningMap, "pink");
       
        if (sList == null){
            handlerCtx.setOutputValue("hasSevereChart", false);
        }else{
            handlerCtx.setOutputValue("severeChartLabels", sList[0]);
            handlerCtx.setOutputValue("severeChartValues", sList[1]);
            handlerCtx.setOutputValue("hasSevereChart", /* true */ doCharting);  //TODO for optimize
        }
        
        if (wList == null){
            handlerCtx.setOutputValue("hasWarningChart", false);
        }else{
            handlerCtx.setOutputValue("warningChartLabels", wList[0]);
            handlerCtx.setOutputValue("warningChartValues", wList[1]);
            handlerCtx.setOutputValue("hasWarningChart", /* true */ doCharting);  //TODO for optimize
        }
        
        
    }
    
    @Handler(id="getFormattedTime",
        input={
            @HandlerInput(name="ms", type=String.class)},
        output={
            @HandlerOutput(name="value", type=String.class)}
     )
     public static void getFormattedTime(HandlerContext handlerCtx) {
          String ms = (String )handlerCtx.getInputValue("ms");
          handlerCtx.setOutputValue("value", getFormattedTime(ms));
      }
    
    private static List[] createChartData(Map<String, Integer> mapDist, String color){
        
        List labelList = new ArrayList();
         for(String module : mapDist.keySet()){
             if (mapDist.get(module).intValue() <= 0)
                 continue;
             Map aMap = new HashMap();
             int lastIndex = module.lastIndexOf(".");
             if (lastIndex == -1){
                 aMap.put("label", module);
                 
             }else{
                 aMap.put("label", module.substring(lastIndex+1, module.length()));
                 aMap.put("title", module + " (" + mapDist.get(module)+")");
             }
             aMap.put("compValue", mapDist.get(module));
             labelList.add(aMap);
         }
         
         if (labelList.size() ==0){
            return null;
         }
         
         Collections.sort(labelList, new moduleCountComparator());
         
         if(labelList.size() > 10){
             int cnt = 0;
             for(int i=10; i< labelList.size(); i++){
                Integer tmp =(Integer) ((Map) labelList.get(i)).get("compValue");
                cnt += tmp.intValue();
             }
             labelList.subList(10, labelList.size()).clear();
             Map aMap = new HashMap();
             aMap.put("label", GuiUtil.getMessage("logAnalyzerLoggers.chart.other"));
             aMap.put("compValue", new Integer(cnt));
             labelList.add(aMap);
         }
         
         // create the value list that corresponds to the Labels
         /* The following will be converted into something like
            {color: 'green',    values : [95,5,0,0,0] }
          */
         List valueList = new ArrayList();
         for(int i=0;  i < labelList.size() ;  i++){
             valueList.add (((Map)labelList.get(i)).get("compValue"));
         }
         
         Map vMap = new HashMap();
         vMap.put("color", color);
         vMap.put("values", valueList);
         List vList = new ArrayList();
         vList.add(vMap);
         
         List[] result = new List[] {labelList, vList};
         return result;
    }
    
    
    private static String getFormattedTime(String ms){
        Date useThis = null;
        if (ms == null || "".equals(ms)){
            useThis = new Date(System.currentTimeMillis());
        }else{
            useThis = new Date( Long.parseLong(ms));
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
        return dateFormat.format(useThis);
    }
    
    
    private static Logging getLoggingBean(String instanceName){
        ServerRootMonitor serverRootMonitor = AMXUtil.getServerRootMonitor(instanceName);
        if (serverRootMonitor == null)
            return null;
        return serverRootMonitor.getLogging();
    }
    
    /*
        Compare Time Spent Maps (for sorting). 
     */
    private final static class moduleCountComparator implements java.util.Comparator
    {
        public int compare( Object o1, Object o2 )
        {
            Integer f1 = (Integer) ((Map)o1).get("compValue");
            Integer f2 = (Integer) ((Map)o2).get("compValue");
            return( f2.compareTo(f1) ); 
        }

        public boolean  equals( Object other )
        {
            return( other instanceof moduleCountComparator );
        }
    }
}
