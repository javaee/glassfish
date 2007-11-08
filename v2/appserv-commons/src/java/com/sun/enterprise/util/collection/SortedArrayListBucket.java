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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/collection/SortedArrayListBucket.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:13 $
 */
 
package com.sun.enterprise.util.collection;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
	

/**
 *
 */
	 
/*
 * A simple test program populated an  ArrayListBucket with 20 entries. Then it performed 
 *	50,000,000 get(int key) took 32.968 seconds!! 60% of the time the key that was searched 
 *	was not in the bucket and hence had to search the maximum.
 */
public class SortedArrayListBucket
	implements Bucket
{

	protected ArrayList		entries;
	
    SortedArrayListBucket() {
    	entries = new ArrayList();
    }
    
    public Object put(long searchKey, Object object) {
    	return put((int) searchKey, object);
    }
    
    public Object put(int searchKey, Object object) {
    	int low = 0, high = entries.size()-1, mid;
    	int entryKey = 0;
    	IntEntry entry = null;
    	while (low <= high) {
    		mid = (low + high) / 2;
    		entry = (IntEntry) entries.get(mid);
    		entryKey = entry.key;
    		if (entryKey == searchKey) {
    			Object oldObject = entry.object;
    			entry.object = object;
    			return oldObject;
    		} else if (searchKey < entryKey) {
    			high = mid - 1;
    		} else {
    			low = mid + 1;
    		}
    	}
    		
    	//totalEntries++;
   		//System.out.println("**Inserting key: " + searchKey + " at Lo: " + low);
		entries.add(low, new IntEntry(searchKey, object));
		return null;
    }
	    
    public Object get(long searchKey) {
    	return get((int) searchKey);
    }
    
    public Object get(int searchKey) {
    	int low = 0, high = entries.size()-1, mid;
    	int entryKey = 0;
    	IntEntry entry = null;
    	while (low <= high) {
    		mid = (low + high) / 2;
    		entry = (IntEntry) entries.get(mid);
    		entryKey = entry.key;
    		if (entryKey == searchKey) {
    			return entry.object;
    		} else if (searchKey < entryKey) {
    			high = mid - 1;
    		} else {
    			low = mid + 1;
    		}
    	}
    	return null;
    }
	    
    public Object remove(long searchKey) {
    	return remove((int) searchKey);
    }
        
    public Object remove(int searchKey) {
    	int low = 0, high = entries.size()-1, mid;
    	int entryKey = 0;
    	IntEntry entry = null;

    	while (low <= high) {
    		mid = (low + high) / 2;
    		entry = (IntEntry) entries.get(mid);
    		entryKey = entry.key;
    		if (entryKey == searchKey) {
    			entries.remove(mid);
    			//totalEntries--;
    			return entry.object;
    		} else if (searchKey < entryKey) {
    			high = mid - 1;
    		} else {
    			low = mid + 1;
    		}
    	}
    	return null;
    }
    
    public int size() {
    	return entries.size();
    }

    public boolean containsKey(int searchKey) {
    	return (get((long) searchKey) != null);
    }

    public boolean containsKey(long searchKey) {
    	return (get(searchKey) != null);
    }

    public Iterator iterator() {
    	return new BucketIterator(entries, false);
    }

    public Iterator entryIterator() {
    	return new BucketIterator(entries, true);
    }

	private class BucketIterator
		implements java.util.Iterator
	{
		ArrayList	entries;
		int			index = 0;
		boolean		iterateEntry;
		
		BucketIterator(ArrayList entries, boolean iterateEntry) {
			this.entries = entries;
			this.index = 0;
			this.iterateEntry = iterateEntry;
		}
		
		public boolean hasNext() {
			return (index < entries.size());
		}
		
		public Object next() {
			return (iterateEntry ? entries.get(index++) : ((IntEntry) entries.get(index++)).object);
		}
		
		public void remove() {
			
		}
		
	}
    	
    
	    
}

