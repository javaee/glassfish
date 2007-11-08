import junit.framework.*;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.xml.sax.InputSource;
import java.io.StringReader;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.config.serverbeans.validation.DomainXmlVerifier;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.1 $
 */

public class MakeCatalogTest extends TestCase {

    public void testInvalidStructure() throws Exception {
        final MakeCatalog mc = new MakeCatalog();
        final String key = "something ugly with = and spaces and ; in it!";
        final String value = "!@##$%^*   \n";
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='"+key+"'>"+ value +" <m:message id='"+key+"'>"+ value + "  </m:message>"+
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        try {
            mc.makeCatalog(is);
                /* FIXME This plus changing the validation to true in
            MakeCatalog is required once the validator is sorted out
            fail("Expected error indicating invalid structure");
                */
        }
        catch (SAXParseException spe){
            assertEquals("", spe.getMessage());
        }
    }
        
    public void testComplexPropertyCreation() throws Exception {
        final MakeCatalog mc = new MakeCatalog();
        final String key = "something ugly with = and spaces and ; in it!";
        final String value = "!@##$%^*   \n";
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='"+key+"'>"+ value +
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        mc.makeCatalog(is);
        assertEquals(value.trim(), mc.getProperties().getProperty(key));
        
    }

    public void testComplexPropertyCreationFailure() throws Exception {
        final MakeCatalog mc = new MakeCatalog();
        final String key = "something ugly with = and spaces and ; in it!";
        final String value = "!@##$%^&*   \n";
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='"+key+"'>"+ value +
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        try {
            mc.makeCatalog(is);
            fail("Expected error indicating illegal reference");
        }
        catch (SAXParseException spe){
        }
    }

    public void testSimplePropertyWithParamCreation() throws Exception {
        final MakeCatalog mc = new MakeCatalog();
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='idPrimaryKey'>"+
        "    foo {0}"+
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        mc.makeCatalog(is);
        final Properties p = mc.getProperties();
        assertNotNull("Didnt get the expected properties", p);
        assertNotNull("didn't find a message for the key", p.getProperty("idPrimaryKey"));
        assertEquals("didn't find the right message for the key", "foo {0}", p.getProperty("idPrimaryKey"));
    }
    
    public void testSimplePropertyCreation() throws Exception {
        final MakeCatalog mc = new MakeCatalog();
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='idPrimaryKey'>"+
        "    foo"+
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        mc.makeCatalog(is);
        final Properties p = mc.getProperties();
        assertNotNull("Didnt get the expected properties", p);
        assertNotNull("didn't find a message for the key", p.getProperty("idPrimaryKey"));
        assertEquals("didn't find the right message for the key", "foo", p.getProperty("idPrimaryKey"));
    }

    public void testSimplePropertyCreationWithPrefix() throws Exception {
        final MakeCatalog mc = new MakeCatalog(DomainXmlVerifier.class.getName());
        final String catalog="<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<m:messages xmlns:m='messages'>"+
        "  <m:message id='idPrimaryKey'>"+
        "    foo"+
        "  </m:message>"+
        "</m:messages>";
        final InputSource is = new InputSource(new StringReader(catalog));
        mc.makeCatalog(is);
        final Properties p = mc.getProperties();
        final String newKey = "com.sun.enterprise.config.serverbeans.validation.DomainXmlVerifier.idPrimaryKey";
        assertNotNull("Didnt get the expected properties", p);
        assertNotNull("didn't find a message for the key", p.getProperty(newKey));
        assertEquals("didn't find the right message for the key", "foo", p.getProperty(newKey));
    }

    public void testBasicProperties() throws Exception {
        final Properties p = new Properties();
        final String key = "something ugly with = and spaces and ; in it!";
        final String value = "!@##$%^&*   \n";
        p.setProperty(key, value);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        p.store(baos, null);

        final Properties p2 = new Properties();
        p2.load(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(p, p2);
        assertEquals(value, p.getProperty(key));
    }

    public MakeCatalogTest(String name){
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
            junit.textui.TestRunner.run(MakeCatalogTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new MakeCatalogTest(args[i]));
        }
        return ts;
    }
}
