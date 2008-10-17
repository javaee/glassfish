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

/**
 * Defines {@link Transport}'s events handling logic.
 */
@AMXConfigInfo(amxInterfaceName="org.glassfish.admin.amx.config.grizzly.SelectionKeyHandlerConfig")
@Configured
public interface SelectionKeyHandler extends ConfigBeanProxy, PropertyBag, Injectable {

    /**
     * Get the name, which could be used as reference
     *
     * @return the name, which could be used as reference
     */
    @Attribute(required = true, key = true)
    public String getName();

    /**
     * Set the {@link SelectionKeyHandler} name, which could be used
     * as reference
     *
     * @param name the name, which could be used as reference
     */
    public void setName(String name);

    /**
     * Get the {@link SelectionKeyHandler} implementation class name
     *
     * @return the {@link SelectionKeyHandler} implementation class name
     */
    @Attribute
    public String getClassname();

    /**
     * Set the implementation class name
     */
    public void setClassname(String classname);

    /**
     * Get the timeout, in seconds, after which idle key
     * will be cancelled and channel closed
     *
     * @return the timeout, in seconds, after which idle key
     * will be cancelled and channel closed
     */
    @Attribute(defaultValue = "120")
    public String getIdleKeyTimeoutSeconds();

    /**
     * Set the timeout, in seconds, after which idle key
     * will be cancelled and channel closed
     *
     * @param idleKeyTimeout the timeout, in seconds, after which idle key
     * will be cancelled and channel closed
     */
    public void setIdleKeyTimeoutSeconds(String idleKeyTimeout);
}








