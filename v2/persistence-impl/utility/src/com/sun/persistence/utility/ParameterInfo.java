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
 * ParameterInfo
 *
 * Created on January 31, 2003
 */


package com.sun.persistence.utility;

//XXX FIXME This file may need to move under support/sqlstore.

public class ParameterInfo {
    /**
     * Parameter index. The index corresponds to JDO QL parameters.
     */
    private final int index;

    /**
     * Parameter type. See FieldTypeEnumeration for possible values.
     */
    private final int type;

    /**
     * Associated field to a parameter for runtime processing. This is defined
     * if and only if the corresponding subfilter is of the form: field
     * [relational op] _jdoParam or _jdoParam [relational op] field Otherwise,
     * this is null.
     */
    private final String associatedField;

    /**
     * Constructor
     */
    public ParameterInfo(int index, int type) {
        this(index, type, null);
    }

    /**
     * Constructs a new ParameterInfo with the specified index, type and
     * associatedField.
     * @param index
     * @param type
     * @param associatedField
     */
    public ParameterInfo(int index, int type, String associatedField) {
        this.index = index;
        this.type = type;
        this.associatedField = associatedField;
    }

    /**
     * Returns the parameter index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the parameter type. See FieldTypeEnumeration for possible
     * values.
     */
    public int getType() {
        return type;
    }

    /**
     * @returns the associated field.
     */
    public String getAssociatedField() {
        return associatedField;
    }
}
