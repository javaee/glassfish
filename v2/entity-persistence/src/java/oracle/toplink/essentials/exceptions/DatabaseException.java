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

import java.io.StringWriter;
import java.sql.SQLException;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <P><B>Purpose</B>:
 * Wrapper for any database exception that occurred through TopLink.
 */
public class DatabaseException extends TopLinkException {
    protected SQLException exception;
    protected transient Call call;
    protected transient DatabaseQuery query;
    protected transient AbstractRecord queryArguments;
    protected transient Accessor accessor;
    public static final int SQL_EXCEPTION = 4002;
    public static final int CONFIGURATION_ERROR_CLASS_NOT_FOUND = 4003;
    public static final int DATABASE_ACCESSOR_NOT_CONNECTED = 4005;
    public static final int ERROR_READING_BLOB_DATA = 4006;
    public static final int COULD_NOT_CONVERT_OBJECT_TYPE = 4007;
    public static final int LOGOUT_WHILE_TRANSACTION_IN_PROGRESS = 4008;
    public static final int SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE = 4009;
    public static final int ERROR_PREALLOCATING_SEQUENCE_NUMBERS = 4011;
    public static final int CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK = 4014;
    public static final int SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME = 4015;
    public static final int CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION = 4016;
    public static final int CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION = 4017;
    public static final int TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER = 4018;
    public static final int ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION = 4019;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by the TopLink code.
     */
    protected DatabaseException(String message) {
        super(message);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by the TopLink code.
     */
    protected DatabaseException(SQLException exception) {
        super(exception.toString(), exception);
    }

    public static DatabaseException cannotRegisterSynchronizatonListenerForUnitOfWork(Exception e) {
        Object[] args = { e };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK, args));
        databaseException.setErrorCode(CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK);
        databaseException.setInternalException(e);
        return databaseException;
    }

    public static DatabaseException configurationErrorClassNotFound(String className) {
        Object[] args = { className };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_CLASS_NOT_FOUND, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_CLASS_NOT_FOUND);
        return databaseException;
    }

    public static DatabaseException configurationErrorNewInstanceIllegalAccessException(IllegalAccessException exception, Class javaClass) {
        Object[] args = { javaClass };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION);
        databaseException.setInternalException(exception);
        return databaseException;
    }

    public static DatabaseException configurationErrorNewInstanceInstantiationException(InstantiationException exception, Class javaClass) {
        Object[] args = { javaClass };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION);
        databaseException.setInternalException(exception);
        return databaseException;
    }

    public static DatabaseException couldNotConvertObjectType(int type) {
        Object[] args = { CR, new Integer(type) };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, COULD_NOT_CONVERT_OBJECT_TYPE, args));
        databaseException.setErrorCode(COULD_NOT_CONVERT_OBJECT_TYPE);
        return databaseException;
    }

    public static DatabaseException databaseAccessorNotConnected() {
        Object[] args = {  };
        String message = oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DatabaseException.class, DATABASE_ACCESSOR_NOT_CONNECTED, args);
        DatabaseException databaseException = new DatabaseException(message);
        databaseException.setErrorCode(DATABASE_ACCESSOR_NOT_CONNECTED);
        return databaseException;
    }

    public static DatabaseException databaseAccessorNotConnected(DatabaseAccessor databaseAccessor) {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, DATABASE_ACCESSOR_NOT_CONNECTED, args));
        databaseException.setErrorCode(DATABASE_ACCESSOR_NOT_CONNECTED);
        databaseException.setAccessor(databaseAccessor);
        return databaseException;
    }

    public static DatabaseException errorPreallocatingSequenceNumbers() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_PREALLOCATING_SEQUENCE_NUMBERS, args));
        databaseException.setErrorCode(ERROR_PREALLOCATING_SEQUENCE_NUMBERS);
        return databaseException;
    }

    public static DatabaseException errorReadingBlobData() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_READING_BLOB_DATA, args));
        databaseException.setErrorCode(ERROR_READING_BLOB_DATA);
        return databaseException;
    }

    /**
     *    PUBLIC:
     *    Return the accessor.
     */
    public Accessor getAccessor() {
        return accessor;
    }

    /**
     * PUBLIC:
     * This is the database error number.
     * Since it is possible to have no internal exception the errorCode will be zero in this case.
     */
    public int getDatabaseErrorCode() {
        if (getInternalException() == null) {
            return super.getErrorCode();
        }
        return ((SQLException)getInternalException()).getErrorCode();
    }

    /**
     * PUBLIC:
     * This is the database error message.
     */
    public String getMessage() {
        if (getInternalException() == null) {
            return super.getMessage();
        } else {
            StringWriter writer = new StringWriter();
            writer.write(super.getMessage());
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("ErrorCodeHeader"));
            if (getInternalException() instanceof SQLException) {
                writer.write(Integer.toString(((SQLException)getInternalException()).getErrorCode()));
            } else {
                writer.write("000");
            }
            if (getCall() != null) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("CallHeader"));
                if (getAccessor() != null) {
                    writer.write(getCall().getLogString(getAccessor()));
                } else {
                    writer.write(getCall().toString());
                }
            }
            if (getQuery() != null) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("QueryHeader"));
                try {
                    writer.write(getQuery().toString());
                } catch (RuntimeException badTooString) {
                }
            }
            return writer.toString();
        }
    }

    /**
     *    PUBLIC:
     *    This method returns the databaseQuery.
     *    DatabaseQuery is a visible class to the TopLink user.
     *    Users create an appropriate query by creating an instance
     *    of a concrete subclasses of DatabaseQuery.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     *    PUBLIC:
     *    Return the call that caused the exception.
     */
    public Call getCall() {
        return call;
    }

    /**
     *    INTERNAL:
     *    Set the call that caused the exception.
     */
    public void setCall(Call call) {
        this.call = call;
    }

    /**
     * PUBLIC:
     * Return the query argements used in the original query when exception is thrown
     */
    public Record getQueryArgumentsRecord() {
        return queryArguments;
    }

    public static DatabaseException logoutWhileTransactionInProgress() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, LOGOUT_WHILE_TRANSACTION_IN_PROGRESS, args));
        databaseException.setErrorCode(LOGOUT_WHILE_TRANSACTION_IN_PROGRESS);
        return databaseException;
    }

    public static DatabaseException sequenceTableInformationNotComplete() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE, args));
        databaseException.setErrorCode(SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE);
        return databaseException;
    }

    /**
     *    INTERNAL:
     *  Set the Accessor.
     */
    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    /**
     *    PUBLIC:
     *    This method set the databaseQuery.
     *    DatabaseQuery is a visible class to the TopLink user.
     *    Users create an appropriate query by creating an instance
     *    of a concrete subclasses of DatabaseQuery.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * PUBLIC:
     * Set the query argements used in the original query when exception is thrown
     */
    public void setQueryArguments(AbstractRecord queryArguments) {
        this.queryArguments = queryArguments;
    }

    public static DatabaseException sqlException(SQLException exception) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        return databaseException;
    }

    public static DatabaseException sqlException(SQLException exception, Accessor accessor, AbstractSession session) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        databaseException.setAccessor(accessor);
        databaseException.setSession(session);
        return databaseException;
    }

    public static DatabaseException sqlException(SQLException exception, Call call, Accessor accessor, AbstractSession session) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        databaseException.setAccessor(accessor);
        databaseException.setCall(call);
        return databaseException;
    }

    public static DatabaseException synchronizedUnitOfWorkDoesNotSupportCommitAndResume() {
        Object[] args = {  };

        String message = oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DatabaseException.class, SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME, args);
        DatabaseException databaseException = new DatabaseException(message);
        databaseException.setErrorCode(SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME);
        return databaseException;
    }

    public static DatabaseException transactionManagerNotSetForJTSDriver() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER, args));
        databaseException.setErrorCode(TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER);
        return databaseException;
    }
    
    public static DatabaseException errorRetrieveDbMetadataThroughJDBCConnection() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION, args));
        databaseException.setErrorCode(ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION);
        return databaseException;
    }
}
