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

import org.glassfish.embed.util.LoggerHelper;
import org.glassfish.embed.util.StringHelper;
import org.glassfish.embed.util.BoolArg;
import org.glassfish.embed.util.Arg;
import org.glassfish.embed.util.ArgProcessor;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embed.util.StringUtils;
import static org.glassfish.embed.util.ServerConstants.*;
/**
 * The default main class for Embedded GlassFish. 
 * The goal is to provide a very simple way to run Embedded GlassFish without
 * having to write any code or compile anything.  You can use this class to:
 * <ul>
 * <li>Start a web server that will serve up static content.
 * <li>Start a server and deploy one or more applications to it
 * <li>Start a server, run asadmin commands to configure it and then use the domain.xml file
 *  that is generated.
 * <li>The possibilities are endless!
 * </ul>
 * The class can be run like so:
 *
 * <xmp>
 * java -jar embedded-jar-file.jar  arg1 arg2...
 * java -cp embedded-jar-file.jar:my-stuff.jar org.glassfish.embed.EmbeddedMain arg1 arg2...
 * </xmp>
 *
 * The most popular API items are supported with command line arguments in this class.
 * Simply enter the following to get documentation on the commands:
 *
 * <xmp>
 * java -jar embedded-jar-file.jar --help
 * </xmp>
 *
 * Here is the current output of the --help command:
 *
 * <xmp>
 Welcome to Embedded GlassFish

Below are a list of valid options.
To use the longname, prepend it with '--'.
To use a short name prepend it with '-'.
The webapp war file names should be supplied after all the options.
You can enter as many web apps as you like.

To start an embedded GlassFish server with a web application packaged in a WAR on port 8080:

    java -jar glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar -p 8080 myapp.war

Files that the embedded GlassFish server uses are located in its installation and
instance directories. If the installation or instance directories to be used are not specified,
they will be created in the directory where you start embedded GlassFish server.
    * default installation directory - embedded_####_glassfish
    * default instance directory - embedded_####_glassfish\domains\domain1

The docroot is located under the instance directory:
    <instance directory>\docroot

To use as a lightweight web server, copy HTML pages to the docroot and start the server:

    java -jar glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar -p 8080

    http://localhost:8080/MyWebPage.html

The autodelete option specifies whether files that the embedded GlassFish
server uses are deleted when the embedded GlassFish server is stopped.
If this option is true, the files are deleted.
If this option is false, the files are not deleted.

The default behavior depends on whether the --installdir option or --instancedir option is set:

    * If the --installdir option or --instancedir option is set, the default is false.
    * If the --installdir option or --instancedir option is not set, the default is true.


Description                                    Long Name      Short Name   Default  Required

HTTP Port                                      --port         -p           -1       true
Filesystem Installation Directory              --installdir   -d                    false
Filesystem Instance Directory                  --instancedir  -i                    false
domain.xml filename or URL                     --xml          -x                    false
Admin HTTP                                     --adminport    -q           -1       false
JMX System Connector Port                      --jmxport      -j           -1       false
Help                                           --help         -h           false    false
Turn on the AutoDeploy Service                 --autodeploy   -b           false    false
Verbose Mode                                   --verbose      -v           true     false
Send logging to instance-root/logs/server.log  --log          -l           true     false
Automatically delete Filesystem                --autodelete   -a           true     false
</xmp>
 *
 *
 * @author Byron Nevins
 */
public class EmbeddedMain {
    /**
     * The class' entry point.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // parse commandline arguments

            ArgProcessor proc = new ArgProcessor(argDescriptions, args);
            Map<String, String> params = proc.getParams();
            List<String> operands = proc.getOperands();

            if(Boolean.parseBoolean(params.get("help")))
                usage();

            /*
            if(Boolean.parseBoolean(params.get("log"))) {
                LoggerHelper.info("log_msg");
                LoggerHelper.stopConsoleLogging();
                LoggerHelper.startFileLogging();
            }
             */

            LoggerHelper.fine("params size = " + params.size());
            Set<Map.Entry<String,String>> set = params.entrySet();
            
            for(Map.Entry<String,String> entry : set) {
                LoggerHelper.fine(entry.getKey() + "=" + entry.getValue());
            }
            
