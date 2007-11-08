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
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/codegen/JavaCompiler.java,v $
 *
 */

package com.sun.ejb.codegen;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.server.Constants;
import com.sun.tools.javac.Main;

class JavaCompiler extends Compiler
{
    private static final String JAVAC_EXT_DIRS_OPTION = "-extdirs";
    private static final String JAVAC_OUT_OF_PROCESS =
                                "com.sun.aas.deployment.javacoutofprocess";


	JavaCompiler(List theOptions, List theFiles) throws JavaCompilerException
	{
		super(theOptions, theFiles);
	}

	///////////////////////////////////////////////////////////////////////////

	protected void internal_compile() throws JavaCompilerException, ProcessExecutorException
	{
		// note: we are NOT catching Exceptions and then trying the next one.
		// An Exception means there was a compile error and it would be a waste of
		// time to run another compile.
		// if they return true -- it means the facility exists AND everything compiled OK
		// note: only allow JavaCompilerException out of here -- catch everything else
		// and wrap it!!
		
		if(userCompile()) {
			return;
                }

		if(fastjavacCompile()) {
			return;
                }

		javacCompile();
	}
	
	///////////////////////////////////////////////////////////////////////////

	private boolean userCompile() throws ProcessExecutorException
	{
		if(userExe == null)
			return false;
		
		ArrayList cmd = new ArrayList();
		cmd.add(userExe.getPath());
                cmd.add(JAVAC_EXT_DIRS_OPTION);
                cmd.add(System.getProperty(JAVA_EXT_DIRS_SYS_PROP));
		cmd.addAll(userOptions);
		cmd.addAll(options);
		cmd.addAll(files);
		
		String[] cmds = new String[cmd.size()];
		cmds = (String[])cmd.toArray(cmds);
		runProcess(cmds, getUserSpecifiedCompilerTimeout() * files.size());
		logCompilerName(userExe.getName());
		return true;
	}
	
	//////////////////////////////////////////////////////////////////////////

	private boolean fastjavacCompile() throws ProcessExecutorException
	{
		if(fastExe == null || jdkDir == null)
			return false;

		ArrayList cmd = new ArrayList();
		cmd.add(fastExe.getPath());
		cmd.add("-jdk");
		cmd.add(jdkDir.getPath());
		cmd.addAll(options);
		addJavaFiles(cmd);
		String[] cmds = new String[cmd.size()];
		cmds = (String[])cmd.toArray(cmds);
		runProcess(cmds, getFastjavacTimeout() * files.size());
		logCompilerName("fastjavac");
		return true;
	}

	///////////////////////////////////////////////////////////////////////////

