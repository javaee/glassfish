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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.jts.jta;

import javax.naming.*;
import javax.naming.spi.*;

/**
 * Factory for producing the UserTransactionImpl objects.
 *
 * @author Ram Jeyaraman
 * @version 1.0 Feb 09, 1999
 */
public class UserTransactionFactory implements ObjectFactory {

	/**
     * @param obj Reference information that can be used in creating an object.
     * @param name of this object relative to nameCtx (optional).
     * @param nameCtx context relative to which the name parameter specified.
     * 	If null, name is relative to the default initial context.
	 * @param environment possibly null environment used in creating the object.
     *
     * @return object created; null if an object cannot be created.
     *
     * @exception java.lang.Exception if this object factory encountered
     * 	an exception while attempting to create an object.
     */
 	public Object getObjectInstance(Object refObj, Name name,
    	Context nameCtx, java.util.Hashtable env)
        throws Exception {

        if (refObj == null || !(refObj instanceof Reference))
        	return null;

        Reference ref = (Reference) refObj;

        if (ref.getClassName().
        	equals(UserTransactionImpl.class.getName())) {
            // create a new object
        	return new UserTransactionImpl();
        }

        return null;
    }
}
