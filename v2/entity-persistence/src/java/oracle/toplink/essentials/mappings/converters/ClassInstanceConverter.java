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

import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Allows a class name to be converter to and from a new instance of the class.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class ClassInstanceConverter implements Converter {
    protected DatabaseMapping mapping;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public ClassInstanceConverter() {
    }

    /**
     * INTERNAL:
     * Convert the class name to a class, then create an instance of the class.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object attributeValue = null;
        if (fieldValue != null) {
            Class attributeClass = (Class)((AbstractSession)session).getDatasourcePlatform().convertObject(fieldValue, ClassConstants.CLASS);
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        attributeValue = AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(attributeClass));
                    } catch (PrivilegedActionException exception) {
                        throw ConversionException.couldNotBeConverted(fieldValue, attributeClass, exception.getException());
                    }
                } else {
                    attributeValue = PrivilegedAccessHelper.newInstanceFromClass(attributeClass);
                }
            } catch (Exception exception) {
                throw ConversionException.couldNotBeConverted(fieldValue, attributeClass, exception);
            }
        }

        return attributeValue;
    }

    /**
     *  INTERNAL:
     *  Convert to the field class.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        if (attributeValue == null) {
            return null;
        }
        return attributeValue.getClass().getName();
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void initialize(DatabaseMapping mapping, Session session) {
        this.mapping = mapping;
        // CR#... Mapping must also have the field classification.
        if (getMapping().isDirectToFieldMapping()) {
            AbstractDirectMapping directMapping = (AbstractDirectMapping)getMapping();

            // Allow user to specify field type to override computed value. (i.e. blob, nchar)
            if (directMapping.getFieldClassification() == null) {
                directMapping.setFieldClassification(ClassConstants.STRING);
            }
        }
    }

    /**
     * INTERNAL:
     * Return the mapping.
     */
    protected DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * INTERNAL:
     * If the converter converts the value to a non-atomic value, i.e.
     * a value that can have its' parts changed without being replaced,
     * then it must return false, serialization can be non-atomic.
     */
    public boolean isMutable() {
        return false;
    }
}
