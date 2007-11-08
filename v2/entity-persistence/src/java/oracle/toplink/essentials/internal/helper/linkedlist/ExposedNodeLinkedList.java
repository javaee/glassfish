/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.internal.helper.linkedlist;

import java.util.*;
import oracle.toplink.essentials.exceptions.ValidationException;

/**
 * INTERNAL:
 * A custom implementation of a linked list.  This list exposes the linked nodes
 * directly to the developer.  It allows nodes to be referenced in code for quick
 * list manipulation (ie reshuffle, remove, or queueing)
 * It is specifically used in the TopLink cache write lock mechanism in order
 * to allow quick removal of objects from the list while still providing the getFirst()
 * addLast() functionality of a queue.  The alternative java classes LinkedList, LinkedHashMap
 * do not provide both functional requirements.
 * @author Gordon Yorke
 * @since 10.0.3
 * @see oracle.toplink.essentials.internal.helper.linkedlist.LinkedNode
 */
public class ExposedNodeLinkedList implements List {
    private transient LinkedNode header;
    private transient int size;

    /**
     * Constructs an empty list.
     */
    public ExposedNodeLinkedList() {
        this.size = 0;
        this.header = new LinkedNode(null, null, null);
        header.next = header;
        header.previous = header;
    }

    // Bunch of List methods not currently implemented.
    public Object[] toArray(Object[] array) {
        throw ValidationException.operationNotSupported("toArray");
    }

    public Object[] toArray() {
        throw ValidationException.operationNotSupported("toArray");
    }

    public Object set(int index, Object value) {
        throw ValidationException.operationNotSupported("set");
    }

    public ListIterator listIterator(int index) {
        throw ValidationException.operationNotSupported("listIterator");
    }

    public ListIterator listIterator() {
        throw ValidationException.operationNotSupported("listIterator");
    }

    public Iterator iterator() {
        throw ValidationException.operationNotSupported("iterator");
    }

    public List subList(int start, int end) {
        throw ValidationException.operationNotSupported("subList");
    }

    public boolean retainAll(Collection collection) {
        throw ValidationException.operationNotSupported("retainAll");
    }

    public boolean removeAll(Collection collection) {
        throw ValidationException.operationNotSupported("removeAll");
    }

    public boolean containsAll(Collection collection) {
        throw ValidationException.operationNotSupported("containsAll");
    }

    public boolean addAll(Collection collection) {
        throw ValidationException.operationNotSupported("addAll");
    }

    public boolean addAll(int index, Collection collection) {
        throw ValidationException.operationNotSupported("addAll");
    }

    public boolean remove(Object object) {
        throw ValidationException.operationNotSupported("remove");
    }

    public boolean add(Object object) {
        addLast(object);
        return true;
    }

    public int lastIndexOf(Object object) {
        throw ValidationException.operationNotSupported("lastIndexOf");
    }

    public void add(int index, Object object) {
        throw ValidationException.operationNotSupported("add");
    }

    public Object remove(int index) {
        throw ValidationException.operationNotSupported("remove");
    }

    public Object get(int index) {
        throw ValidationException.operationNotSupported("get");
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the first contents in this list.
     *
     * @return the first contents in this list. Null if this list is empty.
     */
    public Object getFirst() {
        if (size == 0) {
            return null;
        }
        return header.next.contents;
    }

    /**
     * Returns the last contents in this list.
     *
     * @return the last contents in this list. Null if this list is empty.
     */
    public Object getLast() {
        if (size == 0) {
            return null;
        }
        return header.previous.contents;
    }

    /**
     * Removes and returns the first contents from this list.
     *
     * @return the first contents from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    public Object removeFirst() {
        if (size != 0) {
            Object first = header.next.contents;
            remove(header.next);
            return first;
        }
        return null;
    }

    /**
       * Removes and returns the last contents from this list.
     *
     * @return the last contents from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    public Object removeLast() {
        if (size != 0) {
            Object last = header.previous.contents;
            remove(header.previous);
            return last;
        }
        return null;
    }

    /**
     * Inserts the given contents at the beginning of this list.
     *
     * @param o the contents to be inserted at the beginning of this list.
     */
    public LinkedNode addFirst(Object o) {
        return addAfter(o, header);
    }

    /**
     * Appends the given contents to the end of this list.  (Identical in
     * function to the <tt>add</tt> method; included only for consistency.)
     *
     * @param o the contents to be inserted at the end of this list.
     */
    public LinkedNode addLast(Object o) {
        return addAfter(o, header.previous);
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified contents.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one contents <tt>e</tt> such that <tt>(o==null ? e==null
     * : o.equals(e))</tt>.
     *
     * @param o contents whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified contents.
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns the number of contentss in this list.
     *
     * @return the number of contentss in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Removes all of the contentss from this list.
     */
    public void clear() {
        header.next = header;
        header.previous = header;
        size = 0;
    }

    /**
     * Returns the index in this list of the first occurrence of the
     * specified contents, or -1 if the List does not contain this
     * contents.  More formally, returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o contents to search for.
     * @return the index in this list of the first occurrence of the
     *            specified contents, or -1 if the list does not contain this
     *            contents.
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (LinkedNode n = header.next; n != header; n = n.next) {
                if (n.contents == null) {
                    return index;
                }
                index++;
            }
        } else {
            for (LinkedNode n = header.next; n != header; n = n.next) {
                if (o.equals(n.contents)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    private LinkedNode addAfter(Object o, LinkedNode n) {
        LinkedNode newNode = new LinkedNode(o, n.next, n);
        newNode.previous.next = newNode;
        newNode.next.previous = newNode;
        size++;
        return newNode;
    }

    /**
     * Allows a node to be efficiently removed.
     */
    public void remove(LinkedNode n) {
        if (n == header) {
            throw new NoSuchElementException();
        } else if ((n.previous == null) || (n.next == null)) {
            // Handles case of node having already been removed.
            return;
        }
        n.previous.next = n.next;
        n.next.previous = n.previous;
        // Also clear the nodes references to know that it has been removed.
        n.previous = null;
        n.next = null;
        n.contents = null;
        size--;
    }

    /**
     * Allows a node to be efficiently moved first.
     */
    public void moveFirst(LinkedNode node) {
        if (node == header) {
            throw new NoSuchElementException();
        } else if ((node.previous == null) || (node.next == null)) {
            // Handles case of node having already been removed.
            size++;
        } else {
            node.previous.next = node.next;
            node.next.previous = node.previous;
        }
        node.next = header.next;
        node.previous = header;
        header.next = node;
        node.next.previous = node;
    }
}
