package com.sun.enterprise.admin.cli;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;


/**
 *  CLIProcessExecutor
 *  A simple process executor class that is used by CLI.
 *  @author  jane.young@sun.com
 */
public class CLIProcessExecutor
{
    Process process;

    public CLIProcessExecutor() {
        process = null;
    }
    

    /**
     * This method invokes the runtime exec
     * @param cmd the command to execute
     * @param wait if true, wait for process to end.
     * @exception Exception
     */
    public void execute(String[] cmd, boolean wait) throws Exception
    {
        process=Runtime.getRuntime().exec(cmd);
            //process = new ProcessBuilder(cmd).start();
            
        // start stream flusher to push output to parent streams and null.
        StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), System.err, null);
        sfErr.start();

        // set flusher on stdout also, if not could stop with too much output
        StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), System.out, null);
        sfOut.start();
        try {
            // must sleep for a couple of seconds, so if there is a jvm startup error,
            //the parent process
            //is around to catch and report it when the process in executed in verbose mode.
            Thread.currentThread().sleep(5000);
            //wait is not required for command like start database
            //where the process does not return since start database
            //spawn it's own process.
            if (wait) {
                process.waitFor();
            }
        }
        catch (InterruptedException ie) {
        }
    }

   /**
      return the exit value of this process.
      if process is null, then there is no process running
      therefore the return value is 0.
    */
    public int exitValue() {
        if (process == null) return -1;
        return process.exitValue();
    }

}


class StreamFlusher extends Thread {
    
    private InputStream _input=null;
    private OutputStream _output=null;
    private String _logFile=null;

    
    public StreamFlusher(InputStream input, OutputStream output) {
        this(input, output, null);
    }

    
    public StreamFlusher(InputStream input, OutputStream output, String logFile) {
        this._input=input;
        this._output=output;
        this._logFile=logFile;
    }
    
    public void run() {
        
        // check for null stream
        if (_input == null) return;
        
        PrintStream printStream=null;
        
        // If applicable, write to a log file
        if (_logFile != null) {
            try {
                if(createFileStructure(_logFile)) {
                    // reset streams to logfile
                    printStream = new PrintStream(new FileOutputStream(_logFile, true), true);
                } else {
                    // could not write to log for some reason
                    _logFile=null;
                }
            } catch (IOException ie) {
                ie.printStackTrace();
                _logFile=null;
            }
        }
        
        // transfer bytes from input to output stream
        try {
            int byteCnt=0;
            byte[] buffer=new byte[4096];
            while ((byteCnt=_input.read(buffer)) != -1) {
                if (_output != null && byteCnt > 0) {
                    _output.write(buffer, 0, byteCnt);
                    _output.flush();
                    
                    // also send to log, if it exists
                    if (_logFile != null) {
                        printStream.write(buffer, 0, byteCnt);
                        printStream.flush();
                    }
                }
                yield();
            }
        } catch (IOException e) {
            // shouldn't matter
        }
    }

    
    /**
     * createFileStructure - This method validates that that the file can be written to.  It the
     * if the parent directory structure does not exist, it will be created
     *
     * @param logFile - fully qualified path of the logfile
     */
    protected boolean createFileStructure(String logFile) {
        boolean bRet=false;
        File outputFile=new File(logFile);
        
        try {
            // Verify that we can write to the output file
            File parentFile = new File(outputFile.getParent());
            // To take care of non-existent log directories
            if ( !parentFile.exists() ) {
                // Trying to create non-existent parent directories
                parentFile.mkdirs();
            }
            // create the file if it doesn't exist
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            if (outputFile.canWrite()) {
                // everything is okay to logfile
                bRet=true;
            }
        } catch (IOException e) {
            // will only see on verbose more, so okay
            e.printStackTrace();
        }

        return bRet;
    }    
}
