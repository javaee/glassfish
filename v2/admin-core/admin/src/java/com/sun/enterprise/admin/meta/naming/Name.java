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

package com.sun.enterprise.admin.meta.naming;

import java.io.Serializable;
import java.util.Vector;
import java.util.Iterator;

import javax.management.MalformedObjectNameException;

/**
    A Class to represent a name in the namespace. The name follows the
    syntactical rules defined in BNF. This class is made Serializable as it is
    very easily done and potentially the Names can be transmitted over wire
    between client and server processes.
    <p>
    Serialization is not mandatory, even if
    a Name is to be shared between remote processes, as the String representation
    of this Class guarantees a full and lossless reproduction. In other words,
    new Name( name.toString() ).equals( name ) always returns <strong> true
    </strong>.
	@author Kedar Mhaswade
	@version 1.0
*/

public final class Name implements Serializable
{
	/* javac 1.3 generated serialVersionUID */
	public static final long serialVersionUID					= 4479037048166596417L;
    public static final     int         kInvalidIndex           = -1;

    private                 String      mString                 = null;
    /*
        A vector of strings of name-parts. Note that if there are
        n delimiters, (n+1) are stored into this vector. In other words, for
        a valid name, this vector contains at least one element.
    */
    private                 Vector      mNameParts              = null;

    /**
        The only constructor to create an instance of Name. If a Name can
        be created from given string representation, it is valid and unique
        in NameSpace. It is to be noted that a Name can represent name of an
        attribute or it can represent a Notation.
        e.g. a.b.c represents a Name, whereas a.b.c.* represents a Notation.
        <p>
        The actual parsing technique is transperant to the client of this class.
        <p>
        @param nameString a string representation of name
        @throws MalformedObjectNameException if the given string represents an illegal
            name
    */

    public Name(String nameString) throws MalformedObjectNameException
    {
        //ArgChecker.check(nameString != null && nameString.length() > 0,
        //                "null string for name");

        NameParser parser   =       new NameParser();
        parser.parseIt(nameString);
        Iterator iter       =       parser.getParts();
        mNameParts          =       new Vector();

        while (iter.hasNext())
        {
            Object element = iter.next();
            mNameParts.addElement(element);
        }

        mString             =   nameString;
    }
    /**
      Returns a Name instance that represents mth part of Name such that,
      0 <= m <= n,
      where n is the number of delimiters (excludig escaped ones)in the string.
      0 is applied for the first part.
      <p>
      e.g. For a name "abc.xy.c[5].d",
      <li>
      name-part with partNum = 0 is "abc".
      <li>
      name-part with partNum = 1 is "xy".
      <li>
      name-part with partNum = 2 is "c[5]" and so on.
      <p>
      if there are no delimiters, then this name itself is returned.
      <p>
      @param    partNum is an integer denoting the name-part.
      @return   instance of Name that represents nth name-part.
    */

    public Name getNamePart(int partNum)
    {
        Name    namePart            =       null;
        String namePartString       =       null;
        int lowerLimit              =       -1;                    //lower limit on index
        int upperLimit              =       mNameParts.size();     //upper limit on index

        //ArgChecker.check(partNum, lowerLimit, upperLimit, "number is invalid");

        namePartString              =   (String)mNameParts.elementAt(partNum);

        try
        {
            namePart                =   new Name(namePartString);
        }
        catch(Exception e)
        {
            //ExceptionUtil.ignoreException(e); // this is actually assertion
        }
        return namePart;
    }


    /**
        Returns the Parent Name of this Name. This method is particularly
        useful in determining location of this Name in hierarchy of Names.
        Following are the rules applied to determine parent of a Name:
        <li>
            a.b.c is always parent of a.b.c.d
        <li>
            a.b.c is always parent of a.b.c[n], where n is any valid index
        <li>
            a.b.c is always parent of a.b.c.*
        <li>
            a.b.c[n] is always the parent of a.b.c[n].d
        <li>
            null is always the parent of any string that does not contain
                any delimiter.

        @return the Name that represents Parent of this Name
    */

    public Name getParent()
    {
        Name parentName     =       null;

        //Assert.assert(! mNameParts.isEmpty(), "Vector of name-parts can't be empty");

        int size            =   mNameParts.size();

        if(size > 1)
        {
            String nameSubstring = createPartialNameString(0, size - 1);
            try
            {
                parentName = new Name(nameSubstring);
            }
            catch(MalformedObjectNameException e)
            {
                //ExceptionUtil.ignoreException(e);
                //this is actually assertion and should never happen
            }
        }
        return ( parentName );
    }


