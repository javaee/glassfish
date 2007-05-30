/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.component;


import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Exception thrown by the injection manager when a dependency is not satisfied when
 * performing injection.
 *
 * @author Jerome Dochez
 */
public class UnsatisfiedDepedencyException extends ComponentException {

    Field field = null;
    Method method = null;


    public UnsatisfiedDepedencyException(Field target) {
        super("Unsatisfied dependency exception : " + target);
        this.field = target;
    }
    public UnsatisfiedDepedencyException(Method target) {
        super("Unsatisfied dependency exception : " + target);
        this.method = target;
    }

    public boolean isField() {
        return field!=null;
    }

    public boolean isMethod() {
        return method!=null;
    }

    public String getUnsatisfiedName() {
        if (field!=null) {
            return field.getName();
        }
        if (method!=null) {
            return method.getName().substring(3).toLowerCase();
        }
        return "unknown";
    }

    public AnnotatedElement getUnsatisfiedElement() {
        if (field!=null) return field;
        if (method!=null) return method;
        return null;
    }
}
