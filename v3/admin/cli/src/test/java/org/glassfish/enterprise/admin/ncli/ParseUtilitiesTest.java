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
