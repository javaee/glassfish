

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.util;

import java.lang.Process;
import java.io.File;
import java.io.Writer;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
// import org.apache.catalina.util.StringManager;


//class CGIServlet


/**
     * Encapsulates the knowledge of how to run a CGI script, given the
     * script's desired environment and (optionally) input/output streams
     *
     * <p>
     *
     * Exposes a <code>run</code> method used to actually invoke the
     * CGI.
     *
     * </p>
     * <p>
     *
     * The CGI environment and settings are derived from the information
     * passed to the constuctor.
     *
     * </p>
     * <p>
     *
     * The input and output streams can be set by the <code>setInput</code>
     * and <code>setResponse</code> methods, respectively.
     * </p>
     *
     * @author    Martin Dengler [root@martindengler.com]
     * @version   $Revision: 1.4 $, $Date: 2006/11/06 21:13:39 $
     */
public class ProcessHelper {

private static com.sun.org.apache.commons.logging.Log log=
    com.sun.org.apache.commons.logging.LogFactory.getLog( ProcessHelper.class );

/** script/command to be executed */
private String command = null;

/** environment used when invoking the cgi script */
private Hashtable env = null;

/** working directory used when invoking the cgi script */
private File wd = null;

/** query parameters to be passed to the invoked script */
private Hashtable params = null;

/** stdin to be passed to cgi script */
private InputStream stdin = null;

/** response object used to set headers & get output stream */
private HttpServletResponse response = null;

/** boolean tracking whether this object has enough info to run() */
private boolean readyToRun = false;

/** the debugging detail level for this instance. */
private int debug = 0;

/** the time in ms to wait for the client to send us CGI input data */
private int iClientInputTimeout;

/**
 *  Creates a ProcessHelper and initializes its environment, working
 *  directory, and query parameters.
 *  <BR>
 *  Input/output streams (optional) are set using the
 *  <code>setInput</code> and <code>setResponse</code> methods,
 *  respectively.
 *
 * @param  command  string full path to command to be executed
 * @param  env      Hashtable with the desired script environment
 * @param  wd       File with the script's desired working directory
 * @param  params   Hashtable with the script's query parameters
 *
 * @param  res       HttpServletResponse object for setting headers
 *                   based on CGI script output
 *
 */
public ProcessHelper(String command, Hashtable env, File wd,
                    Hashtable params) {
    this.command = command;
    this.env = env;
    this.wd = wd;
    this.params = params;
    updateReadyStatus();
}



/**
 * Checks & sets ready status
 */
protected void updateReadyStatus() {
    if (command != null
        && env != null
        && wd != null
        && params != null
        && response != null) {
        readyToRun = true;
    } else {
        readyToRun = false;
    }
}



/**
 * Gets ready status
 *
 * @return   false if not ready (<code>run</code> will throw
 *           an exception), true if ready
 */
public boolean isReady() {
    return readyToRun;
}



/**
 * Sets HttpServletResponse object used to set headers and send
 * output to
 *
 * @param  response   HttpServletResponse to be used
 *
 */
public void setResponse(HttpServletResponse response) {
    this.response = response;
    updateReadyStatus();
}



/**
 * Sets standard input to be passed on to the invoked cgi script
 *
 * @param  stdin   InputStream to be used
 *
 */
public void setInput(InputStream stdin) {
    this.stdin = stdin;
    updateReadyStatus();
}



/**
 * Converts a Hashtable to a String array by converting each
 * key/value pair in the Hashtable to a String in the form
 * "key=value" (hashkey + "=" + hash.get(hashkey).toString())
 *
 * @param  h   Hashtable to convert
 *
 * @return     converted string array
 *
 * @exception  NullPointerException   if a hash key has a null value
 *
 */
private String[] hashToStringArray(Hashtable h)
    throws NullPointerException {
    Vector v = new Vector();
    Enumeration e = h.keys();
    while (e.hasMoreElements()) {
        String k = e.nextElement().toString();
        v.add(k + "=" + h.get(k));
    }
    String[] strArr = new String[v.size()];
    v.copyInto(strArr);
    return strArr;
}



/**
 * Executes a process script with the desired environment, current working
 * directory, and input/output streams
 *
 * <p>
 * This implements the following CGI specification recommedations:
 * <UL>
 * <LI> Servers SHOULD provide the "<code>query</code>" component of
 *      the script-URI as command-line arguments to scripts if it
 *      does not contain any unencoded "=" characters and the
 *      command-line arguments can be generated in an unambiguous
 *      manner.
 * <LI> Servers SHOULD set the AUTH_TYPE metavariable to the value
 *      of the "<code>auth-scheme</code>" token of the
 *      "<code>Authorization</code>" if it was supplied as part of the
 *      request header.  See <code>getCGIEnvironment</code> method.
 * <LI> Where applicable, servers SHOULD set the current working
 *      directory to the directory in which the script is located
 *      before invoking it.
 * <LI> Server implementations SHOULD define their behavior for the
 *      following cases:
 *     <ul>
 *     <LI> <u>Allowed characters in pathInfo</u>:  This implementation
 *             does not allow ASCII NUL nor any character which cannot
 *             be URL-encoded according to internet standards;
 *     <LI> <u>Allowed characters in path segments</u>: This
 *             implementation does not allow non-terminal NULL
 *             segments in the the path -- IOExceptions may be thrown;
 *     <LI> <u>"<code>.</code>" and "<code>..</code>" path
 *             segments</u>:
 *             This implementation does not allow "<code>.</code>" and
 *             "<code>..</code>" in the the path, and such characters
 *             will result in an IOException being thrown;
 *     <LI> <u>Implementation limitations</u>: This implementation
 *             does not impose any limitations except as documented
 *             above.  This implementation may be limited by the
 *             servlet container used to house this implementation.
 *             In particular, all the primary CGI variable values
 *             are derived either directly or indirectly from the
 *             container's implementation of the Servlet API methods.
 *     </ul>
 * </UL>
 * </p>
 *
 * For more information, see java.lang.Runtime#exec(String command, 
 * String[] envp, File dir)
 *
 * @exception IOException if problems during reading/writing occur
 *
 */
public void run() throws IOException {

    /*
     * REMIND:  this method feels too big; should it be re-written?
     */

    if (!isReady()) {
        throw new IOException(this.getClass().getName()
                              + ": not ready to run.");
    }

    if (debug >= 1 ) {
        log("runCGI(envp=[" + env + "], command=" + command + ")");
    }

    if ((command.indexOf(File.separator + "." + File.separator) >= 0)
        || (command.indexOf(File.separator + "..") >= 0)
        || (command.indexOf(".." + File.separator) >= 0)) {
        throw new IOException(this.getClass().getName()
                              + "Illegal Character in CGI command "
                              + "path ('.' or '..') detected.  Not "
                              + "running CGI [" + command + "].");
    }

    /* original content/structure of this section taken from
     * http://developer.java.sun.com/developer/
     *                               bugParade/bugs/4216884.html
     * with major modifications by Martin Dengler
     */
    Runtime rt = null;
    BufferedReader commandsStdOut = null;
    BufferedReader commandsStdErr = null;
    BufferedOutputStream commandsStdIn = null;
    Process proc = null;
    byte[] bBuf = new byte[1024];
    char[] cBuf = new char[1024];
    int bufRead = -1;

    //create query arguments
    Enumeration paramNames = params.keys();
    StringBuffer cmdAndArgs = new StringBuffer(command);
    if (paramNames != null && paramNames.hasMoreElements()) {
        cmdAndArgs.append(" ");
        while (paramNames.hasMoreElements()) {
            String k = (String) paramNames.nextElement();
            String v = params.get(k).toString();
            if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                cmdAndArgs.append(k);
                cmdAndArgs.append("=");
                v = java.net.URLEncoder.encode(v);
                cmdAndArgs.append(v);
                cmdAndArgs.append(" ");
            }
        }
    }