    /**
        Returns the number of name-parts in this name. A valid name contains
        at least one name-part, in which case it is this name itself. Every
        name-part is delimited by Tokens.kDelimiterChar.

        @return number of name-parts in this name
    */

    public int getNumParts()
    {
        return ( mNameParts.size() );
    }

    /**
        A method to determine whether this Name represents an Indexed Name. An
        Indexed name represents a collection of other Names. Each indexed
        Name can refer to other Names using different values for the index.
        An example of an Indexed Name is a.b.c[10].
        Note that a.b[0].c is <it> not </it> an Indexed Name.
        An Indexed Name is different than Name of an Attribute that has multiple
        values. e.g. a.b.c can have 3 values, none of which can be independently
        referred to as e.g. a.b.c[1].

        @return true if this Name represents an Indexed Name, false otherwise
    */

    public boolean isIndexed()
    {
        boolean indexed             =       false;

        /* It is already made sure that the length will be greater than zero */

        if(mString.charAt(mString.length() - 1) == Tokens.kSubScriptEnderChar)
        {
            indexed     =   true;
        }

        return ( indexed );
    }


    /**
        Returns the index beginning at 0 for a Name that is Indexed. It returns
        Name.kInvalidIndex for a name i.e. not indexed.
        (e.g. a.b.c or a.b[0].c, as none of these is indexed).
        In general, index of an indexed name a.b.c[n] is n.
        Calling methods have to compare the return value with Name.kInvalidIndex
    */

    public int getIndex()
    {
        int         index       =       kInvalidIndex;
        String      indexString =       null;
        int         startPos, endPos;

        if (isIndexed())
        {
            /*
                Note that a[3].b[7].c.d[4] has an index of 4 and that's why
                we have to taken last index of '[' and ']'. If the name parses
                correct, then the string between last '[' and last ']' is
                bound to be the index of this name.
            */
            startPos    = mString.lastIndexOf(Tokens.kSubScriptBeginnerChar);
            endPos      = mString.lastIndexOf(Tokens.kSubScriptEnderChar);

            //Assert.assert(endPos > startPos, "this should not be indexed!");

            indexString     =       mString.substring(startPos + 1, endPos);
            try
            {
                index   =   Integer.parseInt(indexString);
            }
            catch(NumberFormatException e)
            {
                //ExceptionUtil.ignoreException(e); // this should NEVER happen
            }
        }
        return index;
    }


    /**
        Returns a string that represents this name. Guarantees to return
        non null String.

        @return String representing the name
    */

    public String toString()
    {
        return ( mString );
    }


    /**
        Checks if the given Object is same as this name. It returns true if
        and only if the argument is non null and represents same character
        sequence as that in this name. It is guaranteed that two objects that
        are equal in this way, will always return equal Strings when toString()
        is called on them.

        @param other the object that this name is to be compared with
        @return true if the objects are equal, false otherwise.
    */

    public boolean equals(Object other)
    {
        boolean isSame      =   false;

        if (other instanceof Name)
        {
            Name otherName = (Name) other;
            if (this.mString.equals(otherName.mString))
            {
                isSame = true;
            }
        }
        return ( isSame );
    }


    /**
        Instances of this class can be used as keys in a Hashtable. Since the
        equals() method is overridden by this class, it is necessary that
        hashCode() is also overridden, to guarantee that equal instances of
        this class guarantee to produce equal hashcodes.

        @see java.lang.Object#hashCode()
        @return integer representing hashcode for this Name
    */

    public int hashCode()
    {
        return ( mString.length() * 2 + 1 );
    }

    private String createPartialNameString(int beginIndex, int endIndex)
    {
        boolean isInputValid = (beginIndex <= endIndex) &&
                               (endIndex < mNameParts.size()) &&
                               (beginIndex >= 0);
        //ArgChecker.check(isInputValid, "indices are invalid");
        StringBuffer nameBuffer =  new StringBuffer();
        for(int i = beginIndex ; i < endIndex ; i++)
        {
            String aPart = (String)mNameParts.elementAt(i);
            nameBuffer.append(aPart);
            if(i < endIndex - 1)
            {
                nameBuffer.append(Tokens.kDelimiterChar);
            }
        }

        return ( nameBuffer.toString() );
    }

}