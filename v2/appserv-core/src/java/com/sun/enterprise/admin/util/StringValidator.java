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
 * StringValidator.java
 *
 * Created on April 12, 2001, 10:42 PM
 */
package com.sun.enterprise.admin.util;

/**
 * Class for validating String method parameters.  Current implementation's
 * stock object (getInstance()) verifies that the String exists and has length > 0
 * @author bnevins
 * @version $Revision: 1.5 $
 */

public class StringValidator extends BaseValidator 
{ 
    private final int mMininumLength;
    private static final int kDefaultMinimumLength   = 1; 
    private static final StringValidator sDefaultInstance = new StringValidator(kDefaultMinimumLength); 
    private static final String badArgMessage           = 
        "Can't call StringValidator.validate() with a non-String argument";

    /** Create a StringValidator
     * @param minimumLength The String is invalid if its length is less than this
     */
    public StringValidator(int minimumLength) 
    { 
        Assert.assertRange(minimumLength, 0, Integer.MAX_VALUE, "minimumLength"); 
        mMininumLength = minimumLength; 
    } 

    /** Get the standard StringValidator
     * @return A class variable with a default minimum length of 1
     */	
    public static Validator getInstance()
    {
        return sDefaultInstance;
    } 

    /** Validate a String
     * @param obj The String to be validated
     * @return ValidatorResult is invalid if the String's length was
     * less than the minimum required length
     */
    public ValidatorResult validate(Object obj) 
    { 
        ValidatorResult result = super.validate(obj);

        if (result.isValid()) 
        { 
            Assert.assertit( (obj instanceof String), badArgMessage);

            final String 	str = (String)obj; 
            final int		len	= str.length();

            if(len < mMininumLength) 
            { 
                result = makeBadResult(len);
            } 
        } 
        return result; 
    } 

    private ValidatorResult makeBadResult(final int len) 
    {
        return new ValidatorResult(false, 
                        "The String argument is invalid.  The minimum required length is " +
                        mMininumLength + " and the String's actual length is " + len);
    }
}