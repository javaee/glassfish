package com.sun.enterprise.deployment;

import java.util.Date;


/**
 * This class holds the metadata for a calendar-based timer.
 */
public class ScheduledTimerDescriptor extends DescribableDescriptor {

    public void setSecond(String s) {
        second_ = s;
    }

    public String getSecond() {
        return second_;
    }

    public void setMinute(String m) {
        minute_ = m;
    }

    public String getMinute() {
	    return minute_;
    }

    public void setHour(String h) {
        hour_ = h;
    }

    public String getHour() {
        return hour_;
    }

    public void setDayOfMonth(String d) {
        dayOfMonth_ = d;
    }

    public String getDayOfMonth() {
	    return dayOfMonth_;
    }

    public void setMonth(String m) {
        month_ = m;
    }

    public String getMonth() {
        return month_;
    }

    public void setDayOfWeek(String d) {
        dayOfWeek_ = d;
    }

    public String getDayOfWeek() {
	    return dayOfWeek_;
    }

    public void setYear(String y) {
        year_ = y;
    }

    public String getYear() {
        return year_;
    }

    public void setTimezone(String timezoneID) {
        timezoneID_ = timezoneID;
    }

    public String getTimezone() {
        return timezoneID_;
    }

    public void setStart(Date s) {
        start_ = (s == null) ? null : new Date(s.getTime());
    }

    public Date getStart() {
        return (start_ == null) ? null : new Date(start_.getTime());
    }

    public void setEnd(Date e) {
        end_ = (e == null) ? null : new Date(e.getTime());
    }

    public Date getEnd() {
        return (end_ == null) ? null : new Date(end_.getTime());
    }

    public void setPersistent(boolean flag) {
        persistent_ = flag;
    }

    public boolean getPersistent() {
        return persistent_;
    }

    public void setInfo(String i) {
        info_ = i;
    }

    public String getInfo() {
        return info_;
    }


    public void setTimeoutMethod(MethodDescriptor m) {
        timeoutMethod_ = m;
    }


    public MethodDescriptor getTimeoutMethod() {
        return timeoutMethod_;
    }


    private String second_ = "0";
    private String minute_ = "0";
    private String hour_ = "0";

    private String dayOfMonth_ = "*";
    private String month_ = "*";
    private String dayOfWeek_ = "*";
    private String year_ = "*";

    private String timezoneID_ = null;

    private Date start_ = null;

    private Date end_ = null;

    private MethodDescriptor timeoutMethod_;

    private boolean persistent_ = true;

    private String info_ = null;



}
