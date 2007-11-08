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

import oracle.toplink.essentials.descriptors.DescriptorEvent;

/**
 * <p><b>Purpose</b>: Generic association object.
 * This can be used to map hashtable/map containers where the key and value are non-typed primitives.
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public class TypedAssociation extends Association {
    protected Class keyType;
    protected Class valueType;

    /**
     * Default constructor.
     */
    public TypedAssociation() {
        super();
    }

    /**
     * PUBLIC:
     * Create an association.
     */
    public TypedAssociation(Object key, Object value) {
        super(key, value);
        if (key != null) {
            this.keyType = key.getClass();
        }
        this.value = value;
        if (value != null) {
            this.valueType = value.getClass();
        }
    }

    /**
     * PUBLIC:
     * Return the class of the key.
     */
    public Class getKeyType() {
        return keyType;
    }

    /**
     * PUBLIC:
     * Return the class of the value.
     */
    public Class getValueType() {
        return valueType;
    }

    /**
     * INTERNAL:
     * Handler for the descriptor post build event.
     * Convert the key and values to their appropriate type.
     */
    public void postBuild(DescriptorEvent event) {
        setKey(event.getSession().getDatasourceLogin().getDatasourcePlatform().getConversionManager().convertObject(getKey(), getKeyType()));
        setValue(event.getSession().getDatasourceLogin().getDatasourcePlatform().getConversionManager().convertObject(getValue(), getValueType()));
    }

    /**
     * PUBLIC:
     * Set the class of the key.
     */
    public void setKeyType(Class keyType) {
        this.keyType = keyType;
    }

    /**
     * PUBLIC:
     * Set the class of the value.
     */
    public void setValueType(Class valueType) {
        this.valueType = valueType;
    }
}
