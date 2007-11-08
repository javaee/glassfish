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

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.Serializer;
import com.sun.org.apache.xml.internal.serialize.TextSerializer;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class VariableExpanderTest extends TestCase {
      // In this test we ensure that all characters collected so far
      // are processed before a child element is processed.
    public void testCharactersComeBeforeChild() throws Exception{
        final String input = "<r>"+
        "<parent>"+
        "some child characters that should appear before the child element"+
        "<child>this should come after the preceding characters</child>"+
        "some child characters that should come after the child element"+
        "</parent>"+
        "</r>";
        ve.parse(getInputSource(input));
        assertEquals(input, ""+out);
    }
    
    public void testSetFramerBasic() throws Exception {
        VariableExpander ve = new VariableExpander();
        ve.setFramer(new Framer());
    }
    
                
    public void testSetFramer() throws Exception {
        frameHolder.getDomainFrame().put("domain", "domain from original frame holder");
        FrameHolder newFH = new FrameHolder();
        newFH.getDomainFrame().put("domain", "new");
        Framer framer = new Framer(newFH);
        ve.setFramer(framer);
        ve.parse(getInputSource("<foo a=\"${domain}\"/>"));
        assertEquals("<foo a=\"new\"/>", ""+out);
    }
    
        
    public void testGeneralCharacterHandling() throws Exception {
        constructComplexFrames();
       final String doc = "<doc>" +
        "<config name=\"c1\" att1=\"${d}\" att2=\"att2\">foo far ${c1} ${d}</config>"+
        "<server name=\"s2\">gargle ${s2} ${d} ${cl1}</server>"+
        "</doc>";
        final String expected = "<doc>" +
        "<config name=\"c1\" att1=\"dv1\" att2=\"att2\">foo far cv1 dv1</config>"+
        "<server name=\"s2\">gargle sv2 dv1 clv1</server>"+
        "</doc>";
        ve.parse(getInputSource(doc));
        assertEquals(expected, ""+out);
    }

    public void testAttributeAndCharacterHandling() throws Exception{
        frameHolder.getDomainFrame().put("k", "v");
        frameHolder.getDomainFrame().put("k1", "v1");
        ve.parse(getInputSource("<foo attr1='fee' attr2='${k}'>foo far ${k} ${k1}</foo>"));
        assertEquals("<foo attr1=\"fee\" attr2=\"v\">foo far v v1</foo>", ""+out);
    }
        
    public void testAttributeHandling() throws Exception{
        frameHolder.getDomainFrame().put("k", "v");
        ve.parse(getInputSource("<foo attr1='fee' attr2='${k}'/>"));
        assertEquals("<foo attr1=\"fee\" attr2=\"v\"/>", ""+out);
    }
    

      // Use this to construct a set of frames with inheritance
      // etc. that's useful for testing with.
    private void constructComplexFrames() {
        Frame domain, config1, config2, cluster, server1, server2, server3;
        String d = "d";
        String dv1 = "dv1";
        String c1 = "c1";
        String cv1 = "cv1";
        String c2 = "c2";
        String cv2 = "cv2";
        String cl1 = "cl1";
        String clv1 = "clv1";
        String s1 = "s1";
        String sv1 = "sv1";
        String s2 = "s2";
        String sv2 = "sv2";
        String s3 = "s3";
        String sv3 = "sv3";
        domain = frameHolder.getDomainFrame();
        domain.put(d, dv1);
        config1 = frameHolder.getConfigFrame(c1);
        config1.put(c1, cv1);
        config1.inheritFrom(domain);
        config2 = frameHolder.getConfigFrame(c2);
        config2.put(c2, cv2);
        config2.inheritFrom(domain);
        server1 = frameHolder.getServerFrame(s1);
        server1.put(s1, sv1);
        server1.inheritFrom(config1);
        server2 = frameHolder.getServerFrame(s2);
        server2.put(s2, sv2);
        server3 = frameHolder.getServerFrame(s3);
        server3.put(s3, sv3);
        server3.inheritFrom(config2);
        cluster = frameHolder.getClusterFrame(cl1);
        cluster.put(cl1, clv1);
        cluster.inheritFrom(config1);
        server2.inheritFrom(cluster);
    }
    
        
    public void testConfigCase() throws Exception {
        frameHolder.getDomainFrame().put("domain", "domain_new");
        frameHolder.getConfigFrame("config1").put("config1", "config1_v");
        ve.parse(getInputSource("<foo><system-property name='config1' value='sys-property'/><config name='config1'>${config1}</config></foo>"));
        assertEquals("<foo><system-property name=\"config1\" value=\"sys-property\"/><config name=\"config1\">config1_v</config></foo>", out.toString());
    }

        
    public void testMultipleVariables() throws Exception {
        Frame f = frameHolder.getDomainFrame();
        f.put("k1", "v1");
        f.put("k2", "v2");
        assertEquals("far lar lar v1 foo far v2", ve.eval("far lar lar ${k1} foo far ${k2}", f));
    }
    
    public void testNestedReference() throws Exception {
        Frame f = Frame.newFrame();
        f.put("k", "v");
        assertEquals("${fo${k}}", ve.eval("${fo${k}}", f));
        
    }
    
    public void testPartialReference() throws Exception {
        Frame f = Frame.newFrame();
        f.put("domain", "domain_new");
        assertEquals("${fo", ve.eval("${fo", f));
        
    }
    
    public void testInvalidReference() throws Exception{
        Frame f = Frame.newFrame();
        f.put("domain", "domain_new");
        assertEquals("${foo}", ve.eval("${foo}", f));
    }
    
    public void testSimpleCase() throws Exception {
        Frame f = Frame.newFrame();
        f.put("domain", "domain_new");
        assertEquals("domain_new", ve.eval("${domain}", f));
    }
    

    private InputSource getInputSource(String s) {
        return new InputSource(new StringReader(s));
    }
        
                 
    private XMLReader getXMLReader() throws Exception {
        
        final XMLReader xr =  SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        return xr;

    }
    
            
    public VariableExpanderTest(String name){
        super(name);
    }

    private FrameHolder frameHolder;
    private VariableExpander ve;
    private StringWriter out;
    
    
    protected void setUp() throws Exception {
        frameHolder = new FrameHolder();
        out = new StringWriter();
        ve = new VariableExpander(new Framer(frameHolder));
        ve.setParent(getXMLReader());
        OutputFormat of = new OutputFormat();
        of.setOmitXMLDeclaration(true);        
        Serializer ts = new XMLSerializer(of);
        ts.setOutputCharStream(out);
        ve.setContentHandler(ts.asContentHandler());
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(VariableExpanderTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new VariableExpanderTest(args[i]));
        }
        return ts;
    }
}
