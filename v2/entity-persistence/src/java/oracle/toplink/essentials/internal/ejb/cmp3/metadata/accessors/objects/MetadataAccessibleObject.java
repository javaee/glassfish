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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects;

import java.lang.reflect.Type;
import java.lang.reflect.AnnotatedElement;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;

/**
 * Parent object that is used to hold onto a valid EJB 3.0 decorated method
 * or field.
 * 
 * @author Guy Pelletier
 * @since TopLink 10.1.3/EJB 3.0 Preview
 */
public abstract class MetadataAccessibleObject  {
    private String m_name;
    private Class m_rawClass;
    private Type m_relationType;
    private String m_attributeName;
    private AnnotatedElement m_annotatedElement;
    
    /**
     * INTERNAL:
     */
    public MetadataAccessibleObject(AnnotatedElement annotatedElement) {
        m_annotatedElement = annotatedElement;   
    }
    
    /**
     * INTERNAL:
     * Return the actual field or method.
     */
    public AnnotatedElement getAnnotatedElement() {
        return m_annotatedElement;
    }
    
    /**
     * INTERNAL:
     * Set the relation type of this accessible object.
     */
    public String getAttributeName() {
        return m_attributeName;
    }
    
    /**
     * INTERNAL:
     * This should only be called for accessor's of type Map. It will return
     * the Map key type if generics are used, null otherwise.
     */
    public Class getMapKeyClass() {
        if (MetadataHelper.isGenericCollectionType(m_relationType)) {
            // By default, the reference class is equal to the relation
            // class. But if the relation class is a generic we need to 
            // extract and set the actual reference class from the generic. 
            return MetadataHelper.getMapKeyTypeFromGeneric(m_relationType);
        } else {
            return null;
        }
    }
    
    /**
     * INTERNAL:
     * Set the relation type of this accessible object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * INTERNAL:
     * Return the raw class for this accessible object. E.g. For an 
     * accessible object with a type of java.util.Collection<Employee>, this 
     * method will return java.util.Collection. 
     * @See getReferenceClassFromGeneric() to get Employee.class back.
     */
    public Class getRawClass() {
        if (m_rawClass == null) {
            if (MetadataHelper.isGenericCollectionType(m_relationType)) {
                // By default, the raw class is equal to the relation
                // class. But if the relation class is a generic we need to 
                // extract and set the actual raw class from the generic. 
                m_rawClass = MetadataHelper.getRawClassFromGeneric(m_relationType);
            } else {
                m_rawClass = (Class) m_relationType;
            }
        }
        
        return m_rawClass;
    }
    
    /**
     * INTERNAL:
     * Return the reference class from the generic specification on this 
     * accessible object.
     * Here is what you will get back from this method given the following
     * scenarios:
     * 1 - public Collection<String> getTasks() => String.class
     * 2 - public Map<String, Integer> getTasks() => Integer.class
     * 3 - public Employee getEmployee() => null
     * 4 - public Collection getTasks() => null
     * 5 - public Map getTasks() => null
     */
    public Class getReferenceClassFromGeneric() {
        if (MetadataHelper.isGenericCollectionType(m_relationType)) {
            return MetadataHelper.getReturnTypeFromGeneric(m_relationType);
        } else {
            return null;
        }
    }
    
    /**
     * INTERNAL:
     * Return the relation type of this accessible object.
     */
    public Type getRelationType() {
        return m_relationType;
    }
    
    /**
     * INTERNAL:
     * Set the annotated element for this accessible object.
     * Once the class loader changes, we need to be able to update our
     * classes.
     */
    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        m_annotatedElement = annotatedElement;
    }
    
    /**
     * INTERNAL:
     * Set the relation type of this accessible object.
     */
    protected void setAttributeName(String attributeName) {
        m_attributeName = attributeName;
    }
    
    /**
     * INTERNAL:
     * Set the relation type of this accessible object.
     */
    protected void setName(String name) {
        m_name = name;
    }
    
    /**
     * INTERNAL:
     * Set the relation type of this accessible object.
     */
    protected void setRelationType(Type relationType) {
        m_relationType = relationType;
    }
}
