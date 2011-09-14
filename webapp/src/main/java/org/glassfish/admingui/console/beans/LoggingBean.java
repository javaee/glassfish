/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.faces.bean.*;
import javax.faces.context.FacesContextFactory;
import javax.faces.*;
import javax.faces.context.FacesContext;
import java.util.*;
import org.glassfish.admingui.console.rest.RestUtil;

@ManagedBean(name="loggingBean")
@SessionScoped
public class LoggingBean {

    private String instanceName = "server";
    private String startIndex = null;
    private String searchForward = "false";
    private String firstRecord = null;
    private String lastRecord = null;

    public static final String TIME_FORMAT = " HH:mm:ss.SSS";
    
    public LoggingBean() {
        FacesContextFactory factory = (FacesContextFactory)
            FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        Map requestMap =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        instanceName = (String)requestMap.get("instanceName");
    }

    public LoggingBean(String instanceName) {
        this.instanceName = instanceName;
    }

    public List<Map> getLogMessages() {
        String endPoint = "http://localhost:4848/management/domain/view-log/details.json";
        Map attrs = new HashMap();
        attrs.put("instanceName", instanceName);
        attrs.put("startIndex", startIndex);
        attrs.put("searchForward", searchForward);
        Map data = (Map)RestUtil.restRequest(endPoint, attrs, "GET", null, null, false, true).get("data");
        List<Map> records = (List<Map>) data.get("records");
        records = processLogRecords(records);
        return records;
    }

    private List<Map> processLogRecords(List<Map> records) {
        for (Map<String, Object> record : records) {
            record.put("loggedDateTimeInMS", formatDateForDisplay(Locale.US,
                new Date(new Long(record.get("loggedDateTimeInMS").toString()))));
        }
        if ((records != null) && (records.size() > 0)) {
            lastRecord    = records.get(records.size()-1).get("recordNumber").toString();
	    firstRecord   = records.get(0).get("recordNumber").toString();
	} else {
	    firstRecord = "-1";
            lastRecord  = "-1";
	}
        return records;
    }

    public String previous() {
        searchForward = "false";
        startIndex = firstRecord;
        return null;
    }

    public String next() {
        searchForward = "true";
        startIndex = lastRecord;
        return null;
    }

    private String formatDateForDisplay(Locale locale, Date date) {
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
}