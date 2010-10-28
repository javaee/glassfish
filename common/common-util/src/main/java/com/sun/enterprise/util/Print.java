/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.util;

import java.util.StringTokenizer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.io.PrintWriter;

/*
** Printing/Debug Utilities
**
** This Module contains replacements for various types of print statements used for
** debugging purposes.  
** Example:
**   Replace this
**     System.out.println("test message");
**   With this
**     Print.println("test message");
**       or
**     Print.dprintln("test message");
** The advantage of using Print.println is that the message ("test message") can be 
** pre-pended with the actual location of the print statement itself.  For instance,
** Issuing a call to 'Print.dprintln("hello world")' may print a console message
** similar to the following:
**   [MyCustomClass.doSomething:291] hello world
** This indicates that the 'Print.dprintln' is located in the method 'doSomething'
** in the class 'MyCustomClass' at source file line number 291.  This will help
** show the precise location of various debug message print statements that should
** be removed prior to FCS. (Note: the package is remove from the class before
** printing the stack frame to avoid creating a print line which is excessively long)
**
** Difference between 'println' and 'dprintln':
** Each 'printXXX' method in this module has a corresponding 'dprintXXX' method.
** The 'dprintXXX' methods will only print their specified message if the System
** property "ri.debugMode" is true.  Additionally, 'dprintXXX' methods will always
** prepend the location (stackframe) of the 'Print.dprintXXX' call.
** The 'printXXX' methods always print their specified message to the console.
** However, the default is to _not_ include the location (stackframe) of the print 
** statement unless the System property "ri.print.showStackFrame" is true.  The 
** location of all print statements can then be exposed by setting this system 
** property to true.
**
** Replacements:
** Old method				  New Method
**  System.out.println("hello")	    	=> Print.println("hello")
**  <Throwable>.printStackTrace()   	=> Print.printStackTrace("Error", <Throwable>)
**  Thread.dumpStack()		    	=> Print.printStackTrace("Stack Trace");
**  if (debug) System.out.println("x")	=> Print.dprintln("x");  // not optimized
**  if (debug) System.out.println("x")	=> if (debug) Print.println("x");
**
** To turn on debug mode (to allow 'dprintXXX' to print), include system property:
**   -Dri.debugMode=true
**
** To turn have 'printXXX' methods print the stack frame location, include system property:
**   -Dri.print.showStackFrame=true
**
** -----------------------------------------------------------------------------
** @author Martin D. Flynn
*/

public class Print 
{

    /* -------------------------------------------------------------------------
    */

    /* System property keys */
    public static final String SYSTEM_DEBUG_MODE  = "ri.debugMode";
    public static final String SYSTEM_STACK_FRAME = "ri.print.showStackFrame";

    /* Throwable instance used to obtain stack information */
    private static Throwable stackTrace = null;

    /* by default the source file name is not printed in the location */
    private static boolean showStackFrameSource = false;

    /* -------------------------------------------------------------------------
    */

    /* Set the debug mode
    ** @param specified debug state
    */
    public static void setDebugMode(boolean state)
    {
	System.setProperty(SYSTEM_DEBUG_MODE, String.valueOf(state));
    }

    /* Return the current debug mode state
    ** @return debug state
    */
    public static boolean isDebugMode()
    {
	return Boolean.getBoolean(SYSTEM_DEBUG_MODE);
    }

    /* -------------------------------------------------------------------------
    */

    /* Set the state to force prepending the stack frame to 'print' statements
    ** @param specified printStackFrame state
    */
    public static void setPrintStackFrame(boolean state)
    {
	System.setProperty(SYSTEM_STACK_FRAME, String.valueOf(state));
    }

    /* return the current state of including the stack frame in 'print' statements
    ** @return printStackFrame state
    */
    public static boolean printStackFrame()
    {
	return Boolean.getBoolean(SYSTEM_STACK_FRAME);
    }

    /* -------------------------------------------------------------------------
    */

    /* Set the state to include the module source file rather than the normal stack frame
    ** @param specified showStackFrameSource state
    */
    public static void setShowStackFrameSource(boolean flag)
    {
	showStackFrameSource = flag;
    }

    /* -------------------------------------------------------------------------
    */

    /* Return the specified StackTraceElement
    ** @return the @see java.lang.StackTraceElement for the specified stack frame
    ** @param the specified stack frame to retrieve.
    */
    public static StackTraceElement getStackFrame(int frameNum)
    {
	if (stackTrace == null) { stackTrace = new Throwable(); }
	stackTrace.fillInStackTrace();
	return stackTrace.getStackTrace()[frameNum + 1];
    }

