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

package com.sun.enterprise.backup.util;

/* WBN Valentine's Day, 2000 -- place for handy String utils.
 */

import java.util.*;

public class StringUtils
{
	private StringUtils()
	{
		
	}

	////////////////////////////////////////////////////////////////////////////

	/**
	 * return the length of the String - or 0 if it's null
	 */
	public static int safeLength(String s)
	{
		if(s == null)
			return 0;
		
		return s.length();
	}

	////////////////////////////////////////////////////////////////////////////

	public static boolean ok(String s)
	{
		return s != null && s.length() > 0;
	}

	////////////////////////////////////////////////////////////////////////////

	public static int maxWidth(Vector v)
	{
		// find longest String in a vector of Strings...
		int max = 0;

		if(v == null || v.size() <= 0 || !(v.elementAt(0) instanceof String))
			return 0;

		for(int i = v.size() - 1; i >= 0; i--)
		{
			int len = ((String)v.elementAt(i)).length();

			if(len > max)
				max = len;
		}

		return max;
	}

	////////////////////////////////////////////////////////////////////////////

	public static boolean isHex(String s)
	{
		// is this the String representation of a valid hex number?
		// "5", "d", "D", "F454ecbb" all return true...
		// p.s. there MUST be a better and faster way of doing this...

		final int slen = s.length();

		for(int i = 0; i < slen; i++)
			if(isHex(s.charAt(i)) == false)
				return false;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////

	public static boolean isHex(char c)
	{
		// is this the char a valid hex digit?

		String hex = "0123456789abcdefABCDEF";
		int	hexlen = hex.length();

		for(int i = 0; i < hexlen; i++)
			if(hex.charAt(i) == c)
				return true;

		return false;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String getPenultimateDirName(String s)
	{
		// e.g.  input: "a/b/c/d/foobar.txt"   output: "d"

		if(s == null || s.length() <= 0)
			return s;

		// must be a plain file name -- return empty string...
		if( (s.indexOf('/') < 0) && (s.indexOf('\\') < 0) )
			return "";

		s = s.replace('\\', '/');	// make life easier for the next steps...

		int index = s.lastIndexOf('/');

		if(index < 0)
			return "";	// can't happen!!!

		s = s.substring(0, index);	// this will truncate the last '/'

		index = s.lastIndexOf('/');

		if(index >= 0)
			s = s.substring(index + 1);

		return s;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String toShortClassName(String className)
	{
		int index = className.lastIndexOf('.');

		if(index >= 0 && index < className.length() - 1)
			return className.substring(index + 1);

		return className;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String padRight(String s, int len)
	{
		if(s == null || s.length() >= len)
			return s;

		for(int i = len - s.length(); i > 0; --i)
			s += ' ';

		return s;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String padLeft(String s, int len)
	{
		String ss = "";

		if(s == null || s.length() >= len)
			return s;

		for(int i = len - s.length(); i > 0; --i)
			ss += ' ';

		return ss + s;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String[] toLines(String s)
	{
		if(s == null)
			return new String[0];

		Vector<String> v = new Vector<String>();

		int start	= 0;
		int end		= 0;

		for(end = s.indexOf('\n', start); end >= 0 && start < s.length(); end = s.indexOf('\n', start))
		{
			v.addElement(s.substring(start, end));	// does NOT include the '\n'
			start = end + 1;
		}

		if(start < s.length())
			v.addElement(s.substring(start));

		String[] ss = new String[v.size()];

		v.copyInto(ss);

		return ss;
	}

	////////////////////////////////////////////////////////////////////////////

	public static void prepend(String[] ss, String what)
	{
		for(int i = 0; i < ss.length; i++)
			ss[i] = what + ss[i];
	}

	////////////////////////////////////////////////////////////////////////////

	public static String UpperCaseFirstLetter(String s)
	{
		if(s == null || s.length() <= 0)
			return s;
		
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	////////////////////////////////////////////////////////////////////////////

	public static String replace(String s, String token, String replace)
	{
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

	////////////////////////////////////////////////////////////////////////////

	public static String toString(Properties props)
	{
		if(props == null || props.size() <= 0)
			return "No entries";
		
		Set entries = props.entrySet();
		StringBuffer sb = new StringBuffer();
		
		// first -- to line things up nicely -- find the longest key...
		int keyWidth = 0;
		for(Iterator it = entries.iterator(); it.hasNext(); )
		{
			Map.Entry	me	= (Map.Entry)it.next();
			String		key	= (String)me.getKey();
			int			len = key.length();
		
			if(len > keyWidth)
				keyWidth = len;
		}
		
		++keyWidth;
		
		// now make the strings...
		for(Iterator it = entries.iterator(); it.hasNext(); )
		{
			Map.Entry	me	= (Map.Entry)it.next();
			String		key	= (String)me.getKey();
			String		val	= (String)me.getValue();

			sb.append(padRight(key, keyWidth));
			sb.append("= ");
			sb.append(val);
			sb.append('\n');
		}
		
		return sb.toString();
	}
	

	//  Test Code...

	public static void main(String[] args)
	{
		final int len = args.length;

		if((len == 1) && args[0].equalsIgnoreCase("toLine"))
			testToLine();
		else if((len > 1) && args[0].equalsIgnoreCase("isHex"))
			testHex(args);
		else
			usage();
	}

	////////////////////////////////////////////////////////////////////////////

	private static void usage()
	{
		System.out.println("StringUtils -- main() for testing usage:\n");
		System.out.println("java netscape.blizzard.util.StringUtils toLine");
		System.out.println("java netscape.blizzard.util.StringUtils isHex number1 number2 ...");
	}

	////////////////////////////////////////////////////////////////////////////

	private static void testHex(String[] args)
	{
		System.out.println("StringUtils -- Testing Hex");

		for(int i = 1; i < args.length; i++)
			System.out.println(padRight(args[i], 16) + "  " + (isHex(args[i]) ? "yesHex" : "notHex"));
	}

	////////////////////////////////////////////////////////////////////////////

	private static void testToLine()
	{
		System.out.println("StringUtils -- Testing toLine()");
		String[] ss =
		{
			null,
			"",
			"abc\ndef\n",
			"abc\ndef",
			"abc",
			"abc\n",
			"abc\n\n",
			"q",
			"\n\nk\n\nz\n\n",
			"sd.adj;ld"
		};

		for(int k = 0; k < ss.length; k++)
		{
			String[] s2 = StringUtils.toLines(ss[k]);
			System.out.println("String #" + k + ", Number of Lines:  " + s2.length);

			for(int i = 0; i < s2.length; i++)
				System.out.println(s2[i]);
		}
	}

	
	public static void testUpperCase()
	{
		String[] test = new String[] { "xyz", "HITHERE", "123aa", "aSSS", "yothere" };//NOI18N
		
		for(int i = 0; i < test.length; i++)
		{
			System.out.println(test[i] + " >>> " + UpperCaseFirstLetter(test[i]));//NOI18N
		}
	}
        
	/**
		A utility to get the Operating System specific path from given array
		of Strings.
		@param strings an array of Strings participating in the path.
		@param addTrailing a boolean that determines whether the returned
			String should have a trailing File Separator character. None of
			the strings may be null or empty String. An exception is thrown.
		@return a String that concatenates these Strings and gets a path. Returns
			a null if the array is null or contains no elements.
		@throws IllegalArgumentException if any of the arguments is null or is
			an empty string.
	*/
	
	public static String makeFilePath(String[] strings, boolean addTrailing)
	{
		StringBuffer	path		= null;
		String			separator	= System.getProperty("file.separator");
		if (strings != null)
		{
			path = new StringBuffer();
			for (int i = 0 ; i < strings.length ; i++)
			{
				String element = strings[i];
				if (element == null || element.length () == 0)
				{
					throw new IllegalArgumentException();
				}
				path.append(element);
				if (i < strings.length - 1)
				{
					path.append(separator);
				}
			}
			if (addTrailing)
			{
				path.append(separator);
			}
		}
		return ( path.toString() );
	}

    /**
     * Parses a string containing substrings separated from
     * each other by the standard separator characters and returns
     * a list of strings.
     *
     * Splits the string <code>line</code> into individual string elements 
     * separated by the field separators, and returns these individual 
     * strings as a list of strings. The individual string elements are 
     * trimmed of leading and trailing whitespace. Only non-empty strings
     * are returned in the list.
     *
     * @param line The string to split
     * @return     Returns the list containing the individual strings that
     *             the input string was split into.
     */
    public static List parseStringList(String line)
    {
        return parseStringList(line, null);
    }

    /**
     * Parses a string containing substrings separated from
     * each other by the specified set of separator characters and returns
     * a list of strings.
     *
     * Splits the string <code>line</code> into individual string elements 
     * separated by the field separators specified in <code>sep</code>, 
     * and returns these individual strings as a list of strings. The 
     * individual string elements are trimmed of leading and trailing
     * whitespace. Only non-empty strings are returned in the list.
     *
     * @param line The string to split
     * @param sep  The list of separators to use for determining where the
     *             string should be split. If null, then the standard
     *             separators (see StringTokenizer javadocs) are used.
     * @return     Returns the list containing the individual strings that
     *             the input string was split into.
     */
    public static List<String> parseStringList(String line, String sep)
    {
        if (line == null)
            return null;

        StringTokenizer st;
        if (sep == null)
            st = new StringTokenizer(line);
        else 
            st = new StringTokenizer(line, sep);

        String token;

        List<String> tokens = new Vector<String>();
        while (st.hasMoreTokens())
        {
            token = st.nextToken().trim();
            if (token.length() > 0)
                tokens.add(token);
        }

        return tokens;
    }

}
