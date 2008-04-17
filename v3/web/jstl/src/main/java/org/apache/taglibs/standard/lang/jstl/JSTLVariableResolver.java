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

package org.apache.taglibs.standard.lang.jstl;

import javax.servlet.jsp.PageContext;

/**
 *
 * <p>This is the JSTL-specific implementation of VariableResolver.
 * It looks up variable references in the PageContext, and also
 * recognizes references to implicit objects.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class JSTLVariableResolver
  implements VariableResolver
{
  //-------------------------------------
  /**
   *
   * Resolves the specified variable within the given context.
   * Returns null if the variable is not found.
   **/
  public Object resolveVariable (String pName,
				 Object pContext)
    throws ELException
  {
    PageContext ctx = (PageContext) pContext;

    // Check for implicit objects
    if ("pageContext".equals (pName)) {
      return ctx;
    }
    else if ("pageScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getPageScopeMap ();
    }
    else if ("requestScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getRequestScopeMap ();
    }
    else if ("sessionScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getSessionScopeMap ();
    }
    else if ("applicationScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getApplicationScopeMap ();
    }
    else if ("param".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getParamMap ();
    }
    else if ("paramValues".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getParamsMap ();
    }
    else if ("header".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getHeaderMap ();
    }
    else if ("headerValues".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getHeadersMap ();
    }
    else if ("initParam".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getInitParamMap ();
    }
    else if ("cookie".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getCookieMap ();
    }

    // Otherwise, just look it up in the page context
    else {
      return ctx.findAttribute (pName);
    }
  }
					
  //-------------------------------------
}
