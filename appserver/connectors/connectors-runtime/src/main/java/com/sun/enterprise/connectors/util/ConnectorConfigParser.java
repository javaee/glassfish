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

package com.sun.enterprise.connectors.util;

import java.util.*;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.*;
import com.sun.enterprise.deployment.*;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;

/**
 * Interface class of connector configuration parser methods.
 * Implemented by specific configuration parser classes.
 *
 * @author Srikanth P
 */
public interface ConnectorConfigParser {

    /**
     * Admin object type.
     */
    String AOR = ConnectorConstants.AO;

    /**
     * Managed connection factory type.
     */
    String MCF = ConnectorConstants.MCF;

    /**
     * Resource adapter type.
     */
    String RA = ConnectorConstants.RAR;

    /**
     * Message listener type.
     */
    String MSL = ConnectorConstants.MSL;

    /**
     * Instances of util classes. Used as composition pattern.
     */
    ConnectorDDTransformUtils ddTransformUtil = new ConnectorDDTransformUtils();
    ConnectorConfigParserUtils configParserUtil =
            new ConnectorConfigParserUtils();

    /**
     * Obtains the merged javabean properties (properties present in ra.xml
     * and introspected properties) of a specific configuration.
     *
     * @param desc              ConnectorDescriptor pertaining to rar .
     * @param connectionDefName Connection definition name or
     *                          admin object interface .
     * @return Merged properties.
     */
    Properties getJavaBeanProps(ConnectorDescriptor desc,
                                String connectionDefName, String rarName) throws ConnectorRuntimeException;

    /**
     * Gi
     * @param desc
     * @param rarName
     * @param keyFields
     * @return
     * @throws ConnectorRuntimeException
     */
    List<String> getConfidentialProperties(ConnectorDescriptor desc, String rarName,
                                           String... keyFields) throws ConnectorRuntimeException ;
}
