/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.platform.xml.jaxp;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import oracle.toplink.essentials.platform.xml.XMLPlatformException;
import oracle.toplink.essentials.platform.xml.XMLTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public class JAXPTransformer implements XMLTransformer {
    private boolean fragment;
    private static final String NO = "no";
    private static final String YES = "yes";
    private Transformer transformer;

    public JAXPTransformer() {
        super();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public String getEncoding() {
        return transformer.getOutputProperty(OutputKeys.ENCODING);
    }

    public void setEncoding(String encoding) {
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
    }

    public boolean isFormattedOutput() {
        return transformer.getOutputProperty(OutputKeys.INDENT).equals(YES);
    }

    public void setFormattedOutput(boolean shouldFormat) {
        if (shouldFormat) {
            transformer.setOutputProperty(OutputKeys.INDENT, YES);
        } else {
            transformer.setOutputProperty(OutputKeys.INDENT, NO);
        }
    }

    public String getVersion() {
        return transformer.getOutputProperty(OutputKeys.VERSION);
    }

    public void setVersion(String version) {
        transformer.setOutputProperty(OutputKeys.VERSION, version);
    }

    public void transform(Node sourceNode, OutputStream resultOutputStream) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        StreamResult result = new StreamResult(resultOutputStream);
        if (isFragment()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transform(source, result);
    }

    public void transform(Node sourceNode, ContentHandler resultContentHandler) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        SAXResult result = new SAXResult(resultContentHandler);

        transform(source, result);
    }

    public void transform(Node sourceNode, Result result) throws XMLPlatformException {
        DOMSource source = null;
        if ((isFragment()) && (result instanceof SAXResult)) {
            if (sourceNode instanceof Document) {
                source = new DOMSource(((Document)sourceNode).getDocumentElement());
            }
        } else {
            source = new DOMSource(sourceNode);
        }
        transform(source, result);
    }

    public void transform(Node sourceNode, Writer resultWriter) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        StreamResult result = new StreamResult(resultWriter);

        if (isFragment()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transform(source, result);
    }

    public void transform(Source source, Result result) throws XMLPlatformException {
        try {
            if ((result instanceof StreamResult) && (isFragment())) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public void transform(Document sourceDocument, Node resultParentNode, URL stylesheet) throws XMLPlatformException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            StreamSource stylesheetSource = new StreamSource(stylesheet.openStream());
            Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
            DOMSource source = new DOMSource(sourceDocument);
            DOMResult result = new DOMResult(resultParentNode);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public boolean isFragment() {
        return fragment;
    }
}
