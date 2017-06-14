/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import org.jvnet.hk2.annotations.Contract;

/**
 * <p>
 * This is an admin command interface, command implementations have to be
 * stateless and should also have a {@link org.jvnet.hk2.component.Scope}
 * value of {@link org.glassfish.hk2.api.PerLookup}
 * </p>
 *
 * Command implementations should use the {@link org.glassfish.api.Param}
 * annotation to annotate the command parameters.
 *
 * Command implementations are normal services and are therefore following
 * the normal hk2 service lifecycle and injection features.
 * 
 * <p>
 * Internationalization can be provided by using the {@link org.glassfish.api.I18n}}
 * annotation. Each parameter declaration can also be annotated with an
 * {@link org.glassfish.api.I18n} annotation to point to the parameter .
 * </p>
 *
 * By default, if an {@link org.glassfish.api.I18n} is used to annotate implementations,
 * the value of the annotation will be used as follow to lookup strings in the module's
 * local strings properties files.
 *
 *  key             provide a short description of the command role and expected output
 *  key.usagetext   [optional] if not provided, usage text will be calculated based on
 *                  parameters declaration
 *  key.paramName   [optional] provide a description for the parameter "paramName", it
 *                  can be overriden by annotating the @Param annotated field/method with
 *                  a {@link org.glassfish.api.I18n}
 *
 * @author Jerome Dochez
 */
@Contract
public interface AdminCommand {       
    
    /**
     * Executes the command with the command parameters passed as Properties 
     * where the keys are the parameter names and the values are the parameter values
     * @param context information 
     */
    public void execute(AdminCommandContext context);
    
}
