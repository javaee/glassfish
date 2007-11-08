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

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;

/**
 * <p>
 * <b>Purpose</b>: Define an alias to a foreign object.
 * <p>
 * <b> Responsibilities</b>:
 * <ul>
 * <li> Define the reference class of the foreign object.
 * </ul>
 */
public class ForeignReferenceQueryKey extends QueryKey {
    protected Class referenceClass;
    protected String referenceClassName;
    protected Expression joinCriteria;

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this project to actual class-based
     * settings
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Class referenceClass = null;
        try{
            if (referenceClassName != null){
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        referenceClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(referenceClassName, true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(referenceClassName, exception.getException());
                    }
                } else {
                    referenceClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(referenceClassName, true, classLoader);
                }
            }
            setReferenceClass(referenceClass);
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(referenceClassName, exc);
        }
    }

    /**
     * PUBLIC:
     * Return the join expression for the relationship defined by the query key.
     */
    public Expression getJoinCriteria() {
        return joinCriteria;
    }

    /**
     * PUBLIC:
     * Return the reference class of the relationship.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }
    
    /**
     * PUBLIC:
     * Return the reference class name of the relationship.
     */
    public String getReferenceClassName() {
        if (referenceClassName == null && referenceClass != null){
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * override the isForeignReferenceQueryKey() method in the superclass to return true.
     * @return boolean
     */
    public boolean isForeignReferenceQueryKey() {
        return true;
    }

    /**
     * PUBLIC:
     * Set the join expression for the relationship defined by the query key.
     * <p>Example:
     * <pre><blockquote>
     *     builder.getField("ADDRESS.ADDRESS_ID").equal(builder.getParameter("EMPLOYEE.ADDR_ID");
     * </blockquote></pre>
     */
    public void setJoinCriteria(Expression joinCriteria) {
        this.joinCriteria = joinCriteria;
    }

    /**
     * PUBLIC:
     * Set the reference class of the relationship.
     * This is not required for direct collection query keys.
     */
    public void setReferenceClass(Class referenceClass) {
        this.referenceClass = referenceClass;
    }
    
    /**
     * PUBLIC:
     * Set the reference class name for this relationship
     * This is used when projects are built without using classes
     * @param referenceClassName 
     */
    public void setReferenceClassName(String referenceClassName) {
        this.referenceClassName = referenceClassName;
    }
}
