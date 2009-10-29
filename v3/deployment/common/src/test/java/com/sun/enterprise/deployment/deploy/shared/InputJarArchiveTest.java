/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.deploy.shared;

import com.sun.enterprise.deployment.deploy.shared.InputJarArchive.CollectionWrappedEnumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class InputJarArchiveTest {

    public InputJarArchiveTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getArchiveSize method, of class InputJarArchive.
     */
    @Test
    public void testCollectionWrappedEnumerationSimple() {
        System.out.println("collection wrapped enumeration - simple iterator test");
        final Enumeration<String> e = testEnum();

        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<String>(
                new CollectionWrappedEnumeration.EnumerationFactory() {

            @Override
            public Enumeration enumeration() {
                return e;
            }

        });

        ArrayList<String> answer = new ArrayList<String>(cwe);
        assertEquals("resulting array list != original", testStringsAsArrayList(), answer);
    }

    @Test
    public void testCollectionWrappedEnumerationInitialSize() {
        System.out.println("collection wrapped enumeration - initial size() call");
        final Enumeration<String> e = testEnum();

        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<String>(
                new CollectionWrappedEnumeration.EnumerationFactory() {

            @Override
            public Enumeration enumeration() {
                return e;
            }

        });

        int size = cwe.size();
        ArrayList<String> answer = new ArrayList<String>(cwe);
        assertEquals("array list of size " + size + " after initial size != original", testStringsAsArrayList(), answer);
    }

    @Test
    public void testCollectionWrappedEnumerationMiddleSize() {
        System.out.println("collection wrapped enumeration - middle size() call");

        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<String>(
                new CollectionWrappedEnumeration.EnumerationFactory() {

            @Override
            public Enumeration enumeration() {
                return testEnum();
            }

        });

        ArrayList<String> answer = new ArrayList<String>();
        Iterator<String> it = cwe.iterator();

        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        int size = cwe.size();
        answer.add(it.next());
        answer.add(it.next());

        assertEquals("array list of size " + size + " after middle size call != original", testStringsAsArrayList(), answer);
    }

    @Test
    public void testCollectionWrappedEnumerationEndSize() {
        System.out.println("collection wrapped enumeration - end size() call");

        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<String>(
                new CollectionWrappedEnumeration.EnumerationFactory() {

            @Override
            public Enumeration enumeration() {
                return testEnum();
            }

        });

        List<String> answer = new ArrayList<String>();
        Iterator<String> it = cwe.iterator();

        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        int size = cwe.size();

        assertEquals("array list of size " + size + " after middle size call != original", testStringsAsArrayList(), answer);
    }


    private static Enumeration<String> testEnum() {
        Enumeration<String> e = Collections.enumeration(testStringsAsArrayList());

        return e;
    }
    
    private static List<String> testStringsAsArrayList() {
        return Arrays.asList("one","two","three","four","five");
    }
}