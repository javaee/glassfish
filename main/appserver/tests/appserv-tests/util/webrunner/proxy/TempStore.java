/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.ejte.ccl.webrunner.proxy;

import java.io.*;


	/**
	* This class is for copying array of bytes. 
	* @author       Deepa Singh(deepa.singh@sun.com)
        *Company: Sun Microsystems Inc
	* 
	* 
	*/
public class TempStore
{
	String url;
	MimeHeader mh;
	byte data[];
	int length=0;

	/**
	* Constructor that Takes two Stringa as parameter 
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		u	The string URL
	* @param		m	mimeheader
	*/
	public TempStore(String u,MimeHeader m)
	{
		url=u;
		mh=m;
		String cl=mh.get((String)"Content-Length");
		if(cl!=null)
		{
		data=new byte[Integer.parseInt(cl)];
		}
	}



	/**
	* 
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		d[]	Byte array
	* @param		n Integer
	*/
	void append(byte d[],int n)
	{
		if(data==null)
		{
			data=new byte[n];
			System.arraycopy(d,0,data,0,n);
			length+=n;
		} else if(length+n>data.length)
		{
		byte old[]=data;
		data=new byte[old.length+n];
		System.arraycopy(old,0,data,0,old.length);
		System.arraycopy(d,0,data,old.length,n);
		length+=n;
		}else
		{
		System.arraycopy(d,0,data,length,n);
		length+=n;
		}
	}
}


