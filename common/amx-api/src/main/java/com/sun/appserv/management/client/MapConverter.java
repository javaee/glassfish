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
package com.sun.appserv.management.client;

import static com.sun.appserv.management.base.MapCapable.MAP_CAPABLE_CLASS_NAME_KEY;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl;

import java.io.Serializable;
import java.util.Map;

/**
    Converts Maps obtained from the server back into their
    proprietary (non-standard) java types.
 */
public final class MapConverter
{
    private MapConverter()  {}
    
    /**
        Of course there are more elaborate ways to do this, but given the small
        number of conversions, this straightforward approach is best.
     */
        private static Object
    doConvert( final Map<String,Serializable> m )
    {
        Object  result  = m;    // don't convert, by default
        
        final String interfaceName = (String)m.get(MAP_CAPABLE_CLASS_NAME_KEY);
        if ( interfaceName != null )
        {
            if ( MessageTrace.CLASS_NAME.equals( interfaceName ) )
            {
                result  = new MessageTraceImpl( m, MessageTrace.class.getName());
            }
            else
            {
                // That's OK, we just leave it as a Map
            }
        }
        return result;
    }
    
    
    /**
        This form should be used where the appropriate class is not
        known in advance.
     */
        public static Object
    convert( final Map<String,Serializable> m )
    {
        return doConvert( m );
    }
    
    /**
        This form should be used where the appropriate class is known
        in advance.
     */
        public static <T> T
    convertToClass(
        final Map<String,Serializable> m,
        final Class<T>  theClass )
    {
        return theClass.cast( doConvert( m ) );
    }
}





