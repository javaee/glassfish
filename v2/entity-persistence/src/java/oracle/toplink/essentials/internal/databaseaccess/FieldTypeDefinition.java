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
package oracle.toplink.essentials.internal.databaseaccess;

import java.io.*;
import oracle.toplink.essentials.internal.helper.*;

/**
 * INTERNAL:
 *    <b>Purpose</b>: Define a database platform specific definition for a platform independant Java class type.
 *    This is used for the field creation within a table creation statement.
 *    <p><b>Responsibilities</b>:<ul>
 *    <li> Store a default size and know if the size option is required or optional.
 *    <li> Store the name of the real database type.
 *    <li> Maintain maximum precision and optionall min & max Scale.
 *    </ul>
 */
public class FieldTypeDefinition implements Serializable {
    protected String name;
    protected int defaultSize;
    protected int defaultSubSize;
    protected boolean isSizeAllowed;
    protected boolean isSizeRequired;
    protected int maxPrecision;
    protected int minScale;
    protected int maxScale;
    protected boolean shouldAllowNull; //allow for specific types/platforms to not allow null

    public FieldTypeDefinition() {
        defaultSize = 10;
        isSizeRequired = false;
        isSizeAllowed = true;
        maxPrecision = 10;
        minScale = 0;
        maxScale = 0;
        shouldAllowNull = true;
    }

    /**
    * Return a new field type.
    * @see #setName()
    */
    public FieldTypeDefinition(String databaseTypeName) {
        this();
        name = databaseTypeName;
    }

    /**
     * Return a new field type with a required size defaulting to the defaultSize.
     */
    public FieldTypeDefinition(String databaseTypeName, int defaultSize) {
        this();
        this.name = databaseTypeName;
        this.defaultSize = defaultSize;
        this.isSizeRequired = true;
        setMaxPrecision(defaultSize);
    }

    /**
     * Return a new field type with a required size defaulting to the defaultSize.
     */
    public FieldTypeDefinition(String databaseTypeName, int defaultSize, int defaultSubSize) {
        this();
        this.name = databaseTypeName;
        this.defaultSize = defaultSize;
        this.defaultSubSize = defaultSubSize;
        this.isSizeRequired = true;
        setMaxPrecision(defaultSize);
        setMaxScale(defaultSubSize);
    }

    /**
     * Return a new field type with a required size defaulting to the defaultSize.
     */
    public FieldTypeDefinition(String databaseTypeName, boolean allowsSize) {
        this();
        this.name = databaseTypeName;
        this.isSizeAllowed = allowsSize;
    }
    
    /** Return a new field type with a required size defaulting to the defaultSize and 
     *  shouldAllowNull set to allowsNull.
     */
    public FieldTypeDefinition(String databaseTypeName, boolean allowsSize, boolean allowsNull) {
        this(databaseTypeName, allowsSize);
        this.shouldAllowNull = allowsNull;
    }

    /**
     * Return the default size for this type.
     * This default size will be used if the database requires specification of a size,
     * and the table definition did not provide one.
     */
    public int getDefaultSize() {
        return defaultSize;
    }

    /**
    * Return the default sub-size for this type.
    * This default size will be used if the database requires specification of a size,
    * and the table definition did not provide one.
    */
    public int getDefaultSubSize() {
        return defaultSubSize;
    }

    public int getMaxPrecision() {
        return maxPrecision;
    }

    public int getMaxScale() {
        return maxScale;
    }

    public int getMinScale() {
        return minScale;
    }

    /**
    * Return the name.
    * @param name can be any database primitive type name,
    * this name will then be mapped to the Java primitive type,
    * the datbase type varies by platform and the mappings can be found in the subclasses of DatabasePlatform.
    *
    *    these Java names and their ODBC mappings include;
    *        - Integer        -> SQL_INT
    *        - Float            -> SQL_FLOAT
    *        - Double            -> SQL_DOUBLE
    *        - Long            -> SQL_LONG
    *        - Short            -> SQL_INT
    *        - BigDecimal    -> SQL_NUMERIC
    *        - BigInteger    -> SQL_NUMERIC
    *        - String            -> SQL_VARCHAR
    *        - Array            -> BLOB
    *        - Character[]    -> SQL_CHAR
    *        - Boolean        -> SQL_BOOL
    *        - Text            -> CLOB
    *        - Date            -> SQL_DATE
    *        - Time            -> SQL_TIME
    *        - Timestamp    -> SQL_TIMESTAMP
    *
    * @see oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform
    */
    public String getName() {
        return name;
    }

