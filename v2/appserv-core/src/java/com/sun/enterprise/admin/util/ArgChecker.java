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

import com.sun.enterprise.admin.util.Assert;
import com.sun.enterprise.admin.util.AssertImpl;
import com.sun.enterprise.admin.util.Validator;
import com.sun.enterprise.admin.util.ValidatorResult;

/**
    Use ArgChecker to check method parameters

    <BR>Example usage:
    <BR>ArgChecker.check( name != null, "null name" );
    <BR>ArgChecker.check( index, 0, numItems, "index" );
    <BR>ArgChecker.check( str, "userName");
    <BR>ArgChecker.check( str, minimumStringLength, "userName");
    <BR>ArgChecker.checkValid(parameter, "parameter-name");
*/

public class ArgChecker
{
    /*
        We've made this 'final' because we will never disable checks
        during development.  We *may* change it during release, since
        an IllegalArgumentException could result in the killing of a process,
        since RuntimeExceptions are not generally caught.

        don't change until we ship (and then only maybe)
     */
    private final static boolean	sChecksEnabled	= true;
    private final static AssertImpl	sImpl;

    static
    {
        // need to do this in a static block to guarantee the 
        // setWantStackTrace() call
        sImpl = new AssertImpl("ArgChecker Failure", AssertImpl.sIllegalArgument);
        sImpl.setWantStackTrace(false);
    }

    private ArgChecker()
    {
        Assert.assertit(false, "You can't call the ArgChecker constructor!");
    }

    /**
        If expression is false, take appropriate action to note the failure.
        <p>
        If expression is true, do nothing.

        @param b boolean derived from callers expression.
        @param msg message to be added to IllegalArgumentException upon failure	
        @throws IllegalArgumentException if b is false
     */
    static public final void check( boolean b, Object msg ) 
        throws IllegalArgumentException
    {
        if ( sChecksEnabled )
        {
            sImpl.assertIt(b, msg);
        }
    }

    /**
        Checks that the specified value is in a specified range.
        <p>
        A convenience method which calls checkRange()
        @see #checkRange
        @param	value the value to be range-checked
        @param	min minimum value.  value must be >= min
        @param	max maximum value. value must be <= max
        @param	userMsg	additional user message (optional) to be included
        @throws IllegalArgumentException if the specified value is not in the specified range.
     */
    public static final void check(long	value, long min, long max, 
                                   Object userMsg)
        throws IllegalArgumentException
    {
        checkRange(value, min, max, userMsg);
    }

    /**
        Checks that the specified value is in a specified range.
        <p>
        The test done is ( value >= min && value <= max )

        If the test fails, then a descriptive string is generated
        which lists the value together with the min and max and
        user-specified message.

        @param	value	the value to be range-checked
        @param	min minimum value.  value must be >= min
        @param	max maximum value. value must be <= max
        @param	userMsg	additional user message (optional) to be included
        @throws IllegalArgumentException if the specified value is not in the specified range.
     */
    public static final void checkRange(long value, long min, long max,
                                        Object userMsg)
        throws IllegalArgumentException
    {
        if ( sChecksEnabled )
        {
            sImpl.assertRange(value, min, max, userMsg);
        }
    }

    /**
        Checks that the object is valid generically by using a Validation 
        object.<p>
        If the validation fails, then the check fails as with other checks.
        @param	object the value to be validated
        @param	name name of the object to be validated
        @param	validator validation object to validate the object
        @throws IllegalArgumentException if validator says that object is NOT valid 
     */
    public static void checkValid(Object        object,
                                  String        name,
                                  Validator     validator)
        throws IllegalArgumentException
    {  
        if ( sChecksEnabled )
        {
            sImpl.assertValid(object, name, validator);
        }
    }
	
    /**
        Checks that the object is valid generically by using a Validation object.
        <p>
        Convenience method. Calls checkValid().

        @see checkValid(Object, String, IValidator)
        @param	object		the value to be validated
        @param	name		name of the object to be validated
        @param	validator	validation object to validate the object
        @throws IllegalArgumentException if validator says that object is NOT valid 
     */
    public static void check(Object         object,
                             String         name,
                             Validator      validator)
        throws IllegalArgumentException
    {  
        checkValid(object, name, validator);
    }

    /**
        Check that the object is valid.
        Calls check( object, name, validator ) where validator is either
        the non-null validator or the object itself, if the object implements
        IValidator.
        @param	object the value to be validated
        @param	name name of the object to be validated
        @throws IllegalArgumentException if validator says that object is NOT valid 
     */
    public static void checkValid(Object object, String	name )
        throws IllegalArgumentException
    {
        final Validator validator  = (object instanceof Validator ) ?
                                        (Validator)object : 
                                        BaseValidator.getInstance();
        check( object, name, validator );
    }

    /**
        Check that the object is valid.
        Convenience method. Calls checkValid().

        @see checkValid(Object, String)
        @param	object		the value to be validated
        @param	name		name of the object to be validated
        @throws IllegalArgumentException if validator says that object is NOT valid 
    */
    public static void check(Object object, String name )
        throws IllegalArgumentException
    {
        checkValid( object, name);
    }

    /**
      Check the String with the standard StringValidator
      @param checkMe a String to check
      @param name The name of the String
      @throws IllegalArgumentException if checkMe is null or zero-length
    */
    public static void check(String checkMe, String name) 
        throws IllegalArgumentException
   {
        check(checkMe, name, StringValidator.getInstance()); 
   }

    /**
      Check the String with a custom StringValidator
      @param checkMe a String to check
      @param minimumLength The minimum acceptable length of the String to allow
      @param name The name of the String
      @throws IllegalArgumentException if checkMe is null or zero-length
    */
    public static void check(String checkMe, int minimumLength, String name) 
        throws IllegalArgumentException
    {  
        check(checkMe, name, new StringValidator(minimumLength)); 
    }
}
