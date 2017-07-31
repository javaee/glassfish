/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.security.services.common;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import org.glassfish.hk2.api.Metadata;

/**
 * Qualifier used to enable a security check at the point of service injection or lookup.
 * Security Services, which are to protected against unqualified injection/look-up, should be annotated as something below
 *   e.g.,   @Secure(accessPermissionName = "security/service/<service-type>/<some-specific-name>")
 *   
 * Any caller which injects or looks up the protected security service, the caller's jar/class should be granted the following policy
 *   e.g.,  
 *     grant codeBase "file:<path>/<to>/<caller-jar>" {
 *         permission org.glassfish.security.services.common.SecureServiceAccessPermission "security/service/<service-type>/<some-specific-name>";
 *     };
 *
 */
@Retention(RUNTIME)
@Qualifier
@Inherited
@Target({ TYPE })
public @interface Secure {
	
	public static final String NAME = "accessPermissionName";
	
	public static final String PERMISSION_NAME_PREFIX = "security/service/";
	
    public static final String DEFAULT_PERM_NAME = PERMISSION_NAME_PREFIX + "default";

	/**
	 * the permission name to be protected
	 * if the accessPermissionName is not specified, a default value of "security/service/default" is used.
	 * @return name of the protected HK2 service
	 */	
	@Metadata(NAME)
	public String accessPermissionName();

	
}
