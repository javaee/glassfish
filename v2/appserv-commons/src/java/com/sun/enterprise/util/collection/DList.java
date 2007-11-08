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
 * Filename: DList.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/collection/DList.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:10 $
 */
 
package com.sun.enterprise.util.collection;

import java.util.*;

/**
 * A DList is an implementation of an unsynchronized doubly linked list. Unlike
 *	java.util.LinkedList, each node in the DList (DListNode) can be delinked 
 *	in constant time. However, to do this the application must have the reference
 *	of the node to be delinked. DLists are exrtemely usefull if nodes are 
 *	removed/inserted quite frequently. DList is used in the implementation of
 *	com.sun.enterprise.util.cache.AdaptiveCache.
 */
public class DList
	implements List, DListNodeFactory
{
    
    protected DListNode first;
    protected DListNode last;
    protected int size = 0;
    protected DListNodeFactory		nodeFactory;
    
    /**
     * Create a DList.
     */
    public DList() {
    	first = new DListNode(null);
    	last = new DListNode(null);
    	first.next = last;
    	last.prev = first;
    	first.prev = last.next = null;
    	this.nodeFactory = this;
    }
    
    /**
     * Create a DList.
     */
    public DList(DListNode firstNode, DListNode lastNode, DListNodeFactory factory) {
    	initDListWithNodes(firstNode, lastNode, factory);
    }
    	
    protected void initDListWithNodes(DListNode firstNode, DListNode lastNode, DListNodeFactory factory) {
    	first = firstNode;
    	last = lastNode;
    	first.next = last;
    	last.prev = first;
    	first.prev = last.next = null;
    	this.nodeFactory = (factory == null) ? this : factory;
    }
    
    
    /**
     * Create a DList.
     */
    public DList(DListNodeFactory nodeFactory) {
    	first = new DListNode(null);
    	last = new DListNode(null);
    	first.next = last;
    	last.prev = first;
    	first.prev = last.next = null;
    	this.nodeFactory = nodeFactory;
    }
    
    
    private DList(DListNode firstNode, DListNode lastNode, int size, DListNodeFactory nodeFactory) {
    	first = firstNode;
    	last = lastNode;
    	this.size = size;
    	this.nodeFactory = nodeFactory;
    }
    
    
    
    /**
     * Inserts the object at the specified index. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till the appropriate index is reached.
     * @return The DListNode holding this object or null if the index is invalid.
     */
    public void add(int index, Object object) {
    	insertAt(index, object);
    	size++;
    }
    
    /**
     * Inserts the object at the end of the list.
     * @return The DListNode holding this object or null if the index is invalid.
     */
    public boolean add(Object object) {
    	last.insertBefore(nodeFactory.createDListNode(object));
    	size++;
    	return true;
    }
    
    /**
     * Inserts the object at the specified index. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till the appropriate index is reached.
     * @return The DListNode holding this object or null if the index is invalid.
     */
    public boolean addAll(Collection collection) {
    	Iterator iter = collection.iterator();
    	boolean added = false;
    	while (iter.hasNext()) {
    		add(iter.next());
    		added = true;
    	}
    	
    	size += collection.size();
    	return added;
    }
    
    /**
     * Inserts the object at the specified index. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till the appropriate index is reached.
     * @return The DListNode holding this object or null if the index is invalid.
     */
    public boolean addAll(int index, Collection collection) {
    	DListNode node = getDListNodeAt(index);
    	Iterator iter = collection.iterator();
    	boolean added = iter.hasNext();
    	DListNode head = new DListNode(null);
    	DListNode last = head;
    	while (iter.hasNext()) {
    		last.insertAfter(nodeFactory.createDListNode(iter.next()));
    		last = last.next;
    	}
    	if (head != last) {
    		node.prev.next = head.next;
    		node.prev = last;
    		
    		head.next.prev = node;
    		last.next = node;
    	}
    	
    	size += collection.size();
    	return added;
    }
    
    public void clear() {
    	first.next = last;
    	last.prev = first;
    	size = 0;
    }
    
    public boolean contains(Object o) {
    	return (indexOf(o) != -1);
    }
    
    public boolean containsAll(Collection collection) {
    	Iterator iter = collection.iterator();
    	while (iter.hasNext()) {
    		Object o = iter.next();
    		if (indexOf(o) == -1) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean equals(Object o) {
    	if (o instanceof List) {
    		List list = (List) o;
    		if (list.size() != size()) {
    			return false;
    		} 
    		
    		Object myObj = null, otherObj = null;
    		DListNode node = first;
    		for (int i=0; i<size; i++) {
    			myObj = node.next.object;
    			otherObj = list.get(i);
    			if (! myObj.equals(otherObj)) {
    				return false;
    			}
    			node = node.next;
    		}
    		return true;
    	}
    	return false;
    }
    
    public int hashCode() {
		int hashCode = 1;
    	DListNode node = first;
    	Object myObj = null;
    	for (int i=0; i<size; i++) {
    		myObj = node.next.object;
			hashCode = 31*hashCode + (myObj==null ? 0 : myObj.hashCode());
			node = node.next;
		}
		return hashCode;
    }
    
    /**
     * Return the object at the specified index
     * @param The index between 0 and size()-1
     */
    public Object get(int index) {
    	DListNode node = getDListNodeAt(index);
    	return (node == null) ? null : node.object;
    }
    
    
    /**
     * Obtains the index at which this object appears in the list. The method relies on the 
     *	equals() method to identify objects in the list. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till a match is found.
     * @return The (0 based) index at which this object appears in the list -1 if not found.
     */
    public int indexOf(Object o) {
    	int index = 0;
    	for (DListNode node = first.next; node != last; node = node.next) {
    		if (node.object.equals(o)) {
    			return index;
    		}
    		index++;
    	}
    	return -1;
    }
    
	/**
	 * Returns true if this list contains no elements.
	 * @return true if this list contains no elements false otherwise.
	 */
	public boolean isEmpty() {
		return (this.size > 0);
	}
	
    /**
     * Returns an iterator for iterating the entries in the list. Each object returned
     *	by the iterator.next() is the actual object added to the list.
     * @return An iterator.
     */
	public Iterator iterator() {
		return new DListIterator(first, last, false, 0);
	}
	
    /**
     * Returns the index in this list of the last occurrence of the specified element, 
     *	or -1 if this list does not contain this element. More formally, returns the highest
     *	index i such that (o==null ? get(i)==null : o.equals(get(i))), or -1 if there is no such index.
     * @param element to search for.
     * @return the index in this list of the last occurrence of the specified element, 
     *	or -1 if this list does not contain this element.
     */
	public int lastIndexOf(Object obj) {
    	int index = size - 1;
    	for (DListNode node = last.prev; node != first; node = node.prev) {
    		if (node.object.equals(obj)) {
    			return index;
    		}
    		index--;
    	}
    	return -1;
	}
	
    /**
     * Returns a list iterator of the elements in this list (in proper sequence).
     *	retrieve the object.
     * @return A ListIterator.
     */
	public ListIterator listIterator() {
		return new DListIterator(first, last, true, 0);
	}
	
    /**
     * Returns a list iterator of the elements in this list (in proper sequence), starting 
     *	at the specified position in this list. The specified index indicates the first element
     *	that would be returned by an initial call to the next method. An initial call to the 
     *	previous method would return the element with the specified index minus one.
     * @param index of first element to be returned from the list iterator (by a call to the next method).
     * @return a list iterator of the elements in this list (in proper sequence), starting 
    	at the specified position in this list.
     */
	public ListIterator listIterator(int index) {
		return new DListIterator(first, last, true, index);
	}
	
	/**
	 * Removes the element at the specified position in this list (optional operation). 
	 *	Shifts any subsequent elements to the left (subtracts one from their indices).
     * @return the element that was removed from the list.	 
     */
	public Object remove(int index) {
		DListNode node = getDListNodeAt(index);
		node.delink();
		size--;
		Object object = node.object;
		destroyDListNode(node);
		return object;
	}

	/**
	 * Removes the first occurrence in this list of the specified element (optional operation). 
	 *	If this list does not contain the element, it is unchanged. More formally,
     *	removes the element with the lowest index i such that 
     *	(o==null ? get(i)==null : o.equals(get(i))) (if such an element exists).
     * @return true if this list contained the specified element.
     */
	public boolean remove(Object object) {
		DListNode node = getDListNode(object);
		if (node == null) {
			return false;
		} else {
			node.delink();
			destroyDListNode(node);
			size--;
			return true;
		}
	}

	public boolean removeAll(Collection collection) {
		Iterator iter = collection.iterator();
		boolean removed = false;
		while (iter.hasNext()) {
			if (remove(iter.next())) {
				size--;
				removed = true;
			}
		}
		return removed;
	}

	public boolean retainAll(Collection collection) {
		
		boolean removed = false;
		DListNode node = first;
		DListNode dnode = null;
		while (node.next != last) {
			dnode = node.next;
			if (collection.contains(dnode.object)) {
				dnode.delink();
				destroyDListNode(dnode);
				size--;
				removed = true;
			} else {
				node = node.next;
			}
		}
		return removed;
	}

    /**
     * Return the object at the specified index
     * @param The index between 0 and size()-1
     */
    public Object set(int index, Object object) {
    	DListNode node = getDListNodeAt(index);
    	Object oldObject =  (node == null) ? null : node.object;
    	node.object = object;
    	return oldObject;
    }
    
    
    /**
     * Inserts the object at the specified index. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till the appropriate index is reached.
     * @return The DListNode holding this object or null if the index is invalid.
     */
    public DListNode insertAt(int index, Object object) {
    	if ((index < 0) || (index >= size)) {
    		return null;
    	}
    	int mid = size >> 1;	//Divide by 2!!
   		DListNode node = null;
    	if (index <= mid) {
    		node = first.next;
    		for (int i=0; i<index ; i++) {
    			node = node.next;
    		}
    	} else {
    		index = size - index - 1;
    		node = last.prev;
    		for (int i=0; i<index ; i++) {
    			node = node.prev;
    		}
    	}
    	DListNode newNode = nodeFactory.createDListNode(object);
   		node.insertBefore(newNode);
   		size++;
    	return newNode;
    }
    
    /**
     * Obtain the size of the list.
     * @return The number of entries in the list.
     */
    public int size() {
    	return size;
    }
    
    
    /**
     * Returns a view of the portion of this list between the specified fromIndex, 
     *	inclusive, and toIndex, exclusive. (If fromIndex and toIndex are equal, the
     *	returned list is empty.) The returned list is backed by this list, so changes 
     *	in the returned list are reflected in this list, and vice-versa. The returned list supports
     *	all of the optional list operations supported by this list.
     * <p> This method eliminates the need for explicit range operations (of the sort 
     *	that commonly exist for arrays). Any operation that expects a list can be used as a
     *	range operation by passing a subList view instead of a whole list. For example, 
     *	the following idiom removes a range of elements from a list: 
     * <p> list.subList(from, to).clear();
     * <p>Similar idioms may be constructed for indexOf and lastIndexOf, and all of the 
     *	algorithms in the Collections class can be applied to a subList.
     * <p>The semantics of this list returned by this method become undefined if the backing 
     *	list (i.e., this list) is structurally modified in any way other than via the
     *	returned list. (Structural modifications are those that change the size of this 
     *	list, or otherwise perturb it in such a fashion that iterations in progress may yield
     *	incorrect results.)
     * @param low endpoint (inclusive) of the subList.
     * @param high endpoint (exclusive) of the subList.
     */
    public List subList(int fromIndex, int toIndex) {
    	System.out.println("subList(" + fromIndex + ", " + toIndex + ")");
    	DListNode startNode = getDListNodeAt(fromIndex);
    	System.out.println("nodeAt(" + fromIndex + "): " + startNode.object);
    	DListNode toNode = getDListNodeAt(toIndex);
    	System.out.println("nodeAt(" + toIndex + "): " + toNode.object);
    	return new DList(startNode.prev, toNode, toIndex - fromIndex, nodeFactory);
    }
    
    
    
    /**
	 * Returns an array containing all of the elements in this collection. If the collection 
	 *	makes any guarantees as to what order its elements are returned by its iterator, this 
	 *	method must return the elements in the same order.
	 * <p> The returned array will be "safe" in that no references to it are maintained by this 
	 *	collection. (In other words, this method must allocate a new array even if this
     *	collection is backed by an array). The caller is thus free to modify the returned array.     
     * @return an array containing all of the elements in this collection.
     */
	public Object[] toArray() {
		Object[]	array = new Object[size];
		int index = 0;
    	for (DListNode node = first.next; node != last; node = node.next) {
    		array[index++] = node.object;
    	}
    	return array;
	}
	
    /**
	 * Returns an array containing all of the elements in this collection whose runtime type 
	 *	is that of the specified array. If the collection fits in the specified array, it is
	 *	returned therein. Otherwise, a new array is allocated with the runtime type of the 
	 *	specified array and the size of this collection. 
	 * <p> If this collection fits in the specified array with room to spare (i.e., the 
	 *	array has more elements than this collection), the element in the array immediately 
	 *	following the end of the collection is set to null. This is useful in determining 
	 *	the length of this collection only if the caller knows that this collection does 
	 *	not contain any null elements.)
	 * <p> If this collection makes any guarantees as to what order its elements are returned 
	 *	by its iterator, this method must return the elements in the same order.
	 * <p>Like the toArray method, this method acts as bridge between array-based and 
	 *	collection-based APIs. Further, this method allows precise control over the runtime 
	 *	type of the output array, and may, under certain circumstances, be used to save allocation costs.
	 * <p>Suppose l is a List known to contain only strings. The following code can be 
	 *	used to dump the list into a newly allocated array of String: 
	 * <p>String[] x = (String[]) v.toArray(new String[0]);
	 * @return an array containing all of the elements in this collection.
     */
	public Object[] toArray(Object[] array) {
		
		if (array.length < size) {
			array = (Object[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		
		int index = 0;
    	for (DListNode node = first.next; node != last; node = node.next) {
    		array[index++] = node.object;
    	}

        if (array.length > size) {
            array[size] = null;
        }
            
        return array;

	}
	

	/*******************************************************************************************************/
	/*******************************************************************************************************/
	/*******************************************************************************************************/
	/*******************************************************************************************************/
	/*******************************************************************************************************/

	public DListNode createDListNode(Object object) {
		return new DListNode(object);
	}
	
	public void destroyDListNode(DListNode node) {
	}
	
	public DListNodeFactory getDListNodeFactory() {
		return this.nodeFactory;
	}
	
	public void setDListNodeFactory(DListNodeFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}
	
    /**
     * Add a DListNode as the first node in the list.
     * @param node The node to be added.
     */
    public void addAsFirstNode(DListNode node) {
    	DListNode fNode = first.next;
    	node.next = fNode;
    	node.prev = first;
    	fNode.prev = first.next = node;
    	size++;
    }

    /**
     * Add an object as the first node in the list.
     * @param object The object to be added.
     * @return The DListNode enclosing the object. This
     *	DListNode object can later be used to delink
     *	the object from the list in constant time.
     */
    public DListNode addAsFirstObject(Object object) {
    	DListNode node = nodeFactory.createDListNode(object);
    	addAsFirstNode(node);
    	return node;
    }
    
    /**
     * Add a DListNode as the last node in the list.
     * @param node The node to be added.
     */
    public void addAsLastNode(DListNode node) {
    	DListNode lNode = last.prev;
    	node.next = last;
    	node.prev = lNode;
    	lNode.next = last.prev = node;
    	size++;
    }
    
    /**
     * Add an object as the last node in the list.
     * @param object The object to be added.
     * @return The DListNode enclosing the object. This
     *	DListNode object can later be used to delink
     *	the object from the list in constant time.
     */
    public DListNode addAsLastObject(Object obj) {
    	DListNode node= nodeFactory.createDListNode(obj);
    	addAsLastNode(node);
    	return node;
    }
    
    /**
     * Removes the first DListNode from the list.
     * @return The DListNode at the head of the list or null if the list is empty.
     */
    public DListNode delinkFirstNode() {
    	if (size > 0) {
    		DListNode node = first.next;
    		node.delink();
    		size--;
    		return node;
    	}
    	return null;
    }
    
    /**
     * Removes the first object from the list.
     * @return The object at the head of the list or null if the list is empty.
     */
    public Object removeFirstObject() {
    	DListNode node = delinkFirstNode();
    	if (node == null) {
    		return null;
    	} else {
    		Object object = node.object;
    		destroyDListNode(node);
    		return object;
    	}
    }
    
    
    /**
     * Removes the last DListNode from the list.
     * @return The DListNode at the tail of the list or null if the list is empty.
     */
    public DListNode delinkLastNode() {
    	if (size > 0) {
    		DListNode node = last.prev;
    		node.delink();
    		size--;
    		return node;
    	}
    	return null;
    }
    
    /**
     * Removes the last object from the list.
     * @return The object at the tail of the list or null if the list is empty.
     */
    public Object removeLastObject() {
    	DListNode node = delinkLastNode();
    	if (node == null) {
    		return null;
    	} else {
    		Object object = node.object;
    		destroyDListNode(node);
    		return object;
    	}
    }
        

    /**
     * Obtains the DListNode that contains this object. The method relies on the 
     *	equals() method to identify objects in the list. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till a match is found.
     * @return The DListNode holding this object or null if the object is not in the list.
     */
    public DListNode getDListNode(Object o) {
    	for (DListNode node = first.next; node != last; node = node.next) {
    		if (node.object.equals(o)) {
    			return node;
    		}
    	}
    	return null;
    }
    
    public void delink(DListNode node) {
    	node.delink();
    	size--;
    }
    
    /**
     * Obtains the DListNode at the specified index. Note that this
     *	method is an O(n) operation as it has to iterate through the nodes
     *	in the list till a match is found.
     * @return The DListNode at the specifed index or null if the index is invalid.
     */
    public DListNode getDListNodeAt(int index) {
    	if ((index < 0) || (index >= size)) {
    		throw new ArrayIndexOutOfBoundsException("DList size: " + size + "; index: " + index);
    	}
    	int mid = size >> 1;	//Divide by 2!!
   		DListNode node = null;
    	if (index <= mid) {
    		node = first.next;
    		for (int i=0; i<index ; i++) {
    			node = node.next;
    		}
    	} else {
    		index = size - index - 1;
    		node = last.prev;
    		for (int i=0; i<index ; i++) {
    			node = node.prev;
    		}
    	}
    	return node;
    }
    
    public DListNode getFirstDListNode() {
    	return (size == 0) ? null : first.next;
    }
    
    public DListNode getLastDListNode() {
    	return (size == 0) ? null : last.prev;
    }
    
    public DListNode getNextNode(DListNode node) {
    	DListNode nextNode = node.next;
    	return (nextNode == last) ? null : nextNode;
    }
    
    public DListNode getPreviousNode(DListNode node) {
    	DListNode prevNode = node.prev;
    	return (prevNode == first) ? null : prevNode;
    }
    
    /**
     * Returns an iterator for iterating the entries in the list. Each object returned
     *	by the iterator.next() is an instance of DListNode. Use DListNode.object to
     *	retrieve the object.
     * @return An iterator.
     */
	public Iterator nodeIterator() {
		return new DListIterator( first, last, true, 0);
	}
	

    
    
    
    
	/************************************************************************/
	/* ************** AN INNER CLASS FOR SUPPORTING ITERATOR ************** */
	
	private class DListIterator
		implements java.util.ListIterator
	{
		DListNode firstNode;
		DListNode lastNode;
		DListNode currentNode;
		boolean toReturnNode;
		int currentIndex = -1;
		
		DListIterator(DListNode firstNode, DListNode lastNode, boolean toReturnNode, int skip) {
			this.firstNode =  this.currentNode = firstNode;
			this.lastNode = lastNode;
			this.toReturnNode = toReturnNode;
			
			for (int i=0; i<skip; i++) {
				currentNode = currentNode.next;
			}
			this.currentIndex = skip;
			
		}
		
		DListIterator(int startIndex, int endIndex, boolean toReturnNode, int skip) {
			this.firstNode =  this.currentNode = firstNode;
			this.lastNode = getDListNodeAt(endIndex);
			this.toReturnNode = toReturnNode;
			
			for (int i=0; i<skip; i++) {
				currentNode = currentNode.next;
			}
			this.currentIndex = skip;
			
		}
		
		public void add(Object obj) {
			currentNode.insertAfter(nodeFactory.createDListNode(obj));	
		}
		
		public boolean hasNext() {
			return (currentNode.next != lastNode);
		}
		
		public boolean hasPrevious() {
			return (currentNode != firstNode);
		}
		
		public Object next() {
			if (currentNode.next == lastNode) {
				throw new java.util.NoSuchElementException("No next after this element");
			}
			currentNode = currentNode.next;
			currentIndex++;
			return (toReturnNode ? currentNode : currentNode.object);
		}
		
		public int nextIndex() {
			return (currentIndex+1);
		}
		
		public Object previous() {
			if (currentNode == firstNode) {
				throw new java.util.NoSuchElementException("No previous before this element");
			}
			DListNode node = currentNode;
			currentNode = currentNode.prev;
			currentIndex--;
			return (toReturnNode ? node : node.object);
		}
		
		public int previousIndex() {
			return (currentIndex-1);
		}
		
		public void remove() {
			throw new UnsupportedOperationException("list.remove() not supported by DList iterator....");
		}
		
		public void set(Object o) {
			throw new UnsupportedOperationException("list.remove() not supported by DList iterator....");
		}
	}


}