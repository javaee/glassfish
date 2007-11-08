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

import oracle.toplink.essentials.internal.queryframework.*;

/**
 * Interface used by clients to interact
 * with the assorted mappings that use <code>ContainerPolicy</code>.
 *
 * @see oracle.toplink.essentials.internal.queryframework.ContainerPolicy
 *
 * @author Big Country
 * @since TOPLink/Java 4.0
 */
public interface ContainerMapping {

    /**
     * PUBLIC:
     * Return the mapping's container policy.
     */
    ContainerPolicy getContainerPolicy();

    /**
     * PUBLIC:
     * Set the mapping's container policy.
     */
    void setContainerPolicy(ContainerPolicy containerPolicy);

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.Collection</code> interface.
     */
    void useCollectionClass(Class concreteClass);

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects. The key used to index a value in the
     * <code>Map</code> is the value returned by a call to the specified
     * zero-argument method.
     * The method must be implemented by the class (or a superclass) of any
     * value to be inserted into the <code>Map</code>.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.Map</code> interface.
     * <p>To facilitate resolving the method, the mapping's referenceClass
     * must set before calling this method.
     */
    void useMapClass(Class concreteClass, String methodName);
}
