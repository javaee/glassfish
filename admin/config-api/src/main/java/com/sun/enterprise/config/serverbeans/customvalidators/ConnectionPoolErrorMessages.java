/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

/**
 * Enum containing the different custom validation failures for a connection 
 * pool. The message strings for the different validation scenarios are 
 * used for interpolation and display when there is a failure in 
 * custom validation.
 * 
 * @author Shalini M
 */
public enum ConnectionPoolErrorMessages {
    
    MAX_STEADY_INVALID ("Max-pool-size has to be greater than or equal to steady-pool-size"),
    
    STMT_WRAPPING_DISABLED ("Statement Wrapping should be set to true before performing this operation"),
    
    RES_TYPE_MANDATORY ("Must specify either datasource-classname or driver-classname. " +
            "datasource-classname is mandatory when res-type is " +
                    "javax.sql.DataSource or javax.sql.ConnectionPoolDataSource or " +
                            "javax.sql.XADataSource. driver-classname is mandatory " +
                                    "when res-type is java.sql.Driver."),

    TABLE_NAME_MANDATORY ("Default validation method is 'table' when validation is enabled. Please specify a valid" +
            "table name ['validation-table-name'] before enabling 'table' based validation."),
    
    CUSTOM_VALIDATION_CLASS_NAME_MANDATORY("Must specify 'validation-class-name' when 'connection-validation' " +
            "is enabled and 'validation-method' is 'custom-validation'. Please specify 'validation-class-name' " +
            "before enabling 'custom-validation' based validation");

    private ConnectionPoolErrorMessages(String message) {
        this.message = message;
    }
    private final String message;    
    
    public String toString() {
        return message;
    }
}
