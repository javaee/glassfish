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
package oracle.toplink.essentials.mappings;

import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;

/**
 * <b>Purpose</b>: Maps an attribute to the corresponding database field type.
 * The list of field types that are supported by TopLink's direct to field mapping
 * is dependent on the relational database being used.
 * A converter can be used to convert between the object and data type if they do not match.
 *
 * @see oracle.toplink.essentials.mappings.converters.Converter
 * @see oracle.toplink.essentials.mappings.converters.ObjectTypeConverter
 * @see oracle.toplink.essentials.mappings.converters.TypeConversionConverter
 * @see oracle.toplink.essentials.mappings.converters.SerializedObjectConverter
 *
 * @author Sati
 * @since TopLink/Java 1.0
 */
public class DirectToFieldMapping extends AbstractDirectMapping implements RelationalMapping {

    /**
     * Default constructor.
     */
    public DirectToFieldMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Set the field name in the mapping.
     */
    public void setFieldName(String FieldName) {
        setField(new DatabaseField(FieldName));
    }

    protected void writeValueIntoRow(AbstractRecord row, DatabaseField field, Object fieldValue) {
        row.add(getField(), fieldValue);
    }
}
