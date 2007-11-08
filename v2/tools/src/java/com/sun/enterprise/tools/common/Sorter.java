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
 * Sorter.java
 *
 * Created on January 18, 2001, 3:53 PM
 */

package com.sun.enterprise.tools.common;

/**
 *
 * @author  administrator
 * @version 
 */
// public class Sorter extends java.lang.Object {

import java.util.*;

public class Sorter
{	
	
	////////////////////////////////////////////////////////////////////////////////
	
	public static void sort(Vector v) 
	{
		if(v.size() <= 1)
			return;

		SorterObject[] arr = new SorterObject[v.size()];
		Enumeration e = v.elements();
		
		for(int i = 0 ; e.hasMoreElements() ; i++)
		{
			arr[i] = new SorterObject(e.nextElement());
		}

		mergeSort(arr);

		v.removeAllElements();

		for(int i = 0; i < arr.length; i++)
		{
			v.addElement(arr[i].obj);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////

	private static void mergeSort(SorterObject[] arr)
	{
		int low = 0;
		int high = arr.length;

		for (int i = low; i < high; i++)
		{
			for (int j = i; j > low &&	arr[j-1].sortName.compareTo(arr[j].sortName) > 0; j--)
			{
				swap(arr, j, j-1);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////

	private static void swap(SorterObject x[], int a, int b) 
	{
		SorterObject t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
	
	////////////////////////////////////////////////////////////////////////////////

	private static void print(Vector v, String s)
	{
		System.out.println(s + "\n");//NOI18N

		for(Enumeration e = v.elements(); e.hasMoreElements(); )
		{
			System.out.println("" + e.nextElement());//NOI18N
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		Vector v1 = new Vector();
		//Vector v2 = new Vector();
		//Vector v3 = new Vector();

		v1.addElement("aaaa");//NOI18N
		v1.addElement("zzzz");//NOI18N
		v1.addElement("dddd");//NOI18N
		v1.addElement("ccccx");//NOI18N
		v1.addElement("cccc");//NOI18N
		v1.addElement("cccc");//NOI18N
		v1.addElement("cccc");//NOI18N
		v1.addElement("bbbb");//NOI18N

		print(v1, "****** before");//NOI18N
		sort(v1);
		print(v1, "\n******after");//NOI18N
	}
	
	////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////

class SorterObject
{
	SorterObject(Object obj)
	{
		this.obj = obj;
		sortName = obj.toString();
	}
	String sortName;
	Object obj;
}
