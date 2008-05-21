/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.universal;

/* WBN Valentine's Day, 2000 -- place for handy String utils.
 */

import java.util.ArrayList;
import java.util.Vector;
import java.sql.SQLException;

public class StringUtils
{
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String EOL = NEWLINE;
    
	private StringUtils()
	{
		
	}

	////////////////////////////////////////////////////////////////////////////

	public static String formatSQLException(SQLException ex)
	{
		//Assertion.check(ex);

		String s = "SQLException:\n";

		do
		{
			s += "SQLState: " + ex.getSQLState() + "\n";
			s += "Message:  " + ex.getMessage()  + "\n";
			s += "Vendor:   " + ex.getErrorCode()+ "\n";
			s += "\n";
		}while( (ex = ex.getNextException()) != null);

    	return s;
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

	public static String padLeft(String s, int len, char c)
	{
		String ss = "";

		if(s == null || s.length() >= len)
			return s;

		for(int i = len - s.length(); i > 0; --i)
			ss += c;

		return ss + s;
	}

	////////////////////////////////////////////////////////////////////////////

	public static String padLeft(String s, int len)
	{
        return padLeft(s, len, ' ');
    }

	////////////////////////////////////////////////////////////////////////////

	public static String[] toLines(String s)
	{
		if(s == null)
			return new String[0];

		ArrayList<String> list = new ArrayList<String>();

		int start	= 0;
		int end		= 0;

		for(end = s.indexOf('\n', start); end >= 0 && start < s.length(); end = s.indexOf('\n', start))
		{
			list.add(s.substring(start, end));	// does NOT include the '\n'
			start = end + 1;
		}

		if(start < s.length())
			list.add(s.substring(start));

		String[] ss = new String[list.size()];

		list.toArray(ss);

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

	public static boolean isAllWhite(String s)
	{
        if(s == null)
            return true;
        
        int len = s.length();
        
        for(int i = 0; i < len; i++)
        {
            char c = s.charAt(i);
     
            if(!Character.isWhitespace(c))
                return false;
        }
        
        return true;
    }

	////////////////////////////////////////////////////////////////////////////
	

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
        
	public static boolean ok(String s) 
	{
		return s != null && s.length() > 0;
	}	
   /**
     * Removes the quoting around a String.
     * @param s The String that may have enclosing quotes
     * @return The String resulting from removing the enclosing quotes
     */
    public static String removeEnclosingQuotes(String s)
    {
        if(s == null)
            return null;

        if(isDoubleQuoted(s) || isSingleQuoted(s)) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static boolean isDoubleQuoted(String s) {
        return s.startsWith("\"") && s.endsWith("\"") && s.length() > 1;
    }

    private static boolean isSingleQuoted(String s) {
        return s.startsWith("'") && s.endsWith("'") && s.length() > 1;
    }
}
