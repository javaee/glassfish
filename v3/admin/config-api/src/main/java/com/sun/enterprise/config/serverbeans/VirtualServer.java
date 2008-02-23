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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;


import org.glassfish.admin.amx.loader.AMXConfigInfo;

/* @XmlType(name = "", propOrder = {
    "httpAccessLog",
    "property"
}) */
@AMXConfigInfo( amxInterface=com.sun.appserv.management.config.VirtualServerConfig.class, nameHint="id" )
@Configured
public interface VirtualServer extends ConfigBeanProxy, Injectable  {

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getId();

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpListeners property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getHttpListeners();

    /**
     * Sets the value of the httpListeners property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHttpListeners(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultWebModule property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDefaultWebModule();

    /**
     * Sets the value of the defaultWebModule property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultWebModule(String value) throws PropertyVetoException;

    /**
     * Gets the value of the hosts property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getHosts();

    /**
     * Sets the value of the hosts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHosts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the state property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getState();

    /**
     * Sets the value of the state property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setState(String value) throws PropertyVetoException;

    /**
     * Gets the value of the docroot property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDocroot();

    /**
     * Sets the value of the docroot property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDocroot(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logFile property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getLogFile();

    /**
     * Sets the value of the logFile property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogFile(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpAccessLog property.
     *
     * @return possible object is
     *         {@link HttpAccessLog }
     */
    @Element
    public HttpAccessLog getHttpAccessLog();

    /**
     * Sets the value of the httpAccessLog property.
     *
     * @param value allowed object is
     *              {@link HttpAccessLog }
     */
    public void setHttpAccessLog(HttpAccessLog value) throws PropertyVetoException;

    /**
     * Gets the value of the property property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     */
    @Element("property")
    public List<Property> getProperty();



}
