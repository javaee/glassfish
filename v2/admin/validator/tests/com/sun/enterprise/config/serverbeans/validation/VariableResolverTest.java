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

package com.sun.enterprise.config.serverbeans.validation;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import com.sun.org.apache.xml.internal.serializer.ToXMLStream;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class VariableResolverTest extends TestCase {
    VariableResolver vr;
    StringWriter out;

    public void testXMLSerialization() throws Exception {
        StringWriter w = new StringWriter();
        ToXMLStream xml = new ToXMLStream();
        xml.setWriter(w);
        xml.setOmitXMLDeclaration(true);
        xml.startElement("uri", "localName", "name", null);
        xml.endElement("uri", "localName", "name");
        assertEquals("<name xmlns=\"uri\"/>", w.toString());
    }
    

    public void testFile() throws Exception{
        vr.parse(new InputSource("test.xml"));
        
        String expected = "<r>\n"+
        "  <foo>\n"+
        "    <bar>\n"+
        "      <gargle>la di da</gargle>\n"+
        "    </bar>\n"+
        "    <bar>\n"+
        "      hick\n"+
        "    </bar>\n"+
        "    <system-property name=\"k1\" value=\"this is the value of k1\"/>\n"+
        "    A reference to k1 yields: \"this is the value of k1\"\n"+
        "  </foo>\n"+
        "  <config name=\"config1\">\n"+
        "    k1: \"K1 VALUE\"\n"+
        "    <system-property name=\"k1\" value=\"K1 VALUE\"/>\n"+
        "  </config>\n"+
        "  <config name=\"config2\">\n"+
        "    K1: \"this is the value of k1\"\n"+
        "  </config>\n"+
        "</r>";
        assertEquals(expected, ""+out);
    }
    public void testURI() throws Exception{
        final String uri = new File ("test.xml").toURI().toURL().toString();
        vr.parse(new InputSource(uri));
        
        String expected = "<r>\n"+
        "  <foo>\n"+
        "    <bar>\n"+
        "      <gargle>la di da</gargle>\n"+
        "    </bar>\n"+
        "    <bar>\n"+
        "      hick\n"+
        "    </bar>\n"+
        "    <system-property name=\"k1\" value=\"this is the value of k1\"/>\n"+
        "    A reference to k1 yields: \"this is the value of k1\"\n"+
        "  </foo>\n"+
        "  <config name=\"config1\">\n"+
        "    k1: \"K1 VALUE\"\n"+
        "    <system-property name=\"k1\" value=\"K1 VALUE\"/>\n"+
        "  </config>\n"+
        "  <config name=\"config2\">\n"+
        "    K1: \"this is the value of k1\"\n"+
        "  </config>\n"+
        "</r>";
        assertEquals(expected, ""+out);
    }
    

      // test out the use of a default XML Reader
    public void testUseOfDefaultReader() throws Exception {
        VariableResolver myVr = new VariableResolver();
        myVr.setContentHandler(getContentHandler(out));
        String input = "<foo att=\"${var}\"><system-property name=\"var\" value=\"value\"/></foo>";
        String expected = "<foo att=\"value\"><system-property name=\"var\" value=\"value\"/></foo>";
        myVr.parse(getInputSource(input));
        assertEquals(expected, ""+out);
    }
    
      // here we test that variable references are appropriate to
      // their scope.
    public void testAComplexExample() throws Exception {
        String input = "<r>"+
        "<foo><bar><gargle/>la di da</bar><bar>hick</bar>"+
        "<system-property name=\"k1\" value=\"this is the value of k1\"/>"+
        "A reference to k1 yields: \"${k1}\""+
        "</foo>"+
        "<config name=\"config1\">" +
        "k1: \"${k1}\""+
        "<system-property name=\"k1\" value=\"K1 VALUE\"/>"+
        "</config>"+
        "<config name=\"config2\">"+
        "k1: \"${k1}\""+
        "</config>"+
        "</r>";
        String expected = "<r>"+
        "<foo><bar><gargle/>la di da</bar><bar>hick</bar>"+
        "<system-property name=\"k1\" value=\"this is the value of k1\"/>"+
        "A reference to k1 yields: \"this is the value of k1\""+
        "</foo>"+
        "<config name=\"config1\">" +
        "k1: \"K1 VALUE\""+
        "<system-property name=\"k1\" value=\"K1 VALUE\"/>"+
        "</config>"+
        "<config name=\"config2\">"+
        "k1: \"this is the value of k1\""+
        "</config>"+
        "</r>";
        vr.parse(getInputSource(input));
        assertEquals(expected, ""+out);
    }
    
    public void testConfigStuff() throws Exception {
        String input = "<r>"+
        "<config name=\"c1\" att=\"${k1}\">"+
        "<child>${k2}</child>"+
        "<system-property name=\"k2\" value=\"v2\"/>"+
        "</config>"+
        "<system-property name=\"k1\" value=\"v1\"/>"+
        "</r>";
        String expected = "<r>"+
        "<config name=\"c1\" att=\"v1\">"+
        "<child>v2</child>"+
        "<system-property name=\"k2\" value=\"v2\"/>"+
        "</config>"+
        "<system-property name=\"k1\" value=\"v1\"/>"+
        "</r>";
        vr.parse(getInputSource(input));
        assertEquals(expected, ""+out);
    }
    
        
    public void testBasicSystemVariable() throws Exception {
        String input = "<foo att=\"${var}\"><system-property name=\"var\" value=\"value\"/></foo>";
        String expected = "<foo att=\"value\"><system-property name=\"var\" value=\"value\"/></foo>";
        vr.parse(getInputSource(input));
        assertEquals(expected, ""+out);
    }
    
    public void testSimpleExpansion() throws Exception {
        String input = "<foo>${path.separator}</foo>";
        vr.parse(getInputSource(input));
        assertEquals("<foo>"+System.getProperty("path.separator")+"</foo>", ""+out);
    }
    
    public void testBasicOperation() throws Exception {
        String doc = "<foo/>";
        vr.parse(getInputSource(doc));
        out.flush();
        assertEquals("<foo/>", ""+out);
    }
    private InputSource getInputSource(String s) {
        return new InputSource(new StringReader(s));
    }

    private ContentHandler getContentHandler(StringWriter w) throws Exception{
        ToXMLStream xml = new ToXMLStream();
        xml.setWriter(w);
        xml.setOmitXMLDeclaration(true);
        return xml.asContentHandler();
    }
//     private ContentHandler getContentHandler(StringWriter w) throws Exception{
//         OutputFormat of = new OutputFormat();
//         of.setOmitXMLDeclaration(true);        
//         Serializer ts = new XMLSerializer(of);
//         ts.setOutputCharStream(w);
//         return ts.asContentHandler();
//     }
    
        
    private XMLReader getXMLReader() throws Exception {
        final XMLReader xr =  SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        return xr;
    }

    public VariableResolverTest(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        vr = new VariableResolver();
        vr.setParent(getXMLReader());
        out = new StringWriter();
        vr.setContentHandler(getContentHandler(out));
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(VariableResolverTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new VariableResolverTest(args[i]));
        }
        return ts;
    }
}
