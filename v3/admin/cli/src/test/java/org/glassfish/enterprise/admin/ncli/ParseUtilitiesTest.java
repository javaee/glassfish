package org.glassfish.enterprise.admin.ncli;

import static junit.framework.Assert.*;
import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public class ParseUtilitiesTest extends TestCase {

    public void testCommandName() {
        String cmd = "create-jdbc-resource";
        assertEquals(ParseUtilities.indicatesCommandName(cmd), true);

        cmd = "-invalid-cmd-name";
        assertEquals(ParseUtilities.indicatesCommandName(cmd), false);

        cmd = "0another-invalid";
        assertEquals(ParseUtilities.indicatesCommandName(cmd), false);
    }

    public void testLegacySupportCommandsAreValid() {
        //this is a combined test as it tests multiple things.
        Set<String> slc = new HashSet<String>();
        ParseUtilities.file2Set(Constants.SUPPORTED_CMD_FILE_NAME, slc);
        Iterator<String> it = slc.iterator();
        while(it.hasNext()) {
            String cmd = it.next();
            System.out.println("Testing: " + cmd);
            assertEquals(ParseUtilities.indicatesCommandName(cmd), true);
        }
    }
}