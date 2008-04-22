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
package org.glassfish.admin.amx.mbean;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import java.util.Map;


/**
	Interface for delegating responsibility for handling Attribute getters/setters
	as well as invoke().
 */
public interface Delegate
{
    /**
        An arbitrary (but hopefully meaningful) identifier
        for this Delegate.
     */
    public String   getID();
    
	public Object getAttribute( String attrName )
		throws	AttributeNotFoundException;

	public AttributeList getAttributes( final String[] attrNames );
	
    /**
        Note: singular form of setAttribute() is not supported.
        
        @param attrs the Attributes to change
        @param oldValue oldValues corresponding to each changed Attribute
     */
	public AttributeList setAttributes( final AttributeList attrs, final Map<String,Object> oldValues);
	
	/**
		Return true if the Attribute is supported
	 */
	public boolean	supportsAttribute( String name );
	
	/**
		Return true if the operation is supported
	 */
	public boolean	supportsOperation( 
		String 		operationName,
		Object[]	args,
		String[]	types  );
	
	/**
		invoke the operation.
	 */
	public Object	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types );
		
	
	public void	setOwner( DelegateOwner owner );
}








