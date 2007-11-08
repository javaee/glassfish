/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * MappingElementProperties.java
 *
 */


package com.sun.persistence.api.model.mapping;

/** 
 *
 */
public interface MappingElementProperties {
    // <editor-fold desc="//================== property change constants ======================">

    /**
     * Name of {@link MappingElement#getName name} property.
     */
    public static final String PROP_NAME = "name"; // NOI18N

    /**
     * Name of {@link MappingClass#isModified modified} flag for {@link
     * MappingClass class objects}.
     */
    public static final String PROP_MODIFIED = "modified"; // NOI18N

    /**
     * Name of {@link MappingClass#getConsistencyLevel consistencyLevel}
     * property for {@link MappingClass class objects}.
     */
    public static final String PROP_CONSISTENCY = "consistencyLevel"; // NOI18N

    /**
     * Name of {@link MappingClass#setDatabaseRoot root} property for {@link
     * MappingClass class objects}.
     */
    public static final String PROP_DATABASE_ROOT = "schema"; // NOI18N

    /**
     * Name of {@link MappingClass#getTables tables} property for {@link
     * MappingClass class objects}.
     */
    public static final String PROP_TABLES = "tables"; // NOI18N

    /**
     * Name of {@link MappingClass#getFields fields} property for {@link
     * MappingClass class objects}.
     */
    public static final String PROP_FIELDS = "fields"; // NOI18N

    /**
     * Name of {@link MappingField#isReadOnly read only} property for {@link
     * MappingField field objects}.
     */
    public static final String PROP_READ_ONLY = "readOnly"; // NOI18N

    /**
     * Name of {@link MappingField#isVersion version field} property for {@link
     * MappingField field objects}.
     */
    public static final String PROP_VERSION_FIELD = "versionField"; // NOI18N

    /**
     * Name of {@link MappingField#getFetchGroup fetch group} property for
     * {@link MappingField field objects}.
     */
    public static final String PROP_FETCH_GROUP = "fetchGroup"; // NOI18N

    /**
     * Name of {@link MappingField#getColumns columns} property for {@link
     * MappingField field objects}.
     */
    public static final String PROP_COLUMNS = "columns"; // NOI18N

    /**
     * Name of {@link MappingReferenceKey#getTable table} and {@link
     * MappingTable#getTable table} property for {@link MappingReferenceKey
     * reference key objects} and {@link MappingTable mapping table objects}.
     */
    public static final String PROP_TABLE = "table"; // NOI18N

    /**
     * Name of {@link MappingReferenceKey#getMappingReferenceKey key columns} and
     * {@link MappingTable#getKey key columns} property for {@link
     * MappingReferenceKey reference key objects} and {@link MappingTable
     * mapping table objects}.
     */
    public static final String PROP_KEY_COLUMNS = "keyColumns"; // NOI18N

    /**
     * Name of {@link MappingRelationship#getAssociatedColumns associated
     * columns} property for {@link MappingRelationship relationship objects}.
     */
    public static final String PROP_ASSOCIATED_COLUMNS = "associatedColumns"; // NOI18N

    /**
     * Name of {@link MappingTable#getMappingReferenceKeys reference keys} property
     * for {@link MappingTable mapping table objects}.
     */
    public static final String PROP_REFERENCE_KEYS = "referenceKeys"; // NOI18N

    // </editor-fold>
}
