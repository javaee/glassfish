/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
        this(target,null);
    }
    public UnsatisfiedDepedencyException(Field target,Throwable cause) {
        super("Unsatisfied dependency exception : " + target,cause);
        this.field = target;
    }
    public UnsatisfiedDepedencyException(Method target) {
        this(target,null);
    }
    public UnsatisfiedDepedencyException(Method target,Throwable cause) {
        super("Unsatisfied dependency exception : " + target,cause);
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
