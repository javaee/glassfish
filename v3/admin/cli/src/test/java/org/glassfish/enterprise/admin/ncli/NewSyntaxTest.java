package org.glassfish.enterprise.admin.ncli;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.glassfish.enterprise.admin.ncli.ParseUtilities.*;
import static org.glassfish.enterprise.admin.ncli.ProgramOptionBuilder.*;
import static org.glassfish.enterprise.admin.ncli.Constants.*;

import java.util.Set;

/** Tests for commands with the new syntax.
 * <p>
 *  <code>
 *  [asadmin-program-options] command-name [command-options-and-operands]
 * </code>
 * <p>
 *  All the tests should assert that FirstPassResult#usesDeprecatedSyntax() <b> is FALSE</b>.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public class NewSyntaxTest {
    @Test
    public void hostB4Cmd() throws ParserException {
        String cmd = "new-command";
        String cmdArg1 = "--opt1";
        String cmdArg2 = "operand1";
        String GIVEN_HOST = "foo";
        String GIVEN_PORT = "4544";
        String[] cmdline = new String[]{"--host", GIVEN_HOST, "--port", GIVEN_PORT, "--secure", cmd, cmdArg1, cmdArg2};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        assertEquals(cmd, fpr.getCommandName());
        assertFalse(fpr.usesDeprecatedSyntax());
        assertArrayEquals(new String[]{cmdArg1, cmdArg2}, fpr.getCommandArguments());
        //now test program options
        Option propt = getOptionNamed(fpr.getProgramOptions(), HOST);    //host
        assertEquals(GIVEN_HOST, propt.getEffectiveValue());

        propt = getOptionNamed(fpr.getProgramOptions(), PORT); //port
        assertEquals(GIVEN_PORT, propt.getEffectiveValue());

        propt = getOptionNamed(fpr.getProgramOptions(), SECURE);
        assertEquals("true", propt.getEffectiveValue().toLowerCase());
    }
    @Test
    public void reuseOption() throws ParserException {
        String cmd = "some-cmd";
        String arg1 = "--host";
        String arg2 = "cmdhost";
        String arg3 = "operand1";
        String[] cmdArgs = new String[]{arg1, arg2, arg3};
        String pHost = "asadminhost";
        String[] cmdline = new String[]{"--host", pHost, cmd, arg1, arg2, arg3};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        assertFalse(fpr.usesDeprecatedSyntax());
        assertEquals(cmd, fpr.getCommandName());

        //now test program options
        Option propt = getOptionNamed(fpr.getProgramOptions(), PORT);
        assertEquals(""+DEFAULT_PORT, propt.getEffectiveValue());

        propt = getOptionNamed(fpr.getProgramOptions(), HOST);
        assertEquals(pHost, propt.getEffectiveValue());

        propt = getOptionNamed(fpr.getProgramOptions(), SECURE);
        assertEquals("false", propt.getEffectiveValue().toLowerCase());
    }

    @Test (expected = ParserException.class)
    public void invalidProgramOption() throws ParserException {
        String[] cmdline = new String[]{"--invalid", "some-command", "--option", "value", "operand"}; //there is no program option named invalid
        new Parser(cmdline).firstPass();
    }

    @Test (expected = ParserException.class)
    public void missingCommand() throws ParserException {
        String[] cmdline = new String[]{"--host", "foo", "--port=1234", "-s", "-eI", "-u", "admin"}; // all valid program options, but no command :-)
        new Parser(cmdline).firstPass();
    }

    @Test
    public void allDefaults() throws ParserException {
        String[] cmdline = new String[] {"command-alone"};
        Set<Option> propts = new Parser(cmdline).firstPass().getProgramOptions();
        for(Option propt : propts) {
            String name  = propt.getName();
            String value = propt.getEffectiveValue();
            if (HOST.equals(name))
                assertEquals(DEFAULT_HOST, value);
            else if (PORT.equals(name))
                assertEquals(DEFAULT_PORT + "", value);
            else if (USER.equals(name))
                assertEquals(DEFAULT_USER, value);
            else if (SECURE.equals(name))
                assertEquals(DEFAULT_SECURE.toLowerCase(), value.toLowerCase());
            else if (ECHO.equals(name))
                assertEquals(DEFAULT_ECHO.toLowerCase(), value.toLowerCase());
            else if (TERSE.equals(name))
                assertEquals(DEFAULT_TERSE.toLowerCase(), value.toLowerCase());
            else if (INTERACTIVE.equals(name))
                assertEquals(DEFAULT_INTERACTIVE.toLowerCase(), value.toLowerCase());
            else if (PASSWORD.equals(name))
                assertNull(value);
            else {
                //do nothing, we don't check passwordfile, although we should have defaulted password file!
            }
        }
    }
}