            // create an Info object based on the commandline args
            EmbeddedInfo info = new EmbeddedInfo();
            List<File> wars = new ArrayList<File>(3);
            parseInput(params, operands, info, wars);
            EmbeddedRunner runner = new EmbeddedRunner(info, wars);
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
    private static EmbeddedInfo parseInput(     Map<String, String> params,
                                                List<String> operands,
                                                EmbeddedInfo info,
                                                List<File> wars)
                                                    throws EmbeddedException {
        EmbeddedFileSystem efs = info.getFileSystem();

        /*  Use operands for war filenames
         */

        for(String s : operands) {
            if(StringUtils.ok(s)) {
               wars.add(new File(s));
            }
        }

        //////   port  //////
        { // for scope only
        String port = params.get("port");

        if(!StringUtils.ok(port))
            throw new EmbeddedException("internal", StringHelper.get("no_default_http_port"));

        try {
            info.setHttpPort(Integer.parseInt(port));
        }
        catch(NumberFormatException nfe) {
            throw new EmbeddedException("port_not_int", port);
        }
        } // end scope
        //////   adminport  //////
        // TODO this is a direct copy of the block above.
        //

        { // for scope only

        String adminPort = params.get("adminport");

        if(!StringUtils.ok(adminPort))
            throw new EmbeddedException("internal", StringHelper.get("no_default_admin_http_port"));

        try {
            info.setAdminHttpPort(Integer.parseInt(adminPort));
        }
        catch(NumberFormatException nfe) {
            throw new EmbeddedException("port_not_int", adminPort);
        }
        } //endscope

        ///////   dirs   /////////

        String install = params.get("installdir");

        if(StringUtils.ok(install)) {
            efs.setInstallRoot(new File(install));
            efs.setAutoDelete(false);
        }

        String instance = params.get("instancedir");

        if(StringUtils.ok(instance)) {
            efs.setInstanceRoot(new File(instance));
            efs.setAutoDelete(false);
        }

        ////////  autodelete //////

        efs.setAutoDelete(Boolean.parseBoolean(params.get("autodelete")));

        ////////  domain.xml //////

        String fn = params.get("xml");

        if(StringUtils.ok(fn)) {
            setupDomainXmlUrl(fn, info);
        }

        ////////// jmx port  //////////

        { // scoping

        String jmxPortString = params.get("jmxport");
        try {
            info.setJmxConnectorPort(Integer.parseInt(jmxPortString));
        }
        catch(Exception e) {
            // should never happen
            info.setJmxConnectorPort(8686);
        }
        }
        //scoping

        ////////// logging  //////////

        if(Boolean.parseBoolean(params.get("log")))
                info.setLogging(true);

        ////////// verbose  //////////

        if(Boolean.parseBoolean(params.get("verbose")))
                info.setVerbose(true);


        ///////// AutoDeploy Service ///////////////

        if(Boolean.parseBoolean(params.get("autodeploy"))) {
            info.enableAutoDeploy();
        }

        ////////// done!  //////////
        
        return info;
    }



    private static void setupDomainXmlUrl(String name, EmbeddedInfo info) throws EmbeddedException {
        // This is either a filename or a URL.  E.g. user may have the domain.xml packaged
        // inside their jar.  Or both - in which case the file on disk takes precedence.

        EmbeddedFileSystem efs = info.getFileSystem();

        try {
            // 1.  Check on disk
            File f = new File(name);

            if(f.exists()) {
                efs.setDomainXmlSource(f);
                return;
            }

            
            URL url = EmbeddedMain.class.getResource(name);

            if(url == null)
                throw new EmbeddedException("bad_domain_xml");

            efs.setDomainXmlSource(url);
        }

        catch (EmbeddedException ee) {
            throw ee;
        }
        catch (Exception ex) {
            throw new EmbeddedException("bad_domain_xml", ex);
        }
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
        // EmbeddedMain is meant for quick and fast experiments.
        // So the highest level of verboseness is the default

        //       longname       shortname         default        required           description
        //new Arg("war",          "w",            false,                                       "War File"),
        new Arg("port",             "p",            "" + DEFAULT_HTTP_PORT, true,   "HTTP Port"),
        new Arg("installdir",       "d",                         false,             "Filesystem Installation Directory"),
        new Arg("instancedir",      "i",                         false,             "Filesystem Instance Directory"),
        new Arg("xml",              "x",                         false,             "domain.xml filename or URL"),
        new Arg("adminport",        "q",           "" + DEFAULT_ADMIN_HTTP_PORT,    "Admin HTTP"),
        new Arg("jmxport",          "j",           "" + DEFAULT_JMX_CONNECTOR_PORT, "JMX System Connector Port"),
        new BoolArg("help",         "h",            false,                          "Help"),
        new BoolArg("autodeploy",   "b",            false,                          "Turn on the AutoDeploy Service"),

        // note that --autodelete and --log are NOT BoolArg's
        // TODO make BoolArg more sophisticated so that you can hve the default be false
        // and allow --foo=true and --foo true and --foo
        // BoolArg work kind of weird -- if you use one -- test VERY thoroughly!
        //
        // bnevins Jan 28, 2009 -- I think the trick is to make the default of EVERY BoolArg false.
        // see autodeploy above.  E.g. for log - rename to "nolog" and set the default to false.

        new Arg("verbose",          "v",            "true",                                          "Verbose Mode"),
        new Arg("log",              "l",            "true",                                          "Send logging to instance-root/logs/server.log"),
        new Arg("autodelete",       "a",            "true",                                         "Automatically delete Filesystem"),
    };
    
    private LocalStringsImpl strings = new LocalStringsImpl(this.getClass());
}
