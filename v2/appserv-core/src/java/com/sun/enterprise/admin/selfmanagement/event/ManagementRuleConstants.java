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


package com.sun.enterprise.admin.selfmanagement.event;


public class ManagementRuleConstants {
    
    //Event type
    static public final String EVENT_LIFECYCLE = "lifecycle";
    static public final String EVENT_CLUSTER = "cluster";
    static public final String EVENT_LOG = "log";
    static public final String EVENT_TIMER = "timer";
    static public final String EVENT_TRACE  = "trace";
    static public final String EVENT_MONITOR = "monitor";
    static public final String EVENT_NOTIFICATION = "notification";
    
    //property name for Life Cycle Event:
    static public final String PROPERTY_LIFECYCLE_NAME="name";
    
    //property name for Monitor Event:
    static public final String PROPERTY_MONITOR_COUNTER="countermonitor";
    static public final String PROPERTY_MONITOR_GAUGE="gaugemonitor";
    static public final String PROPERTY_MONITOR_STRING="stringmonitor";
    static public final String PROPERTY_MONITOR_TYPE="monitortype";
    static public final String PROPERTY_MONITOR_OBSERVED_OBJ="observedobject";
    static public final String PROPERTY_MONITOR_OBSERVED_ATTRIBUTE="observedattribute";
    static public final String PROPERTY_MONITOR_GRANULARITY_PERIOD="granularityperiod";
    static public final String PROPERTY_MONITOR_NUMBERTYPE="numbertype";
    static public final String PROPERTY_MONITOR_DIFFERENCEMODE="differencemode";
    static public final String PROPERTY_MONITOR_INIT_THRESHOLD="initthreshold";
    static public final String PROPERTY_MONITOR_OFFSET="offset";
    static public final String PROPERTY_MONITOR_MODULUS="modulus";
    static public final String PROPERTY_MONITOR_LOW_THRESHOLD="lowthreshold";
    static public final String PROPERTY_MONITOR_HIGH_THRESHOLD="highthreshold";
    static public final String PROPERTY_MONITOR_STRING_TO_COMPARE="stringtocompare";
    static public final String PROPERTY_MONITOR_STRING_NOTIFY_MATCH="notifymatch";
    static public final String PROPERTY_MONITOR_STRING_NOTIFY_DIFFER="notifydiffer";
    static public final String PROPERTY_MONITOR_STRING_NOTIFY="stringnotify";
    static public final String PROPERTY_MONITOR_OBSERVED_OBJ_MBEAN_NAME="observedmbean";
    
    //property name for Trace event:
    static public final String PROPERTY_TRACE_NAME="name";
    static public final String PROPERTY_TRACE_IPADDRESS="ipaddress";
    static public final String PROPERTY_TRACE_CALLERPRINCIPAL="callerprincipal";
    static public final String PROPERTY_TRACE_COMPONENTNAME="componentname";
    
    //property name for Log event:
    static public final String PROPERTY_LOG_LOGGERNAME="loggernames";
    static public final String PROPERTY_LOG_LEVEL="level";
    
    //property name for Timer event:
    static public final String PROPERTY_TIMER_PATTERN="pattern";
    static public final String PROPERTY_TIMER_DATESTRING="datestring";
    static public final String PROPERTY_TIMER_PERIOD="period";
    static public final String PROPERTY_TIMER_NUMBER_OF_OCCURRENCES="numberofoccurrences";
    static public final String PROPERTY_TIMER_MESSAGE="message";
    
    //property name for Notification event:
    static public final String PROPERTY_NOTIFICATION_SOURCEMBEAN="sourcembean";
    static public final String PROPERTY_NOTIFICATION_SOURCE_OBJ_NAME="sourceobjectname";
        
    
    //property name for Cluster event:
    static public final String PROPERTY_CLUSTER_NAME="name";
    static public final String PROPERTY_CLUSTER_SERVERNAME="servername";
     
}
