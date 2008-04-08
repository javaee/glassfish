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

package com.sun.enterprise.cli.framework;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;


/**
	This class represents the user is in interactive mode.
	This class provides the users response.
 */
public class UserInput implements IUserInput
{

    private InputStream mInputStr;
    private boolean mInteractive;
    private BufferedReader buffRead;


    /**
       Constructor which sets the InputStream and the interactive mode.
       @param      in is an InputStream object representation of users
                   response.
    */
    public UserInput(InputStream in)
    {
	mInputStr      =    in;
	mInteractive   =    true;
	buffRead = new BufferedReader(new InputStreamReader(mInputStr));
    }


    /**
       Returns a boolean which tells the program is in interactive mode.
    */
    public boolean isInteractive()
    {
	return mInteractive;
    }


    /**
       Closes the underlying input stream.
    */
    public void	close() throws IOException
    {
	mInputStr.close();
    }

    /**
     *  Set the character encoding
     *  @param sEncoding - character encoding to set
     */
    public void setEncoding(String sEncoding) throws IOException
    {
//	buffRead.close();
	InputStreamReader inStrReader = new InputStreamReader(
	              			    mInputStr, sEncoding);
	buffRead = new BufferedReader(inStrReader);
//	inStrReader.close();
    }


    /**
       Returns a String representation of users response.
       Could be next line in the stream provided.
    */
    public String getLine() throws IOException
    {
	//InputStreamReader inStrReader = new InputStreamReader(mInputStr);
	//BufferedReader buffRead = new BufferedReader(inStrReader);
        return buffRead.readLine();
    }

    /**
     *  Returns a Character representation of users response.
     */
    public int getChar() throws IOException
    {
	return buffRead.read();
    }

}

