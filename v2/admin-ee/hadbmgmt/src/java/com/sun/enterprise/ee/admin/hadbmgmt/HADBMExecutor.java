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
 * HADBMExecutor.java
 *
 * Created on April 8, 2004, 9:40 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.util.ProcessExecutor;
import java.io.*;
import java.util.Arrays;

/**
 *
 * @author  bnevins
 */
public class HADBMExecutor
{
	public HADBMExecutor(File Exe, String[] Args)
	{
		exe		= Exe;
		args	= Args;
	}

	///////////////////////////////////////////////////////////////////////////
	
    public int exec() throws HADBSetupException
    {
		String[] commands = init();
		return execute(commands);
	}
	
	///////////////////////////////////////////////////////////////////////////

	private String[] init() throws HADBSetupException
    {
		if(exe == null) 
		{
			throw new HADBSetupException("hadbmgmt-res.InternalError", 
            StringHelper.get("hadbmgmt-res.no_executable"));
        }
		
		String[] commands = new String[args.length + 1];
		commands[0] = exe.getAbsolutePath();
		System.arraycopy(args, 0, commands, 1, args.length);
		
		// prepare a log message...
		StringBuilder info = new StringBuilder("HADBM Command: ");

		for(int i = 0; i < commands.length; i++)
		{
			info.append(commands[i]);
			info.append(' ');
		}

		LoggerHelper.info(info.toString());
		return commands;
	}

	///////////////////////////////////////////////////////////////////////////
	
    private int execute(String[] commands) throws HADBSetupException
    {
		if(HADBUtils.noHADB())
		{
			LoggerHelper.info("Feeble Hardware Test Mode.  Simulating running HADB commands.  "); 
			return HADBUtils.getPhonyReturnValue();
		}

		try
		{
			// note: it's VERY easy to hang apps that are writing to  stdout/stderr
			// be careful!
			
			
			/* NEW FIXME TEMP
			List<String> commandsList = Arrays.asList(commands);
			ProcessBuilder pb = new ProcessBuilder(commandsList);
			//pb.redirectErrorStream(true); -- this will mix stdout and stderr
			Process p = pb.start();
			*/

			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(commands);
			BufferedReader outreader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errreader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            Thread outThread = new Thread(new PipeThread(outreader, stdout));
            Thread errThread = new Thread(new PipeThread(errreader, stderr));
            outThread.start();
            errThread.start();

			// Caution!  Don't move this before readFromStream unless you're doing
			// some new threading changes.
			int ret = p.waitFor();

            // this is important -- the threads may not have had enough time to drain all the stdout/err yet.
            // just wait a moment for them, they are guaranteed to exit soon!
            // ref: bug 6476409
            outThread.join();
            errThread.join();

			return ret;
		}
		catch(Exception e)
		{
			throw new HADBSetupException("hadbmgmt-res.error_executing", e, getCommandLine(commands));
		}
	}


	///////////////////////////////////////////////////////////////////////////
	
	public String getStdout()
	{
		return stdout.toString();
	}

	///////////////////////////////////////////////////////////////////////////
	
	public String getStderr()
	{
		return stderr.toString();
	}

	///////////////////////////////////////////////////////////////////////////

	public boolean isHadbmError(int errno)
	{
		String s = getStdout() + getStderr();
		s = s.toLowerCase();
		String err = "hadbm:error " + errno;
		
		return s.indexOf(err) >= 0;
	}

	///////////////////////////////////////////////////////////////////////////
	
	private String getCommandLine(String[] cmds)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < cmds.length; i++)
		{
			if(i != 0)
				sb.append(' ');
			
			sb.append(cmds[i]);
		}
		
		return sb.toString();
	}	

	///////////////////////////////////////////////////////////////////////////
	
    private			StringBuilder	stdout		= new StringBuilder();
    private			StringBuilder	stderr		= new StringBuilder();
	private			File			exe;
	private			String[]		args;
		//private final static long		TIMEOUT		= 3600;	// 1 hour
	
	static class PipeThread implements Runnable
	{
		PipeThread(BufferedReader In, StringBuilder Out)
		{
			in = In;
			out = Out;
		}
		public void run()
		{
			try
			{
				//LoggerHelper.severe("BLOCKING...");
				String s = in.readLine(); 
				//LoggerHelper.severe("UNBLOCKING...");
				while(s != null)
				{
					out.append(s).append(NL);
					s = in.readLine();
				}
			}
			catch(Exception e)
			{
				LoggerHelper.info("hadbmgmt-res.ErrorReadingFromProcess", e); 
			}
		}
		
		private BufferedReader	in;
		private StringBuilder	out;
		private static String	NL	= System.getProperty("line.separator", "\n");
	}
}

	/** Tried and failed to use the utility class, ProcessExecutor
	 * Problems:
	 * (1) it writes stdout & stderr to files -- a waste of time
	 * (2) it hangs every time when I call 'hadbm get --all'
	 *
	 *
    public int oldExec(File exe, String[] args) throws HADBSetupException
    {
		if(exe == null) 
		{
			throw new HADBSetupException("hadbmgmt-res.InternalError", 
            StringHelper.get("hadbmgmt-res.no_executable"));
        }
		
		String[] commands = new String[args.length + 1];
		commands[0] = exe.getAbsolutePath();
		System.arraycopy(args, 0, commands, 1, args.length);
		
		// prepare a log message...
		StringBuilder info = new StringBuilder("HADBM Command: ");

		for(int i = 0; i < commands.length; i++)
		{
			info.append(commands[i]);
			info.append(' ');
		}

		if(HADBUtils.noHADB())
		{
			LoggerHelper.info("Feeble Hardware Test Mode.  Simulating running HADB commands.  "); 
			LoggerHelper.info(info.toString());
			return HADBUtils.getPhonyReturnValue();
		}

		LoggerHelper.info(info.toString());
		try
		{
			ProcessExecutor pe	= new ProcessExecutor(commands, TIMEOUT);
			// we can't tell a "real" error from a non-zero return value because
			// in both cases the same type of Exception is thrown.
			pe.setWantExceptionsForNonZeroReturnValues(false);
			String[]		out = pe.execute(true);	
			
			if(out == null)
				out = new String[] { "" };
			
			for(String line : out)
				stdout.append(line);

			return pe.getProcessExitValue();
		}
		catch(Exception e)
		{
			throw new HADBSetupException("hadbmgmt-res.error_executing", e, getCommandLine(commands));
		}
	}
*/
