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
package oracle.toplink.essentials.internal.parsing;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Specify type helper methods.
 */
public interface TypeHelper {

    /** Returns the name of the specified type. */
    public String getTypeName(Object type);

    /** Returns the class object of the specified type. */
    public Class getJavaClass(Object type);

    /** Returns a type representation for the specified type name or null if
     * there is no such type. */
    public Object resolveTypeName(String typeName);

    /** Returns the type of the attribute with the specified name in the
     * specified owner class. */ 
    public Object resolveAttribute(Object ownerClass, String attribute);

    /** Returns the type of the class corresponding to the spcified abstract
     * schema type. */
    public Object resolveSchema(String schemaName);

    /** Returns the enum constant if the specified type denotes an enum type
     * and the specified constant denotes a constant of the enum type. */
    public Object resolveEnumConstant(Object enumType, String constant);

    /** Returns the type representation of class Object.*/
    public Object getObjectType();

    /** Returns the boolean type representation.*/
    public Object getBooleanType();

    /** Returns the char type representation.*/
    public Object getCharType();

    /** Returns the int type representation.*/
    public Object getIntType();

    /** Returns the long type representation.*/
    public Object getLongType();

    /** Returns the type representation of class Long.*/
    public Object getLongClassType();

    /** Returns the float type representation.*/
    public Object getFloatType();

    /** Returns the double type representation.*/
    public Object getDoubleType();

    /** Returns the type representation of class Double.*/
    public Object getDoubleClassType();

    /** Returns the type representation oc class String.*/
    public Object getStringType();

    /** Returns the type representation of class BigInteger.*/
    public Object getBigIntegerType();

    /** Returns the type representation of class BigDecimal.*/
    public Object getBigDecimalType();

    /** Returns true if the specified type denotes an enum type. */
    public boolean isEnumType(Object type);

    /** Returns true if the specified type represents an
     * integral type (or wrapper), a floating point type (or wrapper),
     * BigInteger or BigDecimal. */
    public boolean isNumericType(Object type);

    /** Returns true if the specified type represents an
     * integral type or a wrapper class of an integral type. */
    public boolean isIntegralType(Object type);
    
    /** Returns true if the specified type represents an floating point type
     * or a wrapper class of an floating point type. */ 
    public boolean isFloatingPointType(Object type);

    /** Returns true if the specified type represents java.lang.String. */
    public boolean isStringType(Object type);

    /** Returns true if the specified type represents java.math.BigInteger. */
    public boolean isBigIntegerType(Object type);

    /** Returns true if the specified type represents java.math.BigDecimal. */
    public boolean isBigDecimalType(Object type);

    /** Returns true if the specified type denotes an orable type. */
    public boolean isOrderableType(Object type);

    /** Returns true if the specified type denotes an entity class. */
    public boolean isEntityClass(Object type);

    /** Returns true if the specified type denotes an embedded class. */
    public boolean isEmbeddable(Object type);
    
    /** Returns true if the specified type denotes an embedded attribute. */
    public boolean isEmbeddedAttribute(Object ownerClass, String attribute);

    /** Returns true if the specified type denotes a simple state attribute. */
    public boolean isSimpleStateAttribute(Object ownerClass, String attribute);
    
    /** Returns true if the specified attribute denotes a single valued
     * or collection valued relationship attribute. 
     */
    public boolean isRelationship(Object ownerClass, String attribute);
    
    /** Returns true if the specified attribute denotes a single valued
     * relationship attribute. 
     */
    public boolean isSingleValuedRelationship(
        Object ownerClass, String attribute);
    
    /** Returns true if the specified attribute denotes a collection valued 
     * relationship attribute. 
     */
    public boolean isCollectionValuedRelationship(
        Object ownerClass, String attribute);
    
    /** Returns true if left is assignable from right. */
    public boolean isAssignableFrom(Object left, Object right);

    /** Binary numeric promotion as specified in the JLS, extended by
     * wrapper classes, BigDecimal and BigInteger.  */
    public Object extendedBinaryNumericPromotion(Object left, Object right);
}
