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
package com.sun.enterprise.naming;

import java.rmi.*;
import java.io.*;
import java.util.logging.*;
import com.sun.logging.*;
import com.sun.enterprise.util.*;

/**
 * This is a utils class for refactoring the following method.
 */

public class NamingUtils { 

   static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);

   /**
     * method to make a copy of the object.
     */ 

  public static Object makeCopyOfObject(Object obj) {

       
	if ( obj instanceof Serializable ) {
	    try {
		// first serialize the object
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		byte[] data = bos.toByteArray();
		oos.close();
		bos.close();
		
		// now deserialize it
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ObjectInputStream ois = new ObjectInputStreamWithLoader(bis,cl);
             
                               
		return ois.readObject();
	    } catch ( Exception ex ) {
         
                _logger.log(Level.SEVERE,
                            "enterprise_naming.excep_in_copymutableobj", ex);

                RuntimeException re = 
                    new RuntimeException("Cant copy Serializable object:");
                re.initCause(ex);
                throw re;
	    }
	}
	else {
	    // XXX no copy ?
	    return obj;
	}
    }
}
