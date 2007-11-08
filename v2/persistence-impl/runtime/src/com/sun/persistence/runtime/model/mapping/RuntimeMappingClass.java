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
 * RuntimeMappingClass.java
 *
 */


package com.sun.persistence.runtime.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.persistence.api.model.mapping.MappingClass;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public interface RuntimeMappingClass extends MappingClass {

    // <editor-fold desc="//======================= declaring model ===========================">

    /**
     * Returns the declaring mapping model of this mapping class.
     * @return the mapping model that owns this mapping class
     */
    public RuntimeMappingModel getDeclaringMappingModel();

    // </editor-fold>

    // <editor-fold desc="//======================== class handling  ==========================">
 
    /**
     * Returns the mapping class instance corresponding to the superclass of 
     * this mapping class.  Note that this is not necessarily the direct
     * (java) superclass if there is no mapping class for that superclass.  
     * For example, if this mapping class represents a class named 
     * "SalariedEmployee" which has a superclass "Employee", this method 
     * returns the MappingClass instance which represents "Employee".
     * If there is no such superclass, then <code>null</code> is returned.
     * @return the mapping class of the superclass of the mapped class
     *         or <code>null</code> if there is no superclass
     */
    public RuntimeMappingClass getSuperclassMappingClass();
    
    /**
     * Returns the java.lang.Class instance corresponding to this mapping class.
     * For example, if this mapping class represents a class named "Employee", 
     * this method returns Employee.class.  
     * @return the Class instance of the mapped class.
     */
    public Class getJavaClass();
    
    // </editor-fold>

    // <editor-fold desc="//======================= field handling ============================">
    
    /**
     * Returns the list of fields (MappingFields) in this mapping class.  This
     * list includes both local and relationship fields.
     * @return the mapping fields in this mapping class
     */
    public RuntimeMappingField[] getMappingFields();

    /**
     * Scans through this mapping class looking for a field whose name matches
     * the specified name.
     * @param name name of the field to find.
     * @return the mapping field whose name matches the name parameter
     */
    public RuntimeMappingField getMappingField(String name);
    
    /**
     * Scans through this mapping class looking for a field with the 
     * specified absolute field number. The method returns <code>null</code>
     * if there is no such field.
     * @param fieldNumber the number of the field to find.
     * @return the mapping field with the specified field number or 
     * <code>null</code> if there is no such field.
     */
    public RuntimeMappingField getMappingField(int fieldNumber);    
   
    /**
     * This method returns a mapping field for the field with the specified name.
     * If this mapping class already declares such a field, the existing mapping
     * field is returned. Otherwise, it creates a new mapping field, sets its
     * declaring mapping class and returns the new instance.
     * @param name the name of the field
     * @return an existing mapping field if it exists already, a new mapping
     *         field otherwise
     * @throws ModelException if impossible
     */
    public RuntimeMappingField createMappingField(String name) 
        throws ModelException;

    /**
     * Returns the list of version fields in this mapping class. This list only
     * includes fields if the consistency level is VERSION_CONSISTENCY.
     * @return the version fields in this mapping class
     */
    public RuntimeMappingField[] getVersionMappingFields();
    
    /**
     * Returns the MappingField instances corresponding to the primary key
     * fields of the JDOClass associated with this MappingClass.
     * @return the primary key mapping fields in this mapping class
     */
    public RuntimeMappingField[] getPrimaryKeyMappingFields();

    /**
     * Returns the MappingField instances corresponding to the default fetch
     * group of the JDOClass associated with this MappingClass.
     * @return the default fetch group mapping fields in this mapping class
     */
    public RuntimeMappingField[] getDefaultFetchGroupMappingFields();

    // </editor-fold>

}
