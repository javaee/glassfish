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

import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;

/**
 * <p><b>Purpose</b>: This provides an abstract class for setting and retrieving
 * the attribute value for the mapping from an object.
 * It can be used in advanced situations if the attribute
 * requires advanced conversion of the mapping value, or a real attribute does not exist.
 *
 *    @author James
 *    @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public abstract class AttributeAccessor implements Cloneable, Serializable {

    /** Stores the name of the attribute */
    protected String attributeName;

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * INTERNAL:
     * Return the attribute name.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * INTERNAL:
     * Set the attribute name.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Return the class type of the attribute.
     */
    public Class getAttributeClass() {
        return ClassConstants.OBJECT;
    }

    /**
     * Allow any initialization to be performed with the descriptor class.
     */
    public void initializeAttributes(Class descriptorClass) throws DescriptorException {
        if (getAttributeName() == null) {
            throw DescriptorException.attributeNameNotSpecified();
        }
    }

    /**
     * Return the attribute value from the object.
     */
    public abstract Object getAttributeValueFromObject(Object object) throws DescriptorException;

    /**
     * Set the attribute value into the object.
     */
    public abstract void setAttributeValueInObject(Object object, Object value) throws DescriptorException;
}
