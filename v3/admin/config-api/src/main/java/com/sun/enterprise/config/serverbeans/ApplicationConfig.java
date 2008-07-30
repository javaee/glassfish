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

import java.beans.PropertyVetoException;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Represents the {@literal <application-config>} child element of {@literal <application-ref>}.
 * <p>
 * The <code>type</code> attribute identifies the container type for which
 * the application configuration customizations apply.  The text value of the
 * <application-config> element holds CDATA-wrapped text which records the
 * application configuration customization in whatever format the container
 * engineer chooses.
 * <p>
 * As a temporary workaround, the customized configuration should be stored
 * as the <code>config</code> attribute, the value of which is encoded using
 * URLEncoder.  This workaround should be removed and the config stored as
 * the text value once an AMX bug is fixed regarding getting the text value
 * of an element and once we find out how to be able to define the
 * ApplicationRef interface so we can get both the text
 * value of the ApplicationConfig children and also the List of ApplicationConfig
 * children elements themselves. 
 * 
 * @author tjquinn
 */
@Configured
public interface ApplicationConfig extends ConfigBeanProxy, Injectable {

    /**
     * Reports the type value which holds the container type to which this
     * particular configuration customization applies.
     * 
     * @return the type for these customizations
     */
    @Attribute(required=true,key=true)
    public String getType();
    
    /**
     * Sets the type attribute value to the specified container type.
     * 
     * @param the container type for which these customizations apply
     * @throws java.beans.PropertyVetoException
     */
    public void setType(String value) throws PropertyVetoException;
    
    // XXX The following are a temporary workaround used to store the config as an attr instead of a CDATA-wrapped text value
    
    /**
     * Reports the configuration information already stored.
     * <p>
     * If, as suggested, the value was encoded before it was stored using
     * @{link setConfig} then the returned value should be decoded using
     * @{link java.net.URLDecoder} before use by the calling logic.
     * 
     * @return the config
     */
    @Attribute(required=true)
    public String getConfig();
    
    /**
     * Stores the config information.
     * <p>
     * The value stored should have already been encoded using
     * @{link java.net.URLEncoder} if it contains characters that might
     * interfere with the well-formedness of the containing domain.xml
     * XML document.
     * 
     * @param value the configuration information to be stored
     * @throws java.beans.PropertyVetoException
     */
    public void setConfig(String value) throws PropertyVetoException;
}
