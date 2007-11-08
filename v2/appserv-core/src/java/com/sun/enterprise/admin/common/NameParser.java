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

package com.sun.enterprise.admin.common;

import java.util.Iterator;
import java.util.Vector;
import java.util.Stack;
import java.util.EmptyStackException;

// i18n import
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

/**
    A Class that parses the given Character String as per NameSpace Grammar.
    Implemented using a StringBuffer of valid tokens. Uses a two pass technique in
    which first pass does all the validity checks and second pass actually
    segragates the given String into a collection of NameParts delimited by
    the delimiter character from Tokens class.
    @see    Tokens
*/

public class NameParser
{
    private String          mString         =       null;
    private Vector          mNameParts      =       null;

	// i18n SOMLocalStringsManager
	private static final SOMLocalStringsManager localizedStrMgr =
		SOMLocalStringsManager.getManager( NameParser.class );

    /**
        Default constructor for initialization purpose.
    */

    public NameParser()
    {
        mNameParts          =       new Vector();
    }


    /**
        A method in the public interface of this class which initializes
        the parsing process.
        <p>
        @param the string to be parsed. Should be non null.
        <p>
        @throws MalformedNameException, if the string is not per grammer
    */

    public void parseIt(String parseString) throws MalformedNameException
    {
        //ArgChecker.check(parseString != null, "null string to parse");
        mString         =       parseString;
        parseStringForNameParts();
        parseNameParts();
        if(! isWildcardCharValid())
        {

			String msg = localizedStrMgr.getString( "admin.common.invalid_wild-card_char_placement" );
            throw new MalformedNameException( msg );
        }
    }


    /**
        Returns the iterator for all the name-parts, for the parsed string.
        Guaranteed to return a non null iterator even if there are no elements
        in it.

        @return iterator to iterate through the string representations of
                name-parts of a name string
    */

    public Iterator getParts()
    {
        return ( mNameParts.iterator() );
    }


    
    private String removeEscapes(String str)
    {
        int idx;
        while((idx=str.indexOf(Tokens.kEscapeChar))>=0)
            if(idx==0)
                str = str.substring(1);
            else
                str = str.substring(0, idx)+str.substring(idx+1);
        return str;
               
    }
    /**
        Segregates the string to be parsed into the vector of name-parts.
        Each Name-part is delimited by a delimiter character defined in
        the Tokens class. This method while segregating the name-parts,
        determines whether any scanned single character is valid.

        @throws MalformedNameException if the namestring is invalid
    */
    private void parseStringForNameParts() throws MalformedNameException
    {
        int         counter         =          0;
        int         begin           =          counter;
        String      nameString      =          null;

        while(counter < mString.length())
        {
            char parseChar = mString.charAt(counter);
            if(isValidChar(parseChar))
            {
                boolean gotDelimiter =  isDelimiterChar(mString, counter);
                if(gotDelimiter)
                {
                    nameString = mString.substring(begin, counter);
                    begin             = counter + 1;
                    mNameParts.addElement(removeEscapes(nameString));
                    //Debug.println("a name string: " + nameString);
                }
            }
            else
            {
				String msg = localizedStrMgr.getString( "admin.common.invalid_char_encountered", new String( parseChar + "" ) );
                throw new MalformedNameException( msg );
            }
            counter++;
        }
        nameString = mString.substring(begin);
        mNameParts.addElement(removeEscapes(nameString));
        //Debug.println("a name string: " + nameString);
    }


    /**
        Method to parse each individual name-part in the name.

        @throws MalformedNameException if any name-part is invalid.
    */

    private void parseNameParts() throws MalformedNameException
    {
        Iterator partsIter  =   getParts();
        boolean canReduce   =   false;

        while(partsIter.hasNext())
        {
            String aNamePartString      =   (String) partsIter.next();
            canReduce                   =   reduceNamePart(aNamePartString);
            if(! canReduce)
            {
				String msg = localizedStrMgr.getString( "admin.common.invalid_name", mString );
                throw new MalformedNameException( msg );
            }
        }
    }


