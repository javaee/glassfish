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

import java.util.Vector;
import java.lang.reflect.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.sessions.Session;

/**
 * <p><b>Purpose</b>: This exception is used if incorrect state or method arguments are detected
 * in a general TopLink object.
 */
public class ValidationException extends TopLinkException {
    public static final int LOGIN_BEFORE_ALLOCATING_CLIENT_SESSIONS = 7001;
    public static final int POOL_NAME_DOES_NOT_EXIST = 7002;
    public static final int MAX_SIZE_LESS_THAN_MIN_SIZE = 7003;
    public static final int POOLS_MUST_BE_CONFIGURED_BEFORE_LOGIN = 7004;
    public static final int JAVA_TYPE_IS_NOT_A_VALID_DATABASE_TYPE = 7008;
    public static final int MISSING_DESCRIPTOR = 7009;
    public static final int START_INDEX_OUT_OF_RANGE = 7010;
    public static final int STOP_INDEX_OUT_OF_RANGE = 7011;
    public static final int FATAL_ERROR_OCCURRED = 7012;
    public static final int NO_PROPERTIES_FILE_FOUND = 7013;
    public static final int CHILD_DESCRIPTORS_DO_NOT_HAVE_IDENTITY_MAP = 7017;
    public static final int FILE_ERROR = 7018;
    public static final int INCORRECT_LOGIN_INSTANCE_PROVIDED = 7023;
    public static final int INVALID_MERGE_POLICY = 7024;
    public static final int ONLY_FIELDS_ARE_VALID_KEYS_FOR_DATABASE_ROWS = 7025;
    public static final int SEQUENCE_SETUP_INCORRECTLY = 7027;
    public static final int WRITE_OBJECT_NOT_ALLOWED_IN_UNIT_OF_WORK = 7028;
    public static final int CANNOT_SET_READ_POOL_SIZE_AFTER_LOGIN = 7030;
    public static final int CANNOT_ADD_DESCRIPTORS_TO_SESSION_BROKER = 7031;
    public static final int NO_SESSION_REGISTERED_FOR_CLASS = 7032;
    public static final int NO_SESSION_REGISTERED_FOR_NAME = 7033;
    public static final int CANNOT_ADD_DESCRIPTORS_TO_SESSION = 7034;
    public static final int CANNOT_LOGIN_TO_A_SESSION = 7035;
    public static final int CANNOT_LOGOUT_OF_A_SESSION = 7036;
    public static final int CANNOT_MODIFY_SCHEMA_IN_SESSION = 7037;
    public static final int LOG_IO_ERROR = 7038;
    public static final int CANNOT_REMOVE_FROM_READ_ONLY_CLASSES_IN_NESTED_UNIT_OF_WORK = 7039;
    public static final int CANNOT_MODIFY_READ_ONLY_CLASSES_SET_AFTER_USING_UNIT_OF_WORK = 7040;
    public static final int INVALID_READ_ONLY_CLASS_STRUCTURE_IN_UNIT_OF_WORK = 7041;
    public static final int PLATFORM_CLASS_NOT_FOUND = 7042;
    public static final int NO_TABLES_TO_CREATE = 7043;
    public static final int ILLEGAL_CONTAINER_CLASS = 7044;
    public static final int CONTAINER_POLICY_DOES_NOT_USE_KEYS = 7047;
    public static final int MAP_KEY_NOT_DECLARED_IN_ITEM_CLASS = 7048;
    public static final int MISSING_MAPPING = 7051;
    public static final int ILLEGAL_USE_OF_MAP_IN_DIRECTCOLLECTION = 7052;
    public static final int CANNOT_RELEASE_NON_CLIENTSESSION = 7053;
    public static final int CANNOT_ACQUIRE_CLIENTSESSION_FROM_SESSION = 7054;
    public static final int OPTIMISTIC_LOCKING_NOT_SUPPORTED = 7055;
    public static final int WRONG_OBJECT_REGISTERED = 7056;
    public static final int KEYS_MUST_MATCH = 7057;
    public static final int INVALID_CONNECTOR = 7058;
    public static final int INVALID_DATA_SOURCE_NAME = 7059;
    public static final int CANNOT_ACQUIRE_DATA_SOURCE = 7060;
    public static final int JTS_EXCEPTION_RAISED = 7061;
    public static final int FIELD_LEVEL_LOCKING_NOTSUPPORTED_OUTSIDE_A_UNIT_OF_WORK = 7062;
    public static final int EJB_CONTAINER_EXCEPTION_RAISED = 7063;
    public static final int EJB_PRIMARY_KEY_REFLECTION_EXCEPTION = 7064;
    public static final int EJB_CANNOT_LOAD_REMOTE_CLASS = 7065;
    public static final int EJB_MUST_BE_IN_TRANSACTION = 7066;
    public static final int EJB_INVALID_PROJECT_CLASS = 7068;
    public static final int PROJECT_AMENDMENT_EXCEPTION_OCCURED = 7069;
    public static final int EJB_TOPLINK_PROPERTIES_NOT_FOUND = 7070;
    public static final int CANT_HAVE_UNBOUND_IN_OUTPUT_ARGUMENTS = 7071;
    public static final int EJB_INVALID_PLATFORM_CLASS = 7072;
    public static final int ORACLE_OBJECT_TYPE_NOT_DEFINED = 7073;
    public static final int ORACLE_OBJECT_TYPE_NAME_NOT_DEFINED = 7074;
    public static final int ORACLE_VARRAY_MAXIMIM_SIZE_NOT_DEFINED = 7075;
    public static final int DESCRIPTOR_MUST_NOT_BE_INITIALIZED = 7076;
    public static final int EJB_INVALID_FINDER_ON_HOME = 7077;
    public static final int EJB_NO_SUCH_SESSION_SPECIFIED_IN_PROPERTIES = 7078;
    public static final int EJB_DESCRIPTOR_NOT_FOUND_IN_SESSION = 7079;
    public static final int EJB_FINDER_EXCEPTION = 7080;
    public static final int CANNOT_REGISTER_AGGREGATE_OBJECT_IN_UNIT_OF_WORK = 7081;
    public static final int MULTIPLE_PROJECTS_SPECIFIED_IN_PROPERTIES = 7082;
    public static final int NO_PROJECT_SPECIFIED_IN_PROPERTIES = 7083;
    public final static int INVALID_FILE_TYPE = 7084;
    public final static int SUB_SESSION_NOT_DEFINED_FOR_BROKER = 7085;
    public final static int EJB_INVALID_SESSION_TYPE_CLASS = 7086;
    public final static int EJB_SESSION_TYPE_CLASS_NOT_FOUND = 7087;
    public final static int CANNOT_CREATE_EXTERNAL_TRANSACTION_CONTROLLER = 7088;
    public final static int SESSION_AMENDMENT_EXCEPTION_OCCURED = 7089;
    public final static int SET_LISTENER_CLASSES_EXCEPTION = 7091;
    public final static int EXISTING_QUERY_TYPE_CONFLICT = 7092;
    public final static int QUERY_ARGUMENT_TYPE_NOT_FOUND = 7093;
    public final static int ERROR_IN_SESSION_XML = 7094;
    public final static int NO_SESSIONS_XML_FOUND = 7095;
    public final static int CANNOT_COMMIT_UOW_AGAIN = 7096;
    public static final int OPERATION_NOT_SUPPORTED = 7097;
    public static final int PROJECT_XML_NOT_FOUND = 7099;
    public static final int NO_SESSION_FOUND = 7100;
    public static final int NO_TOPLINK_EJB_JAR_XML_FOUND = 7101;
    public static final int NULL_CACHE_KEY_FOUND_ON_REMOVAL = 7102;
    public static final int NULL_UNDERLYING_VALUEHOLDER_VALUE = 7103;
    public static final int INVALID_SEQUENCING_LOGIN = 7104;

    // Security error codes
    public static final int INVALID_ENCRYPTION_CLASS = 7105;
    public static final int ERROR_ENCRYPTING_PASSWORD = 7106;
    public static final int ERROR_DECRYPTING_PASSWORD = 7107;
    public static final int NOT_SUPPORTED_FOR_DATASOURCE = 7108;
    public static final int PROJECT_LOGIN_IS_NULL = 7109;

    // for flashback:
    public static final int HISTORICAL_SESSION_ONLY_SUPPORTED_ON_ORACLE = 7110;
    public static final int CANNOT_ACQUIRE_HISTORICAL_SESSION = 7111;
    public static final int FEATURE_NOT_SUPPORTED_IN_JDK_VERSION = 7112;
    public static final int PLATFORM_DOES_NOT_SUPPORT_CALL_WITH_RETURNING = 7113;
    public static final int ISOLATED_DATA_NOT_SUPPORTED_IN_CLIENTSESSIONBROKER = 7114;
    public static final int CLIENT_SESSION_CANNOT_USE_EXCLUSIVE_CONNECTION = 7115;

    // general validation method's arguments
    public static final int INVALID_METHOD_ARGUMENTS = 7116;

    // customSQL and stored functiuons
    public static final int MULTIPLE_CURSORS_NOT_SUPPORTED = 7117;
    public static final int WRONG_USAGE_OF_SET_CUSTOM_SQL_ARGUMENT_TYPE_METOD = 7118;
    public static final int CANNOT_TRANSLATE_UNPREPARED_CALL = 7119;
    public static final int CANNOT_SET_CURSOR_FOR_PARAMETER_TYPE_OTHER_THAN_OUT = 7120;
    public static final int PLATFORM_DOES_NOT_SUPPORT_STORED_FUNCTIONS = 7121;
    public static final int EXCLUSIVE_CONNECTION_NO_LONGER_AVAILABLE = 7122;

    // From UnitOfWork writeChanges() feature
    public static final int UNIT_OF_WORK_IN_TRANSACTION_COMMIT_PENDING = 7123;
    public static final int UNIT_OF_WORK_AFTER_WRITE_CHANGES_FAILED = 7124;
    public static final int INACTIVE_UNIT_OF_WORK = 7125;
    public static final int CANNOT_WRITE_CHANGES_ON_NESTED_UNIT_OF_WORK = 7126;
    public static final int CANNOT_WRITE_CHANGES_TWICE = 7127;
    public static final int ALREADY_LOGGED_IN = 7128;

    // general validation method's arguments
    public static final int INVALID_NULL_METHOD_ARGUMENTS = 7129;
    public static final int NESTED_UOW_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING = 7130;
    public static final int WRONG_COLLECTION_CHANGE_EVENT_TYPE = 7131;
    public static final int WRONG_CHANGE_EVENT = 7132;
    public static final int OLD_COMMIT_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING = 7133;

    //ServerPlatform exceptions
    public static final int SERVER_PLATFORM_IS_READ_ONLY_AFTER_LOGIN = 7134;
    public static final int CANNOT_COMMIT_AND_RESUME_UOW_WITH_MODIFY_ALL_QUERIES = 7135;
    public static final int NESTED_UOW_NOT_SUPPORTED_FOR_MODIFY_ALL_QUERY = 7136;

    //fetch group
    public final static int UNFETCHED_ATTRIBUTE_NOT_EDITABLE = 7137;
    public final static int OBJECT_NEED_IMPL_TRACKER_FOR_FETCH_GROUP_USAGE = 7138;
    public static final int MODIFY_ALL_QUERIES_NOT_SUPPORTED_WITH_OTHER_WRITES = 7139;

    // Multiple sequences exceptions
    public static final int WRONG_SEQUENCE_TYPE = 7140;
    public static final int CANNOT_SET_DEFAULT_SEQUENCE_AS_DEFAULT = 7141;
    public static final int DEFAULT_SEQUENCE_NAME_ALREADY_USED_BY_SEQUENCE = 7142;
    public static final int SEQUENCE_NAME_ALREADY_USED_BY_DEFAULT_SEQUENCE = 7143;
    public static final int PLATFORM_DOES_NOT_SUPPORT_SEQUENCE = 7144;
    public static final int SEQUENCE_CANNOT_BE_CONNECTED_TO_TWO_PLATFORMS = 7145;
    public static final int QUERY_SEQUENCE_DOES_NOT_HAVE_SELECT_QUERY = 7146;
    public static final int CREATE_PLATFORM_DEFAULT_SEQUENCE_UNDEFINED = 7147;
    public static final int CANNOT_RESUME_SYNCHRONIZED_UOW = 7148;
    
