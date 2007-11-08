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
 * Reminder.java
 *
 * Created on December 30, 2000, 11:33 AM
 */

package com.sun.enterprise.util.diagnostics;

import javax.swing.JOptionPane;

/**
 *
 * This class makes it easy to add a message to be displayed at runtime,
 * and/or a message requiring a Yes/No answer.
 * The message appears in a MODAL dialog so it will always get your full attention.  
 * It is designed for development-time only.  In fact every message box screams 
 * out a message to not allow it into the release.
 *The class name, file name, method name and line number of the caller  is automatically added to the message window.
 *<p><b>Examples:</b></p>
 *
 <p> 1) You just temporarily did something -- like commented-out some code --
 * and you don't want to forget to comment it back in later:
 * <br><code>Reminder.message("Don't forget to uncomment the code here!");</code>
 * <p>2) You want to add a branch in some code, temporarily.  Instead of
 * changing the source and recompiling again and again do the following and
 * determine the branching at runtime:
<br><code>
if(Reminder.message("Use the new improved code?"))
<br>    new_code();
<br>else
<br>    old_code();
 </code>
 *<p> 3) <b>Poor Man's Debugger</b><br>Add watch statements, pause execution so you can check something in the filesystem, etc.
 * @author bnevins
 * @version $Revision: 1.3 $
 */
public class Reminder
{
	/**
	 * Displays a Message Box that reminds you to do something.  You can't ignore it -- 
	 * it is a modal dialog box.
	 *
	 * @param msg The message to display in the Message Box
	 */	
	public static void message(String msg) 
	{
		String s = createMessage(msg);
		JOptionPane.showMessageDialog(null, s, title, JOptionPane.ERROR_MESSAGE); 
    }
	
	//////////////////////////////////////////////////////////
	
	/**
	 * Displays a Message Box with Yes and No buttons.  Handy for temporarily adding a
	 * runtime-chosen decision point in your code.  Use this instead of commenting out
	 * code that you have to remember to uncomment later.
	 * @param msg The message to display in the Message Box
	 * @return returns true if <i>yes</i> was chosen, false if <i>no</i> was chosen.
	 */	
	public static boolean yesno(String msg) 
	{
		String s = createMessage(msg);
		//return true for yes, false for no...
		int reply = JOptionPane.showConfirmDialog(null, s, title, JOptionPane.YES_NO_OPTION);
		
		if(reply == JOptionPane.YES_OPTION)
			return true;
		
		return false;
    }
	
	//////////////////////////////////////////////////////////
	
	private Reminder()
	{
	}
	
	//////////////////////////////////////////////////////////
	
	private static String createMessage(String s)
	{
		String location;
		
		try
		{
			location = "\n\nCode Location: " + new CallerInfo(me).toString();
		}
		catch(CallerInfoException e)
		{
			location = "\n\nUnknown code location";
		}
		
		return preMessage + s + location;
	}
	
	//////////////////////////////////////////////////////////
	
	private final static String		title 		= "Temporary Code Reminder";
	private final static String		preMessage 	= "*****   DO NOT SHIP WITH THIS MESSAGE IN PLACE!!!!  ******\n\n";
	private final static Object[]	me			= { new Reminder() };
	
	//////////////////////////////////////////////////////////

	/**
	 * Simple test code to exercise the class.
	 */	
	public static void main(String[] notUsed)
	{
		ReminderTester rt = new ReminderTester();
		rt.test();
		System.exit(0);
	}
	
}

class ReminderTester
{
	public void test()
	{
		Reminder.message("Here is Reminder.message()");
		boolean ret = Reminder.yesno("Here is Reminder.yesno().  Do you like it?");
		Reminder.message("You replied: " + (ret ? "yes" : "no"));
	}
}