    /**
        Determines whether the wild-card character is per grammar.
        A wild-card character is permissible in name, if and only if
        <li>
            it is a name-part which has no other char with it &&
        <li>
            it is the last name-part.
        <p>
        It is valid to have no wildcard character at all.
    */

    private boolean isWildcardCharValid()
    {
        boolean isWildcardCharValid =   true;
        String  starString          =   new String(new char[]{Tokens.kWildCardChar});

        /*
            if Any name-part contains wild-card char, then that name-part
            should be the only thing that name-part. 
        */
        for(int i = 0 ; i < mNameParts.size(); i++)
        {
            String aPart    =   (String) mNameParts.elementAt(i);
            if(aPart.indexOf(Tokens.kWildCardChar) != -1 &&
               ! aPart.equals(starString))
            {
                isWildcardCharValid =   false;
                break;
            }
        }
        
        return isWildcardCharValid;
    }

    /**
        Determines whether given character is valid as a single character.

        @returns true if argument is valid character, false otherwise
    */

    private boolean isValidChar(char aChar)
    {
        return ( Character.isLetter(aChar)    ||
                 Character.isDigit(aChar)     ||
                 this.isPermissibleChar(aChar)||
                 this.isSpecialChar(aChar)
                );

    }


    /**
        There are certain characters that are neither letters nor digits but are
        allowed in names and this method determines whether the given character
        is such a permissible special character.
        <p>
        @param a character
        @return true if the argument is allowed special character, false otherwise
    */

    private boolean isPermissibleChar(char aChar)
    {
        boolean isPermissibleChar     = false;

        if (aChar   ==  Tokens.kSubScriptBeginnerChar       ||
            aChar   ==  Tokens.kSubScriptEnderChar          ||
            aChar   ==  Tokens.kDelimiterChar               ||
            aChar   ==  Tokens.kEscapeChar                  ||
            aChar   ==  Tokens.kWildCardChar)
        {
            isPermissibleChar     =   true;
        }
        return isPermissibleChar;
    }


    /**
        Determines whether the given character is one of Tokens.kSpecialsString.

        @return true if the character is special, false otherwise
    */

    private boolean isSpecialChar(char aChar)
    {
        return ( Tokens.kSpecialsString.indexOf(aChar) != -1 );
    }


    /**
        Returns if given character is a non-zero digit (1..9)
    */

    private boolean isNonZeroDigit(char aChar)
    {
        return ( Tokens.kNonZeroDigitsString.indexOf(aChar) != -1 );
    }


    /**
        The actual method that does parsing and determines whether the
        sequence of characters in given string is according
        to the grammar.
    */

    private boolean reduceNamePart(String npString)
    {
        boolean canReduce = true;

        if (isSubscriptPresent(npString))
        {
            canReduce = isSubscriptValid(npString);
        }
        if (canReduce)
        {
            String subscriptLessString = removeSubscript(npString);
            canReduce  = isSubscriptLessStringValid(subscriptLessString);
        }
        return ( canReduce );
    }


    /**
        A method to determine whether character at given position in given
        string is Tokens.kDelimiterChar  && is acting as a delimiter.
        <p>
        A  Tokens.kDelimiterChar is delimiter if and only if it is
        <it> not escaped </it> with Tokens.kEscapeChar just before it.
        <p>
        @param aString and the position of a character which needs to be tested
            for being delimiter
        @param position integer position that denotes a delimiter character
        @return boolean true if such character is delimiter, false otherwise
    */

    private boolean isDelimiterChar(String aString, int position)
    {
        boolean isDelim     =       false;

        //Assert.assertRange(position, -1, aString.length(), "invalid position");

        if(aString.charAt(position) == Tokens.kDelimiterChar)
        {
            if(position == 0                                    ||
               aString.charAt(position - 1) != Tokens.kEscapeChar
               )
            {
                isDelim         =       true;
            }
        }
        return ( isDelim );
    }


