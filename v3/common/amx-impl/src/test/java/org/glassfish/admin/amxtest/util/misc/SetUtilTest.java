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
package org.glassfish.admin.amxtest.util.misc;

import com.sun.appserv.management.util.misc.SetUtil;

import java.util.Set;

public final class SetUtilTest
        extends junit.framework.TestCase {
    public SetUtilTest() {
    }


    private static final String S1 = "hello";
    private static final String S2 = "there";
    private static final String S3 = "how";
    private static final String S4 = "are";

    public void
    testMultiNew() {
        Set<String> s;

        s = SetUtil.newSingletonSet(S1);
        assertEquals(S1, s.iterator().next());

        s = SetUtil.newSet(S1, S2);
        assert (s.contains(S1));
        assert (s.contains(S2));

        s = SetUtil.newSet(S1, S2, S3);
        assert (s.contains(S1));
        assert (s.contains(S2));
        assert (s.contains(S3));

        s = SetUtil.newSet(S1, S2, S3, S4);
        assert (s.contains(S1));
        assert (s.contains(S2));
        assert (s.contains(S3));
        assert (s.contains(S4));

        final Set<String> strings = s;
        final Set<Integer> integers = SetUtil.newSingletonSet(new Integer(3));

        final Set<?> both = SetUtil.newSet(strings, integers);
    }


    public void
    testCopy() {
        final Set<String> s = SetUtil.newSingletonSet(S1);
        final Set<String> sCopy = SetUtil.copySet(s);
        assertEquals(s, sCopy);
        assertEquals(sCopy, s);
        assertEquals(S1, SetUtil.getSingleton(s));
        assertEquals(S1, SetUtil.getSingleton(sCopy));
    }

    public void
    testGetSingleton() {
        final Set<String> s = SetUtil.newSingletonSet(S1);

        assertEquals(S1, SetUtil.getSingleton(s));

        s.add(S2);
        try {
            SetUtil.getSingleton(s);
            fail("expecting exception");
        }
        catch (IllegalArgumentException e) {
        }
    }


}






