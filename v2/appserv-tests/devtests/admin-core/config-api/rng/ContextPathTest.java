import junit.framework.*;
import java.util.regex.Pattern;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.1 $
 */

public class ContextPathTest extends TestCase {
//     private static final String pattern="([a-zA-Z0-9$-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*(/([a-zA-Z0-9$-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*)*";
    private static final String pattern="([a-zA-Z0-9$\\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*(/([a-zA-Z0-9$\\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*)*";

    public void testPrint(){
        System.err.println("\""+pattern+"\"");
    }
    
    public void test() {
        assertTrue(Pattern.matches(pattern, "foo"));
        assertTrue(Pattern.matches(pattern, ""));
        assertTrue(Pattern.matches(pattern, "&"));
        assertTrue(Pattern.matches(pattern, "/"));
        assertTrue(Pattern.matches(pattern, "/foo-bar"));
        assertTrue(Pattern.matches(pattern, "/foo/bar!path&;x='y'"));
        assertTrue(Pattern.matches(pattern, "%aF/boo"));
        assertFalse(Pattern.matches(pattern, "invalid context path"));
        assertFalse(Pattern.matches(pattern, "invalid<context>path"));
        
        
    }

    public ContextPathTest(String name){
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(ContextPathTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ContextPathTest(args[i]));
        }
        return ts;
    }
}
