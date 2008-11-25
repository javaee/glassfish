/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.File;
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
        // We will just use the classpath in this JVM for now...
        // TODO

        List<File> list = new ArrayList<File>();
        String s = System.getProperty("java.class.path");
        String[] pathItems = s.split(System.getProperty("path.separator"));

        for(String pathItem : pathItems) {
            list.add(new File(pathItem));
        }
        return list;
    }

    @Override
    String getMainClass() throws GFLauncherException {
        return "org.glassfish.embed.EmbeddedMain";
    }
    @Override
    public synchronized void setup() throws GFLauncherException, MiniXmlParserException {
        setJavaExecutableIfValid(System.getProperty("java.home"));

        setClasspath(System.getProperty("java.class.path"));
        //logCommandLine();
        setCommandLine();
    }

    /*
     @Override
    void setCommandLine() throws GFLauncherException{
        List<String> cmdLine = getCommandLine();
        cmdLine.clear();
        cmdLine.add("java");
        cmdLine.add("-cp");
        cmdLine.add("final.jar");
        cmdLine.add("com.sun.enterprise.admin.cli.AsadminMain");
        cmdLine.add("version");

    }
     * */
}