    /**
        A method to determine whether given string reprensents a valid
        index. An index is invalid if either of the following is true
        <li> it is negative, OR
        <li> it is zero and length of the string is more than 1, OR
        <li> it is greater than zero and its first digit is 0
        <p>
        In all other cases the string represents a valid index.

        @param index string representing the index
    */

    private boolean isValidIndexString(String index)
    {
        boolean isValidIndex = true;

        if (index != null && index.length() > 0)
        {
            try
            {
                int intValue = Integer.parseInt(index);
                if((intValue == 0 && index.length() != 1)                       ||
                   (intValue  > 0 && index.charAt(0) == Tokens.kZeroDigitChar)  ||
                   (intValue < 0)
                   )
                {
                    isValidIndex = false;
                }
            }
            catch(NumberFormatException e)
            {
                //ExceptionUtil.ignoreException(e);
                isValidIndex = false;
            }
        }
        else
        {
            isValidIndex = false;
        }

        return ( isValidIndex );
    }


    /**
        Determines whether any subscript character (Tokens.kSubscriptBeginnerChar)
        or (Tokens.kSubscriptEnderChar) is  present.

        @param npString the string to be tested for subscript characters

        @return true if argument contains either subscript character, false otherwise
    */

    private boolean isSubscriptPresent(String npString)
    {
        boolean subscriptPresent        =       false;

        if(npString.indexOf(Tokens.kSubScriptBeginnerChar) != -1 ||
           npString.indexOf(Tokens.kSubScriptEnderChar)    != -1
           )
        {
            subscriptPresent    =   true;
        }
        return ( subscriptPresent );
    }


    /**
        Checks whether the string present inside  []
        can be a valid index.
        Only non-negative integers are valid with some exceptions. e.g. 32 is a
        valid index, but 03 or 004 is not.
        <p>
        In other words this method does following checks:
        <li>
            The subscript characters are ordered in given string
            #isSubscriptOrdered(java.lang.String)
        <li>
            The contents of string between first index of Tokens.kSubscriptBeginnerChar and
            Tokens.kSubScriptEnderChar evaluates to a permissible integer value.
            #isValidIndexString(java.lang.String)
        <li>
            if there is a subscript, it is always at the end of the string, as
            abc[5]d is invalid.
        <p>

        @param  npString any string
        @return true if the string contains a valid subscript, false otherwise
    */

    private boolean isSubscriptValid(String npString)
    {
        boolean subscriptValid  =   true;

        boolean subscriptOrdered  =   isSubscriptOrdered(npString);
        if(subscriptOrdered)
        {
            int leftPos     =   npString.indexOf(Tokens.kSubScriptBeginnerChar);
            int rightPos    =   npString.lastIndexOf(Tokens.kSubScriptEnderChar);

            String indexString = npString.substring(leftPos + 1, rightPos);
            if(! isValidIndexString(indexString))
            {
                subscriptValid   =   false;
            }
            boolean lastCharIsRightSquareBracket =
                npString.charAt(npString.length() - 1) == Tokens.kSubScriptEnderChar;

            if(! lastCharIsRightSquareBracket)
            {
                subscriptValid  =    false;
            }
        }
        else
        {
            subscriptValid  = false;
        }

        return ( subscriptValid );
    }

    /**
        Simple stack based implementation to test whether the subscripts
        are in order. e.g. [], [][], [[]] are all valid orders and ][, [[], []]
        are all invalid orders.
        <p>
        Note that if the string passed does not contain
        any subscript character i.e. a string without '[' or ']', then this
        method returns false.

        @param npString any string
        @return true if the subscripts are ordered, false otherwise
    */

