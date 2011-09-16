/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.faces.bean.*;
import java.util.*;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.glassfish.admingui.console.rest.RestUtil;

@ManagedBean(name="loggingBean")
@ViewScoped
public class LoggingBean {

    private String instanceName;
    private String startIndex;
    private String searchForward = "false";
    private String firstRecord;
    private String lastRecord;
    private List<SelectItem> selectionList;
    private String selectedIndex;

    public static final String TIME_FORMAT = " HH:mm:ss.SSS";
    
    public LoggingBean() {
        List<String> instanceList = (new EnvironmentBean()).getInstanceNames();
        selectionList = new ArrayList<SelectItem>();
        for (String instance : instanceList) {
                selectionList.add(new SelectItem(instance, instance));
        }
        if (selectionList.size() > 0) {
            instanceName = (String)selectionList.get(0).getValue();
            setSelectedIndex(instanceName);
        }
    }

    public LoggingBean(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(String instance) {
        selectedIndex = instance;
    }

    public List<SelectItem> getSelectionList() {
        return selectionList;
    }

    public List<Map> getLogMessages() {
        String endPoint = "http://localhost:4848/management/domain/view-log/details.json";
        Map attrs = new HashMap();
        attrs.put("instanceName", instanceName);
        attrs.put("startIndex", startIndex);
        attrs.put("searchForward", searchForward);
        attrs.put("maximumNumberOfResults", 30);
        List<Map> records = null;
        if (instanceName != null) {
            Map data = (Map)RestUtil.restRequest(endPoint, attrs, "GET", null, null, false, true).get("data");
            records = (List<Map>) data.get("records");
            records = processLogRecords(records);
        }
        return records;
    }

    private List<Map> processLogRecords(List<Map> records) {
        for (Map<String, Object> record : records) {
            record.put("loggedDateTimeInMS", formatDateForDisplay(Locale.US,
                new Date(new Long(record.get("loggedDateTimeInMS").toString()))));
        }
        if ((records != null) && (records.size() > 0)) {
            firstRecord    = records.get(records.size()-1).get("recordNumber").toString();
            lastRecord   = records.get(0).get("recordNumber").toString();
	} else {
	    firstRecord = "-1";
            lastRecord  = "-1";
	}
        return records;
    }

    public String valueChange(ValueChangeEvent valueChangeEvent) {
        instanceName = (String) valueChangeEvent.getNewValue();
        return null;
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