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
package com.sun.enterprise.repository;

import java.util.Hashtable;
import java.util.Enumeration;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

/**
 * @author Harish Prabandham
 */
public class RepositoryInitContextFactory implements InitialContextFactory {
// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END

    public Context getInitialContext(Hashtable env) {
        return new RepositoryContext(env);
    }

	public static void print(Hashtable env)
	{
// IASRI 4660742		System.out.println("RepositoryContextFactory[");
// START OF IASRI 4660742
		_logger.log(Level.FINE,"RepositoryContextFactory[");
// END OF IASRI 4660742
		for(Enumeration e = env.keys(); e.hasMoreElements(); )
		{
			String key = (String) e.nextElement();
			Object value = env.get(key);

// IASRI 4660742			System.out.println("(" + key + " , " + value + ")" );
// START OF IASRI 4660742
			_logger.log(Level.FINE,"(" + key + " , " + value + ")");
// END OF IASRI 4660742
		}
// IASRI 4660742		System.out.println("]");
// START OF IASRI 4660742
			_logger.log(Level.FINE,"]");
// END OF IASRI 4660742
	}
}
