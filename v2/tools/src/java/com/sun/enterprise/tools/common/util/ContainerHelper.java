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

package com.sun.enterprise.tools.common.util;

import java.util.AbstractCollection;
import java.util.ArrayList;

public class ContainerHelper
{
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String[] toStringArray(AbstractCollection coll) throws ArrayStoreException
	{
		String ss[] = new String[0];

		if(coll.size() > 0)
			ss = (String[]) coll.toArray(ss);

		return ss;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public static ArrayList toArrayList(String[] ss)
	{
		if(ss == null)
			return new ArrayList();

		ArrayList list = new ArrayList(ss.length);

		for(int i = 0; i < ss.length; i++)
			list.add(ss[i]);

		return list;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String toOneString(String[] ss)
	{
		String s = new String();
		
		for(int i = 0; ss != null && i < ss.length; i++)
		{
			s += ss[i] + "\n";//NOI18N
		}
		return s;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		ArrayList L1 = new ArrayList();
		ArrayList L2 = new ArrayList();

		L1.add("Hello");//NOI18N
		L1.add("World");//NOI18N
		L1.add("!!!");//NOI18N
		L2.add(new Integer(5));
		L2.add(new Integer(7));

		String ss[] = toStringArray(L1);

		for(int i = 0; i < ss.length; i++)
			System.out.println("String #" + i + ":  " + ss[i]);//NOI18N
		
		// should throw 
		try
		{
			ss = toStringArray(L2);
		}
		catch(ArrayStoreException e)
		{
			System.out.println("Caught an Exception, as expected:  " + e);//NOI18N
		}
	}
}

