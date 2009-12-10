/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package javax.ejb;

import java.util.Date;
import java.io.Serializable;

/**
 * <p>A calendar-based timeout expression for an enterprise bean timer.  See
 * EJB specification for the full timer expression syntax.</p>
 * 
 * <p>Each expression attribute has two overloaded setter methods, one that takes
 * a String and one that takes an int.  The int version is merely a convenience method
 * for setting the attribute in the common case that the value is a simple integer. </p>
 * 
 * <p>E.g. <pre>scheduleExpression.second(10)</pre> is semantically equivalent to 
 *      <pre>scheduleExpression.second("10")</pre></p>
 *
 * None of the ScheduleExpression methods are required to be called.  The defaults
 * are :
 *
 * <p>{ second , minute , hour } : "0"</p>
 *
 * <p>{ dayOfMonth, month, dayOfWeek, year } : "*"</p>
 *
 * <p>timezone : default JVM time zone</p>
 *
 * <p>startDate : no start date</p>
 * 
 * <p>endDate : no end date</p>
 *
 */

public class ScheduleExpression implements Serializable {

    public ScheduleExpression() {}

    public ScheduleExpression second(String s) {
        second_ = s; 
        return this;
    }

    public ScheduleExpression second(int s) {
        second_ = s + "";
        return this;
    }

    public String getSecond() {
        return second_;
    }

    public ScheduleExpression minute(String m) {
        minute_ = m;
        return this;
    }

    public ScheduleExpression minute(int m) {
        minute_ = m + "";
        return this;
    }

    public String getMinute() {
	return minute_;
    }

    public ScheduleExpression hour(String h) {
        hour_ = h;
        return this;
    }

    public ScheduleExpression hour(int h) {
        hour_ = h + "";
        return this;
    }

    public String getHour() {
        return hour_;
    }

    public ScheduleExpression dayOfMonth(String d) {
        dayOfMonth_ = d;
        return this;
    }

    public ScheduleExpression dayOfMonth(int d) {
        dayOfMonth_ = d + "";
        return this;
    }

    public String getDayOfMonth() {
	return dayOfMonth_;
    }

    public ScheduleExpression month(String m) {
        month_ = m;
        return this;
    }

    public ScheduleExpression month(int m) {
        month_ = m + "";
        return this;
    }

    public String getMonth() {
        return month_;
    }

    public ScheduleExpression dayOfWeek(String d) {
        dayOfWeek_ = d;
        return this;
    }

    public ScheduleExpression dayOfWeek(int d) {
        dayOfWeek_ = d + "";
        return this;
    }

    public String getDayOfWeek() {
	return dayOfWeek_;
    }

    public ScheduleExpression year(String y) {
        year_ = y;
        return this;
    }

    public ScheduleExpression year(int y) {
        year_ = y + "";
        return this;
    }

    public String getYear() {
        return year_;
    }

    public ScheduleExpression timezone(String timezoneID) {
        timezoneID_ = timezoneID;
        return this;
    }

    public String getTimezone() {
        return timezoneID_;
    }

    public ScheduleExpression start(Date s) {
        start_ = (s == null) ? null : new Date(s.getTime());

        return this;
    }

    public Date getStart() {
        return (start_ == null) ? null : new Date(start_.getTime());
    }

    public ScheduleExpression end(Date e) {
        end_ = (e == null) ? null : new Date(e.getTime());

        return this;
    }

    public Date getEnd() {
        return (end_ == null) ? null : new Date(end_.getTime());
    }

    public String toString() {
	return "ScheduleExpression [second=" + second_ 
                + ";minute=" + minute_ 
                + ";hour=" + hour_ 
                + ";dayOfMonth=" + dayOfMonth_
                + ";month=" + month_
                + ";dayOfWeek=" + dayOfWeek_
                + ";year=" + year_
                + ";timezoneID=" + timezoneID_
                + ";start=" + start_
                + ";end=" + end_ 
                + "]";
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

}
