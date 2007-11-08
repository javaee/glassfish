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
package oracle.toplink.essentials.internal.ejb.cmp3.xml;

/**
 * INTERNAL:
 * 
 * Static values for XML processing.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLConstants {
    // miscellaneous values
    public static final String FIELD = "FIELD";
    public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String ORM_SCHEMA_NAME = "orm_1_0.xsd";
    public static final String PERSISTENCE_SCHEMA_NAME = "persistence_1_0.xsd";
    public static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    public static final String VALIDATING = "http://xml.org/sax/features/validation";
    public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final String ALL = "ALL";
    public static final String ALL_CHILDREN = "child::*";
    public static final String CALLBACK_METHODS = "CALLBACK_METHODS";
    public static final String DEFAULT_TEMPORAL = "TIMESTAMP";
    public static final String ENTITIES_TO_DEFAULT = "entitiesToDefault";
    public static final String ENTITIES_TO_PROCESS = "entitiesToProcess";
    public static final String ORDINAL = "ORDINAL";
    public static final String PROPERTY = "PROPERTY";
    public static final String SET = "set";
    public static final String SINGLE_TABLE = "SINGLE_TABLE";
    public static final String TABLE_PER_CLASS = "TABLE_PER_CLASS";
    public static final String TEXT = "text()";
    
    // attribute values
    public static final String ATT_ACCESS = "@access";
    public static final String ATT_ALLOCATION_SIZE = "@allocation-size";
    public static final String ATT_CATALOG = "@catalog";
    public static final String ATT_CLASS = "@class";
    public static final String ATT_COLUMN = "@column";
    public static final String ATT_COLUMN_DEFINITION = "@column-definition";
    public static final String ATT_DISCRIMINATOR_COLUMN = "@discriminator-column";
    public static final String ATT_DISCRIMINATOR_TYPE = "@discriminator-type";
    public static final String ATT_ENTITY_CLASS = "@entity-class";
    public static final String ATT_FETCH = "@fetch";
    public static final String ATT_GENERATOR = "@generator";
    public static final String ATT_INITIAL_VALUE = "@initial-value";
    public static final String ATT_INSERTABLE = "@insertable";
    public static final String ATT_LENGTH = "@length";
    public static final String ATT_MAPPED_BY = "@mapped-by";
    public static final String ATT_METADATA_COMPLETE = "@metadata-complete";
    public static final String ATT_METHOD_NAME = "@method-name";
    public static final String ATT_NAME = "@name";
    public static final String ATT_NULLABLE = "@nullable";
    public static final String ATT_OPTIONAL = "@optional";
    public static final String ATT_PK_COLUMN_NAME = "@pk-column-name";
    public static final String ATT_PK_COLUMN_VALUE = "@pk-column-value";
    public static final String ATT_PRECISION = "@precision";
    public static final String ATT_REFERENCED_COLUMN_NAME = "@referenced-column-name";
    public static final String ATT_RESULT_CLASS = "@result-class";
    public static final String ATT_RESULT_SET_MAPPING = "@result-set-mapping";
    public static final String ATT_SCALE = "@scale";
    public static final String ATT_SCHEMA = "@schema";
    public static final String ATT_SECONDARY_TABLE = "@secondary-table";
    public static final String ATT_TABLE = "@table";
    public static final String ATT_SEQUENCE_NAME = "@sequence-name";
    public static final String ATT_STRATEGY = "@strategy";
    public static final String ATT_TARGET_ENTITY = "@target-entity";
    public static final String ATT_UNIQUE = "@unique";
    public static final String ATT_UPDATABLE = "@updatable";
    public static final String ATT_VALUE = "@value";
    public static final String ATT_VALUE_COLUMN_NAME = "@value-column-name";
    public static final String ATT_VERSION = "@version";

    // element/complex-type values
    public static final String ACCESS = "access";
    public static final String ATTRIBUTES = "attributes";
    public static final String ASSOCIATION_OVERRIDE = "association-override";
    public static final String ATTRIBUTE_OVERRIDE = "attribute-override";
    public static final String BASIC = "basic";
    public static final String CASCADE = "cascade";
    public static final String CASCADE_PERSIST = "cascade-persist";
    public static final String CATALOG = "catalog";
    public static final String COLUMN = "column";
    public static final String COLUMN_NAME = "column-name";
    public static final String COLUMN_RESULT = "column-result";
    public static final String DEFAULT_ENTITY_LISTENERS = "default-entity-listeners";
    public static final String DESCRIPTION = "description";
    public static final String DISCRIMINATOR_COLUMN = "discriminator-column";
    public static final String DISCRIMINATOR_VALUE = "discriminator-value";
    public static final String EMBEDDED = "embedded";
    public static final String EMBEDDED_ID = "embedded-id";
    public static final String EMBEDDABLE = "embeddable";
    public static final String EMBEDDABLE_ATTRIBUTE = "embeddable-attribute";
    public static final String ENTITY = "entity";
    public static final String ENTITY_LISTENER = "entity-listener";
    public static final String ENTITY_LISTENERS = "entity-listeners";
    public static final String ENTITY_MAPPINGS = "entity-mappings";
    public static final String ENTITY_RESULT = "entity-result";
    public static final String ENUMERATED = "enumerated";
    public static final String EXCLUDE_DEFAULT_LISTENERS = "exclude-default-listeners";
    public static final String EXCLUDE_SUPERCLASS_LISTENERS = "exclude-superclass-listeners";
    public static final String FIELD_RESULT = "field-result";
    public static final String FLUSH_MODE = "flush-mode";
    public static final String GENERATED_VALUE = "generated-value";
    public static final String ID = "id";
    public static final String ID_CLASS = "id-class";
    public static final String INHERITANCE = "inheritance";
    public static final String INVERSE_JOIN_COLUMN = "inverse-join-column";
    public static final String JOIN_COLUMN = "join-column";
    public static final String JOIN_TABLE = "join-table";
    public static final String LOB = "lob";
    public static final String MANY_TO_MANY = "many-to-many";
    public static final String MANY_TO_ONE = "many-to-one";
    public static final String MAPKEY = "map-key";
    public static final String MAPPED_SUPERCLASS = "mapped-superclass";
    public static final String METADATA_COMPLETE = "xml-mapping-metadata-complete";
    public static final String NAMED_NATIVE_QUERY = "named-native-query";
    public static final String NAMED_QUERY = "named-query";
    public static final String ONE_TO_MANY = "one-to-many";
    public static final String ONE_TO_ONE = "one-to-one";
    public static final String ORDER_BY = "order-by";
    public static final String PACKAGE = "package";
    public static final String PK_JOIN_COLUMN = "primary-key-join-column";
    public static final String POST_LOAD = "post-load";
    public static final String POST_PERSIST = "post-persist";
    public static final String POST_REMOVE = "post-remove";
    public static final String POST_UPDATE = "post-update";
    public static final String PRE_PERSIST = "pre-persist";
    public static final String PRE_REMOVE = "pre-remove";
    public static final String PRE_UPDATE = "pre-update";
    public static final String PU_DEFAULTS = "persistence-unit-defaults";
    public static final String PU_METADATA = "persistence-unit-metadata";
    public static final String QUERY = "query";
    public static final String QUERY_HINT = "hint";
    public static final String SCHEMA = "schema";
    public static final String SECONDARY_TABLE = "secondary-table";
    public static final String SEQUENCE_GENERATOR = "sequence-generator";
    public static final String SQL_RESULT_SET_MAPPING = "sql-result-set-mapping";
    public static final String TABLE = "table";
    public static final String TABLE_GENERATOR = "table-generator";
    public static final String TEMPORAL = "temporal";
    public static final String TRANSIENT = "transient";
    public static final String UNIQUE_CONSTRAINTS = "unique-constraint";
    public static final String VERSION = "version";
}
