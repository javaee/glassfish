package org.glassfish.enterprise.admin.ncli;

import static org.glassfish.enterprise.admin.ncli.ProgramOptionBuilder.*;
import static org.glassfish.enterprise.admin.ncli.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** A JUnit4 Test Class for FirstPassResult. Uses other utility methods as well.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 *
 * @see FirstPassResult
 */
public class FirstPassResultTest {
    @Test
    public void testGetProgramOptionsForDefaults() {
        String cmd = "foo";
        Map<String, String> poPair = new HashMap<String, String>();
        String[] cmdArgs = new String[]{};

        //this is the command line with *just* the command name on it. Everything should be defaulted in that case.
        FirstPassResult fpr = new FirstPassResult(cmd, poPair, cmdArgs);
        Set<Option> pos = fpr.getProgramOptions();
        
        Option op = ParseUtilities.getOptionNamed(pos, HOST);
        assertEquals(DEFAULT_HOST, op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, PORT);
        assertEquals(DEFAULT_PORT, Integer.parseInt(op.getEffectiveValue()));

        op = ParseUtilities.getOptionNamed(pos, USER);
        assertEquals(DEFAULT_USER, op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, PASSWORD);
        assertNull(op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, PASSWORDFILE);
        assertNull(op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, ECHO);
        assertEquals("false", op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, TERSE);
        assertEquals("false", op.getEffectiveValue());

        op = ParseUtilities.getOptionNamed(pos, INTERACTIVE);
        assertEquals("true", op.getEffectiveValue());
    }
}
