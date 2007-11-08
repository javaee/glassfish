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

import java.lang.reflect.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <b>Purpose</b>: This exception is used for any problem that is detected with a descriptor or mapping.
 */
public class DescriptorException extends ValidationException {
    protected transient ClassDescriptor descriptor;
    protected transient DatabaseMapping mapping;
    public final static int ATTRIBUTE_AND_MAPPING_WITH_INDIRECTION_MISMATCH = 1;
    public final static int ATTRIBUTE_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH = 2;
    public final static int ATTRIBUTE_NAME_NOT_SPECIFIED = 6;
    public final static int ATTRIBUTE_TYPE_NOT_VALID = 7;
    public final static int CLASS_INDICATOR_FIELD_NOT_FOUND = 8;
    public final static int DIRECT_FIELD_NAME_NOT_SET = 9;
    public final static int FIELD_NAME_NOT_SET_IN_MAPPING = 10;
    public final static int FOREIGN_KEYS_DEFINED_INCORRECTLY = 11;
    public final static int IDENTITY_MAP_NOT_SPECIFIED = 12;
    public final static int ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 13;
    public final static int ILLEGAL_ACCESS_WHILE_CLONING = 14;
    public final static int ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION = 15;
    public final static int ILLEGAL_ACCESS_WHILE_EVENT_EXECUTION = 16;
    public final static int ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR = 17;
    public final static int ILLEGAL_ACCESS_WHILE_INSTANTIATING_METHOD_BASED_PROXY = 18;
    public final static int ILLEGAL_ACCESS_WHILE_INVOKING_ATTRIBUTE_METHOD = 19;
    public final static int ILLEGAL_ACCESS_WHILE_INVOKING_FIELD_TO_METHOD = 20;
    public final static int ILLEGAL_ACCESS_WHILE_INVOKING_ROW_EXTRACTION_METHOD = 21;
    public final static int ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION = 22;
    public final static int ILLEGAL_ACCESS_WHILE_OBSOLETE_EVENT_EXECUTION = 23;
    public final static int ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 24;
    public final static int ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR = 25;
    public final static int ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 26;
    public final static int ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR = 27;
    public final static int ILLEGAL_ARGUMENT_WHILE_INSTANTIATING_METHOD_BASED_PROXY = 28;
    public final static int ILLEGAL_ARGUMENT_WHILE_INVOKING_ATTRIBUTE_METHOD = 29;
    public final static int ILLEGAL_ARGUMENT_WHILE_INVOKING_FIELD_TO_METHOD = 30;
    public final static int ILLEGAL_ARGUMENT_WHILE_OBSOLETE_EVENT_EXECUTION = 31;
    public final static int ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 32;
    public final static int ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR = 33;
    public final static int INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION = 34;
    public final static int INVALID_DATA_MODIFICATION_EVENT = 35;
    public final static int INVALID_DATA_MODIFICATION_EVENT_CODE = 36;
    public final static int INVALID_DESCRIPTOR_EVENT_CODE = 37;
    public final static int INVALID_IDENTITY_MAP = 38;
    public final static int JAVA_CLASS_NOT_SPECIFIED = 39;
    public final static int DESCRIPTOR_FOR_INTERFACE_IS_MISSING = 40;
    public final static int MAPPING_FOR_SEQUENCE_NUMBER_FIELD = 41;
    public final static int MISSING_CLASS_FOR_INDICATOR_FIELD_VALUE = 43;
    public final static int MISSING_CLASS_INDICATOR_FIELD = 44;
    public final static int MISSING_MAPPING_FOR_FIELD = 45;
    public final static int NO_MAPPING_FOR_PRIMARY_KEY = 46;
    public final static int MULTIPLE_TABLE_PRIMARY_KEY_NOT_SPECIFIED = 47;
    public final static int MULTIPLE_WRITE_MAPPINGS_FOR_FIELD = 48;
    public final static int NO_ATTRIBUTE_TRANSFORMATION_METHOD = 49;
    public final static int NO_FIELD_NAME_FOR_MAPPING = 50;
    public final static int NO_FOREIGN_KEYS_ARE_SPECIFIED = 51;
    public final static int NO_REFERENCE_KEY_IS_SPECIFIED = 52;
    public final static int NO_RELATION_TABLE = 53;
    public final static int NO_SOURCE_RELATION_KEYS_SPECIFIED = 54;
    public final static int NO_SUCH_METHOD_ON_FIND_OBSOLETE_METHOD = 55;
    public final static int NO_SUCH_METHOD_ON_INITIALIZING_ATTRIBUTE_METHOD = 56;
    public final static int NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION = 57;
    public final static int NO_SUCH_METHOD_WHILE_CONVERTING_TO_METHOD = 58;
    public final static int NO_SUCH_FIELD_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR = 59;
    public final static int NO_SUCH_METHOD_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR = 60;
    public final static int NO_SUCH_METHOD_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD = 61;
    public final static int NO_SUCH_METHOD_WHILE_INITIALIZING_COPY_POLICY = 62;
    public final static int NO_SUCH_METHOD_WHILE_INITIALIZING_INSTANTIATION_POLICY = 63;
    public final static int NO_TARGET_FOREIGN_KEYS_SPECIFIED = 64;
    public final static int NO_TARGET_RELATION_KEYS_SPECIFIED = 65;
    public final static int NOT_DESERIALIZABLE = 66;
    public final static int NOT_SERIALIZABLE = 67;
    public final static int NULL_FOR_NON_NULL_AGGREGATE = 68;
    public final static int NULL_POINTER_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 69;
    public final static int NULL_POINTER_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR = 70;
    public final static int NULL_POINTER_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR = 71;
    public final static int NULL_POINTER_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR = 72;
    public final static int PARENT_DESCRIPTOR_NOT_SPECIFIED = 73;
    public final static int PRIMARY_KEY_FIELDS_NOT_SPECIFIED = 74;
    public final static int REFERENCE_CLASS_NOT_SPECIFIED = 75;
    public final static int REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATE = 77;
    public final static int REFERENCE_KEY_FIELD_NOT_PROPERLY_SPECIFIED = 78;
    public final static int REFERENCE_TABLE_NOT_SPECIFIED = 79;
    public final static int RELATION_KEY_FIELD_NOT_PROPERLY_SPECIFIED = 80;
    public final static int RETURN_TYPE_IN_GET_ATTRIBUTE_ACCESSOR = 81;
    public final static int SECURITY_ON_FIND_METHOD = 82;
    public final static int SECURITY_ON_FIND_OBSOLETE_METHOD = 83;
    public final static int SECURITY_ON_INITIALIZING_ATTRIBUTE_METHOD = 84;
    public final static int SECURITY_WHILE_CONVERTING_TO_METHOD = 85;
    public final static int SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR = 86;
    public final static int SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR = 87;
    public final static int SECURITY_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD = 88;
    public final static int SECURITY_WHILE_INITIALIZING_COPY_POLICY = 89;
    public final static int SECURITY_WHILE_INITIALIZING_INSTANTIATION_POLICY = 90;
    public final static int SEQUENCE_NUMBER_PROPERTY_NOT_SPECIFIED = 91;
    public final static int SIZE_MISMATCH_OF_FOREIGN_KEYS = 92;
    public final static int TABLE_NOT_PRESENT = 93;
    public final static int TABLE_NOT_SPECIFIED = 94;
    public final static int TARGET_FOREIGN_KEYS_SIZE_MISMATCH = 96;
    public final static int TARGET_INVOCATION_WHILE_CLONING = 97;
    public final static int TARGET_INVOCATION_WHILE_EVENT_EXECUTION = 98;
    public final static int TARGET_INVOCATION_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR = 99;
    public final static int TARGET_INVOCATION_WHILE_INSTANTIATING_METHOD_BASED_PROXY = 100;
    public final static int TARGET_INVOCATION_WHILE_INVOKING_ATTRIBUTE_METHOD = 101;
    public final static int TARGET_INVOCATION_WHILE_INVOKING_FIELD_TO_METHOD = 102;
    public final static int TARGET_INVOCATION_WHILE_INVOKING_ROW_EXTRACTION_METHOD = 103;
    public final static int TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION = 104;
    public final static int TARGET_INVOCATION_WHILE_OBSOLETE_EVENT_EXECUTION = 105;
    public final static int TARGET_INVOCATION_WHILE_SETTING_VALUE_THRU_METHOD_ACESSOR = 106;
    public final static int VALUE_NOT_FOUND_IN_CLASS_INDICATOR_MAPPING = 108;
    public final static int WRITE_LOCK_FIELD_IN_CHILD_DESCRIPTOR = 109;
    public final static int DESCRIPTOR_IS_MISSING = 110;
    public final static int MULTIPLE_TABLE_PRIMARY_KEY_MUST_BE_FULLY_QUALIFIED = 111;
    public final static int ONLY_ONE_TABLE_CAN_BE_ADDED_WITH_THIS_METHOD = 112;
    public final static int NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION = 113;
    public final static int NULL_POINTER_WHILE_METHOD_INSTANTIATION = 114;
    public final static int NO_ATTRBUTE_VALUE_CONVERSION_TO_FIELD_VALUE_PROVIDED = 115;
    public final static int NO_FIELD_VALUE_CONVERSION_TO_ATTRIBUTE_VALUE_PROVIDED = 116;
    public final static int LOCK_MAPPING_CANNOT_BE_READONLY = 118;
    public final static int LOCK_MAPPING_MUST_BE_READONLY = 119;
    public final static int CHILD_DOES_NOT_DEFINE_ABSTRACT_QUERY_KEY = 120;
    public final static int SET_EXISTENCE_CHECKING_NOT_UNDERSTOOD = 122;
    public final static int VALUE_HOLDER_INSTANTIATION_MISMATCH = 125;
    public final static int NO_SUB_CLASS_MATCH = 126;
    public final static int RETURN_AND_MAPPING_WITH_INDIRECTION_MISMATCH = 127;
    public final static int RETURN_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH = 128;
    public final static int PARAMETER_AND_MAPPING_WITH_INDIRECTION_MISMATCH = 129;
    public final static int PARAMETER_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH = 130;
    public final static int GET_METHOD_RETURN_TYPE_NOT_VALID = 131;
    public final static int SET_METHOD_PARAMETER_TYPE_NOT_VALID = 133;
    public final static int ILLEGAL_TABLE_NAME_IN_MULTIPLE_TABLE_FOREIGN_KEY = 135;
    public final static int ATTRIBUTE_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH = 138;
    public final static int RETURN_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH = 139;
    public final static int PARAMETER_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH = 140;
    public final static int FIELD_IS_NOT_PRESENT_IN_DATABASE = 141;
    public final static int TABLE_IS_NOT_PRESENT_IN_DATABASE = 142;
    public final static int MULTIPLE_TABLE_INSERT_ORDER_MISMATCH = 143;
    public final static int INVALID_USE_OF_TRANSPARENT_INDIRECTION = 144;
    public final static int MISSING_INDIRECT_CONTAINER_CONSTRUCTOR = 145;
    public final static int COULD_NOT_INSTANTIATE_INDIRECT_CONTAINER_CLASS = 146;
    public final static int INVALID_CONTAINER_POLICY = 147;
    public final static int INVALID_CONTAINER_POLICY_WITH_TRANSPARENT_INDIRECTION = 148;
    public final static int INVALID_USE_OF_NO_INDIRECTION = 149;
    public final static int INDIRECT_CONTAINER_INSTANTIATION_MISMATCH = 150;
    public final static int INVALID_MAPPING_OPERATION = 151;
    public final static int INVALID_INDIRECTION_POLICY_OPERATION = 152;
    public final static int REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATECOLLECTION = 153;
    public final static int INVALID_INDIRECTION_CONTAINER_CLASS = 154;
    public final static int MISSING_FOREIGN_KEY_TRANSLATION = 155;
    public final static int STRUCTURE_NAME_NOT_SET_IN_MAPPING = 156;
    public final static int NORMAL_DESCRIPTORS_DO_NOT_SUPPORT_NON_RELATIONAL_EXTENSIONS = 157;
    public final static int PARENT_CLASS_IS_SELF = 158;
    public final static int PROXY_INDIRECTION_NOT_AVAILABLE = 159;
    public final static int INVALID_ATTRIBUTE_TYPE_FOR_PROXY_INDIRECTION = 160;
    public final static int INVALID_GET_RETURN_TYPE_FOR_PROXY_INDIRECTION = 161;
    public final static int INVALID_SET_PARAMETER_TYPE_FOR_PROXY_INDIRECTION = 162;
    public final static int INCORRECT_COLLECTION_POLICY = 163;
    public final static int INVALID_AMENDMENT_METHOD = 164;
    public final static int ERROR_OCCURED_IN_AMENDMENT_METHOD = 165;
    public final static int VARIABLE_ONE_TO_ONE_MAPPING_IS_NOT_DEFINED = 166;
    public final static int NO_CONSTRUCTOR_INDIRECT_COLLECTION_CLASS = 167;
    public final static int TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION = 168;
    public final static int TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY = 169;
    public final static int ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY = 170;
    public final static int INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY = 171;
    public final static int NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY = 172;
    public final static int NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY = 173;
    public final static int ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION_OF_FACTORY = 174;
    public final static int TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION_OF_FACTORY = 175;
    public final static int NULL_POINTER_WHILE_METHOD_INSTANTIATION_OF_FACTORY = 176;
    public final static int NO_MAPPING_FOR_ATTRIBUTENAME = 177;
    public final static int NO_MAPPING_FOR_ATTRIBUTENAME_IN_ENTITY_BEAN = 178;
    public final static int UNSUPPORTED_TYPE_FOR_BIDIRECTIONAL_RELATIONSHIP_MAINTENANCE = 179;// Bug 2618982
    public final static int REFERENCE_DESCRIPTOR_CANNOT_BE_AGGREGATE = 180;
    public final static int ATTRIBUTE_TRANSFORMER_CLASS_NOT_FOUND = 181;
    public final static int FIELD_TRANSFORMER_CLASS_NOT_FOUND = 182;
    public final static int ATTRIBUTE_TRANSFORMER_CLASS_INVALID = 183;
    public final static int FIELD_TRANSFORMER_CLASS_INVALID = 184;
    public final static int RETURNING_POLICY_FIELD_TYPE_CONFLICT = 185;
    public final static int RETURNING_POLICY_FIELD_INSERT_CONFLICT = 186;
    public final static int RETURNING_POLICY_AND_DESCRIPTOR_FIELD_TYPE_CONFLICT = 187;
    public final static int RETURNING_POLICY_UNMAPPED_FIELD_TYPE_NOT_SET = 188;
    public final static int RETURNING_POLICY_MAPPED_FIELD_TYPE_NOT_SET = 189;
    public final static int RETURNING_POLICY_MAPPING_NOT_SUPPORTED = 190;
    public final static int RETURNING_POLICY_FIELD_NOT_SUPPORTED = 191;
    public final static int CUSTOM_QUERY_AND_RETURNING_POLICY_CONFLICT = 192;
    public final static int NO_CUSTOM_QUERY_FOR_RETURNING_POLICY = 193;
    public final static int CLASS_EXTRACTION_METHOD_MUST_BE_STATIC = 194;
    public final static int ISOLATED_DESCRIPTOR_REFERENCED_BY_SHARED_DESCRIPTOR = 195;
    public final static int UPDATE_ALL_FIELDS_NOT_SET = 196;
    public final static int INVALID_MAPPING_TYPE = 197;
    public final static int NEED_TO_IMPLEMENT_CHANGETRACKER = 198;
    public final static int NEED_TO_IMPLEMENT_FETCHGROUPTRACKER = 199;
    public final static int ATTEMPT_TO_REGISTER_DEAD_INDIRECTION = 200;
    public final static int INTERNAL_ERROR_ACCESSING_PKFIELD = 201;
    public final static int INTERNAL_ERROR_SET_METHOD = 202;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected DescriptorException(String theMessage) {
        super(theMessage);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected DescriptorException(String theMessage, DatabaseMapping mapping) {
        this(theMessage);
        if (mapping != null) {
            this.mapping = mapping;
            this.descriptor = mapping.getDescriptor();
        }
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected DescriptorException(String theMessage, ClassDescriptor descriptor) {
        this(theMessage);
        this.descriptor = descriptor;
    }

    protected DescriptorException(String theMessage, ClassDescriptor descriptor, Throwable exception) {
        this(theMessage, descriptor);
        setInternalException(exception);
    }

    public static DescriptorException attemptToRegisterDeadIndirection(Object object, DatabaseMapping mapping) {
        Object[] args = { object };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTEMPT_TO_REGISTER_DEAD_INDIRECTION, args), mapping);
        exception.setErrorCode(ATTEMPT_TO_REGISTER_DEAD_INDIRECTION);
        return exception;
    }