    /* Return the String form of the current StackTraceElement
    ** @return the @see java.lang.String version of the specified stack frame
    ** @param the specified stack frame to retrieve.
    */
    public static String getStackFrameString(int frameNum)
    {
	return getStackFrameString(frameNum + 1, showStackFrameSource);
    }

    /* Return the String form of the current StackTraceElement
    ** @return the @see java.lang.String version of the specified stack frame
    ** @param the StackTraceElement to convert to a string.
    */
    public static String getStackFrameString(StackTraceElement stackFrame)
    {
	return getStackFrameString(stackFrame, showStackFrameSource);
    }

    /* Return the String form of the current StackTraceElement
    ** @return the @see java.lang.String version of the specified stack frame
    ** @param the specified stack frame to retrieve.
    ** @param indicator whether the String should include the module source file name
    */
    public static String getStackFrameString(int frameNum, boolean showSrc)
    {
	return getStackFrameString(getStackFrame(frameNum + 1), showSrc);
    }

    /* Return the String form of the current StackTraceElement
    ** @return the @see java.lang.String version of the specified stack frame
    ** @param the StackTraceElement to convert to a string.
    ** @param indicator whether the String should include the module source file name
    */
    public static String getStackFrameString(StackTraceElement stackFrame, boolean showSrc)
    {
	StringBuffer sb = new StringBuffer();
	if (showSrc) {
	    String s = stackFrame.getFileName();
	    if (s != null) {
		int p = s.lastIndexOf(File.separator);
	        sb.append((p >= 0)? s.substring(p + 1) : s);
	    } else {
	        String c = stackFrame.getClassName();
	        int p = c.lastIndexOf(".");
	        sb.append("<").append((p >= 0)? c.substring(p + 1) : c).append(">");
	    }
	} else {
	    String c = stackFrame.getClassName();
	    int p = c.lastIndexOf(".");
	    sb.append((p >= 0)? c.substring(p + 1) : c);
	}
	sb.append(".").append(stackFrame.getMethodName());
	if (stackFrame.getLineNumber() >= 0) {
	    sb.append(":").append(stackFrame.getLineNumber());
	}
	return sb.toString();
    }

    /* -------------------------------------------------------------------------
    */

    /* Internal print statement
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the message to print.
    */
    protected static void _println(PrintStream out, int frameNum, String msg)
    {
	String m = (frameNum >= 0)? "[" + getStackFrameString(frameNum + 1) + "] " + msg : msg;
	print(out, m + "\n");
    }

    /* -------------------------------------------------------------------------
    */

    /* Print message to specified PrintStream (default to System.out)
    ** Does not include a linefeed at the end of the message.
    ** @param output @see java.io.PrintStream
    ** @param the message to print.
    */
    public static void print(PrintStream out, String msg)
    {
	((out != null)? out : System.out).print(msg);
    }

    /* Print message to specified PrintStream (default to System.out)
    ** Prints message only if debugMode system property is true.
    ** Does not include a linefeed at the end of the message.
    ** @param output @see java.io.PrintStream
    ** @param the message to print.
    */
    public static void dprint(PrintStream out, String msg)
    {
	if (isDebugMode()) {
	    print(out, msg);
	}
    }

    /* Print message to specified System.out
    ** Does not include a linefeed at the end of the message.
    ** @param the message to print.
    */
    public static void print(String msg)
    {
	print(null, msg);
    }

    /* Print message to specified System.out
    ** Prints message only if debugMode system property is true.
    ** Does not include a linefeed at the end of the message.
    ** @param the message to print.
    */
    public static void dprint(String msg)
    {
	if (isDebugMode()) {
	    print(null, msg);
	}
    }

    /* Print message to specified PrintStream (default to System.out)
    ** StackFrame information is included if the specified frame is >= 0, or if
    ** the showStackFrame system property is true.  To force the stack frame info to
    ** print, set the frameNum to '0' [eg.' Print.println(null, 0, "This location");']
    ** Message is terminated with a linefeed
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the message to print.
    */
    public static void println(PrintStream out, int frameNum, String msg)
    {
	int f = (frameNum >= 0)? (frameNum + 1) : (printStackFrame()? Math.abs(frameNum) : -1);
	_println(out, f, msg);
    }

    /* Print message to specified PrintStream (default to System.out)
    ** Prints message only if debugMode system property is true.
    ** StackFrame information is included.
    ** Message is terminated with a linefeed
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the message to print.
    */
    public static void dprintln(PrintStream out, int frameNum, String msg)
    {
	if (isDebugMode()) {
	    int f = (frameNum >= 0)? (frameNum + 1) : Math.abs(frameNum);
	    _println(out, f, msg);
	}
    }

