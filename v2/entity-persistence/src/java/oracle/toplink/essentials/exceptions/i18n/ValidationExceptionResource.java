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
package oracle.toplink.essentials.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL:
 * English ResourceBundle for ValidationException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author: Xi Chen
 */
public class ValidationExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "7001", "You must login to the ServerSession before acquiring ClientSessions." },
                                           { "7002", "The pool named [{0}] does not exist." },
                                           { "7003", "Max size must be greater than min size." },
                                           { "7004", "Pools must be configured before login." },
                                           { "7008", "The Java type [{0}] is not a valid database type." },
                                           { "7009", "Missing descriptor for [{0}].  Verify that the descriptor has been properly registered with the Session." },
                                           { "7010", "Start index is out of range." },
                                           { "7011", "Stop index is out of range." },
                                           { "7012", "Fatal error occurred." },
                                           { "7013", "You are using the deprecated SessionManager API and no TOPLink.properties file could be found in your classpath.  No sessions could be read in from file." },
                                           { "7017", "Child descriptors do not have an identity map, they share their parent''s" },
                                           { "7018", "File error." },
                                           { "7023", "Incorrect login instance provided.  A DatabaseLogin must be provided." },
                                           { "7024", "Invalid merge policy." },
                                           { "7025", "The only valid keys for DatabaseRows are Strings and DatabaseFields." },
                                           { "7027", "The sequence named [{0}] is setup incorrectly.  Its increment does not match its pre-allocation size." },
                                           { "7028", "writeObject() is not allowed within a UnitOfWork." },
                                           { "7030", "You cannot set read pool size after login." },
                                           { "7031", "You cannot add descriptors to a SessionBroker." },
                                           { "7032", "There is no session registered for the class [{0}]." },
                                           { "7033", "There is no session registered with the name [{0}]." },
                                           { "7038", "Error while logging message to Session log." },
                                           { "7039", "Cannot remove from the set of read-only classes in a nested UnitOfWork. {0}A nested UnitOfWork''s set of read-only classes must be equal to or a superset of its parent''s set of read-only classes." },
                                           { "7040", "Cannot change the set of read-only classes in a UnitOfWork after that UnitOfWork has been used. {0}Changes to the read-only set must be made when acquiring the UnitOfWork or immediately after." },
                                           { "7042", "Database platform class [{0}] not found." },
                                           { "7043", "[{0}] does not have any tables to create on the database." },
                                           { "7044", "The container class specified [{0}] cannot be used as a container because it does not implement Collection or Map." },
                                           { "7047", "The container specified [{0}] does not require keys.  You tried to use the method [{1}]." },
                                           { "7048", "Neither the instance method or field named [{0}] exists for the item class [{1}], and therefore cannot be used to create a key for the Map." },
                                           { "7051", "Missing attribute [{1}] for descriptor [{0}], called from [{2}]" },
                                           { "7052", "An attempt was made to use [{0}] (with the key method [{1}]) as a container for a DirectCollectionMapping [{0}].  The useMapClass() method cannot be used, only the useCollectionClass() API is supported for DirectCollectionMappings." },
                                           { "7053", "release() attempted on a Session that is not a ClientSession.  Only ClientSessions may be released." },
                                           { "7054", "acquire() attempted on a Session that is not a ServerSession.  ClientSessions may only be acquired from ServerSessions." },
                                           { "7055", "Optimistic Locking is not supported with stored procedure generation." },
                                           { "7056", "The wrong object was registered into the UnitOfWork.  The object [{0}] should be the object from the parent cache [{1}]." },
                                           { "7058", "Invalid Connector [{0}] (must be of type DefaultConnector)." },
                                           { "7059", "Invalid data source name [{0}]." },
                                           { "7060", "Cannot acquire data source [{0}]." },
                                           { "7061", "Exception occurred within JTS." },
                                           { "7062", "Field-level locking is not supported outside of a UnitOfWork.  To use field-level locking, a UnitOfWork must be used for ALL writing." },
                                           { "7063", "Exception occurred within EJB container." },
                                           { "7064", "Exception occurred in reflective EJB primary key extraction.  Please ensure your primary key object is defined correctly. {2}Key: [{0}] {2}Bean: [{1}]" },
                                           { "7065", "The remote class for the bean cannot be loaded or found.  Ensure that the correct class loader is set. {2}Bean: [{0}] {2}Remote Class: [{1}]" },
                                           { "7066", "Cannot create or remove beans unless a JTS transaction is present. {1}Bean: [{0}]" },
                                           { "7068", "The project class [{0}] was not found for the project [{1}] using the default class loader." },
                                           { "7069", "An exception occurred looking up or inovking the project amendment method, ''{0}'' on the class ''{1}''." },
                                           { "7070", "A TOPLink.properties resource bundle must be located on the CLASSPATH in a TopLink directory." },
                                           { "7071", "Cannot use input/output parameters without using binding." },
                                           { "7072", "The database platform class [{0}] was not found for the project [{1}] using the default class loader." },
                                           { "7073", "The Oracle object type with type name [{0}] is not defined." },
                                           { "7074", "The Oracle object type name [{0}] is not defined." },
                                           { "7075", "Maximum size is not defined for the Oracle VARRAY type [{0}].  A maximum size must be defined." },
                                           { "7076", "When generating the project class, the project''s descriptors must not be initialized. {1}Descriptor: [{0}]" },
                                           { "7077", "The home interface [{0}], specified during creation of the BMPWrapperPolicy, does not contain a correct findByPrimaryKey() method.  A findByPrimaryKey() method must exist that takes the PrimaryKey class for this bean." },
                                           { "7078", "The \"sessionName\" [{0}], specified on the deployment descriptor for [{1}], does not match any session specified in the TopLink properties file." },
                                           { "7079", "The descriptor for [{0}] was not found in the session [{1}].  Check the project being used for this session." },
                                           { "7080", "A FinderException was thrown when trying to load [{0}], of class [{1}], with primary key [{2}]." },
                                           { "7081", "The aggregate object [{0}] cannot be directly registered in the UnitOfWork.  It must be associated with the source (owner) object." },
                                           { "7082", "The TopLink properties file [{0}] specified multiple project files for the server named [{1}].  Please specify one of [projectClass], [projectFile], or [xmlProjectFile]." },
                                           { "7083", "The TopLink properties file [{0}] did not include any information on the TopLink project to use for the server named [{1}].  Please specify one of [projectClass], [projectFile], or [xmlProjectFile]." },
                                           { "7084", "The file [{0}] is not a valid type for reading.  ProjectReader must be given the deployed XML Project file." },
                                           { "7085", "At least one sub session need to be defined in the TopLink session broker {0}" },
                                           { "7086", "The session type ''{0}'' of the session name ''{1}'' was not defined properly." },
                                           { "7087", "The session type ''{0}'' was not found for the ''{1}'' using default class loader." },
                                           { "7088", "Cannot create an instance of the external transaction controller [{0}], specified in the properties file." },
                                           { "7089", "An exception occurred looking up or invoking the session amendment method [{0}] on the class [{1}] with parameters [{2}]." },
                                           { "7091", "Cannot set listener classes!" },
                                           { "7092", "Cannot add a query whose types conflict with an existing query. Query To Be Added: [{0}] is named: [{1}] with arguments [{2}].The existing conflicting query: [{3}] is named: [{4}] with arguments: [{5}]." },
                                           { "7093", "In the query named [{0}], the class [{2}] for query argument named [{1}] cannot be found. Please include the missing class on your classpath." },
                                           { "7095", "The sessions.xml resource [{0}] was not found on the resource path.  Check that the resource name/path and classloader passed to the SessionManager.getSession are correct.  The sessions.xml should be included in the root of the application's deployed jar, if the sessions.xml is deployed in a sub-directory in the application's jar ensure that the correct resource path using \"/\" not \"\\\" is used." },
                                           { "7096", "Cannot use commit() method to commit UnitOfWork again." },
                                           { "7097", "Operation not supported: [{0}]." },
                                           { "7099", "The deployment project xml resource [{0}] was not found on the resource path.  Check that the resource name/path and classloader passed to the XMLProjectReader are correct.  The project  xml should be included in the root of the application's deployed jar, if the project xml is deployed in a sub-directory in the application's jar ensure that the correct resource path using \"/\" not \"\\\" is used." },
                                           { "7100", "Could not find the session with the name [{0}] in the session.xml file [{1}]" },
                                           { "7101", "No \"meta-inf/toplink-ejb-jar.xml\" could be found in your classpath.  The CMP session could not be read in from file." },
                                           { "7102", "Encountered a null value for a cache key while attempting to remove" + "{2}an object from the identity map: [{0}]" + "{2}containing an object of class: [{1}] (or a class in this hierarchy)" + "{2}The most likely cause of this situation is that the object has already been garbage-" + "{2}collected and therefore does not exist within the identity map." + "{2}Please consider using an alternative identity map to prevent this situation." + "{2}For more details regarding identity maps, please refer to the TopLink documentation." },
                                           { "7103", "A null reference was encountered while attempting to invoke" + "{1}method: [{0}] on an object which uses proxy indirection." + "{1}Please check that this object is not null before invoking its methods." },
                                           { "7104", "Sequencing login should not use External Transaction Controller." },
                                           { "7105", "Error encountered converting encryptiong class: [{0}]" },
                                           { "7106", "Error encountered during string encryption." },
                                           { "7107", "Error encountered during string decryption." },
                                           { "7108", "This operation is not supported for non-relational platforms." },
                                           { "7109", "The login in the project used to create the session is null, it must be a valid login." },
                                           { "7110", "At present HistoricalSession only works with Oracle 9R2 or later databases, as it uses Oracle's Flashback feature." },
                                           { "7111", "You may not acquire a HistoricalSession from a UnitOfWork, another HistoricalSession, a ServerSession, or a ServerSessionBroker.  You may acquire one from a regular session, a ClientSession, or a ClientSessionBroker." },
                                           { "7112", "You have specified that Toplink use the feature : {0}, but this feature is not available in the currently running JDK version :{1}." },
                                           { "7113", "{0} does not support call with returning." },
                                           { "7114", "Isolated Data is not currently supported within a Client Session Broker. Session named {0} contains descriptors representing isolated data." },
                                           { "7115", "A Exclusive Connection cannot be used for ClientSession reads without isolated data.  Please update the ConnectionPolicy, used, to remove ExclusiveConnection configuration or the project to set certain data to be exclusive." },
                                           { "7116", "Invalid arguments are used.  Please refer to public API of the calling method and use valid values for the arguments." },
                                           { "7117", "There is an attempt to use more than one cursor in SQLCall {0}" },
                                           { "7118", "setCustomSQLArgumentType method was called on SQLCall {0}, but this call doesn't use custom SQL" },
                                           { "7119", "Unprepared SQLCall {0} attempted translation" },
                                           { "7120", "Parameter {0} in SQLCall {1} cannot be used as a cursor, because it is has parameter type other than OUT" },
                                           { "7121", "{0} does not support stored functions" },
                                           { "7122", "The exclusive connection associated with the session is unavailable for the query on {0}" },
                                           { "7123", "A successful writeChanges() has been called on this UnitOfWork.  As the commit process has been started but not yet finalized, the only supported operations now are commit, commitAndResume, release, any non-object level query or SQLCall execution.  The operation {0} is not allowed at this time." },
                                           { "7124", "An unsuccessful writeChanges() has been called on this UnitOfWork.  Given the danger that partial changes have been written to the datastore but not rolled back (if inside external transaction), the only supported operations now are release, global transaction rollback, any non-object level query or SQLCall execution.  The operation {0} was attempted." },
                                           { "7125", "Once the UnitOfWork has been committed and/or released, no further operation should be performed on it.  The operation {0} was attempted on it." },
                                           { "7126", "writeChanges cannot be called on a NestedUnitOfWork.  A nested UnitOfWork never writes changes directly to the datastore, only the parent UnitOfWork does." },
                                           { "7127", "You can only writes changes to the datastore once, just as you can only call commit once." },
                                           { "7128", "Session [{0}] is already logged in." },
                                           { "7129", "The method''s arguments cannot have null value." },
                                           { "7130", "Nested unit of work is not supported for attribute change tracking." },
                                           { "7131", "{0} is the wrong type.  The collection change event type has to be add or remove." },
                                           { "7132", "{0} is the wrong event class.  Only PropertyChangeEvent and CollectionChangeEvent are supported." },
                                           { "7133", "Old commit is not supported for attribute change tracking." },
                                           { "7134", "Server platform {0} is read only after login." },
                                           { "7135", "You cannot commit and resume a unit of work containing any modify all queries" },
                                           { "7136", "Nested unit of work is not supported for a modify all query" },
                                           { "7137", "The object is partially fetched (using fecth group), the unfetched attribute ({0}) is not editable." },
                                           { "7139", "Modify all queries cannot be issued within a unit of work containing other write operations." },
                                           { "7140", "Sequence type {0} doesn''t have method {1}." },
                                           { "7141", "{0} sequence is of type DefaultSequence, which cannot be used in setDefaultSequence method." },
                                           { "7142", "{0} sequence can''t be set as default, because a sequence with that name has been already added" },
                                           { "7143", "{0} sequence can''t be added, because a sequence with that name has been already set as default." },
                                           { "7144", "{0}: platform {1} doesn''t support {2}." },
                                           { "7145", "{2} attempts to connect to sequence {0}, but it is already connected to {1}. Likely the two sessions share the DatasourcePlatform object" },
                                           { "7146", "QuerySequence {1} doesn''t have select query." },
                                           { "7147", "Platform {0} cannot create platform default sequence - it doesn''t override createPlatformDefaultSequence method" },
                                           { "7148", "commitAndResume() cannot be used with a JTA/synchronized unit of work." },
                                           { "7149", "The composite primary key attribute [{2}] of type [{4}] on entity class [{0}] should be of the same type as defined on its primary key class [{1}]. That is, it should be of type [{3}]." },
                                           { "7150", "Invalid composite primary key specification. The names of the primary key fields or properties in the primary key class [{1}] and those of the entity bean class [{0}] must correspond and their types must be the same. Also, ensure that you have specified id elements for the corresponding attributes in XML and/or an @Id on the corresponding fields or properties of the entity class." },
                                           { "7151", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for an enumerated mapping. The attribute must be defined as a Java enum."},
                                           { "7152", "Table per class inheritance is not supported. Entity class [{0}]." },
                                           { "7153", "Mapping annotations cannot be applied to fields or properties that are transient or have a @Transient specified. [{0}] is in violation of this restriction." },
                                           { "7154", "The attribute [{3}] in entity class [{2}] has a mappedBy value of [{1}] which does not exist in its owning entity class [{0}]. If the owning entity class is a @MappedSuperclass, this is invalid, and your attribute should reference the correct subclass." },
                                           { "7155", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a serialized mapping. The attribute type must implement the Serializable interface."},
                                           { "7156", "Unable to find the class named [{0}]. Ensure the class name/path is correct and available to the classloader."},
                                           { "7157", "Entity class [{0}] must use a @JoinColumn instead of @Column to map its relationship attribute [{1}]."},
                                           { "7158", "Error encountered when building the @NamedQuery [{1}] from entity class [{0}]."},
                                           { "7159", "The map key [{0}] on the entity class [{1}] could not be found for the mapping [{2}]."},
                                           { "7160", "@OneToMany for attribute name [{1}] in entity class [{0}] should not have @JoinColumn(s) specified. In the case where the @OneToMany is not mapped by another entity (that is, it is the owning side and is uni-directional), it should specify (optional through defaulting) a @JoinTable."},
                                           { "7161", "Entity class [{0}] has no primary key specified. It should define either an @Id, @EmbeddedId or an @IdClass."},
                                           { "7162", "Entity class [{0}] has multiple @EmbeddedId's specified (on attributes [{1}] and [{2}]). Only one @EmbeddedId can be specified per entity."},
                                           { "7163", "Entity class [{0}] has both an @EmbdeddedId (on attribute [{1}]) and an @Id (on attribute [{2}]. Both id types cannot be specified on the same entity."},
                                           { "7164", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a lob mapping. For a lob of type BLOB, the attribute must be defined as a java.sql.Blob, byte[], Byte[] or a Serializable type. For a lob of type CLOB, the attribute must be defined as a java.sql.Clob, char[], Character[] or String type."},
                                           { "7165", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a temporal mapping. The attribute must be defined as java.util.Date or java.util.Calendar."},
                                           { "7166", "A table generator that uses the reserved name [{0}] for its 'name' has been found in [{1}]. It cannot use this name since it is reserved for defaulting a sequence generator's 'sequence name'."},
                                           { "7167", "A sequence generator that uses the reserved name [{0}] for its 'sequence name' has been found in [{1}]. It cannot use this name since it is reserved for defaulting a table generator's 'name'."},
                                           { "7168", "The attribute [{0}] of type [{1}] on the entity class [{2}] is not valid for a version property. The following types are supported: int, Integer, short, Short, long, Long, Timestamp."},
                                           { "7169", "Class [{0}] has two @GeneratedValues: for fields [{1}] and [{2}]. Only one is allowed."} ,
                                           { "7172", "Error encountered when instantiating the class [{0}]."},
                                           { "7173", "A property change event has been fired on a property with name [{1}] in [{0}].  However this property does not exist."},
                                           { "7174", "The getter method [{1}] on entity class [{0}] does not have a corresponding setter method defined."},
                                           { "7175", "The mapping [{0}] does not support cascading version optimistic locking."},
                                           { "7176", "The mapping [{0}] does not support cascading version optimistic locking because it has a custom query."},
                                           { "7177", "The aggregate descriptor [{0}] has privately-owned mappings. Aggregate descriptors do not support cascading version optimistic locking."},
                                           { "7178", "OracleOCIProxyConnector requires OracleOCIConnectionPool datasource."},
                                           { "7179", "OracleJDBC10_1_0_2ProxyConnector requires datasource producing OracleConnections."},
                                           { "7180", "OracleJDBC10_1_0_2ProxyConnector requires Oracle JDBC version 10.1.0.2 or higher so that OracleConnection declares openProxySession method."},
                                           { "7181", "OracleJDBC10_1_0_2ProxyConnector requires ''proxytype'' property to be an int converted to String, for instance Integer.toString(OracleConnection.PROXYTYPE_USER_NAME)"},
                                           { "7182", "EC - Could not find driver class [{0}]"},
                                           { "7183", "Error closing persistence.xml file."},
                                           { "7184", "[{0}] system property not specified. It must be set to a class that defines a ''getContainerConfig()'' method."},
                                           { "7185", "Cannot find class [{0}] specified in [{1}]"},
                                           { "7186", "Cannot invoke method [{0}] on class [{1}] specified in [{2}]"},
                                           { "7187", "[{0}] should define a public static method [{1}] that has no parameters and returns Colleciton"},
                                           { "7188", "Non-null class list is required."},
                                           { "7189", "Cannot create temp classloader from current loader: [{0}]"},
                                           { "7190", "[{0}] failed"},
                                           { "7191", "The entity class [{0}] was not found using class loader [{1}]." },
                                           { "7192", "ClassFileTransformer [{0}] throws an exception when performing transform() on class [{1}]." },
                                           { "7193", "Jar files in persistence XML are not supported in this version of TopLink." },
                                           { "7194", "Could not bind: [{0}] to: [{1}]." },
                                           { "7195", "Exception configuring EntityManagerFactory." },
                                           { "7196", "[{0}] of type [{1}] cannot be casted to [{2}]."},
                                           { "7197", "This operation is not supported for this class: [{0}], [{1}]."},
                                           { "7198", "Class: [{0}] was not found while converting from class names to classes."},
                                           { "7199", "A primary table was not defined for entity {0} in the entity-mappings file: {1}.  A primary table is required to process an entity relationship."},
                                           { "7200", "The attribute [{1}] was not found on the embeddable class [{0}]. It is referenced in the @AttributeOverride for the @Embedded attribute [{3}] on class [{2}]."},
                                           { "7201", "An exception occurred parsing the entity-mappings file: {0}."},
                                           { "7202", "Attribute-override name {0} is invalid - make sure that an attribtue with the same name exists in the embeddable {1}."},
                                           { "7203", "The mapping element [{1}] for the class [{2}] has an unsupported collection type [{0}]. Only Set, List, Map and Collection are supported."},
                                           { "7205", "Entity class [{0}] has multiple embedded-id elements specified (on attributes [{1}] and [{2}]). Only one embedded-id can be specified per entity."},
                                           { "7206", "Entity class [{0}] has both an embedded-id element (on attribute [{1}]) and an id element (on attribute [{2}]. Both id types cannot be specified on the same entity."},
                                           { "7207", "Attribute [{1}] in entity class [{0}] has an invalid type for a lob of type BLOB. The attribute must be defined as a java.sql.Blob, byte[], Byte[] or a Serializable type."},
                                           { "7208", "Attribute [{1}] in entity class [{0}] has an invalid type for a lob of type CLOB. The attribute must be defined as a java.sql.Clob, char[], Character[] or String type."},
                                           { "7209", "one-to-many for attribute name [{1}] in entity class [{0}] should not have join-column(s) specified. In the case where the one-to-many is not mapped by another entity (that is, it is the owning side and is uni-directional), it should specify (optional through defaulting) a join-table."},
                                           { "7210", "Error encountered when building the named-query [{1}]."},
                                           { "7211", "Entity class [{0}] must use join-column instead of column to map its relationship attribute [{1}]."},
                                           { "7212", "The attribute [{0}] from the entity class [{1}] does not specify a temporal type. A temporal type must be specified for persistent fields or properties of type java.util.Date and java.util.Calendar."},
                                           { "7213", "Circular mappedBy references have been specified (Class: [{0}], attribute: [{1}] and Class: [{2}], attribute: [{3}]. This is not valid, only one side can be the owner of the relationship. Therefore, specify a mappedBy value only on the non-owning side of the relationship."},
                                           { "7214", "The target entity of the relationship attribute [{0}] on the class [{1}] cannot be determined.  When not using generics, ensure the target entity is defined on the relationship mapping."},
                                           { "7215", "Cannot determine the type (class) of the field attribute [{0}] in entity class [{1}]. Ensure there is a corresponding field with that name on the entity class."},
                                           { "7216", "Cannot determine the type (class) of the property attribute [{0}] in entity class [{1}]. Ensure there is corresponding get method for that property name on the entity class."},
                                           { "7217", "The order by value [{0}], specified on the element [{2}] from entity [{3}], is invalid. No property or field with that name exists on the target entity [{1}]."},
                                           { "7218", "[{0}] doesn''t override getCreateTempTableSqlPrefix method. Database platform that support temporary tables must override this method."},
                                           { "7219", "[{0}] doesn''t override valueFromRowInternalWithJoin method, but it's isJoiningSupported method returns true. Foreign reference mapping that supports joining must override this method."},
                                           { "7220", "The @JoinColumns on the annotated element [{0}] from the entity class [{1}] is incomplete. When the source entity class uses a composite primary key, a @JoinColumn must be specified for each join column using the @JoinColumns. Both the name and the referenceColumnName elements must be specified in each such @JoinColumn."},
                                           { "7221", "A @JoinColumns was specified on the annotated element [{0}] from the entity class [{1}]. When the source entity class uses a single primary key, only a single (or zero) @JoinColumn should be specified."},
                                           { "7222", "An incomplete @PrimaryKeyJoinColumns was specified on the annotated element [{0}]. When specifying @PrimaryKeyJoinColumns for an entity that has a composite primary key, a @PrimaryKeyJoinColumn must be specified for each primary key join column using the @PrimaryKeyJoinColumns. Both the name and the referenceColumnName elements must be specified in each such @PrimaryKeyJoinColumn."},
                                           { "7223", "A @PrimaryKeyJoinColumns was found on the annotated element [{0}]. When the entity uses a single primary key, only a single (or zero) @PrimaryKeyJoinColumn should be specified."},
                                           { "7224", "The method [{1}] on the listener class [{0}] is an invalid callback method."},
                                           { "7225", "The method [{1}] could not be found on the listener class [{0}]."},
                                           { "7226", "The method [{1}] on the listener class [{0}] has an invalid modifier. Callback methods can not be static or final."},
                                           { "7227", "The listener class [{0}] has multiple lifecycle callback methods for the same lifecycle event ([{1}] and [{2}])."},
                                           { "7228", "The Callback method [{1}] on the listener class [{0}] has an incorrect signature. It should not have any parameters."},
                                           { "7229", "The Callback method [{3}] on the entity listener class [{2}] has an incorrect signature. The method must take 1 parameter which must be assignable from the entity class. Here, the parameter class [{1}] is not assignable from the entity class [{0}]."},
                                           { "7230", "Conflicting values for [{0}] have been encoundered during persistence-unit-metadata element processing.  If the persistence-unit-metadata element occurs in multiple orm xml instance documents within the same persistence unit, please ensure that all [{0}] elements have the same value."},
                                           { "7231", "Cannot persist detached object [{0}]. {3}Class> {1} Primary Key> {2}"}, 
                                           { "7232", "The entity class [{0}] contains multiple @Id declarations, but does not define any <id> elements in the entity mappings instance document.  Please ensure that if there are multiple @Id declarations for a given entity class, the corresponding <entity> definition contains an <id> element for each."},
                                           { "7233", "The join-column element definition for attribute [{0}] from the entity class [{1}] is incomplete.  When the source entity class uses a composite primary key, a join-column element must be specified for each join column.  Both the name and the referenced-column-name attributes must be specified in each such join-column."},
                                           { "7234", "Multiple join-column elements were specified for the attribute [{0}] from the entity class [{1}].  When the source entity class uses a single primary key, only a single (or zero) join-column element should be specified."},
                                           { "7235", "An incomplete primary-key-join-column element was specified on the entity class [{0}].  When specifying primary-key-join-column elements for an entity that has a composite primary key, a primary-key-join-column element must be specified for each primary key.  Both the name and the referenced-column-name attributes must be specified in each such primary-key-join-column element."},
                                           { "7236", "Multiple primary-key-join-column elements were specified on the entity class [{0}]. When the entity uses a single primary key, only a single (or zero) primary-key-join-column element should be specified."},
                                           { "7237", "Entity name must be unique in a persistence unit. Entity name [{0}] is used for the entity classes [{1}] and [{2}]."},
                                           { "7238", "The table generator specified in [{2}] with name == [{0}] conflicts with the sequence generator with the same name specified in [{1}]."},
                                           { "7239", "The sequence generator specified in [{2}] with name == [{0}] conflicts with another sequence generator with the same name in [{1}]."},
                                           { "7240", "The table generator specified in [{2}] with pk column value == [{0}] conflicts with the sequence generator specified in [{1}] with sequence name == [{0}]. They cannot use the same value."},
                                           { "7241", "The table generator specified in [{2}] with name == [{0}] conflicts with another table generator with the same name in [{1}]."},
                                           { "7242", "An attempt was made to traverse a relationship using indirection that had a null Session.  This often occurs when an entity with an uninstantiated LAZY relationship is serialized and that lazy relationship is traversed after serialization.  To avoid this issue, instantiate the LAZY relationship prior to serialization."},
                                           { "7243", "Missing meta data for class [{0}]. Ensure the class is not being excluded from your persistence unit by a <exclude-unlisted-classes>true</exclude-unlisted-classes> setting. If this is the case, you will need to include the class directly by adding a <class>[{0}]</class> entry for your persistence-unit."},
										   { "7244", "An incompatible mapping has been encountered between [{0}] and [{1}]. This usually occurs when the cardinality of a mapping does not correspond with the cardinality of its backpointer."},
                                           { "7245", "The *metadata-less* embeddable class [{0}] is used in entity classes with conflicting access-types. This is not allowed as this may result in inconsistent mappings of the embeddable class in different points of use. This problem can be corrected in two ways: 1. Provide metadata on class [{0}] that allows the access type to be determined. 2. Ensure all users of class [{0}] have the same access type."},
                                           { "7246", "The Entity class [{0}] has an embedded attribute [{1}] of type [{2}] which is NOT an Embeddable class. Probable reason: missing @Embeddable or missing <embeddable> in orm.xml if metadata-complete = true"},
                                           { "7247", "Both fields and methods are annotated in [{0}]"},
                                           { "7248", "For [{0}] access type [{1}] as determined by XML is different from access type [{2}] determined using annotation."},
                                           { "7249", "Entity [{0}] uses [{1}] as embedded id class whose access-type has been determined as [{2}]. But [{1}] does not define any [{2}]. It is likely that you have not provided sufficient metadata in your id class [{1}]."},
                                           { "7250", "[{0}] uses a non-entity [{1}] as target entity in the relationship attribute [{2}]."},
                                           { "7251", "The attribute [{1}] of class [{0}] is mapped to a primary key column in the database. Updates are not allowed."},
                                           { "7252", "There are multiple mapping files called [{1}] in classpath for persistence unit named [ {0} ]."},
                                           { "7253", "There is no mapping file called [{1}] in classpath for persistence unit named [{0}]."}
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