    private boolean isSubscriptOrdered(String npString)
    {
        boolean     subscriptOrdered        =   true;
        int         index                   =   0;
        Stack       charStack               =   new Stack();

        if(isSubscriptPresent(npString))
        {
            while(index < npString.length())
            {
                char ch         =   npString.charAt(index);
                if(ch == Tokens.kSubScriptBeginnerChar)
                {
                    charStack.push(new Character(ch));
                }
                else if(ch == Tokens.kSubScriptEnderChar)
                {
                    if(! charStack.empty())
                    {
                        Character poppedChar =  (Character)charStack.pop();
                        if(poppedChar.charValue() != Tokens.kSubScriptBeginnerChar)
                        {
                            subscriptOrdered = false;
                            break;
                        }
                    }
                    else
                    {
                        subscriptOrdered = false;
                        break;
                    }
                }
                index++;
            }
            if(! charStack.empty())
            {
                subscriptOrdered = false;
            }
        }
        else
        {
            subscriptOrdered = false;
        }

        return ( subscriptOrdered );
    }

    /**
        Returns a string that contains anything in given string upto first
        index of Tokens.SubscriptBeginnerChar(that character excluded). If at all
        this method gets called, it should be made sure that the subscript
        characters are ordered, as this method does not make that check.

        @param npString a String
        @return a string that contains subset of given string upto Tokens.SubscriptBeginnerChar.
            Returns null if null string is passed. Returns null string for "[]".
            Returns the same string if no subscript characters are present.
    */

    private String removeSubscript(final String npString)
    {
        String  subscriptLessString = null;
        int     leftIndex           = npString.indexOf(Tokens.kSubScriptBeginnerChar);

        if ( npString.length() > 0 )
        {
            if(leftIndex != -1)
            {
                subscriptLessString = npString.substring(0, leftIndex);
            }
            else
            {
                subscriptLessString = npString;
            }
        }
        return ( subscriptLessString );
    }


    /**
        A method to validate the string that contains no subscript characters,
        i.e. a String that contains neither Tokens.kSubscriptBeginnerChar nor
        Tokens.kSubscriptEnderChar.
        <p>
        @return true if any subscript character is present in given string, false
            otherwise
    */

    private boolean isSubscriptLessStringValid(String npString)
    {
        boolean remStringValid      =   false;

        if(npString != null && npString.length() > 0)
        {
            //boolean noMoreDotsPresent =   (npString.indexOf(Tokens.kMoreEscapedDelimitersString) == -1);

            //boolean endsWithDelimiter = npString.endsWith(Tokens.kEscapedDelimiterString);

            boolean onlyDelimiterEscaped    =   isOnlyDelimiterEscaped(npString);
            boolean containsEscape          =   npString.indexOf(Tokens.kEscapeChar) != -1;
            boolean isEscapeValid           =   ! containsEscape ||
                                                containsEscape && onlyDelimiterEscaped;

            boolean noMoreStars             = npString.indexOf(Tokens.kMoreWildCardsString) == -1;

            if( isEscapeValid                   &&
                noMoreStars
              )
            {
                remStringValid  =   true;
            }
        }
        return ( remStringValid );
    }


    /**
        Checks whether all the escape characters escape only delimiters.
        Method returns true if and only if
        <li> string contains at least one escape character &&
        <li> all escape charactrs are always followed by a delimiter character.
        <p>
        Returns false, in all other cases.
        <p>
        @param npString any string
        @return true if all the delimiters are escaped, false otherwise
    */

    private boolean isOnlyDelimiterEscaped(String npString)
    {
        boolean onlyDelimiterEscaped     =   true;

        if(npString != null && npString.length() > 0)
        {
            int index       =   0;
            int strlength   =   npString.length();

            while(index < strlength)
            {
                char ch = npString.charAt(index);
                if(ch == Tokens.kEscapeChar)
                {
                    int nextIndex = index + 1;
                    if (nextIndex >= strlength           ||
                        npString.charAt(nextIndex) != Tokens.kDelimiterChar)
                    {
                        onlyDelimiterEscaped = false;
                        break; // no need to continue as at least one occurrance found
                    }
                }
                index++;
            }
        }
        else
        {
            onlyDelimiterEscaped    =   false;
        }
        return ( onlyDelimiterEscaped );
    }

}