    /* Print message to System.out
    ** StackFrame information is included if the specified frame is >= 0, or if
    ** the showStackFrame system property is true.  To force the stack frame info to
    ** print, set the frameNum to '0' [eg.' Print.println(0, "This location");']
    ** Message is terminated with a linefeed
    ** @param the stack frame location of the real 'print' statement.
    ** @param the message to print.
    */
    public static void println(int frameNum, String msg)
    {
	int f = (frameNum >= 0)? (frameNum + 1) : (printStackFrame()? Math.abs(frameNum) : -1);
	_println(null, f, msg);
    }

    /* Print message to System.out
    ** Prints message only if debugMode system property is true.
    ** StackFrame information is included.
    ** Message is terminated with a linefeed
    ** @param the stack frame location of the real 'print' statement.
    ** @param the message to print.
    */
    public static void dprintln(int frameNum, String msg)
    {
	if (isDebugMode()) {
	    int f = (frameNum >= 0)? (frameNum + 1) : Math.abs(frameNum);
	    _println(null, f, msg);
	}
    }

    /* Print message to specified PrintStream (default to System.out)
    ** StackFrame information is included if the showStackFrame system property is true.
    ** Message is terminated with a linefeed
    ** @param output @see java.io.PrintStream
    ** @param the message to print.
    */
    public static void println(PrintStream out, String msg)
    {
	_println(out, (printStackFrame()? 1 : -1), msg);
    }

    /* Print message to specified PrintStream (default to System.out)
    ** Prints message only if debugMode system property is true.
    ** StackFrame information is included.
    ** Message is terminated with a linefeed
    ** @param output @see java.io.PrintStream
    ** @param the message to print.
    */
    public static void dprintln(PrintStream out, String msg)
    {
	if (isDebugMode()) {
	    _println(out, 1, msg);
	}
    }

    /* Print message to System.out
    ** StackFrame information is included if the showStackFrame system property is true.
    ** Message is terminated with a linefeed
    ** @param the message to print.
    */
    public static void println(String msg)
    {
	_println(null, (printStackFrame()? 1 : -1), msg);
    }

    /* Print message to System.out
    ** Prints message only if debugMode system property is true.
    ** StackFrame information is included.
    ** Message is terminated with a linefeed
    ** @param the message to print.
    */
    public static void dprintln(String msg)
    {
	if (isDebugMode()) {
	    _println(null, 1, msg);
	}
    }

    /* -------------------------------------------------------------------------
    */

    private static PrintStream printStackTrace_Stream   = null;
    private static File        printStackTrace_LogFile  = null;

    /* Set default output file for logging stack traces (logging defaults to System.out)
    ** @param the output file to log stack traces
    */
    public static void setStackTraceLogFile(File logFile)
    {

	/* close old log file */
	if (printStackTrace_Stream != null) {
	    printStackTrace_Stream.close();
	    printStackTrace_Stream = null;
	    printStackTrace_LogFile = null;
	}

	/* open new log file */
	if ((logFile != null) && logFile.isAbsolute()) {
	    try {
	        printStackTrace_Stream = new PrintStream(new FileOutputStream(logFile), true);
		printStackTrace_LogFile = logFile;
	    } catch (IOException ioe) {
		Print.println(0, "Unable to open StackTrace log file: " + logFile);
	    }
	}

    }

    /* Format and print stack trace information
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the title of the stack trace.
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    private static void _printStackTrace(PrintStream out, int frame, String title, String msg, Throwable excp)
    {

	/* header vars */
	final String _dash = "----------------------------------------"; // 40
	final String dashLine = _dash + _dash + "\n"; // 80
	final String _ttl = " " + title + " ";
	final String errTitle = dashLine.substring(0, 16) + _ttl + dashLine.substring(16 + _ttl.length());

	/* default PrintStream */
	if (out == null) {
	    out = System.out;
	}

	/* header */
	Print.print(out, "\n");
	Print.print(out, dashLine + errTitle);
	Print.print(out, "[" + Print.getStackFrameString(frame + 1) + "]\n");
	if ((msg != null) && !msg.equals("")) { Print.print(out, msg + "\n"); }

	/* stack trace */
	if (excp != null) {
	    Print.print(out, excp.toString() + "\n");
	    Print.print(out, dashLine);
	    excp.printStackTrace(out);
	} else {
	    Print.print(out, dashLine);
	    Print.print(out, "Stack Trace:\n");
	    Throwable t = new Throwable();
	    t.fillInStackTrace();
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(bos);
	    t.printStackTrace(ps);
	    StringTokenizer st = new StringTokenizer(bos.toString(), "\n");
	    st.nextToken(); // "java.lang.Throwable"
	    for (int i = 0; i < frame + 1; i++) { st.nextToken(); } // discard superfluous stack frames
	    for (;st.hasMoreTokens();) { Print.println(out, st.nextToken()); }
	}

