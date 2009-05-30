/*
 * ProcessManager.java
 * Use this class for painless process spawning.
 * This class was specifically written to be compatable with 1.4
 * @since JDK 1.4
 * @author bnevins
 * Created on October 28, 2005, 10:08 PM
 */
package com.sun.enterprise.universal.process;

import java.io.*;
import java.util.*;

/**
 *
 */
public class ProcessManager {

    public ProcessManager(String... cmds) {
        cmdline = cmds;
    }

    ////////////////////////////////////////////////////////////////////////////
    public ProcessManager(List<String> Cmdline) {
        cmdline = new String[Cmdline.size()];
        cmdline = (String[]) Cmdline.toArray(cmdline);
    }

    ////////////////////////////////////////////////////////////////////////////
    public final void setTimeoutMsec(int num) {
        if (num > 0) {
            timeout = num;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public final void setStdinLines(List<String> list) {
        if (list != null && list.size() > 0) {
            stdinLines = new String[list.size()];
            stdinLines = (String[]) list.toArray(cmdline);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public final int execute() throws ProcessManagerException {
        try {
            sb_out = new StringBuffer();
            sb_err = new StringBuffer();

            Runtime rt = Runtime.getRuntime();
            process = rt.exec(cmdline);
            writeStdin();
            readStream("stderr", process.getErrorStream(), sb_err);
            readStream("stdout", process.getInputStream(), sb_out);
            await();

            try {
                exit = process.exitValue();
                wasError = false;
            } catch (IllegalThreadStateException tse) {
                // this means that the process is still running...
                process.destroy();
                throw new ProcessManagerTimeoutException(tse);
            }
        } catch (ProcessManagerException pme) {
            throw pme;
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }

            throw new ProcessManagerException(e);
        }

        return exit;
    }

    ////////////////////////////////////////////////////////////////////////////
    public final String getStdout() {
        return sb_out.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    public final String getStderr() {
        return sb_err.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    public final int getExitValue() {
        return exit;
    }

    ////////////////////////////////////////////////////////////////////////////
    public String toString() {
        return Arrays.toString(cmdline);
    }

    ////////////////////////////////////////////////////////////////////////////
    private void writeStdin() throws ProcessManagerException {
        if (stdinLines == null || stdinLines.length <= 0) {
            return;
        }

        PrintWriter pipe = null;

        try {
            pipe = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

            for (int i = 0; i < stdinLines.length; i++) {
                debug("InputLine ->" + stdinLines[i] + "<-");
                pipe.println(stdinLines[i]);
            }
            pipe.flush();
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        } finally {
            try {
                pipe.close();
            } catch (Throwable t) {
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void readStream(String name, InputStream stream, StringBuffer sb) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        Thread thread = new Thread(new ReaderThread(reader, sb), name);
        threads.add(thread);
        thread.start();
    }

    ////////////////////////////////////////////////////////////////////////////
    private void await() throws InterruptedException {
        if (timeout <= 0) {
            waitForever();
        } else {
            waitAwhile();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void waitForever() throws InterruptedException {
        process.waitFor();

        // wait for stdin and stderr to finish up
        for (Thread t : threads) {
            t.join();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void waitAwhile() throws InterruptedException {
        {
            Thread processWaiter = new Thread(new TimeoutThread(process));
            processWaiter.start();
            processWaiter.join(timeout);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void debug(String s) {
        if (debugOn) {
            System.out.println(s);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        try {
            if (args.length <= 0) {
                System.out.println("Usage: ProcessManager cmd arg1 arg2 ... argn");
                System.exit(1);
            }

            List<String> cmds = new ArrayList<String>();

            for (int i = 0; i < args.length; i++) {
                cmds.add(args[i]);
            }

            ProcessManager pm = new ProcessManager(cmds);
            pm.execute();

            System.out.println("*********** STDOUT ***********\n" + pm.getStdout());
            System.out.println("*********** STDERR ***********\n" + pm.getStderr());
            System.out.println("*********** EXIT VALUE: " + pm.getExitValue());
        } catch (ProcessManagerException pme) {
            pme.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    private String[] cmdline;
    private StringBuffer sb_out;
    private StringBuffer sb_err;
    private int exit = -1;
    private int timeout;
    private Process process;
    private boolean wasError = true;
    private static final boolean debugOn = false;
    private String[] stdinLines;
    private List<Thread> threads = new ArrayList<Thread>(2);

    ////////////////////////////////////////////////////////////////////////////
    static class ReaderThread implements Runnable {

        ReaderThread(BufferedReader Reader, StringBuffer SB) {
            reader = Reader;
            sb = SB;
        }

        public void run() {
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    sb.append(line).append('\n');
                    System.out.println(line);
                }
            } catch (Exception e) {
            }
            ProcessManager.debug("ReaderThread exiting...");
        }
        private BufferedReader reader;
        private StringBuffer sb;
    }

    static class TimeoutThread implements Runnable {

        TimeoutThread(Process p) {
            process = p;
        }

        public void run() {
            try {
                process.waitFor();
            } catch (Exception e) {
            }
            ProcessManager.debug("TimeoutThread exiting...");
        }
        private Process process;
    }
}
