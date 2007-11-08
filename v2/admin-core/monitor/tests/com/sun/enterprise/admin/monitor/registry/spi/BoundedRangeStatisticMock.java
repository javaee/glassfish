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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * BoundedRangeStatisticImpl.java
 *
 * Created on August 8, 2003, 12:30 PM
 */

package com.sun.enterprise.admin.monitor.registry.spi;
import javax.management.j2ee.statistics.*;

/**
 * Mock BoundedRangeStatistic implementation
 * @author  sg112326
 */
public class BoundedRangeStatisticMock extends StatisticMock implements BoundedRangeStatistic {
    private long current=0;
    private long highWater=0;
    private long lowWater=0;
    private long upperBound=0;
    private long lowerBound=0;
    
    /** Creates a new instance of BoundedRangeStatisticImpl */
    public BoundedRangeStatisticMock(long current, long highWater, long lowWater,
    long upperBound, long lowerBound, String name,
    String unit, String desc, long sampleTime,
    long startTime) {
        super(name, unit, desc, startTime, sampleTime);
        this.current=current;
        this.highWater=highWater;
        this.lowWater=lowWater;
        this.upperBound=upperBound;
        this.lowerBound=lowerBound;
    }
    
    public long getCurrent() {
        System.out.println("returning current:"+current);
        return current;
    }
    
    public long getHighWaterMark() {
        return highWater;
    }
    
    public long getLowWaterMark() {
        return lowWater;
    }
    
    public long getLowerBound() {
        return lowerBound;
    }
    
    public long getUpperBound() {
        return upperBound;
    }
}
