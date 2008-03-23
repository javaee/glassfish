/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.tag.common.sql;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;


/**
 * <p>A simple <code>DataSource</code> utility for the standard
 * <code>DriverManager</code> class.
 *
 * TO DO: need to cache DataSource
 * 
 * @author Justyna Horwat
 */
public class DataSourceUtil {

    private static final String ESCAPE = "\\";
    private static final String TOKEN = ",";

    /**
     * If dataSource is a String first do JNDI lookup.
     * If lookup fails parse String like it was a set of JDBC parameters
     * Otherwise check to see if dataSource is a DataSource object and use as
     * is
     */
    static DataSource getDataSource(Object rawDataSource, PageContext pc)
	throws JspException
    {
	DataSource dataSource = null;

        if (rawDataSource == null) {
            rawDataSource = Config.find(pc, Config.SQL_DATA_SOURCE);
        }

	if (rawDataSource == null) {
	    return null;
	}

        /*
	 * If the 'dataSource' attribute's value resolves to a String
	 * after rtexpr/EL evaluation, use the string as JNDI path to
	 * a DataSource
	 */
        if (rawDataSource instanceof String) {
            try {
                Context ctx = new InitialContext();
                // relative to standard JNDI root for J2EE app
                Context envCtx = (Context) ctx.lookup("java:comp/env");
                dataSource = (DataSource) envCtx.lookup((String) rawDataSource);
            } catch (NamingException ex) {
                dataSource = getDataSource((String) rawDataSource);
            }
        } else if (rawDataSource instanceof DataSource) {
            dataSource = (DataSource) rawDataSource;
        } else {
	    throw new JspException(
                Resources.getMessage("SQL_DATASOURCE_INVALID_TYPE"));
	}

	return dataSource;
    }

    /**
     * Parse JDBC parameters and setup dataSource appropriately
     */
    private static DataSource getDataSource(String params)
	throws JspException
    {
        DataSourceWrapper dataSource = new DataSourceWrapper();

        String[] paramString = new String[4];
        int escCount = 0; 
        int aryCount = 0; 
        int begin = 0;

        for(int index=0; index < params.length(); index++) {
            char nextChar = params.charAt(index);
            if (TOKEN.indexOf(nextChar) != -1) {
                if (escCount == 0) {
                    paramString[aryCount] = params.substring(begin,index).trim();
                    begin = index + 1;
                    if (++aryCount > 4) {
                        throw new JspTagException(
                            Resources.getMessage("JDBC_PARAM_COUNT"));
                    }
                }
            }
            if (ESCAPE.indexOf(nextChar) != -1) {
                escCount++;
            }
            else {
                escCount = 0;
            }
        }
        paramString[aryCount] = params.substring(begin).trim();

	// use the JDBC URL from the parameter string
        dataSource.setJdbcURL(paramString[0]);

	// try to load a driver if it's present
        if (paramString[1] != null) {
            try {
                dataSource.setDriverClassName(paramString[1]);
            } catch (Exception ex) {
                throw new JspTagException(
                    Resources.getMessage("DRIVER_INVALID_CLASS",
					 ex.toString()), ex);
            }
	}

	// set the username and password
        dataSource.setUserName(paramString[2]);
        dataSource.setPassword(paramString[3]);

	return dataSource;
    }

}
