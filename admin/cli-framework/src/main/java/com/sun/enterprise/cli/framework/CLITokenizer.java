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
 *  $Id: CLITokenizer.java,v 1.3 2005/12/25 03:46:55 tcfujii Exp $
 */

package com.sun.enterprise.cli.framework;

import java.util.ListIterator;

/**
 * This CLI Tokenizer class allows CLI command to break strings into tokens.
 * The tokenizer checks for the escape characters and the quotes to determine 
 * the tokens.
 * Consider the following examples:
 * <li> 
 *   string is <code>name1=value1:name2=value2</code> and the delimiter is :
 *   CLI tokenizer will tokenized the string to:
 *   <blockquote><pre>
 *   name1=value1
 *   name2=value2
 *   </pre></blockquote>
 * </li> 
 * <li> 
 *   string is <code>name1=abc\:def:name2=value2</code> and the delimiter is :
 *   CLI tokenizer will tokenized the string to:
 *   <blockquote><pre>
 *   name1=abc:def
 *   name2=value2
 *   </pre></blockquote>
 *   notice that abc\:def is not tokenized since it contains an escape character
 *   before the :.
 * </li> 
 * <li> 
 *   string is <code>name1="abc:def":name2=value2</code> and the delimiter is :
 *   CLI tokenizer will tokenized the string to:
 *   <blockquote><pre>
 *   name1=abc:def
 *   name2=value2
 *   </pre></blockquote>
 *   notice that "abc:def" is not not tokenized since it's in the quotes
 * </li> 
 * <li> 
 *   string is <code>name1="abc\:def":name2=value2</code> and the delimiter is :
 *   CLI tokenizer will tokenized the string to:
 *   <blockquote><pre>
 *   name1=abc\:def
 *   name2=value2
 *   </pre></blockquote>
 * </li> 
 * @author  Jane Young
 */
public class CLITokenizer 
{
    private final static char    ESCAPE_CHAR  = '\\';
    private final static String  QUOTE_STRING = "\"";
    private int size = 0;
    private ListIterator tokenIterator = null;


    /**
     *  constructor that calls popluateList to create the tokeIterator
     *  and size variables.
     *  @param stringToken - the string to tokenize.
     *  @param delimiter - the delimiter to tokenize.
     */
    public CLITokenizer(String stringToken, String delimiter)
        throws CommandException
    {
        if (!checkForMatchingQuotes(stringToken))
            throw new CommandException(getLocalizedString("UnclosedString"));

        if (stringToken != null && delimiter != null)
            tokenIterator = populateList(stringToken, delimiter);
        else
            throw new CommandException(getLocalizedString("CouldNotCreateCliTokenizer"));
    }

    /**
     *  returns the number of tokens in the string.
     *  @return number of tokens
     */
    public int countTokens()
    {
        return size;
    }

    /**
     *  returns true is there are more token in the list
     *  @return true if there are more tokens else false.
     */
    public boolean hasMoreTokens()
    {
        return tokenIterator.hasNext();
    }

    /**
     *  returns the token string without the  escape characters
     *  @return the next string token without the escape characters.
     */
    public String nextTokenWithoutEscapeAndQuoteChars() throws CommandException
    {
        final String strWOEscape = removeEscapeChars((String)tokenIterator.next());
        final String strWOQuotes = removeQuoteChars(strWOEscape);
        return removeEscapeCharsFromQuotes(strWOQuotes);
    }


    /**
     *  returns the next token string
     *  @return the next string token 
     */
    public String nextToken() throws CommandException
    {
        return (String)tokenIterator.next();
    }


    /**
     *  This method will check for matching quotes.  If quotes do not match then
     *  return false else return true.
     *  @param str - string to check for matching quotes
     *  @return boolean - true if quotes match else false.
     */
    private boolean checkForMatchingQuotes(String str) throws CommandException
    {
        //get index of the first quote in the string
        int beginQuote = getStringDelimiterIndex(str, QUOTE_STRING, 0);

        while (beginQuote != -1)
        {
            int endQuote = getStringDelimiterIndex(str, QUOTE_STRING, beginQuote+1);
            if (endQuote == -1) return false;
            beginQuote = getStringDelimiterIndex(str, QUOTE_STRING, endQuote+1);
        }
        return true;
    }


    /**
     *  this methos calls the getStringDelimiterIndex to determine the index
     *  of the delimiter and use that to populate the tokenIterator.
     *  @param strToken - string to tokenize
     *  @param delimiter - delimiter to tokenize the string
     *  @return ListIterator
     */
    private ListIterator populateList(String strToken, String delimiter)
        throws CommandException
    {
        java.util.List tokenList = new java.util.Vector();
        int endIndex = getStringDelimiterIndex(strToken, delimiter, 0);
        if (endIndex == -1) tokenList.add(strToken);
        else
        {
            int beginIndex = 0;
            while (endIndex > -1)
            {
                    //do not want to add to the list if the string is empty
                if (beginIndex != endIndex)
                    tokenList.add(strToken.substring(beginIndex, endIndex));
                beginIndex = endIndex + 1;
                endIndex = getStringDelimiterIndex(strToken, delimiter, beginIndex);
            }
                //do not want to add to the list if the begindIndex is the last index
            if (beginIndex != strToken.length())
                tokenList.add(strToken.substring(beginIndex));
        }
        size = tokenList.size();
        try {
            return tokenList.listIterator();
        }
        catch (java.lang.IndexOutOfBoundsException ioe) {
            throw new CommandException(ioe);
        }
    }


