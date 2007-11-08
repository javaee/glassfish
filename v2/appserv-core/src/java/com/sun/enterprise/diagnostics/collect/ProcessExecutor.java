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
package com.sun.enterprise.diagnostics.collect;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.logging.LogDomains;


/**
 * ProcessExecutor executes any OS specific commands as a sub process but does
 * not wait more than a specified timeout value for the process to complete.
 * This is a wrapper over java.lang.Runtime.getRuntime().exec()
 */
public class ProcessExecutor {

    static final Logger logger =
            Logger.getLogger(LogDomains.ADMIN_LOGGER);

    private String[] command;

    private long timeout;

    private ProcessRunner runner = null;

    /**
     * Create a new process executor object.
     * @param cmd AdminCommand to be executed. See java.lang.Runtime.exec(String[])
     *     for more information.
     * @param timeout time to wait for the process to complete. This is in
     *     milliseconds
     * @throws IllegalArgumentException if cmd is null or zero length array
     *     or timeout is <= 0
     */
    public ProcessExecutor(String[] cmd, long timeout) {
        if (cmd == null || cmd.length == 0) {
            throw new IllegalArgumentException(
                    "process.null_or_empty_command");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException(
                "process.invalid_timeout_value " + new Long(timeout));
        }
        this.command = cmd;
        this.timeout= timeout;
    }

    /**
     * Get command as a string.
     */
    public String getCommandString() {
        String cmdString = null;
        if (runner != null) {
            cmdString = runner.getCommandString();
        }
        if (cmdString == null) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < command.length; i++) {
                buf.append(command[i] + " ");
            }
            cmdString = buf.toString();
        }
        return cmdString;
    }

    /**
     * Get timeout value (in milliseconds)
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set timeout to specified value.
     * @param timeout time to wait for process to complete execution (in
     *     milliseconds)
     * @throws IllegalArgumentException if specified timeout <= 0
     */
    public void setTimeout(long timeout) {
        if (timeout >= 0) {
            this.timeout = timeout;
        } else {
            throw new IllegalArgumentException(
                    "process.invalid_timeout_value " + new Long(timeout));
        }
    }

    /**
     * Execute the command.
     * @throws IllegalStateException if process already executed
     * @throws ProcessExecutorException if process execution times out or if
     *      there is any error in execution
     */
    public String execute() throws ProcessExecutorException {
        if (runner != null) {
            throw new IllegalStateException(
                    "process.already_executed");
        }
        runner = new ProcessRunner(command, timeout);
        Thread runnerThread = new Thread(runner);
        runnerThread.start();
        try {
            runnerThread.join(timeout);
        } catch (InterruptedException ie) {
            logger.log(Level.FINEST, "process.waiter_interrupted",
                    getCommandString());
        }
        if (runnerThread.isAlive()) {
            if (!runner.completed) {
                logger.log(Level.FINEST, "process.interrupting",
                        new Object[] {new Long(timeout), getCommandString()});
                runnerThread.interrupt();
                try {
                    // Wait for 500 ms for thread to terminate
                    runnerThread.join(500);
                } catch (InterruptedException ie) {
                    // Ignore this exception. Interrupted while waiting for
                    // runner thread to respond to interrupt() call
                }
                if (!runner.completed && !runner.interrupted) {
                    // Thread did not finish, force the status to interrupted
                    runner.interrupted = true;
                }
            }
        }
        if (runner.interrupted || runner.exception != null) {
            if (runner.exception == null) {
                // Thread did not complete but there is no exception, assume
                // it did not finish because of timeout
                runner.makeTimeoutException();
            }
            throw runner.exception;
        }
        return runner.stdout.toString();
    }

    /**
     * Is process execution complete.
     * @ returns true if process execution was completed, false otherwise
     */
    public boolean isCompleted() {
        boolean completed = false;
        if (runner != null) {
            completed = runner.completed;
        }
        return completed;
    }

    /**
     * Is (was) process execution interrupted.
     * @ returns true if the process execution was interrupted (typically
     *      because it did not finish in specified timeout), false otherwise
     */
    public boolean isInterrupted() {
        boolean interrupted = false;
        if (runner != null) {
            interrupted = runner.interrupted;
        }
        return interrupted;
    }

    /**
     * Get standard output of the process.
     */
    public String getStdout() {
        String stdout = null;
        if (runner != null) {
            stdout = runner.stdout.toString();
        }
        return stdout;
    }

    /**
     * Get standard error of the process.
     */
    public String getStderr() {
        String stderr = null;
        if (runner != null) {
            stderr = runner.stderr.toString();
        }
        return stderr;
    }

    /**
     * Get exit code of the process.
     * @throws IllegalStateException if the process execution has not finished
     *     yet (or has not been started yet)
     */
    public int getExitCode() {
        int exitCode = 0;
        if (runner != null && runner.completed) {
            exitCode = runner.exitCode;
        } else {
            throw new IllegalStateException(
                    "process.not_yet_executed");
        }
        return exitCode;
    }

    /**
     * Execute the process again. All results of previous execution are lost.
     */
    public String executeAgain() throws ProcessExecutorException {
        runner = null;
        return execute();
    }
}

