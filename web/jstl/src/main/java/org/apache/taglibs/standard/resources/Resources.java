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

package org.apache.taglibs.standard.resources;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Provides locale-neutral access to string resources.  Only the
 * documentation and code are in English. :-)
 *
 * <p>The major goal, aside from globalization, is convenience.
 * Access to resources with no parameters is made in the form:</p>
 * <pre>
 *     Resources.getMessage(MESSAGE_NAME);
 * </pre>
 *
 * <p>Access to resources with one parameter works like</p>
 * <pre>
 *     Resources.getMessage(MESSAGE_NAME, arg1);
 * </pre>
 *
 * <p>... and so on.</p>
 *
 * @author Shawn Bayern
 */
public class Resources {

    //*********************************************************************
    // Static data

    /** The location of our resources. */
    private static final String RESOURCE_LOCATION
	= "org.apache.taglibs.standard.resources.Resources";

    /** Our class-wide ResourceBundle. */
    private static ResourceBundle rb =
	ResourceBundle.getBundle(RESOURCE_LOCATION);


    //*********************************************************************
    // Public static methods

    /** Retrieves a message with no arguments. */
    public static String getMessage(String name)
	    throws MissingResourceException {
	return rb.getString(name);
    }

    /** Retrieves a message with arbitrarily many arguments. */
    public static String getMessage(String name, Object[] a)
	    throws MissingResourceException {
	String res = rb.getString(name);
	return MessageFormat.format(res, a);
    }

    /** Retrieves a message with one argument. */
    public static String getMessage(String name, Object a1)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1 });
    }

    /** Retrieves a message with two arguments. */
    public static String getMessage(String name, Object a1, Object a2)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1, a2 });
    }

    /** Retrieves a message with three arguments. */
    public static String getMessage(String name,
				    Object a1,
				    Object a2,
				    Object a3)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1, a2, a3 });
    }

    /** Retrieves a message with four arguments. */
    public static String getMessage(String name,
			 	    Object a1,
				    Object a2,
				    Object a3,
				    Object a4)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1, a2, a3, a4 });
    }

    /** Retrieves a message with five arguments. */
    public static String getMessage(String name,
				    Object a1,
				    Object a2,
				    Object a3,
				    Object a4,
				    Object a5)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1, a2, a3, a4, a5 });
    }

    /** Retrieves a message with six arguments. */
    public static String getMessage(String name,
				    Object a1,
				    Object a2,
				    Object a3,
				    Object a4,
				    Object a5,
				    Object a6)
	    throws MissingResourceException {
	return getMessage(name, new Object[] { a1, a2, a3, a4, a5, a6 });
    }

}