    String postIn = getPostInput(params);
    int contentLength = (postIn.length()
            + System.getProperty("line.separator").length());
    if ("POST".equals(env.get("REQUEST_METHOD"))) {
        env.put("CONTENT_LENGTH", Integer.valueOf(contentLength));
    }

    rt = Runtime.getRuntime();
    proc = rt.exec(cmdAndArgs.toString(), hashToStringArray(env), wd);


    /*
     * provide input to cgi
     * First  -- parameters
     * Second -- any remaining input
     */
    commandsStdIn = new BufferedOutputStream(proc.getOutputStream());
    if (debug >= 2 ) {
        log("runCGI stdin=[" + stdin + "], qs="
            + env.get("QUERY_STRING"));
    }
    if ("POST".equals(env.get("REQUEST_METHOD"))) {
        if (debug >= 2) {
            log("runCGI: writing ---------------\n");
            log(postIn);
            log("runCGI: new content_length=" + contentLength
                + "---------------\n");
        }
        commandsStdIn.write(postIn.getBytes());
    }
    if (stdin != null) {
        //REMIND: document this
        /* assume if nothing is available after a time, that nothing is
         * coming...
         */
        if (stdin.available() <= 0) {
            if (debug >= 2 ) {
                log("runCGI stdin is NOT available ["
                    + stdin.available() + "]");
            }
            try {
                Thread.currentThread().sleep(iClientInputTimeout);
            } catch (InterruptedException ignored) {
            }
        }
        if (stdin.available() > 0) {
            if (debug >= 2 ) {
                log("runCGI stdin IS available ["
                    + stdin.available() + "]");
            }
            bBuf = new byte[1024];
            bufRead = -1;
            try {
                while ((bufRead = stdin.read(bBuf)) != -1) {
                    if (debug >= 2 ) {
                        log("runCGI: read [" + bufRead
                            + "] bytes from stdin");
                    }
                    commandsStdIn.write(bBuf, 0, bufRead);
                }
                if (debug >= 2 ) {
                    log("runCGI: DONE READING from stdin");
                }
            } catch (IOException ioe) {
                //REMIND: replace with logging
                //REMIND: should I throw this exception?
                log("runCGI: couldn't write all bytes.");
                ioe.printStackTrace();
            }
        }
    }
    commandsStdIn.flush();
    commandsStdIn.close();

