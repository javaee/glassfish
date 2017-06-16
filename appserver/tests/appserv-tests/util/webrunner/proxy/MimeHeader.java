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

import java.util.*;

/**
* MIME is an internet standard for communicating multimedia content over e-mail systems.MimeHeader extends HashTable so that it can store key value pairs.
* @author       Deepa Singh(deepa.singh@sun.com)
 *Company: Sun Microsystems Inc
* 
*/
public class MimeHeader extends Hashtable
{
	/**
	* Takes a String as a parameter and parses it.Takes a raw MIME-formatted String and enter its key/value pairs into a given instance of MimeHeader.Uses
         *StringTokenizer to split the input data into individual lines marked by CRLF(\r\n) sequence.
	* @author       Deepa Singh(deepa.singh@sun.com)
	* @return       void
	* @param		data	The string to be parsed
	*/
	void parse(String data)
	{
	StringTokenizer st=new StringTokenizer(data,"\r\n");
		while(st.hasMoreTokens())
		{
			String s=st.nextToken();
			int colon=s.indexOf(':');
			String key=s.substring(0,colon);
			String val=s.substring(colon+2);
			put(key,val);
		}
	}
	
	/**
	* Default Constructor 
         *Class is a subclass of HashTable so that it can conveneintly store and retreive the key/value pairs
         *associated with a MIME header.It creates a blank MimeHeader with no keys.
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		None
	*/
	MimeHeader(){}
	
	/**
	* This constructor takes strng formatted as MIME header and parses it for the initial contents of the objects.
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		d	The string to be parsed
	*/
	MimeHeader(String d)
	{
		parse(d);
	}
	
	/**
	* Converts to String .It takes current key/value pairs stored in the MimeHeader and returns a string representation of them in
         *MIME format, where keys are printed followed by colon and a space, and then value followed by CRLF.
	* @author       Deepa Singh(deepa.singh@sun.com)
	* @return       String
	*/
	public String toString()
	{
		String ret="";
		Enumeration e=keys();
		while(e.hasMoreElements())
		{
		String key=(String)e.nextElement();
		String val=(String)get(key);
		ret+=key + ": " + val + "\r\n";
		}
	return ret;
	}

	/**
	*To remove the discrepancy in MIME specification for "Content-Type" and "content-type" and "Content-Length" to "content-length"
         *To avoid problems, all incoming and outgoing MimeHeader keys are converted to canonical form. 
	* @author       Deepa Singh(deepa.singh@sun.com)
	* @return       String
	* @param		ms	String to be operated upon
	*/
	private String fix(String ms)
	{
		char chars[]=ms.toLowerCase().toCharArray();
		boolean upcaseNext=true;
		for(int i=0;i<chars.length-1;i++)
		{
			char ch=chars[i];
			if(upcaseNext && 'a' <=ch && ch <='z')
			{
				chars[i]=(char)(ch-('a'-'A'));
			}
			upcaseNext=ch=='-';
		}
	return new String(chars);
	}

	/**
	* @author       Deepa Singh(deepa.singh@sun.com)
	* @return       String
	* @param		key	String to be fetched	
	*/
	public String get(String key)
	{
		return (String)super.get(fix(key));
	}

	/**
	*  
	* @author       Deepa Singh(deepa.singh@sun.com)
	* @return       void
	* @param		key		The Key String	
	* @param		val  	The value String	
	*/
	public void put(String key,String val)
	{
		super.put(fix(key),val);
	}
	
	
}