/**
 * This class does the actual process execution. This is run in a different
 * thread and the invoker thread waits on this thread.
 */
class ProcessRunner implements Runnable {

    static final Logger logger = ProcessExecutor.logger;
    long timeout;
    String[] cmd;
    StringBuffer stdout = new StringBuffer();
    StringBuffer stderr = new StringBuffer();
    int exitCode;
    boolean completed = false;
    boolean interrupted = false;
    ProcessExecutorException exception;
    private String cmdString = null;


    /**
     * Create a new process runner for specified command. The timeout value
     * passed in here is used only for creating log messages.
     */
    ProcessRunner(String[] cmd, long timeout) {
        this.cmd = cmd;
        this.timeout = timeout;
    }

    /**
     * Run command by creating a sub process
     */
    public void run() {
        Process process = null;
        InputStream is = null;
        InputStream es = null;
        try {
            try {
                process = Runtime.getRuntime().exec(cmd);
            } catch (IOException ioe) {
                logger.log(Level.FINE, "process.creation_failed", ioe);
                makeOtherException(ioe);
                return;
            }
            if (checkInterrupted()) {
                return;
            }
            is = process.getInputStream();
            es = process.getErrorStream();
            readFromStream(es, stderr);
            if (checkInterrupted()) {
                return;
            }
            readFromStream(is, stdout);
            if (checkInterrupted()) {
                return;
            }
            try {
                is.close();
                es.close();
                is = null;
                es = null;
            } catch (IOException ioe) {
                logger.log(Level.FINEST, "process.stream_close_error", ioe);
            }
            exitCode = process.waitFor();
            if (exitCode != 0) {
                makeAbnormalTerminationException();
            }
            process.destroy();
            process = null;
            completed = true;
        } catch (InterruptedException ie) {
            logger.log(Level.FINEST, "process.interrupted");
            interrupted = true;
            makeTimeoutException();
        } catch (Exception e) {
            logger.log(Level.FINEST, "process.execution_failed", e);
            makeOtherException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (es != null) {
                    es.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Throwable t) {
                // Do nothing
            }
        }
    }

    /**
     * Read data from specified stream and append it to buffer.
     */
    void readFromStream(InputStream stream, StringBuffer buffer) {
        byte[] bytes = new byte[1024];
        try {
            int count = 0;
            while ((count = stream.read(bytes)) != -1) {
                buffer.append(new String(bytes, 0 , count));
                if (checkInterrupted()) {
                    return;
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.FINEST, "process.stream_read_error", ioe);
        }
    }

    /**
     * Check whether current thread has been interrupted and if so set
     * exception appropriately (caused by timeout in this case)
     */
    private boolean checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            interrupted = true;
            makeTimeoutException();
        }
        return interrupted;
    }

    /**
     * Helper method to set ProcessExecutor exception
     */
    void makeTimeoutException() {
        String outs = stderr.toString() + "\n" + stdout.toString();
        exception = new ProcessExecutorException("process.timeout",
                "Process timed out.\nTimeout was {2} msecs\n"
                + "Attempted command: {0}\nOutput from command: {1}",
                new Object[] { getCommandString(), outs, new Long(timeout) });
    }

    /**
     * Helper method to set ProcessExecutor exception
     */
    void makeAbnormalTerminationException() {
        String outs = stderr.toString() + "\n" + stdout.toString();
        exception = new ProcessExecutorException("process.abnormal_termination",
                "Abnormal process termination -- process returned: {0}\n"
                +  "Attempted command: {1}\nOutput from command: {2}",
                    new Object[] { new Integer(exitCode), getCommandString(),
                    outs} );
    }

    /**
     * Helper method to set ProcessExecutor exception
     */
    void makeOtherException(Throwable t) {
        String outs = stderr.toString() + "\n" + stdout.toString();
        exception = new ProcessExecutorException("process.unknown_exception",
                "Abnormal process termination -- process threw an Exception.\n"
                + "Attempted command: {0}\nOutput from command: {1}",
                new Object[] { getCommandString(), outs}, t );
    }

    /**
     * Get command as a string
     */
    String getCommandString() {
        if (cmdString == null) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < cmd.length; i++) {
                buf.append(cmd[i] + " ");
            }
            cmdString = buf.toString();
        }
        return cmdString;
    }
}
