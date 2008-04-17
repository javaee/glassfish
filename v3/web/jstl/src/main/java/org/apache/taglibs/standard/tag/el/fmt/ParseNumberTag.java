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

package org.apache.taglibs.standard.tag.el.fmt;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.tag.common.fmt.ParseNumberSupport;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;

/**
 * <p>A handler for &lt;parseNumber&gt; that accepts attributes as Strings
 * and evaluates them as expressions at runtime.</p>
 *
 * @author Jan Luehe
 */

public class ParseNumberTag extends ParseNumberSupport {

    //*********************************************************************
    // 'Private' state (implementation details)

    private String value_;                       // stores EL-based property
    private String type_;                        // stores EL-based property
    private String pattern_;		         // stores EL-based property
    private String parseLocale_;	         // stores EL-based property
    private String integerOnly_;	         // stores EL-based property


    //*********************************************************************
    // Constructor

    /**
     * Constructs a new ParseNumberTag.  As with TagSupport, subclasses
     * should not provide other constructors and are expected to call
     * the superclass constructor
     */
    public ParseNumberTag() {
        super();
        init();
    }


    //*********************************************************************
    // Tag logic

    // evaluates expression and chains to parent
    public int doStartTag() throws JspException {

        // evaluate any expressions we were passed, once per invocation
        evaluateExpressions();

	// chain to the parent implementation
	return super.doStartTag();
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Accessor methods

    // for EL-based attribute
    public void setValue(String value_) {
        this.value_ = value_;
	this.valueSpecified = true;
    }

    // for EL-based attribute
    public void setType(String type_) {
        this.type_ = type_;
    }

    // for EL-based attribute
    public void setPattern(String pattern_) {
        this.pattern_ = pattern_;
    }

    // for EL-based attribute
    public void setParseLocale(String parseLocale_) {
        this.parseLocale_ = parseLocale_;
    }

    // for EL-based attribute
    public void setIntegerOnly(String integerOnly_) {
        this.integerOnly_ = integerOnly_;
	this.integerOnlySpecified = true;
    }


    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
	value_ = type_ = pattern_ = parseLocale_ = integerOnly_ = null;
    }

    // Evaluates expressions as necessary
    private void evaluateExpressions() throws JspException {
	Object obj = null;

        /* 
         * Note: we don't check for type mismatches here; we assume
         * the expression evaluator will return the expected type
         * (by virtue of knowledge we give it about what that type is).
         * A ClassCastException here is truly unexpected, so we let it
         * propagate up.
         */

	// 'value' attribute
	if (value_ != null) {
	    value = (String) ExpressionEvaluatorManager.evaluate(
	        "value", value_, String.class, this, pageContext);
	}

	// 'type' attribute
	if (type_ != null) {
	    type = (String) ExpressionEvaluatorManager.evaluate(
	        "type", type_, String.class, this, pageContext);
	}

	// 'pattern' attribute
	if (pattern_ != null) {
	    pattern = (String) ExpressionEvaluatorManager.evaluate(
	        "pattern", pattern_, String.class, this, pageContext);
	}

	// 'parseLocale' attribute
	if (parseLocale_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "parseLocale", parseLocale_, Object.class, this, pageContext);
	    if (obj != null) {
		if (obj instanceof Locale) {
		    parseLocale = (Locale) obj;
		} else {
		    String localeStr = (String) obj;
		    if (!"".equals(localeStr)) {
			parseLocale = SetLocaleSupport.parseLocale(localeStr);
		    }
		}
	    }
	}

	// 'integerOnly' attribute
	if (integerOnly_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "integerOnly", integerOnly_, Boolean.class, this, pageContext);
	    if (obj != null) {
		isIntegerOnly = ((Boolean) obj).booleanValue();
	    }
	}
    }
}

