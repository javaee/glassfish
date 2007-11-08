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

package org.apache.taglibs.standard.tag.common.core;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>A handler for the &lt;remove&gt; tag, which removes the variable
 * identified by 'var' (and 'scope', if present).
 *
 * @author Shawn Bayern
 */
public class RemoveTag extends TagSupport {

    //*********************************************************************
    // Constants

    /* We support these 'scopes'. */

    private final String APPLICATION = "application";
    private final String SESSION = "session";
    private final String REQUEST = "request";
    private final String PAGE = "page";

    //*********************************************************************
    // Internal state

    private int scope;					// tag attribute
    private boolean scopeSpecified;			// ... by tag attribute
    private String var;					// tag attribute


    //*********************************************************************
    // Construction and initialization

    /**
     * Constructs a new handler.  As with TagSupport, subclasses should
     * not provide other constructors and are expected to call the
     * superclass constructor.
     */
    public RemoveTag() {
        super();
        init();
    }

    // resets local state
    private void init() {
        var = null;
        scope = PageContext.PAGE_SCOPE;
        scopeSpecified = false;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Tag logic

    // removes the variable (from a specific scope, if specified)
    public int doEndTag() throws JspException {
        if (!scopeSpecified)
            pageContext.removeAttribute(var);
        else
            pageContext.removeAttribute(var, scope);
	return EVAL_PAGE;
    }


    //*********************************************************************
    // Accessor methods

    // for tag attribute
    public void setVar(String var) {
	this.var = var;
    }

    // for tag attribute
    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
	scopeSpecified = true;
    }
}
