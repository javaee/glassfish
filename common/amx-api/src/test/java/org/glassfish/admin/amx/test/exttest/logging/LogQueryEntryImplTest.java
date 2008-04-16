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
package org.glassfish.admin.amx.exttest.logging;

import com.sun.appserv.management.ext.logging.LogQueryEntry;
import com.sun.appserv.management.ext.logging.LogQueryEntryImpl;

import javax.management.openmbean.OpenDataException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

/**
 */
public final class LogQueryEntryImplTest
        extends junit.framework.TestCase {
    public LogQueryEntryImplTest() {
    }


    private static final String N1 = "foo";
    private static final String V1 = "foo-value";
    private static final String N2 = LogQueryEntry.THREAD_ID_KEY;
    private static final String V2 = "13347";

    private static String
    nvp(
            final String name,
            final String value) {
        return (name + "=" + value);
    }

    public static LogQueryEntryImpl
    createDummy() {
        return new LogQueryEntryImpl(
                0,
                new Date(),
                Level.OFF.toString(),
                "Sun Appserver whatever",
                "hello world",
                "MSG01",
                "module",
                nvp(N1, V1) + ";" + nvp(N2, V2));
    }

    public void
    testCreate() {
        createDummy();
    }

    public void
    testEquals() {
        final LogQueryEntryImpl d = createDummy();

        assertEquals(d, d);

        final LogQueryEntryImpl dCopy = new LogQueryEntryImpl(
                d.getRecordNumber(),
                d.getDate(),
                d.getLevel(),
                d.getProductName(),
                d.getMessage(),
                d.getMessageID(),
                d.getModule(),
                d.getNameValuePairs());

        assertEquals(d, dCopy);
        assertEquals(dCopy, d);
    }


    public void
    testGetNameValuePairsMap() {
        final LogQueryEntryImpl d = createDummy();

        final Map<String, String> m = d.getNameValuePairsMap();
        assertEquals(V1, m.get(N1));
        assertEquals(V2, m.get(N2));
    }

    public void
    testGetters()
            throws OpenDataException {
        final LogQueryEntryImpl d = createDummy();

        d.getRecordNumber();
        d.getDate();
        d.getLevel();
        d.getMessage();
        d.getMessageID();
        d.getModule();
        d.getMessage();
        d.getNameValuePairs();
        d.getNameValuePairsMap();
    }

    /*
         public void
     testAsMap()
     {
         final LogQueryEntryImpl dummy = createDummy();

         final Mapxxx   m   = dummy.asMap();

         final LogQueryEntryImpl copy    = new LogQueryEntryImpl( m );
         assertEquals( dummy, copy );
     }

         public void
     testAsCompositeData()
         throws OpenDataException
     {
         final LogQueryEntryImpl dummy = createDummy();

         final CompositeData   d   = dummy.asCompositeData();

         final LogQueryEntryImpl copy    = new LogQueryEntryImpl( d );
         assertEquals( dummy, copy );

         assert( copy.toString().equals( dummy.toString() ) );
     }
     */
}









