/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.session;

import org.apache.catalina.core.SessionCookieConfigImpl;
import org.apache.catalina.core.StandardContext;

/**
 * This class extends SessionCookieConfigImpl to handle additional
 * secure cookie functionality from glassfish-web.xml
 *
 * @author  Shing Wai Chan
 */
public final class WebSessionCookieConfig extends SessionCookieConfigImpl {

    // the following enum match cookieSecure property value in glassfish-web.xml
    public enum CookieSecureType {
        TRUE, FALSE, DYNAMIC
    };

    // default web.xml(secure=false) = glassfish-web.xml(cookieSecure=dynamic)
    private CookieSecureType secureCookieType = CookieSecureType.DYNAMIC;

    public WebSessionCookieConfig(StandardContext context) {
        super(context);
    }

    @Override
    public void setSecure(boolean secure) {
        super.setSecure(secure);

        secureCookieType = ((secure)? CookieSecureType.TRUE : CookieSecureType.DYNAMIC);
    }

    public void setSecure(String secure) {
        boolean isTrue = Boolean.parseBoolean(secure);

        super.setSecure(isTrue);

        if (isTrue) {
            secureCookieType = CookieSecureType.TRUE;
        } else if ("false".equalsIgnoreCase(secure)) {
            secureCookieType = CookieSecureType.FALSE;
        } else {
            secureCookieType = CookieSecureType.DYNAMIC;
        }
    }

    public CookieSecureType getSecure() {
        return secureCookieType;
    }
}
