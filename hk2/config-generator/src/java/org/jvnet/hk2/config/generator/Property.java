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
package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.TypeMirror;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Dom;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents configurable property of the component.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class Property {
    /**
     * Field/method declaration of this property.
     * Used to read annotations on this property.
     */
    abstract MemberDeclaration decl();

    /**
     * Name used as a seed for the XML name of this property.
     * This is the property name / field name.
     */
    String seedName() {
        return decl().getSimpleName();
    };

    /**
     * The type of the property.
     */
    abstract TypeMirror type();

    abstract void assign(JVar $target, JBlock block, JExpression rhs);

    <A extends Annotation> A getAnnotation(Class<A> a) {
        return decl().getAnnotation(a);
    }

    private Boolean isKey=null;
    
    /**
     * Does this property have {@link Attribute#key()} or {@link Element#key()}.
     */
    final boolean isKey() {
        if(isKey==null)
            isKey = _isKey();
        return isKey;
    }

    private boolean _isKey() {
        Element e = getAnnotation(Element.class);
        if(e!=null && e.key())  return true;

        Attribute a = getAnnotation(Attribute.class);
        if(a!=null && a.key())  return true;

        return false;
    }

    String inferName(String name) {
        if(name.length()==0)
            name = Dom.convertName(seedName());
        return name;
    }

    /**
     * Property that consists of a set/add/get method.
     */
    static final class Method extends Property {
        final MethodDeclaration decl;
        /**
         * True if this property is based on the getter method. False if the setter/adder.
         */
        final boolean getter;

        public Method(MethodDeclaration decl) {
            this.decl = decl;
            getter = !decl.getReturnType().toString().equals("void");
        }

        MemberDeclaration decl() {
            return decl;
        }

        TypeMirror type() {
            if(getter)
                return decl.getReturnType();
            else
                return decl.getParameters().iterator().next().getType();
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.invoke($target,decl.getSimpleName()).arg(rhs);
        }
    }

    /**
     * Property that consists of a field.
     */
    static final class Field extends Property {
        final FieldDeclaration decl;

        public Field(FieldDeclaration decl) {
            this.decl = decl;
        }

        MemberDeclaration decl() {
            return decl;
        }

        TypeMirror type() {
            return decl.getType();
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.assign($target.ref(decl.getSimpleName()),rhs);
        }
    }
}
