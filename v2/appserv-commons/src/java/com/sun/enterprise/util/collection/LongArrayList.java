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

package	com.sun.enterprise.util.collection;

import java.util.*;

public class LongArrayList
	implements List
{

	long[]			data;
	int				minIndex;		//used for sub list implementation
	int				size;
	double			growFactor;

	public LongArrayList() {
		this(6, 0.5);
	}

	public LongArrayList(int initialCapacity) {
		this(initialCapacity, 0.5);
	}

	public LongArrayList(int initialCapacity, double growFactor) {
		data = new long[initialCapacity];
		this.growFactor = growFactor;
	}

	private LongArrayList(long[] data, int minIndex, int size, double growFactor) {
		this.data = data;
		this.minIndex = minIndex;
		this.size = size;
		this.growFactor = growFactor;
	}

	public void add(int index, Object o) {
		long val = ((Long) o).longValue();
		if (size == data.length) {
			makeRoom(1);
		}

		System.arraycopy(data, index, data, index+1, size - index);
		data[index] = val;
		size++;

	}

	public void add(int index, long val) {
		if (size == data.length) {
			makeRoom(1);
		}

		System.arraycopy(data, index, data, index+1, size - index);
		data[index] = val;
		size++;

	}

	public boolean add(Object o) {
		long		val = ((Long) o).longValue();
		if (size == data.length) {
			makeRoom(1);
		}

		data[size++] = val;
		return true;
	}

	public boolean add(long val) {
		if (size == data.length) {
			makeRoom(1);
		}

		data[size++] = val;
		return true;
	}

	public boolean addAll(Collection coll) {
		int collSize = coll.size();
		makeRoom(collSize);
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			data[size++] = ((Long) iter.next()).longValue();
		}
		return (collSize > 0);
	}

	public boolean addAll(LongArrayList coll) {
		int collSize = coll.size();
		makeRoom(collSize);
		LongIterator longIter = coll.longIterator();
		while (longIter.hasNext()) {
			data[size++] = longIter.nextLong();
		}
		return (collSize > 0);
	}

	public boolean addAll(int index, Collection coll) {
		int collSize = coll.size();
		makeRoom(collSize);
		System.arraycopy(data, index, data, index+collSize, size - index);
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			data[index++] = ((Long) iter.next()).longValue();
		}
		size += collSize;
		return (collSize > 0);
	}

	public boolean addAll(int index, LongArrayList coll) {
		int collSize = coll.size();
		makeRoom(collSize);
		System.arraycopy(data, index, data, index+collSize, size - index);
		LongIterator longIter = coll.longIterator();
		while (longIter.hasNext()) {
			data[index++] = longIter.nextLong();
		}
		size += collSize;
		return (collSize > 0);
	}

	public void clear() {
		size = 0;
		data = new long[6];
	}

	public boolean contains(Object o) {
		return (indexOf(o) >= 0);
	}

	public boolean contains(long val) {
		return (indexOf(val) >= 0);
	}

	public boolean containsAll(Collection coll) {
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			if (contains(iter.next()) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean containsAll(LongArrayList coll) {
		LongIterator iter = coll.longIterator();
		while (iter.hasNext()) {
			if (contains(iter.nextLong()) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean containsAll(long[] array) {
		for (int i=array.length; i > 0; ) {
			if (contains(array[--i]) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean equals(Object o) {
		//For now do this way. Fix it later
		return this == o;
	}

	public Object get(int index) {
		return Long.valueOf(data[index]);
	}

	public long getValue(int index) {
		return data[index];
	}

	public int hashCode() {
		long hashCode = 1;
		for (int i=0; i<size; i++) {
			hashCode = 31*hashCode + data[i];
		}
		return (int) hashCode;
	}

	public int indexOf(Object o) {
		long val = ((Long) o).longValue();
		for (int i=0; i<size; i++) {
			if (data[i] == val) {
				return i;
			}
		}
		return -1;
	}

	public int indexOf(long val) {
		for (int i=0; i<size; i++) {
			if (data[i] == val) {
				return i;
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int lastIndexOf(Object o) {
		long val = ((Long) o).longValue();
		for (int i=size-1; i >= 0; i--) {
			if (data[i] == val) {
				return i;
			}
		}
		return -1;
	}

	public int lastIndexOf(long val) {
		for (int i=size-1; i >= 0; i--) {
			if (data[i] == val) {
				return i;
			}
		}
		return -1;
	}

	public ListIterator listIterator() {
		return new LongIterator(0);
	}

	public ListIterator listIterator(int index) {
		return new LongIterator(index);
	}

	public Object remove(int index) {
		long val = data[index];
		if (index < size - 1) {
			System.arraycopy(data, index+1, data, index, size - index);
		}
		size--;
		return Long.valueOf(val);
	}

	public long removeLong(int index) {
		long val = data[index];
		if (index < size - 1) {
			System.arraycopy(data, index+1, data, index, size - index);
		}
		size--;
		return val;
	}

	public boolean remove(Object object) {
		long val = ((Long) object).longValue();
		for (int i=0; i<size; i++) {
			if (data[i] == val) {
				if (i < size - 1) {
					System.arraycopy(data, i+1, data, i, size - i);
					size--;
					return true;
				}
			}
		}
		return false;
	}

	public boolean remove(long val) {
		for (int i=0; i<size; i++) {
			if (data[i] == val) {
				if (i < size - 1) {
					System.arraycopy(data, i+1, data, i, size - i);
					size--;
					return true;
				}
			}
		}
		return false;
	}



	public boolean removeAll(Collection coll) {
		boolean	removed = false;
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			if (remove(iter.next()) == true) {
				removed = true;
			}
		}
		return removed;
	}

	public boolean removeAll(LongArrayList coll) {
		boolean	removed = false;
		LongIterator iter = coll.longIterator();
		while (iter.hasNext()) {
			if (remove(iter.nextLong()) == true) {
				removed = true;
			}
		}
		return removed;
	}

	public boolean removeAll(long[] array) {
		boolean	removed = false;
		for (int i=0; i<array.length; i++) {
			if (remove(array[i]) == true) {
				removed = true;
			}
		}
		return removed;
	}


	public boolean retainAll(Collection coll) {
		return false;
	}

	public boolean retainAll(LongArrayList coll) {
		return false;
	}

	public boolean retainAll(long[] array) {
		return false;
	}


	public Object set(int index, Object o) {
		long oldVal = data[index];
		data[index] = ((Long) o).longValue();
		return Long.valueOf(oldVal);
	}
		
	public long set(int index, long val) {
		long oldVal = data[index];
		data[index] = val;
		return oldVal;
	}
		

	public int size() {
		return size;
	}

	public List subList(int fromIndex, int toIndex) {
		return new LongArrayList(data, fromIndex, toIndex, growFactor);
	}

	public Object[]	toArray() {
		Long[]	array = new Long[size];
		for (int i = 0; i < size; i++) {
                     array[i] = Long.valueOf(data[i]);
                 }
		return array;
	}

	public Object[]	toArray(Object[] arr) {
		Long[]	array = new Long[size];
		for (int i = 0; i < size; i++) {
                     array[i] = Long.valueOf(data[i]);
                 }
		return array;
	}


	protected void makeRoom(int space) {

		if (space + size >= data.length) {
			long[]	oldData = data;

			int newSize = size + space + ((int) (size * growFactor)) + 1;
			data = new long[newSize];
	
			System.arraycopy(oldData, 0, data, 0, oldData.length);
		}
	}


	public Iterator iterator() {
		return new LongIterator(0);
	}

	public LongIterator longIterator() {
		return new LongIterator(0);
	}

	public void print() {
		System.out.print("Data (size: " + size + "): ");
		for (int i=0; i<size; i++) {
			System.out.print(" " + data[i]);
		}
		System.out.println();
	}


	public static void main(String[] args) {
		LongArrayList list = new LongArrayList();

		for (int i=0; i<15; i+=2) {
			list.add(i);
		}

		for (int i=1; i<15; i+=2) {
			list.add(i);
		}
	}


	private class LongIterator
		implements ListIterator
	{
		int		startIndex;
		int		index = 0;

		LongIterator(int startAt) {
			startIndex = startAt;
		}

		public void add(Object o) {
			;
		}

		public boolean hasPrevious() {
			return index > startIndex;
		}

		public boolean hasNext() {
			return index < size;
		}

		public Object previous() {
			return Long.valueOf(data[--index]);
		}

		public int previousIndex() {
			return index;
		}

		public Object next() {
			return Long.valueOf(data[index++]);
		}

		public int nextIndex() {
			return index;
		}

		public long previousLong() {
			return data[--index];
		}

		public long nextLong() {
			return data[index++];
		}

		public void remove() {
			//;
		}

		public void set(Object o) {
			;
		}
	}

}
