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
 * Filename: TooManyTasksException.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/collection/LongHashMap.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:13 $
 */
 
package com.sun.enterprise.util.collection;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class LongHashMap {
	
	int				maxBuckets = 0;
	Bucket[]		buckets;
	
	public LongHashMap() {
		this(89);
	}
	
	public LongHashMap(int maxBuckets) {
		this.maxBuckets = maxBuckets;
		buckets = new Bucket[maxBuckets];
		for (int i=0; i<maxBuckets; i++) {
			buckets[i] = new SortedArrayListBucket();
		}
	}
	
	public LongHashMap(Bucket[]	buckets) {
		this.buckets = buckets;
		this.maxBuckets = buckets.length;
	}
	
	public void put(long key, Object object) {
		int index = (int) Math.abs(key % maxBuckets);
		buckets[index].put(key, object);
	}
	
	public Object get(long key) {
		int index = (int) Math.abs(key % maxBuckets);
		return buckets[index].get(key);
	}
	
	public Object remove(long key) {
		int index = (int) Math.abs(key % maxBuckets);
		return buckets[index].remove(key);
	}
	
	public void print() {
		for (int i=0; i<maxBuckets; i++) {
			System.out.println("Bucket[" + i + "]: " + buckets[i]);
		}
	}   
	
	public static void main(String[] args) {
		
    	int count = 20;
    	long time=0, t1=0, t2 = 0;
    	String data = "SomeData_";

		LongHashMap map = new LongHashMap();
		for (long i=0; i<count; i+= 5) {
			map.put(i, data + i);
		}
		
		for (long i=1; i<count; i+= 3) {
			map.put(i, data + i);
		}
		
		for (long i=3; i<count; i+= 4) {
			map.put(i, data + i);
		}
		
		for (long i=-23; i<count; i+= 4) {
			map.put(i, data + i);
		}
		
   		for (long j=-25; j<25; j++) {
    		System.out.println("Key: " + j + "; val: " + map.get(j));
    	}
    	t2 = System.currentTimeMillis();
		
	}

}