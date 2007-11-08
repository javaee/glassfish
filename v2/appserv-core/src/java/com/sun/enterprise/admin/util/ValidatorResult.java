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

// JDK includes
import java.io.Serializable;


/**
    Represents the result of validation by an IValidator.
 */
public class ValidatorResult implements Serializable
{
    private final boolean	mValid;
    private final String	mMsg;	// may be null

    // don't new up a new ValidatorResult every time an object is valid;
    // instead use this prebuilt one over and over.
    public static final ValidatorResult	kValid	= new ValidatorResult();

    /**
        Constructor intended for non-public use by Assert.
     */
    public ValidatorResult( boolean valid, String msg )
    {
        mValid	= valid;

        // keep message only if invalid (convenience for caller)
        mMsg	= valid ? null : msg;
    }

    /**
        Constructor indicates validation succeeded.
     */
    protected ValidatorResult( )
    {
        mValid	= true;
        mMsg	= null;
    }

    /**
        Return True if valid, false otherwise.

        @returns  true if valid, false otherwise
     */
    public boolean isValid()
    {
        return( mValid );
    }

    /**
        Return a String describing the validation failure.  A null will
        be returned if there was no failure.

        @returns  validation failure String (may be null if no failure)
     */
    public String getString()
    {
        return( mMsg );
    }

    /**
        Represent as a String
     */
    public String toString()
    {
        String	result	= null;

        if ( mValid )
        {
            result	= "valid";
        }
        else
        {
            result	= "INVALID = " + getString();
        }

        return( result );
    }
}