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

package org.apache.taglibs.standard.tag.common.sql;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>Tag handler for &lt;Driver&gt; in JSTL, used to create
 * a simple DataSource for prototyping.</p>
 * 
 * @author Hans Bergsten
 */
public class DriverTag extends TagSupport {
    private static final String DRIVER_CLASS_NAME =
	"javax.servlet.jsp.jstl.sql.driver";
    private static final String JDBC_URL =
	"javax.servlet.jsp.jstl.sql.jdbcURL";
    private static final String USER_NAME =
	"javax.servlet.jsp.jstl.sql.userName";
    private static final String PASSWORD =
	"javax.servlet.jsp.jstl.sql.password";

    private String driverClassName;
    private String jdbcURL;
    private int scope = PageContext.PAGE_SCOPE;
    private String userName;
    private String var;

    //*********************************************************************
    // Accessor methods

    public void setDriver(String driverClassName) {
	this.driverClassName = driverClassName;
    }

    public void setJdbcURL(String jdbcURL) {
	this.jdbcURL = jdbcURL;
    }

    /**
     * Setter method for the scope of the variable to hold the
     * result.
     *
     */
    public void setScope(String scopeName) {
        if ("page".equals(scopeName)) {
            scope = PageContext.PAGE_SCOPE;
        }
        else if ("request".equals(scopeName)) {
            scope = PageContext.REQUEST_SCOPE;
        }
        else if ("session".equals(scopeName)) {
            scope = PageContext.SESSION_SCOPE;
        }
        else if ("application".equals(scopeName)) {
            scope = PageContext.APPLICATION_SCOPE;
        }
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    public void setVar(String var) {
	this.var = var;
    }

    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {
	DataSourceWrapper ds = new DataSourceWrapper();
	try {
	ds.setDriverClassName(getDriverClassName());
	}
	catch (Exception e) {
	    throw new JspTagException("Invalid driver class name: " +
		e.toString(), e);
	}
	ds.setJdbcURL(getJdbcURL());
	ds.setUserName(getUserName());
	ds.setPassword(getPassword());
	pageContext.setAttribute(var, ds, scope);
	return SKIP_BODY;
    }


    //*********************************************************************
    // Private utility methods

    private String getDriverClassName() {
	if (driverClassName != null) {
	    return driverClassName;
	}
	ServletContext application = pageContext.getServletContext();
	return application.getInitParameter(DRIVER_CLASS_NAME);
    }

    private String getJdbcURL() {
	if (jdbcURL != null) {
	    return jdbcURL;
	}
	ServletContext application = pageContext.getServletContext();
	return application.getInitParameter(JDBC_URL);
    }

    private String getUserName() {
	if (userName != null) {
	    return userName;
	}
	ServletContext application = pageContext.getServletContext();
	return application.getInitParameter(USER_NAME);
    }

    private String getPassword() {
	ServletContext application = pageContext.getServletContext();
	return application.getInitParameter(PASSWORD);
    }
}
