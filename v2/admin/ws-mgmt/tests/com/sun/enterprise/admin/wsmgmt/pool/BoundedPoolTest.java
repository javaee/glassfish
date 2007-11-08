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
package com.sun.enterprise.admin.wsmgmt.pool;

import com.sun.enterprise.admin.wsmgmt.pool.impl.BoundedPool;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.Collection;
import java.util.Iterator;

public class BoundedPoolTest extends TestCase {
   
    public BoundedPoolTest(String name) {
        super(name);        
    }       

    public void testPool() {                        
        try {
            pool.resize(2);
            pool.put("key0", "val0");
            pool.put("key1", "val1");
            pool.put("key2", "val2");
            assertTrue(pool.size() == 2);
            System.out.println("Bounded Test Passed");

            Collection c = pool.values();
            for (Iterator itr=c.iterator(); itr.hasNext();) {
                Object val = itr.next();
                System.out.println(val);
            }
            pool.resize(10);
            pool.put("key3", "val3");
            pool.put("key4", "val4");
            assertTrue(pool.getMaxSize() == 10);
            System.out.println("Resize Test Passed");

            boolean keyTF = pool.containsKey("key4");
            assertTrue(keyTF == true);
            boolean valTF = pool.containsValue("val4");
            assertTrue(valTF == true);
            System.out.println("Contains Test Passed");

            Object val4 = pool.get("key4");
            assertTrue(val4.equals("val4"));
            System.out.println("Get Test Passed");

            Object val2 = pool.remove("key2");
            assertTrue(val2 != null);
            assertTrue(val2.equals("val2"));
            System.out.println("Remove Test Passed");

            pool.resize(1);

            pool.clear();
            assertTrue(pool.size() == 0);
            System.out.println("Clear Test Passed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setUp() {
        pool = new BoundedPool("Test");
    }
    private BoundedPool pool = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(BoundedPoolTest.class);
    }
}
