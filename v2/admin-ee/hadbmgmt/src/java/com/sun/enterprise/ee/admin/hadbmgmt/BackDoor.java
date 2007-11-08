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
 * BackDoor.java
 *
 * Created on May 17, 2005, 12:49 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.util.ProcessExecutor;
import java.util.*;

/**
 *
 * @author bnevins
 */
public class BackDoor {
	
	/** Creates a new instance of BackDoor */
	public BackDoor(List<String> args) 
	{
		this.args = args;
	}
	
	public Object[] exec()
	{
		Object[] bad = new Object[] {"Bad Args", args };
		//return new Object[] { "Backdoor Here!" };
		
		if(args.size() < 1)
			return bad;
		
		String subCommand = args.get(0);
		args.remove(0);
		
		if(subCommand.equals("run-process"))
		{
			return runProcess(args);
		}
		else if(subCommand.equals("listdomain"))
		{
			
		}
		return bad;
	}
	
	private Object[] runProcess(List<String> commandsList)
	{
		try
		{
			String[] commands = commandsList.toArray(new String[commandsList.size()]);
			ProcessExecutor pe	= new ProcessExecutor(commands);
			String[]		out = pe.execute(true);	
			Arrays.toString(out);
			int retVal = pe.getProcessExitValue();

			Object[] ret = new Object[3];
			ret[0] = "Output Strings:";
			ret[1] = Arrays.toString(out);
			ret[2] = "Return Value: " + retVal;
			
			return ret;
		}
		catch(Exception e)
		{
			return new Object[] { "EXCEPTION!", e };
		}
	}
	private List<String> args;
}
