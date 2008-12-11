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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.BitSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.io.Serializable;

import javax.ejb.ScheduleExpression;
import javax.ejb.Schedule;
import javax.ejb.EJBException;

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

    private boolean automatic_ = true;
    private boolean configured = false;
    private boolean lastDayOfMonth = false;
    private int dayBeforeEndOfMonth = 0;

    private BitSet seconds = new BitSet(60);
    private BitSet minutes = new BitSet(60);
    private BitSet hours = new BitSet(24);
    private BitSet days = new BitSet(31);
    private BitSet daysOfWeek = new BitSet(7);
    private BitSet daysOfMonth = new BitSet(31);
    private BitSet months = new BitSet(12);

    private static Map<Object, Integer> conversionTable = new HashMap<Object, Integer>();

    private List<String> daysOfWeekOrRangesOfDaysInMonth = new ArrayList<String>();

    private static final Pattern simpleRangePattern = Pattern.compile("[0-9]+\\s*-\\s*([0-9]+|last)");
    private static final Pattern positivePattern = Pattern.compile("[0-9]+");
    private static final Pattern negativePattern = Pattern.compile("-[1-7]");
    private static final Pattern orderedDayPattern = Pattern.compile("(1st|2nd|3rd|[45]th|last)\\s+[a-z][a-z][a-z]");

    private static final char rangeChar     = '-';
    private static final char incrementChar = '/';
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String DAY_OF_WEEK = "dayOfWeek";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    static {
        conversionTable.put("jan", 1);
        conversionTable.put("feb", 2);
        conversionTable.put("mar", 3);
        conversionTable.put("apr", 4);
        conversionTable.put("may", 5);
        conversionTable.put("jun", 6);
        conversionTable.put("jul", 7);
        conversionTable.put("aug", 8);
        conversionTable.put("sep", 9);
        conversionTable.put("oct", 10);
        conversionTable.put("nov", 11);
        conversionTable.put("dec", 12);

        conversionTable.put("sun", 0);
        conversionTable.put("mon", 1);
        conversionTable.put("tue", 2);
        conversionTable.put("wed", 3);
        conversionTable.put("thu", 4);
        conversionTable.put("fri", 5);
        conversionTable.put("sat", 6);

        conversionTable.put(0, Calendar.SUNDAY);
        conversionTable.put(1, Calendar.MONDAY);
        conversionTable.put(2, Calendar.TUESDAY);
        conversionTable.put(3, Calendar.WEDNESDAY);
        conversionTable.put(4, Calendar.THURSDAY);
        conversionTable.put(5, Calendar.FRIDAY);
        conversionTable.put(6, Calendar.SATURDAY);
        conversionTable.put(7, Calendar.SUNDAY);
    }

    /** Construct TimerSchedule instance with all defaults.
      */
    public TimerSchedule() {}

    /** Construct TimerSchedule instance from a given ScheduleExpression.
      * Need to copy all values because ScheduleExpression is mutable
      * and can be modified by the user.
      */
    public TimerSchedule(ScheduleExpression se) {
        second_ = se.getSecond().trim();
        minute_ = se.getMinute().trim();
        hour_ = se.getHour().trim();
        dayOfMonth_ = se.getDayOfMonth().trim();
        month_ = se.getMonth().trim();
        dayOfWeek_ = se.getDayOfWeek().trim();
        year_ = se.getYear().trim();

        // Create local copies
        start(se.getStart());
        end(se.getEnd());

        configure();
    }

    /** Construct TimerSchedule instance from a given Schedule annotation.
      */
    public TimerSchedule(Schedule s) {
        second_ = s.second().trim();
        minute_ = s.minute().trim();
        hour_ = s.hour().trim();
        dayOfMonth_ = s.dayOfMonth().trim();
        month_ = s.month().trim();
        dayOfWeek_ = s.dayOfWeek().trim();
        year_ = s.year().trim();

        configure();
    }

    /** Reconstruct TimerSchedule instance from a given String.
      */
    public TimerSchedule(String s) {
        String[] sp = s.split(" # ");

        if (sp.length != 10) {
            throw new EJBException("Cannot construct TimerSchedule from " + s);
        }

        second_ = sp[0];
        minute_ = sp[1];
        hour_ = sp[2];
        dayOfMonth_ = sp[3];
        month_ = sp[4];
        dayOfWeek_ = sp[5];
        year_ = sp[6];
        start_ = (sp[7].equals("null")? null : new Date(Long.parseLong(sp[7])));
        end_ = (sp[8].equals("null")? null : new Date(Long.parseLong(sp[8])));
        automatic_ = Boolean.parseBoolean(sp[9]);

        configure();
    }

    public TimerSchedule second(String s) {
        second_ = s.trim(); 
        return this;
    }

    public String getSecond() {
        return second_;
    }

    public TimerSchedule minute(String m) {
        minute_ = m.trim();
        return this;
    }

    public String getMinute() {
	return minute_;
    }

    public TimerSchedule hour(String h) {
        hour_ = h.trim();
        return this;
    }

    public String getHour() {
        return hour_;
    }

    public TimerSchedule dayOfMonth(String d) {
        dayOfMonth_ = d.trim();
        return this;
    }

    public String getDayOfMonth() {
	return dayOfMonth_;
    }

    public TimerSchedule month(String m) {
        month_ = m.trim();
        return this;
    }

    public String getMonth() {
        return month_;
    }

    public TimerSchedule dayOfWeek(String d) {
        dayOfWeek_ = d.trim();
        return this;
    }

    public String getDayOfWeek() {
	return dayOfWeek_;
    }

    public TimerSchedule year(String y) {
        year_ = y.trim();
        return this;
    }

    public String getYear() {
        return year_;
    }

    public TimerSchedule start(Date s) {
        // Create a copy of the user's value
        start_ = (s == null) ? null : new Date(s.getTime());

        return this;
    }

    public Date getStart() {
        // Return a copy of the internal value
        return (start_ == null) ? null : new Date(start_.getTime());
    }

    public TimerSchedule end(Date e) {
        // Create a copy of the user's value
        end_ = (e == null) ? null : new Date(e.getTime());
        return this;
    }

    public Date getEnd() {
        // Return a copy of the internal value
        return (end_ == null) ? null : new Date(end_.getTime());
    }

    public TimerSchedule setAutomatic(boolean b) {
        automatic_ = b;
        return this;
    }

    public boolean isAutomatic() {
        return automatic_;
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
               .append(((end_ == null) ? null : end_.getTime()))
               .append(" # ").append(automatic_);

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

    /**
     * Returns true if this date can be used for the next timeout of
     * the schedule represented by this instance.
     */
    public boolean isValid(Calendar date) {
        if ((end_ != null && date.getTimeInMillis() > end_.getTime()) ||
                (!year_.equals("*") && date.get(Calendar.YEAR) > Integer.parseInt(year_))) {
            return false;
        }

        return true;
    }

    /**
     * Returns the Date of the next possible timeout.
     */
    public Calendar getNextTimeout() {
        if (!configured) {
            configure();
        }

        Calendar next = new GregorianCalendar();
        next.add(Calendar.SECOND, 1);
        next.set(Calendar.MILLISECOND, 0);

        int year = 0;
        if (!year_.equals("*")) {
            year = Integer.parseInt(year_);
            if (next.get(Calendar.YEAR) < year) {
                // set to the beginning of the year
                next.set(year, 0, 1, 0, 0, 0);
                System.out.println("==> Year reset " + next.getTime()); 
            }
        }

        while (end_ == null || !next.getTime().after(end_)) {

            if (year != 0 && next.get(Calendar.YEAR) > year) {
                break;
            }

            int currvalue = next.get(Calendar.MONTH);
            if(skipToNextValue(next, months, Calendar.MONTH, Calendar.YEAR)) {
                next.set(Calendar.DAY_OF_MONTH, 1);
                next.set(Calendar.HOUR_OF_DAY, 0);
                next.set(Calendar.MINUTE, 0);
                next.set(Calendar.SECOND, 0);

                continue;
            }

            if (dayOfWeek_.equals("*") || !dayOfMonth_.equals("*")) {
                //System.out.println("==> Processing DAY_OF_MONTH ...");
                if(skipToNextValue(next, daysOfMonth, Calendar.DAY_OF_MONTH, Calendar.MONTH)) {
                    next.set(Calendar.HOUR_OF_DAY, 0);
                    next.set(Calendar.MINUTE, 0);
                    next.set(Calendar.SECOND, 0);
                    continue;
                }
            }

            if (dayOfMonth_.equals("*") || !dayOfWeek_.equals("*")) {
                //System.out.println("==> Processing DAY_OF_WEEK ...");
                if(skipToNextValue(next, daysOfWeek, Calendar.DAY_OF_WEEK, Calendar.WEEK_OF_MONTH)) {
                    next.set(Calendar.HOUR_OF_DAY, 0);
                    next.set(Calendar.MINUTE, 0);
                    next.set(Calendar.SECOND, 0);
                    continue;
                }
            }

            if(skipToNextValue(next, hours, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH)) {
                next.set(Calendar.MINUTE, 0);
                next.set(Calendar.SECOND, 0);
                continue;
            }

            if(skipToNextValue(next, minutes, Calendar.MINUTE, Calendar.HOUR_OF_DAY)) {
                next.set(Calendar.SECOND, 0);
                continue;
            }

            if(skipToNextValue(next, seconds, Calendar.SECOND, Calendar.MINUTE)) {
                continue;
            }

            break;
        }

        if (start_ != null && next.getTimeInMillis() < start_.getTime()) {
            next.setTime(start_);
        }

        return next;
    }

    /**
     * Populate all internale structures to be used for the next timeout
     * calculations
     */
    private void configure() {
        // XXX Can it ever be called by different threads?
        parseNumbersOrNames(second_, seconds, 0, 60, false, SECOND);
        parseNumbersOrNames(minute_, minutes, 0, 60, false, MINUTE);
        parseNumbersOrNames(hour_, hours, 0, 24, false, HOUR);
        parseNumbersOrNames(dayOfWeek_, daysOfWeek, 0, 7, true, DAY_OF_WEEK);
        parseNumbersOrNames(month_, months, 1, 12, false, MONTH);
        parseDaysOfMonth();

        configured = true;
    }

    /**
     * Populate the BitSet where true bits represent set values.
     * Input data can be either a number or a case insensitive abbreviated name.
     */
    private void parseNumbersOrNames(String s, BitSet bits, 
            int start, int size, boolean useCalendarValue, String field) {
        // All
        if (s.equals("*")) {
            bits.set(0, size);
            return;
        }

        // List
        if (s.indexOf(',') > 0) {
            String[] arr = splitList(s);
            for (String s0 : arr) {
                bits.set(getNumericValue(s0, start, size, useCalendarValue, field));
            }
            return;
        }

        // Range
        if (s.indexOf(rangeChar) > 0) {
            String[] arr = splitBy(s, rangeChar);
            int begin = getNumericValue(arr[0], start, size, useCalendarValue, field);
            int end = getNumericValue(arr[1], start, size, useCalendarValue, field);
            setBitsRange(bits, begin, end);
            return;
        }

        // Increments
        if (s.indexOf(incrementChar) > 0) {
            String[] arr = splitBy(s, incrementChar);
            int begin = 0;
            if (!arr[0].equals("*")) {
                begin = getNumericValue(arr[0], start, size, useCalendarValue, field);
            }

            int incr = getNumericValue(arr[1], start, size, useCalendarValue, field);
            for (int i = begin; i < size; ) {
                bits.set(i);
                i = i + incr;
            }
            return;
        }

        // Single value
        bits.set(getNumericValue(s, start, size, useCalendarValue, field));
        
    }

    /**
     * Preprocess data that represents days of the month.
     * Input data can be one or more of a positive or a negative number, an order,
     * or a case insensitive abbreviated name.
     */
    private void parseDaysOfMonth() {
        String s = dayOfMonth_.trim();

        // All
        if (s.equals("*")) {
            daysOfMonth.set(1, 32);
            return;
        }

        // List
        if (s.indexOf(',') > 0) {
            String[] arr = splitList(dayOfMonth_);
            for (String s0 : arr) {
                processDayOfMonth(s0);
            }
            return;
        }

        // Range
        if (s.indexOf(rangeChar, 1) > 0) {
            if (simpleRangePattern.matcher(dayOfMonth_).matches()) {
                // If these are positive numbers or a range from a positive
                // number to the last day of the month - process them now
                String[] arr = splitBy(s, rangeChar);
                int begin = Integer.parseInt(arr[0]);
                int end = 31;
                if (positivePattern.matcher(arr[1]).matches()) {
                    end = Integer.parseInt(arr[1]);
                }

                if (begin < 0 || end < begin) {
                    throw new IllegalArgumentException("Invalid dayOfMonth value: " + s);
                }

                setBitsRange(daysOfMonth, begin, end);

             } else {
                 // Otherwise just remember - we'll process it later
                 daysOfWeekOrRangesOfDaysInMonth.add(dayOfMonth_.toLowerCase());
             } 

            return;
        }

        // Single value
        processDayOfMonth(dayOfMonth_);
    }

    private boolean skipToNextValue(Calendar date, BitSet bits, int field, int highfiled) {
        boolean changed = false;

        int currvalue = date.get(field);
        int nextvalue = currvalue;
        if (field == Calendar.DAY_OF_MONTH) {
             bits = populateCurrentMonthBits(date);
        }
        if (!bits.get(currvalue)) {
            nextvalue = bits.nextSetBit(currvalue);
            if (nextvalue == -1 || nextvalue > date.getActualMaximum(field)) {
                nextvalue = bits.nextSetBit(0);
                //System.out.println("==> Incrementing ...");
                date.add(highfiled, 1);
            }

            if (nextvalue == -1) 
                throw new IllegalStateException("Should not happen - no value found");

            date.set(field, nextvalue);
            changed = true;
        }

        return changed;
    }

    /**
     * Split a String that represents a list of values.
     */
    private String[] splitList(String s) {
        return s.split("\\s*,\\s*");
    }

    /**
     * Split a String that represents a range of values.
     */
    private String[] splitBy(String s, char ch) {
        int i = s.indexOf(ch, 1);
        return new String[] {s.substring(0,i).trim(), s.substring(i+1).trim()};
    }

    /**
     * Convert a String to a number. If the String represents a 
     * number, return its int value. If the String represents a 
     * (case insensitive) name of the day of the week or a month, 
     * return the corresponding numeric value from the conversionTable. 
     * If useCalendarValue is true, return the value from the conversionTable
     * that represents Calendar's value of the result.
     */
    private int getNumericValue(String s, int start, int size, 
            boolean useCalendarValue, String field) {

        int i = start;
        if (positivePattern.matcher(s).matches()) {
            i = Integer.parseInt(s);
            if (i < start || i > (start + size -1)) {
                throw new IllegalArgumentException("Invalid " + field + " value: " + s);
            }
        } else {
            Integer val = conversionTable.get(s.toLowerCase());
            if (val == null) {
                throw new IllegalArgumentException("Invalid " + field + " value: " + s);
            }
            i = val.intValue();
        }

        return (useCalendarValue)? conversionTable.get(i) : i - start;
    }

    /**
     * Process a single value that represents a day of the month.
     * Input data can be a positive or a negative number, an order,
     * or a case insensitive abbreviated name.
     */
    private void processDayOfMonth(String s) {
        String s0 = s.toLowerCase();

        if (positivePattern.matcher(s0).matches()) {
             daysOfMonth.set(Integer.parseInt(s0));
        } else if (negativePattern.matcher(s0).matches()) {
             dayBeforeEndOfMonth = Integer.parseInt(s0.substring(1));
        } else if (s0.equals("last")) {
             lastDayOfMonth = true;
        } else {
             // Just remember - we'll process it later
             daysOfWeekOrRangesOfDaysInMonth.add(s0);
        }
    }

    /**
     * Use preprocessed values to create a BitSet that represents set 
     * days of this month.
     */
    private BitSet populateCurrentMonthBits(Calendar date) {
        if(dayOfMonth_.equals("*")) {
            return daysOfMonth;
        }

        BitSet bits = (BitSet)daysOfMonth.clone();
        if (lastDayOfMonth) {
            bits.set(date.getActualMaximum(Calendar.DAY_OF_MONTH));
        }

        if (dayBeforeEndOfMonth > 0) {
            bits.set(date.getActualMaximum(Calendar.DAY_OF_MONTH) - dayBeforeEndOfMonth);
        }

        int size = daysOfWeekOrRangesOfDaysInMonth.size();
        for (int i = 0; i < size; i++) {
            setDaysOfWeek(bits, date, daysOfWeekOrRangesOfDaysInMonth.get(i));
        }

        return bits;
    }

    /**
     * Return day of the month that represents the specific occurance of 
     * this day of the week, like "2nd Mon" or "Last Wed" or part of a range
     * which in turn can be any valid option for dayOfMonth.
     */
    private int getDayForDayOfMonth(Calendar date, String s) {
        if (positivePattern.matcher(s).matches()) {
             return Integer.parseInt(s);
        }

        Calendar testdate = (Calendar)date.clone();
        int lastday = testdate.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (s.equals("last")) {
            return lastday;

        } else if (negativePattern.matcher(s).matches()) {
             return lastday - Integer.parseInt(s.substring(1));

        } else if (orderedDayPattern.matcher(s).matches()) {
             String arr[] = s.split("\\s");
             int num = -1;
             if (!arr[0].equals("last")) {
                 num = Integer.parseInt(arr[0].substring(0, 1));
             }

             // Convert name of the day to a number, then number to the
             // Calendar's value for that day.
             int day = conversionTable.get(conversionTable.get(arr[1]));

             return getDayForDayOfWeek(testdate, lastday, day, num);
        }

        throw new IllegalArgumentException("Invalid dayOfMonth value: " + s);
    }

    /**
     * Return day of the month that represents the specific occurance of
     * this day of the week, like "2nd Mon" or "Last Wed".
     */
    private int getDayForDayOfWeek(Calendar testdate, int lastday, int day, int num) {

        if (num == -1) {
            return getLastDayForDayOfWeek(testdate, day, lastday);
        }

        int result = 1;
        for (int i = (num - 1) * 7 + 1; i <= lastday; i++ ) {
            testdate.set(Calendar.DAY_OF_MONTH, i);
            int testday = testdate.get(Calendar.DAY_OF_WEEK);
            if (testday == day) {
                result = i;
                break;
            }
        }

        return result;
    }

    /**
     * Return day of the month that represents the last occurance of this day of the week
     */
    private int getLastDayForDayOfWeek(Calendar testdate, int day, int lastday) {

        int result = lastday;
        for (int i = lastday; i >= 1; i--) {
            testdate.set(Calendar.DAY_OF_MONTH, i);
            int testday = testdate.get(Calendar.DAY_OF_WEEK);
            if (testday == day) {
                result = i;
                break;
            }
        }

        return result;
    }

    private void setDaysOfWeek(BitSet bits, Calendar date, String s) {
        // Check if it's a range
        if (s.indexOf(rangeChar, 1) > 0) {
            String[] arr = splitBy(s, rangeChar);

            int begin = getDayForDayOfMonth(date, arr[0]);
            int end = getDayForDayOfMonth(date, arr[1]);
            setBitsRange(bits, begin, end);

        } else {
            bits.set(getDayForDayOfMonth(date, s));
        } 
    }

    /**
     * Set bits on for all values between begin and end (inclusive).
     */
    private void setBitsRange(BitSet bits, int begin, int end) {
        for (int i = begin; i <= end; i++) {
            bits.set(i);
        }
    }
}
