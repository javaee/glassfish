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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.FileReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class SchematronTest extends TestCase {
    public void testSimpleTransform() throws Exception {
        Schematron s = new Schematron("/simple.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver("/sun-domain_1_1.dtd"));
        SAXSource src = new SAXSource(vr,
                                      new InputSource("domain.xml"));
        s.analyze(src, r);
    }

    
    public void testWithSAXSourceWithVRAndER() throws Exception {
        Schematron s = new Schematron("/domain.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver("/sun-domain_1_1.dtd"));
        SAXSource src = new SAXSource(vr,
                                      new InputSource("domain.xml.no.dtd"));
        s.analyze(src, r);
        assertEquals(1540, actual.toString().length());

    }
        // This is the test that demonstrates the problem - note that
        // the VR has no EntityResolver
    
    public void testWithSAXSourceWithVRNoER() throws Exception {
        Schematron s = new Schematron("/domain.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        VariableResolver vr = new VariableResolver();
        SAXSource src = new SAXSource(vr,
                                      new InputSource(new FileReader("domain.xml.no.dtd")));
        s.analyze(src, r);
        assertEquals(1540, actual.toString().length());
    }
        // This test demonstrates that a SAXSource object works OK
        // without a VariableResolver
    public void testWithSAXSourceNoVR() throws Exception {
        Schematron s = new Schematron("/domain.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        SAXSource src = new SAXSource(new InputSource("domain.xml.no.dtd"));
        s.analyze(src, r);
        assertEquals(1540, actual.toString().length());
    }
    
        // This test relies on the fact that the URL can be resolved,
        // but is some funky page that is not a DTD
    public void testWithEntityResolverError() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        String input = "<?xml version='1.0'?><!DOCTYPE root PUBLIC 'DO NOT MATCH' 'http://www.google.com'><root id=\"${var}\"><system-property name=\"var\" value=\"value\"/></root>";
        VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver("/schema2.dtd"));
        SAXSource src = new SAXSource(vr,
                                      new InputSource(new StringReader(input)));
        try {
            s.analyze(src, r);
            fail("Expected a Transformer Exception");
        }
        catch (TransformerException te){
            assertEquals("javax.xml.transform.TransformerException: com.sun.org.apache.xml.internal.utils.WrappedRuntimeException: External parameter entity \"%[dtd];\" has characters after markup.", te.getMessage());
        }

    }
        // This test doesn't reveal any errors with an external data
        // source and an external entity resolution
    public void testExternalWithEntityResolver() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver("/schema2.dtd"));
        SAXSource src = new SAXSource(vr,
                                      new InputSource("schema2.xml"));
        
        s.analyze(src, r);
        assertEquals("This is the id: value", ""+actual);
    }
        // This test doesn't reveal any errors in handling an external
        // data source
    public void testExternal() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        SAXSource src = new SAXSource(new VariableResolver(),
                                      new InputSource("schema2.xml"));
        
        s.analyze(src, r);
        assertEquals("This is the id: value", ""+actual);
    }
        // This test shows that an EntityResolver doesn't break the
        // test with internal data
    public void testWithEntityResolver() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        String input = "<?xml version='1.0'?><!DOCTYPE domain PUBLIC '-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN' 'http://www.sun.com/software/appserver/dtds/sun-domain_1_1.dtd'><root id=\"${var}\"><system-property name=\"var\" value=\"value\"/></root>";
        VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver("/schema2.dtd"));
        SAXSource src = new SAXSource(vr,
                                      new InputSource(new StringReader(input)));
        
        s.analyze(src, r);
        assertEquals("This is the id: value", ""+actual);
    }

        public void testComment() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        String input = "<?xml version='1.0'?><!-- comment --><root id=\"${var}\"><system-property name=\"var\" value=\"value\"/></root>";
        SAXSource src = new SAXSource(new VariableResolver(),
                                      new InputSource(new StringReader(input)));
        s.analyze(src, r);
        assertEquals("This is the id: value", ""+actual);
    }

        // This test shows that the Schematron works with an
        // externally defined XSL file, a VariableResolver with no
        // EntityResolution, and an internal stream.
    public void testPipeline() throws Exception {
        Schematron s = new Schematron("/schema2.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        String input = "<?xml version='1.0'?><root id=\"${var}\"><system-property name=\"var\" value=\"value\"/></root>";
        SAXSource src = new SAXSource(new VariableResolver(),
                                      new InputSource(new StringReader(input)));
        s.analyze(src, r);
        assertEquals("This is the id: value", ""+actual);
    }
        
    public void testSchematron() throws Exception  {
        Schematron s = new Schematron("/schema.xsl");
        StringWriter actual = new StringWriter();
        Result r = new StreamResult(actual);
        s.analyze(new SAXSource(new InputSource(new StringReader("<top><root id ='foo'/></top>"))), r);
        assertEquals("This is the result", ""+actual);
    }

    public SchematronTest(String name){
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
            TestRunner.run(SchematronTest.class);
        } else {
            TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new SchematronTest(args[i]));
        }
        return ts;
    }

    private static class MyEntityResolver implements EntityResolver
    {
        private String myDtd = "";

        MyEntityResolver(){}
        
        MyEntityResolver(String dtd){
            myDtd = dtd;
        }
        
        public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId) throws SAXException, IOException {
            if (null != publicId && publicId.equals("-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN")) {
                final InputStream is = getClass().getResourceAsStream(myDtd);
                return (null != is ) ? new InputSource(is) : null;
            } else {
                return null;
            }
        }
    }

                
}
