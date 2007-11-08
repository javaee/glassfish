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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners;

import java.lang.reflect.Method;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.exceptions.ValidationException;

/**
 * A callback listener for those entities that define callback methods. 
 * Callback methods on an entity must be signatureless, hence, this class 
 * overrides behavior from CBListener.
 * 
 * @author Guy Pelletier
 * @since TopLink 10.1.3/EJB 3.0 Preview
 */
public class MetadataEntityClassListener extends MetadataEntityListener {
    /**
     * INTERNAL: 
     */
    public MetadataEntityClassListener(Class entityClass) {
        super(entityClass);
    }
    
    /**
     * INTERNAL: 
     * For entity classes listener methods, they need to override listeners 
     * from mapped superclasses for the same method. So we need to override 
     * this method and make the override check instead of it throwing an
     * exception for multiple lifecycle methods for the same event.
     */
    public void addEventMethod(String event, Method method) {
        if (! hasOverriddenEventMethod(method, event)) {
            super.addEventMethod(event, method);
        }
    }
    
    /**
     * INTERNAL:
     */
    public Class getListenerClass() {
        return getEntityClass();
    }
    
    /**
     * INTERNAL: 
     */
    protected void invokeMethod(String event, DescriptorEvent descriptorEvent) {
        Object[] objectList = {};
        invokeMethod(getEventMethod(event), descriptorEvent.getObject(), objectList, descriptorEvent);
    }
    
    /**
     * INTERNAL:
     */
     public boolean isEntityClassListener() {
        return true;
    }
    
    /**
     * INTERNAL:
     */
    protected void validateMethod(Method method) {
        if (method.getParameterTypes().length > 0) {
            throw ValidationException.invalidEntityCallbackMethodArguments(getEntityClass(), method.getName());
        } else {
            // So far so good, now check the method modifiers.
            validateMethodModifiers(method);
        }
    }
}
