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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.sql.SQLExecutionTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.resources.Resources;


/**
 * <p>Tag handler for &lt;Param&gt; in JSTL, used to set
 * parameter values for a SQL statement.</p>
 * 
 * @author Justyna Horwat
 */

public abstract class DateParamTagSupport extends TagSupport {

    //*********************************************************************
    // Private constants
    
    private static final String TIMESTAMP_TYPE = "timestamp";
    private static final String TIME_TYPE = "time";
    private static final String DATE_TYPE = "date";
	

    //*********************************************************************
    // Protected state

    protected String type;
    protected java.util.Date value;


    //*********************************************************************
    // Constructor

    public DateParamTagSupport() {
        super();
        init();
    }

    private void init() {
        value = null;
        type = null;
    }


    //*********************************************************************
    // Tag logic

    public int doEndTag() throws JspException {
	SQLExecutionTag parent = (SQLExecutionTag) 
	    findAncestorWithClass(this, SQLExecutionTag.class);
	if (parent == null) {
	    throw new JspTagException(
                Resources.getMessage("SQL_PARAM_OUTSIDE_PARENT"));
	}

        if (value != null) {
            convertValue();
        }

	parent.addSQLParameter(value);
	return EVAL_PAGE;
    }


    //*********************************************************************
    // Private utility methods

    private void convertValue() throws JspException {

	if ((type == null) || (type.equalsIgnoreCase(TIMESTAMP_TYPE))) {
	    if (!(value instanceof java.sql.Timestamp)) {
		value = new java.sql.Timestamp(value.getTime());
	    }
	} else if (type.equalsIgnoreCase(TIME_TYPE)) {
	    if (!(value instanceof java.sql.Time)) {
		value = new java.sql.Time(value.getTime());
	    }
	} else if (type.equalsIgnoreCase(DATE_TYPE)) {
	    if (!(value instanceof java.sql.Date)) {
		value = new java.sql.Date(value.getTime());
	    }
	} else {
	    throw new JspException(
                Resources.getMessage("SQL_DATE_PARAM_INVALID_TYPE", type));
	}
    }
}
