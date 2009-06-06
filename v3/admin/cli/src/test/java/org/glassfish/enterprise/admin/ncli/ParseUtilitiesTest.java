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
        assertEquals(indicatesCommandName(cmd), true);

        cmd = "-invalid-cmd-name";
        assertEquals(indicatesCommandName(cmd), false);

        cmd = "0another-invalid";
        assertEquals(indicatesCommandName(cmd), false);

        cmd = "invalid with space";
        assertEquals(indicatesCommandName(cmd), false);
    }

    @Test
    public void indicatesCommandName2() {
        //this is a combined test as it tests multiple things.
        Set<String> slc = new HashSet<String>();
        file2Set(Constants.SUPPORTED_CMD_FILE_NAME, slc);
        for (String c : slc) {
            assertEquals(indicatesCommandName(c), true);
        }
    }

    @Test
    public void indicatesOption1() {
        String s = "--host"; //valid long option
        assertEquals(indicatesOption(s), true);
        s = "-f";  //valid short option
        assertEquals(indicatesOption(s), true);
        s = "-f=false"; //valid short option with value, separated by an '='
        assertEquals(indicatesOption(s), true);
        s = "-p=name=value"; //valid short option with property specification
        assertEquals(indicatesOption(s), true);
        s = "--prop=a=b:c=d";  //valid long option with multiple properties delimited with a ':'
        assertEquals(indicatesOption(s), true);
        s = "--no-force";
        assertEquals(indicatesOption(s), true);

        //invalid cases
        s = "-prop"; //invalid long or short option
        assertEquals(indicatesOption(s), false);
        s = "foo"; //option must start with a - or -- or --no-
        assertEquals(indicatesOption(s), false);
        s = "-"; //short option should have at least a character
        assertEquals(indicatesOption(s), false);
        s = "--"; //long option should have at least 2 characters in name
        assertEquals(indicatesOption(s), false);
        s = "--f"; //neither a short option (one additional '-') or a long option (lack of a character)
        assertEquals(indicatesOption(s), false);
        s = "--no-f";
        assertEquals(indicatesOption(s), false);
        s = "--with-hyphen"; //unfortunately, '-' is not allowed in the name of the option
        assertEquals(indicatesOption(s), false);
        s = "-to"; //a symbol is should be one character long
        assertEquals(indicatesOption(s), false);
        s = "--space banned in option name";
        assertEquals(indicatesOption(s), false);
    }

    @Test
    public void getOptionNameFromLongOption1() {
        String s = "--host=localhost";
        assertEquals(getOptionNameFromLongOption(s), "host");
        s = "--host";
        assertEquals(getOptionNameFromLongOption(s), "host");
    }
    @Test
    public void getOptionSymbolFromShortOption1() {
        String s = "-h=129.23.44.224";
        assertEquals(getOptionSymbolFromShortOption(s), 'h');
        s = "-h";
        assertEquals(getOptionSymbolFromShortOption(s), 'h');        
    }

    @Test
    public void hasOptionNameAndValue1() {
        String s = "-h=localhost";
        assertEquals(hasOptionNameAndValue(s), true);
        s = "--desc=application display name";
        assertEquals(hasOptionNameAndValue(s), true);
        s = "-h";
        assertEquals(hasOptionNameAndValue(s), false);
        s = "--host";
        assertEquals(hasOptionNameAndValue(s), false);
        s = "--with_equals=a=b";
        assertEquals(hasOptionNameAndValue(s), true);
    }

    @Test
    public void getOptionValue1() {
        String s = "-h=localhost";
        assertEquals(getOptionValue(s), "localhost");
        s = "--desc=application display name";
        assertEquals(getOptionValue(s), "application display name");
        s = "--prop=a=b";
        assertEquals(getOptionValue(s), "a=b");        
    }
}