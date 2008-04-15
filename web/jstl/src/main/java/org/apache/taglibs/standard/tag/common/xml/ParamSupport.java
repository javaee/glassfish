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

package org.apache.taglibs.standard.tag.common.xml;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Support for tag handlers for &lt;param&gt;, the XML parameter
 * subtag for &lt;transformt&lt;.</p>
 *
 * @see TransformSupport
 * @author Shawn Bayern
 */

public abstract class ParamSupport extends BodyTagSupport {

    //*********************************************************************
    // Protected state

    protected String name;                       // 'name' attribute
    protected Object value;                      // 'value' attribute

    //*********************************************************************
    // Constructor and initialization

    public ParamSupport() {
	super();
	init();
    }

    private void init() {
	name = null;
	value = null;
    }


    //*********************************************************************
    // Tag logic

    // simply send our name and value to our parent <transform> tag
    public int doEndTag() throws JspException {
	Tag t = findAncestorWithClass(this, TransformSupport.class);
	if (t == null)
	    throw new JspTagException(
		Resources.getMessage("PARAM_OUTSIDE_TRANSFORM"));
	TransformSupport parent = (TransformSupport) t;

	Object value = this.value;
	if (value == null) {
            if (bodyContent == null || bodyContent.getString() == null)
                value = "";
            else
                value = bodyContent.getString().trim();
        }
	parent.addParameter(name, value);
	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }
}
