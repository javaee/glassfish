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
import oracle.toplink.essentials.exceptions.i18n.*;

/**
 * <P><B>Purpose</B>: EJBQL parsing and resolution problems will raise this exception
 */
public class EJBQLException extends TopLinkException {
    public static final int recognitionException = 8001;
    public static final int generalParsingException = 8002;
    public static final int classNotFoundException = 8003;
    public static final int aliasResolutionException = 8004;
    public static final int resolutionClassNotFoundException = 8005;
    public static final int missingDescriptorException = 8006;
    public static final int missingMappingException = 8007;
    public static final int invalidContextKeyException = 8008;
    public static final int expressionNotSupported = 8009;
    public static final int generalParsingException2 = 8010;
    public static final int invalidCollectionMemberDecl = 8011;
    public static final int notYetImplemented = 8012;
    public static final int constructorClassNotFound = 8013;
    public static final int invalidSizeArgument = 8014;
    public static final int invalidEnumLiteral = 8015;
    public static final int invalidSelectForGroupByQuery = 8016;
    public static final int invalidHavingExpression = 8017;
    public static final int invalidMultipleUseOfSameParameter = 8018;
    public static final int multipleVariableDeclaration = 8019;
    public static final int invalidFunctionArgument = 8020;
    public static final int expectedOrderableOrderByItem = 8021;
    public static final int invalidExpressionArgument = 8022;
    public static final int syntaxError = 8023;
    public static final int syntaxErrorAt = 8024;
    public static final int unexpectedToken = 8025;
    public static final int unexpectedChar = 8026;
    public static final int expectedCharFound = 8027;
    public static final int unexpectedEOF = 8028;
    public static final int invalidNavigation = 8029;
    public static final int unknownAttribute = 8030;
    public static final int unsupportJoinArgument = 8031;
    public static final int invalidSetClauseTarget = 8032;
    public static final int invalidSetClauseNavigation = 8033;
    public static final int unknownAbstractSchemaType = 8034;
    public static final int invalidEnumEqualExpression = 8035;
    public static final int invalidCollectionNavigation = 8036;
    public static final int unknownAbstractSchemaType2 = 8037;
    public static final int resolutionClassNotFoundException2 = 8038;
    public Collection internalExceptions = null;

    /**
    * INTERNAL
    * Only TopLink can throw and create these excpetions
    */
    protected EJBQLException() {
        super();
    }

    /**
    * INTERNAL
    * Only TopLink can throw and create these excpetions
    */
    protected EJBQLException(String theMessage) {
        super(theMessage);
    }

    /**
    * INTERNAL
    * Only TopLink can throw and create these excpetions
    */
    protected EJBQLException(String message, Exception internalException) {
        super(message, internalException);
    }

    /**
    * INTERNAL
    * Only TopLink can throw and create these excpetions
    */
    protected EJBQLException(String message, Exception internalException, int theErrorCode) {
        this(message, internalException);
        this.setErrorCode(theErrorCode);
    }

    /**
     * INTERNAL
     * Create an exception to wrap the recognition exception thrown
     */
    public static EJBQLException recognitionException(String theEjbql, String theMessage) {
        Object[] args = { theEjbql, theMessage };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, recognitionException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(recognitionException);
        return exception;
    }