    // EJB annotation processing validation exceptions
    public static final int INVALID_COMPOSITE_PK_ATTRIBUTE = 7149;
    public static final int INVALID_COMPOSITE_PK_SPECIFICATION = 7150;
    public static final int INVALID_TYPE_FOR_ENUMERATED_ATTRIBUTE = 7151;
    public static final int TABLE_PER_CLASS_INHERITANCE_NOT_SUPPORTED = 7152;
    public static final int MAPPING_ANNOTATIONS_APPLIED_TO_TRANSIENT_ATTRIBUTE = 7153;
    public static final int NO_MAPPED_BY_ATTRIBUTE_FOUND = 7154;
    public static final int INVALID_TYPE_FOR_SERIALIZED_ATTRIBUTE = 7155;
    public static final int UNABLE_TO_LOAD_CLASS = 7156;
    public static final int INVALID_COLUMN_ANNOTATION_ON_RELATIONSHIP = 7157;
    public static final int ERROR_PROCESSING_NAMED_QUERY_ANNOTATION = 7158;
    public static final int COULD_NOT_FIND_MAP_KEY = 7159;
    public static final int UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ANNOTATIONS = 7160;
    public static final int NO_PK_ANNOTATIONS_FOUND = 7161;
    public static final int MULTIPLE_EMBEDDED_ID_ANNOTATIONS_FOUND = 7162;
    public static final int EMBEDDED_ID_AND_ID_ANNOTATIONS_FOUND = 7163;
    public static final int INVALID_TYPE_FOR_LOB_ATTRIBUTE = 7164;
    public static final int INVALID_TYPE_FOR_TEMPORAL_ATTRIBUTE = 7165;
    public static final int TABLE_GENERATOR_RESERVED_NAME = 7166;
    public static final int SEQUENCE_GENERATOR_RESERVED_NAME = 7167;
    public static final int INVALID_TYPE_FOR_VERSION_ATTRIBUTE = 7168;
    public static final int ONLY_ONE_GENERATED_VALURE_IS_ALLOWED = 7169;
    public static final int ERROR_INSTANTIATING_CLASS = 7172;

	// Change Tracking
    public static final int WRONG_PROPERTY_NAME_IN_CHANGE_EVENT = 7173;
    
    // Added for BUG 4349991
    public static final int NO_CORRESPONDING_SETTER_METHOD_DEFINED = 7174;
    
    // Added for Cascaded Optimistic Locking support
    public static final int UNSUPPORTED_CASCADE_LOCKING_MAPPING = 7175;
    public static final int UNSUPPORTED_CASCADE_LOCKING_MAPPING_WITH_CUSTOM_QUERY = 7176;
    public static final int UNSUPPORTED_CASCADE_LOCKING_DESCRIPTOR = 7177;

    // Added for Proxy Authentication
    public static final int ORACLEOCIPROXYCONNECTOR_REQUIRES_ORACLEOCICONNECTIONPOOL = 7178;
    public static final int ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION = 7179;
    public static final int ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION_VERSION = 7180;
    public static final int ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_INT_PROXYTYPE = 7181;

    // EJB 3.0
    public static final int COULD_NOT_FIND_DRIVER_CLASS = 7182;
    public static final int ERROR_CLOSING_PERSISTENCE_XML = 7183;
    public static final int CONFIG_FACTORY_NAME_PROPERTY_NOT_SPECIFIED = 7184;
    public static final int CONFIG_FACTORY_NAME_PROPERTY_NOT_FOUND = 7185;
    public static final int CANNOT_INVOKE_METHOD_ON_CONFIG_CLASS = 7186;
    public static final int CONFIG_METHOD_NOT_DEFINED = 7187;
    public static final int CLASS_LIST_MUST_NOT_BE_NULL = 7188;
    public static final int CURRENT_LOADER_NOT_VALID = 7189;
    public static final int METHOD_FAILED = 7190;
    public static final int ENTITY_CLASS_NOT_FOUND = 7191;
    public static final int CLASS_FILE_TRANSFORMER_THROWS_EXCEPTION = 7192;
    public static final int JAR_FILES_IN_PERSISTENCE_XML_NOT_SUPPORTED = 7193;
    public static final int COULD_NOT_BIND_JNDI = 7194;
    public static final int EXCEPTION_CONFIGURING_EM_FACTORY = 7195;
    public static final int CANNOT_CAST_TO_CLASS = 7196;
    public static final int NOT_SUPPORTED = 7197;
    public static final int CLASS_NOT_FOUND_WHILE_CONVERTING_CLASSNAMES = 7198;
    public static final int PRIMARY_TABLE_NOT_DEFINED_FOR_RELATIONSHIP = 7199;    
    public static final int INVALID_EMBEDDABLE_ATTRIBUTE = 7200;
    public static final int INVALID_ENTITY_MAPPINGS_DOCUMENT = 7201;    
    public static final int INVALID_ATTRIBUTE_OVERRIDE_NAME = 7202;    
    public static final int INVALID_COLLECTION_TYPE_FOR_RELATIONSHIP = 7203;
    public static final int MULTIPLE_EMBEDDED_ID_ELEMENTS_FOUND = 7205;
    public static final int EMBEDDED_ID_AND_ID_ELEMENTS_FOUND = 7206;
    public static final int INVALID_CLASS_TYPE_FOR_BLOB_ATTRIBUTE = 7207;
    public static final int INVALID_CLASS_TYPE_FOR_CLOB_ATTRIBUTE = 7208;
    public static final int UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ELEMENTS = 7209;
    public static final int ERROR_PROCESSING_NAMED_QUERY_ELEMENT = 7210;
    public static final int INVALID_COLUMN_ELEMENT_ON_RELATIONSHIP = 7211;
    public static final int NO_TEMPORAL_TYPE_SPECIFIED = 7212;
    public static final int CIRCULAR_MAPPED_BY_REFERENCES = 7213;
    public static final int UNABLE_TO_DETERMINE_TARGET_ENTITY = 7214;
    public static final int UNABLE_TO_DETERMINE_CLASS_FOR_FIELD = 7215;
    public static final int UNABLE_TO_DETERMINE_CLASS_FOR_PROPERTY = 7216;
    public static final int INVALID_ORDER_BY_VALUE = 7217;
    public static final int PLATFORM_DOES_NOT_OVERRIDE_GETCREATETEMPTABLESQLPREFIX = 7218;
    public static final int MAPPING_DOES_NOT_OVERRIDE_VALUEFROMROWINTERNALWITHJOIN = 7219;
    
    // EJB 3.0 JoinColumn(s) and PrimaryKeyJoinColumn(s) validation
    public static final int INCOMPLETE_JOIN_COLUMNS_SPECIFIED = 7220;
    public static final int EXCESSIVE_JOIN_COLUMNS_SPECIFIED = 7221;
    public static final int INCOMPLETE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED = 7222;
    public static final int EXCESSIVE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED = 7223;
    
    // EJB 3.0 Callback validation
    public static final int INVALID_CALLBACK_METHOD = 7224;
    public static final int INVALID_CALLBACK_METHOD_NAME = 7225;
    public static final int INVALID_CALLBACK_METHOD_MODIFIER = 7226;
    public static final int MULTIPLE_CALLBACK_METHODS_DEFINED = 7227;
    public static final int INVALID_ENTITY_CALLBACK_METHOD_ARGUMENTS = 7228;
    public static final int INVALID_ENTITY_LISTENER_CALLBACK_METHOD_ARGUMENTS = 7229;

    public static final int PERSISTENCE_UNIT_METADATA_CONFLICT = 7230;
    public static final int CANNOT_PERSIST_MANAGED_OBJECT = 7231;
    public static final int UNSPECIFIED_COMPOSITE_PK_NOT_SUPPORTED = 7232;

    public static final int INCOMPLETE_JOIN_COLUMN_ELEMENTS_SPECIFIED = 7233;
    public static final int EXCESSIVE_JOIN_COLUMN_ELEMENTS_SPECIFIED = 7234;
    public static final int INCOMPLETE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED = 7235;
    public static final int EXCESSIVE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED = 7236;

    public static final int NON_UNIQUE_ENTITY_NAME = 7237;
    
    public static final int CONFLICTING_SEQUENCE_AND_TABLE_GENERATORS_SPECIFIED = 7238;
    public static final int CONFLICTING_SEQUENCE_GENERATORS_SPECIFIED = 7239;
    public static final int CONFLICTING_SEQUENCE_NAME_AND_TABLE_PK_COLUMN_VALUE_SPECIFIED = 7240;
    public static final int CONFLICTING_TABLE_GENERATORS_SPECIFIED = 7241;
    public static final int INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION = 7242;

    public static final int CLASS_NOT_LISTED_IN_PERSISTENCE_UNIT = 7243;

	public static final int INVALID_MAPPING = 7244;
    public static final int CONFLICTNG_ACCESS_TYPE_FOR_EMBEDDABLE = 7245;
    public static final int INVALID_EMBEDDED_ATTRIBUTE = 7246;
    public static final int BOTH_FIELDS_AND_PROPERTIES_ANNOTATED = 7247;
    public static final int INCORRECT_OVERRIDING_OF_ACCESSTYPE = 7248;
    public static final int EMBEDDED_ID_CLASS_HAS_NO_ATTR = 7249;
    public static final int NON_ENTITY_AS_TARGET_IN_REL = 7250;

    public static final int PRIMARY_KEY_UPDATE_DISALLOWED = 7251;

    public static final int NON_UNIQUE_MAPPING_FILE_NAME = 7252;

