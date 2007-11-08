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

import java.util.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <P><B>Purpose</B>: Conversion exceptions such as method or class not defined will raise this exception.
 */
public class ConversionException extends TopLinkException {
    protected Class classToConvertTo;
    protected transient Object sourceObject;
    public final static int COULD_NOT_BE_CONVERTED = 3001;
    public final static int COULD_NOT_BE_CONVERTED_EXTENDED = 3002;
    public final static int INCORRECT_DATE_FORMAT = 3003;
    public final static int INCORRECT_TIME_FORMAT = 3004;
    public final static int INCORRECT_TIMESTAMP_FORMAT = 3005;
    public final static int COULD_NOT_CONVERT_TO_BYTE_ARRAY = 3006;
    public final static int COULD_NOT_BE_CONVERTED_TO_CLASS = 3007;
    public final static int INCORRECT_DATE_TIME_FORMAT = 3008;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected ConversionException(String message, Object sourceObject, Class classToConvertTo, Exception exception) {
        super(message, exception);
        setSourceObject(sourceObject);
        setClassToConvertTo(classToConvertTo);
    }

    // Couldn't find a way of simply changing the message on an existing exception. 
    // therefore, create a new exception with appropriate message and port existing
    // info (stack trace and internal exception over)
    public static ConversionException couldNotBeConverted(Object mapping, Object descriptor, ConversionException exception) {
        Object sourceObject = exception.getSourceObject();
        Class javaClass = exception.getClassToConvertTo();
        Exception original = (Exception)exception.getInternalException();

        Object[] args = { sourceObject, sourceObject.getClass(), mapping, descriptor, javaClass };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, COULD_NOT_BE_CONVERTED_EXTENDED, args);
        ConversionException conversionException = new ConversionException(message, sourceObject, javaClass, original);
        conversionException.setStackTrace(exception.getStackTrace());
        conversionException.setErrorCode(COULD_NOT_BE_CONVERTED_EXTENDED);
        return conversionException;
    }

    public static ConversionException couldNotBeConverted(Object object, Class javaClass) {
        Object[] args = { object, object.getClass(), javaClass };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, COULD_NOT_BE_CONVERTED, args);
        ConversionException conversionException = new ConversionException(message, object, javaClass, null);
        conversionException.setErrorCode(COULD_NOT_BE_CONVERTED);
        return conversionException;
    }

    public static ConversionException couldNotBeConverted(Object object, Class javaClass, Exception exception) {
        Object[] args = { object, object.getClass(), javaClass };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, COULD_NOT_BE_CONVERTED, args);
        ConversionException conversionException = new ConversionException(message, object, javaClass, exception);
        conversionException.setErrorCode(COULD_NOT_BE_CONVERTED);
        return conversionException;
    }

    public static ConversionException couldNotBeConvertedToClass(Object object, Class javaClass, Exception exception) {
        Object[] args = { object, object.getClass(), javaClass };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, COULD_NOT_BE_CONVERTED_TO_CLASS, args);
        ConversionException conversionException = new ConversionException(message, object, javaClass, exception);
        conversionException.setErrorCode(COULD_NOT_BE_CONVERTED_TO_CLASS);
        return conversionException;
    }

    public static ConversionException couldNotConvertToByteArray(Object object) {
        Object[] args = { object };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, COULD_NOT_CONVERT_TO_BYTE_ARRAY, args);
        ConversionException conversionException = new ConversionException(message, object, byte[].class, null);
        conversionException.setErrorCode(COULD_NOT_CONVERT_TO_BYTE_ARRAY);
        return conversionException;
    }

    public static ConversionException incorrectDateFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, INCORRECT_DATE_FORMAT, args);
        ConversionException conversionException = new ConversionException(message, dateString, java.sql.Date.class, null);
        conversionException.setErrorCode(INCORRECT_DATE_FORMAT);
        return conversionException;
    }

    public static ConversionException incorrectTimeFormat(String timeString) {
        Object[] args = { timeString };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, INCORRECT_TIME_FORMAT, args);
        ConversionException conversionException = new ConversionException(message, timeString, java.sql.Time.class, null);
        conversionException.setErrorCode(INCORRECT_TIME_FORMAT);
        return conversionException;
    }

    public static ConversionException incorrectTimestampFormat(String timestampString) {
        Object[] args = { timestampString };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, INCORRECT_TIMESTAMP_FORMAT, args);
        ConversionException conversionException = new ConversionException(message, timestampString, java.sql.Timestamp.class, null);
        conversionException.setErrorCode(INCORRECT_TIMESTAMP_FORMAT);
        return conversionException;
    }

    public static ConversionException incorrectDateTimeFormat(String dateTimeString) {
        Object[] args = { dateTimeString };
        String message = ExceptionMessageGenerator.buildMessage(ConversionException.class, INCORRECT_DATE_TIME_FORMAT, args);
        ConversionException conversionException = new ConversionException(message, dateTimeString, Calendar.class, null);
        conversionException.setErrorCode(INCORRECT_DATE_TIME_FORMAT);
        return conversionException;
    }

    /**
     * PUBLIC:
     * Return the class to convert to.
     */
    public Class getClassToConvertTo() {
        return classToConvertTo;
    }

    /**
     * PUBLIC:
     * Return the object for which the problem was detected.
     */
    public Object getSourceObject() {
        return sourceObject;
    }

    /**
     * INTERNAL:
     * Set the class to convert to.
     */
    public void setClassToConvertTo(Class classToConvertTo) {
        this.classToConvertTo = classToConvertTo;
    }

    /**
     * INTERNAL:
     * Set the object for which the problem was detected.
     */
    public void setSourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
    }
}
