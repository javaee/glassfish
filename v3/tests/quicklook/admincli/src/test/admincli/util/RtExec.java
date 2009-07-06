/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.admincli.util;

import java.io.*;

/**
 *
 * @author Administrator
 */
public class RtExec {
    private static boolean result=false;
    private static Process proc;
    private static Runtime rt = Runtime.getRuntime();
    private static int exitVal = 101;

    public static boolean execute(String cmd) throws IOException {
        try {
            proc = rt.exec(cmd);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // Flush out the OUTPUT and ERROR
            errorGobbler.start();
            outputGobbler.start();

            // Checking exit satus
            exitVal = proc.waitFor();
//            System.out.println("ExitValue: " + exitVal);
            if (exitVal == 0){
                result = true;
            } else {
                result = false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }
}
