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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.EnumSet;
import java.util.Iterator;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;

/**
 * <b>Purpose</b>: Object type converter is used to match a fixed number of 
 * database data values to a Java enum object value. It can be used when the 
 * values on the database and in the Java differ. To create an object type 
 * converter, simply specify the set of conversion value pairs. A default value 
 * and one-way conversion are also supported for legacy data situations.
 *
 * @author Guy Pelletier
 * @since Toplink 10.1.4RI
 */
public class EnumTypeConverter extends ObjectTypeConverter {
    private Class m_enumClass;
    private String m_enumClassName;
    
    /**
     * PUBLIC:
     * Creating an enum converter this way will create the conversion values
     * for you using ordinal or name values.
     */
    public EnumTypeConverter(DatabaseMapping mapping, Class enumClass, boolean useOrdinalValues) {
        super(mapping);
        m_enumClassName = enumClass.getName();
        
        EnumSet theEnums = EnumSet.allOf(enumClass);
        Iterator<Enum> i = theEnums.iterator();
        
        while (i.hasNext()) {
            Enum theEnum = i.next();
            
            if (useOrdinalValues) {
                addConversionValue(theEnum.ordinal(), theEnum.name());
            } else {
                addConversionValue(theEnum.name(), theEnum.name());
            }
        }
    }
    
    /**
     * PUBLIC:
     * Creating an enum converter this way expects that you will provide
     * the conversion values separately.
     */
    public EnumTypeConverter(DatabaseMapping mapping, String enumClassName) {
        super(mapping);
        m_enumClassName = enumClassName;
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this converter to actual 
     * class-based settings. This method is used when converting a project 
     * that has been built with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    m_enumClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(m_enumClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(m_enumClassName, exception.getException());
                }
            } else {
                m_enumClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(m_enumClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exception){
            throw ValidationException.classNotFoundWhileConvertingClassNames(m_enumClassName, exception);
        }
    }
    
    /**
     * INTERNAL:
     * Returns the corresponding attribute value for the specified field value.
     * Wraps the super method to return an Enum type from the string conversion.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object obj = super.convertDataValueToObjectValue(fieldValue, session);
        
        if (fieldValue == null) {
            return obj;
        } else {
            return Enum.valueOf(m_enumClass, (String) obj);
        }
    }
    
    /**
     * INTERNAL:
     * Convert Enum object to the data value. Internal enums are stored as
     * strings (names) so this method wraps the super method in that if
     * breaks down the enum to a string name before converting it.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        if (attributeValue == null) {
            return super.convertObjectValueToDataValue(null, session);
        } else {
            return super.convertObjectValueToDataValue((Enum.class.cast(attributeValue)).name(), session);
        }
    }
}
