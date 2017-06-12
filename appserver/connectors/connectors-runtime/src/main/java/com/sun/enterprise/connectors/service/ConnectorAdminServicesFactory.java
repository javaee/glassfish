/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;

/**
 * This is a factory class for the connector admin services classes.
 *
 * @author Srikanth P
 */
public class ConnectorAdminServicesFactory {

    /**
     * Returns a specific connector admin service implemntation class based
     * on the type of service.
     *
     * @param type Service type.
     * @return COnnectorAdminService implementation class
     */
    public static ConnectorService getService(String type) {

        if (type == null) {
            return null;
        }

        if (type.equals(ConnectorConstants.CCP)) {
            return new ConnectorConnectionPoolAdminServiceImpl();
        } else if (type.equals(ConnectorConstants.CR)) {
            return new ConnectorResourceAdminServiceImpl();
        } else if (type.equals(ConnectorConstants.RA)) {
            return new ResourceAdapterAdminServiceImpl();
        } else if (type.equals(ConnectorConstants.SEC)) {
            return new ConnectorSecurityAdminServiceImpl();
        } else if (type.equals(ConnectorConstants.AOR)) {
            return new ConnectorAdminObjectAdminServiceImpl();
        } else {
            return null;
        }
    }
}
