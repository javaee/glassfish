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
package org.apache.taglibs.standard.tag.el.sql;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.tag.common.sql.QueryTagSupport;

/**
 * Subclass for the JSTL library with EL support.
 *
 * @author Hans Bergsten
 * @author Justyna Horwat
 */
public class QueryTag extends QueryTagSupport {

    private String dataSourceEL;
    private String sqlEL;
    private String startRowEL;
    private String maxRowsEL;

    //*********************************************************************
    // Constructor

    /**
     * Constructs a new QueryTag.  As with TagSupport, subclasses
     * should not provide other constructors and are expected to call
     * the superclass constructor
     */
    public QueryTag() {
        super();
    }

    //*********************************************************************
    // Accessor methods

    public void setDataSource(String dataSourceEL) {
	this.dataSourceEL = dataSourceEL;
	this.dataSourceSpecified = true;
    }

    /**
     * The index of the first row returned can be
     * specified using startRow.
     */
    public void setStartRow(String startRowEL) {
	this.startRowEL = startRowEL;
    }

    /**
     * Query result can be limited by specifying
     * the maximum number of rows returned.
     */
    public void setMaxRows(String maxRowsEL) {
	this.maxRowsEL = maxRowsEL;
	this.maxRowsSpecified = true;
    }

    /**
     * Setter method for the SQL statement to use for the
     * query. The statement may contain parameter markers
     * (question marks, ?). If so, the parameter values must
     * be set using nested value elements.
     */
    public void setSql(String sqlEL) {
	this.sqlEL = sqlEL;
    }

    public int doStartTag() throws JspException {
        evaluateExpressions();
	return super.doStartTag();
    }

    //*********************************************************************
    // Private utility methods

    // Evaluates expressions as necessary
    private void evaluateExpressions() throws JspException {
        Integer tempInt = null;

        if (dataSourceEL != null) {
            rawDataSource = (Object) ExpressionEvaluatorManager.evaluate(
                "dataSource", dataSourceEL, Object.class, this, pageContext);
        }

        if (sqlEL != null) {
            sql = (String) ExpressionEvaluatorManager.evaluate("sql", sqlEL,
                String.class, this, pageContext);
        }

	if (startRowEL != null) {
	    tempInt = (Integer) ExpressionEvaluatorManager.evaluate(
                "startRow", startRowEL, Integer.class, this, pageContext);
	    if (tempInt != null)
		startRow = tempInt.intValue();
	}

	if (maxRowsEL != null) {
	    tempInt = (Integer) ExpressionEvaluatorManager.evaluate(
                "maxRows", maxRowsEL, Integer.class, this, pageContext);
	    if (tempInt != null)
		maxRows = tempInt.intValue();
	}
    }
}