    public static DescriptorException attributeAndMappingWithIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTRIBUTE_AND_MAPPING_WITH_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(ATTRIBUTE_AND_MAPPING_WITH_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException attributeAndMappingWithoutIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTRIBUTE_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(ATTRIBUTE_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException attributeAndMappingWithTransparentIndirectionMismatch(DatabaseMapping mapping, String validTypeName) {
        Object[] args = { mapping.getAttributeName(), validTypeName };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTRIBUTE_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(ATTRIBUTE_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException attributeNameNotSpecified() {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTRIBUTE_NAME_NOT_SPECIFIED, args));
        descriptorException.setErrorCode(ATTRIBUTE_NAME_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException attributeTypeNotValid(CollectionMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ATTRIBUTE_TYPE_NOT_VALID, args), mapping);
        exception.setErrorCode(ATTRIBUTE_TYPE_NOT_VALID);
        return exception;
    }

    public static DescriptorException childDoesNotDefineAbstractQueryKeyOfParent(ClassDescriptor child, ClassDescriptor parent, String queryKeyName) {
        Object[] args = { queryKeyName, parent, child };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, CHILD_DOES_NOT_DEFINE_ABSTRACT_QUERY_KEY, args));
        descriptorException.setErrorCode(CHILD_DOES_NOT_DEFINE_ABSTRACT_QUERY_KEY);
        return descriptorException;
    }

