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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

import java.util.HashMap;

import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;

/**
 * Logger class for the metadata processors. It defines the common log messages 
 * used by the metadata processors.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataLogger  {
    /*************************************************************************/
    /*                         ORM SPECIFIC MESSSAGES                        */ 
    /*************************************************************************/
    public static final String IGNORE_ID_CLASS_ELEMENT = "orm_warning_ignore_id_class";
    
    public static final String IGNORE_TABLE_ELEMENT = "orm_warning_ignore_table";
    public static final String IGNORE_SECONDARY_TABLE_ELEMENT = "orm_warning_ignore_secondary_table";
    
    public static final String IGNORE_NAMED_QUERY_ELEMENT = "orm_warning_ignore_named_query";
    public static final String IGNORE_NAMED_NATIVE_QUERY_ELEMENT = "orm_warning_ignore_named_native_query";
    
    public static final String IGNORE_MAPPING_ON_WRITE = "orm_warning_ignore_mapping_on_write";
    public static final String IGNORE_QUERY_HINT_UNKNOWN_TYPE = "orm_warning_ignore_query_hint_unknown_type";
    public static final String IGNORE_QUERY_HINT_UNSUPPORTED_TYPE = "orm_warning_ignore_query_hint_unsupported_type";
    public static final String ERROR_LOADING_ORM_XML_FILE = "orm_warning_exception_loading_orm_xml_file";
    public static final String COULD_NOT_FIND_ORM_XML_FILE = "orm_could_not_find_orm_xml_file";
    
    /*************************************************************************/
    /*                  ANNOTATION SPECIFIC MESSSAGES                        */ 
    /*************************************************************************/
    public static final String IGNORE_ANNOTATION = "annotation_warning_ignore_annotation";
    public static final String IGNORE_ID_CLASS_ANNOTATION = "annotation_warning_ignore_id_class";
    
    public static final String IGNORE_TABLE_ANNOTATION = "annotation_warning_ignore_table";
    public static final String IGNORE_SECONDARY_TABLE_ANNOTATION = "annotation_warning_ignore_secondary_table";
    
    public static final String IGNORE_NAMED_QUERY_ANNOTATION = "annotation_warning_ignore_named_query";
    public static final String IGNORE_NAMED_NATIVE_QUERY_ANNOTATION = "annotation_warning_ignore_named_native_query";
    
    /*************************************************************************/
    /*                       COMMON IGNORE MESSSAGES                         */ 
    /*************************************************************************/
    public static final String IGNORE_MAPPING = "metadata_warning_ignore_mapping";
    public static final String IGNORE_LOB = "metadata_warning_ignore_lob";
    public static final String IGNORE_TEMPORAL = "metadata_warning_ignore_temporal";
    public static final String IGNORE_ENUMERATED = "metadata_warning_ignore_enumerated";
    public static final String IGNORE_SERIALIZED = "metadata_warning_ignore_serialized";
    public static final String IGNORE_INHERITANCE = "metadata_warning_ignore_inheritance";
    public static final String IGNORE_PRIMARY_KEY = "metadata_warning_ignore_primary_key";
    public static final String IGNORE_EMBEDDED_ID = "metadata_warning_ignore_embedded_id";
    public static final String IGNORE_VERSION_LOCKING = "metadata_warning_ignore_version_locking";
    public static final String IGNORE_BASIC_FETCH_LAZY = "metadata_warning_ignore_basic_fetch_lazy";
    
    /*************************************************************************/
    /*                       COMMON DEFAULT MESSSAGES                        */ 
    /*************************************************************************/
    public static final String ALIAS = "metadata_default_alias";
    public static final String MAP_KEY_ATTRIBUTE_NAME = "metadata_default_map_key_attribute_name";
    
    public static final String TABLE_NAME = "metadata_default_table_name"; 
    public static final String TABLE_SCHEMA = "metadata_default_table_schema";
    public static final String TABLE_CATALOG = "metadata_default_table_catalog";
    
    public static final String JOIN_TABLE_NAME = "metadata_default_join_table_name";
    public static final String JOIN_TABLE_SCHEMA = "metadata_default_join_table_schema";
    public static final String JOIN_TABLE_CATALOG = "metadata_default_join_table_catalog";
    
    public static final String SECONDARY_TABLE_NAME = "metadata_default_secondary_table_name";
    public static final String SECONDARY_TABLE_SCHEMA = "metadata_default_secondary_table_schema";
    public static final String SECONDARY_TABLE_CATALOG = "metadata_default_secondary_table_catalog";
    
    public static final String COLUMN = "metadata_default_column";
    public static final String PK_COLUMN = "metadata_default_pk_column";
    public static final String FK_COLUMN = "metadata_default_fk_column";
    public static final String SOURCE_PK_COLUMN = "metadata_default_source_pk_column";
    public static final String SOURCE_FK_COLUMN = "metadata_default_source_fk_column";
    public static final String TARGET_PK_COLUMN = "metadata_default_target_pk_column";
    public static final String TARGET_FK_COLUMN = "metadata_default_target_fk_column";
    public static final String DISCRIMINATOR_COLUMN = "metadata_default_discriminator_column";
    public static final String INHERITANCE_PK_COLUMN = "metadata_default_inheritance_pk_column";
    public static final String INHERITANCE_FK_COLUMN = "metadata_default_inheritance_fk_column";
    public static final String SECONDARY_TABLE_PK_COLUMN = "metadata_default_secondary_table_pk_column";
    public static final String SECONDARY_TABLE_FK_COLUMN = "metadata_default_secondary_table_fk_column";
    
    public static final String ONE_TO_ONE_MAPPING = "metadata_default_one_to_one_mapping";
    public static final String ONE_TO_MANY_MAPPING = "metadata_default_one_to_many_mapping";
    public static final String ONE_TO_ONE_MAPPING_REFERENCE_CLASS = "metadata_default_one_to_one_reference_class";
    public static final String ONE_TO_MANY_MAPPING_REFERENCE_CLASS = "metadata_default_one_to_many_reference_class";
    public static final String MANY_TO_ONE_MAPPING_REFERENCE_CLASS = "metadata_default_many_to_one_reference_class";
    public static final String MANY_TO_MANY_MAPPING_REFERENCE_CLASS = "metadata_default_many_to_many_reference_class";
    /*************************************************************************/
    
    protected AbstractSession session; 
    protected HashMap ctxStrings;

    /**
     * INTERNAL:
     */
    public MetadataLogger(AbstractSession session) {
        this.session = session;
        
        // Initialize the context strings.
        ctxStrings = new HashMap();
    
        // ORM specific
        ctxStrings.put(IGNORE_ID_CLASS_ELEMENT, IGNORE_ID_CLASS_ELEMENT);
        ctxStrings.put(IGNORE_TABLE_ELEMENT, IGNORE_TABLE_ELEMENT);
        ctxStrings.put(IGNORE_SECONDARY_TABLE_ELEMENT, IGNORE_SECONDARY_TABLE_ELEMENT);
        ctxStrings.put(IGNORE_NAMED_QUERY_ELEMENT, IGNORE_NAMED_QUERY_ELEMENT);
        ctxStrings.put(IGNORE_NAMED_NATIVE_QUERY_ELEMENT, IGNORE_NAMED_NATIVE_QUERY_ELEMENT);
        ctxStrings.put(IGNORE_MAPPING_ON_WRITE, IGNORE_MAPPING_ON_WRITE);
        ctxStrings.put(IGNORE_QUERY_HINT_UNSUPPORTED_TYPE, IGNORE_QUERY_HINT_UNSUPPORTED_TYPE);
        ctxStrings.put(IGNORE_QUERY_HINT_UNKNOWN_TYPE, IGNORE_QUERY_HINT_UNKNOWN_TYPE);
        ctxStrings.put(ERROR_LOADING_ORM_XML_FILE, ERROR_LOADING_ORM_XML_FILE);
        ctxStrings.put(COULD_NOT_FIND_ORM_XML_FILE, COULD_NOT_FIND_ORM_XML_FILE);
        
        // Annotation specific
        ctxStrings.put(IGNORE_ANNOTATION, IGNORE_ANNOTATION);
        ctxStrings.put(IGNORE_ID_CLASS_ANNOTATION, IGNORE_ID_CLASS_ANNOTATION);
        ctxStrings.put(IGNORE_TABLE_ANNOTATION, IGNORE_TABLE_ANNOTATION);
        ctxStrings.put(IGNORE_SECONDARY_TABLE_ANNOTATION, IGNORE_SECONDARY_TABLE_ANNOTATION);
        ctxStrings.put(IGNORE_NAMED_QUERY_ANNOTATION, IGNORE_NAMED_QUERY_ANNOTATION);
        ctxStrings.put(IGNORE_NAMED_NATIVE_QUERY_ANNOTATION, IGNORE_NAMED_NATIVE_QUERY_ANNOTATION);
        
        // Common ignore messages for ORM and annotations.
        ctxStrings.put(IGNORE_MAPPING, IGNORE_MAPPING);
        ctxStrings.put(IGNORE_LOB, IGNORE_LOB);
        ctxStrings.put(IGNORE_TEMPORAL, IGNORE_TEMPORAL);
        ctxStrings.put(IGNORE_ENUMERATED, IGNORE_ENUMERATED);
        ctxStrings.put(IGNORE_SERIALIZED, IGNORE_SERIALIZED);
        ctxStrings.put(IGNORE_INHERITANCE, IGNORE_INHERITANCE);
        ctxStrings.put(IGNORE_PRIMARY_KEY, IGNORE_PRIMARY_KEY);
        ctxStrings.put(IGNORE_EMBEDDED_ID, IGNORE_EMBEDDED_ID);
        ctxStrings.put(IGNORE_VERSION_LOCKING, IGNORE_VERSION_LOCKING);
        ctxStrings.put(IGNORE_BASIC_FETCH_LAZY, IGNORE_BASIC_FETCH_LAZY);
        
        // Common default messages for ORM and annotations.
        ctxStrings.put(ALIAS, ALIAS);
        ctxStrings.put(MAP_KEY_ATTRIBUTE_NAME, MAP_KEY_ATTRIBUTE_NAME);
        
        ctxStrings.put(TABLE_NAME, TABLE_NAME);
        ctxStrings.put(TABLE_SCHEMA, TABLE_SCHEMA);
        ctxStrings.put(TABLE_CATALOG, TABLE_CATALOG);
        
        ctxStrings.put(JOIN_TABLE_NAME, JOIN_TABLE_NAME);
        ctxStrings.put(JOIN_TABLE_SCHEMA, JOIN_TABLE_SCHEMA);
        ctxStrings.put(JOIN_TABLE_CATALOG, JOIN_TABLE_CATALOG);
        
        ctxStrings.put(SECONDARY_TABLE_NAME, SECONDARY_TABLE_NAME);
        ctxStrings.put(SECONDARY_TABLE_SCHEMA, SECONDARY_TABLE_SCHEMA);
        ctxStrings.put(SECONDARY_TABLE_CATALOG, SECONDARY_TABLE_CATALOG);
        
        ctxStrings.put(COLUMN, COLUMN);
        ctxStrings.put(PK_COLUMN, PK_COLUMN);
        ctxStrings.put(FK_COLUMN, FK_COLUMN);
        ctxStrings.put(SOURCE_PK_COLUMN, SOURCE_PK_COLUMN);
        ctxStrings.put(SOURCE_FK_COLUMN, SOURCE_FK_COLUMN);
        ctxStrings.put(TARGET_PK_COLUMN, TARGET_PK_COLUMN);
        ctxStrings.put(TARGET_FK_COLUMN, TARGET_FK_COLUMN);
        ctxStrings.put(DISCRIMINATOR_COLUMN, DISCRIMINATOR_COLUMN);
        ctxStrings.put(INHERITANCE_PK_COLUMN, INHERITANCE_PK_COLUMN);
        ctxStrings.put(INHERITANCE_FK_COLUMN, INHERITANCE_FK_COLUMN);
        ctxStrings.put(SECONDARY_TABLE_PK_COLUMN, SECONDARY_TABLE_PK_COLUMN);
        ctxStrings.put(SECONDARY_TABLE_FK_COLUMN, SECONDARY_TABLE_FK_COLUMN);
        
        ctxStrings.put(ONE_TO_ONE_MAPPING, ONE_TO_ONE_MAPPING);
        ctxStrings.put(ONE_TO_MANY_MAPPING, ONE_TO_MANY_MAPPING);
        ctxStrings.put(ONE_TO_ONE_MAPPING_REFERENCE_CLASS, ONE_TO_ONE_MAPPING_REFERENCE_CLASS);
        ctxStrings.put(ONE_TO_MANY_MAPPING_REFERENCE_CLASS, ONE_TO_MANY_MAPPING_REFERENCE_CLASS);
        ctxStrings.put(MANY_TO_ONE_MAPPING_REFERENCE_CLASS, MANY_TO_ONE_MAPPING_REFERENCE_CLASS);
        ctxStrings.put(MANY_TO_MANY_MAPPING_REFERENCE_CLASS, MANY_TO_MANY_MAPPING_REFERENCE_CLASS);
    }
    
    /**
     * INTERNAL:
	 * Return the logging context string for the given context.
     */
	protected String getLoggingContextString(String context) {
        return (String) ctxStrings.get(context);
	}  
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void log(int level, String ctx, Object[] params) {
        session.log(level, SessionLog.EJB_OR_METADATA, getLoggingContextString(ctx), params);
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logConfigMessage(String ctx, MetadataAccessor accessor, Object param) {
        log(SessionLog.CONFIG, ctx, new Object[] { accessor.getAnnotatedElement(), param });
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logConfigMessage(String ctx, MetadataAccessor accessor, Object param1, Object param2) {
        log(SessionLog.CONFIG, ctx, new Object[] { accessor.getJavaClass(), accessor.getAnnotatedElement(), param1, param2 });
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logConfigMessage(String ctx, MetadataDescriptor descriptor, Object param) {
        log(SessionLog.CONFIG, ctx, new Object[] { descriptor.getJavaClass(), param });
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logConfigMessage(String ctx, Object object) {
        log(SessionLog.CONFIG, ctx, new Object[] { object });    
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logConfigMessage(String ctx, Object param1, Object param2) {
        log(SessionLog.CONFIG, ctx, new Object[] { param1, param2 });    
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logWarningMessage(String ctx, MetadataAccessor accessor) {
        log(SessionLog.WARNING, ctx, new Object[] { accessor.getJavaClass(), accessor.getAnnotatedElement() });
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logWarningMessage(String ctx, Object param) {
        log(SessionLog.WARNING, ctx, new Object[] { param });    
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logWarningMessage(String ctx, Object param1, Object param2) {
        log(SessionLog.WARNING, ctx, new Object[] { param1, param2 });    
    }
    
    /**
     * INTERNAL:
     * Logging utility method.
     */
    public void logWarningMessage(String ctx, Object param1, Object param2, Object param3) {
        log(SessionLog.WARNING, ctx, new Object[] {param1, param2, param3});
    }
    
    /**
     * INTERNAL:
     * Set the session to log to.
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }
}
