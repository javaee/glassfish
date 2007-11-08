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
package oracle.toplink.essentials.mappings.converters;

import java.io.Serializable;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.sessions.Session;

/**
 * <p><b>Purpose</b>: Conversion interface to allow conversion between object and data types.
 * This can be used in any mapping to convert between the object and data types without requiring code
 * placed in the object model.
 * TopLink provides several common converters, but the application can also define it own.
 *
 * @see DirectToFieldMapping#setConverter(Converter)
 * @see DirectCollectionMapping#setConverter(Converter)
 * @see ObjectTypeConverter
 * @see TypeConversionConverter
 * @see SerializedObjectConverter
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public interface Converter extends Serializable {

    /**
     * PUBLIC:
     * Convert the object's representation of the value to the databases' data representation.
     * For example this could convert between a Calendar Java type and the sql.Time datatype.
     */
    Object convertObjectValueToDataValue(Object objectValue, Session session);

    /**
     * PUBLIC:
     * Convert the databases' data representation of the value to the object's representation.
     * For example this could convert between an sql.Time datatype and the Java Calendar type.
     */
    Object convertDataValueToObjectValue(Object dataValue, Session session);

    /**
     * PUBLIC:
     * If the converter converts the value to a mutable value, i.e.
     * a value that can have its' parts changed without being replaced,
     * then it must return true.  If the value is not mutable, cannot be changed without
     * replacing the whole value then false must be returned.
     * This is used within the UnitOfWork to determine how to clone.
     */
    public boolean isMutable();

    /**
     * PUBLIC:
     * Allow for any initialization.
     */
    void initialize(DatabaseMapping mapping, Session session);
}
