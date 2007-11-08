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

import java.util.*;
import javax.naming.*;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

/*
 * @author Harish Prabandham
 */
class Repository {

// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END
	/**
	 * Finds a value corresponding to a name stored within this
	 * Repository.
	 * @return A string Value corresponding to the name within the 
	 * repository or null if no such value exists.
	 */
    public String find(String name) {
        try {
            String value = (String) ctx.lookup(name);
            // System.out.println(name + " is bound to " + value);
// START OF IASRI 4660742
            // _logger.log(Level.FINE,name + " is bound to " + value);
// END OF IASRI 4660742
            return value;
        } catch (Exception e) {
      //      e.printStackTrace();
// START OF IASRI 4660742
            // _logger.log(Level.SEVERE,"enterprise.findinrepository_exception",e);
// END OF IASRI 4660742
            return null;
        }
    }
    
	/**
	 * Adds a value corresponding to a name within this Repository.
	 * @return A value true indicates that the value was successfully 
	 * added to the repository.
	 */
    public boolean add(String name, String value) {
        try {
            ctx.rebind(name, value);
            // System.out.println("Added " + name + ", " + value);
// START OF IASRI 4660742
	    //if(_logger.isLoggable(Level.FINE))
                 // _logger.log(Level.FINE,"Added " + name + ", " + value);
// END OF IASRI 4660742
            return true;
	} catch (NamingException ne) {
// IASRI 4660742	    ne.printStackTrace();
// START OF IASRI 4660742
             _logger.log(Level.SEVERE,"enterprise.addinrepository_exception",ne);
// END OF IASRI 4660742
            return false;
	}
    }

	public boolean remove(String name){
    try {
        ctx.unbind(name);
        // System.out.println("Added " + name + ", " + value);
// START OF IASRI 4660742
	    //if(_logger.isLoggable(Level.FINE))
                 // _logger.log(Level.FINE,"Added " + name + ", " + value);
// END OF IASRI 4660742
        return true;
	} catch (NamingException ne) {
// IASRI 4660742	    ne.printStackTrace();
// START OF IASRI 4660742
             _logger.log(Level.SEVERE,"enterprise.delinrepository_exception",ne);
// END OF IASRI 4660742
        return false;
	}
	}

	public String[] keys()
	{
		Vector v = new Vector(10);
		Enumeration e = null;
        try {
            e = ctx.listBindings("");
		} catch (NamingException ne) {
// IASRI 4660742	   		 ne.printStackTrace();
// START OF IASRI 4660742
             _logger.log(Level.SEVERE,"enterprise.naming_exception",ne);
// END OF IASRI 4660742
		}

		while((e != null) && e.hasMoreElements())
		{
			Binding b = (Binding) e.nextElement();
			v.add(b.getName());
		}

		String[] keynames = new String[v.size()];
		v.copyInto(keynames);

		return keynames;
	}

	public String getName()
	{
        return RepositoryContext.getRepositoryName(name);
	}
    
    /**
     * Constructor.....
     */
    public Repository(String repositoryName) {
        Properties env = new Properties();
        env.put("java.naming.factory.initial", 
                "com.sun.enterprise.repository.RepositoryInitContextFactory");
        env.put("com.sun.enterprise.repository.name", repositoryName);
	name = repositoryName;
	init(env);
    }


    /**
     * Constructor.....
     */
    public Repository(String repositoryName, String repositoryDir) {
        Properties env = new Properties();
        env.put("java.naming.factory.initial", 
                "com.sun.enterprise.repository.RepositoryInitContextFactory");
        env.put("com.sun.enterprise.repository.name", repositoryName);
        env.put("com.sun.enterprise.repository.dir", repositoryDir);
	name = repositoryName;
	init(env);
    }


    /**
     * Initializing the context... 
     */
    private void init(Properties env) {
        try {
            ctx = new InitialContext(env);
        } catch (NamingException ne) {
// IASRI 4660742            ne.printStackTrace();
// START OF IASRI 4660742
             _logger.log(Level.SEVERE,"enterprise.naming_exception",ne);
// END OF IASRI 4660742
            ctx = null;
        }
    }

    
    private Context ctx = null;
	private String name = null;
}