    /**
    * Return if this type can support a size specification.
    */
    public boolean isSizeAllowed() {
        return isSizeAllowed;
    }

    /**
    * Return if this type must have a size specification.
    */
    public boolean isSizeRequired() {
        return isSizeRequired;
    }

    /** 
     * Return if this type is allowed to be null for this platform
     */
    public boolean shouldAllowNull() {
        return this.shouldAllowNull;
    }
    
    /**
    * Set the default size for this type.
    * This default size will be used if the database requires specification of a size,
    * and the table definition did not provide one.
    */
    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    /**
    * Set the default sub-size for this type.
    * This default size will be used if the database requires specification of a size,
    * and the table definition did not provide one.
    */
    public void setDefaultSubSize(int defaultSubSize) {
        this.defaultSubSize = defaultSubSize;
    }

    /**
    * Set if this type can support a size specification.
    */
    public void setIsSizeAllowed(boolean aBoolean) {
        isSizeAllowed = aBoolean;
    }

    /**
    * Set if this type must have a size specification.
    */
    public void setIsSizeRequired(boolean aBoolean) {
        isSizeRequired = aBoolean;
    }

    /**
     * Set if this type is allowed to be null for this platform
     */
    public void setShouldAllowNull(boolean allowsNull) {
        this.shouldAllowNull = allowsNull;
    }
    
    /**
     *    Set the maximum precision and the minimum and maximum scale.
     *    @return    this    Allowing the method to be invoked inline with constructor
     */
    public FieldTypeDefinition setLimits(int maxPrecision, int minScale, int maxScale) {
        setMaxPrecision(maxPrecision);
        setMinScale(minScale);
        setMaxScale(maxScale);
        return this;
    }

    public void setMaxPrecision(int maximum) {
        maxPrecision = maximum;
    }

    public void setMaxScale(int maximum) {
        maxScale = maximum;
    }

    public void setMinScale(int minimum) {
        minScale = minimum;
    }

    /**
    * Set the name.
    * @param name can be any database primitive type name,
    * this name will then be mapped to the Java primitive type,
    * the datbase type varies by platform and the mappings can be found in the subclasses of DatabasePlatform.
    *
    *    these Java names and their ODBC mappings include;
    *        - Integer        -> SQL_INT
    *        - Float            -> SQL_FLOAT
    *        - Double        -> SQL_DOUBLE
    *        - Long            -> SQL_LONG
    *        - Short            -> SQL_INT
    *        - BigDecimal    -> SQL_NUMERIC
    *        - BigInteger    -> SQL_NUMERIC
    *        - String        -> SQL_VARCHAR
    *        - Array            -> BLOB
    *        - Character[]    -> SQL_CHAR
    *        - Boolean        -> SQL_BOOL
    *        - Text            -> CLOB
    *        - Date            -> SQL_DATE
    *        - Time            -> SQL_TIME
    *        - Timestamp    -> SQL_TIMESTAMP
    *
    * @see oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform
    */
    public void setName(String name) {
        this.name = name;
    }

    /**
    * Set this type to not allow a size specification.
    */
    public void setSizeDisallowed() {
        setIsSizeAllowed(false);
    }

    /**
    * Set this type to optionally have a size specification.
    */
    public void setSizeOptional() {
        setIsSizeRequired(false);
        setIsSizeAllowed(true);
    }

    /**
    * Set this type to require to have a size specification.
    */
    public void setSizeRequired() {
        setIsSizeRequired(true);
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getName() + ")";
    }
}
