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
 * RuntimeMappingField.java
 *
 */


package com.sun.persistence.runtime.model.mapping.impl;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.org.apache.jdo.store.Transcriber;

import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.impl.model.mapping.MappingFieldImplDynamic;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public class RuntimeMappingFieldImpl extends MappingFieldImplDynamic
        implements RuntimeMappingField {

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Creates a new instance of RuntimeMappingFieldImpl
     */
    public RuntimeMappingFieldImpl() {
    }

    /**
     * Create new RuntimeMappingFieldImpl with the corresponding name and
     * declaring class.
     * @param name the name of the field
     * @param declaringClass the class to attach to
     */
    protected RuntimeMappingFieldImpl(String name,
            MappingClass declaringMappingClass) {
        super(name, declaringMappingClass);
    }

    // </editor-fold>

    // <editor-fold desc="//======= RuntimeMappingField & related convenience methods =========">

    /**
     * @inheritDoc 
     */
    public Transcriber getTranscriber() {
        throw new UnsupportedOperationException();
    }

    /**
     * @inheritDoc 
     */
    public String getSQLCast() {
        throw new UnsupportedOperationException();
    }
 
    /**
     * @inheritDoc 
     */
    public MappingTable getFirstMappingTable() {
        ColumnElement[] allColumns = getColumns();

        return ((allColumns.length > 0) ?
            getDeclaringMappingClass().getMappingTable(
                allColumns[0].getDeclaringTable().getName().getName()) : null);
    }

    // </editor-fold>
}
