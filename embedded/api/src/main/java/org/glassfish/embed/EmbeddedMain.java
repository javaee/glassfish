/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embed.args.*;

/**
 *
 * @author bnevins
 */
public class EmbeddedMain {
    public static void main(String[] args) {
        if(args.length == 0)
            usage();

    }

    private static void usage()
    {
        try {
            InputStream is = EmbeddedMain.class.getResourceAsStream("/org/glassfish/embed/MainHelp.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            for (String s = br.readLine(); s != null; s = br.readLine()) {
                System.out .println(s);
            }
            System.out.println(Arg.toHelp(argDescriptions));
            System.exit(1);
        }
        catch (IOException ex) {
            Logger.getLogger(EmbeddedMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final static Arg[] argDescriptions = new Arg[]
    {
        //       longname       shortname   default or req      description
        new Arg("war",          "w",            false,          "War File"),
        
        /*new BoolArg("regexp", "r", false, "Regular Expression"),
        new Arg("dir", "d", ".", "Search Directory Root"),
        new Arg("ext", "x", "java", "File Extensions"),
        new BoolArg("ic", null, true, "Case Insensitive"),
        new BoolArg("filenameonly", "f", false, "Return Filenames Only"),
         */
    };
}
