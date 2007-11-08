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
 * JavaCompiler.java
 *
 * Created on June 22, 2002, 8:56 PM
 * 
 * @author  bnevins
 * @version $Revision: 1.3 $
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/codegen/RMICompiler.java,v $
 *
 */

package com.sun.ejb.codegen;

import java.util.*;
import java.io.*;
import java.util.logging.Level;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.server.Constants;
import sun.rmi.rmic.Main;

class RMICompiler extends Compiler
{
    private static final String RMIC_EXT_DIRS_OPTION = "-extdirs";

	RMICompiler(List theOptions, List theFiles) throws JavaCompilerException
	{
		super(theOptions, theFiles);
	}
	
	///////////////////////////////////////////////////////////////////////////

	void setClasspath(String cp)
	{
		classpath = cp;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void internal_compile() throws JavaCompilerException, ProcessExecutorException
	{
		try
		{
			if(nativeExternalCompile())
				return;
		}
		catch(Throwable t)
		{
                    logger.log(Level.WARNING, 
                               "ejb.rmic_compilation_exception", t);
			// fall through to nativeCompile()
			// I know what you're thinking -- why run the native compile
			// if the the external already didn't work?  For some reason
			// the external will time out and we won't get an error message.
			// The native call will always give an error message.
		}
		
		nativeCompile();
	}
	
	///////////////////////////////////////////////////////////////////////////

	private boolean nativeExternalCompile() throws ProcessExecutorException
	{
		if(classpath == null || javaExe == null)
			return false;
		
                
		ArrayList cmd = new ArrayList();
		cmd.add(javaExe.getPath());
		cmd.add("-classpath");
		cmd.add(classpath);
                if (OS.isDarwin()) {
                    // add lib/endorsed so it finds the right rmic
                    cmd.add("-Djava.endorsed.dirs=" + System.getProperty("com.sun.aas.installRoot") + 
                        File.separatorChar + "lib" + File.separatorChar + "endorsed");
                }
                cmd.add("-D" + JAVA_EXT_DIRS_SYS_PROP + "=" 
                        + System.getProperty(JAVA_EXT_DIRS_SYS_PROP));
		cmd.add("sun.rmi.rmic.Main");
		cmd.addAll(options);
		addJavaFiles(cmd);
		String[] cmds = new String[cmd.size()];
		cmds = (String[])cmd.toArray(cmds);

		runProcess(cmds, getRmicTimeout() * files.size());
		logCompilerName("rmic in external JVM");
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/** This can't work -- the jdk's rmic executable doesn't have the iAS 
	 * hacks
	 *
	private boolean rmicCompile() throws ProcessExecutorException
	{
		if(rmicExe == null || DONT_USE_RMIC_YET)
			return false;
		
		ArrayList cmd = new ArrayList();
		cmd.add(rmicExe.getPath());
		cmd.addAll(options);
		addJavaFiles(cmd);
		String[] cmds = new String[cmd.size()];
		cmds = (String[])cmd.toArray(cmds);
		runProcess(cmds, getRmicTimeout() * files.size());
		logCompilerName("stand-alone rmic");
		return true;
	}
	 */

	///////////////////////////////////////////////////////////////////////////

	private void nativeCompile() throws JavaCompilerException
	{
		//options.add("-Xnocompile");
		//options.add("-verbose");
            options.add(RMIC_EXT_DIRS_OPTION);
            options.add(System.getProperty(JAVA_EXT_DIRS_SYS_PROP));
		options.addAll(files);
		String[] cmds = new String[options.size()];
		cmds = (String[])options.toArray(cmds);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Main	compiler	= new Main(baos, "rmic");
		boolean	good		= compiler.compile(cmds);
		//good = true;	// it ALWAYS returns an "error" if -Xnocompile is used!!
		
		String output = baos.toString();
		parseGeneratedFilenames(output);
		
		if(!good)
		{
			throw new JavaCompilerException("rmi_compiler.error",
				"RMI compiler returned an error: {0}", output);
		}
		
		logCompilerName("native rmic (sun.rmi.rmic.Main)");
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void internal_init()
	{
		rmicExe		= null;
		javaExe		= null;
		String rmicName, javaName;
	
		if(jdkDir == null)
			return;
		
		if(OS.isWindows())
		{
			rmicName	= "rmic.exe";
			javaName	= "java.exe";
		}
		else
		{
			rmicName	= "rmic";
			javaName	= "java";
		}
		
		// if rmic app can be located -- set it
		rmicExe = new File(jdkDir + "/bin/" + rmicName);
		
		if(rmicExe.exists())
			rmicExe = FileUtils.safeGetCanonicalFile(rmicExe);
		else
			rmicExe = null;

		// if external JVM can be located -- set it
		javaExe = new File(jdkDir + "/bin/" + javaName);
		
		if(javaExe.exists())
			javaExe = FileUtils.safeGetCanonicalFile(javaExe);
		else
			javaExe = null;

        logger.log(Level.FINE, "[RMICompiler] after internal_init: "
                + "javaExe: " + javaExe + "; rmicExe: " + rmicExe);
	}

	/** 
	 * Returns the timeout, in milliseconds, for each java file.
	 * The compiler calling code will multiply this value by the
	 * number of java files.  If the compiler takes longer than
	 * this amount of time the process will be killed.
	 * This is to avoid hangs.
	 *
	 * <p>For flexibility, a environmental variable is checked first.  Failing that,
	 * it will use the hard-coded default value.
	 *
	 * <p>This method caches the value of timeout in "timeout" variable to prevent
	 * memory leak seen in the System.getProperty method in Compiler.java
	 *
	 * @return The timeout, in milliseconds, for each java file
	 */	
	private static int getRmicTimeout()
	{
	    	if(timeout < 0 ) 
		{
			timeout = getTimeout(Constants.RMIC_TIMEOUT_MS, Constants.DEFAULT_RMIC_TIMEOUT_MS, 5000, 300000);
		}
		return timeout;
	}	
	
	/** 
	 * sun.rmi.rmic will return output like this:
	 * [generated d:\iAS7\domains\domain1\server1\generated\ejb\j2ee-apps\JDBCSimple\samples\jdbc\simple\ejb\_GreeterDBBean_EJBObjectImpl_Tie.java in 50 ms]
	 * [generated d:\iAS7\domains\domain1\server1\generated\ejb\j2ee-apps\JDBCSimple\org\omg\stub\com\sun\ejb\containers\_EJBObjectImpl_Tie.java in 20 ms]
	 * [loaded C:\iplanetx\jars\appserv-ext.jar(javax/ejb/CreateException.class) in 0 ms]
	 * A better solution is to modify rmic to simply return an array of filenames.  But I can't get rmic
	 * changes to stick -- it's unclear which version is really being called. This will work for now
	 *
	 *@return a Set of full path filenames of the generated files.
	 */	
	private void parseGeneratedFilenames(String s)
	{
		generatedFilenames = new HashSet();
		
		StringTokenizer tk = new StringTokenizer(s);

		while(tk.hasMoreTokens()) 
		{
			String token = tk.nextToken();
			
			if(token.equals("[generated") && tk.hasMoreTokens())
			{
					String fName = tk.nextToken();
					generatedFilenames.add(fName);
					logger.log(Level.FINER, "[RMIC] Generated: " + fName);
			}
		}
	}

	Set getGeneratedFilenames()
	{
		return generatedFilenames;
	}
	
	///////////////////////////////////////////////////////////////////////////

	private	File	rmicExe, javaExe;
	private	Set		generatedFilenames	= null;
	private String	classpath			= null;
	private static int	timeout			= -1;
}
