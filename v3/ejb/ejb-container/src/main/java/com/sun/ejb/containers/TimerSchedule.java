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
package com.sun.ejb.containers;

import java.util.Date;
import java.io.Serializable;

import javax.ejb.ScheduleExpression;

/**
 * A runtime representation of the user-defined calendar-based 
 * timeout expression for an enterprise bean timer.
 */

public class TimerSchedule implements Serializable {

    private String second_ = "0";
    private String minute_ = "0";
    private String hour_ = "0";

    private String dayOfMonth_ = "*";
    private String month_ = "*";
    private String dayOfWeek_ = "*";
    private String year_ = "*";

    private Date start_ = null;
    private Date end_ = null;

    public TimerSchedule(ScheduleExpression se) {
        second_ = se.getSecond();
        minute_ = se.getMinute();
        hour_ = se.getHour();
        dayOfMonth_ = se.getDayOfMonth();
        month_ = se.getMonth();
        dayOfWeek_ = se.getDayOfWeek();
        year_ = se.getYear();
        start_ = se.getStart();
        end_ = se.getEnd();
    }

    public TimerSchedule second(String s) {
        second_ = s; 
        return this;
    }

    public TimerSchedule second(int s) {
        second_ = s + "";
        return this;
    }

    public String getSecond() {
        return second_;
    }

    public TimerSchedule minute(String m) {
        minute_ = m;
        return this;
    }

    public TimerSchedule minute(int m) {
        minute_ = m + "";
        return this;
    }

    public String getMinute() {
	return minute_;
    }

    public TimerSchedule hour(String h) {
        hour_ = h;
        return this;
    }

    public TimerSchedule hour(int h) {
        hour_ = h + "";
        return this;
    }

    public String getHour() {
        return hour_;
    }

    public TimerSchedule dayOfMonth(String d) {
        dayOfMonth_ = d;
        return this;
    }

    public TimerSchedule dayOfMonth(int d) {
        dayOfMonth_ = d + "";
        return this;
    }

    public String getDayOfMonth() {
	return dayOfMonth_;
    }

    public TimerSchedule month(String m) {
        month_ = m;
        return this;
    }

    public TimerSchedule month(int m) {
        month_ = m + "";
        return this;
    }

    public String getMonth() {
        return month_;
    }

    public TimerSchedule dayOfWeek(String d) {
        dayOfWeek_ = d;
        return this;
    }

    public TimerSchedule dayOfWeek(int d) {
        dayOfWeek_ = d + "";
        return this;
    }

    public String getDayOfWeek() {
	return dayOfWeek_;
    }

    public TimerSchedule year(String y) {
        year_ = y;
        return this;
    }

    public TimerSchedule year(int y) {
        year_ = y + "";
        return this;
    }

    public String getYear() {
        return year_;
    }

    public TimerSchedule start(Date s) {
        start_ = (s == null) ? null : new Date(s.getTime());

        return this;
    }

    public Date getStart() {
        return (start_ == null) ? null : new Date(start_.getTime());
    }

    public TimerSchedule end(Date e) {
        end_ = (e == null) ? null : new Date(e.getTime());
        return this;
    }

    public Date getEnd() {
        return (end_ == null) ? null : new Date(end_.getTime());
    }

    public String getScheduleAsString() {
        StringBuffer s = new StringBuffer()
               .append(second_).append(" # ")
               .append(minute_).append(" # ") 
               .append( hour_).append(" # ") 
               .append( dayOfMonth_).append(" # ") 
               .append( month_).append(" # ") 
               .append( dayOfWeek_).append(" # ") 
               .append( year_).append(" # ") 
               .append(((start_ == null) ? null : start_.getTime()))
               .append(" # ") 
               .append(((end_ == null) ? null : end_.getTime()));
        return s.toString();
    }

    public ScheduleExpression getScheduleExpression() {
        return new ScheduleExpression().
                second(second_).
                minute(minute_).
                hour(hour_).
                dayOfMonth(dayOfMonth_).
                month(month_).
                dayOfWeek(dayOfWeek_).
                year(year_).
                start(start_).
                end(end_);

    }

    public int hashCode() {
        return getScheduleAsString().hashCode();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null || !(o instanceof TimerSchedule))
            return false;

        TimerSchedule t = (TimerSchedule)o;
        return getScheduleAsString().equals(t.getScheduleAsString());

    }
}
