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
package oracle.toplink.essentials.internal.expressions;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.mappings.converters.ObjectTypeConverter;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 *  INTERNAL:
 *  Used by function operators in deployment xml generation to accomodate custom functions.
 *  There is no more validation on read because any custom function has to be accepted.
 *  The custom function is assumed to be a normal prefix function.  The first element in the 
 *  databaseStrings of the operator is in the format of databaseString(, e.g. AVG(.  "(" will
 *  be removed on write and attached back on read.
 */

public class ExpressionOperatorConverter extends ObjectTypeConverter  {
    /**
     *  INTERNAL:
     *  Convert to the data value.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        Object fieldValue;
        if (attributeValue == null) {
            fieldValue = getAttributeToFieldValues().get(Helper.getNullWrapper());
        } else {
            fieldValue = getAttributeToFieldValues().get(attributeValue);
            if (fieldValue == null) {
                //Custom function.  Remove "(".
                if (((ExpressionOperator)attributeValue).getDatabaseStrings() != null) {
                    String databaseString = ((ExpressionOperator)attributeValue).getDatabaseStrings()[0];
                    fieldValue = databaseString.substring(0, databaseString.length()-1);                    
                } else {
                    throw DescriptorException.noAttributeValueConversionToFieldValueProvided(attributeValue, getMapping());
                }
            }
        }
        return fieldValue;
    }

    /**
     * INTERNAL:
     * Returns the corresponding attribute value for the specified field value.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object attributeValue = null;

        if (fieldValue == null) {
            attributeValue = getFieldToAttributeValues().get(Helper.getNullWrapper());
        } else {
            try {
                fieldValue = ((AbstractSession)session).getDatasourcePlatform().getConversionManager().convertObject(fieldValue, getFieldClassification());
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
            }

            attributeValue = getFieldToAttributeValues().get(fieldValue);
            //Custom function.  Create an operator for it.
            if (attributeValue == null) {
                attributeValue = ExpressionOperator.simpleFunction(0, (String)fieldValue);
            }
        }
        return attributeValue;
    }
}
