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
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>WhenTagSupport is an abstract class that facilitates
 * implementation of &lt;when&gt;-style tags in both the rtexprvalue
 * and expression-evaluating libraries.  It also supports
 * &lt;otherwise&gt;.</p>
 *
 * <p>In particular, this base class does the following:</p>
 * 
 * <ul>
 *  <li> overrides ConditionalTagSupport.doStartTag() to implement the
 *       appropriate semantics of subtags of &lt;choose&gt; </li>
 * </ul>
 *
 * @author Shawn Bayern
 */
public abstract class WhenTagSupport extends ConditionalTagSupport
{
    //*********************************************************************
    // Implementation of exclusive-conditional behavior

    /*
     * Includes its body if condition() evalutes to true AND its parent
     * ChooseTag wants it to do so.  The condition will not even be
     * evaluated if ChooseTag instructs us not to run.
     */
    public int doStartTag() throws JspException {

        Tag parent;

        // make sure we're contained properly
        if (!((parent = getParent()) instanceof ChooseTag))
            throw new JspTagException(
		Resources.getMessage("WHEN_OUTSIDE_CHOOSE"));

        // make sure our parent wants us to continue
        if (!((ChooseTag) parent).gainPermission())
            return SKIP_BODY;                   // we've been reeled in

        // handle conditional behavior
        if (condition()) {
            ((ChooseTag) parent).subtagSucceeded();
            return EVAL_BODY_INCLUDE;
        } else
            return SKIP_BODY;
    }
}