	private boolean javacCompile() throws JavaCompilerException, 
                                              ProcessExecutorException
	{
		if(javacExe == null)
			return false;
		
                boolean outOfProcess = 
                    Boolean.getBoolean(JAVAC_OUT_OF_PROCESS);

		ArrayList cmd = new ArrayList();
                if (outOfProcess) {
                    cmd.add(javacExe.getPath());
                }
                cmd.add(JAVAC_EXT_DIRS_OPTION);
                cmd.add(System.getProperty(JAVA_EXT_DIRS_SYS_PROP));
    	        cmd.addAll(options);
	        addJavaFiles(cmd);
	        String[] cmds = new String[cmd.size()];
	        cmds = (String[])cmd.toArray(cmds);

                if (outOfProcess) {
                    runProcess(cmds, getJavacTimeout() * files.size());
                } else {
                    try {
                        ByteArrayOutputStream bos = 
                            new ByteArrayOutputStream();
                        PrintWriter pw = new PrintWriter(bos); 
                        Main compiler = new Main();
                        int ret = compiler.compile(cmds, pw);
                        if (ret != 0) {
                            byte[] errorBytes = bos.toByteArray();
                            String errorString = new String(errorBytes); 
                            throw new JavaCompilerException(
                                "java_compiler.error", "Native compiler returned an error: {0}\nError messages are: {1}", new Object[] { new Integer (ret), errorString } );
                        }
                    }
                    catch(JavaCompilerException jce) {
                        throw jce;
                    }
                    catch(Throwable t)
                    {
                        throw new JavaCompilerException(
                            "java_compiler.unknown_exception",
                            "JavaC compiler threw an Exception", t);
                    }
                }
		logCompilerName("javac");
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void internal_init()
	{
		fastExe		= null;
		javacExe	= null;
		userExe		= null;
		userOptions	= new ArrayList();		
		
		initUserCompiler();
		initFastjavac();
		initJavac();
		logger.log(Level.FINE, "fastExe: " + ((fastExe == null)		? "null" : fastExe.getPath()) );
		logger.log(Level.FINE, "javacExe: " + ((javacExe == null)	? "null" : javacExe.getPath()) );
		logger.log(Level.FINE, "jdkDir: " + ((jdkDir == null)		? "null" : jdkDir.getPath()) );
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void initUserCompiler()
	{
		String userSpecified = getSystemPropertyIgnoreCase(Constants.USER_SPECIFIED_COMPILER);
		
		if(!StringUtils.ok(userSpecified))
			return;
		
		userExe = new File(userSpecified);

		if(!userExe.exists())
		{
			String msg = localStrings.getStringWithDefault(
				"java_compiler.bad_user_compiler", 
				"Can't locate user-specified Java Compiler for deployment.  " 
					+"Environmental Variable= {0}, Value = {1}",
				new Object[] { Constants.USER_SPECIFIED_COMPILER, userSpecified } );

			logger.warning(msg);
			userExe = null;
			return;
		}
		
		// note: it is difficult to handle spaces inside options.
		// at least without requiring the user to specify the args as:
		// xxx1, xxx2, xxx3, etc.  That's too painful for them.  Or I could
		// parse out quote-delimited Strings.  Maybe later.  What
		// are the chances that they will use spaces in filenames anyways?

		userExe = FileUtils.safeGetCanonicalFile(userExe);
		String opts = getSystemPropertyIgnoreCase(Constants.USER_SPECIFIED_COMPILER_OPTIONS);
		
		if(!StringUtils.ok(opts))
			return;
		
		StringTokenizer tok = new StringTokenizer(opts);
		
		while(tok.hasMoreTokens())
		{
			userOptions.add(tok.nextToken());
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////

	private void initFastjavac()
	{
		if(installRoot == null || jdkDir == null)
			return;
		
		String fastName;
		/*
		// WBN -- Allow config of fastjavac in the environment
		String fastName = System.getProperty(Constants.FASTJAVAC_COMPILER);
		
		if(StringUtils.ok(fastName))
		{
			fastExe = new File(fastName); 

			if(fastExe.exists())
			{
				fastExe = FileUtils.safeGetCanonicalFile(fastExe);
				return;
			}
			fastExe = null;
		}
		*/
		
		if(OS.isWindows())
			fastName	= "fastjavac.exe";
		else if(OS.isSun())
			fastName	= "fastjavac.sun";
		else if(OS.isLinux())
			fastName	= "fastjavac.linux";
		else
			fastName	= null;
		
		if(fastName == null)
			return;

		// if fastjavac app exists -- set it
		fastExe = new File(installRoot + "/studio4/bin/fastjavac/" + fastName); //now named studio4

		if(fastExe.exists())
			fastExe = FileUtils.safeGetCanonicalFile(fastExe);
		else
			fastExe = null;
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void initJavac()
	{
		if(jdkDir == null)
			return;
		
		String javacName;
		
		if(OS.isWindows())
		{
			javacName	= "javac.exe";
		}
		else
		{
			javacName	= "javac";
		}

		javacExe = new File(jdkDir, "/bin/" + javacName);

		if(javacExe.exists())
			javacExe = FileUtils.safeGetCanonicalFile(javacExe);
		else
			javacExe = null;
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
 	 * <p>This method caches the value of timeout in "fastJavacTimeout" variable to prevent
         * memory leak seen in the System.getProperty method in Compiler.java
	 *
	 * @return The timeout, in milliseconds, for each java file
	 */	
	private static int getFastjavacTimeout()
	{
		if (fastJavacTimeout < 0 ) 
		{
			fastJavacTimeout = getTimeout(Constants.FASTJAVAC_TIMEOUT_MS, Constants.DEFAULT_FASTJAVAC_TIMEOUT_MS, 1000, 300000);
		}
		return fastJavacTimeout;
	}

	/** Returns the timeout, in milliseconds, for each java file.
	 * The compiler calling code will multiply this value by the
	 * number of java files.  If the compiler takes longer than
	 * this amount of time the process will be killed.
	 * This is to avoid hangs.
	 *
	 * <p>For flexibility, a environmental variable is checked first.  Failing that,
	 * it will use the hard-coded default value.
	 *
 	 * <p>This method caches the value of timeout in "javacTimeout" variable to prevent
         * memory leak seen in the System.getProperty method in Compiler.java
	 *
	 * @return The timeout, in milliseconds, for each java file
	 */	
	private static int getJavacTimeout()
	{
		if (javacTimeout < 0 ) 
		{
			javacTimeout = getTimeout(Constants.JAVAC_TIMEOUT_MS, Constants.DEFAULT_JAVAC_TIMEOUT_MS, 1000, 900000);
		}
		return javacTimeout;
	}

	/** Returns the timeout, in milliseconds, for each java file.
	 * The compiler calling code will multiply this value by the
	 * number of java files.  If the compiler takes longer than
	 * this amount of time the process will be killed.
	 * This is to avoid hangs.
	 *
	 * <p>For flexibility, a environmental variable is checked first.  Failing that,
	 * it will use the hard-coded default value.
	 *
 	 * <p>This method caches the value of timeout in "userTimeout" variable to prevent
         * memory leak seen in the System.getProperty method in Compiler.java
	 *
	 * @return The timeout, in milliseconds, for each java file
	 */	
	private static int getUserSpecifiedCompilerTimeout()
	{
		if (userTimeout < 0 ) 
		{
			userTimeout = getTimeout(Constants.USER_SPECIFIED_COMPILER_TIMEOUT_MS, Constants.DEFAULT_USER_SPECIFIED_COMPILER_TIMEOUT_MS, 1000, 900000);
		}
		return userTimeout;
	}
	
	///////////////////////////////////////////////////////////////////////////

	private					File			userExe;
	private					File			fastExe;
	private					File			javacExe;
	private					List			userOptions;

	// used for caching timeouts to prevent memory leaks. must be initialized to -1.
	private	static				int			fastJavacTimeout = -1;
	private	static				int			javacTimeout = -1;
	private	static				int			userTimeout = -1;

	///////////////////////////////////////////////////////////////////////////
/* testing code...
	public static void main(String[] args)
	{
		System.out.println("Test 1: install-root == C:/ias7 and JAVA_HOME not set");
		System.setProperty(Constants.INSTALL_ROOT, "C:/ias7");
		test();
		System.out.println("Test 2: install-root == C:/ias7 and JAVA_HOME == c:/jdk1.4");
		System.setProperty("JAVA_HOME", "C:/jdk1.4");
		test();
	}		
	
	
	///////////////////////////////////////////////////////////////////////////

	public static void test()
	{
		List opt = new ArrayList();
		List f   = new ArrayList();
	
		opt.add("-d");
		opt.add("c:/tmp/crap");
		opt.add("-g");
		f.add("c:/src/java/junk/Crap.java");
	
		try
		{
			JavaCompiler jc = new JavaCompiler(opt, f);
			logger.log(Level.SEVERE, "fastExe: " + ((jc.fastExe == null)		? "null" : jc.fastExe.getPath()) );
			logger.log(Level.SEVERE, "javacExe: " + ((jc.javacExe == null)	? "null" : jc.javacExe.getPath()) );
			logger.log(Level.SEVERE, "jdkDir: " + ((jc.jdkDir == null)		? "null" : jc.jdkDir.getPath()) );
			jc.compile();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
 **/
}

