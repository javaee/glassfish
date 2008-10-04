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
package org.glassfish.admin.amx.test.utiltest;


import com.sun.appserv.management.util.misc.CircularList;


public class CircularListTest
        extends junit.framework.TestCase {
    public CircularListTest() {
    }

    public void
    testCreate() {
        new CircularList<Object>(Object.class, 10);
    }


    public void
    testCreateIllegal() {
        try {
            new CircularList<Object>(Object.class, 0);
            assert (false);
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void
    testSizeAndCapacity() {
        final CircularList<Object> list = new CircularList<Object>(Object.class, 10);

        assertEquals(0, list.size());
        assertEquals(10, list.capacity());
    }

    public void
    testClear() {
        final CircularList<Object> list = new CircularList<Object>(Object.class, 10);

        assertEquals(0, list.size());

        list.add("hello");
        assertEquals(1, list.size());
        list.clear();
        assertEquals(0, list.size());

        assertEquals(10, list.capacity());
    }

    public void
    testAddUntilFull() {
        final int capacity = 10;
        final CircularList<Integer> list = new CircularList<Integer>(Integer.class, capacity);
        assertEquals(capacity, list.capacity());

        for (int i = 0; i < capacity; ++i) {
            list.add(new Integer(i));
            assert (list.size() == i + 1);
            assert (((Integer) list.get(i)).intValue() == i);
        }
        assert (list.capacity() == capacity);
    }

    public void
    testAddPastFull() {
        final int capacity = 10;
        final int count = capacity * 10 + 1;
        final CircularList<Integer> list = new CircularList<Integer>(Integer.class, capacity);

        for (int i = 0; i < count; ++i) {
            list.add(new Integer(i));

            Integer value = (Integer) list.get(list.size() - 1);
            assertEquals(i, value.intValue());

            if (i >= capacity) {
                value = (Integer) list.get(0);
                assertEquals(1 + (i - capacity), value.intValue());
            }
        }
        assert (list.capacity() == capacity);
    }

    public void
    testRemoveFirstLast() {
        final int capacity = 3;
        final CircularList<String> list = new CircularList<String>(String.class, capacity);

        list.add("hello");
        list.add("xxx");
        list.add("there");

        assertEquals("hello", list.removeFirst());
        assertEquals("there", list.removeLast());
        assertEquals(1, list.size());
    }


    public void
    testRemoveFirst() {
        final int capacity = 100;
        final CircularList<Integer> list = new CircularList<Integer>(Integer.class, capacity);

        for (int i = 0; i < capacity; ++i) {
            list.add(new Integer(i));
        }

        for (int i = 0; i < capacity; ++i) {
            final Integer value = (Integer) list.removeFirst();
            assertEquals(i, value.intValue());
        }
    }

    public void
    testSet() {
        final int capacity = 100;
        final CircularList<Integer> list = new CircularList<Integer>(Integer.class, capacity);

        for (int i = 0; i < capacity; ++i) {
            list.add(new Integer(i));
        }

        for (int i = 0; i < capacity; ++i) {
            final Integer value = (Integer) list.get(i);
            list.set(i, value);
            assertEquals(value, list.get(i));
        }
    }

    public void
    testEquals() {
        final int capacity = 2;
        final CircularList<String> list1 = new CircularList<String>(String.class, capacity);
        final CircularList<String> list2 = new CircularList<String>(String.class, capacity);
        assert (list1.equals(list2));

        list1.add("hello");
        list1.add("there");
        list2.add("hello");
        list2.add("there");

        assert (list1.equals(list2));
    }
}






