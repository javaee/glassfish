/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.deployment.admin;

import java.io.File;
import org.glassfish.api.Param;
import org.glassfish.api.deployment.DeployCommandParameters;

/**
 * Parameters for the remote instance deploy command, beyond the ones already
 * defined for the DAS DeployCommand.
 * 
 * @author Tim Quinn
 */
public class InstanceDeployCommandParameters extends DeployCommandParameters {

    @Param(name=ParameterNames.GENERATED_EJB_DIRECTORY)
    public File generatedejbdir = null;

    @Param(name=ParameterNames.GENERATED_JSP_DIRECTORY)
    public File generatedjspdir = null;

    @Param(name=ParameterNames.GENERATED_POLICY_DIRECTORY, optional=true)
    public File generatedpolicydir = null;

    @Param(name=ParameterNames.GENERATED_XML_DIRECTORY)
    public File generatedxmldir = null;

    public static class ParameterNames {
        public static final String GENERATED_EJB_DIRECTORY = "generatedejbdir";
        public static final String GENERATED_JSP_DIRECTORY = "generatedjspdir";
        public static final String GENERATED_POLICY_DIRECTORY = "generatedpolicydir";
        public static final String GENERATED_XML_DIRECTORY = "generatedxmldir";

    }


}
