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
package com.sun.enterprise.instance;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.ConfigException;

/** 
 * The purpose of this class is to create localized ConfigException
 * objects conveniently.  The code is here instead of in ConfigException itself
 * to avoid passing in a StringManager reference everytime a ConfigException
 * is thrown -- and the concommitant increase in the number of constructors.
 * To use this class you create one instance for each calling class so that
 * the correct properties file is chosen automatically.
 *
 * Note that StringManager only uses the *package* name to locate the properties file.  
 * Also note that only this package can access this class.  Thus the StringManager
 * can be shared by all callers of this class.
 *
 * @author Byron Nevins
 */

class Localizer
{
	private Localizer()
	{
	}

	///////////////////////////////////////////////////////////////////////////

	static String getValue(ExceptionType type)
	{
		String key = type.getString();
		
		if(localStrings == null)
			return key;
		
		try
		{
			return localStrings.getStringWithDefault(key, key);
		}
		catch(Exception e)
		{
			return key;
		}
	}

	///////////////////////////////////////////////////////////////////////////

	static String getValue(ExceptionType type, String arg1of1)
	{
		return getValue(type, new Object[] { arg1of1 });
	}

	///////////////////////////////////////////////////////////////////////////

	static String getValue(ExceptionType type, int arg1of1)
	{
		return getValue(type, new Object[] { new Integer(arg1of1) });
	}

	///////////////////////////////////////////////////////////////////////////

	static String getValue(ExceptionType type, Object[] objs)
	{
		if(objs == null || objs.length <= 0)
			return getValue(type);

		String key = type.getString();
		
		if(localStrings == null)
			return key;
		
		try
		{
			return localStrings.getStringWithDefault(key, key, objs);
		}
		catch(Exception e)
		{
			return key;
		}
	}

	///////////////////////////////////////////////////////////////////////////

	private static StringManager localStrings;
	
	static
	{
		try
		{
			localStrings = StringManager.getManager(Localizer.class.getPackage().getName());
		}
		catch(Exception e)
		{
			//StringManager has already logged the problem...
			localStrings = null;
		}
	}
}
