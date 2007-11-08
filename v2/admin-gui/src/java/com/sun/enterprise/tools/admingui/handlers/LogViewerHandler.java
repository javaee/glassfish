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
 * LogViewerHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Iterator;
import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.handlers.ConfigurationHandlers;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.LogServiceConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.ext.logging.LogFileAccess;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.j2ee.J2EEServer;

import com.sun.appserv.management.ext.logging.LogFileAccess;
import com.sun.appserv.management.ext.logging.LogQuery;
import com.sun.appserv.management.ext.logging.Logging;
import com.sun.appserv.management.ext.logging.LogQueryEntry;

import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.util.Util;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;

import javax.servlet.http.HttpServletRequest;

import javax.management.AttributeList;
import javax.management.Attribute;

import java.lang.reflect.Method;


public class LogViewerHandler {
    /** Creates a new instance of InstanceHandler */
    public LogViewerHandler() {
    }
    
    /**
     * <p> This handler returns all instance names of the domain </p>
     *
     * <p> Output value: "Instances" -- Type: <code>java.util.SelectItem</code>
     * @param  context The HandlerContext.
     */
    @Handler(id="getInstancesForDropdown",
        output={
            @HandlerOutput(name="Instances", type=SelectItem[].class)})
    public static void getInstancesForDropdown(HandlerContext handlerCtx) {
        try{
            Map<String,ServerConfig> serverMap =
                    (Map)AMXUtil.getDomainConfig().getServerConfigMap();
            String[] servers = serverMap == null ?  null : 		(String[])serverMap.keySet().toArray(new String[serverMap.size()]);
            SelectItem[] s = ConfigurationHandlers.getModOptions(servers);
            handlerCtx.setOutputValue("Instances", s);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     * <p> This handler returns the log file names for selected instances of the domain </p>
     *
     * <p> Output value: "LogFileNames" -- Type: <code>java.util.SelectItem</code>
     * @param  context The HandlerContext.
     */  
     
    @Handler(id="getLogFilesForDropdown",
        input={
        @HandlerInput(name="Instance", type=String.class, required=true)},    
        output={
            @HandlerOutput(name="LogFileNames", type=SelectItem[].class)})
        public static void getLogFilesForDropdown(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("Instance");
        try {
            String[] fileNames = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name).getMonitoringPeer().getServerRootMonitor().getLogging().getLogFileNames(LogFileAccess.SERVER_KEY);;
            SelectItem[] f = ConfigurationHandlers.getOptions(fileNames);
            handlerCtx.setOutputValue("LogFileNames", f);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     * <p> This handler returns the current log file name for selected instance of the domain </p>
     *
     * <p> Output value: "LogFileName" -- Type: <code>java.lang.String</code>
     * @param  context The HandlerContext.
     */  
     
    @Handler(id="getCurrentLogFile",
        input={
        @HandlerInput(name="Instance", type=String.class, required=true)},    
        output={
            @HandlerOutput(name="LogFileName", type=String.class)})
        public static void getCurrentLogFile(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("Instance");
        try {
            String[] fileNames = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name).getMonitoringPeer().getServerRootMonitor().getLogging().getLogFileNames(LogFileAccess.SERVER_KEY);;
            String logFileName = fileNames[0];
            handlerCtx.setOutputValue("LogFileName", logFileName);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
   /**
     * <p> This handler returns the first and last log record </p>
     *
     * <p> Output value: "LogFileNames" -- Type: <code>java.util.SelectItem</code>
     * @param  context The HandlerContext.
     */  
     
    @Handler(id="getFirstLastRecord",
        input={
        @HandlerInput(name="FirstRecord", type=String.class, required=true),
        @HandlerInput(name="LastRecord", type=String.class, required=true)},    
        output={
            @HandlerOutput(name="First", type=String.class),
            @HandlerOutput(name="Last", type=String.class)})
        public static void getFirstLastRecord(HandlerContext handlerCtx) {
	// Get the first/last row numbers
	String firstLogRow = (String)handlerCtx.getInputValue("FirstRecord");
	String lastLogRow = (String)handlerCtx.getInputValue("LastRecord");
        if (firstLogRow == null) {
            firstLogRow="0";
	}
        if (lastLogRow == null) {
            lastLogRow="0";
	}
	int firstRow = 0;
	try {
	    firstRow = Integer.parseInt(firstLogRow);
	    int lastRow = Integer.parseInt(lastLogRow);
	    if (firstRow > lastRow) {
		String temp = firstLogRow;
		firstLogRow = lastLogRow;
		lastLogRow = temp;
		firstRow = lastRow;
	    }
            handlerCtx.setOutputValue("First", firstLogRow);
            handlerCtx.setOutputValue("Last", lastLogRow);
	} catch (NumberFormatException ex) {
	    // ignore
	}        
    }    
    
  /**
     *	<p> This handler checks to see if instance is running
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="checkRunning",
        input={
            @HandlerInput(name="InstanceName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="isRunning", type=Boolean.class)})
    public static void checkRunning(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("InstanceName");
        try{
        if (isServerRunning(instanceName))
            handlerCtx.setOutputValue("isRunning", true);
        else
            handlerCtx.setOutputValue("isRunning", false);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }            
    }      
    
    /**
     *	<P>This method returns the current date (as a String).  The DATE_FORMAT
     *	must be specified, if it is not this method will fail.  You may set it
     *	to "short", "medium", "long", or "FULL".</P>
     *
     *	<P>If you do not set it to one of these values, you may set it to a
     *	valid format string.</P>
     */
    @Handler(id="getDate",
        input={
            @HandlerInput(name="DateFormat", type=String.class, required=true)},
        output={
            @HandlerOutput(name="Date", type=String.class)})    
    public void getDate(HandlerContext handlerCtx) {
    	// Get the required attribute
	String formatString = (String)handlerCtx.getInputValue("DateFormat");

	// Get the type
	int formatType = -1;
	if (formatString.equals(GET_DATE_SHORT)) {
	    formatType = DateFormat.SHORT;
	} else if (formatString.equals(GET_DATE_MEDIUM)) {
	    formatType = DateFormat.MEDIUM;
	} else if (formatString.equals(GET_DATE_LONG)) {
	    formatType = DateFormat.LONG;
	} else if (formatString.equals(GET_DATE_FULL)) {
	    formatType = DateFormat.FULL;
	}
	DateFormat df = null;
	if (formatType == -1) {
	    df = DateFormat.getDateInstance(
		DateFormat.SHORT, GuiUtil.getLocale());
	    ((SimpleDateFormat)df).applyLocalizedPattern(formatString);
	} else {
	    df = DateFormat.getDateInstance(
		formatType, GuiUtil.getLocale());
	}

	// Set the return value
	handlerCtx.setOutputValue("Date", df.format(new Date()));
    }    
    
    /**
     *	<P>This method puts the current time (as a String) in the desired
     *	attribute.  The result attribute must be specified via an attribute
     *	named "getTimeResultAttribute"</P>
     */
   @Handler(id="getTime",
        output={
            @HandlerOutput(name="Time", type=String.class)})        
    public void getTime(HandlerContext handlerCtx) {
        try{
	DateFormat df = DateFormat.getTimeInstance(
	    DateFormat.SHORT, GuiUtil.getLocale());
	((SimpleDateFormat)df).applyLocalizedPattern(TIME_FORMAT);

	// Set the return value
	handlerCtx.setOutputValue("Time", df.format(new Date()));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }           
    }
   
    /**
     *	<p> This handler returns the list of Clusters and config info for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLogResultsTable",
        input={
            @HandlerInput(name="InstanceName", type=String.class, required=true),
            @HandlerInput(name="LogFileName", type=String.class, required=true),
            @HandlerInput(name="LogLevel", type=String.class, required=true),
            @HandlerInput(name="FromRecord", type=Long.class),
            @HandlerInput(name="AfterRecord", type=Boolean.class),
            @HandlerInput(name="DateEnabled", type=String.class),
            @HandlerInput(name="FromDate", type=Object.class),
            @HandlerInput(name="FromTime", type=Object.class),
            @HandlerInput(name="ToDate", type=Object.class),
            @HandlerInput(name="ToTime", type=Object.class),
            @HandlerInput(name="Loggers", type=Object.class),
            @HandlerInput(name="CustomLoggers", type=Object.class),
            @HandlerInput(name="Nvp", type=Object.class),
            @HandlerInput(name="NumToDisplay", type=Integer.class),
            @HandlerInput(name="OnlyLevel", type=Boolean.class),
            @HandlerInput(name="LogDateSortDirection", type=Boolean.class),
            @HandlerInput(name="TruncateMessage", type=Boolean.class),
            @HandlerInput(name="TruncateLength", type=Integer.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="LogFileDir", type=String.class),
            @HandlerOutput(name="HasResults", type=Boolean.class),
            @HandlerOutput(name="FirstLogRow", type=Integer.class),
            @HandlerOutput(name="LastLogRow", type=Integer.class)}
     )
     public static void getLogResultsTable(HandlerContext handlerCtx){
	// Attempt to read values passed in
        String archivedLogFile = (String)handlerCtx.getInputValue("LogFileName");
	Long fromRecord = (Long)handlerCtx.getInputValue("FromRecord");
	Boolean after = (Boolean)handlerCtx.getInputValue("AfterRecord");
	//Boolean dateEnabled = (Boolean)handlerCtx.getInputValue("DateEnabled");
        String dateEnabledString = (String)handlerCtx.getInputValue("DateEnabled");
	Object fromDate = handlerCtx.getInputValue("FromDate");
	Object fromTime = handlerCtx.getInputValue("FromTime");
	Object toDate = handlerCtx.getInputValue("ToDate");
	Object toTime = handlerCtx.getInputValue("ToTime");
	Object loggers = handlerCtx.getInputValue("Loggers");
	Object logLevel = handlerCtx.getInputValue("LogLevel");
	Object customLoggers = handlerCtx.getInputValue("CustomLoggers");
	Object nvp = handlerCtx.getInputValue("Nvp");
	Integer numberToDisplay = (Integer)handlerCtx.getInputValue("NumToDisplay");
	Boolean onlyLevel =
	    (Boolean)handlerCtx.getInputValue("OnlyLevel");
	Boolean direction =
	    (Boolean)handlerCtx.getInputValue("LogDateSortDirection");
	Boolean truncMsg =
	    (Boolean)handlerCtx.getInputValue("TruncateMessage");
	Integer truncLenInteger =
	    (Integer)handlerCtx.getInputValue("TruncateLength");        
        List result = new ArrayList(); 
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        String logFileDir = "";
        Boolean hasResults = new Boolean(false);
        if(instanceName != null && archivedLogFile != null) {
         // Determine if messages should be truncated
	boolean truncateMessage = true;
	if (truncMsg != null) {
	    truncateMessage = truncMsg.booleanValue();
	}
	int truncLen = 100;
	if (truncLenInteger != null) {
	    truncLen = truncLenInteger.intValue();
	}

 
        Boolean dateEnabled = null;
	// Convert Date/Time fields
        if ("enabled".equals(dateEnabledString)){
                dateEnabled = TRUE;     
        }
	boolean dateEnabledFlag = false;
	if (dateEnabled != null) {
	    dateEnabledFlag = dateEnabled.booleanValue();
	}
	if (dateEnabledFlag) {
	    // Date is enabled, figure out what the values are
	    fromDate = convertDateTime(handlerCtx, fromDate, fromTime);
	    toDate = convertDateTime(handlerCtx, toDate, toTime);
	    if ((fromDate == null)) {
		GuiUtil.handleError(handlerCtx, "Specific Date Range was chosen, however, date fields are incomplete.");
	    }
            if (toDate != null && fromDate != null)
                if(((Date)fromDate).after((Date)toDate)) {
		GuiUtil.handleError(handlerCtx, "Timestamp value of 'From: ' field " + fromDate +
		    " must not be greater than 'To: ' field value " + toDate);
 	    }
	} else {
	    // Date not enabled, ignore from/to dates
	    fromDate = null;
	    toDate = null;
	}

	if (logLevel != null) {
	    if (logLevel.toString().trim().length() == 0) {
		logLevel = null;
	    }
	}

	if (onlyLevel == null) {
	    onlyLevel = FALSE;
	}

	// Convert module array to List
	List moduleList = null;
	if (loggers != null) {
	    int len = ((Object[])loggers).length;
	    moduleList = new ArrayList(len);
	    Object val;
	    for (int count=0; count<len; count++) {
		val = (((Object[])loggers)[count]);
		if ((val == null) || (val.toString().trim().length() == 0)) {
		    continue;
		}
		moduleList.add(val);
	    }
	}

	// Add custom loggers
	if ((customLoggers != null) &&
		(customLoggers.toString().trim().length() != 0)) {
	    StringTokenizer tok = new StringTokenizer(
		customLoggers.toString(),
		CUSTOM_LOGGER_DELIMITERS);
	    String token;
            if(moduleList == null) {
                moduleList = new ArrayList();
            } 
	                
	    while (tok.hasMoreTokens()) {
		token = tok.nextToken();
                
		if ((token == null) || (token.length()==0)) {
		    continue;
		}
		moduleList.add(token);
	    }
	}


	// Deal w/ NVPs
	Hashtable nvpProps = null;
	if ((nvp != null) && (nvp.toString().trim().length() != 0)) {
	    nvpProps = new Properties();
	    int equalsIdx;
	    String token;
	    // Iterate over the entries
	    StringTokenizer tok =
		new StringTokenizer(nvp.toString(), NVP_DELIMITERS);
	    while (tok.hasMoreTokens()) {
		token = tok.nextToken();
		if ((token == null) || (token.length()==0)) {
		    continue;
		}
                
                try {
		equalsIdx = token.indexOf(EQUALS);
		// Make sure = exists and it is not the first character
		if (equalsIdx < 0) {
		    GuiUtil.handleError(handlerCtx, "Name-Value Pairs must be in the format \""+
			"<name>=<value>\".");
		}

                String key = null;
                    key = token.substring(0, equalsIdx++);
                    
                    ArrayList valueList = (ArrayList) nvpProps.get(key);
                    if (valueList == null){
                        valueList = new ArrayList();
                        valueList.add(token.substring(equalsIdx));
                        nvpProps.put(key, valueList);
                    }else{
                        valueList.add(token.substring(equalsIdx));
                    }
                }catch(Exception ex){
                    GuiUtil.handleError(handlerCtx, "Name-Value Pairs must be in the format \""+
                            "<name>=<value>\".");
                }
	    }
	}


	// Get the number to Display
	if (numberToDisplay == null) {
	    numberToDisplay = DEFAULT_NUMBER_TO_DISPLAY;
	}

	// Get the direction
	if (direction == null) {
	    direction = FALSE;
	}

	// Get AfterRecord flag
	if (after == null) {
	    // Not supplied, use direction
	    after = direction;
	}

	// Build the params object[]
	Object params[] = new Object[QUERY_SIGNATURE.length];
        params[0] = archivedLogFile;    //log file name. if null, use current.
	params[1] = fromRecord;		// fromRecord
	params[2] =			// next (after or before fromRecord)
	    (fromRecord == null) ? direction : after;
	params[3] = direction;		// forward
	params[4] = numberToDisplay;	// requestedCount
	params[5] = fromDate;		// fromDate
	params[6] = toDate;		// toDate
	params[7] = logLevel;		// logLevel
	params[8] = onlyLevel;		// onlyLevel
	params[9] = moduleList;		// listOfModules
	params[10] = nvpProps;		// nameValueMap


	// Search for the log entries
	AttributeList results = null;
	try {
            results = (AttributeList)getLogRecordsUsingQuery(handlerCtx, "com.sun.appserv:name=logmanager,category=runtime,server="+instanceName, instanceName, params, QUERY_SIGNATURE);
 	}catch (Exception ex) {
	    GuiUtil.handleError(handlerCtx, "Error while querying Log File.");
	}

	// Add the results to the Model

	String message;
	List headerRow = (List)(((Attribute)results.get(0)).getValue());
	List rowList = (List)(((Attribute)results.get(1)).getValue());
	List row;
	Iterator it = rowList.iterator();
	while (it.hasNext()) {
	    row = (List)it.next();            
            HashMap oneRow = new HashMap();
	    if (row.size() != headerRow.size()) {
		GuiUtil.handleError(handlerCtx, "Row had '"+row.size()+"' columns, header has '"+
		    headerRow.size()+"' columns!");
	    }
            oneRow.put("recNumber", row.get(0));
            oneRow.put("dateTime", formatDateForDisplay(
		GuiUtil.getLocale(), (Date)row.get(1)));

            String msgId = (String) row.get(6);
 	    String level = (String)row.get(2);
            //only SEVERE msg provoides diagnostic info.
	    if (level.equalsIgnoreCase("severe")) {
		// NOTE: Image name/location is hard-coded
		oneRow.put("levelImage", GuiUtil.getMessage("common.errorGif"));
		oneRow.put(SHOW_LEVEL_IMAGE, new Boolean(true));
                oneRow.put("diagnosticCauses", getDiagnosticCauses(handlerCtx, msgId));
                oneRow.put("diagnosticChecks", getDiagnosticChecks(handlerCtx, msgId));
                oneRow.put("diagnosticURI", getDiagnosticURI(handlerCtx, msgId));
	    } else {
		oneRow.put(SHOW_LEVEL_IMAGE, new Boolean(false));
                oneRow.put("diagnostic", "");
	    }
	    oneRow.put("level", level);
	    oneRow.put("productName", row.get(3));
	    oneRow.put("logger", row.get(4));
	    oneRow.put("nvp", row.get(5));
	    oneRow.put("messageID", msgId );
	    message = ((String)row.get(7)).trim();
	    if (truncateMessage && (message.length() > truncLen)) {
		message = message.substring(0, truncLen).concat("...\n");
	    }
	    message = Util.htmlEscape(message);
            oneRow.put("message", message);
	    //oneRow.put("message", formatMessageForDisplay(message));  
            result.add(oneRow);
        }
        logFileDir = getLogFilesDirectory(instanceName);
        	
        // Set the first / last record numbers as attributes
	if (rowList.size() > 0) {
	    handlerCtx.setOutputValue("FirstLogRow",
		((List)rowList.get(0)).get(0));
	    handlerCtx.setOutputValue("LastLogRow",
		((List)rowList.get(rowList.size()-1)).get(0));
            hasResults = new Boolean(true);
	} else {
	    handlerCtx.setOutputValue("FirstLogRow", "-1");
	    handlerCtx.setOutputValue("LastLogRow", "-1");
            
	}
            
	}  
            //Logging logging = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(instance).getMonitoringPeer().getServerRootMonitor().getLogging();
            //LogQueryEntry[] query = logging.queryServerLog(name, 1, true, LogQuery.FIRST_RECORD, null, null, level, null, null).getEntries();

        handlerCtx.setOutputValue("LogFileDir", logFileDir);
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("HasResults", hasResults);
    }    
     private static AttributeList getLogRecordsUsingQuery(HandlerContext handlerCtx, String logManagerObjectName, String instanceName, Object[]params, String[] QUERY_SIGNATURE)
     {
         AttributeList results = null;
         if (JMXUtil.isValidMBean(logManagerObjectName)){
	     results = (AttributeList)JMXUtil.invoke(
		logManagerObjectName,
		"getLogRecordsUsingQuery",
		params,
		QUERY_SIGNATURE);
            return results;
         }
         try {
             //go through node agent proxy
             Class provider = Class.forName("com.sun.enterprise.ee.tools.admingui.handlers.NodeAgentLogProvider");
             Class[] type = new Class[]{
                            String.class,
                            Object[].class};
            Method method = provider.getMethod("getLogRecordsUsingQuery", type);
            Object[] args = new Object[] { instanceName, params};
            results = (AttributeList) method.invoke(null, args);
         }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
         return results;
     }    
     
    /**
     *	This method simply takes the given SimpleDateFormat and parses the
     *	given String after applying the given format String.
     */
    private  static Date parseDateString(SimpleDateFormat fmt, String format, String dateTime) throws ParseException {
	fmt.applyLocalizedPattern(format);
	return fmt.parse(dateTime);
    }
    
    /**
     *	This method converts a date/time string to a Date.
     *
     *	@param	request	The ServletRequest
     *	@param	date	The date as a String (or the date/time as a Date)
     *	@param	time	The time as a String (or null)
     *	@param	vd	The ViewDescriptor (for exception handling)
     *	@param	view	The View (for exception handling)
     */
    protected static Date convertDateTime(HandlerContext handlerCtx, Object date, Object time) {
    	// If Date is already a Date, then do nothing
	if (date instanceof Date) {
	    return (Date)date;
	}
	// If Date is null or empty, return null
	if ((date == null) || (date.toString().trim().length() == 0)) {
	    return null;
	}

	// Get the date / time string
        //if(time != null && time.toString().trim().length() == 0){
	//    time = null;
	//}
	String dateTime = date.toString()+
	    ((time == null) ? "" : (" "+time.toString()));
	DateFormat df = DateFormat.getDateInstance(
	    DateFormat.SHORT, GuiUtil.getLocale());
	if ((time != null) && (df instanceof SimpleDateFormat)) {
	    SimpleDateFormat fmt = (SimpleDateFormat)df;
	    String formatPrefix = fmt.toLocalizedPattern();
	    try {
		// Try w/ HH:mm:ss.SSS
		date = parseDateString(
		    fmt, formatPrefix+TIME_FORMAT, dateTime);
	    } catch (ParseException ex) {
		try {
		    // Try w/ HH:mm:ss
		    date = parseDateString(
			fmt, formatPrefix+TIME_FORMAT_2, dateTime);
		} catch (ParseException ex2) {
		    try {
			// Try w/ HH:mm
			date = parseDateString(
			    fmt, formatPrefix+TIME_FORMAT_3, dateTime);
		    } catch (ParseException ex3) {
			GuiUtil.handleError(handlerCtx, "Unable to parse Date/Time: '"+dateTime+"'.");
		    }
		}
	    }
	} else if (time != null) {
	    // I don't think this ever happens
	    df = DateFormat.getDateTimeInstance(
		DateFormat.SHORT, DateFormat.LONG, GuiUtil.getLocale());
	    try {
		date = df.parse(dateTime);
	    } catch (ParseException ex) {
		GuiUtil.handleError(handlerCtx, "Unable to parse Date/Time: '"+dateTime+"'.");
	    }
	} else {
	    try {
		date = df.parse(dateTime);
	    } catch (ParseException ex) {
		GuiUtil.handleError(handlerCtx, "Unable to parse Date/Time: '"+dateTime+"'.");
	    }
	}

	// Return the result  
        Date convertDate = null;
        try {
            convertDate = (Date)date;
        } catch (Exception ex) {
            convertDate = null;
	    
        }
        return convertDate;
    }     
    
    /**
     *	This method formats the log message to be displayed appropriately for
     *	HTML (it converts '\n' characters to &lt;br&gt;'s.
    
    protected static String formatMessageForDisplay(String message) {
	return HtmlUtil.escape(message).replaceAll("\n", "<br>");
    } */
    
    /**
     *	This method formats a log file date to a more readable date (based on
     *	locale).
     */
    public static String formatDateForDisplay(Locale locale, Date date) {
	DateFormat dateFormat = DateFormat.getDateInstance(
	    DateFormat.MEDIUM, locale);
	if (dateFormat instanceof SimpleDateFormat) {
	    SimpleDateFormat fmt = (SimpleDateFormat)dateFormat;
	    fmt.applyLocalizedPattern(fmt.toLocalizedPattern()+TIME_FORMAT);
	    return fmt.format(date);
	} else {
	    dateFormat = DateFormat.getDateTimeInstance(
		DateFormat.MEDIUM, DateFormat.LONG, locale);
	    return dateFormat.format(date);
	}
    }    
    
     public static boolean isServerRunning(String instanceName){
        ServerRootMonitor serverRootMonitor = AMXUtil.getServerRootMonitor(instanceName);
        if (serverRootMonitor == null)
            return false;
        return true;
    }
     
    /**
     * * This method get the diagnostic based on the message id
     */
    private static String getDiagnosticCauses(HandlerContext handlerCtx, String msgId){

        if (msgId == null || "".equals(msgId))
            return formatArrayListForDisplay(null);
        String params[] = {msgId};
        String signatures[] = {"String"};
	    ArrayList results = (ArrayList)JMXUtil.invoke(
		"com.sun.appserv:name=logmanager,category=runtime,server=server",
		"getDiagnosticCausesForMessageId",
		params,
		signatures);
             String res =  formatArrayListForDisplay(results);
             return res;
    }

   /**
     * * This method get the diagnostic Checks based on the message id
     */
    private static String getDiagnosticChecks(HandlerContext handlerCtx, String msgId){

        if (msgId == null || "".equals(msgId))
            return formatArrayListForDisplay(null);
        String params[] = {msgId};
        String signatures[] = {"String"};
	    ArrayList results = (ArrayList)JMXUtil.invoke(
		"com.sun.appserv:name=logmanager,category=runtime,server=server",
		"getDiagnosticChecksForMessageId",
		params,
		signatures);
             String res =  formatArrayListForDisplay(results);
             return res;

    }     
    
    /**
     * * This method get the diagnostic Checks based on the message id
     */
    private static String getDiagnosticURI(HandlerContext handlerCtx, String msgId){

        if (msgId == null || "".equals(msgId))
            return "";
        String params[] = {msgId};
        String signatures[] = {"String"};
	    String res = (String)JMXUtil.invoke(
		"com.sun.appserv:name=logmanager,category=runtime,server=server",
		"getDiagnosticURIForMessageId",
		params,
		signatures);
            return res;
            //return "http://www.google.com";

    }    
     
    /**
     *  This method formats the diagnostic to be displayed for HTML
     *  Add '<br>' to each elements of the ArrayList and returns the String.
     */
    protected static String formatArrayListForDisplay(ArrayList diag) {
        if ((diag == null) || (diag.size() == 0)) {
	    return "";
	}
        StringBuffer buf = new StringBuffer("<br>");
        for(int i=0; i<diag.size(); i++){
            buf.append( (String)diag.get(i));
	    buf.append("<br>");
        }
        return buf.toString();
    }
    
     public static String getLogFilesDirectory(String instanceName){
        if (isEmpty(instanceName))
            return "";
        String dir = "";
        try{
            if (JMXUtil.isValidMBean("com.sun.appserv:name=logmanager,category=runtime,server="+instanceName)){
                dir = (String) JMXUtil.invoke(
                "com.sun.appserv:name=logmanager,category=runtime,server="+instanceName,
                "getLogFilesDirectory",
                null,
                null);
            }else{
                //go through node agent proxy
                Class provider = Class.forName("com.sun.enterprise.ee.tools.admingui.handlers.NodeAgentLogProvider");
                Class[] type = new Class[]{ String.class };
                Method method = provider.getMethod("getLogFilesDirectory", type);
                Object[] args = new Object[] {instanceName};
                dir = (String) method.invoke(null, args);
            }
        }catch(Exception ex){
        }
        return dir;
     }    
     
    /**
     *	<P>This method returns the formatted date (as a String). </P>
     *
     */
    @Handler(id="getFormatedDateTime",
        input={
            @HandlerInput(name="Timestamp", type=String.class, required=true),
            @HandlerInput(name="AddHour", type=Boolean.class)},
        output={
            @HandlerOutput(name="Time", type=String.class),
            @HandlerOutput(name="Date", type=String.class)})         
      public void getFormatedDateTime(HandlerContext handlerCtx) {
          String ts = (String )handlerCtx.getInputValue("Timestamp");
          Boolean addHour =
	    (Boolean)handlerCtx.getInputValue("AddHour");
          Date date = null;
          if (ts == null || "".equals(ts)){
            date = new Date(System.currentTimeMillis());
          } else {
              if(addHour != null) {
                date = new Date( Long.parseLong(ts)+ ONE_HOUR);   
              } else {
              date = new Date( Long.parseLong(ts));
              }
          }
          DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, GuiUtil.getLocale());  
          DateFormat tf = DateFormat.getTimeInstance(DateFormat.MEDIUM, GuiUtil.getLocale());
          ((SimpleDateFormat)tf).applyLocalizedPattern(" HH:mm:ss.SSS");
          
          String ftime = tf.format(date);
          String fdate = df.format(date);
          handlerCtx.setOutputValue("Time", ftime);
          handlerCtx.setOutputValue("Date", fdate);
       }      
     
       /**
     *	<p> This handler returns the values for loggers
     *      in LogViewer Page.</p>
     *  <p> Output value: "LoggerList" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLoggers",
        input={
            @HandlerInput(name="selectedLoggers", type=String[].class)},
    output={
        @HandlerOutput(name="LoggerList", type=SelectItem[].class)})

        public static void getLoggers(HandlerContext handlerCtx) {
			String[] selectedLoggers = (String[])handlerCtx.getInputValue("selectedLoggers");
		String loggers[] = getLoggers(selectedLoggers);
        SelectItem[] options = ConfigurationHandlers.getOptions(loggers);
        handlerCtx.setOutputValue("LoggerList", options);
    }     

	private static String[] getLoggers(String[] selectedLoggers) {
		final String[] loggers = {"javax.enterprise",
			"javax.enterprise.resource",
        	"javax.enterprise.resource.corba",
        	"javax.enterprise.resource.javamail",
        	"javax.enterprise.resource.jdo",
        	"javax.enterprise.resource.jms",
        	"javax.enterprise.resource.jta",
        	"javax.enterprise.resource.resourceadapter",
        	"javax.enterprise.resource.webservices",
        	"javax.enterprise.resource.webservices.registry",
        	"javax.enterprise.resource.webservices.rpc",
        	"javax.enterprise.resource.webservices.saaj",
        	"javax.enterprise.system",
        	"javax.enterprise.system.container",
        	"javax.enterprise.system.container.cmp",
        	"javax.enterprise.system.container.ejb",
        	"javax.enterprise.system.container.mdb",
        	"javax.enterprise.system.container.web",
        	"javax.enterprise.system.core",
        	"javax.enterprise.system.core.classloading",
        	"javax.enterprise.system.core.config",
        	"javax.enterprise.system.core.naming",
        	"javax.enterprise.system.core.security",
        	"javax.enterprise.system.core.transaction",
        	"javax.enterprise.system.stream.out",
        	"javax.enterprise.system.stream.err",
        	"javax.enterprise.system.tools.admin",
        	"javax.enterprise.system.tools.deployment",
        	"javax.enterprise.system.tools.verifier"};

			HashSet<String> set = new LinkedHashSet<String>();
			if(selectedLoggers != null) {
				set.addAll(Arrays.asList(selectedLoggers));
			}
			set.addAll(Arrays.asList(loggers));
			return set.toArray(new String[0]);
	}
    
    private static boolean isEmpty(String test) {
        return ((test == null) || "".equals(test));
    }
    /**
     *	This defines the short date format, used by DATE_FORMAT. ("short")
     */
    public static final String GET_DATE_SHORT = "short";

    /**
     *	This defines the medium date format, used by DATE_FORMAT. ("medium")
     */
    public static final String GET_DATE_MEDIUM = "medium";

    /**
     *	This defines the long date format, used by DATE_FORMAT. ("long")
     */
    public static final String GET_DATE_LONG = "long";

    /**
     *	This defines the full date format, used by DATE_FORMAT. ("full")
     */
    public static final String GET_DATE_FULL = "full";
     
    /**
     *	This specifies how TIME fields are input and displayed.  We need to do
     *	this in order to get a display/input that works with milliseconds.
     *	Perhaps in the future we may want to just append the milliseconds?
     */
    public static final String TIME_FORMAT = " HH:mm:ss.SSS";
    public static final String TIME_FORMAT_2 = " HH:mm:ss";
    public static final String TIME_FORMAT_3 = " HH:mm";

    /**
     *	This is the log query method signature.
     */
    public static final String[] QUERY_SIGNATURE = {
            "java.lang.String",         //Added the extra parameter to bring up the page. Need to revisit.
	    "java.lang.Long",		// fromRecord
	    "java.lang.Boolean",	// next?
	    "java.lang.Boolean",	// forward?
	    "java.lang.Integer",	// requestedCount
	    "java.util.Date",		// fromDate
	    "java.util.Date",		// toDate
	    "java.lang.String",		// logLevel
	    "java.lang.Boolean",	// onlyLevel?
	    "java.util.List",		// listOfModules
	    "java.util.Properties"	// nameValueMap
	};
    /**
     *
     */
    public static final Integer ONE = new Integer(1);

    /**
     *	FALSE
     */
    public static final Boolean FALSE = new Boolean(false);

    /**
     *	TRUE
     */
    public static final Boolean TRUE = new Boolean(true);    
    
    /**
     *	If the number to display is not specified, this value will be used
     *	(40).
     */
    public static final Integer DEFAULT_NUMBER_TO_DISPLAY = new Integer(40);
    
    /**
     *
     */
    public static final String FIRST_LOG_ROW = "firstLogRow";

    /**
     *
     */
    public static final String LAST_LOG_ROW = "lastLogRow";
    
   /**
     *	The following constant defines the valid delimiters that can be used
     *	to seperate custom loggers on input. (" \t\n\r\f,;:")
     */
    public static final String CUSTOM_LOGGER_DELIMITERS = " \t\n\r\f,;:";
    /**
     *	The following constant defines the valid delimiters that can be used
     *	to seperate nvp entries on input. (" \t\n\r\f,;:")
     */
    public static final String NVP_DELIMITERS = " \t\n\r\f,;:";    
    /**
     *	This is the delimiter between the property name and property value.
     */
    public static final char EQUALS = '=';    
    /**
     *	This model key is set by the filter method, it is true if a
     *	level image should be displayed.
     */
    public static final String SHOW_LEVEL_IMAGE = "showLevelImage";

    /**
     *	This is the root directory of the alert images
     */
    public static final String LEVEL_IMAGE_ROOT =
	"/com_sun_web_ui/images/alerts/";    
    
    public static final long ONE_HOUR = (1000 /* ms */) * (60 /* sec */) * (60 /* min */);     
}
        
 
