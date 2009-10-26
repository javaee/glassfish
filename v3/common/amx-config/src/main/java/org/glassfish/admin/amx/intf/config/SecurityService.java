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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.base.Singleton;

import java.util.Map;

/**
Configuration for the &lt;security-service&gt; element.
 */
public interface SecurityService extends
        PropertiesAccess, ConfigElement, Singleton
{
    public String getAnonymousRole();
    public void setAnonymousRole(String val);
    
    public String getAuditEnabled();

    public void setAuditEnabled(String value);

    public String getAuditModules();

    public void setAuditModules(String value);

    public String getDefaultPrincipalPassword();

    public void setDefaultPrincipalPassword(String value);

    public String getDefaultPrincipal();

    public void setDefaultPrincipal(String value);

    public String getDefaultRealm();

    public void setDefaultRealm(String value);

    public String getJacc();

    public void setJacc(String value);

    /**                                               
    This attribute is used to customize the
    java.security.Principal implementation class used in the
    default principal to role mapping. This attribute is
    optional. When it is not specified,
    com.sun.enterprise.deployment.Group implementation of
    java.security.Principal is used. The value of this attribute
    is only relevant when the activate-default
    principal-to-role-mapping attribute is set to true.
    @since AppServer 9.0
     */
    public String getMappedPrincipalClass();

    /**
    @see #getMappedPrincipalClass
    @since AppServer 9.0
     */
    public void setMappedPrincipalClass(String theClass);

    /**                                                     
    Causes the appserver to apply a default principal to role
    mapping, to any application that does not have an application
    specific mapping defined. Every role is mapped to a
    same-named (as the role) instance of a
    java.security.Principal implementation class (see
    mapped-principal-class). This behavior is similar to that of
    Tomcat servlet container. It is off by default.
    @since AppServer 9.0
     */
    public String getActivateDefaultPrincipalToRoleMapping();

    /**
    @see #getActivateDefaultPrincipalToRoleMapping
    @since AppServer 9.0
     */
    public void setActivateDefaultPrincipalToRoleMapping(String enabled);

    public Map<String, JaccProvider> getJaccProvider();

    public Map<String, AuthRealm> getAuthRealm();

    public Map<String, AuditModule> getAuditModule();

    public Map<String, MessageSecurityConfig> getMessageSecurityConfig();
}




