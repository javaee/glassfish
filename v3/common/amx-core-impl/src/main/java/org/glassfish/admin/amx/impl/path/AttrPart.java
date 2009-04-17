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
package org.glassfish.admin.amx.impl.path;

import static org.glassfish.admin.amx.impl.path.DottedNameSpecialChars.ESCAPE_CHAR;
import static org.glassfish.admin.amx.impl.path.DottedNameSpecialChars.WILDCARDS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
	Represents the attribute portion of a V3 path name
 */
public final class AttrPart
{
    final String mName;
    
    /** may be null */
    final String mValue;
													  
		public
	AttrPart( final String name, final String value ) {
        mName  = name;
        mValue = value;
	}
    
    public AttrPart( final String name  ) {
        this( name, null );
    }
    
    public String getName()  { return mName; }
    public String getValue() { return mValue; }
    
    public static AttrPart parseAttrPart( final String s ) {
        final int idx = s.indexOf("=");
        
        AttrPart part = null;
        if ( idx < 0 )  {
            part = new AttrPart(s);
        }
        else {
            part = new AttrPart( s.substring(0,idx), s.substring(idx+1,s.length()));
        }
        
        return part;
    }
    
    public String toString() {
        String result = mName;
        if ( mValue != null ) {
            result = result + "=" + mValue;
        }
        return result;
    }
}





