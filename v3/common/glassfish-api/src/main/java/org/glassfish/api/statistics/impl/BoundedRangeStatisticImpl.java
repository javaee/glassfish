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
import org.glassfish.api.statistics.BoundedRangeStatistic;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.lang.reflect.*;


/** 
 * @author Sreenivas Munnangi
 */
public final class BoundedRangeStatisticImpl extends StatisticImpl 
    implements BoundedRangeStatistic, InvocationHandler {
    
    private AtomicLong lowerBound = new AtomicLong(0L);
    private AtomicLong upperBound = new AtomicLong(0L);
    private AtomicLong currentVal = new AtomicLong(Long.MIN_VALUE);
    private AtomicLong highWaterMark = new AtomicLong(Long.MIN_VALUE);
    private AtomicLong lowWaterMark = new AtomicLong(Long.MIN_VALUE);
    
    private BoundedRangeStatistic bs = (BoundedRangeStatistic) Proxy.newProxyInstance(
            BoundedRangeStatistic.class.getClassLoader(),
            new Class[] { BoundedRangeStatistic.class },
            this);

    public String toString() {
        return super.toString() + NEWLINE + 
            "Current: " + getCurrent() + NEWLINE +
            "LowWaterMark: " + getLowWaterMark() + NEWLINE +
            "HighWaterMark: " + getHighWaterMark() + NEWLINE +
            "LowerBound: " + getLowerBound() + NEWLINE +
            "UpperBound: " + getUpperBound();
    }


    public BoundedRangeStatisticImpl(long curVal, long highMark, long lowMark,
                                     long upper, long lower, String name,
                                     String unit, String desc, long startTime,
                                     long sampleTime) {
        super(name, unit, desc, startTime, sampleTime);
        currentVal.set(curVal);
        highWaterMark.set(highMark);
        lowWaterMark.set(lowMark);
        upperBound.set(upper);
        lowerBound.set(lower);
    }
    
    public synchronized BoundedRangeStatistic getStatistic() {
        return bs;
    }

    public synchronized Map getStaticAsMap() {
        Map m = super.getStaticAsMap();
        m.put("current", getCurrent());
        m.put("lowerbound", getLowerBound());
        m.put("upperbound", getUpperBound());
        m.put("lowwatermark", getLowWaterMark());
        m.put("highwatermark", getHighWaterMark());
        return m;
    }

    public long getCurrent() {
        return currentVal.get();
    }
    
    public void setCurrent(long curVal) {
        currentVal.set(curVal);
    }
    public long getHighWaterMark() {
        return highWaterMark.get();
    }
    
    public void setHighWaterMark(long highMark) {
        highWaterMark.set(highMark);
    }
    public long getLowWaterMark() {
        return lowWaterMark.get();
    }
    
    public void setLowWaterMark(long lowMark) {
        lowWaterMark.set(lowMark);
    }
    public long getLowerBound() {
        return lowerBound.get();
    }
    
    public void setLowerBound(long lower) {
        lowerBound.set(lower);
    }
    /**
     * Returns the highest possible value, that this statistic is permitted to attain.
     */
    public long getUpperBound() {
        return upperBound.get();
    }
	
    public void setUpperBound(long upper) {
        upperBound.set(upper);
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
