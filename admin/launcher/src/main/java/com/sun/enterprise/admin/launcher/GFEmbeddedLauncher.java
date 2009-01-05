/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bnevins
 */
class GFEmbeddedLauncher extends GFLauncher{


    public GFEmbeddedLauncher(GFLauncherInfo info)   {
        super(info);

    }

    @Override
    void internalLaunch() throws GFLauncherException {
        try {
            launchInstance();
        }
        catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    @Override
    List<File> getMainClasspath() throws GFLauncherException {
        List<File> list = new ArrayList<File>(1);

        String gfeJar = System.getenv(GFE_JAR);
        
        if(GFLauncherUtils.ok(gfeJar)) {
            File f = new File(gfeJar);

            if(f.isFile()) {
                list.add(f);
                return list;
            }
        }
        throw new GFLauncherException("no_gfe_jar");
    }

    @Override
    String getMainClass() throws GFLauncherException {
        return "org.glassfish.embed.EmbeddedMain";
    }
    @Override
    public synchronized void setup() throws GFLauncherException, MiniXmlParserException {
        // remember -- this is designed exclusively for SQE usage
        // don't do it mmore than once -- that would be silly!

        if(setup)
            return;
        else
            setup = true;

        try {
            setupFromEnv();
        }
        catch(GFLauncherException gfle) {
            String msg = "";
            throw new GFLauncherException(GENERAL_MESSAGE + gfle.getMessage());
        }

        setClasspath(gfeJar.getPath());
        setCommandLine();

        /* it is NOT an error for there to be no domain.xml (yet).
         * so eat exceptions.  Also just set the default to 4848 if we don't find
         * the port...
         */

        GFLauncherInfo info = getInfo();

        try {
            File parent = info.getDomainParentDir();
            String domainName = info.getDomainName();
            String instanceName = info.getInstanceName();

            if(instanceName == null)
                instanceName = "server";

            File dom = new File(parent, domainName);
            File dx = new File(dom, "config/domain.xml");


            MiniXmlParser parser = new MiniXmlParser(dx, instanceName);
            info.setAdminPorts(parser.getAdminPorts());

        }
        catch(Exception e) {
            // temp todo
            e.printStackTrace();
        }

        Set<Integer> adminPorts = info.getAdminPorts();

        if(adminPorts == null || adminPorts.isEmpty()) {
            adminPorts = new HashSet<Integer>();
            adminPorts.add(4848);
            info.setAdminPorts(adminPorts);
        }
        
        /*
        String domainName = parser.getDomainName();
        if(GFLauncherUtils.ok(domainName)) {
            info.setDomainName(domainName);
        }
        */

    }
    
    @Override
    void setCommandLine() throws GFLauncherException {
        List<String> cmdLine = getCommandLine();
        cmdLine.clear();
        cmdLine.add(javaExe.getPath());
        cmdLine.add("-cp");
        cmdLine.add(getClasspath());
        addDebug(cmdLine);
        cmdLine.add(getMainClass());
        //cmdLine.add("--port");
        //cmdLine.add("8080");
        //cmdLine.add("--xml");
        //cmdLine.add(domainXml.getPath() );
        cmdLine.add("--installDir");
        cmdLine.add(installDir.getPath());
        cmdLine.add("--instanceDir");
        cmdLine.add(domainDir.getPath());
        cmdLine.add("--autodelete");
        cmdLine.add("false");
    }

    private void addDebug(List<String> cmdLine) {
        String s = System.getenv("GFE_DEBUG_PORT");

        if(ok(s)) {
            cmdLine.add("-Xdebug");
            cmdLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + s);
        }
    }



    private void setupFromEnv() throws GFLauncherException {
        // we require several env. variables to be set for embedded-cli usage
        setupEmbeddedJar();
        setupInstallationDir();
        setupJDK();
        setupDomainDir();
    }

    private void setupDomainDir() throws GFLauncherException {
        String domainDirName = getInfo().getDomainName();
        domainDir = getInfo().getDomainParentDir();
        domainDir = new File(domainDir, domainDirName);

        if(!domainDir.isDirectory())
            domainDir.mkdirs();

        if(!domainDir.isDirectory())
            throw new GFLauncherException("Can not create directory: " + domainDir);

        domainDir = SmartFile.sanitize(domainDir);
        domainXml = SmartFile.sanitize(new File(domainDir, "config/domain.xml"));
    }

    private void setupJDK() throws GFLauncherException {
        String err = "You must set the environmental variable JAVA_HOME to point " +
                "at a valid JDK.  <jdk>/bin/javac[.exe] must exist.";

        String jdkDirName = System.getenv(JAVA_HOME);
        if(!ok(jdkDirName))
            throw new GFLauncherException(err);

        File jdkDir = new File(jdkDirName);

        if(!jdkDir.isDirectory())
            throw new GFLauncherException(err);

        if(File.separatorChar == '\\')
            javaExe = new File(jdkDir, "bin/java.exe");
        else
            javaExe = new File(jdkDir, "bin/java");

        if(!javaExe.isFile())
            throw new GFLauncherException(err);

        javaExe = SmartFile.sanitize(javaExe);
    }

    private void setupInstallationDir() throws GFLauncherException {
        String err = "You must set the environmental variable S1AS_HOME to point " +
                "at a GlassFish installation or at an empty directory or at a " +
                "location where an empty directory can be created.";
        String installDirName = System.getenv(INSTALL_HOME);

        if(!ok(installDirName))
            throw new GFLauncherException(err);

        installDir = new File(installDirName);

        if(!installDir.isDirectory())
            installDir.mkdirs();

        if(!installDir.isDirectory())
            throw new GFLauncherException(err);

        installDir = SmartFile.sanitize(installDir);
    }

    private void setupEmbeddedJar() throws GFLauncherException {
        String err = "You must set the environmental variable GFE_JAR to point " +
                "at the Embedded jarfile.";
        
        String gfeJarName = System.getenv(GFE_JAR);

        if(!ok(gfeJarName))
            throw new GFLauncherException(err);

         gfeJar = new File(gfeJarName);

        if(!gfeJar.isFile() || gfeJar.length() < 1000000L)
            throw new GFLauncherException(err);

         gfeJar = SmartFile.sanitize(gfeJar);
}

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private boolean setup = false;
    private File gfeJar;
    private File installDir;
    private File javaExe;
    private File domainDir;
    private File domainXml;
    private static final String GFE_JAR          = "GFE_JAR";
    private static final String INSTALL_HOME    = "S1AS_HOME";
    private static final String JAVA_HOME        = "JAVA_HOME";
    //private static final String DOMAIN_DIR       = "GFE_DOMAIN";
    private static final String GENERAL_MESSAGE =
            " *********  GENERAL MESSAGE ********\n" +
            "You must setup four different environmental variables to run embedded" +
            " with asadmin.  They are\n" +
            "GFE_JAR - path to the embedded jar\n" +
            "S1AS_HOME - path to installation directory.  This can be empty or not exist yet.\n" +
            "JAVA_HOME - path to a JDK installation.  JRE installation is generally not good enough\n" +
            "GFE_DOMAIN - path to the domain dir's config dir.  I.e. this is where the domain.xml will be written.\n" +
            "GFE_DEBUG_PORT - optional debugging port.  It will start suspended.\n" +
            "\n*********  SPECIFIC MESSAGE ********\n";

}