    public static DescriptorException classIndicatorFieldNotFound(ClassDescriptor parentDescriptor, ClassDescriptor descriptor) {
        Object[] args = { descriptor, parentDescriptor, CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, CLASS_INDICATOR_FIELD_NOT_FOUND, args), descriptor);
        descriptorException.setErrorCode(CLASS_INDICATOR_FIELD_NOT_FOUND);
        return descriptorException;
    }

    public static DescriptorException couldNotInstantiateIndirectContainerClass(Class containerClass, Exception exception) {
        Object[] args = { containerClass, Helper.getShortClassName(containerClass) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, COULD_NOT_INSTANTIATE_INDIRECT_CONTAINER_CLASS, args));
        descriptorException.setErrorCode(COULD_NOT_INSTANTIATE_INDIRECT_CONTAINER_CLASS);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException descriptorForInterfaceIsMissing(String interfaceName) {
        Object[] args = { interfaceName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, DESCRIPTOR_FOR_INTERFACE_IS_MISSING, args));
        descriptorException.setErrorCode(DESCRIPTOR_FOR_INTERFACE_IS_MISSING);
        return descriptorException;
    }

    public static DescriptorException descriptorIsMissing(String className, DatabaseMapping mapping) {
        Object[] args = { className };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, DESCRIPTOR_IS_MISSING, args), mapping);
        descriptorException.setErrorCode(DESCRIPTOR_IS_MISSING);
        return descriptorException;
    }

    public static DescriptorException directFieldNameNotSet(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, DIRECT_FIELD_NAME_NOT_SET, args), mapping);
        descriptorException.setErrorCode(DIRECT_FIELD_NAME_NOT_SET);
        return descriptorException;
    }

    public static DescriptorException errorOccuredInAmendmentMethod(Class amendmentClass, String method, Exception exception, ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ERROR_OCCURED_IN_AMENDMENT_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(ERROR_OCCURED_IN_AMENDMENT_METHOD);
        return descriptorException;
    }

    public static DescriptorException fieldIsNotPresentInDatabase(ClassDescriptor descriptor, String tableName, String fieldName) {
        Object[] args = { fieldName, tableName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, FIELD_IS_NOT_PRESENT_IN_DATABASE, args), descriptor);
        descriptorException.setErrorCode(FIELD_IS_NOT_PRESENT_IN_DATABASE);
        return descriptorException;
    }

    public static DescriptorException fieldNameNotSetInMapping(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, FIELD_NAME_NOT_SET_IN_MAPPING, args), mapping);
        descriptorException.setErrorCode(FIELD_NAME_NOT_SET_IN_MAPPING);
        return descriptorException;
    }

    public static DescriptorException foreignKeysDefinedIncorrectly(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, FOREIGN_KEYS_DEFINED_INCORRECTLY, args), mapping);
        descriptorException.setErrorCode(FOREIGN_KEYS_DEFINED_INCORRECTLY);
        return descriptorException;
    }

    /**
     * PUBLIC:
     * Return the descriptor that the problem was detected in.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * PUBLIC:
     * Return the mapping that the problem was detected in.
     */
    public DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * PUBLIC:
     * Return the exception error message.
     * TopLink error messages are multi-line so that detail descriptions of the exception are given.
     */
    public String getMessage() {
        if (getDescriptor() == null) {
            return super.getMessage();
        }

        if (getMapping() != null) {
            return super.getMessage() + cr() + getIndentationString() + ExceptionMessageGenerator.getHeader("MappingHeader") + getMapping().toString() + cr() + getIndentationString() + ExceptionMessageGenerator.getHeader("DescriptorHeader") + getDescriptor().toString();
        } else {
            return super.getMessage() + cr() + getIndentationString() + ExceptionMessageGenerator.getHeader("DescriptorHeader") + getDescriptor().toString();
        }
    }

    public static DescriptorException getMethodReturnTypeNotValid(CollectionMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, GET_METHOD_RETURN_TYPE_NOT_VALID, args), mapping);
        exception.setErrorCode(GET_METHOD_RETURN_TYPE_NOT_VALID);
        return exception;
    }

    public static DescriptorException identityMapNotSpecified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, IDENTITY_MAP_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(IDENTITY_MAP_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException illegalAccesstWhileGettingValueThruInstanceVaraibleAccessor(String attributeName, String objectName, Throwable exception) {
        Object[] args = { attributeName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileCloning(Object domainObject, String methodName, ClassDescriptor descriptor, Throwable exception) {
        Object[] args = { domainObject, methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_CLONING, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_CLONING);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileConstructorInstantiation(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileConstructorInstantiationOfFactory(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileEventExecution(String eventMethodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { eventMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_EVENT_EXECUTION, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_EVENT_EXECUTION);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileGettingValueThruMethodAccessor(String methodName, String objectName, Throwable exception) {
        Object[] args = { methodName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileInstantiatingMethodBasedProxy(Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_INSTANTIATING_METHOD_BASED_PROXY, args));
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_INSTANTIATING_METHOD_BASED_PROXY);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileInvokingAttributeMethod(DatabaseMapping mapping, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_INVOKING_ATTRIBUTE_METHOD, args));
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_INVOKING_ATTRIBUTE_METHOD);
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileInvokingFieldToMethod(String methodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_INVOKING_FIELD_TO_METHOD, args));
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_INVOKING_FIELD_TO_METHOD);
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileInvokingRowExtractionMethod(AbstractRecord row, Method method, ClassDescriptor descriptor, Throwable exception) {
        Object[] args = { row, method };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_INVOKING_ROW_EXTRACTION_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_INVOKING_ROW_EXTRACTION_METHOD);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileMethodInstantiation(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileMethodInstantiationOfFactory(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_METHOD_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileObsoleteEventExecute(String eventMethodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { eventMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_OBSOLETE_EVENT_EXECUTION, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_OBSOLETE_EVENT_EXECUTION);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileSettingValueThruInstanceVariableAccessor(String attributeName, String objectName, Object objectValue, Throwable exception) {
        Object[] args = { attributeName, objectName, String.valueOf(objectValue), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalAccessWhileSettingValueThruMethodAccessor(String setMethodName, Object value, Throwable exception) {
        Object[] args = { setMethodName, String.valueOf(value) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ACCESS_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileGettingValueThruInstanceVariableAccessor(String attributeName, String typeName, String objectName, Throwable exception) {
        Object[] args = { attributeName, typeName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileGettingValueThruMethodAccessor(String methodName, String objectName, Throwable exception) {
        Object[] args = { methodName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileInstantiatingMethodBasedProxy(Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_INSTANTIATING_METHOD_BASED_PROXY, args));
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_INSTANTIATING_METHOD_BASED_PROXY);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileInvokingAttributeMethod(DatabaseMapping mapping, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_INVOKING_ATTRIBUTE_METHOD, args));
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_INVOKING_ATTRIBUTE_METHOD);
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileInvokingFieldToMethod(String methodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_INVOKING_FIELD_TO_METHOD, args));
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_INVOKING_FIELD_TO_METHOD);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileObsoleteEventExecute(String eventMethodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { eventMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_OBSOLETE_EVENT_EXECUTION, args), descriptor, exception);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_OBSOLETE_EVENT_EXECUTION);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileSettingValueThruInstanceVariableAccessor(String attributeName, String typeName, Object value, Throwable exception) {
        Object[] args = { String.valueOf(value), attributeName, typeName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalArgumentWhileSettingValueThruMethodAccessor(String setMethodName, Object value, Throwable exception) {
        Object[] args = { setMethodName, String.valueOf(value) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(ILLEGAL_ARGUMENT_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException illegalTableNameInMultipleTableForeignKeyField(ClassDescriptor descriptor, DatabaseTable table) {
        Object[] args = { table };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ILLEGAL_TABLE_NAME_IN_MULTIPLE_TABLE_FOREIGN_KEY, args), descriptor);
        descriptorException.setErrorCode(ILLEGAL_TABLE_NAME_IN_MULTIPLE_TABLE_FOREIGN_KEY);
        return descriptorException;
    }

    public static DescriptorException incorrectCollectionPolicy(DatabaseMapping mapping, Class attributeClass, Class containerClass) {
        Object[] args = { attributeClass, containerClass };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INCORRECT_COLLECTION_POLICY, args), mapping);
        descriptorException.setErrorCode(INCORRECT_COLLECTION_POLICY);
        return descriptorException;
    }

    public static DescriptorException indirectContainerInstantiationMismatch(Object attributeValue, DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName(), attributeValue, CR };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INDIRECT_CONTAINER_INSTANTIATION_MISMATCH, args), mapping);
        exception.setErrorCode(INDIRECT_CONTAINER_INSTANTIATION_MISMATCH);
        return exception;
    }

    public static DescriptorException instantiationWhileConstructorInstantiation(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException instantiationWhileConstructorInstantiationOfFactory(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(INSTANTIATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException invalidAmendmentMethod(Class amendmentClass, String method, Exception exception, ClassDescriptor descriptor) {
        Object[] args = { amendmentClass, method, CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_AMENDMENT_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(INVALID_AMENDMENT_METHOD);
        return descriptorException;
    }

    public static DescriptorException invalidAttributeTypeForProxyIndirection(Class attributeType, Class[] targetInterfaces, DatabaseMapping mapping) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < targetInterfaces.length; i++) {
            buffer.append(((Class)targetInterfaces[i]).getName());
            if (i != (targetInterfaces.length - 1)) {
                buffer.append(", ");
            }
        }

        Object[] args = { mapping.getAttributeName(), mapping.getDescriptor().getJavaClass().getName(), attributeType.getName(), buffer.toString(), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_ATTRIBUTE_TYPE_FOR_PROXY_INDIRECTION, args), mapping);
        descriptorException.setErrorCode(INVALID_ATTRIBUTE_TYPE_FOR_PROXY_INDIRECTION);
        return descriptorException;
    }

    public static DescriptorException invalidContainerPolicy(oracle.toplink.essentials.internal.queryframework.ContainerPolicy containerPolicy, Class javaClass) {
        Object[] args = { containerPolicy, javaClass };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_CONTAINER_POLICY, args));
        exception.setErrorCode(INVALID_CONTAINER_POLICY);
        return exception;
    }

    public static DescriptorException invalidContainerPolicyWithTransparentIndirection(DatabaseMapping mapping, oracle.toplink.essentials.internal.queryframework.ContainerPolicy containerPolicy) {
        Object[] args = { containerPolicy };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_CONTAINER_POLICY_WITH_TRANSPARENT_INDIRECTION, args), mapping);
        exception.setErrorCode(INVALID_CONTAINER_POLICY_WITH_TRANSPARENT_INDIRECTION);
        return exception;
    }

    public static DescriptorException invalidDataModificationEvent(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_DATA_MODIFICATION_EVENT, args), mapping);
        descriptorException.setErrorCode(INVALID_DATA_MODIFICATION_EVENT);
        return descriptorException;
    }

    public static DescriptorException invalidDataModificationEventCode(Object event, ForeignReferenceMapping mapping) {
        Object[] args = { event };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_DATA_MODIFICATION_EVENT_CODE, args), mapping);
        descriptorException.setErrorCode(INVALID_DATA_MODIFICATION_EVENT_CODE);
        return descriptorException;
    }

    public static DescriptorException invalidDescriptorEventCode(DescriptorEvent event, ClassDescriptor descriptor) {
        Object[] args = { new Integer(event.getEventCode()) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_DESCRIPTOR_EVENT_CODE, args), descriptor);
        descriptorException.setErrorCode(INVALID_DESCRIPTOR_EVENT_CODE);
        return descriptorException;
    }

    public static DescriptorException invalidGetMethodReturnTypeForProxyIndirection(Class attributeType, Class[] targetInterfaces, DatabaseMapping mapping) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < targetInterfaces.length; i++) {
            buffer.append(((Class)targetInterfaces[i]).getName());
            if (i != (targetInterfaces.length - 1)) {
                buffer.append(", ");
            }
        }

        Object[] args = { mapping.getGetMethodName(), mapping.getDescriptor().getJavaClass().getName(), attributeType.getName(), buffer.toString(), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_GET_RETURN_TYPE_FOR_PROXY_INDIRECTION, args), mapping);
        descriptorException.setErrorCode(INVALID_GET_RETURN_TYPE_FOR_PROXY_INDIRECTION);
        return descriptorException;
    }

    public static DescriptorException invalidIdentityMap(ClassDescriptor descriptor, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_IDENTITY_MAP, args), descriptor, exception);
        descriptorException.setErrorCode(INVALID_IDENTITY_MAP);
        return descriptorException;
    }

    public static DescriptorException invalidIndirectionPolicyOperation(oracle.toplink.essentials.internal.indirection.IndirectionPolicy policy, String operation) {
        Object[] args = { policy, operation };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_INDIRECTION_POLICY_OPERATION, args), policy.getMapping());
        descriptorException.setErrorCode(INVALID_INDIRECTION_POLICY_OPERATION);
        return descriptorException;
    }

    public static DescriptorException invalidMappingOperation(DatabaseMapping mapping, String operation) {
        Object[] args = { operation };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_MAPPING_OPERATION, args), mapping);
        descriptorException.setErrorCode(INVALID_MAPPING_OPERATION);
        return descriptorException;
    }

    public static DescriptorException invalidSetMethodParameterTypeForProxyIndirection(Class attributeType, Class[] targetInterfaces, DatabaseMapping mapping) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < targetInterfaces.length; i++) {
            buffer.append(((Class)targetInterfaces[i]).getName());
            if (i != (targetInterfaces.length - 1)) {
                buffer.append(", ");
            }
        }

        Object[] args = { mapping.getSetMethodName(), mapping.getDescriptor().getJavaClass().getName(), attributeType.getName(), buffer.toString(), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_SET_PARAMETER_TYPE_FOR_PROXY_INDIRECTION, args), mapping);
        descriptorException.setErrorCode(INVALID_SET_PARAMETER_TYPE_FOR_PROXY_INDIRECTION);
        return descriptorException;
    }

    public static DescriptorException invalidUseOfNoIndirection(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_USE_OF_NO_INDIRECTION, args), mapping);
        exception.setErrorCode(INVALID_USE_OF_NO_INDIRECTION);
        return exception;
    }

    public static DescriptorException invalidUseOfTransparentIndirection(DatabaseMapping mapping) {
        Object[] args = {  };

        String message = oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_USE_OF_TRANSPARENT_INDIRECTION, args);
        DescriptorException exception = new DescriptorException(message, mapping);
        exception.setErrorCode(INVALID_USE_OF_TRANSPARENT_INDIRECTION);
        return exception;
    }

    public static DescriptorException isolateDescriptorReferencedBySharedDescriptor(String referenceClass, String sourceClass, DatabaseMapping mapping) {
        Object[] args = { referenceClass, sourceClass };

        String message = oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DescriptorException.class, ISOLATED_DESCRIPTOR_REFERENCED_BY_SHARED_DESCRIPTOR, args);
        DescriptorException exception = new DescriptorException(message, mapping);
        exception.setErrorCode(ISOLATED_DESCRIPTOR_REFERENCED_BY_SHARED_DESCRIPTOR);
        return exception;
    }

    public static DescriptorException javaClassNotSpecified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, JAVA_CLASS_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(JAVA_CLASS_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException mappingCanNotBeReadOnly(DatabaseMapping mapping) {
        Object[] args = { mapping.getDescriptor().getClass() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, LOCK_MAPPING_CANNOT_BE_READONLY, args), mapping);
        descriptorException.setErrorCode(LOCK_MAPPING_CANNOT_BE_READONLY);
        return descriptorException;
    }

    public static DescriptorException mappingForSequenceNumberField(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MAPPING_FOR_SEQUENCE_NUMBER_FIELD, args));
        descriptorException.setErrorCode(MAPPING_FOR_SEQUENCE_NUMBER_FIELD);
        descriptorException.setDescriptor(descriptor);
        return descriptorException;
    }

    public static DescriptorException missingClassForIndicatorFieldValue(Object classFieldValue, ClassDescriptor descriptor) {
        Object[] args = { classFieldValue, classFieldValue.getClass() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MISSING_CLASS_FOR_INDICATOR_FIELD_VALUE, args), descriptor);
        descriptorException.setErrorCode(MISSING_CLASS_FOR_INDICATOR_FIELD_VALUE);
        return descriptorException;
    }

    public static DescriptorException missingClassIndicatorField(AbstractRecord row, ClassDescriptor descriptor) {
        Object[] args = { row };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MISSING_CLASS_INDICATOR_FIELD, args), descriptor);
        descriptorException.setErrorCode(MISSING_CLASS_INDICATOR_FIELD);
        return descriptorException;
    }

    public static DescriptorException missingForeignKeyTranslation(ForeignReferenceMapping mapping, DatabaseField primaryKeyField) {
        Object[] args = { primaryKeyField };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MISSING_FOREIGN_KEY_TRANSLATION, args), mapping);
        exception.setErrorCode(MISSING_FOREIGN_KEY_TRANSLATION);
        return exception;
    }

    public static DescriptorException missingIndirectContainerConstructor(Class containerClass) {
        Object[] args = { containerClass.getName(), containerClass.getName() + "()" };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MISSING_INDIRECT_CONTAINER_CONSTRUCTOR, args));
        exception.setErrorCode(MISSING_INDIRECT_CONTAINER_CONSTRUCTOR);
        return exception;
    }

    public static DescriptorException missingMappingForField(DatabaseField field, ClassDescriptor descriptor) {
        Object[] args = { field };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MISSING_MAPPING_FOR_FIELD, args));
        descriptorException.setErrorCode(MISSING_MAPPING_FOR_FIELD);
        descriptorException.setDescriptor(descriptor);
        return descriptorException;
    }

    public static DescriptorException multipleTableInsertOrderMismatch(ClassDescriptor aDescriptor) {
        Object[] args = { aDescriptor.getMultipleTableInsertOrder(), aDescriptor.getTables(), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MULTIPLE_TABLE_INSERT_ORDER_MISMATCH, args));
        descriptorException.setErrorCode(MULTIPLE_TABLE_INSERT_ORDER_MISMATCH);
        descriptorException.setDescriptor(aDescriptor);
        return descriptorException;
    }

    public static DescriptorException multipleTablePrimaryKeyMustBeFullyQualified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MULTIPLE_TABLE_PRIMARY_KEY_MUST_BE_FULLY_QUALIFIED, args), descriptor);
        descriptorException.setErrorCode(MULTIPLE_TABLE_PRIMARY_KEY_MUST_BE_FULLY_QUALIFIED);
        return descriptorException;
    }

    public static DescriptorException multipleTablePrimaryKeyNotSpecified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MULTIPLE_TABLE_PRIMARY_KEY_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(MULTIPLE_TABLE_PRIMARY_KEY_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException multipleWriteMappingsForField(String fieldName, DatabaseMapping mapping) {
        Object[] args = { fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, MULTIPLE_WRITE_MAPPINGS_FOR_FIELD, args), mapping);
        descriptorException.setErrorCode(MULTIPLE_WRITE_MAPPINGS_FOR_FIELD);
        return descriptorException;
    }

    public static DescriptorException mustBeReadOnlyMappingWhenStoredInCache(DatabaseMapping mapping) {
        Object[] args = { mapping.getDescriptor().getJavaClass() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, LOCK_MAPPING_MUST_BE_READONLY, args), mapping);
        descriptorException.setErrorCode(LOCK_MAPPING_MUST_BE_READONLY);
        return descriptorException;
    }

    public static DescriptorException noAttributeTransformationMethod(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_ATTRIBUTE_TRANSFORMATION_METHOD, args), mapping);
        descriptorException.setErrorCode(NO_ATTRIBUTE_TRANSFORMATION_METHOD);
        return descriptorException;
    }

    public static DescriptorException noAttributeValueConversionToFieldValueProvided(Object attributeValue, DatabaseMapping mapping) {
        Object[] args = { attributeValue };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_ATTRBUTE_VALUE_CONVERSION_TO_FIELD_VALUE_PROVIDED, args), mapping);
        descriptorException.setErrorCode(NO_ATTRBUTE_VALUE_CONVERSION_TO_FIELD_VALUE_PROVIDED);
        return descriptorException;
    }

    public static DescriptorException noFieldNameForMapping(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_FIELD_NAME_FOR_MAPPING, args), mapping);
        descriptorException.setErrorCode(NO_FIELD_NAME_FOR_MAPPING);
        return descriptorException;
    }

    // CR#3779 - Added field as an argument
    public static DescriptorException noFieldValueConversionToAttributeValueProvided(Object fieldValue, DatabaseField field, DatabaseMapping mapping) {
        Object[] args = { fieldValue, field };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_FIELD_VALUE_CONVERSION_TO_ATTRIBUTE_VALUE_PROVIDED, args), mapping);
        descriptorException.setErrorCode(NO_FIELD_VALUE_CONVERSION_TO_ATTRIBUTE_VALUE_PROVIDED);
        return descriptorException;
    }

    public static DescriptorException noForeignKeysAreSpecified(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_FOREIGN_KEYS_ARE_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(NO_FOREIGN_KEYS_ARE_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException noMappingForPrimaryKey(DatabaseField field, ClassDescriptor descriptor) {
        Object[] args = { field };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_MAPPING_FOR_PRIMARY_KEY, args), descriptor);
        descriptorException.setErrorCode(NO_MAPPING_FOR_PRIMARY_KEY);
        return descriptorException;
    }

    public static DescriptorException noReferenceKeyIsSpecified(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_REFERENCE_KEY_IS_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(NO_REFERENCE_KEY_IS_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException noRelationTable(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_RELATION_TABLE, args), mapping);
        descriptorException.setErrorCode(NO_RELATION_TABLE);
        return descriptorException;
    }

    public static DescriptorException normalDescriptorsDoNotSupportNonRelationalExtensions(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NORMAL_DESCRIPTORS_DO_NOT_SUPPORT_NON_RELATIONAL_EXTENSIONS, args), descriptor);
        descriptorException.setErrorCode(NORMAL_DESCRIPTORS_DO_NOT_SUPPORT_NON_RELATIONAL_EXTENSIONS);
        return descriptorException;
    }

    public static DescriptorException noSourceRelationKeysSpecified(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SOURCE_RELATION_KEYS_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(NO_SOURCE_RELATION_KEYS_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException noSubClassMatch(Class theClass, DatabaseMapping mapping) {
        Object[] args = { theClass };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUB_CLASS_MATCH, args), mapping);

        descriptorException.setErrorCode(NO_SUB_CLASS_MATCH);
        return descriptorException;
    }

    public static DescriptorException noSuchFieldWhileInitializingAttributesInInstanceVariableAccessor(String attributeName, String javaClassName, Throwable exception) {
        Object[] args = { attributeName, javaClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_FIELD_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(NO_SUCH_FIELD_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodOnFindObsoleteMethod(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_ON_FIND_OBSOLETE_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_ON_FIND_OBSOLETE_METHOD);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodOnInitializingAttributeMethod(String attributeMethodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { attributeMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_ON_INITIALIZING_ATTRIBUTE_METHOD, args));
        descriptorException.setErrorCode(NO_SUCH_METHOD_ON_INITIALIZING_ATTRIBUTE_METHOD);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileConstructorInstantiation(ClassDescriptor descriptor, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileConstructorInstantiationOfFactory(ClassDescriptor descriptor, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileConvertingToMethod(String methodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_CONVERTING_TO_METHOD, args));
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_CONVERTING_TO_METHOD);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileInitializingAttributesInMethodAccessor(String setMethodName, String getMethodName, String javaClassName) {
        Object[] args = { setMethodName, getMethodName, javaClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR, args));
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileInitializingClassExtractionMethod(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileInitializingCopyPolicy(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_INITIALIZING_COPY_POLICY, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_INITIALIZING_COPY_POLICY);
        return descriptorException;
    }

    public static DescriptorException noSuchMethodWhileInitializingInstantiationPolicy(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_SUCH_METHOD_WHILE_INITIALIZING_INSTANTIATION_POLICY, args), descriptor, exception);
        descriptorException.setErrorCode(NO_SUCH_METHOD_WHILE_INITIALIZING_INSTANTIATION_POLICY);
        return descriptorException;
    }

    public static DescriptorException noTargetForeignKeysSpecified(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_TARGET_FOREIGN_KEYS_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(NO_TARGET_FOREIGN_KEYS_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException noTargetRelationKeysSpecified(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_TARGET_RELATION_KEYS_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(NO_TARGET_RELATION_KEYS_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException notDeserializable(DatabaseMapping mapping, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NOT_DESERIALIZABLE, args));
        descriptorException.setErrorCode(NOT_DESERIALIZABLE);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException notSerializable(DatabaseMapping mapping, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NOT_SERIALIZABLE, args));
        descriptorException.setErrorCode(NOT_SERIALIZABLE);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);

        return descriptorException;
    }

    public static DescriptorException nullForNonNullAggregate(Object object, DatabaseMapping mapping) {
        Object[] args = { object };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_FOR_NON_NULL_AGGREGATE, args), mapping);
        exception.setErrorCode(NULL_FOR_NON_NULL_AGGREGATE);
        return exception;
    }

    public static DescriptorException nullPointerWhileConstructorInstantiation(ClassDescriptor descriptor, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileConstructorInstantiationOfFactory(ClassDescriptor descriptor, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileGettingValueThruInstanceVariableAccessor(String attributeName, String objectName, Throwable exception) {
        Object[] args = { attributeName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_GETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileGettingValueThruMethodAccessor(String methodName, String objectName, Throwable exception) {
        Object[] args = { methodName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileMethodInstantiation(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_METHOD_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_METHOD_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileMethodInstantiationOfFactory(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_METHOD_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_METHOD_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileSettingValueThruInstanceVariableAccessor(String attributeName, Object objectValue, Throwable exception) {
        Object[] args = { attributeName, String.valueOf(objectValue) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_SETTING_VALUE_THRU_INSTANCE_VARIABLE_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException nullPointerWhileSettingValueThruMethodAccessor(String setMethodName, Object value, Throwable exception) {
        Object[] args = { setMethodName, String.valueOf(value) };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NULL_POINTER_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(NULL_POINTER_WHILE_SETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException onlyOneTableCanBeAddedWithThisMethod(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, ONLY_ONE_TABLE_CAN_BE_ADDED_WITH_THIS_METHOD, args), descriptor);
        descriptorException.setErrorCode(ONLY_ONE_TABLE_CAN_BE_ADDED_WITH_THIS_METHOD);
        return descriptorException;
    }

    public static DescriptorException parameterAndMappingWithIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PARAMETER_AND_MAPPING_WITH_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(PARAMETER_AND_MAPPING_WITH_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException parameterAndMappingWithoutIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PARAMETER_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(PARAMETER_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException parameterAndMappingWithTransparentIndirectionMismatch(DatabaseMapping mapping, String validTypeName) {
        Object[] args = { mapping.getAttributeName(), validTypeName };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PARAMETER_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(PARAMETER_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException parentClassIsSelf(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PARENT_CLASS_IS_SELF, args), descriptor);
        exception.setErrorCode(PARENT_CLASS_IS_SELF);
        return exception;
    }

    public static DescriptorException parentDescriptorNotSpecified(String parentClassName, ClassDescriptor descriptor) {
        Object[] args = { parentClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PARENT_DESCRIPTOR_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(PARENT_DESCRIPTOR_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException primaryKeyFieldsNotSepcified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PRIMARY_KEY_FIELDS_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(PRIMARY_KEY_FIELDS_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException proxyIndirectionNotAvailable(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, PROXY_INDIRECTION_NOT_AVAILABLE, args), mapping);
        descriptorException.setErrorCode(PROXY_INDIRECTION_NOT_AVAILABLE);
        return descriptorException;
    }

    public static DescriptorException referenceClassNotSpecified(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, REFERENCE_CLASS_NOT_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(REFERENCE_CLASS_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException referenceDescriptorIsNotAggregate(String className, DatabaseMapping mapping) {
        Object[] args = { className };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATE, args), mapping);
        descriptorException.setErrorCode(REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATE);
        return descriptorException;
    }

    public static DescriptorException referenceDescriptorCannotBeAggregate(DatabaseMapping mapping) {
        DescriptorException descriptorException = new DescriptorException("Reference descriptor cannot be aggregate.", mapping);
        descriptorException.setErrorCode(REFERENCE_DESCRIPTOR_CANNOT_BE_AGGREGATE);
        return descriptorException;
    }

    public static DescriptorException referenceDescriptorIsNotAggregateCollection(String className, DatabaseMapping mapping) {
        Object[] args = { className };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATECOLLECTION, args), mapping);
        descriptorException.setErrorCode(REFERENCE_DESCRIPTOR_IS_NOT_AGGREGATECOLLECTION);
        return descriptorException;
    }

    public static DescriptorException referenceKeyFieldNotProperlySpecified(DatabaseField field, ForeignReferenceMapping mapping) {
        Object[] args = { field };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, REFERENCE_KEY_FIELD_NOT_PROPERLY_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(REFERENCE_KEY_FIELD_NOT_PROPERLY_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException referenceTableNotSpecified(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, REFERENCE_TABLE_NOT_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(REFERENCE_TABLE_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException relationKeyFieldNotProperlySpecified(DatabaseField field, ForeignReferenceMapping mapping) {
        Object[] args = { field };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RELATION_KEY_FIELD_NOT_PROPERLY_SPECIFIED, args), mapping);
        descriptorException.setErrorCode(RELATION_KEY_FIELD_NOT_PROPERLY_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException returnAndMappingWithIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURN_AND_MAPPING_WITH_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(RETURN_AND_MAPPING_WITH_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException returnAndMappingWithoutIndirectionMismatch(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURN_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(RETURN_AND_MAPPING_WITHOUT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException returnAndMappingWithTransparentIndirectionMismatch(DatabaseMapping mapping, String validTypeName) {
        Object[] args = { mapping.getAttributeName(), validTypeName };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURN_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH, args), mapping);
        exception.setErrorCode(RETURN_AND_MAPPING_WITH_TRANSPARENT_INDIRECTION_MISMATCH);
        return exception;
    }

    public static DescriptorException returnTypeInGetAttributeAccessor(String attributeMethodName, DatabaseMapping mapping) {
        Object[] args = { attributeMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURN_TYPE_IN_GET_ATTRIBUTE_ACCESSOR, args));
        descriptorException.setErrorCode(RETURN_TYPE_IN_GET_ATTRIBUTE_ACCESSOR);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException securityOnFindMethod(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_ON_FIND_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(SECURITY_ON_FIND_METHOD);
        return descriptorException;
    }

    public static DescriptorException securityOnFindObsoleteMethod(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_ON_FIND_OBSOLETE_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(SECURITY_ON_FIND_OBSOLETE_METHOD);
        return descriptorException;
    }

    public static DescriptorException securityOnInitializingAttributeMethod(String attributeMethodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { attributeMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_ON_INITIALIZING_ATTRIBUTE_METHOD, args));
        descriptorException.setErrorCode(SECURITY_ON_INITIALIZING_ATTRIBUTE_METHOD);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException securityWhileConvertingToMethod(String methodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_CONVERTING_TO_METHOD, args));
        descriptorException.setErrorCode(SECURITY_WHILE_CONVERTING_TO_METHOD);
        descriptorException.setMapping(mapping);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException securityWhileInitializingAttributesInInstanceVariableAccessor(String attributeName, String javaClassName, Throwable exception) {
        Object[] args = { attributeName, javaClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR, args));
        descriptorException.setErrorCode(SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException securityWhileInitializingAttributesInMethodAccessor(String setMethodName, String getMethodName, String javaClassName) {
        Object[] args = { setMethodName, getMethodName, javaClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR, args));
        descriptorException.setErrorCode(SECURITY_WHILE_INITIALIZING_ATTRIBUTES_IN_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException securityWhileInitializingClassExtractionMethod(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(SECURITY_WHILE_INITIALIZING_CLASS_EXTRACTION_METHOD);
        return descriptorException;
    }

    public static DescriptorException classExtractionMethodMustBeStatic(String methodName, ClassDescriptor descriptor) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, CLASS_EXTRACTION_METHOD_MUST_BE_STATIC, args), descriptor);
        descriptorException.setErrorCode(CLASS_EXTRACTION_METHOD_MUST_BE_STATIC);
        return descriptorException;
    }

    public static DescriptorException securityWhileInitializingCopyPolicy(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_INITIALIZING_COPY_POLICY, args), descriptor, exception);
        descriptorException.setErrorCode(SECURITY_WHILE_INITIALIZING_COPY_POLICY);
        return descriptorException;
    }

    public static DescriptorException securityWhileInitializingInstantiationPolicy(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SECURITY_WHILE_INITIALIZING_INSTANTIATION_POLICY, args), descriptor, exception);
        descriptorException.setErrorCode(SECURITY_WHILE_INITIALIZING_INSTANTIATION_POLICY);
        return descriptorException;
    }

    public static DescriptorException sequenceNumberPropertyNotSpecified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SEQUENCE_NUMBER_PROPERTY_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(SEQUENCE_NUMBER_PROPERTY_NOT_SPECIFIED);
        return descriptorException;
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public static DescriptorException setExistenceCheckingNotUnderstood(String token, ClassDescriptor descriptor) {
        Object[] args = { token };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SET_EXISTENCE_CHECKING_NOT_UNDERSTOOD, args), descriptor);
        descriptorException.setErrorCode(SET_EXISTENCE_CHECKING_NOT_UNDERSTOOD);
        return descriptorException;
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void setMapping(DatabaseMapping mapping) {
        if (mapping != null) {
            this.mapping = mapping;
            setDescriptor(mapping.getDescriptor());
        }
    }

    public static DescriptorException setMethodParameterTypeNotValid(CollectionMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SET_METHOD_PARAMETER_TYPE_NOT_VALID, args), mapping);
        exception.setErrorCode(SET_METHOD_PARAMETER_TYPE_NOT_VALID);
        return exception;
    }

    public static DescriptorException sizeMismatchOfForeignKeys(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, SIZE_MISMATCH_OF_FOREIGN_KEYS, args), mapping);
        descriptorException.setErrorCode(SIZE_MISMATCH_OF_FOREIGN_KEYS);
        return descriptorException;
    }

    public static DescriptorException structureNameNotSetInMapping(DatabaseMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, STRUCTURE_NAME_NOT_SET_IN_MAPPING, args), mapping);
        descriptorException.setErrorCode(STRUCTURE_NAME_NOT_SET_IN_MAPPING);
        return descriptorException;
    }

    public static DescriptorException tableIsNotPresentInDatabase(ClassDescriptor descriptor) {
        Object[] args = { descriptor.getTableName() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TABLE_IS_NOT_PRESENT_IN_DATABASE, args), descriptor);
        descriptorException.setErrorCode(TABLE_IS_NOT_PRESENT_IN_DATABASE);
        return descriptorException;
    }

    public static DescriptorException tableNotPresent(String tableName, ClassDescriptor descriptor) {
        Object[] args = { tableName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TABLE_NOT_PRESENT, args), descriptor);
        descriptorException.setErrorCode(TABLE_NOT_PRESENT);
        return descriptorException;
    }

    public static DescriptorException tableNotSpecified(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TABLE_NOT_SPECIFIED, args), descriptor);
        descriptorException.setErrorCode(TABLE_NOT_SPECIFIED);
        return descriptorException;
    }

    public static DescriptorException targetForeignKeysSizeMismatch(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_FOREIGN_KEYS_SIZE_MISMATCH, args), mapping);
        descriptorException.setErrorCode(TARGET_FOREIGN_KEYS_SIZE_MISMATCH);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileCloning(Object domainObject, String methodName, ClassDescriptor descriptor, Throwable exception) {
        Object[] args = { domainObject, methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_CLONING, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_CLONING);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileConstructorInstantiation(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileConstructorInstantiationOfFactory(ClassDescriptor descriptor, Exception exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_CONSTRUCTOR_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileEventExecution(String eventMethodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { eventMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_EVENT_EXECUTION, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_EVENT_EXECUTION);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileGettingValueThruMethodAccessor(String methodName, String objectName, Throwable exception) {
        Object[] args = { methodName, objectName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_GETTING_VALUE_THRU_METHOD_ACCESSOR);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileInstantiatingMethodBasedProxy(Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_INSTANTIATING_METHOD_BASED_PROXY, args));
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_INSTANTIATING_METHOD_BASED_PROXY);
        descriptorException.setInternalException(exception);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileInvokingAttributeMethod(DatabaseMapping mapping, Throwable exception) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_INVOKING_ATTRIBUTE_METHOD, args));
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_INVOKING_ATTRIBUTE_METHOD);
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileInvokingFieldToMethod(String methodName, DatabaseMapping mapping, Throwable exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_INVOKING_FIELD_TO_METHOD, args));
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_INVOKING_FIELD_TO_METHOD);
        descriptorException.setInternalException(exception);
        descriptorException.setMapping(mapping);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileInvokingRowExtractionMethod(AbstractRecord row, Method method, ClassDescriptor descriptor, Throwable exception) {
        Object[] args = { row, method };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_INVOKING_ROW_EXTRACTION_METHOD, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_INVOKING_ROW_EXTRACTION_METHOD);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileMethodInstantiation(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileMethodInstantiationOfFactory(String methodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { methodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION_OF_FACTORY, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_METHOD_INSTANTIATION_OF_FACTORY);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileObsoleteEventExecute(String eventMethodName, ClassDescriptor descriptor, Exception exception) {
        Object[] args = { eventMethodName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_OBSOLETE_EVENT_EXECUTION, args), descriptor, exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_OBSOLETE_EVENT_EXECUTION);
        return descriptorException;
    }

    public static DescriptorException targetInvocationWhileSettingValueThruMethodAccessor(String setMethodName, Object value, Throwable exception) {
        Object[] args = { setMethodName, String.valueOf(value), CR };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, TARGET_INVOCATION_WHILE_SETTING_VALUE_THRU_METHOD_ACESSOR, args));
        descriptorException.setInternalException(exception);
        descriptorException.setErrorCode(TARGET_INVOCATION_WHILE_SETTING_VALUE_THRU_METHOD_ACESSOR);
        return descriptorException;
    }

    // Bug 2618982
    public static DescriptorException unsupportedTypeForBidirectionalRelationshipMaintenance(DatabaseMapping mapping, oracle.toplink.essentials.internal.queryframework.ContainerPolicy containerPolicy) {
        Object[] args = { mapping.getAttributeName(), containerPolicy };
        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, UNSUPPORTED_TYPE_FOR_BIDIRECTIONAL_RELATIONSHIP_MAINTENANCE, args), mapping);
        exception.setErrorCode(UNSUPPORTED_TYPE_FOR_BIDIRECTIONAL_RELATIONSHIP_MAINTENANCE);
        return exception;
    }

    public static DescriptorException valueHolderInstantiationMismatch(Object attributeValue, DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName(), attributeValue };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, VALUE_HOLDER_INSTANTIATION_MISMATCH, args), mapping);
        exception.setErrorCode(VALUE_HOLDER_INSTANTIATION_MISMATCH);
        return exception;
    }

    public static DescriptorException valueNotFoundInClassIndicatorMapping(ClassDescriptor parentDescriptor, ClassDescriptor descriptor) {
        Object[] args = { parentDescriptor };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, VALUE_NOT_FOUND_IN_CLASS_INDICATOR_MAPPING, args), descriptor);
        descriptorException.setErrorCode(VALUE_NOT_FOUND_IN_CLASS_INDICATOR_MAPPING);
        return descriptorException;
    }

    public static DescriptorException variableOneToOneMappingIsNotDefinedProperly(DatabaseMapping mapping, ClassDescriptor descriptor, String targetKeyName) {
        Object[] args = { targetKeyName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, VARIABLE_ONE_TO_ONE_MAPPING_IS_NOT_DEFINED, args), mapping);
        descriptorException.setErrorCode(VARIABLE_ONE_TO_ONE_MAPPING_IS_NOT_DEFINED);
        return descriptorException;
    }

    public static DescriptorException writeLockFieldInChildDescriptor(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, WRITE_LOCK_FIELD_IN_CHILD_DESCRIPTOR, args), descriptor);
        descriptorException.setErrorCode(WRITE_LOCK_FIELD_IN_CHILD_DESCRIPTOR);
        return descriptorException;
    }

    public static DescriptorException mappingForAttributeIsMissing(String attributeName, ClassDescriptor descriptor) {
        Object[] args = { attributeName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_MAPPING_FOR_ATTRIBUTENAME, args), descriptor);
        descriptorException.setErrorCode(NO_MAPPING_FOR_ATTRIBUTENAME);
        return descriptorException;
    }

    public static DescriptorException attributeMappingIsMissingForEntityBean(String attributeName, String beanName) {
        Object[] args = { attributeName, beanName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_MAPPING_FOR_ATTRIBUTENAME_IN_ENTITY_BEAN, args));
        descriptorException.setErrorCode(NO_MAPPING_FOR_ATTRIBUTENAME_IN_ENTITY_BEAN);
        return descriptorException;
    }

    public static DescriptorException returningPolicyFieldTypeConflict(String fieldName, String type1Name, String type2Name, ClassDescriptor descriptor) {
        Object[] args = { fieldName, type1Name, type2Name };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_FIELD_TYPE_CONFLICT, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_FIELD_TYPE_CONFLICT);
        return descriptorException;
    }

    public static DescriptorException returningPolicyFieldInsertConflict(String fieldName, ClassDescriptor descriptor) {
        Object[] args = { fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_FIELD_INSERT_CONFLICT, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_FIELD_INSERT_CONFLICT);
        return descriptorException;
    }

    public static DescriptorException returningPolicyAndDescriptorFieldTypeConflict(String fieldName, String returningPolicyFieldTypeName, String descriptorFieldTypeName, ClassDescriptor descriptor) {
        Object[] args = { fieldName, returningPolicyFieldTypeName, descriptorFieldTypeName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_AND_DESCRIPTOR_FIELD_TYPE_CONFLICT, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_AND_DESCRIPTOR_FIELD_TYPE_CONFLICT);
        return descriptorException;
    }

    public static DescriptorException returningPolicyUnmappedFieldTypeNotSet(String fieldName, ClassDescriptor descriptor) {
        Object[] args = { fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_UNMAPPED_FIELD_TYPE_NOT_SET, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_UNMAPPED_FIELD_TYPE_NOT_SET);
        return descriptorException;
    }

    public static DescriptorException returningPolicyMappedFieldTypeNotSet(String fieldName, ClassDescriptor descriptor) {
        Object[] args = { fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_MAPPED_FIELD_TYPE_NOT_SET, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_MAPPED_FIELD_TYPE_NOT_SET);
        return descriptorException;
    }

    public static DescriptorException returningPolicyMappingNotSupported(String fieldName, String mappingClassName, DatabaseMapping mapping) {
        Object[] args = { fieldName, mappingClassName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_MAPPING_NOT_SUPPORTED, args), mapping);
        descriptorException.setErrorCode(RETURNING_POLICY_MAPPING_NOT_SUPPORTED);
        return descriptorException;
    }

    public static DescriptorException returningPolicyFieldNotSupported(String fieldName, ClassDescriptor descriptor) {
        Object[] args = { fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, RETURNING_POLICY_FIELD_NOT_SUPPORTED, args), descriptor);
        descriptorException.setErrorCode(RETURNING_POLICY_FIELD_NOT_SUPPORTED);
        return descriptorException;
    }

    public static DescriptorException customQueryAndReturningPolicyFieldConflict(String queryTypeName, String fieldName, ClassDescriptor descriptor) {
        Object[] args = { queryTypeName, fieldName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, CUSTOM_QUERY_AND_RETURNING_POLICY_CONFLICT, args), descriptor);
        descriptorException.setErrorCode(CUSTOM_QUERY_AND_RETURNING_POLICY_CONFLICT);
        return descriptorException;
    }

    public static DescriptorException noCustomQueryForReturningPolicy(String queryTypeName, String platformTypeName, ClassDescriptor descriptor) {
        Object[] args = { queryTypeName, platformTypeName };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NO_CUSTOM_QUERY_FOR_RETURNING_POLICY, args), descriptor);
        descriptorException.setErrorCode(NO_CUSTOM_QUERY_FOR_RETURNING_POLICY);
        return descriptorException;
    }

    public static DescriptorException updateAllFieldsNotSet(ClassDescriptor descriptor) {
        Object[] args = {  };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, UPDATE_ALL_FIELDS_NOT_SET, args), descriptor);
        descriptorException.setErrorCode(UPDATE_ALL_FIELDS_NOT_SET);
        return descriptorException;
    }

    public static DescriptorException invalidMappingType(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INVALID_MAPPING_TYPE, args), mapping);
        descriptorException.setErrorCode(INVALID_MAPPING_TYPE);
        return descriptorException;
    }

    public static DescriptorException needToImplementChangeTracker(ClassDescriptor descriptor) {
        Object[] args = { descriptor.getJavaClass() };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NEED_TO_IMPLEMENT_CHANGETRACKER, args), descriptor);
        descriptorException.setErrorCode(NEED_TO_IMPLEMENT_CHANGETRACKER);
        return descriptorException;
    }

    public static DescriptorException needToImplementFetchGroupTracker(Class aClass, ClassDescriptor descriptor) {
        Object[] args = { aClass };

        DescriptorException descriptorException = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, NEED_TO_IMPLEMENT_FETCHGROUPTRACKER, args), descriptor);
        descriptorException.setErrorCode(NEED_TO_IMPLEMENT_FETCHGROUPTRACKER);
        return descriptorException;
    }
    
    public static DescriptorException errorUsingPrimaryKey(Object primaryKey, ClassDescriptor descriptor, Exception underlying) {
        Object[] args = { String.valueOf(INTERNAL_ERROR_ACCESSING_PKFIELD), primaryKey };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INTERNAL_ERROR_ACCESSING_PKFIELD, args), descriptor, underlying);
        JavaPlatform.setExceptionCause(exception, underlying);
        return exception;
    }
    
    public static DescriptorException errorAccessingSetMethodOfEntity(Class aClass, String methodName, ClassDescriptor descriptor, Exception underlying) {
        Object[] args = { aClass, methodName };

        DescriptorException exception = new DescriptorException(ExceptionMessageGenerator.buildMessage(DescriptorException.class, INTERNAL_ERROR_SET_METHOD, args), descriptor, underlying);
        JavaPlatform.setExceptionCause(exception, underlying);
        return exception;
    }
    
    
}
