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

import com.sun.enterprise.admin.util.Validator;
import com.sun.enterprise.admin.util.ValidatorResult;

// i18n import 
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

/**
 * Implementation class for Assert and CheckArgs
 */

final class AssertImpl
{
    private int     mExceptionType  = 0;
    private boolean mWantStackTrace = true;
    private String  mPreamble       = null;

    private static final String	sDefaultPreamble    = "Assertion Failure: ";
    static final int            sAssertError        = 0;
    static final int            sIllegalArgument    = 1;
    
	// i18n SOMLocalStringsManager
	private static SOMLocalStringsManager localizedStrMgr =
		SOMLocalStringsManager.getManager( AssertImpl.class );

    AssertImpl(int exceptionType)
    {
        this("", exceptionType);
    }

    AssertImpl(String msg, int exceptionType)
    {
        mPreamble 		= msg;
        mExceptionType	= exceptionType;

        if(mPreamble == null)
        {
            mPreamble = sDefaultPreamble;
        }

        // add a ": " -- if it isn't an empty string
        if(mPreamble.length() > 0 && !mPreamble.endsWith(": "))
        {
            mPreamble += ": ";
        }

        if(mExceptionType < sAssertError || mExceptionType > sIllegalArgument)
        {
            lowLevelAssert("Invalid exception type id.  Must be 0 or 1");

            // caller could swallow that assert and call assert() later
            // so let's setup a reasonable value.
            mExceptionType = sAssertError;	
        }
    }
	
    void setWantStackTrace(boolean what)
    {
        mWantStackTrace = what;
    }

    void assertIt(boolean b, Object userMsg)
    {
        if (b)
        {
            return;
        }
        String msg = null;
        if(userMsg != null)
        {
            msg = userMsg.toString();
        }
        else
        {
            msg = "boolean test was false";
        }
        toss(msg);
    }

    void assertRange(long value, long min, long max, Object userMsg)
    {
        if (value < min || value > max)
        {
            final String rangeString = "[" + min + ", " + max + "]";
            String msg	= "illegal integer value = " + value +
                    " must be in range " + rangeString;
            if (userMsg != null)
            {
                msg += " ( " + userMsg.toString() + " )";
            }
            toss(msg);
        }
    }

    void assertValid(Object object, String name, Validator validator)
    {  
        final ValidatorResult result = validator.validate(object);

        if (!result.isValid())
        {
            final String msg	= "Validation failed for " + name +
                                            ": " + result.getString();
            toss(msg);
        }
    }

    /**
        An assertion has failed, do something with the message.

        Our current implemention is to dump the stack trace
        and throw an AssertError, so that the failure is obnoxious
        and will be noticed.
     */
    private void toss(String msg) throws IllegalArgumentException, AssertError
    {
        String s = mPreamble + msg;
        Throwable t = null;

        /* yes -- the following is ugly.  But there is NO WAY to throw the 
         * common superclass -- Throwable -- without making everyone on 
         * the call stack declare it!!
         **/

        if(mExceptionType == sIllegalArgument)
        {
            IllegalArgumentException iae = new IllegalArgumentException(s);

            if(mWantStackTrace)
            {
                Debug.printStackTrace(iae);
            }
            throw iae;
        }
        else if(mExceptionType == sAssertError)
        {
            AssertError ae = new AssertError(s);

            if(mWantStackTrace)
            {
                Debug.printStackTrace(ae);
            }
            throw ae;
        }
        else
        {
            lowLevelAssert("Impossible condition -- bad mExceptionType -- " 
                    + mExceptionType);
        }
    }

    private void pr(String s)
    {
        Debug.println(s);
    }

    private void lowLevelAssert(String s)
    {
        // if there is a problem in this code -- we can't do a normal assert or
        // we are going to enter an infinite loop - since Assert calls us!!

		String msg = localizedStrMgr.getString( "admin.util.fatal_error_in_setupexceptionconstructor", s );
        throw new AssertError( msg );
    }
}
