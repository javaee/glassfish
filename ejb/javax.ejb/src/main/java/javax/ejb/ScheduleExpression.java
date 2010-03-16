/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2010 Sun Microsystems, Inc. All rights reserved.
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

    private static final long serialVersionUID = -3813254457230997879L;

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
