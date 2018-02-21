/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package util;

import com.sun.enterprise.util.net.NetUtils;


/**
 *
 *
 *
 *
 */
public class Util {

	private static final String DAS_PORT;
	private static final String DAS_HOST;
	private static final String ADMIN_USER;
	private static final String ADMIN_PASSWD;
	private static final String INSTALL_TYPE;
	private static boolean IS_EE;

	static {
		DAS_HOST = System.getProperty("ADMIN_HOST", "localhost");
		DAS_PORT = System.getProperty("ADMIN_PORT", "4849");
		ADMIN_USER = System.getProperty("ADMIN_USER", "admin");
		ADMIN_PASSWD = System.getProperty("ADMIN_PASSWORD", "adminadmin");
		try {
			IS_EE = NetUtils.isSecurePort(DAS_HOST, Integer.parseInt(DAS_PORT));
		} catch(Exception ex) {
			//default make it PE. Squelch it, and let the connection handle this
			IS_EE=false;
		}

		if (IS_EE)
		    INSTALL_TYPE = "ee";
		else
		    INSTALL_TYPE = "pe";

	}

    public static String getAdminHost() {
        return DAS_HOST;
    }

    public static String getAdminPort() {
        return DAS_PORT;
    }

    public static String getAdminUser() {
        return  ADMIN_USER;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWD;
    }

    public static String getInstallType() {
        return INSTALL_TYPE;
    }


}
