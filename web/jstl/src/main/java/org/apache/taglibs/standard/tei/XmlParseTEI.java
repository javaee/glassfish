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

package org.apache.taglibs.standard.tei;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

/**
 * <p>An implementation of TagExtraInfo that implements validation for
 * <x:parse>'s attributes</p>
 *
 * @author Shawn Bayern
 */
public class XmlParseTEI extends TagExtraInfo {

    final private static String VAR = "var";
    final private static String VAR_DOM = "varDom";
    final private static String SCOPE = "scope";
    final private static String SCOPE_DOM = "scopeDom";

    public boolean isValid(TagData us) {
	// must have no more than one of VAR and VAR_DOM ...
	if (Util.isSpecified(us, VAR) && Util.isSpecified(us, VAR_DOM))
	    return false;

	// ... and must have no less than one of VAR and VAR_DOM
	if (!(Util.isSpecified(us, VAR) || Util.isSpecified(us, VAR_DOM)))
	    return false;

	// When either 'scope' is specified, its 'var' must be specified
	if (Util.isSpecified(us, SCOPE) && !Util.isSpecified(us, VAR))
	    return false;
	if (Util.isSpecified(us, SCOPE_DOM) && !Util.isSpecified(us, VAR_DOM))
	    return false;

        return true;
    }

}
