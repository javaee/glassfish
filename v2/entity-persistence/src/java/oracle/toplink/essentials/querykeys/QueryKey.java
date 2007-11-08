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
package oracle.toplink.essentials.querykeys;

import java.io.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: Define a Java appropriate alias to a database field or function.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the name of the alias.
 * <li> Define the descriptor of the alias.
 * </ul>
 */
public class QueryKey implements Cloneable, Serializable {
    protected String name;
    protected ClassDescriptor descriptor;

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        Object object = null;

        try {
            object = super.clone();
        } catch (Exception exception) {
            throw new InternalError(exception.toString());
        }

        return object;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this QueryKey to actual class-based
     * settings
     * Will be overridded by subclasses
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){}

    /**
     * INTERNAL:
     * Return the descriptor.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * PUBLIC:
     * Return the name for the query key.
     * This is the name that will be used in the expression.
     */
    public String getName() {
        return name;
    }

    /**
     * INTERNAL:
     * Initialize any information in the receiver that requires its descriptor.
     * Set the receiver's descriptor back reference.
     * @param aDescriptor is the owner descriptor of the receiver.
     */
    public void initialize(ClassDescriptor aDescriptor) {
        setDescriptor(aDescriptor);
    }

    /**
     * INTERNAL:
     * return whether this query key is abstract
     * @return boolean
     */
    public boolean isAbstractQueryKey() {
        return (this.getClass().equals(oracle.toplink.essentials.internal.helper.ClassConstants.QueryKey_Class));
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isCollectionQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isDirectCollectionQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isDirectQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isForeignReferenceQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isManyToManyQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isOneToManyQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isOneToOneQueryKey() {
        return false;
    }

    /**
     * INTERNAL:
     * This is a QueryKey.  return true.
     * @return boolean
     */
    public boolean isQueryKey() {
        return true;
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * PUBLIC:
     * Set the name for the query key.
     * This is the name that will be used in the expression.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * INTERNAL:
     * return a string representation of this instance of QueryKey
     */
    public String toString() {
        return oracle.toplink.essentials.internal.helper.Helper.getShortClassName(this) + "(" + getName() + ")";
    }
}
