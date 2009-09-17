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
package org.glassfish.api.embedded.admin;

import java.util.Properties;

/**
 * Command Paramters, needs to be refined...
 *
 * @author Jerome Dochez
 */
public class CommandParameters {

    // todo : change Properties to Map<String, String>
    private Properties params = new Properties();
    private String operand;

    /**
     * Sets the command primary (operand) parameter.
     * @param operand the command operand
     */
    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getOperand() {
        return operand;
    }

    /**
     * Sets a command option as the user would specify it using the
     * CLI command for instance
     *
     * @param optionName option name (without leading -- chars)
     * @param optionValue option value
     */
    public void setOption(String optionName, String optionValue) {
        params.put(optionName, optionValue);
    }

    public Properties getOptions() {
        return new Properties(params);
    }
}


