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
package com.sun.enterprise.admin.util;

/**
	Class for replacing numbered tokens in Strings with other Strings.  The
	static methods can be used for working with passed-in String arguments.
	Use the non-static methods for working with <i>indirect</i> Strings.
	I.e. String keys are passed in which in turn are used to locate the actual
	String to substitute.  The class in that case is instantiated by passing in 
	a IStringSource object which is used for fetching Strings.
	<p>The token format is <b>{#}</b>.  Where <b>#</b> is 1-9.  <b>Note that the 
	numbering is one-based, not zero-based.</b>  E.g.
	<p><code>MessageFormatter.format("Hello {1}, how are {2}?", "Carbon-based Lifeform", "you");</code>
	
 */ 


public class MessageFormatter implements IStringSource
{
	/** Create an instance using the supplied IStringSource to find Strings
	 * @param lookup The string library
	 */	
	public MessageFormatter(IStringSource source ) 
	{
		setSource(source);
	} 
	
	
	/** Set the IStringSource object
	 * @param lookup The IStringSource object
	 */	
	public void setSource(IStringSource source)
	{
		//ArgChecker.check(source, "source");
		//ArgChecker.check(source != this, "Can't setSource to self!");
		mSource	= source;
	} 
	
	
	/** Get the IStringSource object
	 * @return the IStringSource object
	 */	
	public IStringSource getSource()
	{
		Assert.assertit((mSource!=null), "mSource");
		return mSource;
	}
	
	
	/** IStringSource signature method.  In this case it asks the IStringSource
	 * member variable to get the String.
	 * @param lookupKey key to locate String with
	 * @return The String value for the key.  Null if not found.
	 */	
	public String getString(String lookupKey)
	{
		//ArgChecker.check(lookupKey, "lookupKey");
		return(mSource.getString(lookupKey));
	}

	
	/** This is the workhorse method of all of these overloaded methods.
	 * The array of Objects' toString() methods are called one at a time.  The
	 * resulting Strings are used to replace numbered tokens.  I.e. replacement
	 * String #0 replaces this token: <b>{1}</b> and replacement String #1 
	 * replaces this token: <b>{2}</b>.
	 * Avoid messy calling code by dedicating methods to the common cases of 
	 * 1-3 inserts.
	 * @param base String with embedded tokens
	 * @param toInsert An ordered array of Objects to replace the tokens with
	 * @return The original String with token substitution
	 */	
	public static String format(String base, Object[] toInsert)
	{
		// the only "format" method that actually does much work!
		// let's be gentle with nulls & empty Strings...
		
		//ArgChecker.checkValid(base, "base");
		
		if(toInsert == null || toInsert.length <= 0)
			return null;
		
		String ret = base;
		
		for(int i = 0; i < toInsert.length; i++)
		{
			String token	= makeToken(i + 1);
			String replace	= toInsert[i].toString();
			ret = replaceToken(ret, token, replace);
			
			if(ret == null || ret.length() <= 0)
				return null;
		}
		return ret;
	}
	
	
    /** Version of format with one token to replace
	 * @param base String with embedded tokens
	 * @param o1 An Object whose toString() results replace the token
	 * @return The original String with token substitution
	 */	
	public static String format(String base, Object o1)
	{
		return format(base, new Object[] {o1});
	}
	
	
	/** Version of format with two tokens to replace
	 * @param base String with embedded tokens
	 * @param o1 An Object whose toString() results replace the token
	 * @param o2 An Object whose toString() results replace the token
	 * @return The original String with token substitution
	 */	
	public static String format(String base, Object o1, Object o2)
	{
		return format(base, new Object[] {o1, o2});
	}

	
	/** Version of format with three tokens to replace
	 * @param base String with embedded tokens
	 * @param o1 An Object whose toString() results replace the token
	 * @param o2 An Object whose toString() results replace the token
	 * @param o3 An Object whose toString() results replace the token
	 * @return The original String with token substitution
	 */	
	public static String format(String base, Object o1, Object o2, Object o3)
	{
		return format(base, new Object[] {o1, o2, o3});
	}

	
	/** Get the String from the member IStringSource object -- then call
	 * <b>format</b> using that String.
	 *
	 * @param key String key to locate String value with
	 * @param o1 An Object whose toString() results replace the token
	 * @return The token-replaced String
	 */	
	public String getAndFormat(String key, Object o1)
	{
		return getAndFormat(key, new Object[] { o1} );
	}
        
	
	/** Get the String from the member IStringSource object -- then call
	 * <b>format</b> using that String.
	 *
	 * @param key String key to locate String value with
	 * @param o1 An Object whose toString() results replace the token
	 * @param o2 An Object whose toString() results replace the token
	 * @return The token-replaced String
	 */	
	public String getAndFormat(String key, Object o1, Object o2)
	{
		return getAndFormat(key, new Object[] { o1, o2} );
	}
        
	
	/** Get the String from the member IStringSource object -- then call
	 * <b>format</b> using that String.
	 *
	 * @param key String key to locate String value with
	 * @param o1 An Object whose toString() results replace the token
	 * @param o2 An Object whose toString() results replace the token
	 * @param o3 An Object whose toString() results replace the token
	 * @return The token-replaced String
	 */	
	public String getAndFormat(String key, Object o1, Object o2, Object o3)
	{
		return getAndFormat(key, new Object[] { o1, o2, o3} );
	}
        
	
	/** Get the String from the member IStringSource object -- then call
	 * <b>format</b> using that String.
	 *
	 * @param key key String to locate value String with
	 * @param toInsert An ordered array of Objects to replace the tokens with
	 * @return The located String with token substitution
	 */	
	public String getAndFormat(String key, Object[] toInsert)
	{
		return format(getString(key), toInsert);
	}
		

	private static String makeToken(int num)
	{
            /* this is the one and only place where the specifics of how tokens
             * are represented are kept.
             * It would have been easy (and nice!) to change this token 
             * programmatically.  But unfortunately, impossible, because we
             * have static methods
             */
            return "{" + num + "}";
	}
	
	
	private static String replaceToken(String s, String token, String replace)
	{
            /* look for the token, 'token', inside the String, 's', and replace
             * with the String, 'replace'
             */
            
            if(s == null || s.length() <= 0 || token == null || token.length() <= 0)
			return s;
		
		int index = s.indexOf(token);

		if(index < 0)
			return s;

		int tokenLength = token.length();
		String ret = s.substring(0, index);
		ret += replace;
		ret += s.substring(index + tokenLength);

		return ret;
	}
	
	
	private	IStringSource mSource = null;

	/**
         * TEMPORARY -- until unit testing code is created...
	 * @param notUsed  */	
	public static void main(String[] notUsed)
	{
		String test2A = "hello {1}, How are {2}?";
		String test2AResult = format(test2A, "Carbon-based lifeform", "you");
		Debug.println("INPUT:  " + test2A + "\nOUTPUT:  " + test2AResult);
	}
}


