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

import org.xml.sax.InputSource;
import java.util.Properties;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXNotRecognizedException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;

class MakeCatalog extends DefaultHandler
{
    final String prefix;

    MakeCatalog(){
        prefix = "";
    }
    
    MakeCatalog(final String prefix){
        this.prefix = (prefix != null ? prefix +"." : "");
    }
    

    public static void main(String [] args) throws Exception {
        if (args.length < 2 || 3 < args.length ){
            useage();
            System.exit(1);
        }

        final String prefix = (args.length == 2 ? null : args[0]);
        final String in = (args.length == 2 ? args[0] : args[1]);
        final String out = (args.length == 2 ? args[1] : args[2]);
        
        final MakeCatalog mc = new MakeCatalog(prefix);
        final InputSource is = new InputSource(new FileReader(new File(in)));
        is.setSystemId(in);
        mc.makeCatalog(is);
        mc.getProperties().save(new FileOutputStream(new File(out)), null);
        System.exit(0);
    }

    private static void useage(){
        System.err.println("Useage: MakeCatalog [prefix] in out");
    }
    
        
    private static final String NAMESPACES="http://xml.org/sax/features/namespaces";
    private static final String PREFIXES="http://xml.org/sax/features/namespace-prefixes";
    private static final File SCHEMA=new File("./message-catalog.rng");
        
    void makeCatalog(InputSource is) throws ParserConfigurationException, SAXNotRecognizedException, SAXException, IOException {
//         final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParserFactory spf = new com.sun.msv.verifier.jaxp.SAXParserFactoryImpl();

        spf.setNamespaceAware(true);
        spf.setValidating(false);
        spf.setFeature(NAMESPACES, true);
        spf.setFeature(PREFIXES, false);

        final SAXParser p = spf.newSAXParser();
        p.setProperty("http://www.sun.com/xml/msv/schema", SCHEMA);        
//         spf.newSAXParser().parse(is, this);
        p.parse(is, this);
    }
    
    Properties getProperties(){
        return properties;
    }

    Properties properties;

    public void startDocument() throws SAXException{
        properties = new Properties();
    }

    public void startElement(final String uri,
                         final String localName,
                         final String qName,
                         final Attributes attributes)
        throws SAXException{
        if (null == localName){
            throw new SAXException("localName is null");
        }
        
        if (localName.equals("messages")){ // do nothing
        } else if (localName.equals("message")) {
            startMessage(attributes);
        } else {
            throw new SAXException("Unknown element: "+uri+":"+localName);
        }
    }

    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException{
        if (null != sb) {
            sb.append(ch, start, length);
        }
    }
    
    public void endElement(final String uri,
                           final String localName,
                           final String qName) throws SAXException {
        if (localName.equals("messages")){ // do nothing
        } else if (localName.equals("message")) {
            endMessage();
        } else {
            throw new SAXException("Unknown element: "+uri+":"+localName);
        }
    }


    private String key;
    private StringBuffer sb;
    
    private void startMessage(final Attributes attributes) throws SAXException {
        key = (prefix != null ? prefix : "") + attributes.getValue("", "id");
        if (null == key){
            throw new SAXException("message element had no id attribute");
        }
        sb = new StringBuffer();
    }

    private void endMessage() throws SAXException {
        if (null != sb){
            properties.setProperty(key, sb.toString().trim());
        }
        sb = null;
    }
}
