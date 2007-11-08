/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


/*
 * ConnectionFactoryFactory.java
 *
 * Created on November 12, 2001, 3:17 PM
 */

package com.sun.xml.registry.common;

import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.*;

/**
 *
 * @author  Forte 4 Java
 */
public class ConnectionFactoryFactory implements ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context context,
        Hashtable hashtable) throws Exception {
            if (obj instanceof Reference) {
                
                // use this later if conn factory has state
                Reference ref = (Reference) obj;
                return new ConnectionFactoryImpl();
            } else {
                return null;
            }
    }    
    
}
