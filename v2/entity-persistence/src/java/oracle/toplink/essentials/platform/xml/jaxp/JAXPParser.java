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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import oracle.toplink.essentials.platform.xml.XMLParser;
import oracle.toplink.essentials.platform.xml.XMLPlatformException;

public class JAXPParser implements XMLParser {
    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private DocumentBuilderFactory documentBuilderFactory;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;

    public JAXPParser() {
        super();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        setNamespaceAware(true);
        setWhitespacePreserving(false);
    }

    public void setNamespaceAware(boolean isNamespaceAware) {
        documentBuilderFactory.setNamespaceAware(isNamespaceAware);
    }

    public void setWhitespacePreserving(boolean isWhitespacePreserving) {
        documentBuilderFactory.setIgnoringElementContentWhitespace(!isWhitespacePreserving);
    }

    public int getValidationMode() {
        if (!documentBuilderFactory.isValidating()) {
            return XMLParser.NONVALIDATING;
        }

        try {
            if (null == documentBuilderFactory.getAttribute(SCHEMA_LANGUAGE)) {
                return XMLParser.DTD_VALIDATION;
            }
        } catch (IllegalArgumentException e) {
            return XMLParser.DTD_VALIDATION;
        }

        return XMLParser.SCHEMA_VALIDATION;
    }

    public void setValidationMode(int validationMode) {
        switch (validationMode) {
        case XMLParser.NONVALIDATING: {
            documentBuilderFactory.setValidating(false);
            // documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, null);			
            return;
        }
        case XMLParser.DTD_VALIDATION: {
            documentBuilderFactory.setValidating(true);
            // documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, null);
            return;
        }
        case XMLParser.SCHEMA_VALIDATION: {
            try {
                documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
                documentBuilderFactory.setValidating(true);
            } catch (IllegalArgumentException e) {
                // This parser does not support XML Schema validation so leave it as
                // a non-validating parser.
            }
            return;
        }
        }
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setXMLSchema(URL url) throws XMLPlatformException {
        if (null == url) {
            return;
        }
        try {
            documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
            documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, url.toString());
        } catch (IllegalArgumentException e) {
            // The attribute isn't supported so do nothing
        } catch (Exception e) {
            XMLPlatformException.xmlPlatformErrorResolvingXMLSchema(url, e);
        }
    }

    public void setXMLSchemas(Object[] schemas) throws XMLPlatformException {
        if ((null == schemas) || (schemas.length == 0)) {
            return;
        }
        try {
            documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
            documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
        } catch (IllegalArgumentException e) {
            // The attribute isn't supported so do nothing
        } catch (Exception e) {
            XMLPlatformException.xmlPlatformErrorResolvingXMLSchemas(schemas, e);
        }
    }

    public Document parse(InputSource inputSource) throws XMLPlatformException {
        try {
            return getDocumentBuilder().parse(inputSource);
        } catch (SAXException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        } catch (IOException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        }
    }

    public Document parse(File file) throws XMLPlatformException {
        try {
            return getDocumentBuilder().parse(file);
        } catch (SAXParseException e) {
            throw XMLPlatformException.xmlPlatformSAXParseException(e);
        } catch (SAXException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        } catch (IOException e) {
            throw XMLPlatformException.xmlPlatformFileNotFoundException(file, e);
        }
    }

    public Document parse(InputStream inputStream) throws XMLPlatformException {
        try {
            return getDocumentBuilder().parse(inputStream);
        } catch (SAXParseException e) {
            throw XMLPlatformException.xmlPlatformSAXParseException(e);
        } catch (SAXException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        } catch (IOException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        }
    }

    public Document parse(Reader reader) throws XMLPlatformException {
        InputSource inputSource = new InputSource(reader);
        return parse(inputSource);
    }

    public Document parse(Source source) throws XMLPlatformException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            return (Document)domResult.getNode();
        } catch (TransformerException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        }
    }

    public Document parse(URL url) throws XMLPlatformException {
        try {
            InputStream inputStream = url.openStream();
            return parse(inputStream);
        } catch (IOException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(entityResolver);
            documentBuilder.setErrorHandler(errorHandler);
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw XMLPlatformException.xmlPlatformParseException(e);
        }
    }
}
