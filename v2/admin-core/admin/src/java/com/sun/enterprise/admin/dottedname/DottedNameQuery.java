/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 
/*
 * $Header: /cvs/glassfish/admin-core/admin/src/java/com/sun/enterprise/admin/dottedname/DottedNameQuery.java,v 1.3 2005/12/25 03:47:32 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:47:32 $
 */
package com.sun.enterprise.admin.dottedname;
 
 
import java.util.Set;
import javax.management.ObjectName;

/*
	A lookup service for DottedNames. No assumptions are made about how the
	association between DottedNames and ObjectNames is done.
 */
public interface DottedNameQuery extends DottedNameSource
{
	/*
		Find the ObjectName which corresponds to the dotted name. The name
		is assumed to be the full dotted name *prefix* (eg the name of a value
		is *not* included).
		
		@param dottedName	the full dotted name for an MBean
		@returns ObjectName	an ObjectName, or null if not found
	 */
	ObjectName	dottedNameToObjectName( String	dottedName );
	
	/*
		Return a Set consisting of all dotted name strings.  This set is the caller's
		copy and may be freely modified.
		
		@returns Set	the Set of all ObjectNames
	 */
	Set		allDottedNameStrings(  );
}
