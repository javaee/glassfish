/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.examples.startup;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.taglibs.standard.examples.beans.Customers;

/**
 * Initialization class. Builds all the data structures
 * used in the "examples" webapp.
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:39 $
 */
public class Init implements ServletContextListener {
    
    //*********************************************************************
    // ServletContextListener methods
    
    // recovers the one context parameter we need
    public void contextInitialized(ServletContextEvent sce) {
        //p("contextInitialized");
        init(sce);
    }
    
    public void contextDestroyed(ServletContextEvent sce) {
        //p("contextInitialized");
    }
    
    //*********************************************************************
    // Initializations
    
    private void init(ServletContextEvent sce) {
        /*
         *  Customers
         */
        Customers.create("Richard", "Maurice", "5/15/35",
        "123 Chemin Royal", "Appt. #301",
        "Montreal", "QC", "H3J 9R9", "Canada");
        Customers.create("Mikita", "Stan", "12/25/47",
        "45 Fisher Blvd", "Suite 203",
        "Chicago", "IL", "65982", "USA", "(320)876-9784", null);
        Customers.create("Gilbert", "Rod", "3/11/51",
        "123 Main Street", "",
        "New-York City", "NY", "19432", "USA");
        Customers.create("Howe", "Gordie", "7/25/46",
        "7654 Wings Street", "",
        "Detroit", "MG", "07685", "USA", "(465)675-0761", "(465)879-9802");
        Customers.create("Sawchuk", "Terrie", "11/05/46",
        "12 Maple Leafs Avenue", "",
        "Toronto", "ON", "M5C 1Z1", "Canada");
        sce.getServletContext().setAttribute("customers", Customers.findAll());

	/**
	 * Array of primitives (int)
	 */
	int[] intArray = new int[] {10, 20, 30, 40, 50};
        sce.getServletContext().setAttribute("intArray", intArray);

	/**
	 * Array of Objects (String)
	 */
	String[] stringArray = new String[] {
	    "A first string",
	    "La deuxieme string",
	    "Ella troisiemo stringo",
	};
        sce.getServletContext().setAttribute("stringArray", stringArray);

	/**
        * String-keyed Map
        */
        Hashtable stringMap = new Hashtable();
        sce.getServletContext().setAttribute("stringMap", stringMap);
        stringMap.put("one", "uno");
        stringMap.put("two", "dos");
        stringMap.put("three", "tres");
        stringMap.put("four", "cuatro");
        stringMap.put("five", "cinco");
        stringMap.put("six", "seis");
        stringMap.put("seven", "siete");
        stringMap.put("eight", "ocho");
        stringMap.put("nine", "nueve");
        stringMap.put("ten", "diez");

        /**
         * Integer-keyed Map
	 */
	// we use a Hashtable so we can get an Enumeration easily, below
        Hashtable numberMap = new Hashtable();
	sce.getServletContext().setAttribute("numberMap", numberMap);
	numberMap.put(new Integer(1), "uno");
	numberMap.put(new Integer(2), "dos");
	numberMap.put(new Integer(3), "tres");
	numberMap.put(new Integer(4), "cuatro");
	numberMap.put(new Integer(5), "cinco");
	numberMap.put(new Integer(6), "seis");
	numberMap.put(new Integer(7), "siete");
	numberMap.put(new Integer(8), "ocho");
	numberMap.put(new Integer(9), "nueve");
	numberMap.put(new Integer(10), "diez");

	/**
	 * Enumeration
	 */
	Enumeration enum_ = numberMap.keys();
	// don't use 'enum' for attribute name because it is a 
	// reserved word in EcmaScript.
        sce.getServletContext().setAttribute("enumeration", enum_);

	/**
	 * Message arguments for parametric replacement
	 */
	Object[] serverInfoArgs =
	    new Object[] {
		sce.getServletContext().getServerInfo(),
		System.getProperty("java.version")
	    };
	sce.getServletContext().setAttribute("serverInfoArgs", serverInfoArgs);
    }
    
    //*********************************************************************
    // Initializations
    
    private void p(String s) {
        System.out.println("[Init] " + s);
    }
}