    /**
     * Removes the escape characters from the property value
     * @param strValue - string value to remove the escape character
     * @return the string with escape character removed
     */
    private String removeEscapeChars(String strValue)
        throws CommandException
    {
        int prefixIndex = 0;
        java.lang.StringBuffer strbuff = new java.lang.StringBuffer();

        while (prefixIndex < strValue.length())
        {
            int delimeterIndex = getStringDelimiterIndex(strValue,
                                                         String.valueOf(ESCAPE_CHAR), prefixIndex);
            if (delimeterIndex == -1)
            {
                strbuff.append(strValue.substring(prefixIndex));
                break;
            }

            //if a quote is follow by an esacpe then keep the escape character
            if (delimeterIndex+1 < strValue.length() &&
                String.valueOf(strValue.charAt(delimeterIndex+1)).equals(QUOTE_STRING))
                strbuff.append(strValue.substring(prefixIndex, delimeterIndex+1));
            else
                strbuff.append(strValue.substring(prefixIndex, delimeterIndex));
            
            prefixIndex = delimeterIndex+1;
        }
        return strbuff.toString();
    }

    /**
     * Removes escape characters that precedes quotes
     * @param strValue - the string value to remove the escape characters
     * @return string value with escape characters removed
     */
    private String removeEscapeCharsFromQuotes(String strValue) throws CommandException
    {
        int prefixIndex = 0;
        java.lang.StringBuffer strbuff = new java.lang.StringBuffer();

        while (prefixIndex < strValue.length())
        {
            int delimeterIndex = strValue.indexOf(String.valueOf(ESCAPE_CHAR), prefixIndex);
            if (delimeterIndex == -1)
            {
                strbuff.append(strValue.substring(prefixIndex));
                break;
            }
            //if a quote is follow by an esacpe then remove the escape character
            if (String.valueOf(strValue.charAt(delimeterIndex+1)).equals(QUOTE_STRING))
                strbuff.append(strValue.substring(prefixIndex, delimeterIndex));
            else
                strbuff.append(strValue.substring(prefixIndex, delimeterIndex+1));
            
            prefixIndex = delimeterIndex+1;
        }
        return strbuff.toString();
    }


    /**
     * Removes the quote characters from the property value
     * @return string value with quotes removed
     */
    private String removeQuoteChars(String strValue)
        throws CommandException
    {
        int prefixIndex = 0;
        java.lang.StringBuffer strbuff = new java.lang.StringBuffer();

        while (prefixIndex < strValue.length())
        {
            int delimeterIndex = getStringDelimiterIndex(strValue,
                                                         QUOTE_STRING, prefixIndex);
            if (delimeterIndex == -1)
            {
                strbuff.append(strValue.substring(prefixIndex));
                break;
            }
            strbuff.append(strValue.substring(prefixIndex, delimeterIndex));
            prefixIndex = delimeterIndex+1;
        }
        return strbuff.toString();
    }


    /** 
     *  This method returns the index of the delimiter.  It will factor out the
     *  escape and quote characters.
     *  @param strToken - string to token
     *  @param delimiter - the delimiter to tokenize
     *  @param fromIndex - the index to start the tokenize
     *  @return index - index of the delimiter in the strToken
     *  @throw CommandException if the end quote do not match.
     */
    private int getStringDelimiterIndex(String strToken, String delimiter,
                                        int fromIndex)
        throws CommandException
    {
        if (fromIndex > strToken.length()-1) return -1;
        
            //get index of the delimiter
        final int hasDelimiter = strToken.indexOf(delimiter, fromIndex);

            //get index of the first quote in the string token
        final int quoteBeginIndex = strToken.indexOf(QUOTE_STRING, fromIndex);

            // ex: set server.ias1.jdbcurl="jdbc://oracle"
            // if there's is a quote and a delimiter, then find the end quote
        if ((quoteBeginIndex != -1) && (hasDelimiter != -1) &&
            (quoteBeginIndex < hasDelimiter))
        {
            //get index of the end quote in the string token
            final int quoteEndIndex = strToken.indexOf(QUOTE_STRING, quoteBeginIndex+1);
            
            if (quoteEndIndex == -1)
                throw new CommandException(getLocalizedString("UnclosedString"));
            if (quoteEndIndex != (strToken.length()-1))
            {
                return getStringDelimiterIndex(strToken, delimiter, quoteEndIndex + 1);
            }
            else
            {
                return -1;
            }
        }
        if ((hasDelimiter > 0) && (strToken.charAt(hasDelimiter-1) == ESCAPE_CHAR))
        {
            return getStringDelimiterIndex(strToken, delimiter, hasDelimiter+1);
        }
        else
        {
            return hasDelimiter;
        }
    }


    /** 
     *  returns the localized string from the properties file as defined in the 
     *  CommandProperties element of CLIDescriptor.xml file
     *  Calls the LocalStringsManagerFactory.getCommandLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @return key value
     */
    private String getLocalizedString(String key)
    {
        LocalStringsManager lsm = null;
        try
        {
            lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
        return lsm.getString(key);
    }


    public static void main(String[] args) 
    {
        try {
            final CLITokenizer ct = new CLITokenizer(args[0], ":");
            while (ct.hasMoreTokens()) {
                final String nameAndvalue = ct.nextToken();
                final CLITokenizer ct2 = new CLITokenizer(nameAndvalue, "=");
                System.out.println("+++++ ct2 tokens = " + ct2.countTokens() + " +++++");
                if (ct2.countTokens() == 1)
                {
                    System.out.println(ct2.nextTokenWithoutEscapeAndQuoteChars());
                }
                else if (ct2.countTokens() == 2)
                {
                    System.out.println(ct2.nextTokenWithoutEscapeAndQuoteChars() + "  " +  
                                       ct2.nextTokenWithoutEscapeAndQuoteChars());
                }
                System.out.println("+++++ " + nameAndvalue + " +++++");
            }
            System.out.println("***** the end *****");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }



}
