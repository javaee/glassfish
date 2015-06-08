/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.api;

import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
public interface XmlHk2ConfigurationBean {
    /**
     * Returns a read-only copy of the
     * bean-like map corresponding to the current
     * state of this bean
     * 
     * @return A copy of the bean-like map associated
     * with this bean
     */
    public Map<String, Object> _getBeanLikeMap();
    
    /**
     * Returns the parent of this bean, or null if this
     * object is the root of the true
     * 
     * @return The parent of this object, or null if this
     * is the root of the tree
     */
    public Object _getParent();
    
    /**
     * Returns the XmlPath for this object
     * 
     * @return The XmlPath for this object
     */
    public String _getXmlPath();
    
    /**
     * Returns the instance path/name for this object
     * 
     * @return The instance path/name for this object
     */
    public String _getInstanceName();
    
    /**
     * Returns the name of the property that
     * returns the key for this bean, or
     * null if this bean does not have a key
     * property
     * 
     * @return The name of the key property for
     * this bean or null if this bean doesn
     * not have a key property
     */
    public String _getKeyPropertyName();
    
    /**
     * Returns the key value for this object
     * 
     * @return The instance path/name for this object
     */
    public String _getKeyValue();
    
    /**
     * Returns the type of this bean.<UL>
     * <LI>{@link XmlHk2BeanType#NORMAL} - A bean created via add or by Xml parsing</LI>
     * <LI>{@link XmlHk2BeanType#DEFAULT_CHILD} - A pseudo bean here due to a
     * {@link org.glassfish.hk2.xml.api.annotation.DefaultChild} annotation in the parent</LI>
     * </UL>
     * 
     * @return the type of this bean
     */
    public XmlHk2BeanType _getBeanType();
    
    /**
     * If this bean has {@link XmlHk2BeanType#DEFAULT_CHILD} type then this method
     * will change the type to {@link XmlHk2BeanType#NORMAL}.  No fields of the
     * bean will be changed and there will be no transaction in the configuration Hub
     * if the bean is registered in the hub
     */
    public void _normalizeBean();
}
