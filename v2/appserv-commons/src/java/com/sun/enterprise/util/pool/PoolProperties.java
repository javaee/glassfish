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

//NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference.

/*
 * Filename: PoolProperties.java	
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
 
/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/PoolProperties.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:28 $
 */
 
package com.sun.enterprise.util.pool;

import java.util.Properties;

public class PoolProperties {

	/**
	 * The factory class name property. Not used now.
	 */
	public static final String		FACTORY_CLASS_NAME = "pool.factory.class";
	
	/**
	 * Minimum pool size property.
	 */
	public static final String		MINIMUM_SIZE = "pool.minsize";
	public static final String		INITIAL_SIZE = "pool.initialsize";
	public static final String		LOW_WATER_MARK = "pool.lowwatermark";
	public static final String		HI_WATER_MARK = "pool.hiwatermark";
	public static final String		MAX_STRONG_REFERENCES = "pool.maxstrongrefs";
	public static final String		POOL_LIMIT = "pool.limit";
	public static final String		MAX_IDLE_TIME = "pool.maxidletime";
	
	public long		key;
	public String	factoryClassName;
	/**
	 * The minimum pool size. Default 0.
	 */
	public int		minimumSize;
	
	/**
	 * The initial pool size. Default 0.
	 */
	public int		initialSize;
	
	/**
	 * Low water mark. Pool implementation may interpret this property accordingly.
	 */
	public int		lowWaterMark;
	
	/**
	 * High water mark. Pool implementation may interpret this property accordingly.
	 */
	public int		hiWaterMark;
	
	/**
	 * Maximum strong References. SoftObjectPools uses this property. This property tells
	 *	the maximum number of strong reference that can be maintained in the pool. This must
	 *	be less than the pool limit and difference indicates the number of SoftReferences
	 *	that will be used to hold the objects in the pool.
	 */
	public int		maxStrongRefs;
	
	/**
	 * The maximum size of the pool. <b>If this value is less than or equal to zero, then this
	 *	pool will be treated as an UnBounded pool.</b>
	 */
	public int		poolLimit;
	public long		maxIdleTime;

	public PoolProperties() {
	}
	
	public PoolProperties(int minimumSize, int poolLimit) {
		this.minimumSize = minimumSize;
		this.poolLimit = poolLimit;
	}
	
	public PoolProperties(Properties props) {
		factoryClassName = props.getProperty(FACTORY_CLASS_NAME);
		minimumSize = getIntProperty(props, MINIMUM_SIZE, 0);
		initialSize = getIntProperty(props, INITIAL_SIZE, 0);
		lowWaterMark = getIntProperty(props, LOW_WATER_MARK, 0);
		hiWaterMark = getIntProperty(props, HI_WATER_MARK, 0);
		poolLimit = getIntProperty(props, POOL_LIMIT, 0);
		maxIdleTime = getLongProperty(props, MAX_IDLE_TIME, 60 * 1000);
	}
	
	public String	getFactoryClassName() {
		return this.factoryClassName;
	}
	
	public void setFactoryClassName(String name) {
		this.factoryClassName = name;
	}
	
	
	public int getMinimumSize() {
		return this.minimumSize;
	}
	public void setMinimumSize(int val) {
		this.minimumSize = val;
	}
		
	
	public int		getInitialSize() {
		return this.initialSize;
	}
	public void setInitialSize(int val) {
		this.initialSize = val;
	}
	
	
	public int		getLowWaterMark() {
		return this.lowWaterMark;
	}
	public void setLowWaterMark(int val) {
		this.lowWaterMark = val;
	}
	
	
	public int		getHiWaterMark() {
		return this.hiWaterMark;
	}
	public void setHiWaterMark(int val) {
		this.hiWaterMark = val;
	}
	
	
	public int		getMaxStrongRefs() {
		return this.maxStrongRefs;
	}
	public void setMaxStrongRefs(int val) {
		this.maxStrongRefs = val;
	}
	
	
	public int		getPoolLimit() {
		return this.poolLimit;
	}
	public void setPoolLimit(int val) {
		this.poolLimit = val;
	}
	
	
	public long getMaxIdleTime() {
		return this.maxIdleTime;
	}
	public void setMaxIdleTime(long val) {
		this.maxIdleTime = val;
	}
	
	
	
	
    private int getIntProperty(Properties props, String name, int defaultValue) {
    	try {
    		return Integer.parseInt(props.getProperty(name));
    	} catch (Throwable th) {
	    	return defaultValue;
    	}
    }
    
    
    private long getLongProperty(Properties props, String name, long defaultValue) {
    	try {
    		return Long.parseLong(props.getProperty(name));
    	} catch (Throwable th) {
	    	return defaultValue;
    	}
    }
    
    public String toString() {
    	StringBuffer sbuf = new StringBuffer();
    	sbuf.append("key: ").append(key)
    		.append("; min: ").append(minimumSize)
    		.append("; init: ").append(initialSize)
    		.append("; lowWM: ").append(lowWaterMark)
    		.append("; hiWM: ").append(hiWaterMark)
    		.append("; maxSRefs: ").append(maxStrongRefs)
    		.append("; limit: ").append(poolLimit);
    	return sbuf.toString();
    }
    
}   
