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
package oracle.toplink.essentials.platform.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.xml.sax.SAXParseException;
import oracle.toplink.essentials.exceptions.TopLinkException;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;

public class XMLPlatformException extends TopLinkException {
    public static final int XML_PLATFORM_CLASS_NOT_FOUND = 27001;
    public static final int XML_PLATFORM_COULD_NOT_INSTANTIATE = 27002;
    public static final int XML_PLATFORM_COULD_NOT_CREATE_DOCUMENT = 27003;
    public static final int XML_PLATFORM_INVALID_XPATH = 27004;
    public static final int XML_PLATFORM_VALIDATION_EXCEPTION = 27005;
    public static final int XML_PLATFORM_PARSER_ERROR_RESOLVING_XML_SCHEMA = 27006;
    public static final int XML_PLATFORM_PARSE_EXCEPTION = 27101;
    public static final int XML_PLATFORM_PARSER_FILE_NOT_FOUND_EXCEPTION = 27102;
    public static final int XML_PLATFORM_PARSER_SAX_PARSE_EXCEPTION = 27103;
    public static final int XML_PLATFORM_TRANSFORM_EXCEPTION = 27201;
    public static final int XML_PLATFORM_INVALID_TYPE = 27202;

    protected XMLPlatformException(String message) {
        super(message);
    }

    public static XMLPlatformException xmlPlatformClassNotFound(String xmlPlatformClassName, Exception nestedException) {
        Object[] args = { xmlPlatformClassName };
        int errorCode = XML_PLATFORM_CLASS_NOT_FOUND;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformCouldNotInstantiate(String xmlPlatformClassName, Exception nestedException) {
        Object[] args = { xmlPlatformClassName };
        int errorCode = XML_PLATFORM_COULD_NOT_INSTANTIATE;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformCouldNotCreateDocument(Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_COULD_NOT_CREATE_DOCUMENT;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        return exception;
    }

    public static XMLPlatformException xmlPlatformInvalidXPath(Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_INVALID_XPATH;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformValidationException(Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_VALIDATION_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    /**
     * Takes an error messsage string
     */
    public static XMLPlatformException xmlPlatformValidationException(String errorMessage) {
        int errorCode = XML_PLATFORM_VALIDATION_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(errorMessage);
        exception.setErrorCode(errorCode);
        return exception;
    }

    /**
     * Handles an invalid type setting in a schema reference.
     *
     * @see oracle.toplink.essentials.platform.xml.XMLSchemaReference.getType()
     */
    public static XMLPlatformException xmlPlatformInvalidTypeException(int type) {
        Object[] args = { new Integer(type) };
        int errorCode = XML_PLATFORM_INVALID_TYPE;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        return exception;
    }

    public static XMLPlatformException xmlPlatformParseException(Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_PARSE_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformFileNotFoundException(File file, IOException nestedException) {
        Object[] args = { file.getAbsolutePath() };
        int errorCode = XML_PLATFORM_PARSER_FILE_NOT_FOUND_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformSAXParseException(SAXParseException nestedException) {
        Object[] args = { new Integer(nestedException.getLineNumber()), nestedException.getSystemId(), nestedException.getMessage() };
        int errorCode = XML_PLATFORM_PARSER_SAX_PARSE_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformErrorResolvingXMLSchema(URL url, Exception nestedException) {
        Object[] args = { url };
        int errorCode = XML_PLATFORM_PARSER_ERROR_RESOLVING_XML_SCHEMA;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformErrorResolvingXMLSchemas(Object[] schemas, Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_PARSER_ERROR_RESOLVING_XML_SCHEMA;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLPlatformException xmlPlatformTransformException(Exception nestedException) {
        Object[] args = {  };
        int errorCode = XML_PLATFORM_TRANSFORM_EXCEPTION;
        XMLPlatformException exception = new XMLPlatformException(ExceptionMessageGenerator.buildMessage(XMLPlatformException.class, errorCode, args));
        exception.setErrorCode(errorCode);
        exception.setInternalException(nestedException);
        return exception;
    }
}