    /* we want to wait for the process to exit,  Process.waitFor()
     * is useless in our situation; see
     * http://developer.java.sun.com/developer/
     *                               bugParade/bugs/4223650.html
     */

    boolean isRunning = true;
    commandsStdOut = new BufferedReader
        (new InputStreamReader(proc.getInputStream()));
    commandsStdErr = new BufferedReader
        (new InputStreamReader(proc.getErrorStream()));
    BufferedWriter servletContainerStdout = null;

    try {
        if (response.getOutputStream() != null) {
            servletContainerStdout =
                new BufferedWriter(new OutputStreamWriter
                    (response.getOutputStream()));
        }
    } catch (IOException ignored) {
        //NOOP: no output will be written
    }

    while (isRunning) {

        try {
            //read stderr first
            cBuf = new char[1024];
            while ((bufRead = commandsStdErr.read(cBuf)) != -1) {
                if (servletContainerStdout != null) {
                    servletContainerStdout.write(cBuf, 0, bufRead);
                }
            }

            //set headers
            String line = null;
            while (((line = commandsStdOut.readLine()) != null)
                   && !("".equals(line))) {
                if (debug >= 2) {
                    log("runCGI: addHeader(\"" + line + "\")");
                }
                if (line.startsWith("HTTP")) {
                    //TODO: should set status codes (NPH support)
                    /*
                     * response.setStatus(getStatusCode(line));
                     */
                } else {
                    response.addHeader
                        (line.substring(0, line.indexOf(":")).trim(),
                         line.substring(line.indexOf(":") + 1).trim());
                }
            }

            //write output
            cBuf = new char[1024];
            while ((bufRead = commandsStdOut.read(cBuf)) != -1) {
                if (servletContainerStdout != null) {
                    if (debug >= 4) {
                        log("runCGI: write(\"" + cBuf + "\")");
                    }
                    servletContainerStdout.write(cBuf, 0, bufRead);
                }
            }

            if (servletContainerStdout != null) {
                servletContainerStdout.flush();
            }

            proc.exitValue(); // Throws exception if alive

            isRunning = false;

        } catch (IllegalThreadStateException e) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    } //replacement for Process.waitFor()


}


/**
 * Gets a string for input to a POST cgi script
 *
 * @param  params   Hashtable of query parameters to be passed to
 *                  the CGI script
 * @return          for use as input to the CGI script
 */

protected String getPostInput(Hashtable params) {
    String lineSeparator = System.getProperty("line.separator");
    Enumeration paramNames = params.keys();
    StringBuffer postInput = new StringBuffer("");
    StringBuffer qs = new StringBuffer("");
    if (paramNames != null && paramNames.hasMoreElements()) {
        while (paramNames.hasMoreElements()) {
            String k = (String) paramNames.nextElement();
            String v = params.get(k).toString();
            if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                postInput.append(k);
                qs.append(k);
                postInput.append("=");
                qs.append("=");
                postInput.append(v);
                qs.append(v);
                postInput.append(lineSeparator);
                qs.append("&");
            }
        }
    }
    qs.append(lineSeparator);
    return qs.append(postInput).toString();
}


private void log(String s) {
    if (log.isDebugEnabled())
        log.debug(s);
}


public int getIClientInputTimeout(){
        return iClientInputTimeout;
    }


public void setIClientInputTimeout(int iClientInputTimeout){
        this.iClientInputTimeout = iClientInputTimeout;
    }
}


