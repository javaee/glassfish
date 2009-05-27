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

package org.glassfish.api.statistics.impl;
import org.glassfish.api.statistics.Statistic;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 
 * @author Sreenivas Munnangi
 */
public abstract class StatisticImpl implements Statistic,Serializable {
    
    private String statisticDesc = "description";
    private AtomicLong sampleTime = new AtomicLong(System.currentTimeMillis());
    private String statisticName = "name";
    private AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    private String statisticUnit = "unit";
    public static final String UNIT_COUNT = "count";
    public static final String UNIT_SECOND = "second";
    public static final String UNIT_MILLISECOND = "millisecond";
    public static final String UNIT_MICROSECOND = "microsecond";
    public static final String UNIT_NANOSECOND = "nanosecond";

    protected Map<String, Object> statMap = new ConcurrentHashMap<String, Object> ();
    
    protected static final String NEWLINE = System.getProperty( "line.separator" );

    protected StatisticImpl(String name, String unit, String desc, 
                          long start_time, long sample_time) {
        statisticName = name;
        statisticUnit = unit;
        statisticDesc = desc;
        startTime.set(start_time);
        sampleTime.set(sample_time);
    }

    protected StatisticImpl(String name, String unit, String desc) {
        statisticName = name;
        statisticUnit = unit;
        statisticDesc = desc;
    }

    public synchronized Map getStaticAsMap() {
        statMap.put("name", statisticName);
        statMap.put("unit", statisticUnit);
        statMap.put("description", statisticDesc);
        statMap.put("starttime", startTime.get());
        statMap.put("lastsampletime", sampleTime.get());
        return statMap;
    }
    
    public String getName() {
        return this.statisticName;
    }
    
    public synchronized void setName(String name) {
        this.statisticName = name;
    }

    public String getDescription() {
        return this.statisticDesc;
    }
    
    public synchronized void setDescription(String desc) {
        this.statisticDesc = desc;
    }

    public String getUnit() {
        return this.statisticUnit;
    }
    
    public synchronized void setUnit(String unit) {
        this.statisticUnit = unit;
    }

    public long getLastSampleTime() {
        return sampleTime.get();
    }
    
    public void setLastSampleTime(long sample_time) {
        sampleTime.set(sample_time);
    }

    public long getStartTime() {
        return startTime.get();
    }

    public void setStartTime(long start_time) {
        startTime.set(start_time);
    }

    public String toString() {
        return "Statistic " + getClass().getName() + NEWLINE +
            "Name: " + getName() + NEWLINE +
            "Description: " + getDescription() + NEWLINE +
            "Unit: " + getUnit() + NEWLINE +
            "LastSampleTime: " + getLastSampleTime() + NEWLINE +
            "StartTime: " + getStartTime();
    }
}
