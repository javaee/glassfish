package org.glassfish.embed.util;

/* WBN Valentine's Day, 2000 -- place for handy String utils.
 */

import java.util.*;
import java.util.ArrayList;
import java.util.Vector;
import java.sql.SQLException;

public class StringUtils
{
    public static final String EOL = System.getProperty("line.separator");
    
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

	public static int maxWidth(Collection<String> v)
	{
        if(v == null || v.size() <= 0)
            return 0;

        return maxWidth(v.toArray(new String[v.size()]));
	}

	////////////////////////////////////////////////////////////////////////////

	public static int maxWidth(String[] ss)
	{
        if(ss == null || ss.length <= 0)
            return 0;
		// find longest String in a Collection of Strings...
		int max = 0;
        
		for(String s : ss)
		{
            if(s == null)
                continue;
            
			int len = s.length();

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

}
