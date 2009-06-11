package org.glassfish.enterprise.admin.ncli;

import org.junit.*;
import static org.junit.Assert.*;
import static org.glassfish.enterprise.admin.ncli.ProgramOptionBuilder.*;

import java.util.Set;

/** A Junit4 test to test out the parser with various command lines. It works with examination of FirstPassResult
 *  and SecondPassResult.
 * 
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public class ParserTest {

    @Test (expected = ParserException.class)
    public void handleUnsupportedLegacyCommandMethod() throws ParserException {
        Parser p = new Parser(new String[]{"create-cluster", "create-instance"});
        p.firstPass();
    }

    @Test
    public void testHost1() throws ParserException {
        final String value = "1.2.3.4";
        final String CMD   = "foo";
        Parser p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST), value});  // foo --host 1.2.3.4
        FirstPassResult fpr = p.firstPass();
        Set<Option> pos = fpr.getProgramOptions();
        Option host = ParseUtilities.getOptionNamed(pos, HOST);
        assertEquals(fpr.getCommandName(), CMD);
        assertNotNull(host);
        assertEquals(value, host.getEffectiveValue());

        p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST_SYMBOL), value});
        fpr = p.firstPass();
        pos = fpr.getProgramOptions();
        host = ParseUtilities.getOptionNamed(pos, HOST);
        assertEquals(fpr.getCommandName(), CMD);
        assertNotNull(host);
        assertEquals(value, host.getEffectiveValue());
    }
    @Test
    public void testHost2() throws ParserException {
        final String value = "1.2.3.4";
        final String CMD   = "foo";
        Parser p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST) + "=" + value});  // foo --host=1.2.3.4
        FirstPassResult fpr = p.firstPass();
        Set<Option> pos = fpr.getProgramOptions();
        Option host = ParseUtilities.getOptionNamed(pos, HOST);
        assertEquals(fpr.getCommandName(), CMD);
        assertNotNull(host);
        assertEquals(value, host.getEffectiveValue());

        p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST_SYMBOL) + "=" + value});
        fpr = p.firstPass();
        pos = fpr.getProgramOptions();
        host = ParseUtilities.getOptionNamed(pos, HOST);
        assertEquals(fpr.getCommandName(), CMD);
        assertNotNull(host);
        assertEquals(value, host.getEffectiveValue());
    }

    @Test
    public void testAnyTwo1() throws ParserException {
        String hv = "foo.sun.com", pv = "3355", CMD = "bar";
        Parser p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST), hv, Option.toCommandLineOption(PORT), pv }); //bar --host foo.sun.com --port 3355
        FirstPassResult fpr = p.firstPass();
        Option host = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), HOST);
        Option port = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), PORT);
        assertEquals(host.getEffectiveValue(), hv);
        assertEquals(port.getEffectiveValue(), pv);

        p = new Parser(new String[]{CMD, Option.toCommandLineOption(HOST), hv, Option.toCommandLineOption(PORT_SYMBOL), pv }); //bar --host foo.sun.com --p 3355
        fpr = p.firstPass();
        host = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), HOST);
        port = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), PORT);
        assertEquals(host.getEffectiveValue(), hv);
        assertEquals(port.getEffectiveValue(), pv);
    }

    @Test
    public void testDefaults() throws ParserException {
        Parser p = new Parser(new String[]{"cmd"});
        FirstPassResult fpr = p.firstPass();
        Option host = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), HOST);
        assertTrue("default value of host is not: "+ Constants.DEFAULT_HOST, Constants.DEFAULT_HOST.equals(host.getEffectiveValue()));

        Option port = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), PORT);
        int pn = Integer.parseInt(port.getEffectiveValue());
        assertEquals(Constants.DEFAULT_PORT, pn);
    }

    @Test(expected = ParserException.class)
    public void testLimitation1() throws ParserException {
        String[] cmdline = new String[]{"cmd", "--host", "1.2.3.4", "--host", "5.6.7.8", "-p=4555", "-g"};
        //this should throw a ParserException because we know nothing about -g and the only way in which we could
        //support this is if option name/symbol + value is specified as a single argument, i.e. -g=value or --gggg=value
        //this is the parser limitation as it does not have the metadata for the command!
        new Parser(cmdline).firstPass();
    }

    @Test
    public void testWorkAroundLimitation1() throws ParserException {
        //this is opposite of testLimitation1
        String[] cmdline = new String[]{"cmd", "--host", "1.2.3.4", "-p=4555", "-g=some value"};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        String[] ca = fpr.getCommandArguments();
        assertArrayEquals("arrays are not same", ca, new String[]{"-g=some value"}); //note that first pass always removes -- or -
    }
    @Test
    public void testIntermingled1() throws ParserException {
        //this uses the work-around
        String[] cmdline = new String[]{"create-jdbc-resource", "--cmdopt=abcde", "-H", "internal.sun.com", "--port=9999", "-s"};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        assertArrayEquals("arrays are not same", fpr.getCommandArguments(), new String[]{"--cmdopt=abcde"});
        assertTrue(fpr.usesDeprecatedSyntax());
    }
    @Test
    public void testBooleanOptionList() throws ParserException {
        String[] cmdline = new String[]{"cmd", "-eIt", "--", "abc"};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        Option bo = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), INTERACTIVE);
        assertEquals(bo.getEffectiveValue(), "true");
        bo = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), TERSE);
        assertEquals(bo.getEffectiveValue(), "true");
        bo = ParseUtilities.getOptionNamed(fpr.getProgramOptions(), ECHO);
        assertEquals(bo.getEffectiveValue(), "true");
    }

    @Test (expected=ParserException.class)
    public void rejectPassword1() throws ParserException {
        String[] cmdline = new String[]{"cmd", "--password=secret"};
        new Parser(cmdline).firstPass();
    }
    @Test (expected=ParserException.class)
    public void rejectPassword2() throws ParserException {
        String[] cmdline = new String[]{"cmd", "-w", "secret"};
        new Parser(cmdline).firstPass();
    }

    @Test
    public void assertDeprecated1() throws ParserException {
        String cmd = "create-http-listener"; //this is a legacy command, now, we will specify it the legacy way, deliberately.
        String[] cmdline = new String[]{cmd, "--host=localhost", "--port=1234", "--secure", "true", "listener1"};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        assertTrue(fpr.usesDeprecatedSyntax());
    }
    @Test
    public void assertNotDeprecated1() throws ParserException {
        String cmd = "possibly-new-command"; //this is a new command, now, we will specify it the legacy way, deliberately. Such specification lets program options be reused
        String[] cmdline = new String[]{cmd, "--host=localhost", "--port=1234", "--secure", "true", "listener1"};
        Parser p = new Parser(cmdline);
        FirstPassResult fpr = p.firstPass();
        assertFalse(fpr.usesDeprecatedSyntax());  //this is NOT deprecated as --host is reused by possibly-new-command
    }
}