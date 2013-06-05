/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * An Injectee represents the point of injection.  It can be used
 * by injection resolvers to discover all of the information available about
 * the entity being injected into.
 * 
 * @author jwells
 */
public interface Injectee {
    /**
     * This is the required type of the injectee.  The object
     * that is injected into this point must be type-safe
     * with regards to this type
     * 
     * @return The type that this injectee is expecting.  Any object
     * injected into this injection point must be type-safe with
     * regards to this type
     */
    public Type getRequiredType();
    
    /**
     * This is the set of required qualifiers for this injectee.  All
     * of these qualifiers must be present on the implementation class
     * of the object that is injected into this injectee.  Note that the
     * fields of the annotation must also match
     * 
     * @return Will not return null, but may return an empty set.  The
     * set of all qualifiers that must match.
     */
    public Set<Annotation> getRequiredQualifiers();
    
    /**
     * If this Injectee is a constructor or method parameter, this will
     * return the index of the parameter.  If this Injectee is a field,
     * this will return -1
     * 
     * @return the position of the parameter, or -1 if this is a field
     */
    public int getPosition();
    
    /**
     * Returns the parent class for this injectee.  This is the
     * class of the object that will be injected into.  This
     * field may return null if this is from a lookup
     * with {@link ServiceLocator#getInjecteeDescriptor(Injectee)}.
     * 
     * @return The class of the object that will be injected into
     */
    public Class<?> getInjecteeClass();
    
    /**
     * If this Injectee is in a constructor this will return the 
     * constructor being injected into.  If this Injectee is in a
     * method this will return the method being injected into.  If this
     * injectee represents a field, this will return the field being
     * injected into.  This injectee may be neither (it may be
     * the result of a lookup with {@link ServiceLocator#getInjecteeDescriptor(Injectee)})
     * in which case this will return null.
     * 
     * @return The parent of the injectee, which may be null
     */
    public AnnotatedElement getParent();
    
    /**
     * This method returns true if this injection point is annotated
     * with &#86;Optional.  In this case if there is no definition for
     * the injection point in the system it is allowable for the system
     * to merely return null
     * 
     * @return true if the injection point is annotated with &#86;Optional,
     * false otherwise
     */
    public boolean isOptional();
    
    /**
     * This method returns true if this injection point is annotated
     * with &#86;Self.  In this case the required type must have
     * a raw class of {@link ActiveDescriptor}, isOptional must be
     * false and the set of required qualifiers must be empty
     * 
     * @return true if the injection point is annotated with &#86;Self,
     * false otherwise
     */
    public boolean isSelf();
    
    /**
     * This method returns the {@link Unqualified} annotation if it is present
     * on the injection point.  This can be used to determine what qualifiers
     * should NOT be given to the injection point.
     * 
     * @return The {@link Unqualified} annotation on the injection point, or null
     * if there is no {@link Unqualified} annotation on the injection point
     */
    public Unqualified getUnqualified();
    
    /**
     * This returns the ActiveDescriptor of the injectee
     * if it is known
     * 
     * @return The ActiveDescriptor of the injectee,
     * or null if the parent is not known
     */
    public ActiveDescriptor<?> getInjecteeDescriptor();
}
