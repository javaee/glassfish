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

import junit.framework.*;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.xml.sax.SAXParseException;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.File;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class RNGValidatorTest extends TestCase {
    public void testacceptanceTestNoEntityFile() throws Exception{
        final RNGValidator r = new RNGValidator();
        final VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver());
        final InputStream schema = this.getClass().getResourceAsStream("/domain.rng");
        if (null ==schema ){
            throw new IOException("couldn't get resource: /domain.rng");
        }
        StringWriter out = new StringWriter();
        try {
            r.validate(new InputSource(schema),
                       new InputSource("domain.xml"),
                       vr,
                       out);
            fail("Expected Error indicating no file");
        }
        catch (SAXParseException spe){
            assertEquals("External entity not found: \"http://www.sun.com/software/appserver/dtds/sun-domain_1_1.dtd\".", spe.getMessage());
        }
        
    }
    public void testacceptanceTestNoInputFile() throws Exception{
        final RNGValidator r = new RNGValidator();
        final VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver(new File ("sun-domain_1_1.dtd")));
        final InputStream schema = this.getClass().getResourceAsStream("/domain.rng");
        if (null ==schema ){
            throw new IOException("couldn't get resource: /domain.rng");
        }
        StringWriter out = new StringWriter();
        try {
            r.validate(new InputSource(schema),
                       new InputSource("domain.xml.notHere"),
                       vr,
                       out);
            fail("Expected Error indicating no file");
        }
        catch (IOException ioe){
            assertEquals("/opt/S1AS8/NewBuild/admin/validator/tests/com/sun/enterprise/config/serverbeans/validation/domain.xml.notHere (No such file or directory)", ioe.getMessage());
        }
    }
    public void testacceptanceTest() throws Exception{
        final RNGValidator r = new RNGValidator();
        final VariableResolver vr = new VariableResolver();
        vr.setEntityResolver(new MyEntityResolver(new File ("sun-domain_1_1.dtd")));
        final InputStream schema = this.getClass().getResourceAsStream("/domain.rng");
        if (null ==schema ){
            throw new IOException("couldn't get resource: /domain.rng");
        }
        StringWriter out = new StringWriter();
        r.validate(new InputSource(schema),
                   new InputSource("domain.xml"),
                   vr,
                   out);
        assertEquals("Error: URI=file:/opt/S1AS8/NewBuild/admin/validator/tests/com/sun/enterprise/config/serverbeans/validation/domain.xml Line=11: attribute \"load-order\" has a bad value: \"ABC\" does not satisfy the \"positiveInteger\" type\n", ""+out);
    }

    public void testEntityResolverWithError() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<empty/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";


        final String source ="<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE top PUBLIC '-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN' 'http://www.sun.com/software/appserver/dtds/sun-domain_1_1.dtd'><top/>";
        VariableResolver vr = new VariableResolver();
        StringWriter out = new StringWriter();
        
        vr.setEntityResolver(new MyEntityResolver());
        try {
            
            r.validate(new InputSource(new StringReader(schema)),
                       new InputSource(new StringReader(source)),
                       vr,
                       out);
            fail("Expected a SAX Exception indicating problems with the external entity");
        }
        catch (SAXParseException spe){
            assertEquals("External parameter entity \"%[dtd];\" has characters after markup.", spe.getMessage());
        }
    }
    public void testEntityResolver() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<empty/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";

        final String dtd = ""+
        "<?xml version='1.0' encoding='UTF-8'?>"+
        "<!ELEMENT top (bottom)>"+
        "<!ELEMENT bottom EMPTY>";

        final String source ="<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE top PUBLIC '-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN' 'http://www.sun.com/software/appserver/dtds/sun-domain_1_1.dtd'><top/>";
        VariableResolver vr = new VariableResolver();
        StringWriter out = new StringWriter();
        
        vr.setEntityResolver(new MyEntityResolver(dtd));
        r.validate(new InputSource(new StringReader(schema)),
                   new InputSource(new StringReader(source)),
                   vr,
                   out);
        assertEquals("", out+"");
    }
    public void testBasicWithSubstitutionError() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<ref name='system-property'/>"+
        "<attribute name='id'>"+
        "<value>VALUE</value>"+
        "</attribute>"+
        "</element>"+
        "</define>"+
        "<define name='system-property'>"+
        "<element name='system-property'>"+
        "<empty/>"+
        "<attribute name='name'/>"+
        "<attribute name='value'/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";

        final String source ="<?xml version='1.0'?> <top id='${var}'><system-property name='var' value='Not Expected Value'/></top>";
        StringWriter out = new StringWriter();
        VariableResolver vr = new VariableResolver();
        r.validate(new InputSource(new StringReader(schema)),
                   new InputSource(new StringReader(source)),
                   vr,
                   out);
        assertEquals("Error: URI=null Line=1: attribute \"id\" has a bad value\n", ""+out);
                     
    }
    public void testBasicWithSubstitution() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<ref name='system-property'/>"+
        "<attribute name='id'>"+
        "<value>VALUE</value>"+
        "</attribute>"+
        "</element>"+
        "</define>"+
        "<define name='system-property'>"+
        "<element name='system-property'>"+
        "<empty/>"+
        "<attribute name='name'/>"+
        "<attribute name='value'/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";

        final String source ="<?xml version='1.0'?> <top id='${var}'><system-property name='var' value='VALUE'/></top>";
        StringWriter out = new StringWriter();
        VariableResolver vr = new VariableResolver();
        r.validate(new InputSource(new StringReader(schema)),
                   new InputSource(new StringReader(source)),
                   vr,
                   out);
        assertEquals("", ""+out);
                     
    }

    public void testBasic() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<empty/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";

        final String source ="<?xml version='1.0'?> <top></top>";
        StringWriter out = new StringWriter();
        r.validate(new InputSource(new StringReader(schema)),
                   new InputSource(new StringReader(source)),
                   out);
        assertEquals("", ""+out);
                     
    }

    public void testBasicError() throws Exception {
        RNGValidator r = new RNGValidator();
        final String schema =""+
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"+
        "<grammar xmlns='http://relaxng.org/ns/structure/1.0'>"+
        "<define name='top'>"+
        "<element name='top'>"+
        "<empty/>"+
        "</element>"+
        "</define>"+
        "<start>"+
        "<ref name='top'/>"+
        "</start>"+
        "</grammar>";

        final String source ="<?xml version='1.0'?> <top><bottom/></top>";
        StringWriter out = new StringWriter();
        r.validate(new InputSource(new StringReader(schema)),
                   new InputSource(new StringReader(source)),
                   out);
        assertEquals("Error: URI=null Line=1: element \"bottom\" was found where no element may occur\n", ""+out);
                     
    }

    public RNGValidatorTest(String name){
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
            junit.textui.TestRunner.run(RNGValidatorTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new RNGValidatorTest(args[i]));
        }
        return ts;
    }

    private static class MyEntityResolver implements EntityResolver
    {
        String string;
        File file;

        MyEntityResolver(){}
        
        
        MyEntityResolver(String dtdSrc){
            string  = dtdSrc;
        }

        MyEntityResolver(File f) throws IOException {
            file = f;
        }
        
        
        public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)
            throws SAXException, java.io.IOException {
            if (null != string){
                return new InputSource(new StringReader(string));
            } else if (null != file) {
                return new InputSource(new FileReader(file));
            } else {
                return null;
            }
        }
    }


}
