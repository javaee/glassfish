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

/**
 * This can be placed on XmlElements that represent
 * children properties of a bean.  If there is no child in
 * the unmarshalled XML corresponding to this XmlElement
 * then this annotation indicates that a single default
 * child should be added with values set either via
 * defaulting on the child bean or via the value property
 * of this annotation.  In particular required but not
 * defaulted properties must be set via the value field
 * of this annotation
 * <p>
 * When getting a read-only copy of the tree (see
 * {@link org.glassfish.hk2.xml.api.XmlRootHandle#getReadOnlyRoot(boolean)})
 * where defaults are not represented this default bean will NOT be returned.
 * If any value in this bean is modified then this will no longer be
 * considered the default child bean and hence will show up
 * when getting the read-only version of this bean
 * <p>
 * Cycles of Default children bean are not supported and will cause the system
 * to fail at runtime
 * 
 * @author jwells
 *
 */
public @interface DefaultChild {
    /**
     * Each string in this array must be of
     * the form &quot;name[=value]&quot;
     * where name is the xml name of the non-child element
     * to set and value is the string representation (that will
     * be transformed if necessary) of the value.  Null
     * can be represented by not having an =.  These values
     * will be explicitly set on the default child (as
     * opposed to becoming the default value of the
     * attribute or element)
     * 
     * @return Strings of form &quot;name[=value]&quot;
     * that will be set in the Default child if one
     * is created
     */
    public String[] value() default {};
}
