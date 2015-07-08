/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.api.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes the string that constitutes the plural of this element or attribute name.
 * Can also be used to supply the exact method name for the adder, remover
 * and locator methods for this element.  This annotation will ONLY be read from
 * the method that also has an {@link javax.xml.bind.annotation.XmlElement} annotation
 * on it
 * <p>
 * The rules for determining the singular form of the element name is the following:<OL>
 * <LI>Remove the get or set from the method name.  The remainder is the element name</LI>
 * <LI>decapitalize the element name</LI>
 * <LI>If the element name has more than one letter and ends in &quot;s&quot;, remove the &quot;s&quot;</LI>
 * <LI>The remainder is the singular of the element name</LI>
 * </LI>
 * <p>
 * For example, if the method name is getDoctors, the singular element name will be &quot;doctor&quot;.
 * In that case the adder method will be addDoctor, the remover method will be removeDoctor and
 * the lookup method will be lookupDoctor.  In some cases the singular of a word is the same as the
 * plural.  If that word does not end in s the default behavior works fine.  For example if the
 * method name is getMoose, the singular element name will be &quot;moose&quot;.  The adder method
 * will be addMoose and so on.
 * <p>
 * In cases that do not conform to the above rule this annotation is provided, which allows the
 * user to specify what this element name is the plural of.  For example, if the method
 * name is getMice then this annotation should be used:
 * <code>
 * &#86;PluralOf("mouse")
 * </code>
 * In this case the adder method will become addMouse, the remover will be removeMouse and the
 * lookup will be lookupMouse.
 * <p>
 * This annotation can also be used to specify the exact method name that should be used for
 * the adder, remover and lookup.  If those fields are filled in the will override the algorithm
 * for determining the singular for this element name.
 * 
 * @author jwells
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface PluralOf {
    /**
     * Returns the singular of the element name described by this
     * setter or getter method.  This name should be fully
     * capitalized as it should appear after the &quot;add&quot;,
     * &quot;remove&quot; or &quot;lookup&quot; in the method
     * name.
     * <p>
     * For example, if this is returning the singular for
     * mice, it should return &quot;Mouse&quot;.
     * 
     * @return The singular of the element name, or
     * {@link PluralOf#USE_NORMAL_PLURAL_PATTERN} if the
     * normal algorithm should be applied
     */
    public String value() default USE_NORMAL_PLURAL_PATTERN;
    
    /**
     * Returns the exact name of the method that should be
     * used as the adder for this element
     * 
     * @return The exact name of the method that should be
     * used as the adder for this element, or
     * {@link PluralOf#USE_NORMAL_PLURAL_PATTERN} if
     * the normal algorithm should be used (as modified
     * by the {@link PluralOf#value()} method)
     */
    public String add() default USE_NORMAL_PLURAL_PATTERN;
    
    /**
     * Returns the exact name of the method that should be
     * used as the remover for this element
     * 
     * @return The exact name of the method that should be
     * used as the remover for this element, or
     * {@link PluralOf#USE_NORMAL_PLURAL_PATTERN} if
     * the normal algorithm should be used (as modified
     * by the {@link PluralOf#value()} method)
     */
    public String remove() default USE_NORMAL_PLURAL_PATTERN;
    
    /**
     * Returns the exact name of the method that should be
     * used as the lookkup for this element
     * 
     * @return The exact name of the method that should be
     * used as the lookup for this element, or
     * {@link PluralOf#USE_NORMAL_PLURAL_PATTERN} if
     * the normal algorithm should be used (as modified
     * by the {@link PluralOf#value()} method)
     */
    public String lookup() default USE_NORMAL_PLURAL_PATTERN;
    
    /**
     * This value is used to indicate that the normal
     * algorithm should be used for determining the
     * singular of the element name
     */
    public final static String USE_NORMAL_PLURAL_PATTERN = "*";

}
