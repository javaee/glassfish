package org.glassfish.javaee7.batch.test.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: makannan
 * Date: 4/5/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandUtil {

    private int exitCode;

    private Throwable cause;

    private List<String> result = new ArrayList<String>();

    private CommandUtil() {}

    public static CommandUtil getInstance() {
        return new CommandUtil();
    }

    public CommandUtil executeCommandAndGetAsList(String... command) {
        return executeCommandAndGetAsList(true, command);
    }

    public CommandUtil executeCommandAndGetAsList(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }
    public CommandUtil executeCommandAndGetErrorOutput(String... command) {
        return executeCommandAndGetErrorOutput(true, command);
    }

    public CommandUtil executeCommandAndGetErrorOutput(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }

    public boolean ranOK() {
        return cause == null && exitCode == 0;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Throwable getCause() {
        return cause;
    }

    public List<String> result() {
        return result;
    }
}
