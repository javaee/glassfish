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
package org.glassfish.admin.amx.dotted;
 
import java.util.Set;
import javax.management.ObjectName;



/*
	A registry for DottedName-to-ObjectName mapping.
 */
public interface DottedNameRegistry extends DottedNameQuery
{
	/*
		Add a mapping from a dotted name to an ObjectName.  If an existing
		mapping is present, it is replaced.  An exception may also
		be thrown if the dotted name is illegal or either parameter is null,
		or in general the implementing class does not allow such a mapping.
		
		Multiple dotted names may be associated with the same ObjectName; it is
		a policy decision of the caller whether to use that facility.
		
		@param dottedName	the dotted name to which the ObjectName should be associated
		@param objectName	the name of the MBean which associates with the dotted name.
	 */
	void	add( String dottedName, ObjectName objectName );
	
	/*
		Removes the entry associating the dotted name with an ObjectName. No error
		is produced if there is no such mapping.
		
		@param dottedName	the dotted name to which the ObjectName should be associated
	 */
	void	remove( String dottedName );
	
	/*
		Removes all dotted names associated with this ObjectName.  No error
		is produced if there is no such mapping.
		
		@param objectName	the ObjectName associated with one or more dotted names
	 */
	void	remove( ObjectName objectName );
}

