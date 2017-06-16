package org.glassfish.jdbc.devtests.v3.util;

import java.io.PrintWriter;

/**
 *
 * @author jagadish
 */
public class HtmlUtil {

    /**
     * Prints the exceptions generated.
     * @param e
     * @param out
     */
    public static void printException(Throwable e, PrintWriter out) {
        StackTraceElement elements[] = e.getStackTrace();
        out.println("Following exception occurred :<br>");
        out.println(e.getMessage() + "<br>");
        for (StackTraceElement element : elements) {
            out.println(element.toString() + "<br>");
        }
    }
    
    /**
     * Prints a horizontal ruler.
     * @param out
     */
    public static void printHR(PrintWriter out){
        out.println("<hr>");
    }
}
