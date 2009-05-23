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
import org.glassfish.api.statistics.TimeStatistic;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.lang.reflect.*;

/** 
 * @author Sreenivas Munnangi
 */
public class TimeStatisticImpl extends StatisticImpl 
    implements TimeStatistic, InvocationHandler {
    
    private AtomicLong count = new AtomicLong(Long.MIN_VALUE);
    private final long timeNow = System.currentTimeMillis();
    private AtomicLong maxTime = new AtomicLong(timeNow);
    private AtomicLong minTime = new AtomicLong(timeNow);
    private AtomicLong totTime = new AtomicLong(0L);

    private TimeStatistic ts = (TimeStatistic) Proxy.newProxyInstance(
            TimeStatistic.class.getClassLoader(),
            new Class[] { TimeStatistic.class },
            this);

    public final String toString() {
        return super.toString() + NEWLINE + 
            "Count: " + getCount() + NEWLINE +
            "MinTime: " + getMinTime() + NEWLINE +
            "MaxTime: " + getMaxTime() + NEWLINE +
            "TotalTime: " + getTotalTime();
    }

    public TimeStatisticImpl(long counter, long maximumTime, long minimumTime,
                             long totalTime, String name, String unit, 
                             String desc, long startTime, long sampleTime) {
        super(name, unit, desc, startTime, sampleTime);
        count.set(counter);
        maxTime.set(maximumTime);
        minTime.set(minimumTime);
        totTime.set(totalTime);
    }

    public synchronized TimeStatistic getStatistic() {
        return ts;
    }
    
    public synchronized Map getStaticAsMap() {
        Map m = super.getStaticAsMap();
        m.put("count", getCount());
        m.put("maxtime", getMaxTime());
        m.put("mintime", getMinTime());
        m.put("totaltime", getTotalTime());
        return m;
    }

    /**
     * Returns the number of times an operation was invoked 
     */
    public long getCount() {
        return count.get();
    }
    
    public void setCount(long counter) {
        count.set(counter);
    }
    
    /**
     * Returns the maximum amount of time that it took for one invocation of an
     * operation, since measurement started.
     */
    public long getMaxTime() {
        return maxTime.get();
    }
    
    public void setMaxTime(long maximumTime) {
        maxTime.set(maximumTime);
    }
    
    /**
     * Returns the minimum amount of time that it took for one invocation of an
     * operation, since measurement started.
     */
    public long getMinTime() {
        return minTime.get();
    }    

    public void setMinTime(long minimumTime) {
        minTime.set(minimumTime);
    }
    
    /**
     * Returns the amount of time that it took for all invocations, 
     * since measurement started.
     */
    public long getTotalTime() {
        return totTime.get();
    }

    public void setTotalTime(long totalTime) {
        totTime.set(totalTime);
    }

    // todo: equals implementation
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object result;
        try {
            result = m.invoke(this, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " +
                       e.getMessage());
        } finally {
        }
        return result;
    }
}
