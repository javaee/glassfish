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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Implementation for resource type validation.
 * Validation of datasource/driver classnames when resource type is not null 
 * is done. 
 * When resourcetype is null one of datasource/driver classname should be 
 * provided. 
 * 
 * @author Shalini M
 */
public class ResTypeConstraintValidator 
        implements ConstraintValidator<ResTypeConstraint, JdbcConnectionPool> {

    @Override
    public void initialize(ResTypeConstraint arg0) {
    }

    /**
     * Checks if the classname is valid based on the resource type. The 
     * datasource classname should not be null when resType is 
     * javax.sql.DataSource/CPDS/XADS. Driver classname should not be null when
     * resType is java.sql.Driver.
     * @param className
     * @param context
     * @return
     */
    @Override
    public boolean isValid(JdbcConnectionPool jdbcPool, ConstraintValidatorContext context) {
        String resType = jdbcPool.getResType();
        String dsClassName = jdbcPool.getDatasourceClassname();
        String driverClassName = jdbcPool.getDriverClassname();
        if(resType == null) {
            //One of datasource/driver classnames must be provided.
            if ((dsClassName == null || dsClassName.equals("")) && 
                    (driverClassName == null || driverClassName.equals(""))) {
                return false;
            } else {
                //Check if both are provided and if so, return false
                if(dsClassName != null && driverClassName != null) {
                    return false;
                }
            }
        } else if (resType.equals("javax.sql.DataSource") ||
                resType.equals("javax.sql.ConnectionPoolDataSource") ||
                resType.equals("javax.sql.XADataSource")) {
            //Then datasourceclassname cannot be empty
            if (dsClassName == null || dsClassName.equals("")) {
                return false;
            }
        } else if (resType.equals("java.sql.Driver")) {
            //Then driver classname cannot be empty
            if (driverClassName == null || driverClassName.equals("")) {
                return false;
            }
        }
        return true;
    }
}
