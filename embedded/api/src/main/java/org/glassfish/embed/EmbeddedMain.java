/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embed.args.*;
import org.glassfish.embed.util.StringUtils;

/**
 * 
 * @author bnevins
 */
public class EmbeddedMain {
    public static void main(String[] args) {
        try {
            if(args.length == 0)
                usage();

            // parse commandline arguments
            ArgProcessor proc = new ArgProcessor(argDescriptions, args);
            Map<String, String> params = proc.getParams();
            List<String> operands = proc.getOperands();

            if(Boolean.parseBoolean(params.get("help")))
                usage();

            LoggerHelper.fine("params size = " + params.size());
            Set<Map.Entry<String,String>> set = params.entrySet();
            
            for(Map.Entry<String,String> entry : set) {
                LoggerHelper.fine(entry.getKey() + "=" + entry.getValue());
            }
            
            // create an Info object based on the commandline args
            EmbeddedInfo info = paramsToInfo(params, operands);
            LoggerHelper.finer(info.toString());
            EmbeddedRunner runner = new EmbeddedRunner(info);
            runner.run();
        }
        catch(Exception e) {
            LoggerHelper.severe(e.toString());
            //usage();
        }
    }

    /**
     * This method knows and understands what the commandline args mean...
     * Do minimal error detection here.  The ironclad checking is done in
     * the Info object later.
     */
    private static EmbeddedInfo paramsToInfo(Map<String, String> params, List<String> operands) throws EmbeddedException {
        EmbeddedInfo info = new EmbeddedInfo();
        
        /*  Use operands for war filenames -- for now....
        String warName = params.get("war");
        
        if(StringUtils.ok(warName)) {
           info.addArchive(new File(warName)); 
        }
         */

        for(String s : operands) {
            if(StringUtils.ok(s)) {
               info.addArchive(new File(s));
            }
        }
        
        String port = params.get("port");
        
        if(!StringUtils.ok(port)) 
            throw new EmbeddedException("internal", StringHelper.get("no_default_http_port"));
        
        try {
            info.setHttpPort(Integer.parseInt(port));
        }
        catch(NumberFormatException nfe) {
            throw new EmbeddedException("port_not_int", port);
        }
        
        return info;
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
        
        System.exit(1);
    }

    private final static Arg[] argDescriptions = new Arg[]
    {
        //       longname       shortname   default or req                                      description
        //new Arg("war",          "w",            false,                                          "War File"),
        new Arg("port",         "p",            "" + ServerConstants.DEFAULT_HTTP_PORT,          "HTTP Port"),
        new BoolArg("help",     "h",            false,                                         "Help"),
    };
    
    private LocalStringsImpl strings = new LocalStringsImpl(this.getClass());
}
