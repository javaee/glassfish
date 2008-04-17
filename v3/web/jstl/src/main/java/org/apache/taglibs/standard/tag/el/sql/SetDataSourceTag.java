/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.el.sql;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.tag.common.sql.SetDataSourceTagSupport;

/**
 * <p>Tag handler for &lt;SetDataSource&gt; in JSTL, used to create
 * a simple DataSource for prototyping.</p>
 * 
 */
public class SetDataSourceTag extends SetDataSourceTagSupport {

    private String dataSourceEL;
    private String driverClassNameEL;
    private String jdbcURLEL;
    private String userNameEL;
    private String passwordEL;

    //*********************************************************************
    // Accessor methods

    public void setDataSource(String dataSourceEL) {
	this.dataSourceEL = dataSourceEL;
	this.dataSourceSpecified = true;
    }

    public void setDriver(String driverClassNameEL) {
	this.driverClassNameEL = driverClassNameEL;
    }

    public void setUrl(String jdbcURLEL) {
	this.jdbcURLEL = jdbcURLEL;
    }

    public void setUser(String userNameEL) {
	this.userNameEL = userNameEL;
    }

    public void setPassword(String passwordEL) {
	this.passwordEL = passwordEL;
    }

    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {
        evaluateExpressions();

        return super.doStartTag();
    }


    //*********************************************************************
    // Private utility methods

    // Evaluates expressions as necessary
    private void evaluateExpressions() throws JspException {
        if (dataSourceEL != null) {
                dataSource = ExpressionEvaluatorManager.evaluate
                ("dataSource", dataSourceEL, Object.class, this, pageContext);
        }

        if (driverClassNameEL != null) {
                driverClassName = (String) ExpressionEvaluatorManager.evaluate
                ("driver", driverClassNameEL, String.class, this, pageContext);
        }

        if (jdbcURLEL != null) {
                jdbcURL = (String) ExpressionEvaluatorManager.evaluate
                ("url", jdbcURLEL, String.class, this, pageContext);
        }

        if (userNameEL != null) {
                userName = (String) ExpressionEvaluatorManager.evaluate
                ("user", userNameEL, String.class, this, pageContext);
        }

        if (passwordEL != null) {
                password = (String) ExpressionEvaluatorManager.evaluate
                ("password", passwordEL, String.class, this, pageContext);
        }
    }

}
