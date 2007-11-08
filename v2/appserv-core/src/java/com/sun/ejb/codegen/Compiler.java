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
 * @version $Revision: 1.4 $
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/codegen/Compiler.java,v $
 *
 */

package com.sun.ejb.codegen;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.sun.logging.LogDomains;
import com.sun.enterprise.instance.UniqueIdGenerator;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.StringUtils;

abstract class Compiler 
{
    protected static final String JAVA_EXT_DIRS_SYS_PROP = "java.ext.dirs";

	Compiler(List theOptions, List theFiles) throws JavaCompilerException
	{
		if(theOptions == null || theOptions.size() <= 0)
			throw new JavaCompilerException("java_compiler.badargs",
				"JavaCompiler given null or empty {0} list",
				new Object[] { "options" } );
		if(theFiles == null || theFiles.size() <= 0)
			throw new JavaCompilerException("java_compiler.badargs",
				"JavaCompiler given null or empty {0} list",
				new Object[] { "file" } );
		

		options = theOptions;
		files	= theFiles;
		init();
	}
	
	///////////////////////////////////////////////////////////////////////////

	final void compile() throws JavaCompilerException
	{
		try
		{
			internal_compile();
		}
		catch(JavaCompilerException jce)
		{
			throw jce;
		}
		catch(Throwable t)
		{
			// might be a ProcessExecutorException
			throw new JavaCompilerException(t);
		}
		finally
		{
			if (fileOfFilenames != null) 
			{
				if(!fileOfFilenames.delete()) // todo: add log message here!!!
					fileOfFilenames.deleteOnExit();
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	abstract protected void internal_compile() throws JavaCompilerException, ProcessExecutorException;
	abstract protected void internal_init();
	
	///////////////////////////////////////////////////////////////////////////

	final void init()
	{
		installRoot = System.getProperty(Constants.INSTALL_ROOT);
		initJDKDir();
		logger.log(Level.FINE, "[Compiler] JDK Directory: " + ((jdkDir == null) ? "null" : jdkDir.getPath()));
		
		String enableJavacFileStr = System.getProperty(Constants.ENABLE_JAVAC_FILE);

		if(enableJavacFileStr != null)
			useFileContainingFilenames = Boolean.valueOf(enableJavacFileStr).booleanValue();
		else
			useFileContainingFilenames = OS.isWindows();

		internal_init();
	}
	
	///////////////////////////////////////////////////////////////////////////

	final void initJDKDir() {
            //Try this jre's parent
            String jreHome = System.getProperty("java.home");
            if(StringUtils.ok(jreHome)) {
                // on the mac the java.home does not point to the jre
                // subdirectory.
                if (OS.isDarwin()) {
                    jdkDir = new File(jreHome);
                } else {
                    jdkDir = (new File(jreHome)).getParentFile();	//jdk_dir/jre/..
                }
                
                if(FileUtils.safeIsDirectory(jdkDir)) {
                    jdkDir = FileUtils.safeGetCanonicalFile(jdkDir);
                    return;
                }
            }
        
		jdkDir = null;
                
		// Check for "JAVA_HOME" -- which is set via Server.xml during initialization
		// of the Server that is calling us.  
		
		String jh = System.getProperty("JAVA_HOME");
		
		if(StringUtils.ok(jh))
		{
			jdkDir = new File(jh);	// e.g. c:/ias7/jdk

			if(FileUtils.safeIsDirectory(jdkDir))
			{
				jdkDir = FileUtils.safeGetCanonicalFile(jdkDir);
				return;
			}
		}

		jdkDir = null;
		
		//Somehow, JAVA_HOME is not set. Try the "well-known" location...
		if(installRoot != null) {
			jdkDir = new File(installRoot + "/jdk");

			if(FileUtils.safeIsDirectory(jdkDir)) {
				jdkDir = FileUtils.safeGetCanonicalFile(jdkDir);
				return;
			}
		}
		
                //Give up!!
		jdkDir = null;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void runProcess(String[] cmds, long timeout) throws ProcessExecutorException
	{
		ProcessExecutor exec = new ProcessExecutor(cmds, timeout);
		exec.execute();
		// they are always empty! FIXME
		//logger.log(Level.FINER, "STDOUT: " + exec.getStdout());
		//logger.log(Level.FINER, "STDERR: " + exec.getStderr());
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void logCompilerName(String compilerName)
	{
		logger.log(Level.FINE, "[EJBC] Successfully compiled with " + compilerName);
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected static String getSystemPropertyIgnoreCase(final String key)
	{
		Properties	p	= System.getProperties();
		Set			set = p.entrySet();

		for(Iterator it = set.iterator(); it.hasNext(); )
		{
			Map.Entry	me		= (Map.Entry)it.next();
			String		propKey = (String)me.getKey();
			
			if(key.compareToIgnoreCase(propKey) == 0)
				return (String)me.getValue();
		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void addJavaFiles(List list)
	{
		// if we aren't using a File -- add all the filenames & return
		if(!useFileContainingFilenames)
		{
			list.addAll(files);
			return;
		}
		
		// attempt to write the filenames into a file
		writeFileOfFilenames();
		
		if(fileOfFilenames == null)
		{
			// oops -- error writing the file.  Let's try the normal method
			// and hope for the best instead of bailing out!
			list.addAll(files);
			return;
		}
		
		list.add("@" + FileUtils.safeGetCanonicalPath(fileOfFilenames));
	}
	
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Construct a temporary file containing a list of java separated
	 * by line breaks.
	 * <p> Sets fileOfFilenames to the created temp file object, or to null if 
	 * there was a problem.
	 */
	protected void writeFileOfFilenames()
	{
		fileOfFilenames = null;
		BufferedWriter writer = null;
		
		try 
		{
			fileOfFilenames = File.createTempFile(
				hostUniqueStr +
				Long.toString(UniqueIdGenerator.getInstance().getNextUniqueId(), 16) +
				"_", ".s1a");

			writer = new BufferedWriter(new FileWriter(fileOfFilenames));

			for(Iterator it = files.iterator(); it.hasNext(); )
			{
                                /*
                                 *If the file spec includes any embedded blank, enclose the file spec
                                 *in double quote marks and make sure single backslashes are doubled
                                 *because Java's string manipulation treats single backslashes as 
                                 *quote characters.
                                 */
                                String fileSpec = (String) it.next();
                                if (fileSpec.indexOf(' ') != -1) {
                                    fileSpec = prepareFileSpec(fileSpec);
                                }
				writer.write(fileSpec);
				writer.newLine();
			}
		}
		catch(Exception e)
		{
			fileOfFilenames = null;
		}
		
		finally 
		{
			try 
			{
				if (writer != null) 
				{
					writer.close();
				}
			} 
			catch(Exception ex) 
			{
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected static int getTimeout(String what, int defValue, int min, int max)
	{
		int		to			= defValue;
		String	sysValue	= System.getProperty(what);

		if(sysValue != null)
		{
			try
			{
				to = Integer.parseInt(sysValue);
			}
			catch(Exception e)
			{
				to = defValue;
			}
		}
		
		if(to < min || to > max)
			to = defValue;

		return to;
	}
	

    /*
     *Replace occurences of a single backslash character with double backslashes.  This
     *allows the backslash character to make it through the quoting process
     *as the pseudo-command line is prepared for the Java compilation.  This is especially 
     *helpful for Windows platforms
     *
     *@param original path specification
     *@return path spec with single backslashes replaced by double backslashes
     */
    protected static String ensureDoubleBackslashes(String original) {
        StringBuffer answer = new StringBuffer();
        int match = -1; // the index where the next single backslash occurs
        int placeAfterPreviousSlash = 0; // the index just after the most-recently-located backslash
        while ((match = original.indexOf("\\", placeAfterPreviousSlash)) != -1) {
            /*
             *Before we replace this slash, make sure this is not already a double-backslash that
             *we should leave as-is.  We need to insert an added backslash if any of the following is true:
             *  - the slash we found is the last character in the string
             *  - the slash found is NOT at the end and is not followed by another slash
             */
            boolean slashIsAtEnd = (match + 1) >= original.length();
            boolean slashIsDoubled = (! slashIsAtEnd) && (original.charAt(match + 1) == '\\');
            if (slashIsAtEnd || ! slashIsDoubled) {
                /*
                 *Append the part of the original string just after the previously-found backslash
                 *up to and including the just-found backslash.  Then append the second backslash to 
                 *create a quoted backslash in the result.
                 */
                answer.append(original.substring(placeAfterPreviousSlash, match + 1)).append("\\");
            }
            if (slashIsDoubled) {
                placeAfterPreviousSlash = match + 2;
            } else {
                placeAfterPreviousSlash = match + 1;
            }
        }
        answer.append(original.substring(placeAfterPreviousSlash));
        return answer.toString();
    }

     /**
     *Enclose the file spec in double quote marks and replace backslashes with
     *double backslashes.
     *<p>
     *Embedded spaces in file paths confuse the compiler into thinking that the command line
     *argument has ended at the space, whereas in fact the argument should continue on.  By 
     *enclosing the file paths in double quote marks we prevent this.  
     *
     *@param the file spec to be prepared
     *@return the adjusted file spec
     */
    protected static String prepareFileSpec (String fileSpec) {
        String result = "\"" + ensureDoubleBackslashes(fileSpec) + "\"";
        return result;
    }
	///////////////////////////////////////////////////////////////////////////

	protected					File			jdkDir						= null;
	protected					String			installRoot					= null;
	protected					List			options;
	protected					List			files;
	protected					File			fileOfFilenames				= null;
	protected					boolean			useFileContainingFilenames	= false;
    protected static final		Logger			logger						= LogDomains.getLogger(LogDomains.DPL_LOGGER);
	protected static final		StringManager	localStrings				= StringManager.getManager(JavaCompiler.class);
	protected static final		String			hostUniqueStr				= Integer.toString((new Object()).hashCode(), 16) + "_";
}

