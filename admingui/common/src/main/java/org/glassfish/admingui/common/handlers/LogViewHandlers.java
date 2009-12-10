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
 * CommonHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.sun.jsftemplating.util.Util;

import javax.management.Attribute;

import java.util.Locale;
import java.util.Map;
import java.io.Serializable;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;

import org.glassfish.admin.amx.logging.LogQueryResult;
import org.glassfish.admin.amx.logging.LogQuery;
import org.glassfish.admin.amx.logging.LogQueryEntry;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.logging.LogFileAccess;

public class LogViewHandlers {
    
    /** Creates a new instance of LogViewHandlers */
    public LogViewHandlers() {
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
            @HandlerInput(name="FromRecord", type=Integer.class),
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
            @HandlerOutput(name="HasResults", type=Boolean.class),
            @HandlerOutput(name="FirstLogRow", type=Integer.class),
            @HandlerOutput(name="LastLogRow", type=Integer.class)}
     )
    public static void getLogResultsTable(HandlerContext handlerCtx) {
        // Attempt to read values passed in
        String archivedLogFile = (String) handlerCtx.getInputValue("LogFileName");
        Integer fromRecord = (Integer) handlerCtx.getInputValue("FromRecord");
        Boolean after = (Boolean) handlerCtx.getInputValue("AfterRecord");
        //Boolean dateEnabled = (Boolean)handlerCtx.getInputValue("DateEnabled");
        String dateEnabledString = (String) handlerCtx.getInputValue("DateEnabled");
        Object fromDate = handlerCtx.getInputValue("FromDate");
        Object fromTime = handlerCtx.getInputValue("FromTime");
        Object toDate = handlerCtx.getInputValue("ToDate");
        Object toTime = handlerCtx.getInputValue("ToTime");
        Object loggers = handlerCtx.getInputValue("Loggers");
        String logLevel = (String) handlerCtx.getInputValue("LogLevel");
        Object customLoggers = handlerCtx.getInputValue("CustomLoggers");
        Object nvp = handlerCtx.getInputValue("Nvp");
        Integer numberToDisplay = (Integer) handlerCtx.getInputValue("NumToDisplay");
        Boolean onlyLevel =
                (Boolean) handlerCtx.getInputValue("OnlyLevel");
        Boolean direction =
                (Boolean) handlerCtx.getInputValue("LogDateSortDirection");
        Boolean truncMsg =
                (Boolean) handlerCtx.getInputValue("TruncateMessage");
       
        Integer truncLenInteger =
                (Integer) handlerCtx.getInputValue("TruncateLength");
        List result = new ArrayList();
        String instanceName = (String) handlerCtx.getInputValue("InstanceName");
        String logFileDir = "";
        Boolean hasResults = new Boolean(false);
        if (instanceName != null && archivedLogFile != null) {
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
            if ("enabled".equals(dateEnabledString)) {
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
                if (toDate != null && fromDate != null) {
                    if (((Date) fromDate).after((Date) toDate)) {
                        GuiUtil.handleError(handlerCtx, "Timestamp value of 'From: ' field " + fromDate +
                                " must not be greater than 'To: ' field value " + toDate);
                    }
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
            //List moduleList = null;
            //Set moduleList = new HashSet();
            Set moduleList = null;
            if (loggers != null) {
                int len = ((Object[]) loggers).length;
                moduleList = new HashSet();
                Object val;
                for (int count = 0; count < len; count++) {
                    val = (((Object[]) loggers)[count]);
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
                    moduleList = new HashSet();
                } 

                while (tok.hasMoreTokens()) {
                    token = tok.nextToken();

                    if ((token == null) || (token.length() == 0)) {
                        continue;
                    }
                    moduleList.add(token);
                }
            }


            // Deal w/ NVPs
            //Hashtable nvpProps = null;
            List nvpProps = null;
            if ((nvp != null) && (nvp.toString().trim().length() != 0)) {
                nvpProps = new ArrayList();
                int equalsIdx;
                String token;
                // Iterate over the entries
                StringTokenizer tok =
                        new StringTokenizer(nvp.toString(), NVP_DELIMITERS);
                while (tok.hasMoreTokens()) {
                    token = tok.nextToken();
                    if ((token == null) || (token.length() == 0)) {
                        continue;
                    }

                    try {
                        equalsIdx = token.indexOf(EQUALS);
                        // Make sure = exists and it is not the first character
                        if (equalsIdx < 0) {
                            GuiUtil.handleError(handlerCtx, "Name-Value Pairs must be in the format \"" +
                                    "<name>=<value>\".");
                        }

                        String key = null;
                        key = token.substring(0, equalsIdx++);
                        Attribute attr = new Attribute(key, token.substring(equalsIdx));
                        nvpProps.add(attr);
                    } catch (Exception ex) {
                        GuiUtil.handleError(handlerCtx, "Name-Value Pairs must be in the format \"" +
                                "<name>=<value>\".");
                    }
                }
            }


            // Get the number to Display
            if (numberToDisplay == null) {
                numberToDisplay = DEFAULT_NUMBER_TO_DISPLAY;
            } else {
                numberToDisplay.intValue();
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
            if (fromRecord == null) {
                fromRecord = LogQuery.LAST_RECORD;
            } else {
                fromRecord.intValue();
            }

            // Search for the log entries
            List<Serializable[]> results = null;
            Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
            try {
                results =  logging.queryServerLog(
                        archivedLogFile,
                        fromRecord,
                        direction,
                        numberToDisplay,
                        (fromDate == null) ? null : ((Date)fromDate).getTime(),
                        (toDate == null) ? null : ((Date)toDate).getTime(),
                        logLevel,
                        moduleList,
                        nvpProps);
            } catch (Exception ex) {
                GuiUtil.handleError(handlerCtx, "Error while querying Log File.");
            }
            String message;
            LogQueryEntry[] query = null;
            
            if (results != null) {
                LogQueryResult r = LogQuery.Helper.toLogQueryResult(results);
                query = r.getEntries();
               // Add the results to the Model
                for (int i = 0; i < query.length; i++) {
                    HashMap oneRow = new HashMap();
                    LogQueryEntry row = (LogQueryEntry) query[i];
                    oneRow.put("recNumber", row.getRecordNumber());
                    oneRow.put("dateTime", formatDateForDisplay(
                            GuiUtil.getLocale(), row.getDate()));
                    String msgId = (String) row.getMessageID();
                    String level = (String) row.getLevel();
                    String moduleName = (String)row.getModule();
                    //only SEVERE msg provoides diagnostic info.
                    if (level.equalsIgnoreCase("severe")) {
                        // NOTE: Image name/location is hard-coded
                        oneRow.put("levelImage", GuiUtil.getMessage("common.errorGif"));
                        oneRow.put(SHOW_LEVEL_IMAGE, new Boolean(true));
                        oneRow.put("diagnosticCauses", getDiagnosticCauses(handlerCtx, msgId, moduleName));
                        oneRow.put("diagnosticChecks", getDiagnosticChecks(handlerCtx, msgId, moduleName));
//                        oneRow.put("diagnosticURI", getDiagnosticURI(handlerCtx, msgId));
                    } else {
                        oneRow.put(SHOW_LEVEL_IMAGE, new Boolean(false));
                        oneRow.put("diagnostic", "");
                    }
                    oneRow.put("level", level);
                    oneRow.put("productName", row.getProductName());
                    oneRow.put("logger", moduleName);
                    try {
                        oneRow.put("nvp", row.getNameValuePairsMap());
                    } catch (Exception ex) {
                        // ignore
                        oneRow.put("nvp", "");
                        
                    }                    
                    oneRow.put("messageID", msgId);
                    message = ((String) row.getMessage().trim());
                    if (truncateMessage && (message.length() > truncLen)) {
                        message = message.substring(0, truncLen).concat("...\n");
                    }
                    oneRow.put("message", (message == null) ? " " : Util.htmlEscape(message));
                    result.add(oneRow);
                }
            }
            
            // Set the first / last record numbers as attributes
            if (query != null && query.length > 1) {
                handlerCtx.setOutputValue("FirstLogRow",
                        ((LogQueryEntry) query[1]).getRecordNumber());
                handlerCtx.setOutputValue("LastLogRow",
                        ((LogQueryEntry) query[query.length -1]).getRecordNumber());
                hasResults = new Boolean(true);
            } else {
                handlerCtx.setOutputValue("FirstLogRow", "-1");
                handlerCtx.setOutputValue("LastLogRow", "-1");

            }

        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("HasResults", hasResults);

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
     *	This method simply takes the given SimpleDateFormat and parses the
     *	given String after applying the given format String.
     */
    private  static Date parseDateString(SimpleDateFormat fmt, String format, String dateTime) throws ParseException {
	fmt.applyLocalizedPattern(format);
	return fmt.parse(dateTime);
    }
    
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
    
   /**
     * * This method get the diagnostic Checks based on the message id
     */
    private static String getDiagnosticChecks(HandlerContext handlerCtx, String msgId, String moduleName) {
        if (msgId == null || "".equals(msgId)) {
            return formatArrayForDisplay(null);
        }
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        String[] results = logging.getDiagnosticChecks(msgId, moduleName);
        String res = formatArrayForDisplay(results);
        return res;

    } 
    
    /**
     * * This method get the diagnostic based on the message id
     */

    private static String getDiagnosticCauses(HandlerContext handlerCtx, String msgId, String moduleName) {

        if (msgId == null || "".equals(msgId)) {
            return formatArrayForDisplay(null);
        }
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        String[] results = logging.getDiagnosticCauses(msgId, moduleName);

        String res = formatArrayForDisplay(results);
        return res;
    }
       
     public static String getLogFilesDirectory(String instanceName){
        if (GuiUtil.isEmpty(instanceName))
            return "";
        String dir = "";
        
        return dir;
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
     * * This method get the diagnostic Checks based on the message id
     */
    private static String getDiagnosticURI(HandlerContext handlerCtx, String msgId, String moduleName) {
        if (msgId == null || "".equals(msgId)) {
            return "";
        }
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        String res = logging.getDiagnosticURI(msgId);
        return res;

    }

        /**
     *  This method formats the diagnostic to be displayed for HTML
     *  Add '<br>' to each elements of the ArrayList and returns the String.
     */
    protected static String formatArrayForDisplay(String[] diag) {
        if ((diag == null) || (diag.length == 0)) {
	    return "";
	}
        StringBuffer buf = new StringBuffer("<br>");
        for(int i=0; i<diag.length; i++){
            buf.append( (String)diag[i]);
	    buf.append("<br>");
        }
        return buf.toString();
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
     *	<p> This handler returns the values for loggers
     *      in LogViewer Page.</p>
     *  <p> Output value: "LoggerList" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLoggers",
        input={
            @HandlerInput(name="selectedLoggers", type=String[].class)},
    output={
        @HandlerOutput(name="LoggerList", type=List.class),
        @HandlerOutput(name="SelectLoggersCommaString", type=String.class)})

        public static void getLoggers(HandlerContext handlerCtx) {
	String[] selectedLoggers = (String[])handlerCtx.getInputValue("selectedLoggers");
        String selected = GuiUtil.arrayToString(selectedLoggers, ",");
        List loggernames = new ArrayList();
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        Map<String, String> loggers = logging.getLoggingProperties();
        if (loggers != null)   {
            for(String oneLogger:  loggers.keySet()){
                if (oneLogger.endsWith(".level")&& !oneLogger.equals(".level") ){
                    loggernames.add(oneLogger.substring(0,oneLogger.lastIndexOf(".level")));
                
                }
            }
        }
        handlerCtx.setOutputValue("LoggerList", loggernames);
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
     *	FALSE
     */
    public static final Boolean FALSE = new Boolean(false).booleanValue();

    /**
     *	TRUE
     */
    public static final Boolean TRUE = new Boolean(true).booleanValue();      
    /**
     *	If the number to display is not specified, this value will be used
     *	(40).
     */
    public static final Integer DEFAULT_NUMBER_TO_DISPLAY = new Integer(40);
    
    /**
     *
     */
    public static final String FIRST_LOG_ROW = "firstLogRow";
    
    public static final int FROM_RECORD = 0;

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