	/* final dashed line */
	Print.print(out, dashLine);
	Print.print(out, "\n");

    }

    /* Print stack trace information to specified PrintStream.
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the title of the stack trace.
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void printStackTrace(PrintStream out, int frame, String title, String msg, Throwable excp)
    {
	if (out == null) { out = printStackTrace_Stream; }
	if ((out != null) && (out != System.out) && (out != System.err)) {
	    Print.print("\n");
	    if (excp != null) {
	        String m = ((msg != null) && !msg.equals(""))? (msg + " - ") : "";
		Print.println(null, frame + 1, "Error: " + m + excp + "");
		StackTraceElement ste[] = excp.getStackTrace();
		if (ste.length > 0) {
	            Print.print("  ==> at " + getStackFrameString(ste[0]) + "\n");
		}
	    } else {
	        Print.println(null, frame + 1, msg);
	    }
	    if (printStackTrace_LogFile != null) {
	        Print.print("(Stack trace logged to '" + printStackTrace_LogFile + "')\n");
	    }
	    Print.print("\n");
	}
	_printStackTrace(out, frame + 1, title, msg, excp);
    }

    /* Print stack trace information to specified PrintStream.
    ** Prints stack trace only if debugMode system property is true.
    ** @param output @see java.io.PrintStream
    ** @param the stack frame location of the real 'print' statement.
    ** @param the title of the stack trace.
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void dprintStackTrace(PrintStream out, int frame, String title, String msg, Throwable excp)
    {
	if (isDebugMode()) {
	    if (out == null) { out = printStackTrace_Stream; }
	    if ((out != null) && (out != System.out) && (out != System.err)) {
	        String m = (excp != null)? (msg + " (" + excp + ")") : msg;
	        Print.println(null, frame + 1, "{log} " + m);
	    }
	    _printStackTrace(out, frame + 1, title, msg, excp); 
	}
    }

    /* Print stack trace information to default PrintStream.
    ** @param the title of the stack trace.
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void printStackTrace(String title, String msg, Throwable excp)
    {
	printStackTrace(null, 1, title, msg, excp);
    }

    /* Print stack trace information to default PrintStream.
    ** Prints stack trace only if debugMode system property is true.
    ** @param the title of the stack trace.
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void dprintStackTrace(String title, String msg, Throwable excp)
    {
	if (isDebugMode()) {
	    dprintStackTrace(null, 1, title, msg, excp);
	}
    }

    /* Print stack trace information to default PrintStream.
    ** Use this method instead of '<Throwable>.printStackTrace'
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void printStackTrace(String msg, Throwable excp)
    {
	String title = (excp != null)? "Exception" : "StackTrace";
	printStackTrace(null, 1, title, msg, excp);
    }

    /* Print stack trace information to default PrintStream.
    ** Prints stack trace only if debugMode system property is true.
    ** Use this method instead of '<Throwable>.printStackTrace'
    ** @param the message to print.
    ** @param the Throwable containing the stack trace to print.
    */
    public static void dprintStackTrace(String msg, Throwable excp)
    {
	if (isDebugMode()) {
	    String title = (excp != null)? "(DEBUG) Exception" : "(DEBUG) StackTrace";
	    dprintStackTrace(null, 1, title, msg, excp);
	}
    }

    /* Print stack trace information to default PrintStream.
    ** Use this method instead of 'Thread.dumpStack'
    ** @param the message to print.
    */
    public static void printStackTrace(String msg)
    {
	printStackTrace(null, 1, "StackTrace", msg, null);
    }

    /* Print stack trace information to default PrintStream.
    ** Prints stack trace only if debugMode system property is true.
    ** Use this method instead of 'Thread.dumpStack'
    ** @param the message to print.
    */
    public static void dprintStackTrace(String msg)
    {
	if (isDebugMode()) {
	    dprintStackTrace(null, 1, "(DEBUG) StackTrace", msg, null);
	}
    }

    /* Print stack trace information to default PrintStream.
    ** Allows specifying frame
    ** @param the stack frame to print.
    ** @param the message to print.
    */
    public static void printStackTrace(int frame, String msg)
    {
	printStackTrace(null, frame + 1, "StackTrace", msg, null);
    }

    /* Print stack trace information to default PrintStream.
    ** Prints stack trace only if debugMode system property is true.
    ** Allows specifying frame
    ** @param the stack frame to print.
    ** @param the message to print.
    */
    public static void dprintStackTrace(int frame, String msg)
    {
	if (isDebugMode()) {
	    dprintStackTrace(null, frame + 1, "(DEBUG) StackTrace", msg, null);
	}
    }

    public static String printStackTraceToString()
    {
        Throwable t = new Throwable("printStackTraceToString");
        t.fillInStackTrace();

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        printWriter.close();

        return stackTrace;
    }
}
