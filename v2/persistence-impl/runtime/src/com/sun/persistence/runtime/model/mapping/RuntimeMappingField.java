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


package com.sun.persistence.runtime.model.mapping;

import com.sun.org.apache.jdo.store.Transcriber;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingTable;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public interface RuntimeMappingField extends MappingField {

    /**
     * 
     * @return 
     */
    public Transcriber getTranscriber();

    /**
     * 
     * @return 
     */
    public String getSQLCast();

    /**
     * Returns the MappingTable corresponding to the first ColumnElement in 
     * the mapping of this field.  Note that the columns are stored in the 
     * order they were added using {@link #addColumn}.
     * @return The MappingTable object that corresponds to the declaring 
     * TableElement of the first ColumnElement in this field's mapping.
     * @see #addColumn
     */
    public MappingTable getFirstMappingTable();
}
