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
 * MonitorEventFactory.java
 *
 * Created on May 24, 2005, 3:48 PM
 */

package com.sun.enterprise.admin.selfmanagement.event;


import javax.management.NotificationEmitter;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.StringMonitor;
import javax.management.monitor.Monitor;
import javax.management.MBeanServer;
import java.util.Hashtable;
import javax.management.ObjectName;
import java.util.logging.Level;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static com.sun.enterprise.admin.selfmanagement.event.ManagementRuleConstants.*;

/**
 *
 * This is the factory class to create and configure MonitorEventType.
 *
 * @author Sun Micro Systens, Inc
 */

public final class MonitorEventFactory extends EventAbstractFactory {
    
    private MonitorEventFactory( ) {
        super();
        EventBuilder.getInstance().addEventFactory(EVENT_MONITOR, this);
    }
    
    
    public Event instrumentEvent(
            ElementProperty[] properties, String description ) {
        if( properties == null ){
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","null properties","monitor"));
        }
        Hashtable<String, String> table = new Hashtable<String,String>();
        for( int i = 0; i < properties.length; i++ ){
            table.put( properties[i].getName( ).toLowerCase(), properties[i].getValue());
        }

        boolean isMustang = false;
        boolean isComplexType = false;

        String monitorType = table.get(PROPERTY_MONITOR_TYPE);
        if (monitorType == null)
            monitorType = PROPERTY_MONITOR_COUNTER;
        else
            monitorType = monitorType.toLowerCase();
        
        String observedAttribute =  table.get(PROPERTY_MONITOR_OBSERVED_ATTRIBUTE);
        if (observedAttribute == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_OBSERVED_ATTRIBUTE,"monitor"));
        }

        if (observedAttribute.indexOf('.') != -1) {
            isComplexType = true;
        }
        Float jdkVersion = new Float( System.getProperty("java.specification.version") );
        if( jdkVersion != null ) {
            if( jdkVersion.floatValue() >= 1.5 ) 
                isMustang = true;
        } else {
            Float javaVersion = new Float( System.getProperty("java.version") );
            if( javaVersion.floatValue() >= 1.5 )
                isMustang = true;
        }

        if( isMustang && isComplexType ) {
            StatisticMonitor monitor = null;
            if( monitorType.equals(PROPERTY_MONITOR_COUNTER)){
                monitor = createCounterStatisticMonitor(table);
            } else if( monitorType.equals(PROPERTY_MONITOR_GAUGE)){
                monitor = createGaugeStatisticMonitor(table);
            } else if( monitorType.equals(PROPERTY_MONITOR_STRING)) {
                monitor = createStringStatisticMonitor(table);
            }
            if( monitor == null ){
                _logger.log(Level.WARNING,"smgt.internal_error");
                return null;
            }
            try {
                String sourceMbeanObjName = null;
                String sourceMbeanName = null;
                if (table.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ)) {
                    sourceMbeanObjName = table.get(PROPERTY_MONITOR_OBSERVED_OBJ);
                } else if (table.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME)) {
                    sourceMbeanName = table.get(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME);
                }
                if (sourceMbeanName ==  null && sourceMbeanObjName == null) {
                    throw new IllegalArgumentException(
                            sm.getString("selfmgmt_event.invalid_event_property","observedobject","monitor"));
                }
                String sourceMbean = null;
                if(sourceMbeanObjName != null) {
                    //final String serverNameVal  = System.getProperty("com.sun.aas.instanceName");
                    Pattern pat = Pattern.compile("\\$\\{instance.name\\}");
                    Matcher m = pat.matcher(sourceMbeanObjName);
                    if(m.find()) {
                        sourceMbean = m.replaceAll(instanceName);
                    } else {
                        sourceMbean = sourceMbeanObjName;
                    }
                } else if(sourceMbeanName != null) {
                    sourceMbean = ManagementRulesMBeanHelper.getObjName(sourceMbeanName);
                }
                /*if (!(sourceMbean.endsWith(",server=" + instanceName))) {
                    sourceMbean = sourceMbean + ",server=" + instanceName;
                }*/
                ObjectName objName = new ObjectName(sourceMbean);
                //ObjectName objName = new ObjectName(table.get(PROPERTY_MONITOR_OBSERVED_OBJ));
                monitor.addObservedObject(objName);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","observedobject","monitor"), ex);
            }

            monitor.setObservedAttribute(observedAttribute);

            String granularityPeriod =  table.get(PROPERTY_MONITOR_GRANULARITY_PERIOD);
            if (granularityPeriod != null) {
                try {
                    long gPeriod = Long.parseLong(granularityPeriod);
                    monitor.setGranularityPeriod(gPeriod);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_GRANULARITY_PERIOD,"monitor"), ex);
                }
            }
                                                                                                                                               
            ObjectName objName = null;
            try {
                table.put("version",getNewVersionString());
                Hashtable<String,String> t = (Hashtable<String,String>)table.clone();
                if(t.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME)) {
                    t.remove(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME);
                } else if(t.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ)) {
                    t.remove(PROPERTY_MONITOR_OBSERVED_OBJ);
                }
                objName = new ObjectName(MonitorEvent.DOMAIN_NAME,t);
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"smgt.internal_error", ex);
            }
            return new MonitorEvent(monitor,objName,description);

        } else {
            Monitor monitor = null;
            if( monitorType.equals(PROPERTY_MONITOR_COUNTER)){
                monitor = createCounterMonitor(table);
            } else if( monitorType.equals(PROPERTY_MONITOR_GAUGE)){
                monitor = createGaugeMonitor(table);
            } else if( monitorType.equals(PROPERTY_MONITOR_STRING)) {
                monitor = createStringMonitor(table);
            }
            if( monitor == null ){
                _logger.log(Level.WARNING,"smgt.internal_error");
                return null;
            }
            try {
                String sourceMbeanObjName = null;
                String sourceMbeanName = null;
                if (table.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ)) {
                    sourceMbeanObjName = table.get(PROPERTY_MONITOR_OBSERVED_OBJ);
                } else if (table.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME)) {
                    sourceMbeanName = table.get(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME);
                }
                if (sourceMbeanName ==  null && sourceMbeanObjName == null) {
                    throw new IllegalArgumentException(
                            sm.getString("selfmgmt_event.invalid_event_property","observedobject","monitor"));
                }
                String sourceMbean = null;
                if(sourceMbeanObjName != null) {
                    //final String serverNameVal  = System.getProperty("com.sun.aas.instanceName");
                    Pattern pat = Pattern.compile("\\$\\{instance.name\\}");
                    Matcher m = pat.matcher(sourceMbeanObjName);
                    if(m.find()) {
                        sourceMbean = m.replaceAll(instanceName);
                    } else {
                        sourceMbean = sourceMbeanObjName;
                    }
                } else if(sourceMbeanName != null) {
                    sourceMbean = ManagementRulesMBeanHelper.getObjName(sourceMbeanName);
                }
                /*if (!(sourceMbean.endsWith(",server=" + instanceName))) {
                    sourceMbean = sourceMbean + ",server=" + instanceName;
                }*/
                ObjectName objName = new ObjectName(sourceMbean);
                //ObjectName objName = new ObjectName(table.get(PROPERTY_MONITOR_OBSERVED_OBJ));
                monitor.addObservedObject(objName);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","observedobject","monitor"), ex);
            }
            monitor.setObservedAttribute(observedAttribute);
            String granularityPeriod =  table.get(PROPERTY_MONITOR_GRANULARITY_PERIOD);
            if (granularityPeriod != null) {
                try {
                    long gPeriod = Long.parseLong(granularityPeriod);
                    monitor.setGranularityPeriod(gPeriod);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_GRANULARITY_PERIOD,"monitor"), ex);
                }
            }
                                                                                                                                               
            ObjectName objName = null;
            try {
                table.put("version",getNewVersionString());
                Hashtable<String,String> t = (Hashtable<String,String>)table.clone();
                if(t.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME)) {
                    t.remove(PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME);
                } else if(t.containsKey(PROPERTY_MONITOR_OBSERVED_OBJ)) {
                    t.remove(PROPERTY_MONITOR_OBSERVED_OBJ);
                }
                //t.remove(PROPERTY_MONITOR_OBSERVED_OBJ);
                objName = new ObjectName(MonitorEvent.DOMAIN_NAME,t);
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"smgt.internal_error", ex);
            }
            return new MonitorEvent(monitor,objName,description);

        }
    }
   
    private Monitor createCounterMonitor(Hashtable<String,String> table) {
        CounterMonitor monitor = null;
        try {
            monitor = (CounterMonitor)getMBeanServer().instantiate("javax.management.monitor.CounterMonitor");
        } catch ( Exception rex) {
            _logger.log(Level.WARNING,"smgt.internal_error", rex);
        }
        String strDiffMode = table.get(PROPERTY_MONITOR_DIFFERENCEMODE);
        if (strDiffMode != null) {
            try {
                boolean diffMode = Boolean.parseBoolean(strDiffMode);
                monitor.setDifferenceMode(diffMode);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_DIFFERENCEMODE,"monitor"), ex);
            }
        }
        
        String numberType = table.get(PROPERTY_MONITOR_NUMBERTYPE);
        if (numberType == null)
            numberType = "long";
        String initTheshold = table.get(PROPERTY_MONITOR_INIT_THRESHOLD);
        String strOffset = table.get(PROPERTY_MONITOR_OFFSET);
        String strModulus = table.get(PROPERTY_MONITOR_MODULUS);
        Number threshold = null;
        Number offset = null;
        Number modulus = null;
        if (initTheshold == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_INIT_THRESHOLD,"monitor"));
        } else {
            try {
                if (numberType.equals("long")) {
                    threshold = Long.parseLong(initTheshold);
                    if (strOffset != null)
                        offset = Long.parseLong(strOffset);
                    else
                        offset = 0L;
                    if (strModulus != null)
                        modulus = Long.parseLong(strModulus);
                    else
                        modulus = 0L;
                } else if (numberType.equals("int")) {
                    threshold = Integer.parseInt(initTheshold);
                    if (strOffset != null)
                        offset = Integer.parseInt(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Integer.parseInt(strModulus);
                    else modulus = 0;
                } else if (numberType.equals("short")) {
                    threshold = Short.parseShort(initTheshold);
                    if (strOffset != null)
                        offset = Short.parseShort(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Short.parseShort(strModulus);
                    else modulus = 0;
                } else if (numberType.equals("byte")) {
                    threshold = Byte.parseByte(initTheshold);
                    if (strOffset != null)
                        offset = Byte.parseByte(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Byte.parseByte(strModulus);
                    else modulus = 0;
                }
                monitor.setInitThreshold(threshold);
                if (offset != null)
                    monitor.setOffset(offset);
                if (modulus != null)
                    monitor.setModulus(modulus);
            }catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_INIT_THRESHOLD,"monitor"),ex);
            }
        }
        if (!monitor.getNotify()) {
            monitor.setNotify(true);
        }
        return monitor;
    }
   
    private StatisticMonitor createCounterStatisticMonitor(Hashtable<String,String> table) {
        CounterStatisticMonitor monitor = null;
        try {
            monitor = (CounterStatisticMonitor)getMBeanServer().instantiate("com.sun.enterprise.admin.selfmanagement.event.CounterStatisticMonitor");
        } catch ( Exception rex) {
            _logger.log(Level.WARNING,"smgt.internal_error", rex);
        }
        String strDiffMode = table.get(PROPERTY_MONITOR_DIFFERENCEMODE);
        if (strDiffMode != null) {
            try {
                boolean diffMode = Boolean.parseBoolean(strDiffMode);
                monitor.setDifferenceMode(diffMode);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_DIFFERENCEMODE,"monitor"), ex);
            }
        }
        
        String numberType = table.get(PROPERTY_MONITOR_NUMBERTYPE);
        if (numberType == null)
            numberType = "long";
        String initTheshold = table.get(PROPERTY_MONITOR_INIT_THRESHOLD);
        String strOffset = table.get(PROPERTY_MONITOR_OFFSET);
        String strModulus = table.get(PROPERTY_MONITOR_MODULUS);
        Number threshold = null;
        Number offset = null;
        Number modulus = null;
        if (initTheshold == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_INIT_THRESHOLD,"monitor"));
        } else {
            try {
                if (numberType.equals("long")) {
                    threshold = Long.parseLong(initTheshold);
                    if (strOffset != null)
                        offset = Long.parseLong(strOffset);
                    else
                        offset = 0L;
                    if (strModulus != null)
                        modulus = Long.parseLong(strModulus);
                    else
                        modulus = 0L;
                } else if (numberType.equals("int")) {
                    threshold = Integer.parseInt(initTheshold);
                    if (strOffset != null)
                        offset = Integer.parseInt(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Integer.parseInt(strModulus);
                    else modulus = 0;
                } else if (numberType.equals("short")) {
                    threshold = Short.parseShort(initTheshold);
                    if (strOffset != null)
                        offset = Short.parseShort(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Short.parseShort(strModulus);
                    else modulus = 0;
                } else if (numberType.equals("byte")) {
                    threshold = Byte.parseByte(initTheshold);
                    if (strOffset != null)
                        offset = Byte.parseByte(strOffset);
                    else offset = 0;
                    if (strModulus != null)
                        modulus = Byte.parseByte(strModulus);
                    else modulus = 0;
                }
                monitor.setInitThreshold(threshold);
                if (offset != null)
                    monitor.setOffset(offset);
                if (modulus != null)
                    monitor.setModulus(modulus);
            }catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_INIT_THRESHOLD,"monitor"),ex);
            }
        }
        if (!monitor.getNotify()) {
            monitor.setNotify(true);
        }
        return monitor;
    }
 
    private Monitor createGaugeMonitor(Hashtable<String,String> table) {
        GaugeMonitor monitor = null;
        try {
            monitor = (GaugeMonitor)getMBeanServer().instantiate("javax.management.monitor.GaugeMonitor");
        } catch( Exception ex ) {
            _logger.log(Level.WARNING, "sgmt.internal_error", ex);
        }

        String strDiffMode = table.get(PROPERTY_MONITOR_DIFFERENCEMODE);
        if (strDiffMode != null) {
            try {
                boolean diffMode = Boolean.parseBoolean(strDiffMode);
                monitor.setDifferenceMode(diffMode);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_DIFFERENCEMODE,"monitor"), ex);
            }
        }
        String numberType = table.get(PROPERTY_MONITOR_NUMBERTYPE);
        if (numberType == null)
            numberType = "long";
        String lowTheshold = table.get(PROPERTY_MONITOR_LOW_THRESHOLD);
        String highTheshold = table.get(PROPERTY_MONITOR_HIGH_THRESHOLD);
        if ( (lowTheshold == null) || (highTheshold == null)) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","lowthreshold or highthreshold","monitor"));
        }
        Number lThreshold = null;
        Number hThreshold = null;
        try {
            if (numberType.equals("long")) {
                lThreshold = Long.parseLong(lowTheshold);
                hThreshold = Long.parseLong(highTheshold);
            } else if (numberType.equals("int")) {
                lThreshold = Integer.parseInt(lowTheshold);
                hThreshold = Integer.parseInt(highTheshold);
            } else if (numberType.equals("short")) {
                lThreshold = Short.parseShort(lowTheshold);
                hThreshold = Short.parseShort(highTheshold);
            } else if (numberType.equals("double")) {
                lThreshold = Double.parseDouble(lowTheshold);
                hThreshold = Double.parseDouble(highTheshold);
            } else if (numberType.equals("float")) {
                lThreshold = Float.parseFloat(lowTheshold);
                hThreshold = Float.parseFloat(highTheshold);
            } else if (numberType.equals("byte")) {
                lThreshold = Byte.parseByte(lowTheshold);
                hThreshold = Byte.parseByte(highTheshold);
            }
            monitor.setThresholds(hThreshold, lThreshold);
        }catch (Exception ex) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","lowthreshold or highthreshold","monitor"),ex);
        }
        if (!monitor.getNotifyHigh()) {
            monitor.setNotifyHigh(true);
        }
        if (!monitor.getNotifyLow()) {
            monitor.setNotifyLow(true);
        }
        return monitor;
    }

    private StatisticMonitor createGaugeStatisticMonitor(Hashtable<String,String> table) {
        GaugeStatisticMonitor monitor = null;
        try {
            monitor = (GaugeStatisticMonitor)getMBeanServer().instantiate("com.sun.enterprise.admin.selfmanagement.event.GaugeStatisticMonitor");
        } catch( Exception ex ) {
            _logger.log(Level.WARNING, "sgmt.internal_error", ex);
        }
                                                                                                                                               
        String strDiffMode = table.get(PROPERTY_MONITOR_DIFFERENCEMODE);
        if (strDiffMode != null) {
            try {
                boolean diffMode = Boolean.parseBoolean(strDiffMode);
                monitor.setDifferenceMode(diffMode);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_DIFFERENCEMODE,"monitor"), ex);
            }
        }
        String numberType = table.get(PROPERTY_MONITOR_NUMBERTYPE);
        if (numberType == null)
            numberType = "long";
        String lowTheshold = table.get(PROPERTY_MONITOR_LOW_THRESHOLD);
        String highTheshold = table.get(PROPERTY_MONITOR_HIGH_THRESHOLD);
        if ( (lowTheshold == null) || (highTheshold == null)) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","lowthreshold or highthreshold","monitor"));
        }
        Number lThreshold = null;
        Number hThreshold = null;
        try {
            if (numberType.equals("long")) {
                lThreshold = Long.parseLong(lowTheshold);
                hThreshold = Long.parseLong(highTheshold);
            } else if (numberType.equals("int")) {
                lThreshold = Integer.parseInt(lowTheshold);
                hThreshold = Integer.parseInt(highTheshold);
            } else if (numberType.equals("short")) {
                lThreshold = Short.parseShort(lowTheshold);
                hThreshold = Short.parseShort(highTheshold);
            } else if (numberType.equals("double")) {
                lThreshold = Double.parseDouble(lowTheshold);
                hThreshold = Double.parseDouble(highTheshold);
            } else if (numberType.equals("float")) {
                lThreshold = Float.parseFloat(lowTheshold);
                hThreshold = Float.parseFloat(highTheshold);
            } else if (numberType.equals("byte")) {
                lThreshold = Byte.parseByte(lowTheshold);
                hThreshold = Byte.parseByte(highTheshold);
            }
            monitor.setThresholds(hThreshold, lThreshold);
        }catch (Exception ex) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property","lowthreshold or highthreshold","monitor"),ex);
        }
        if (!monitor.getNotifyHigh()) {
            monitor.setNotifyHigh(true);
        }
        if (!monitor.getNotifyLow()) {
            monitor.setNotifyLow(true);
        }
        return monitor;
    }

    private Monitor createStringMonitor(Hashtable<String,String> table) {
        StringMonitor monitor = null;
        try {
            monitor = (StringMonitor)getMBeanServer().instantiate("javax.management.monitor.StringMonitor");
        } catch( Exception ex ) {
            _logger.log(Level.WARNING, "sgmt.internal_error", ex);
        }
        
        String strToCompare = table.get(PROPERTY_MONITOR_STRING_TO_COMPARE);
        String strNotify = table.get(PROPERTY_MONITOR_STRING_NOTIFY);
        if (strToCompare == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_STRING_TO_COMPARE,"monitor"));
        } 
        monitor.setStringToCompare(strToCompare);
        if(strNotify == null || strNotify.equals(PROPERTY_MONITOR_STRING_NOTIFY_MATCH)) { // match has higher priority than differ
            monitor.setNotifyMatch(true);
            return monitor;
        } else if(strNotify.equals(PROPERTY_MONITOR_STRING_NOTIFY_DIFFER))
            monitor.setNotifyDiffer(true);
        
        return monitor;
    }
    
    private StatisticMonitor createStringStatisticMonitor(Hashtable<String,String> table) {
        StringStatisticMonitor monitor = null;
        try {
            monitor = (StringStatisticMonitor)getMBeanServer().instantiate("com.sun.enterprise.admin.selfmanagement.event.StringStatisticMonitor");
        } catch( Exception ex ) {
            _logger.log(Level.WARNING, "sgmt.internal_error", ex);
        }
                                                                                                                                               
        String strToCompare = table.get(PROPERTY_MONITOR_STRING_TO_COMPARE);
        String strNotify = table.get(PROPERTY_MONITOR_STRING_NOTIFY);
        if (strToCompare == null) {
            throw new IllegalArgumentException(
                    sm.getString("selfmgmt_event.invalid_event_property",PROPERTY_MONITOR_STRING_TO_COMPARE,"monitor"));
        }
        monitor.setStringToCompare(strToCompare);
        if(strNotify == null || strNotify.equals(PROPERTY_MONITOR_STRING_NOTIFY_MATCH)) { // match has higher priority than differ
            monitor.setNotifyMatch(true);
            return monitor;
        } else if(strNotify.equals(PROPERTY_MONITOR_STRING_NOTIFY_DIFFER))
            monitor.setNotifyDiffer(true);
        return monitor;
    }

    static MonitorEventFactory getInstance() {
        return instance;
    }
    
    
    private synchronized String getNewVersionString() {
        seqno++;
        return  seqno.toString();
    }
    private static final MonitorEventFactory instance = new MonitorEventFactory();
    private Long seqno = 0L;
    private static final String instanceName = (ApplicationServer.getServerContext()).getInstanceName();    
}
