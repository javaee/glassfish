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

import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Support for the XML library's &lt;forEach&gt; tag.</p>
 *
 * @see javax.servlet.jsp.jstl.core.LoopTagSupport
 * @author Shawn Bayern
 */
public class ForEachTag extends LoopTagSupport {

    //*********************************************************************
    // Private state

    private String select;				// tag attribute
    private List nodes;					// XPath result
    private int nodesIndex;				// current index
    private org.w3c.dom.Node current;			// current node

    //*********************************************************************
    // Iteration control methods

    // (We inherit semantics and Javadoc from LoopTagSupport.) 

    protected void prepare() throws JspTagException {
        nodesIndex = 0;
        XPathUtil xu = new XPathUtil(pageContext);
        nodes = xu.selectNodes(XPathUtil.getContext(this), select);
    }

    protected boolean hasNext() throws JspTagException {
        return (nodesIndex < nodes.size());
    }

    protected Object next() throws JspTagException {
	Object o = nodes.get(nodesIndex++);
	if (!(o instanceof org.w3c.dom.Node))
	    throw new JspTagException(
		Resources.getMessage("FOREACH_NOT_NODESET"));
	current = (org.w3c.dom.Node) o;
        return current;
    }


    //*********************************************************************
    // Tag logic and lifecycle management

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
        super.release();
    }


    //*********************************************************************
    // Attribute accessors

    public void setSelect(String select) {
	this.select = select;
    }

    public void setBegin(int begin) throws JspTagException {
        this.beginSpecified = true;
        this.begin = begin;
        validateBegin();
    }

    public void setEnd(int end) throws JspTagException {
        this.endSpecified = true;
        this.end = end;
        validateEnd();
    }

    public void setStep(int step) throws JspTagException {
        this.stepSpecified = true;
        this.step = step;
        validateStep();
    }
    
    //*********************************************************************
    // Public methods for subtags

    /* Retrieves the current context. */
    public org.w3c.dom.Node getContext() throws JspTagException {
	// expose the current node as the context
        return current;
    }


    //*********************************************************************
    // Private utility methods

    private void init() {
	select = null;
	nodes = null;
	nodesIndex = 0;
	current = null;
    }	
}

