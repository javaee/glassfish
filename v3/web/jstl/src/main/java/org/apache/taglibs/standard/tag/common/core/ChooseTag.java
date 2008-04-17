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

package org.apache.taglibs.standard.tag.common.core;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Tag handler for &lt;choose&gt; in JSTL.</p>
 * 
 * <p>&lt;choose&gt; is a very simple tag that acts primarily as a container;
 * it always includes its body and allows exactly one of its child
 * &lt;when&gt; tags to run.  Since this tag handler doesn't have any
 * attributes, it is common.core to both the rtexprvalue and expression-
 * evaluating versions of the JSTL library.
 *
 * @author Shawn Bayern
 */

public class ChooseTag extends TagSupport {

    //*********************************************************************
    // Constructor and lifecycle management

    // initialize inherited and local state
    public ChooseTag() {
        super();
        init();
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Private state

    private boolean subtagGateClosed;      // has one subtag already executed?


    //*********************************************************************
    // Public methods implementing exclusivity checks

    /**
     * Returns status indicating whether a subtag should run or not.
     *
     * @return <tt>true</tt> if the subtag should evaluate its condition
     *         and decide whether to run, <tt>false</tt> otherwise.
     */
    public synchronized boolean gainPermission() {
        return (!subtagGateClosed);
    }

    /**
     * Called by a subtag to indicate that it plans to evaluate its
     * body.
     */
    public synchronized void subtagSucceeded() {
        if (subtagGateClosed)
            throw new IllegalStateException(
		Resources.getMessage("CHOOSE_EXCLUSIVITY"));
        subtagGateClosed = true;
    }


    //*********************************************************************
    // Tag logic

    // always include body
    public int doStartTag() throws JspException {
        subtagGateClosed = false;	// when we start, no children have run
        return EVAL_BODY_INCLUDE;
    }


    //*********************************************************************
    // Private utility methods

    private void init() {
        subtagGateClosed = false;                          // reset flag
    }
}
