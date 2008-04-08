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

import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
    A class which stores the inputs and outputs for the asadmin-specific
    information.
 */
public class InputsAndOutputs
{

    
    private static InputsAndOutputs sIO = null;
    private IUserInput  userInput      = new UserInput( System.in );
    private IUserOutput userOutputImpl = new UserOutputImpl( System.out, false );
    private IErrorOutput errorOutputImpl = new ErrorOutputImpl( System.err, false );

    /**
        Default constructor.
    */
    private InputsAndOutputs()
    {
    }


    /**
       Singleton method that sets the instance of InputsAndOutputs
    */
    public static void setInstance(InputsAndOutputs iao)
    {
        if (iao == null) {
            if (sIO!=null) {
                try {
                    sIO.userOutputImpl.close();
                    sIO.userInput.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            sIO = new InputsAndOutputs();
            return;
        }
        if (iao != null && sIO != iao)
            sIO = iao;
    }


    /**
       Singleton method that returns an instance of InputsAndOutputs
    */
    public static synchronized InputsAndOutputs getInstance()
    {
	if (sIO == null)
	{
	    sIO = new InputsAndOutputs();
	}
	return sIO;
    }


    /**
	Returns the userOutputImpl class object.
    */
    public IUserOutput getUserOutput()
    {
	return this.userOutputImpl;
    }


    /**
	Creates and sets the userOutputImpl class with the given OutputStream
	object.

	@param os is the OutputStream object to  be set.
    */
    public void setUserOutput( OutputStream os )
    {
	this.userOutputImpl.close();
	this.userOutputImpl = new UserOutputImpl( os, false );
    }


    /**
	 Set the output file name and force new output to this file
	 immediately.

	 Any existing output will be flushed and closed.
     @param fileName     is file in the file system to be set.
	 @throws IOException
    */
    public void setUserOutputFile( String fileName ) throws IOException
    {
        FileOutputStream userOutputFile = new FileOutputStream( fileName );
        this.userOutputImpl.close();
        this.userOutputImpl = new UserOutputImpl( userOutputFile, true );
    }


    /**
	Returns the userInput object that is set in InputsAndOutputs.
    */
    public IUserInput getUserInput()
    {
        return this.userInput;
    }


    /**
	Sets the InputStream object in the InputsAndOutputs.
	@param   userInput is the UserInput object to be set.
	@throws IOException
    */
    public void setUserInput( InputStream is ) throws IOException
    {
        userInput.close();
        this.userInput = new UserInput(is);
    }


    /**
     *	Sets the InputStream object in the InputsAndOutputs with specified 
     *  character set.
     *	@param   userInput is the UserInput object to be set.
     *  @throws  IOException
     */
    public void setUserInput( InputStream is, String sEncoding )
	throws IOException
    {
        userInput.close();
        this.userInput = new UserInput(is);
        this.userInput.setEncoding(sEncoding);
    }


    /**
	Sets the userInput object in the InputsAndOutputs.
	@param   userInput is the UserInput object to be set.
	@throws  IOException
    */
    public void setUserInput( IUserInput userInput )
        throws IOException
    {
        this.userInput.close();
        this.userInput = userInput;
    }


    /**
	 Set the input to a file name 

     @param fileName     is file in the file system to be set.
	 @throws IOException
    */
    public void setUserInputFile( String fileName ) throws IOException
    {
        final FileInputStream userInputFile = new FileInputStream( fileName );
        setUserInput(userInputFile);
    }


    /**
	 Set the input to a file name with specified encoding

     @param fileName     is file in the file system to be set.
	 @throws IOException
    */
    public void setUserInputFile( String fileName, String sEncoding ) 
        throws IOException
    {
        final FileInputStream userInputFile = new FileInputStream( fileName );
        setUserInput(userInputFile, sEncoding);
    }


    /**
	Returns the errorOutputImpl class object.
    */
    public IErrorOutput getErrorOutput()
    {
        return this.errorOutputImpl;
    }


    /**
	Creates and sets the errorOutputImpl class with the given OutputStream
	object.

	@param os is the OutputStream object to  be set.
    */
    public void setErrorOutput( OutputStream os )
    {
        this.errorOutputImpl.close();
        this.errorOutputImpl = new ErrorOutputImpl( os, false );
    }


    /**
	 Set the output file name and force new output to this file
	 immediately.

	 Any existing output will be flushed and closed.
     @param fileName     is file in the file system to be set.
	 @throws IOException
    */
    public void setErrorOutputFile( String fileName ) throws IOException
    {
        FileOutputStream errorOutputFile = new FileOutputStream( fileName );
        this.errorOutputImpl.close();
        this.errorOutputImpl = new ErrorOutputImpl( errorOutputFile, true );
    }

}
