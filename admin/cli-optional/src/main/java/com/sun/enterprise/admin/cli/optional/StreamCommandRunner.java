package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.admin.cli.AsadminMain;

import java.io.*;
import java.util.Arrays;

/**  A scaled-down implementation of multi-mode command.
 * @author केदार(km@dev.java.net)
 */
public class StreamCommandRunner extends S1ASCommand {
    
    public void runCommand() throws CommandException, CommandValidationException {
        super.validateOptions();
        String fname = super.getOption("file");
        BufferedReader reader = null;
        try {
            if (fname == null) {
                if (System.console() == null) {
                    throw new CommandException("To use this command, either provide correct file name, or be connected to terminal");

                }
                System.out.println("Enter commands one per \"line\", ^D to quit");
                reader = new BufferedReader(new InputStreamReader(System.in));
            }
            else {
                File file = new File(fname);
                if (!file.canRead()) {
                    throw new CommandException("File: " + fname + " can not be read");
                }
                reader = new BufferedReader(new FileReader(file));
            }
            executeCommands(reader);
        } catch(IOException e) {
            throw new CommandException(e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch(IOException e) {
                //can't do much
                throw new CommandException(e);
            }
        }
    }

    private void executeCommands(BufferedReader reader) throws CommandException, IOException {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] args = getArgs(line);
                execute(args);
            }
    }

    private String[] getArgs(String line) {
        //for now, just split on white-space character, this is not enough for args (quoted) with white spaces in them
        String regex = "\\s";
        String[] parts = line.split(regex);
        return parts;
    }
    
    private void execute(String[] args) throws CommandException {
        AsadminMain main = new AsadminMain();
        int code;                   // a dead store per findbugs - tbd
        try {
            code = main.local(args);
            System.out.println("Ran: " + Arrays.toString(args) + " locally");
        } catch(Exception e) {
            try {
                code = main.remote(args);
                System.out.println("Ran: " + Arrays.toString(args) + " remotely");
            } catch(Exception ee) {
                System.out.println("Error executing command: " + Arrays.toString(args) + " " + ee.getMessage());
            }
        }
    }
}

