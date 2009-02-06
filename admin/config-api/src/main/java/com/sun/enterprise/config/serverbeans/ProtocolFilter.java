/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.amx.AMXConfigInfo;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import java.util.List;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;

import org.glassfish.quality.ToDo;

/**
 * defines single {@link ProtocolFilter} in a processing chain.
 */
@AMXConfigInfo(amxInterfaceName="org.glassfish.admin.amx.config.grizzly.ProtocolFilterConfig")
@Configured
public interface ProtocolFilter extends ConfigBeanProxy, PropertyBag, Injectable {
    /**
     * Get the {@link ProtocolFilter} name, which could be used
     * as reference
     *
     * @return the {@link ProtocolFilter} name, which could be used
     * as reference
     */
    @Attribute(required = true, key = true)
    public String getName();

    /**
     * Set the {@link ProtocolFilter} name, which could be used
     * as reference
     *
     * @param name the {@link ProtocolFilter} name, which could be used
     * as reference
     */
    public void setName(String name);
    
    /**
     * Gets the class name of the {@link ProtocolFilter} implementation
     *
     * @return the class name of the {@link ProtocolFilter} implementation
     */
    @Attribute(required=true)
    public String getClassname();

    /**
     * Sets the class name of the {@link ProtocolFilter} implementation
     *
     * @param classname the class name of the {@link ProtocolFilter} implementation
     */
    public void setClassname(String classname);
}











