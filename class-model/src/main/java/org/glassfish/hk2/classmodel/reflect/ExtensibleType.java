/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.hk2.classmodel.reflect;

import java.util.Collection;

/**
 * An extensible type is a type that can be subclassed like an interface
 * or a class.
 *
 * @param <T> parent type which is always the same as the child type
 * (classes extends classes, interfaces extends interfaces...)
 *  
 * @author Jerome Dochez
 */
public interface ExtensibleType<T extends ExtensibleType> extends Type {

    /**
     * Return the parent type instance. If there are more than one parent
     * with the same FQCN within the various URI we parsed, we return the
     * one defined within the same URI (if it exists). If there is more
     * than one parsed metadata with the same FQCN and none of them are
     * defined within the same URI as this type, then null is returned.
     *
     * @return the parent type instance or null
     */
    T getParent();

    /**
     * Returns the child subtypes of this type. A child subtype is a
     * type which parent is this type.
     *
     * @return the immediate subtypes
     */
    Collection<T> subTypes();

    /**
     * Returns all the children subtypes (including grand children) of
     * this type. 
     *
     * @return all the children
     */
    Collection<T> allSubTypes();

    /**
     * Returns an unmodifiable list of interfaces implemented or extended by
     * this type.
     *
     * @return collection of implemented or extended interfaces
     */
    Collection<InterfaceModel> getInterfaces();

    Collection<ParameterizedInterfaceModel> getParameterizedInterfaces();

    /**
     * Returns an unmodifiable list of static fields defined by this type
     *
     * @reutrn collection of defined static fields
     */
    Collection<FieldModel> getStaticFields();
}
