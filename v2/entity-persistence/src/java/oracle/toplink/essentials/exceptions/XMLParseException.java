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
package oracle.toplink.essentials.exceptions;

import java.io.File;
import java.net.URL;

import oracle.toplink.essentials.exceptions.i18n.*;

public class XMLParseException extends TopLinkException {

    public static final int EXCEPTION_CREATING_DOCUMENT_BUILDER = 34000;
    public static final int EXCEPTION_READING_XML_DOCUMENT = 34001;
    public static final int EXCEPTION_CREATING_SAX_PARSER = 34002;
    public static final int EXCEPTION_CREATING_XML_READER = 34003;
    public static final int EXCEPTION_SETTING_SCHEMA_SOURCE = 34004;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    public XMLParseException() {
        super();
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected XMLParseException(String message) {
        super(message);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected XMLParseException(String message, Throwable internalException) {
        super(message);
        setInternalException(internalException);
    }

	/**
	 * INTERNAL:
	 */
    public static XMLParseException exceptionCreatingDocumentBuilder(String xmlDocument, Exception cause) {
    	return XMLParseException.getXMLParseException(new Object[] {xmlDocument}, cause, EXCEPTION_CREATING_DOCUMENT_BUILDER);
    }

	/**
	 * INTERNAL:
	 */
    public static XMLParseException exceptionCreatingSAXParser(URL url, Exception cause) {
    	return XMLParseException.getXMLParseException(new Object[] {url}, cause, EXCEPTION_CREATING_SAX_PARSER);
    }

	/**
	 * INTERNAL:
	 */
    public static XMLParseException exceptionCreatingXMLReader(URL url, Exception cause) {
    	return XMLParseException.getXMLParseException(new Object[] {url}, cause, EXCEPTION_CREATING_XML_READER);
    }

    /**
	 * INTERNAL:
	 */
    public static XMLParseException exceptionReadingXMLDocument(String xmlDocument, Exception cause) {
    	return XMLParseException.getXMLParseException(new Object[] {xmlDocument}, cause, EXCEPTION_READING_XML_DOCUMENT);
    }

	/**
	 * INTERNAL:
	 */
    public static XMLParseException exceptionSettingSchemaSource(URL baseUrl, URL schemaUrl, Exception cause) {
    	return XMLParseException.getXMLParseException(new Object[] {baseUrl, schemaUrl}, cause, EXCEPTION_SETTING_SCHEMA_SOURCE);
    }

    /*
     * INTERNAL: 
     */
    private static XMLParseException getXMLParseException(Object[] args, Exception cause, int errorCode) {
        XMLParseException parseException = new XMLParseException(ExceptionMessageGenerator.buildMessage(XMLParseException.class, errorCode, args), cause);
        parseException.setErrorCode(errorCode);
        return parseException;
    }
}
