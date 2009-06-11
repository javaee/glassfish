/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *   Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 *   The contents of this file are subject to the terms of either the GNU
 *   General Public License Version 2 only ("GPL") or the Common Development
 *   and Distribution License("CDDL") (collectively, the "License").  You
 *   may not use this file except in compliance with the License. You can obtain
 *   a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *   or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *   language governing permissions and limitations under the License.
 *
 *   When distributing the software, include this License Header Notice in each
 *   file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *   Sun designates this particular file as subject to the "Classpath" exception
 *   as provided by Sun in the GPL Version 2 section of the License file that
 *   accompanied this code.  If applicable, add the following below the License
 *   Header, with the fields enclosed by brackets [] replaced by your own
 *   identifying information: "Portions Copyrighted [year]
 *   [name of copyright owner]"
 *
 *   Contributor(s):
 *
 *   If you wish your version of this file to be governed by only the CDDL or
 *   only the GPL Version 2, indicate your decision by adding "[Contributor]
 *   elects to include this software in this distribution under the [CDDL or GPL
 *   Version 2] license."  If you don't indicate a single choice of license, a
 *   recipient has the option to distribute your version of this file under
 *   either the CDDL, the GPL Version 2 or to extend the choice of license to
 *   its licensees as provided above.  However, if you add GPL Version 2 code
 *   and therefore, elected the GPL Version 2 license, then the option applies
 *   only if the new code is made subject to such option by the copyright
 *   holder.
 */

package org.glassfish.enterprise.admin.ncli;

import static org.glassfish.enterprise.admin.ncli.ParseUtilities.*;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public class ParseUtilitiesTest {

    @Test
    public void indicatesCommandName1() {
        String cmd = "create-jdbc-resource";
        assertEquals(true, indicatesCommandName(cmd));

        cmd = "-invalid-cmd-name";
        assertEquals(false, indicatesCommandName(cmd));

        cmd = "0another-invalid";
        assertEquals(false, indicatesCommandName(cmd));

        cmd = "invalid with space";
        assertEquals(false, indicatesCommandName(cmd));
    }

    @Test
    public void indicatesCommandName2() {
        //this is a combined test as it tests multiple things.
        Set<String> slc = new HashSet<String>();
        file2Set(Constants.SUPPORTED_CMD_FILE_NAME, slc);
        for (String c : slc) {
            assertEquals(true, indicatesCommandName(c));
        }
    }

    @Test
    public void indicatesOption1() {
        String s = "--host"; //valid long option
        assertEquals(true, indicatesOption(s));
        s = "-f";  //valid short option
        assertEquals(true, indicatesOption(s));
        s = "-f=false"; //valid short option with value, separated by an '='
        assertEquals(true, indicatesOption(s));
        s = "-p=name=value"; //valid short option with property specification
        assertEquals(true, indicatesOption(s));
        s = "--prop=a=b:c=d";  //valid long option with multiple properties delimited with a ':'
        assertEquals(true, indicatesOption(s));
        s = "--no-force";
        assertEquals(true, indicatesOption(s));

        //invalid cases
        s = "-prop"; //invalid long or short option
        assertEquals(false, indicatesOption(s));
        s = "foo"; //option must start with a - or -- or --no-
        assertEquals(false, indicatesOption(s));
        s = "-"; //short option should have exactly one character as its symbol
        assertEquals(false, indicatesOption(s));
        s = "--"; //long option should have at least 2 characters in name
        assertEquals(false, indicatesOption(s));
        s = "--f"; //neither a short option (one additional '-') or a long option (lack of a character)
        assertEquals(false, indicatesOption(s));
        s = "--no-f";
        assertEquals(false, indicatesOption(s));
        s = "--with-hyphen"; //unfortunately, '-' is not allowed in the name of the option
        assertEquals(false, indicatesOption(s));
        s = "-to"; //a symbol is should be one character long
        assertEquals(false, indicatesOption(s));
        s = "--space banned in option name";
        assertEquals(false, indicatesOption(s));
    }

    @Test
    public void getOptionNameFromLongOption1() {
        String s = "--host=localhost";
        assertEquals("host", getOptionNameFromLongOption(s));
        s = "--host";
        assertEquals("host", getOptionNameFromLongOption(s));
    }
    @Test
    public void getOptionSymbolFromShortOption1() {
        String s = "-h=129.23.44.224";
        assertEquals('h', getOptionSymbolFromShortOption(s));
        s = "-h";
        assertEquals('h', getOptionSymbolFromShortOption(s));        
    }

    @Test
    public void hasOptionNameAndValue1() {
        String s = "-h=localhost";
        assertEquals(true, hasOptionNameAndValue(s));
        s = "--desc=application display name";
        assertEquals(true, hasOptionNameAndValue(s));
        s = "-h";
        assertEquals(false, hasOptionNameAndValue(s));
        s = "--host";
        assertEquals(false, hasOptionNameAndValue(s));
        s = "--with_equals=a=b";
        assertEquals(true, hasOptionNameAndValue(s));
    }

    @Test
    public void getOptionValue1() {
        String s = "-h=localhost";
        assertEquals("localhost", getOptionValue(s));
        s = "--desc=application display name";
        assertEquals("application display name", getOptionValue(s));
        s = "--prop=a=b";
        assertEquals("a=b", getOptionValue(s));        
    }

    @Test
    public void booleanOptionList() {
        String bol = "-abc";
        assertTrue(indicatesBooleanOptionList(bol));
        bol = "--abc";
        assertFalse(indicatesBooleanOptionList(bol));
        bol = "-abc=true";
        assertFalse(indicatesBooleanOptionList(bol));
    }
}
