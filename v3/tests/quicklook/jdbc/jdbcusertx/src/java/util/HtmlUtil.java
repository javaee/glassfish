package myapp.util;

import java.io.PrintWriter;

/**
 *
 * @author jagadish
 */
public class HtmlUtil {

        public static void printException(Throwable e, PrintWriter out) {
        StackTraceElement elements[] = e.getStackTrace();
        out.println("Following exception occurred :<br>");
        out.println(e.getMessage() + "<br>");
        for (StackTraceElement element : elements) {
            out.println(element.toString() + "<br>");
        }
    }
        public static void printHR(PrintWriter out){
            out.println("<hr>");
        }

}
