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

package com.sun.enterprise.cli.commands;

import java.io.*;
import com.sun.enterprise.cli.framework.InputsAndOutputs;

/**
 *  This class contains useful api used for the commands module.
 *  These methods are:
 *  getPassword() - this method prompts the user for a password and attempts 
 *  to mask inputs with "".
 *  @version  $Revision: 1.3 $
 */
public class CommandUtil
{

    /**
     *  This method prompts the user to enter the password and attempts
     *  to mask inputs with "".
     *  @param prompt - prompt to display on the output device.
     */
    public String getPassword (String prompt) throws IOException
    {
	//password holder
	String password = "";
	final MaskingThread maskingThread = new MaskingThread(prompt);
	final Thread thread = new Thread(maskingThread);
	thread.start();
	//block until enter is pressed;
	while (true)
	{
	    char ch = (char)InputsAndOutputs.getInstance().getUserInput().getChar();
	    //char ch = (char)System.in.read();

	    //assume enter pressed, stop masking
	    maskingThread.stopMasking();

	    if (ch == '\r') break;
	    else if (ch == '\n') break;
	    else
            {
		//store the password
		password += ch;
	    }
	}
	InputsAndOutputs.getInstance().getUserOutput().println("");
	return password;
    }


    public static void main(String argv[])
    {
	CommandUtil passfield = new CommandUtil();
	String password = null;
	try 
	{
	    password = passfield.getPassword("Enter your password: " );
	}
	catch (IOException ioe)
	{
	    ioe.printStackTrace();
	}
	System.out.println("The password entered is " + password);
    }
}


/**
 *  This is an inner class used only in CommandUtil.java.
 *  This class attempts to erase characters echoed ot the console.
 */
class MaskingThread extends Thread
{
    private boolean stop = false;
    private int index;
    private String prompt;

    /**
     *  Constructor
     *  @param prompt The prompt displayed to the user
     */
    public MaskingThread(String prompt)
    {
	this.prompt = prompt;
    }

    /**
     *  Begin masking until asked to stop.
     */
    public void run()
    {
	while (!stop)
	{
	    try 
            {
		//attempt masking at this rate
		this.sleep(1);
	    }
	    catch (InterruptedException iex)
            {
		iex.printStackTrace();
	    }
	    if (!stop) 
	    {
		InputsAndOutputs.getInstance().getUserOutput().print(" ");
		InputsAndOutputs.getInstance().getUserOutput().print( "\r" + prompt );
		//System.out.print(" ");
		//System.out.print("\r" + prompt );
	    }
	    InputsAndOutputs.getInstance().getUserOutput().flush();
	    //System.out.flush();
	}
    }

    /**
     *  Instruct the thread to stop masking.
     */
    public void stopMasking()
    {
	this.stop = true;
    }
}