    /**
     * INTERNAL
     * Create an exception to wrap a general parsing exception
     */
    public static EJBQLException generalParsingException(String theEjbql, Exception theException) {
        Object[] args = { theEjbql, theException.getMessage() };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, generalParsingException, args);
        EJBQLException exception = new EJBQLException(message, theException, generalParsingException);
        exception.setErrorCode(generalParsingException);
        return exception;
    }

    /**
     * INTERNAL
     * Create an exception to wrap a general parsing exception
     */
    public static EJBQLException generalParsingException(String theEjbql) {
        Object[] args = { theEjbql };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, generalParsingException2, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(generalParsingException);
        return exception;
    }

    public static EJBQLException classNotFoundException(String theClassName, String theMessage, Exception theException) {
        Object[] args = { theClassName, theMessage };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, classNotFoundException, args);
        EJBQLException exception = new EJBQLException(message, theException, classNotFoundException);
        exception.setErrorCode(classNotFoundException);
        return exception;
    }

    public static EJBQLException resolutionClassNotFoundException(String query, String theClassName) {
        Object[] args = { query, theClassName };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, resolutionClassNotFoundException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(resolutionClassNotFoundException);
        return exception;
    }

    public static EJBQLException resolutionClassNotFoundException2(String query, int line, int column, String theClassName) {
        Object[] args = { query, line, column, theClassName };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, resolutionClassNotFoundException2, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(resolutionClassNotFoundException2);
        return exception;
    }

    public static EJBQLException missingDescriptorException(String query, String theClassName) {
        Object[] args = { query, theClassName };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, missingDescriptorException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(missingDescriptorException);

        return exception;
    }

    public static EJBQLException missingMappingException(String query, String theAttributeName) {
        Object[] args = { query, theAttributeName };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, missingMappingException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(missingMappingException);

        return exception;
    }

    public static EJBQLException aliasResolutionException(String query, int line, int column, String theAlias) {
        Object[] args = { query, line, column, theAlias };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, aliasResolutionException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(aliasResolutionException);
        return exception;
    }

    public static EJBQLException invalidContextKeyException(String query, String theKey) {
        Object[] args = { query, theKey };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, invalidContextKeyException, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidContextKeyException);
        return exception;
    }

    public static EJBQLException expressionNotSupported(String query, String unsupportedExpression) {
        Object[] args = { query, unsupportedExpression };

        String message = ExceptionMessageGenerator.buildMessage(EJBQLException.class, expressionNotSupported, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(expressionNotSupported);
        return exception;
    }

    public static EJBQLException invalidCollectionMemberDecl(String query, int line, int column, String attributeName) {
        Object[] args = { query, line, column, attributeName };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidCollectionMemberDecl, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidCollectionMemberDecl);
        return exception;
    }

    public static EJBQLException notYetImplemented(String query, String detail) {
        Object[] args = { query, detail };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, notYetImplemented, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(notYetImplemented);
        return exception;
    }

    public static EJBQLException constructorClassNotFound(String query, int line, int column, String className) {
        Object[] args = { query, line, column, className };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, constructorClassNotFound, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(constructorClassNotFound);
        return exception;
    }

    public static EJBQLException invalidSizeArgument(String query, int line, int column, String attributeName) {
        Object[] args = { query, line, column, attributeName };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidSizeArgument, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidSizeArgument);
        return exception;
    }

    public static EJBQLException invalidEnumLiteral(String query, int line, int column, String enumType, String literal) {
        Object[] args = { query, line, column, enumType, literal };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidEnumLiteral, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidEnumLiteral);
        return exception;
    }

    public static EJBQLException invalidSelectForGroupByQuery(String query, int line, int column, String select, String groupBy) {
        Object[] args = { query, line, column, select, groupBy };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidSelectForGroupByQuery, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidSelectForGroupByQuery);
        return exception;
    }

    public static EJBQLException invalidHavingExpression(String query, int line, int column, String having, String groupBy) {
        Object[] args = { query, line, column, having, groupBy };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidHavingExpression, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidHavingExpression);
        return exception;
    }

    public static EJBQLException invalidMultipleUseOfSameParameter(
        String query, int line, int column, String parameter, String oldType, String newType) {
        Object[] args = { query, line, column , parameter, oldType, newType };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidMultipleUseOfSameParameter, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidMultipleUseOfSameParameter);
        return exception;
    }

    public static EJBQLException multipleVariableDeclaration(
        String query, int line, int column, String variable, String oldDecl) {
        Object[] args = { query, line, column, variable, oldDecl };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, multipleVariableDeclaration, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(multipleVariableDeclaration);
        return exception;
    }

    public static EJBQLException invalidFunctionArgument(String query, int line, int column, String functionName, String attributeName, String type) {
        Object[] args = { query, line, column, functionName, attributeName, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidFunctionArgument, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidFunctionArgument);
        return exception;
    }

    public static EJBQLException invalidExpressionArgument(String query, int line, int column, String expression, String attributeName, String type) {
        Object[] args = { query, line, column, expression, attributeName, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidExpressionArgument, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidExpressionArgument);
        return exception;
    }

    public static EJBQLException unsupportJoinArgument(String query, int line, int column, String join, String type) {
        Object[] args = { query, line, column, join, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unsupportJoinArgument, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(unsupportJoinArgument);
        return exception;
    }

    public static EJBQLException expectedOrderableOrderByItem(String query, int line, int column, String item, String type) {
        Object[] args = { query, line, column, item, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, expectedOrderableOrderByItem, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(expectedOrderableOrderByItem);
        return exception;
    }

    public static EJBQLException syntaxError(String query, Exception ex) {
        Object[] args = { query };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, syntaxError, args);
        EJBQLException exception = new EJBQLException(message, ex);
        exception.setErrorCode(syntaxError);
        return exception;
    }

    public static EJBQLException syntaxErrorAt(String query, int line, int column, String token, Exception ex) {
        Object[] args = { query, line, column, token };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, syntaxErrorAt, args);
        EJBQLException exception = new EJBQLException(message, ex);
        exception.setErrorCode(syntaxErrorAt);
        return exception;
    }

    public static EJBQLException unexpectedToken(String query, int line, int column, String token, Exception ex) {
        Object[] args = { query, line, column, token };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unexpectedToken, args);
        EJBQLException exception = new EJBQLException(message, ex);
        exception.setErrorCode(unexpectedToken);
        return exception;
    }

    public static EJBQLException unexpectedChar(String query, int line, int column, String unexpected, Exception ex) {
        Object[] args = { query, line, column, unexpected };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unexpectedChar, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(unexpectedChar);
        return exception;
    }

    public static EJBQLException expectedCharFound(String query, int line, int column, String expected, String found, Exception ex) {
        Object[] args = { query, line, column, expected, found };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, expectedCharFound, args);
        EJBQLException exception = new EJBQLException(message, ex);
        exception.setErrorCode(expectedCharFound);
        return exception;
    }

    public static EJBQLException unexpectedEOF(String query, int line, int column, Exception ex) {
        Object[] args = { query, line, column};

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unexpectedEOF, args);
        EJBQLException exception = new EJBQLException(message, ex);
        exception.setErrorCode(unexpectedEOF);
        return exception;
    }

    public static EJBQLException invalidNavigation(
        String query, int line, int column, String expr, String lhs, String type) {
        Object[] args = { query, line, column, expr, lhs, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidNavigation, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidNavigation);
        return exception;
    }

    public static EJBQLException invalidCollectionNavigation(
        String query, int line, int column, String expr, String attribute) {
        Object[] args = { query, line, column, expr, attribute };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidCollectionNavigation, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidCollectionNavigation);
        return exception;
    }

    public static EJBQLException invalidSetClauseTarget(
        String query, int line, int column, String expr, String attribute) {
        Object[] args = { query, line, column, attribute, expr };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidSetClauseTarget, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidSetClauseTarget);
        return exception;
    }

    public static EJBQLException invalidSetClauseNavigation(
        String query, int line, int column, String expr, String relationship) {
        Object[] args = { query, line, column, expr, relationship };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidSetClauseNavigation, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidSetClauseNavigation);
        return exception;
    }

    public static EJBQLException unknownAttribute(
        String query, int line, int column, String attribute, String type) {
        Object[] args = { query, line, column, attribute, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unknownAttribute, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(unknownAttribute);
        return exception;
    }
    
    public static EJBQLException invalidEnumEqualExpression(String query, int line, int column, String enumType, String type) {
        Object[] args = { query, line, column, enumType, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, invalidEnumEqualExpression, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(invalidEnumEqualExpression);
        return exception;
    }

    public static EJBQLException unknownAbstractSchemaType(String query, String type) {
        Object[] args = { query, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unknownAbstractSchemaType, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(unknownAbstractSchemaType);
        return exception;
    }

    public static EJBQLException unknownAbstractSchemaType2(String query, int line, int column, String type) {
        Object[] args = { query, line, column, type };

        String message = ExceptionMessageGenerator.buildMessage(
            EJBQLException.class, unknownAbstractSchemaType2, args);
        EJBQLException exception = new EJBQLException(message);
        exception.setErrorCode(unknownAbstractSchemaType2);
        return exception;
    }

    /**
     * INTERNAL
     * Add an internal Exception to the collection of
     * internal Exceptions
     */
    public Object addInternalException(Object theException) {
        getInternalExceptions().add(theException);
        return theException;
    }

    /**
     * INTERNAL
     * Does this exception have any internal errors?
     */
    public boolean hasInternalExceptions() {
        return !getInternalExceptions().isEmpty();
    }

    /**
     * INTERNAL
     * Return the collection of internal Exceptions.
     * Intialize if there are no exceptions
     */
    public Collection getInternalExceptions() {
        if (internalExceptions == null) {
            setInternalExceptions(new Vector());
        }
        return internalExceptions;
    }

    /**
     * INTERNAL
     * Store the exceptions related to this exception
     */
    public void setInternalExceptions(Collection theExceptions) {
        internalExceptions = theExceptions;
    }

    /**
     * PUBLIC
     * Print the stack trace for each error generated by the
     * parser. This method is intended to assist in debugging
     * problems in EJBQL
     */
    public void printFullStackTrace() {
        if (hasInternalExceptions()) {
            Iterator exceptions = getInternalExceptions().iterator();
            while (exceptions.hasNext()) {
                Throwable error = (Throwable)exceptions.next();
                error.printStackTrace();
            }
        }
    }
}
