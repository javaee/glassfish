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

import java.io.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The serialized object converter can be used to store an arbitrary object or set of objects into a database blob field.
 * It uses the Java serializer so the target must be serializable.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class SerializedObjectConverter implements Converter {
    protected DatabaseMapping mapping;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public SerializedObjectConverter() {
    }

    /**
     * PUBLIC:
     * Default constructor.
     */
    public SerializedObjectConverter(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     * The fieldValue will be a byte array.  Create a ByteArrayInputStream
     * on the fieldValue.  Create an ObjectInputStream on the ByteArrayInputStream
     * to read in the objects.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) throws DescriptorException {
        byte[] bytes;
        try {
            bytes = (byte[])((AbstractSession)session).getDatasourcePlatform().convertObject(fieldValue, ClassConstants.APBYTE);
        } catch (ConversionException e) {
            throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
        }

        if ((bytes == null) || (bytes.length == 0)) {
            return null;
        }
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        Object object = null;
        try {
            // BUG# 2813583
            CustomObjectInputStream objectIn = new CustomObjectInputStream(byteIn, session);
            object = objectIn.readObject();
        } catch (Exception exception) {
            throw DescriptorException.notDeserializable(getMapping(), exception);
        }

        return object;
    }

    /**
     *  INTERNAL:
     *  Convert the object to a byte array through serialize.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(attributeValue);
            objectOut.flush();
        } catch (IOException exception) {
            throw DescriptorException.notSerializable(getMapping(), exception);
        }
        return byteOut.toByteArray();
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
                directMapping.setFieldClassification(ClassConstants.APBYTE);
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
        return true;
    }
}
