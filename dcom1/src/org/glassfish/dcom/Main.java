package org.glassfish.dcom;

import java.util.Random;
import org.jinterop.dcom.test.MSShell;
import org.jinterop.dcom.test.TestWinReg;

/**
 *
 * @author wnevins
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("HELLO DCOM!!");
           TestWinReg.main(getWinRegArgs());
        //MSShell.main(authInfo);
    }

    private static String[] getWinRegArgs() {
        // address domain username password keyname
        String regitem = "rrrr" + new Random(System.nanoTime()).nextLong();
        String[] ret = new String[5];
        ret[0] = authInfo[0];
        ret[1] = authInfo[1];
        ret[2] = authInfo[2];
        ret[3] = authInfo[3];
        ret[4] = regitem;
        System.out.println("REGITEM: " + regitem);
        return ret;
    }
    // address domain username password
    static final String[] authInfo = new String[]{
        //"wnevins-lnr",
        //"wnevins-lnr",
        //"wnevins-lap",
        //"wnevins-lap",
        //"bnevins.com",
        //"bnevins.com",
        //"wnevins",
        //"Benny235",};

        "wnevins-lnr",
        "wnevins-lnr",
        "wnevins",
        "Swteim235",
    };



}
/*
 * %JAVA_HOME%\bin\java -classpath ../../lib;../../lib/j-interopdeps.jar;../../lib/jcifs-1.2.19.jar;../../lib/j-interop.jar
org.jinterop.dcom.test.TestWinReg %1 %2 %3 %4 %5
execute wnevins-lnr wnevins-lnr wnevins password zzzz0000001

d:\gf_other\j-Interop\examples\WinReg>
 */