    public static final int MAPPING_FILE_NOT_FOUND = 7253;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    public ValidationException() {
        super();
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected ValidationException(String theMessage) {
        super(theMessage);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected ValidationException(String message, Exception internalException) {
        super(message, internalException);
    }
    
    public static ValidationException alreadyLoggedIn(String sessionName) {
        Object[] args = { sessionName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ALREADY_LOGGED_IN, args));
        validationException.setErrorCode(ALREADY_LOGGED_IN);
        return validationException;
    }
    
    public static ValidationException cannotAcquireClientSessionFromSession() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_ACQUIRE_CLIENTSESSION_FROM_SESSION, args));
        validationException.setErrorCode(CANNOT_ACQUIRE_CLIENTSESSION_FROM_SESSION);
        return validationException;
    }

    public static ValidationException cannotAcquireDataSource(Object name, Exception exception) {
        Object[] args = { name };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_ACQUIRE_DATA_SOURCE, args), exception);
        validationException.setErrorCode(CANNOT_ACQUIRE_DATA_SOURCE);
        return validationException;
    }
    
    // The following is for flashback.
    public static ValidationException cannotAcquireHistoricalSession() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_ACQUIRE_HISTORICAL_SESSION, args));
        validationException.setErrorCode(CANNOT_ACQUIRE_HISTORICAL_SESSION);
        return validationException;
    }
    
    public static ValidationException cannotAddDescriptorsToSessionBroker() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_ADD_DESCRIPTORS_TO_SESSION_BROKER, args));
        validationException.setErrorCode(CANNOT_ADD_DESCRIPTORS_TO_SESSION_BROKER);
        return validationException;
    }
    
    public static ValidationException cannotCastToClass(Object ob, Class objectClass, Class castClass) {
        Object[] args = { ob, objectClass, castClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_CAST_TO_CLASS, args));
        validationException.setErrorCode(CANNOT_CAST_TO_CLASS);
        return validationException;
    }
    
    public static ValidationException cannotCommitAndResumeSynchronizedUOW(UnitOfWorkImpl uow) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_RESUME_SYNCHRONIZED_UOW, args));
        validationException.setErrorCode(CANNOT_RESUME_SYNCHRONIZED_UOW);
        validationException.setSession(uow);
        return validationException;
    }
    
    public static ValidationException cannotCommitAndResumeUOWWithModifyAllQueries() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_COMMIT_AND_RESUME_UOW_WITH_MODIFY_ALL_QUERIES, args));
        validationException.setErrorCode(CANNOT_COMMIT_AND_RESUME_UOW_WITH_MODIFY_ALL_QUERIES);
        return validationException;
    }
    
    public static ValidationException cannotCommitUOWAgain() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_COMMIT_UOW_AGAIN, args));
        validationException.setErrorCode(CANNOT_COMMIT_UOW_AGAIN);
        return validationException;
    }
    
    public static ValidationException cannotCreateExternalTransactionController(String externalTransactionControllerName) {
        Object[] args = { externalTransactionControllerName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_CREATE_EXTERNAL_TRANSACTION_CONTROLLER, args));
        validationException.setErrorCode(CANNOT_CREATE_EXTERNAL_TRANSACTION_CONTROLLER);
        return validationException;
    }
    
    public static ValidationException notSupportedForDatasource() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NOT_SUPPORTED_FOR_DATASOURCE, args));
        validationException.setErrorCode(NOT_SUPPORTED_FOR_DATASOURCE);
        return validationException;
    }

    public static ValidationException notSupported(String operation, Class theClass) {
        Object[] args = { operation, theClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NOT_SUPPORTED, args));
        validationException.setErrorCode(NOT_SUPPORTED);
        return validationException;
    }

    public static ValidationException cannotSetListenerClasses(Exception exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SET_LISTENER_CLASSES_EXCEPTION, args), exception);
        validationException.setErrorCode(SET_LISTENER_CLASSES_EXCEPTION);
        return validationException;
    }

    public static ValidationException cannotHaveUnboundInOutputArguments() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANT_HAVE_UNBOUND_IN_OUTPUT_ARGUMENTS, args));
        validationException.setErrorCode(CANT_HAVE_UNBOUND_IN_OUTPUT_ARGUMENTS);
        return validationException;
    }

    public static ValidationException cannotModifyReadOnlyClassesSetAfterUsingUnitOfWork() {
        Object[] args = { CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_MODIFY_READ_ONLY_CLASSES_SET_AFTER_USING_UNIT_OF_WORK, args));
        validationException.setErrorCode(CANNOT_MODIFY_READ_ONLY_CLASSES_SET_AFTER_USING_UNIT_OF_WORK);
        return validationException;
    }

    public static ValidationException cannotRegisterAggregateObjectInUnitOfWork(Class type) {
        Object[] args = { type };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_REGISTER_AGGREGATE_OBJECT_IN_UNIT_OF_WORK, args));
        validationException.setErrorCode(CANNOT_REGISTER_AGGREGATE_OBJECT_IN_UNIT_OF_WORK);
        return validationException;
    }

    public static ValidationException cannotReleaseNonClientSession() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_RELEASE_NON_CLIENTSESSION, args));
        validationException.setErrorCode(CANNOT_RELEASE_NON_CLIENTSESSION);
        return validationException;
    }

    public static ValidationException cannotRemoveFromReadOnlyClassesInNestedUnitOfWork() {
        Object[] args = { CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_REMOVE_FROM_READ_ONLY_CLASSES_IN_NESTED_UNIT_OF_WORK, args));
        validationException.setErrorCode(CANNOT_REMOVE_FROM_READ_ONLY_CLASSES_IN_NESTED_UNIT_OF_WORK);
        return validationException;
    }

    public static ValidationException cannotSetReadPoolSizeAfterLogin() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_SET_READ_POOL_SIZE_AFTER_LOGIN, args));
        validationException.setErrorCode(CANNOT_SET_READ_POOL_SIZE_AFTER_LOGIN);
        return validationException;
    }

    public static ValidationException childDescriptorsDoNotHaveIdentityMap() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CHILD_DESCRIPTORS_DO_NOT_HAVE_IDENTITY_MAP, args));
        validationException.setErrorCode(CHILD_DESCRIPTORS_DO_NOT_HAVE_IDENTITY_MAP);
        return validationException;
    }
    
    public static ValidationException circularMappedByReferences(Class cls1, String attributeName1, Class cls2, String attributeName2) {
        Object[] args = { cls1, attributeName1, cls2, attributeName2  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CIRCULAR_MAPPED_BY_REFERENCES, args));
        validationException.setErrorCode(CIRCULAR_MAPPED_BY_REFERENCES);
        return validationException;
    }

    public static ValidationException clientSessionCanNotUseExclusiveConnection() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CLIENT_SESSION_CANNOT_USE_EXCLUSIVE_CONNECTION, args));
        validationException.setErrorCode(CLIENT_SESSION_CANNOT_USE_EXCLUSIVE_CONNECTION);
        return validationException;
    }

    public static ValidationException containerPolicyDoesNotUseKeys(ContainerPolicy aPolicy, String methodName) {
        Object[] args = { aPolicy.getContainerClass().toString(), methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONTAINER_POLICY_DOES_NOT_USE_KEYS, args));
        validationException.setErrorCode(CONTAINER_POLICY_DOES_NOT_USE_KEYS);
        return validationException;
    }

    public static ValidationException descriptorMustBeNotInitialized(ClassDescriptor descriptor) {
        Object[] args = { descriptor, CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, DESCRIPTOR_MUST_NOT_BE_INITIALIZED, args));
        validationException.setErrorCode(DESCRIPTOR_MUST_NOT_BE_INITIALIZED);
        return validationException;
    }

    public static ValidationException ejbCannotLoadRemoteClass(Exception exception, Class beanClass, String remoteClass) {
        Object[] args = { beanClass, remoteClass, CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_CANNOT_LOAD_REMOTE_CLASS, args), exception);
        validationException.setErrorCode(EJB_CANNOT_LOAD_REMOTE_CLASS);
        return validationException;
    }

    public static ValidationException ejbContainerExceptionRaised(Exception exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_CONTAINER_EXCEPTION_RAISED, args), exception);
        validationException.setErrorCode(EJB_CONTAINER_EXCEPTION_RAISED);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  The descriptor listed was not found in the session specified on the deployment descriptor.
    * Action:  Check that the project specified in the TopLink.properties file is the desired project.
    */
    public static ValidationException ejbDescriptorNotFoundInSession(Class beanClass, String sessionName) {
        Object[] args = { beanClass.getName(), sessionName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_DESCRIPTOR_NOT_FOUND_IN_SESSION, args));
        validationException.setErrorCode(EJB_DESCRIPTOR_NOT_FOUND_IN_SESSION);
        return validationException;
    }

    public static ValidationException ejbFinderException(Object bean, Throwable finderException, Vector primaryKey) {
        Object[] args = { bean, bean.getClass(), primaryKey };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_FINDER_EXCEPTION, args));
        validationException.setErrorCode(EJB_FINDER_EXCEPTION);
        return validationException;
    }

    public static ValidationException ejbInvalidHomeInterfaceClass(Class homeClassName) {
        Object[] args = { homeClassName.toString() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_INVALID_FINDER_ON_HOME, args));
        validationException.setErrorCode(EJB_INVALID_FINDER_ON_HOME);
        return validationException;
    }

    public static ValidationException ejbInvalidPlatformClass(String platformName, String projectName) {
        Object[] args = { platformName, projectName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_INVALID_PLATFORM_CLASS, args));
        validationException.setErrorCode(EJB_INVALID_PLATFORM_CLASS);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  The project class specified in the TopLink.properties file for the session specified on the toplink_session_name environment variable can not be found.
    * Action: Check that the project class given in the exception is on the WebSphere dependent classpath.
    */
    public static ValidationException ejbInvalidProjectClass(String projectClassName, String projectName, Throwable aThrowable) {
        Object[] args = { projectClassName, projectName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_INVALID_PROJECT_CLASS, args));
        validationException.setInternalException(aThrowable);
        validationException.setErrorCode(EJB_INVALID_PROJECT_CLASS);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  The session type specified in the TopLink.properties file for the session specified on the toplink_session_name environment variable is not a valid type.
    * Action: Check the session type must be either DatabaseSession or it's subclasses type (including the user-defined session type, which must be extended from DatabaseSession).
    */
    public static ValidationException ejbInvalidSessionTypeClass(String sessionType, String sessionName) {
        Object[] args = { sessionType, sessionName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_INVALID_SESSION_TYPE_CLASS, args));
        validationException.setErrorCode(EJB_INVALID_SESSION_TYPE_CLASS);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  An attempt was made to create or remove a been outside of a transaction.
    * Action:  Ensure that all removing and creating of beans is done within a transaction.
    */
    public static ValidationException ejbMustBeInTransaction(Object bean) {
        Object[] args = { bean, CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_MUST_BE_IN_TRANSACTION, args));
        validationException.setErrorCode(EJB_MUST_BE_IN_TRANSACTION);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:   The toplink_session_name value on the beans environment variable does not match with one in the TopLink.properties file.
    * Action:  Check that the session is in the properties file and check for any possible spelling differences. If necessary, correct the value of toplink_sessoin_name, regenerate thedeployment code, and redeploy.
    */
    public static ValidationException ejbNoSuchSessionSpecifiedInProperties(String sessionName, Class beanClass) {
        Object[] args = { sessionName, beanClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_NO_SUCH_SESSION_SPECIFIED_IN_PROPERTIES, args));
        validationException.setErrorCode(EJB_NO_SUCH_SESSION_SPECIFIED_IN_PROPERTIES);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  An incorrect primary key object is being used with a bean.
    * Action: Ensure that you are using the correct primary key object for a bean.
    */
    public static ValidationException ejbPrimaryKeyReflectionException(Exception exception, Object primaryKey, Object bean) {
        Object[] args = { primaryKey, bean, CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_PRIMARY_KEY_REFLECTION_EXCEPTION, args), exception);
        validationException.setErrorCode(EJB_PRIMARY_KEY_REFLECTION_EXCEPTION);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  The session type specified in the TopLink.properties file for the session specified on the toplink_session_name environment variable is not found using the default class loader.
    * Action: Check that the session class given in the exception is on the app server classpath.
    */
    public static ValidationException ejbSessionTypeClassNotFound(String sessionType, String sessionName, Throwable exception) {
        Object[] args = { sessionType, sessionName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_SESSION_TYPE_CLASS_NOT_FOUND, args));
        validationException.setErrorCode(EJB_SESSION_TYPE_CLASS_NOT_FOUND);
        validationException.setInternalException(exception);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause: The TopLink.properties file can not be found.
    * Action: Make sure that the location of the TopLink.properties files is on the classpath.  If the exception is within VisualAge the TopLink.properties file must be in the project resources for the TopLink project.
    */
    public static ValidationException ejbTopLinkPropertiesNotFound(Exception exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EJB_TOPLINK_PROPERTIES_NOT_FOUND, args));
        validationException.setInternalException(exception);
        validationException.setErrorCode(EJB_TOPLINK_PROPERTIES_NOT_FOUND);
        return validationException;
    }

    public static ValidationException errorProcessingNamedQueryAnnotation(Class entityClass, String namedQuery, Exception exception) {
        Object[] args = { entityClass, namedQuery };
        
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_PROCESSING_NAMED_QUERY_ANNOTATION, args), exception);
        validationException.setErrorCode(ERROR_PROCESSING_NAMED_QUERY_ANNOTATION);
        return validationException;
    }
    
    public static ValidationException errorProcessingNamedQueryElement(String namedQuery, Exception exception) {
        Object[] args = { namedQuery };
        
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_PROCESSING_NAMED_QUERY_ELEMENT, args), exception);
        validationException.setErrorCode(ERROR_PROCESSING_NAMED_QUERY_ELEMENT);
        return validationException;
    }
    
    /**
     * PUBLIC:
     * The session named "name" could not be found in the Sessions.XML
     */
    public static ValidationException errorInSessionsXML(String translatedExceptions) {
        ValidationException validationException = new ValidationException(translatedExceptions);
        validationException.setErrorCode(ERROR_IN_SESSION_XML);
        return validationException;
    }

    public static ValidationException errorInstantiatingClass(Class cls, Exception exception) {
        Object[] args = { cls };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_INSTANTIATING_CLASS, args), exception);
        validationException.setErrorCode(ERROR_INSTANTIATING_CLASS);
        return validationException;
    }

    public static ValidationException noPropertiesFileFound(Exception exception) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_PROPERTIES_FILE_FOUND, args), exception);
        validationException.setErrorCode(NO_PROPERTIES_FILE_FOUND);
        return validationException;
    }

    public static ValidationException noSessionsXMLFound(String resourceName) {
        Object[] args = { resourceName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_SESSIONS_XML_FOUND, args));
        validationException.setErrorCode(NO_SESSIONS_XML_FOUND);
        return validationException;
    }

    public static ValidationException errorEncryptingPassword(Exception exception) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_ENCRYPTING_PASSWORD, args), exception);
        validationException.setErrorCode(ERROR_ENCRYPTING_PASSWORD);
        return validationException;
    }

    public static ValidationException embeddedIdAndIdAnnotationFound(Class entityClass, String embeddedIdAttributeName, String idAttributeName) {
        Object[] args = { entityClass, embeddedIdAttributeName, idAttributeName };
        
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EMBEDDED_ID_AND_ID_ANNOTATIONS_FOUND, args));
        validationException.setErrorCode(EMBEDDED_ID_AND_ID_ANNOTATIONS_FOUND);
        return validationException;
    }

    public static ValidationException embeddedIdAndIdElementFound(Class entityClass, String embeddedIdAttributeName, String idAttributeName) {
        Object[] args = { entityClass, embeddedIdAttributeName, idAttributeName };
        
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EMBEDDED_ID_AND_ID_ELEMENTS_FOUND, args));
        validationException.setErrorCode(EMBEDDED_ID_AND_ID_ELEMENTS_FOUND);
        return validationException;
    }

    public static ValidationException errorDecryptingPassword(Exception exception) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_DECRYPTING_PASSWORD, args), exception);
        validationException.setErrorCode(ERROR_DECRYPTING_PASSWORD);
        return validationException;
    }

    public static ValidationException invalidEncryptionClass(String className, Throwable throwableError) {
        Object[] args = { className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ENCRYPTION_CLASS, args));
        validationException.setErrorCode(INVALID_ENCRYPTION_CLASS);
        validationException.setInternalException(throwableError);
        return validationException;
    }
    
    public static ValidationException invalidEntityCallbackMethodArguments(Class entityClass, String methodName) {
        Object[] args = { entityClass, methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ENTITY_CALLBACK_METHOD_ARGUMENTS, args));
        validationException.setErrorCode(INVALID_ENTITY_CALLBACK_METHOD_ARGUMENTS);
        return validationException;
    }
    
    public static ValidationException invalidEntityListenerCallbackMethodArguments(Class entityClass, Class parameterClass, Class entityListener, String methodName) {
        Object[] args = { entityClass, parameterClass, entityListener, methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ENTITY_LISTENER_CALLBACK_METHOD_ARGUMENTS, args));
        validationException.setErrorCode(INVALID_ENTITY_LISTENER_CALLBACK_METHOD_ARGUMENTS);
        return validationException;
    }

    public static ValidationException noTopLinkEjbJarXMLFound() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_TOPLINK_EJB_JAR_XML_FOUND, args));
        validationException.setErrorCode(NO_TOPLINK_EJB_JAR_XML_FOUND);
        return validationException;
    }

    public static ValidationException excusiveConnectionIsNoLongerAvailable(DatabaseQuery query, AbstractSession session) {
        Object[] args = { query.getReferenceClass().getName() };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCLUSIVE_CONNECTION_NO_LONGER_AVAILABLE, args));
        validationException.setErrorCode(EXCLUSIVE_CONNECTION_NO_LONGER_AVAILABLE);
        validationException.setSession(session);
        return validationException;
    }

    public static ValidationException existingQueryTypeConflict(DatabaseQuery newQuery, DatabaseQuery existingQuery) {
        Object[] args = { newQuery, newQuery.getName(), newQuery.getArgumentTypes(), existingQuery, existingQuery.getName(), existingQuery.getArgumentTypes() };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXISTING_QUERY_TYPE_CONFLICT, args));
        validationException.setErrorCode(EXISTING_QUERY_TYPE_CONFLICT);
        return validationException;
    }

    public static ValidationException fatalErrorOccurred(Exception exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, FATAL_ERROR_OCCURRED, args), exception);
        validationException.setErrorCode(FATAL_ERROR_OCCURRED);
        return validationException;
    }

    public static ValidationException featureIsNotAvailableInRunningJDKVersion(String feature) {
        Object[] args = { feature, System.getProperty("java.version") };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, FEATURE_NOT_SUPPORTED_IN_JDK_VERSION, args));
        validationException.setErrorCode(FEATURE_NOT_SUPPORTED_IN_JDK_VERSION);
        return validationException;
    }

    public static ValidationException fieldLevelLockingNotSupportedWithoutUnitOfWork() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, FIELD_LEVEL_LOCKING_NOTSUPPORTED_OUTSIDE_A_UNIT_OF_WORK, args));
        validationException.setErrorCode(FIELD_LEVEL_LOCKING_NOTSUPPORTED_OUTSIDE_A_UNIT_OF_WORK);
        return validationException;
    }

    /**
     * PUBLIC:
     * Possible cause:  The order-by value provided does not correspond to an attribute on the target entity.
     * Action: Ensure that an attribute with the same name as the order-by value exists on the target entity.
     */
    public static ValidationException invalidOrderByValue(String referenceAttribute, Class referenceClass, String entityAttribute, Class entityClass) {
        Object[] args = { referenceAttribute, referenceClass, entityAttribute, entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ORDER_BY_VALUE, args));
        validationException.setErrorCode(INVALID_ORDER_BY_VALUE);
        return validationException;
    }

    public static ValidationException fileError(java.io.IOException exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, FILE_ERROR, args), exception);
        validationException.setErrorCode(FILE_ERROR);
        return validationException;
    }
    
    public static ValidationException illegalContainerClass(Class aClass) {
        Object[] args = { aClass.toString() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ILLEGAL_CONTAINER_CLASS, args));
        validationException.setErrorCode(ILLEGAL_CONTAINER_CLASS);
        return validationException;
    }

    public static ValidationException illegalUseOfMapInDirectCollection(oracle.toplink.essentials.mappings.DirectCollectionMapping directCollectionMapping, Class aMapClass, String keyMethodName) {
        Object[] args = { directCollectionMapping, keyMethodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ILLEGAL_USE_OF_MAP_IN_DIRECTCOLLECTION, args));
        validationException.setErrorCode(ILLEGAL_USE_OF_MAP_IN_DIRECTCOLLECTION);
        return validationException;
    }

    public static ValidationException incorrectLoginInstanceProvided() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCORRECT_LOGIN_INSTANCE_PROVIDED, args));
        validationException.setErrorCode(INCORRECT_LOGIN_INSTANCE_PROVIDED);
        return validationException;
    }
    
    public static ValidationException instantiatingValueholderWithNullSession() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION, args));
        validationException.setErrorCode(INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION);
        return validationException;
    }
    
    /**
     * PUBLIC:
     * Possible cause:  A mapping for the attribute name specified in the attribute-override cannot be found 
     * in the descriptor for the embeddable class.
     * Action:  Ensure that there is an attribute on the embeddable class matching the attribute name in the 
     * attribute-override declaration.
     */
    public static ValidationException invalidAttributeOverrideName(String columnName, Class embeddableClass) {
        Object[] args = { columnName, embeddableClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ATTRIBUTE_OVERRIDE_NAME, args));
        validationException.setErrorCode(INVALID_ATTRIBUTE_OVERRIDE_NAME);
        return validationException;
    }

    public static ValidationException invalidCallbackMethod(Class listenerClass, String methodName) {
        Object[] args = { listenerClass, methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CALLBACK_METHOD, args));
        validationException.setErrorCode(INVALID_CALLBACK_METHOD);
        return validationException;
    }
    
    public static ValidationException invalidCallbackMethodModifier(Class listenerClass, String methodName) {
        Object[] args = { listenerClass, methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CALLBACK_METHOD_MODIFIER, args));
        validationException.setErrorCode(INVALID_CALLBACK_METHOD_MODIFIER);
        return validationException;
    }
    
    public static ValidationException invalidCallbackMethodName(Class listenerClass, String methodName) {
        Object[] args = { listenerClass, methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CALLBACK_METHOD_NAME, args));
        validationException.setErrorCode(INVALID_CALLBACK_METHOD_NAME);
        return validationException;
    }
    
    public static ValidationException invalidClassTypeForBLOBAttribute(Class entityClass, String attributeName) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CLASS_TYPE_FOR_BLOB_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_CLASS_TYPE_FOR_BLOB_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidClassTypeForCLOBAttribute(Class entityClass, String attributeName) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CLASS_TYPE_FOR_CLOB_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_CLASS_TYPE_FOR_CLOB_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidTypeForEnumeratedAttribute(String attributeName, Class targetClass, Class entityClass) {
        Object[] args = { attributeName, targetClass, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_TYPE_FOR_ENUMERATED_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_TYPE_FOR_ENUMERATED_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidTypeForLOBAttribute(String attributeName, Class targetClass, Class entityClass) {
        Object[] args = { attributeName, targetClass, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_TYPE_FOR_LOB_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_TYPE_FOR_LOB_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidTypeForSerializedAttribute(String attributeName, Class targetClass, Class entityClass) {
        Object[] args = { attributeName, targetClass, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_TYPE_FOR_SERIALIZED_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_TYPE_FOR_SERIALIZED_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidTypeForTemporalAttribute(String attributeName, Class targetClass, Class entityClass) {
        Object[] args = { attributeName, targetClass, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_TYPE_FOR_TEMPORAL_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_TYPE_FOR_TEMPORAL_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidTypeForVersionAttribute(String attributeName, Class lockingType, Class entityClass) {
        Object[] args = { attributeName, lockingType, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_TYPE_FOR_VERSION_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_TYPE_FOR_VERSION_ATTRIBUTE);
        return validationException;
    }

    public static ValidationException invalidCollectionTypeForRelationship(Class cls, Class rawClass, Object element) {
        Object[] args = { rawClass, element, cls };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_COLLECTION_TYPE_FOR_RELATIONSHIP, args));
        validationException.setErrorCode(INVALID_COLLECTION_TYPE_FOR_RELATIONSHIP);
        return validationException;
    }
    
    public static ValidationException invalidColumnAnnotationOnRelationship(Class entityClass, String attributeName) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_COLUMN_ANNOTATION_ON_RELATIONSHIP, args));
        validationException.setErrorCode(INVALID_COLUMN_ANNOTATION_ON_RELATIONSHIP);
        return validationException;
    }
        
    public static ValidationException invalidColumnElementOnRelationship(Class entityClass, String attributeName) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_COLUMN_ELEMENT_ON_RELATIONSHIP, args));
        validationException.setErrorCode(INVALID_COLUMN_ELEMENT_ON_RELATIONSHIP);
        return validationException;
    }

    public static ValidationException invalidCompositePKAttribute(Class entityClass, String pkClassName, String attributeName, Type expectedType, Type actualType) {
        Object[] args = { entityClass, pkClassName, attributeName, expectedType, actualType };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_COMPOSITE_PK_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_COMPOSITE_PK_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException invalidCompositePKSpecification(Class entityClass, String pkClassName) {
        Object[] args = { entityClass, pkClassName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_COMPOSITE_PK_SPECIFICATION, args));
        validationException.setErrorCode(INVALID_COMPOSITE_PK_SPECIFICATION);
        return validationException;
    }
    
    public static ValidationException invalidConnector(oracle.toplink.essentials.sessions.Connector connector) {
        Object[] args = { connector };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_CONNECTOR, args));
        validationException.setErrorCode(INVALID_CONNECTOR);
        return validationException;
    }

    public static ValidationException invalidDataSourceName(String name, Exception exception) {
        Object[] args = { name };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_DATA_SOURCE_NAME, args), exception);
        validationException.setErrorCode(INVALID_DATA_SOURCE_NAME);
        return validationException;
    }

    public static ValidationException invalidEmbeddableAttribute(Class aggregateClass, String aggregateAttributeName, Class owningClass, String owningAttributeName) {
        Object[] args = { aggregateClass, aggregateAttributeName,  owningClass, owningAttributeName};

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_EMBEDDABLE_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_EMBEDDABLE_ATTRIBUTE);
        return validationException;
    }

    /**
     * PUBLIC:
     * Possible cause:  Either the URL for the entity-mappings document is invalid, or there is an error in the document.
     * Action: Verify that the URL is correct.  If so, analyze the exception message for an indication of what is wrong withthe document.
     */
    public static ValidationException invalidEntityMappingsDocument(String fileName, Exception exception) {
        Object[] args = { fileName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_ENTITY_MAPPINGS_DOCUMENT, args), exception);
        validationException.setErrorCode(INVALID_ENTITY_MAPPINGS_DOCUMENT);
        return validationException;
    }
    
    public static ValidationException invalidFileName(String fileName) {
        Object[] args = { fileName };

        ValidationException exception = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_FILE_TYPE, args));
        exception.setErrorCode(INVALID_FILE_TYPE);
        return exception;
    }
    
    public static ValidationException invalidMapping(Class entityClass, Class targetClass) {
    	Object[] args = { entityClass, targetClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_MAPPING, args));
        validationException.setErrorCode(INVALID_MAPPING);
        return validationException;
    }

    public static ValidationException invalidMergePolicy() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_MERGE_POLICY, args));
        validationException.setErrorCode(INVALID_MERGE_POLICY);
        return validationException;
    }

    public static ValidationException javaTypeIsNotAValidDatabaseType(Class javaClass) {
        Object[] args = { javaClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, JAVA_TYPE_IS_NOT_A_VALID_DATABASE_TYPE, args));
        validationException.setErrorCode(JAVA_TYPE_IS_NOT_A_VALID_DATABASE_TYPE);
        return validationException;
    }

    public static ValidationException jtsExceptionRaised(Exception exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, JTS_EXCEPTION_RAISED, args), exception);
        validationException.setErrorCode(JTS_EXCEPTION_RAISED);
        return validationException;
    }

    public static ValidationException loginBeforeAllocatingClientSessions() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, LOGIN_BEFORE_ALLOCATING_CLIENT_SESSIONS, args));
        validationException.setErrorCode(LOGIN_BEFORE_ALLOCATING_CLIENT_SESSIONS);
        return validationException;
    }

    public static ValidationException logIOError(java.io.IOException exception) {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, LOG_IO_ERROR, args), exception);
        validationException.setErrorCode(LOG_IO_ERROR);
        return validationException;
    }
    
    public static ValidationException mapKeyNotDeclaredInItemClass(String keyName, Class aClass) {
        Object[] args = { keyName, aClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MAP_KEY_NOT_DECLARED_IN_ITEM_CLASS, args));
        validationException.setErrorCode(MAP_KEY_NOT_DECLARED_IN_ITEM_CLASS);
        return validationException;
    }

    public static ValidationException maxSizeLessThanMinSize() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MAX_SIZE_LESS_THAN_MIN_SIZE, args));
        validationException.setErrorCode(MAX_SIZE_LESS_THAN_MIN_SIZE);
        return validationException;
    }

    public static ValidationException noMappedByAttributeFound(Class ownerClass, String mappedByAttributeName, Class entityClass, String attributeName) {
        Object[] args = { ownerClass, mappedByAttributeName, entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_MAPPED_BY_ATTRIBUTE_FOUND, args));
        validationException.setErrorCode(NO_MAPPED_BY_ATTRIBUTE_FOUND);
        return validationException;
    }
    
    public static ValidationException nonEntityTargetInRelationship(Class javaClass, Class targetEntity, AnnotatedElement annotatedElement) {
        Object[] args = {javaClass, targetEntity, annotatedElement};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NON_ENTITY_AS_TARGET_IN_REL, args));
        validationException.setErrorCode(NON_ENTITY_AS_TARGET_IN_REL);
        return validationException;
    }
    
    public static ValidationException nonUniqueEntityName(String clsName1, String clsName2, String name) {
        Object[] args = { name, clsName1, clsName2, CR };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NON_UNIQUE_ENTITY_NAME, args));
        validationException.setErrorCode(NON_UNIQUE_ENTITY_NAME);
        return validationException;
    }
    
    public static ValidationException nonUniqueMappingFileName(String puName, String mf) {
        Object[] args = {puName, mf};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NON_UNIQUE_MAPPING_FILE_NAME, args));
        validationException.setErrorCode(NON_UNIQUE_MAPPING_FILE_NAME);
        return validationException;
    }
    
    public static ValidationException noPrimaryKeyAnnotationsFound(Class entityClass) {
        Object[] args = { entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_PK_ANNOTATIONS_FOUND, args));
        validationException.setErrorCode(NO_PK_ANNOTATIONS_FOUND);
        return validationException;
    }
    
    /**
    * PUBLIC:
    * Possible cause:  The TopLink.properties file does not include any reference to a project class, file, or xml project file for the session specified in the deployment descriptor.
    * Action: Edit the TopLink.properties file to include the desired project.
    */
    public static ValidationException noProjectSpecifiedInProperties(String bundleName, String serverName) {
        Object[] args = { bundleName, serverName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_PROJECT_SPECIFIED_IN_PROPERTIES, args));
        validationException.setErrorCode(NO_PROJECT_SPECIFIED_IN_PROPERTIES);
        return validationException;
    }

    public static ValidationException noSessionFound(String sessionName, String resourceName) {
        Object[] args = { sessionName, resourceName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_SESSION_FOUND, args));
        validationException.setErrorCode(NO_SESSION_FOUND);
        return validationException;
    }

    public static ValidationException noSessionRegisteredForClass(Class domainClass) {
        Object[] args = { domainClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_SESSION_REGISTERED_FOR_CLASS, args));
        validationException.setErrorCode(NO_SESSION_REGISTERED_FOR_CLASS);
        return validationException;
    }

    public static ValidationException noSessionRegisteredForName(String sessionName) {
        Object[] args = { sessionName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_SESSION_REGISTERED_FOR_NAME, args));
        validationException.setErrorCode(NO_SESSION_REGISTERED_FOR_NAME);
        return validationException;
    }

    public static ValidationException noTablesToCreate(oracle.toplink.essentials.sessions.Project project) {
        Object[] args = { project };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_TABLES_TO_CREATE, args));
        validationException.setErrorCode(NO_TABLES_TO_CREATE);
        return validationException;
    }
    
    public static ValidationException noTemporalTypeSpecified(String attributeName, Class entityClass) {
        Object[] args = { attributeName, entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_TEMPORAL_TYPE_SPECIFIED, args));
        validationException.setErrorCode(NO_TEMPORAL_TYPE_SPECIFIED);
        return validationException;
    }

    public static ValidationException uniDirectionalOneToManyHasJoinColumnAnnotations(String attributeName, Class entityClass) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ANNOTATIONS, args));
        validationException.setErrorCode(UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ANNOTATIONS);
        return validationException;
    }
    
    public static ValidationException uniDirectionalOneToManyHasJoinColumnElements(String attributeName, Class entityClass) {
        Object[] args = { entityClass, attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ELEMENTS, args));
        validationException.setErrorCode(UNI_DIRECTIONAL_ONE_TO_MANY_HAS_JOINCOLUMN_ELEMENTS);
        return validationException;
    }
    
    public static ValidationException onlyFieldsAreValidKeysForDatabaseRows() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ONLY_FIELDS_ARE_VALID_KEYS_FOR_DATABASE_ROWS, args));
        validationException.setErrorCode(ONLY_FIELDS_ARE_VALID_KEYS_FOR_DATABASE_ROWS);
        return validationException;
    }

    public static ValidationException operationNotSupported(String methodName) {
        Object[] args = { methodName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, OPERATION_NOT_SUPPORTED, args));
        validationException.setErrorCode(OPERATION_NOT_SUPPORTED);
        return validationException;
    }

    public static ValidationException optimisticLockingNotSupportedWithStoredProcedureGeneration() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, OPTIMISTIC_LOCKING_NOT_SUPPORTED, args));
        validationException.setErrorCode(OPTIMISTIC_LOCKING_NOT_SUPPORTED);
        return validationException;
    }
    
    public static ValidationException oracleObjectTypeIsNotDefined(String typeName) {
        Object[] args = { typeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLE_OBJECT_TYPE_NOT_DEFINED, args));
        validationException.setErrorCode(ORACLE_OBJECT_TYPE_NOT_DEFINED);
        return validationException;
    }

    public static ValidationException oracleObjectTypeNameIsNotDefined(Class type) {
        Object[] args = { type };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLE_OBJECT_TYPE_NAME_NOT_DEFINED, args));
        validationException.setErrorCode(ORACLE_OBJECT_TYPE_NAME_NOT_DEFINED);
        return validationException;
    }

    public static ValidationException oracleVarrayMaximumSizeNotDefined(String typeName) {
        Object[] args = { typeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLE_VARRAY_MAXIMIM_SIZE_NOT_DEFINED, args));
        validationException.setErrorCode(ORACLE_VARRAY_MAXIMIM_SIZE_NOT_DEFINED);
        return validationException;
    }

    public static ValidationException persistenceUnitMetadataConflict(String tagName) {
        Object[] args = { tagName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PERSISTENCE_UNIT_METADATA_CONFLICT, args));
        validationException.setErrorCode(PERSISTENCE_UNIT_METADATA_CONFLICT);
        return validationException;
    }

    public static ValidationException platformClassNotFound(Exception exception, String className) {
        Object[] args = { className };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PLATFORM_CLASS_NOT_FOUND, args), exception);
        validationException.setErrorCode(PLATFORM_CLASS_NOT_FOUND);
        return validationException;
    }

    public static ValidationException poolNameDoesNotExist(String poolName) {
        Object[] args = { poolName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, POOL_NAME_DOES_NOT_EXIST, args));
        validationException.setErrorCode(POOL_NAME_DOES_NOT_EXIST);
        return validationException;
    }

    public static ValidationException poolsMustBeConfiguredBeforeLogin() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, POOLS_MUST_BE_CONFIGURED_BEFORE_LOGIN, args));
        validationException.setErrorCode(POOLS_MUST_BE_CONFIGURED_BEFORE_LOGIN);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  Instance document is incomplete - primary tables must be 
    * defined for both sides of a relationhip.
    * Action:  Make sure that each entity of a relationship has a primary table defined.
    */
    public static ValidationException primaryTableNotDefined(Class javaClass, String instanceDocName) {
        Object[] args = { javaClass, instanceDocName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PRIMARY_TABLE_NOT_DEFINED_FOR_RELATIONSHIP, args));
        validationException.setErrorCode(PRIMARY_TABLE_NOT_DEFINED_FOR_RELATIONSHIP);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  An amendment method was called but can not be found.
    * Action:  Check that the required amendment method exists on the class specified.
    */
    public static ValidationException projectAmendmentExceptionOccured(Exception exception, String amendmentMethod, String amendmentClass) {
        Object[] args = { amendmentMethod, amendmentClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PROJECT_AMENDMENT_EXCEPTION_OCCURED, args));
        validationException.setInternalException(exception);
        validationException.setErrorCode(PROJECT_AMENDMENT_EXCEPTION_OCCURED);
        return validationException;
    }

    public static ValidationException projectXMLNotFound(String projectXMLFile, Exception exception) {
        Object[] args = { projectXMLFile };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PROJECT_XML_NOT_FOUND, args));
        validationException.setInternalException(exception);
        validationException.setErrorCode(PROJECT_XML_NOT_FOUND);
        return validationException;
    }

    public static ValidationException queryArgumentTypeNotFound(DatabaseQuery query, String argumentName, String typeAsString, Exception exception) {
        Object[] args = { query.getName(), argumentName, typeAsString };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, QUERY_ARGUMENT_TYPE_NOT_FOUND, args));
        validationException.setInternalException(exception);
        validationException.setErrorCode(QUERY_ARGUMENT_TYPE_NOT_FOUND);
        return validationException;
    }

    public static ValidationException sequenceSetupIncorrectly(String sequenceName) {
        Object[] args = { sequenceName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SEQUENCE_SETUP_INCORRECTLY, args));
        validationException.setErrorCode(SEQUENCE_SETUP_INCORRECTLY);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  An attempt was made to modify the ServerPlatform after login.
    * Action:  All changes to the ServerPlatform must be made before login.
    */
    public static ValidationException serverPlatformIsReadOnlyAfterLogin(String serverPlatformClassName) {
        Object[] args = { serverPlatformClassName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SERVER_PLATFORM_IS_READ_ONLY_AFTER_LOGIN, args));
        validationException.setErrorCode(SERVER_PLATFORM_IS_READ_ONLY_AFTER_LOGIN);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  An amendment method was called but can not be found.
    * Action:  Check that the required amendment method exists on the class specified.
    */
    public static ValidationException sessionAmendmentExceptionOccured(Exception exception, String amendmentMethod, String amendmentClass, Class[] parameters) {
        StringBuffer buf = new StringBuffer(30);
        for (int i = 0; i < (parameters.length - 1); i++) {
            buf.append(parameters[i].getName());
            if (i != (parameters.length - 1)) {
                buf.append(", ");
            }
        }
        buf.append(parameters[parameters.length - 1].getName());

        Object[] args = { amendmentClass, amendmentMethod, buf.toString() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SESSION_AMENDMENT_EXCEPTION_OCCURED, args));
        validationException.setInternalException(exception);
        validationException.setErrorCode(SESSION_AMENDMENT_EXCEPTION_OCCURED);
        return validationException;
    }

    public static ValidationException startIndexOutOfRange() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, START_INDEX_OUT_OF_RANGE, args));
        validationException.setErrorCode(START_INDEX_OUT_OF_RANGE);
        return validationException;
    }

    public static ValidationException stopIndexOutOfRange() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, STOP_INDEX_OUT_OF_RANGE, args));
        validationException.setErrorCode(STOP_INDEX_OUT_OF_RANGE);
        return validationException;
    }

    /**
    * PUBLIC:
    * Possible cause:  The session class specified in the TopLink.properties file for the session specified on the toplink_session_name environment variable can not be found.
    * Action: Check that the session class given in the exception is on the application server dependent classpath.
    */
    public static ValidationException subSessionsNotDefinedForBroker(String brokerName) {
        Object[] args = { brokerName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SUB_SESSION_NOT_DEFINED_FOR_BROKER, args));
        validationException.setErrorCode(SUB_SESSION_NOT_DEFINED_FOR_BROKER);
        return validationException;
    }

    public static ValidationException tablePerClassInheritanceNotSupported(Class entityClass) {
        Object[] args = { entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, TABLE_PER_CLASS_INHERITANCE_NOT_SUPPORTED, args));
        validationException.setErrorCode(TABLE_PER_CLASS_INHERITANCE_NOT_SUPPORTED);
        return validationException;
    }

    public static ValidationException writeObjectNotAllowedInUnitOfWork() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRITE_OBJECT_NOT_ALLOWED_IN_UNIT_OF_WORK, args));
        validationException.setErrorCode(WRITE_OBJECT_NOT_ALLOWED_IN_UNIT_OF_WORK);
        return validationException;
    }

    public static ValidationException wrongObjectRegistered(Object registered, Object parent) {
        Object[] args = { registered, parent };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_OBJECT_REGISTERED, args));
        validationException.setErrorCode(WRONG_OBJECT_REGISTERED);
        return validationException;
    }
    
    public static ValidationException cannotIssueModifyAllQueryWithOtherWritesWithinUOW() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MODIFY_ALL_QUERIES_NOT_SUPPORTED_WITH_OTHER_WRITES, args));
        validationException.setErrorCode(MODIFY_ALL_QUERIES_NOT_SUPPORTED_WITH_OTHER_WRITES);
        return validationException;
    }

    public static ValidationException nullCacheKeyFoundOnRemoval(IdentityMap map, Class clazz) {
        Object[] args = { map, clazz, CR };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NULL_CACHE_KEY_FOUND_ON_REMOVAL, args));
        validationException.setErrorCode(NULL_CACHE_KEY_FOUND_ON_REMOVAL);
        return validationException;
    }

    public static ValidationException nullUnderlyingValueHolderValue(String methodName) {
        Object[] args = { methodName, CR };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NULL_UNDERLYING_VALUEHOLDER_VALUE, args));
        validationException.setErrorCode(NULL_UNDERLYING_VALUEHOLDER_VALUE);
        return validationException;
    }

    public static ValidationException invalidSequencingLogin() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_SEQUENCING_LOGIN, args));
        validationException.setErrorCode(INVALID_SEQUENCING_LOGIN);
        return validationException;
    }

    public static ValidationException isolatedDataNotSupportedInSessionBroker(String sessionName) {
        Object[] args = { sessionName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ISOLATED_DATA_NOT_SUPPORTED_IN_CLIENTSESSIONBROKER, args));
        validationException.setErrorCode(ISOLATED_DATA_NOT_SUPPORTED_IN_CLIENTSESSIONBROKER);
        return validationException;
    }

    public static ValidationException projectLoginIsNull(AbstractSession session) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PROJECT_LOGIN_IS_NULL, args));
        validationException.setErrorCode(PROJECT_LOGIN_IS_NULL);
        return validationException;
    }

    public static ValidationException historicalSessionOnlySupportedOnOracle() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, HISTORICAL_SESSION_ONLY_SUPPORTED_ON_ORACLE, args));
        validationException.setErrorCode(HISTORICAL_SESSION_ONLY_SUPPORTED_ON_ORACLE);
        return validationException;
    }

    public static ValidationException platformDoesNotSupportCallWithReturning(String platformTypeName) {
        Object[] args = { platformTypeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PLATFORM_DOES_NOT_SUPPORT_CALL_WITH_RETURNING, args));
        validationException.setErrorCode(PLATFORM_DOES_NOT_SUPPORT_CALL_WITH_RETURNING);
        return validationException;
    }

    public static ValidationException invalidNullMethodArguments() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_NULL_METHOD_ARGUMENTS, args));
        validationException.setErrorCode(INVALID_NULL_METHOD_ARGUMENTS);
        return validationException;
    }

    public static ValidationException invalidMethodArguments() {
        Object[] args = {  };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_METHOD_ARGUMENTS, args));
        validationException.setErrorCode(INVALID_METHOD_ARGUMENTS);
        return validationException;
    }

    public static ValidationException wrongUsageOfSetCustomArgumentTypeMethod(String callString) {
        Object[] args = { callString };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_USAGE_OF_SET_CUSTOM_SQL_ARGUMENT_TYPE_METOD, args));
        validationException.setErrorCode(WRONG_USAGE_OF_SET_CUSTOM_SQL_ARGUMENT_TYPE_METOD);
        return validationException;
    }

    public static ValidationException cannotTranslateUnpreparedCall(String callString) {
        Object[] args = { callString };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_TRANSLATE_UNPREPARED_CALL, args));
        validationException.setErrorCode(CANNOT_TRANSLATE_UNPREPARED_CALL);
        return validationException;
    }

    public static ValidationException cannotSetCursorForParameterTypeOtherThanOut(String fieldName, String callString) {
        Object[] args = { fieldName, callString };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_SET_CURSOR_FOR_PARAMETER_TYPE_OTHER_THAN_OUT, args));
        validationException.setErrorCode(CANNOT_SET_CURSOR_FOR_PARAMETER_TYPE_OTHER_THAN_OUT);
        return validationException;
    }

    public static ValidationException platformDoesNotSupportStoredFunctions(String platformTypeName) {
        Object[] args = { platformTypeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PLATFORM_DOES_NOT_SUPPORT_STORED_FUNCTIONS, args));
        validationException.setErrorCode(PLATFORM_DOES_NOT_SUPPORT_STORED_FUNCTIONS);
        return validationException;
    }

    public static ValidationException illegalOperationForUnitOfWorkLifecycle(int lifecycle, String operation) {
        switch (lifecycle) {
        case UnitOfWorkImpl.CommitTransactionPending:
            return unitOfWorkInTransactionCommitPending(operation);
        case UnitOfWorkImpl.WriteChangesFailed:
            return unitOfWorkAfterWriteChangesFailed(operation);
        case UnitOfWorkImpl.Death:default:
            return inActiveUnitOfWork(operation);
        }
    }

    public static ValidationException unitOfWorkInTransactionCommitPending(String operation) {
        Object[] args = { operation };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNIT_OF_WORK_IN_TRANSACTION_COMMIT_PENDING, args));
        validationException.setErrorCode(UNIT_OF_WORK_IN_TRANSACTION_COMMIT_PENDING);
        return validationException;
    }
    
    public static ValidationException unspecifiedCompositePKNotSupported(Class entityClass) {
        Object[] args = { entityClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNSPECIFIED_COMPOSITE_PK_NOT_SUPPORTED, args));
        validationException.setErrorCode(UNSPECIFIED_COMPOSITE_PK_NOT_SUPPORTED);
        return validationException;
    }
    
    public static ValidationException unsupportedCascadeLockingDescriptor(ClassDescriptor descriptor) {
        Object[] args = { descriptor };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNSUPPORTED_CASCADE_LOCKING_DESCRIPTOR, args));
        validationException.setErrorCode(UNSUPPORTED_CASCADE_LOCKING_DESCRIPTOR);
        return validationException;
    }
    
    public static ValidationException unsupportedCascadeLockingMapping(DatabaseMapping mapping) {
        Object[] args = { mapping };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNSUPPORTED_CASCADE_LOCKING_MAPPING, args));
        validationException.setErrorCode(UNSUPPORTED_CASCADE_LOCKING_MAPPING);
        return validationException;
    }
    
    public static ValidationException unsupportedCascadeLockingMappingWithCustomQuery(DatabaseMapping mapping) {
        Object[] args = { mapping };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNSUPPORTED_CASCADE_LOCKING_MAPPING_WITH_CUSTOM_QUERY, args));
        validationException.setErrorCode(UNSUPPORTED_CASCADE_LOCKING_MAPPING_WITH_CUSTOM_QUERY);
        return validationException;
    }

    public static ValidationException unitOfWorkAfterWriteChangesFailed(String operation) {
        Object[] args = { operation };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNIT_OF_WORK_AFTER_WRITE_CHANGES_FAILED, args));
        validationException.setErrorCode(UNIT_OF_WORK_AFTER_WRITE_CHANGES_FAILED);
        return validationException;
    }

    public static ValidationException inActiveUnitOfWork(String operation) {
        Object[] args = { operation };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INACTIVE_UNIT_OF_WORK, args));
        validationException.setErrorCode(INACTIVE_UNIT_OF_WORK);
        return validationException;
    }
    
    public static ValidationException incompleteJoinColumnsSpecified(Object annotatedElement, Class javaClass) {
        Object[] args = { annotatedElement, javaClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCOMPLETE_JOIN_COLUMNS_SPECIFIED, args));
        validationException.setErrorCode(INCOMPLETE_JOIN_COLUMNS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException incompleteJoinColumnElementsSpecified(Object attributeName, Class javaClass) {
        Object[] args = { attributeName, javaClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCOMPLETE_JOIN_COLUMN_ELEMENTS_SPECIFIED, args));
        validationException.setErrorCode(INCOMPLETE_JOIN_COLUMN_ELEMENTS_SPECIFIED);
        return validationException;
    }

    public static ValidationException incompletePrimaryKeyJoinColumnElementsSpecified(Object annotatedElement) {
        Object[] args = { annotatedElement };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCOMPLETE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED, args));
        validationException.setErrorCode(INCOMPLETE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED);
        return validationException;
    }

    public static ValidationException incompletePrimaryKeyJoinColumnsSpecified(Object annotatedElement) {
        Object[] args = { annotatedElement };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCOMPLETE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED, args));
        validationException.setErrorCode(INCOMPLETE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException unitOfWorkInTransactionCommitPending() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNIT_OF_WORK_IN_TRANSACTION_COMMIT_PENDING, args));
        validationException.setErrorCode(UNIT_OF_WORK_IN_TRANSACTION_COMMIT_PENDING);
        return validationException;
    }

    public static ValidationException writeChangesOnNestedUnitOfWork() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_WRITE_CHANGES_ON_NESTED_UNIT_OF_WORK, args));
        validationException.setErrorCode(CANNOT_WRITE_CHANGES_ON_NESTED_UNIT_OF_WORK);
        return validationException;
    }

    public static ValidationException cannotWriteChangesTwice() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_WRITE_CHANGES_TWICE, args));
        validationException.setErrorCode(CANNOT_WRITE_CHANGES_TWICE);
        return validationException;
    }
    
    public static ValidationException nestedUOWNotSupportedForAttributeTracking() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NESTED_UOW_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING, args));
        validationException.setErrorCode(NESTED_UOW_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING);
        return validationException;
    }

    public static ValidationException nestedUOWNotSupportedForModifyAllQuery() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NESTED_UOW_NOT_SUPPORTED_FOR_MODIFY_ALL_QUERY, args));
        validationException.setErrorCode(NESTED_UOW_NOT_SUPPORTED_FOR_MODIFY_ALL_QUERY);
        return validationException;
    }
    
    public static ValidationException noCorrespondingSetterMethodDefined(Class entityClass, Method method) {
        Object[] args = { entityClass, method };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, NO_CORRESPONDING_SETTER_METHOD_DEFINED, args));
        validationException.setErrorCode(NO_CORRESPONDING_SETTER_METHOD_DEFINED);
        return validationException;
    }

    public static ValidationException wrongCollectionChangeEventType(int eveType) {
        Object[] args = { new Integer(eveType) };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_COLLECTION_CHANGE_EVENT_TYPE, args));
        validationException.setErrorCode(WRONG_COLLECTION_CHANGE_EVENT_TYPE);
        return validationException;
    }

    public static ValidationException wrongChangeEvent(Class eveClass) {
        Object[] args = { eveClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_CHANGE_EVENT, args));
        validationException.setErrorCode(WRONG_CHANGE_EVENT);
        return validationException;
    }

    public static ValidationException oldCommitNotSupportedForAttributeTracking() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, OLD_COMMIT_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING, args));
        validationException.setErrorCode(OLD_COMMIT_NOT_SUPPORTED_FOR_ATTRIBUTE_TRACKING);
        return validationException;
    }

    /**
     * PUBLIC:
     * Possible cause: An field name has been encountered that does not exist on 
     * the associated entity.
     * Action: Ensure that a field with a matching name exists on the associated 
     * entity.
     */
    public static ValidationException unableToDetermineClassForField(String fieldName, Class entityClass) {
        Object[] args = { fieldName, entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNABLE_TO_DETERMINE_CLASS_FOR_FIELD, args));
        validationException.setErrorCode(UNABLE_TO_DETERMINE_CLASS_FOR_FIELD);
        return validationException;
    }
    
    /**
     * PUBLIC:
     * Possible cause: An property name has been encountered that does not exist 
     * on the associated entity.
     * Action: Ensure that a property with a matching name exists on the 
     * associated entity.
     */
    public static ValidationException unableToDetermineClassForProperty(String propertyName, Class entityClass) {
        Object[] args = { propertyName, entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNABLE_TO_DETERMINE_CLASS_FOR_PROPERTY, args));
        validationException.setErrorCode(UNABLE_TO_DETERMINE_CLASS_FOR_PROPERTY);
        return validationException;
    }    

    /**
     * PUBLIC:
     * Possible cause: the type of the attribute is Map, Set, List or Collection, and no target-entity is defined.
     * Action: ensure that the target-entity is defined in the instance doc. for the relationship mapping.
     */
    public static ValidationException unableToDetermineTargetEntity(String attributeName, Class entityClass) {
        Object[] args = { attributeName, entityClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNABLE_TO_DETERMINE_TARGET_ENTITY, args));
        validationException.setErrorCode(UNABLE_TO_DETERMINE_TARGET_ENTITY);
        return validationException;
    }
    
    public static ValidationException unableToLoadClass(String classname, Exception exception) {
        Object[] args = { classname };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNABLE_TO_LOAD_CLASS, args), exception);
        validationException.setErrorCode(UNABLE_TO_LOAD_CLASS);
        return validationException;
    }

    public static ValidationException unfetchedAttributeNotEditable(String attributeName) {
        Object[] args = { attributeName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, UNFETCHED_ATTRIBUTE_NOT_EDITABLE, args));
        validationException.setErrorCode(UNFETCHED_ATTRIBUTE_NOT_EDITABLE);
        return validationException;
    }

    public static ValidationException objectNeedImplTrackerForFetchGroupUsage(String className) {
        Object[] args = { className };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OBJECT_NEED_IMPL_TRACKER_FOR_FETCH_GROUP_USAGE, args));
        queryException.setErrorCode(OBJECT_NEED_IMPL_TRACKER_FOR_FETCH_GROUP_USAGE);
        return queryException;
    }

    public static ValidationException wrongSequenceType(String typeName, String methodName) {
        Object[] args = { typeName, methodName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_SEQUENCE_TYPE, args));
        validationException.setErrorCode(WRONG_SEQUENCE_TYPE);
        return validationException;
    }

    public static ValidationException cannotSetDefaultSequenceAsDefault(String seqName) {
        Object[] args = { seqName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_SET_DEFAULT_SEQUENCE_AS_DEFAULT, args));
        validationException.setErrorCode(CANNOT_SET_DEFAULT_SEQUENCE_AS_DEFAULT);
        return validationException;
    }

    public static ValidationException defaultSequenceNameAlreadyUsedBySequence(String seqName) {
        Object[] args = { seqName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, DEFAULT_SEQUENCE_NAME_ALREADY_USED_BY_SEQUENCE, args));
        validationException.setErrorCode(DEFAULT_SEQUENCE_NAME_ALREADY_USED_BY_SEQUENCE);
        return validationException;
    }

    public static ValidationException sequenceNameAlreadyUsedByDefaultSequence(String seqName) {
        Object[] args = { seqName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SEQUENCE_NAME_ALREADY_USED_BY_DEFAULT_SEQUENCE, args));
        validationException.setErrorCode(SEQUENCE_NAME_ALREADY_USED_BY_DEFAULT_SEQUENCE);
        return validationException;
    }

    public static ValidationException platformDoesNotSupportSequence(String seqName, String platformTypeName, String sequenceTypeName) {
        Object[] args = { seqName, platformTypeName, sequenceTypeName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PLATFORM_DOES_NOT_SUPPORT_SEQUENCE, args));
        validationException.setErrorCode(PLATFORM_DOES_NOT_SUPPORT_SEQUENCE);
        return validationException;
    }

    public static ValidationException sequenceCannotBeConnectedToTwoPlatforms(String seqName, String ownerPlatformName, String otherPlatformName) {
        Object[] args = { seqName, ownerPlatformName, otherPlatformName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SEQUENCE_CANNOT_BE_CONNECTED_TO_TWO_PLATFORMS, args));
        validationException.setErrorCode(SEQUENCE_CANNOT_BE_CONNECTED_TO_TWO_PLATFORMS);
        return validationException;
    }

    public static ValidationException querySequenceDoesNotHaveSelectQuery(String seqName) {
        Object[] args = { seqName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, QUERY_SEQUENCE_DOES_NOT_HAVE_SELECT_QUERY, args));
        validationException.setErrorCode(QUERY_SEQUENCE_DOES_NOT_HAVE_SELECT_QUERY);
        return validationException;
    }

    public static ValidationException createPlatformDefaultSequenceUndefined(String platformTypeName) {
        Object[] args = { platformTypeName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CREATE_PLATFORM_DEFAULT_SEQUENCE_UNDEFINED, args));
        validationException.setErrorCode(CREATE_PLATFORM_DEFAULT_SEQUENCE_UNDEFINED);
        return validationException;
    }

    public static ValidationException sequenceGeneratorUsingAReservedName(String reservedName, String location) {
        Object[] args = { reservedName, location };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, SEQUENCE_GENERATOR_RESERVED_NAME, args));
        validationException.setErrorCode(SEQUENCE_GENERATOR_RESERVED_NAME);
        return validationException;
    }
    
    public static ValidationException tableGeneratorUsingAReservedName(String reservedName, String location) {
        Object[] args = { reservedName, location };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, TABLE_GENERATOR_RESERVED_NAME, args));
        validationException.setErrorCode(TABLE_GENERATOR_RESERVED_NAME);
        return validationException;
    }

    public static ValidationException onlyOneGeneratedValueIsAllowed(Class cls, String field1, String field2) {
        Object[] args = { cls, field1, field2 };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ONLY_ONE_GENERATED_VALURE_IS_ALLOWED, args));
        validationException.setErrorCode(ONLY_ONE_GENERATED_VALURE_IS_ALLOWED);
        return validationException;
    }

    public static ValidationException wrongPropertyNameInChangeEvent(Class objectClass, String propertyName) {
        Object[] args = { objectClass, propertyName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, WRONG_PROPERTY_NAME_IN_CHANGE_EVENT, args));
        validationException.setErrorCode(WRONG_PROPERTY_NAME_IN_CHANGE_EVENT);
        return validationException;
    }

    public static ValidationException oracleOCIProxyConnectorRequiresOracleOCIConnectionPool() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLEOCIPROXYCONNECTOR_REQUIRES_ORACLEOCICONNECTIONPOOL, args));
        validationException.setErrorCode(ORACLEOCIPROXYCONNECTOR_REQUIRES_ORACLEOCICONNECTIONPOOL);
        return validationException;
    }

    public static ValidationException oracleJDBC10_1_0_2ProxyConnectorRequiresOracleConnection() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION, args));
        validationException.setErrorCode(ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION);
        return validationException;
    }

    public static ValidationException oracleJDBC10_1_0_2ProxyConnectorRequiresOracleConnectionVersion() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION_VERSION, args));
        validationException.setErrorCode(ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_ORACLECONNECTION_VERSION);
        return validationException;
    }

    public static ValidationException oracleJDBC10_1_0_2ProxyConnectorRequiresIntProxytype() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_INT_PROXYTYPE, args));
        validationException.setErrorCode(ORACLEJDBC10_1_0_2PROXYCONNECTOR_REQUIRES_INT_PROXYTYPE);
        return validationException;
    }

    public static ValidationException couldNotFindDriverClass(Object driver, Exception ex) {
        Object[] args = { driver };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, COULD_NOT_FIND_DRIVER_CLASS, args), ex);
        validationException.setErrorCode(COULD_NOT_FIND_DRIVER_CLASS);
        return validationException;
    }
    
    public static ValidationException couldNotFindMapKey(String attributeName, Class entityClass, DatabaseMapping mapping) {
        Object[] args = { attributeName, entityClass, mapping };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, COULD_NOT_FIND_MAP_KEY, args));
        validationException.setErrorCode(COULD_NOT_FIND_MAP_KEY);
        return validationException;
    }

    public static ValidationException errorClosingPersistenceXML(Exception ex) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ERROR_CLOSING_PERSISTENCE_XML, args), ex);
        validationException.setErrorCode(ERROR_CLOSING_PERSISTENCE_XML);
        return validationException;
    }
    
    public static ValidationException configFactoryNamePropertyNotSpecified(String configFactory) {
        Object[] args = { configFactory };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFIG_FACTORY_NAME_PROPERTY_NOT_SPECIFIED, args));
        validationException.setErrorCode(CONFIG_FACTORY_NAME_PROPERTY_NOT_SPECIFIED);
        return validationException;
    }

    public static ValidationException configFactoryNamePropertyNotFound(String configClass, String configFactory, Exception ex) {
        Object[] args = { configClass, configFactory };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFIG_FACTORY_NAME_PROPERTY_NOT_FOUND, args), ex);
        validationException.setErrorCode(CONFIG_FACTORY_NAME_PROPERTY_NOT_FOUND);
        return validationException;
    }

    public static ValidationException cannotInvokeMethodOnConfigClass(String configMethod, String configClass, String configFactory, Exception ex) {
        Object[] args = { configMethod, configClass, configFactory };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_INVOKE_METHOD_ON_CONFIG_CLASS, args), ex);
        validationException.setErrorCode(CANNOT_INVOKE_METHOD_ON_CONFIG_CLASS);
        return validationException;
    }

    public static ValidationException configMethodNotDefined(String configClass, String configMethod) {
        Object[] args = { configClass, configMethod };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFIG_METHOD_NOT_DEFINED, args));
        validationException.setErrorCode(CONFIG_METHOD_NOT_DEFINED);
        return validationException;
    }

   public static ValidationException conflictingSequenceAndTableGeneratorsSpecified(String name, String sequenceGeneratorLocation, String tableGeneratorLocation) {
        Object[] args = { name, sequenceGeneratorLocation, tableGeneratorLocation };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFLICTING_SEQUENCE_AND_TABLE_GENERATORS_SPECIFIED, args));
        validationException.setErrorCode(CONFLICTING_SEQUENCE_AND_TABLE_GENERATORS_SPECIFIED);
        return validationException;
    }

    public static ValidationException conflictingSequenceGeneratorsSpecified(String name, String location1, String location2) {
        Object[] args = { name, location1, location2 };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFLICTING_SEQUENCE_GENERATORS_SPECIFIED, args));
        validationException.setErrorCode(CONFLICTING_SEQUENCE_GENERATORS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException conflictingSequenceNameAndTablePkColumnValueSpecified(String name, String sequenceGeneratorLocation, String tableGeneratorLocation) {
        Object[] args = { name, sequenceGeneratorLocation, tableGeneratorLocation };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFLICTING_SEQUENCE_NAME_AND_TABLE_PK_COLUMN_VALUE_SPECIFIED, args));
        validationException.setErrorCode(CONFLICTING_SEQUENCE_NAME_AND_TABLE_PK_COLUMN_VALUE_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException conflictingTableGeneratorsSpecified(String name, String location1, String location2) {
        Object[] args = { name, location1, location2 };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFLICTING_TABLE_GENERATORS_SPECIFIED, args));
        validationException.setErrorCode(CONFLICTING_TABLE_GENERATORS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException classListMustNotBeNull() {
        Object[] args = { };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CLASS_LIST_MUST_NOT_BE_NULL, args));
        validationException.setErrorCode(CLASS_LIST_MUST_NOT_BE_NULL);
        return validationException;
    }

    public static ValidationException currentLoaderNotValid(ClassLoader loader) {
        Object[] args = { loader };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CURRENT_LOADER_NOT_VALID, args));
        validationException.setErrorCode(CURRENT_LOADER_NOT_VALID);
        return validationException;
    }

    public static ValidationException methodFailed(String methodName, Exception ex) {
        Object[] args = { methodName };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, METHOD_FAILED, args), ex);
        validationException.setErrorCode(METHOD_FAILED);
        return validationException;
    }
    
    public static ValidationException missingDescriptor(String className) {
        Object[] args = { className };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MISSING_DESCRIPTOR, args));
        validationException.setErrorCode(MISSING_DESCRIPTOR);
        return validationException;
    }
    
    /**
     * Create a validation exception for the look up of a mapping on a descriptor for an unknown attribute name. The source
     * is a string describing where the lookup was called from.
     */
    public static ValidationException missingMappingForAttribute(ClassDescriptor descriptor, String attributeName, String source) {
        Object[] args = { descriptor, attributeName, source };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MISSING_MAPPING, args));
        validationException.setErrorCode(MISSING_MAPPING);
        return validationException;
    }
    
    public static ValidationException multipleCursorsNotSupported(String callString) {
        Object[] args = { callString };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MULTIPLE_CURSORS_NOT_SUPPORTED, args));
        validationException.setErrorCode(MULTIPLE_CURSORS_NOT_SUPPORTED);
        return validationException;
    }
    
    public static ValidationException multipleEmbeddedIdAnnotationsFound(Class entityClass, String attributeName1, String attributeName2) {
        Object[] args = { entityClass, attributeName1, attributeName2 };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MULTIPLE_EMBEDDED_ID_ANNOTATIONS_FOUND, args));
        validationException.setErrorCode(MULTIPLE_EMBEDDED_ID_ANNOTATIONS_FOUND);
        return validationException;
    }

    public static ValidationException multipleEmbeddedIdElementsFound(Class entityClass, String attributeName1, String attributeName2) {
        Object[] args = { entityClass, attributeName1, attributeName2 };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MULTIPLE_EMBEDDED_ID_ELEMENTS_FOUND, args));
        validationException.setErrorCode(MULTIPLE_EMBEDDED_ID_ELEMENTS_FOUND);
        return validationException;
    }
    
    public static ValidationException multipleLifecycleCallbackMethodsForSameLifecycleEvent(Class listenerClass, Method method1, Method method2) {
        Object[] args = { listenerClass, method1, method2 };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MULTIPLE_CALLBACK_METHODS_DEFINED, args));
        validationException.setErrorCode(MULTIPLE_CALLBACK_METHODS_DEFINED);
        return validationException;
    }
    
    /**
     * PUBLIC:
     * Possible cause:  More than one of projectClass, projectFile, and xmlProjectFile are specified for the same session in the TopLink.properties file.
     * Action:  Remove one or more of the entries so that only one of the three is specified in the TopLink.properties file.
     */
    public static ValidationException multipleProjectsSpecifiedInProperties(String bundleName, String serverName) {
        Object[] args = { bundleName, serverName };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MULTIPLE_PROJECTS_SPECIFIED_IN_PROPERTIES, args));
        validationException.setErrorCode(MULTIPLE_PROJECTS_SPECIFIED_IN_PROPERTIES);
        return validationException;
    }

    public static ValidationException entityClassNotFound(String entityClass, ClassLoader loader, Exception ex) {
        Object[] args = { entityClass, loader };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, ENTITY_CLASS_NOT_FOUND, args), ex);
        validationException.setErrorCode(ENTITY_CLASS_NOT_FOUND);
        return validationException;
    }

    public static ValidationException classFileTransformerThrowsException(Object transformer, String className, Exception ex) {
        Object[] args = { transformer, className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CLASS_FILE_TRANSFORMER_THROWS_EXCEPTION, args), ex);
        validationException.setErrorCode(CLASS_FILE_TRANSFORMER_THROWS_EXCEPTION);
        return validationException;
    }

    public static ValidationException jarFilesInPersistenceXmlNotSupported() {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, JAR_FILES_IN_PERSISTENCE_XML_NOT_SUPPORTED, args));
        validationException.setErrorCode(JAR_FILES_IN_PERSISTENCE_XML_NOT_SUPPORTED);
        return validationException;
    }

    public static ValidationException couldNotBindJndi(String bindName, Object bindValue, Exception ex) {
        Object[] args = { bindName, bindValue };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, COULD_NOT_BIND_JNDI, args), ex);
        validationException.setErrorCode(COULD_NOT_BIND_JNDI);
        return validationException;
    }

    public static ValidationException exceptionConfiguringEMFactory(Exception ex) {
        Object[] args = {  };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCEPTION_CONFIGURING_EM_FACTORY, args), ex);
        validationException.setErrorCode(EXCEPTION_CONFIGURING_EM_FACTORY);
        return validationException;
    }
    
    public static ValidationException excessiveJoinColumnElementsSpecified(String attributeName, Class javaClass) {
        Object[] args = { attributeName, javaClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCESSIVE_JOIN_COLUMN_ELEMENTS_SPECIFIED, args));
        validationException.setErrorCode(EXCESSIVE_JOIN_COLUMN_ELEMENTS_SPECIFIED);
        return validationException;
    }

    public static ValidationException excessiveJoinColumnsSpecified(Object annotatedElement, Class javaClass) {
        Object[] args = { annotatedElement, javaClass };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCESSIVE_JOIN_COLUMNS_SPECIFIED, args));
        validationException.setErrorCode(EXCESSIVE_JOIN_COLUMNS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException excessivePrimaryKeyJoinColumnElementsSpecified(Object annotatedElement) {
        Object[] args = { annotatedElement };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCESSIVE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED, args));
        validationException.setErrorCode(EXCESSIVE_PK_JOIN_COLUMN_ELEMENTS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException excessivePrimaryKeyJoinColumnsSpecified(Object annotatedElement) {
        Object[] args = { annotatedElement };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EXCESSIVE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED, args));
        validationException.setErrorCode(EXCESSIVE_PRIMARY_KEY_JOIN_COLUMNS_SPECIFIED);
        return validationException;
    }
    
    public static ValidationException classNotFoundWhileConvertingClassNames(String className, Exception exception) {
        Object[] args = { className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CLASS_NOT_FOUND_WHILE_CONVERTING_CLASSNAMES, args), exception);
        validationException.setErrorCode(CLASS_NOT_FOUND_WHILE_CONVERTING_CLASSNAMES);
        return validationException;
    }
    
    public static ValidationException platformDoesNotOverrideGetCreateTempTableSqlPrefix(String className) {
        Object[] args = { className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PLATFORM_DOES_NOT_OVERRIDE_GETCREATETEMPTABLESQLPREFIX, args));
        validationException.setErrorCode(PLATFORM_DOES_NOT_OVERRIDE_GETCREATETEMPTABLESQLPREFIX);
        return validationException;
    }

    public static ValidationException mappingAnnotationsAppliedToTransientAttribute(Object annotatedElement) {
        Object[] args = { annotatedElement };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MAPPING_ANNOTATIONS_APPLIED_TO_TRANSIENT_ATTRIBUTE, args));
        validationException.setErrorCode(MAPPING_ANNOTATIONS_APPLIED_TO_TRANSIENT_ATTRIBUTE);
        return validationException;
    }
    
    public static ValidationException mappingDoesNotOverrideValueFromRowInternalWithJoin(String className) {
        Object[] args = { className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MAPPING_DOES_NOT_OVERRIDE_VALUEFROMROWINTERNALWITHJOIN, args));
        validationException.setErrorCode(MAPPING_DOES_NOT_OVERRIDE_VALUEFROMROWINTERNALWITHJOIN);
        return validationException;
    }
    
    public static ValidationException mappingFileNotFound(String puName, String mf) {
        Object[] args = {puName, mf};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, MAPPING_FILE_NOT_FOUND, args));
        validationException.setErrorCode(MAPPING_FILE_NOT_FOUND);
        return validationException;
    }
    
    public static ValidationException cannotPersistExistingObject(Object registeredObject, Session session) {
        Vector key = new Vector();
        if (session != null) {
            key = session.keyFromObject(registeredObject);
        }
        Object[] args = { registeredObject, registeredObject.getClass().getName(), key, CR };
        
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CANNOT_PERSIST_MANAGED_OBJECT, args));     
        validationException.setErrorCode(CANNOT_PERSIST_MANAGED_OBJECT);
        return validationException;
    }

    public static ValidationException classNotListedInPersistenceUnit(String className) {
        Object[] args = { className };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CLASS_NOT_LISTED_IN_PERSISTENCE_UNIT, args));
        validationException.setErrorCode(CLASS_NOT_LISTED_IN_PERSISTENCE_UNIT);
        return validationException;
    }

    public static ValidationException conflictingAccessTypeForEmbeddable(Class embeddableClass) {
        Object[] args = {embeddableClass };
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, CONFLICTNG_ACCESS_TYPE_FOR_EMBEDDABLE, args));
        validationException.setErrorCode(CONFLICTNG_ACCESS_TYPE_FOR_EMBEDDABLE);
        return validationException;
    }

    public static ValidationException invalidEmbeddedAttribute(
        Class javaClass, String attributeName, Class embeddableClass) {
        Object[] args = {javaClass, attributeName, embeddableClass};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INVALID_EMBEDDED_ATTRIBUTE, args));
        validationException.setErrorCode(INVALID_EMBEDDED_ATTRIBUTE);
        return validationException;
    }

    public static ValidationException bothFieldsAndPropertiesAnnotated(Class javaClass) {
        Object[] args = {javaClass};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, BOTH_FIELDS_AND_PROPERTIES_ANNOTATED, args));
        validationException.setErrorCode(BOTH_FIELDS_AND_PROPERTIES_ANNOTATED);
        return validationException;
    }

    public static ValidationException incorrectOverridingOfAccessType(
        Class javaClass, String xmlAccessType, String annotAccessType) {
        Object[] args = {javaClass, xmlAccessType, annotAccessType};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, INCORRECT_OVERRIDING_OF_ACCESSTYPE, args));
        validationException.setErrorCode(INCORRECT_OVERRIDING_OF_ACCESSTYPE);
        return validationException;
    }

    public static ValidationException embeddedIdHasNoAttributes(Class entityClass, Class embeddableClass, String accessType) {
        Object[] args = {entityClass, embeddableClass, accessType};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, EMBEDDED_ID_CLASS_HAS_NO_ATTR, args));
        validationException.setErrorCode(EMBEDDED_ID_CLASS_HAS_NO_ATTR);
        return validationException;
    }
    
    public static ValidationException primaryKeyUpdateDisallowed(String className, String attributeName) {
        Object[] args = {className, attributeName};
        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(ValidationException.class, PRIMARY_KEY_UPDATE_DISALLOWED, args));
        validationException.setErrorCode(PRIMARY_KEY_UPDATE_DISALLOWED);
        return validationException;
    }
}
