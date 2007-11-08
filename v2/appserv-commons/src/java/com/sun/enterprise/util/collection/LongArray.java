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

public class LongArray
{

	long[]			data;
	int				size;
	double			growFactor;

	public LongArray() {
		this(6, 0.5);
	}

	public LongArray(int initialCapacity) {
		this(initialCapacity, 0.5);
	}

	public LongArray(int initialCapacity, double growFactor) {
		data = new long[initialCapacity];
		this.growFactor = growFactor;
	}

	public void add(int index, long val) {
		if (size == data.length) {
			makeRoom(1);
		}

		System.arraycopy(data, index, data, index+1, size - index);
		data[index] = val;
		size++;

	}

	public boolean add(long val) {
		if (size == data.length) {
			makeRoom(1);
		}

		data[size++] = val;
		return true;
	}

	public boolean addAll(LongArray coll) {
		int collSize = coll.size();
		makeRoom(collSize);
		LongIterator longIter = coll.longIterator();
		while (longIter.hasNext()) {
			data[size++] = longIter.nextLong();
		}
		return (collSize > 0);
	}

	public boolean addAll(int index, LongArray coll) {
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

	public boolean contains(long val) {
		return (indexOf(val) >= 0);
	}

	public boolean containsAll(LongArray coll) {
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

	public long get(int index) {
		return data[index];
	}

	public int hashCode() {
		long hashCode = 1;
		for (int i=0; i<size; i++) {
			hashCode = 31*hashCode + data[i];
		}
		return (int) hashCode;
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

	public long remove(int index) {
		long val = data[index];
		if (index < size - 1) {
			System.arraycopy(data, index+1, data, index, size - index);
		}
		size--;
		return val;
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

	public boolean removeAll(LongArray coll) {
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


	public boolean retainAll(LongArray coll) {
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

	public long[]	toArray() {
		return data;
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

	public void printStat() {
		System.out.println("Size: " + size);
		System.out.println("Length: " + data.length);
	}


	public static void main(String[] args) {
		int count = 1000000;

		ArrayList al = new ArrayList(count);
		LongArray la = new LongArray(count, 0.1);
		LongArrayList lal = new LongArrayList(count, 0.1);

		long t1, t2;

		t1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
                     al.add(Long.valueOf(i));
                 }
		t2 = System.currentTimeMillis();
		System.out.println("ArrayList took: " + ((t2 - t1) / 1000.0) + " sec.");
		al.clear();

		t1 = System.currentTimeMillis();
		for (int i=0; i<count; i++) {
			la.add(i);
		}
		t2 = System.currentTimeMillis();
		System.out.println("LongArray took: " + ((t2 - t1) / 1000.0) + " sec.");
		la.printStat();
		la.clear();

		t1 = System.currentTimeMillis();
		for (int i=0; i<count; i++) {
			lal.add(i);
		}
		t2 = System.currentTimeMillis();
		System.out.println("LongArrayList took: " + ((t2 - t1) / 1000.0) + " sec.");
		lal.clear();

		for (int i=0; i<count; i++) {
			la.add(i);
		}

		for (int i=0; i<10; i++) {
			for (int j=0; j<count; j++) {
				if (la.get(j) != j) {
					System.out.println("data["+j+"]: " + la.get(j));
				}
			}
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
