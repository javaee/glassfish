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

import static org.glassfish.admin.amx.impl.path.DottedNameSpecialChars.SUBSCRIPT_CHARS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
	Represents the attribute portion of a V3 path name
 */
public final class PathPart
{
    final String mPart;
    final String mType;
    final String mName;
													  
		public
	PathPart( final String part ) {
        if ( part.length() == 0 ) {
            throw new IllegalArgumentException(part);
        }
        
        mPart  = part;
        
        String[] typeAndName = parse(part);
        mType = typeAndName[0];
        mName = typeAndName[1];
	}
    
    /** could be "servers" or "servers[name]". */
    private static String[] parse( final String s ) {
        String type = null;
        String name = null;
        
        final int b1 = s.indexOf( SUBSCRIPT_CHARS.charAt(0) );
        if ( b1 > 0 ) {
            final int b2 = s.indexOf( SUBSCRIPT_CHARS.charAt(1) );
            if ( b2 == s.length() - 1 ) {
                type = s.substring(0,b1);
                name = s.substring(b1+1,b2);
            }
        }
        else {
            type = s;
        }
        
        return new String[] { type, name };
    }
    
    public String toString()        { return mPart; }
    
    public boolean isNamed() {
        return getName() != null;
    }
    
    public String getType() {
        return mType;
    }
    
    public String getName() {
        return mName;
    }
}





