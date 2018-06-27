/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.connector.cciblackbox;

/**
 * This implementation class represents an ordered collection of record elements
 *
 * @author Sheetal Vartak
 */

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class CciIndexedRecord implements javax.resource.cci.IndexedRecord {

  private String recordName;

  private String description;

  private Vector indexedRecord;

  public CciIndexedRecord() {
    indexedRecord = new Vector();
  }

  public CciIndexedRecord(String name) {
    indexedRecord = new Vector();
    recordName = name;
  }

  public String getRecordName() {
    return recordName;
  }

  public void setRecordName(String name) {
    recordName = name;
  }

  public String getRecordShortDescription() {
    return description;
  }

  public void setRecordShortDescription(String description) {
    description = description;
  }

  public boolean equals(Object other) {
    return this.equals(other);
  }

  public int hashCode() {
    String result = "" + recordName;
    return result.hashCode();
  }

  public Object clone() throws CloneNotSupportedException {
    return this.clone();
  }

  //java.util.List methods

  public void add(int index, Object element) {
    indexedRecord.add(index, element);
  }

  public boolean add(Object o) {
    return indexedRecord.add(o);
  }

  public boolean addAll(Collection c) {
    return indexedRecord.addAll(c);
  }

  public boolean addAll(int index, Collection c) {
    return indexedRecord.addAll(index, c);
  }

  public void addElement(Object o) {
    indexedRecord.addElement(o);
  }

  public int capacity() {
    return indexedRecord.capacity();
  }

  public void clear() {
    indexedRecord.clear();
  }

  public boolean contains(Object elem) {
    return indexedRecord.contains(elem);
  }

  public boolean containsAll(Collection c) {
    return indexedRecord.containsAll(c);
  }

  public Object get(int index) {
    return (Object) indexedRecord.get(index);
  }

  public int indexOf(Object elem) {
    return indexedRecord.indexOf(elem);
  }

  public int indexOf(Object elem, int index) {
    return indexedRecord.indexOf(elem, index);
  }

  public boolean isEmpty() {
    return indexedRecord.isEmpty();
  }

  public Iterator iterator() {
    return indexedRecord.iterator();
  }

  public ListIterator listIterator() {
    return indexedRecord.listIterator();
  }

  public ListIterator listIterator(int index) {
    return indexedRecord.listIterator(index);
  }

  public Object lastElement() {
    return indexedRecord.lastElement();
  }

  public int lastIndexOf(Object elem) {
    return indexedRecord.lastIndexOf(elem);
  }

  public int lastIndexOf(Object elem, int index) {
    return indexedRecord.lastIndexOf(elem, index);
  }

  public Object remove(int index) {
    return indexedRecord.remove(index);
  }

  public boolean remove(Object o) {
    return indexedRecord.remove(o);
  }

  public boolean removeAll(Collection c) {
    return indexedRecord.remove(c);
  }

  public boolean retainAll(Collection c) {
    return indexedRecord.retainAll(c);
  }

  public Object set(int index, Object element) {
    return indexedRecord.set(index, element);
  }

  public int size() {
    return indexedRecord.size();
  }

  public List subList(int fromIndex, int toIndex) {
    return indexedRecord.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    return indexedRecord.toArray();
  }

  public Object[] toArray(Object[] a) {
    return indexedRecord.toArray(a);
  }

  public String toString() {
    return indexedRecord.toString();
  }

  public void trimToSize() {
    indexedRecord.trimToSize();
  }

}
