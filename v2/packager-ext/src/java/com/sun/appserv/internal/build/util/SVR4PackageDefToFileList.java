/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.appserv.internal.build.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.StringTokenizer;
import java.util.ArrayList;

import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is a utility intended to ease the conversion of Solaris package
 * definitions defined in a prototype_com file into a file listing needed
 * to drive the creation of a zip file. Each Solaris package (e.g.
 * SUNWasuo) contains a prototype_com file that defines the files that
 * make up that package. There are corresponding prototype_com.win2k and
 * prototype_com.unix files the define the file listing for the zip files.
 * These zip files are used for the evaluation installer on Solaris and
 * the mainstream and evaluation installers on Win2K. This utility helps
 * create the prototype_com.win2k and prototype_com.unix files from the
 * prototype_com file. For and example see iplanet/ias/server/src/pkg/SUNWasu.
 */
public class SVR4PackageDefToFileList{
    
    private static final boolean _debug = false;
    public static final String SPARC = "sparc";
    public static final String SPARCV9 = "sparcv9";
    public static final String X86 = "x86";
    public static final String AMD64 = "amd64";
    
    public static String parseSymlink(String line) {
	// Same as parse file except we are interested only in portion before =
        StringTokenizer tok = new StringTokenizer(line);
        tok.nextToken();
        tok.nextToken();
        String result = tok.nextToken().replaceAll("\\$ASINSTDIR/", "");
        result = result.replaceAll("usr/appserver/", "");
        result = result.replaceAll("var/appserver/", "");
        result = result.replaceAll("appserver/", "");
        result = result.replaceAll("usr/share/", "share/");
        int pos = result.indexOf("=");
        if (_debug) {
            System.out.println("result=" + result + " pos " + pos);
        }
        if (pos > 0) {
            return result.substring(0,pos);
        } else {
            System.out.println("No = found in symlink -" + result);
	}
        return result;
    }

    
    public static String parseFile(String line, String libDirFor64Bit) {
        StringTokenizer tok = new StringTokenizer(line);
        tok.nextToken();
        tok.nextToken();
        String result = tok.nextToken().replaceAll("\\$ASINSTDIR/", "");
        if(result.indexOf("$osarch") != -1 && libDirFor64Bit.equals("")) {
             return null;
        }
        result = result.replaceAll("\\$osarch", libDirFor64Bit);
        result = result.replaceAll("usr/appserver/", "");
        result = result.replaceAll("var/appserver/", "");
        result = result.replaceAll("appserver/", "");
        result = result.replaceAll("usr/share/", "share/");
        int pos = result.indexOf("=");
        if (_debug) {
            System.out.println("result=" + result + " pos " + pos);
        }
        if (pos > 0) {
            return result.substring(pos + 1);
        }
        return result;
    }

    
    public static String parse(String line, String libDirFor64Bit) {
        String result = line.trim();
        //skip empty lines
        if (result.length() == 0) {
            return null;
        }
        //skip comments
        if (result.startsWith("#")) {
            return null;
        }
        //skip directories
        if (result.startsWith("d ")) {
            return null;
        }
        //skip included packaging files
        if (result.startsWith("i ")) {
            return null;
        }
        if (result.startsWith("v ")) {
            return parseFile(result, libDirFor64Bit);
        }
        //process normal files
        if (result.startsWith("f ")) {
            return parseFile(result, libDirFor64Bit);
        }
	//editable files
        if (result.startsWith("e ")) {
            return parseFile(result, libDirFor64Bit);
        }
	// and symbolic links
        if (result.startsWith("s ")) {
            return parseSymlink(result);
        }
        return result;
    }
    
    protected static void writeOutputFile(String out, ArrayList fileList)
    throws FileNotFoundException, IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(out, false));
            for (int i = 0; i < fileList.size(); i++) {
                writer.write((String)fileList.get(i));
                writer.newLine();
            }
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected static ArrayList readInputFile(String in)
    throws FileNotFoundException, IOException {
        ArrayList result = new ArrayList();
        BufferedReader reader = null;
        String osArch = System.getProperty("os.arch");
        String libDirFor64Bit = "";
        if(osArch.equals(SPARC)) libDirFor64Bit = SPARCV9;
        else if(osArch.equals(X86))  libDirFor64Bit = AMD64;
        try {
            reader = new BufferedReader(new FileReader(in));
            String line = null;
            String newLine = null;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                newLine = parse(line, libDirFor64Bit);
                if (newLine != null) {
                    result.add(newLine);
                }
            }
            return result;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected static void filesetElement(Node filesetNode,
    ArrayList fileList) {
        NamedNodeMap attributes = filesetNode.getAttributes();
        ArrayList removeList = new ArrayList();
        Node excludes = attributes.getNamedItem("remove");
        if (excludes != null) {
            if (_debug) {
                System.out.println("excludes node value: " + excludes.getNodeValue());
            }
            StringTokenizer tok = new StringTokenizer(excludes.getNodeValue());            
            while (tok.hasMoreTokens()) {                                
                removeList.add(tok.nextToken());                
            }                  
            // match using a regular expression e.g. ".*/*\.so" to match any so file or
            // ".*/jax*\.jar" to match jax*.jar.
            for (int j = 0; j < removeList.size(); j++) {
                Pattern p = Pattern.compile((String)removeList.get(j));
                for (int i = 0; i < fileList.size(); i++) {
                    if (p.matcher((String)fileList.get(i)).matches()) {
                        fileList.remove(i--);
                        if (_debug) {
                            System.out.println("removed " + fileList.get(i));
                        }
                    }
                }
            }                       
        }
        Node includes = attributes.getNamedItem("add");
        if (includes != null) {
            StringTokenizer tok = new StringTokenizer(includes.getNodeValue());
            while (tok.hasMoreTokens()) {
                fileList.add(tok.nextToken());
            }
        }
    }
    
    protected static void zipfileElement(Node zipfileNode)
    throws FileNotFoundException, IOException {
        NamedNodeMap attributes = zipfileNode.getAttributes();
        String inputFile = attributes.getNamedItem("input").getNodeValue();        
        String outputFile = attributes.getNamedItem("output").getNodeValue();
        if (_debug) {
            System.out.println("inputFile = " + inputFile);
            System.out.println("output = " + outputFile);
        }
        ArrayList fileList = readInputFile(inputFile);
        if (_debug) {
            for (int i = 0; i < fileList.size(); i++) {
                System.out.println("fileList before " + i + " = " +
                fileList.get(i));
            }
        }
        NodeList children = zipfileNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node childNode = children.item(j);
            if (childNode.getNodeName().equals("fileset")) {
                filesetElement(childNode, fileList);
            }
        }
        if (_debug) {
            for (int i = 0; i < fileList.size(); i++) {
                System.out.println("fileList after " + i + " = " +
                fileList.get(i));
            }
        }
        writeOutputFile(outputFile, fileList);
    }
    
    protected static void parseXML(String fileName)
    throws FileNotFoundException, SAXException, IOException,
    ParserConfigurationException {
        DocumentBuilder builder  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        FileInputStream source = new FileInputStream(fileName);
        Document document = builder.parse(source);
        NodeList nodeList = document.getElementsByTagName("zipfile");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                zipfileElement(node);
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: SVR4PackageDefToFileList xmlFile");
        } else {
            String xmlFile = args[0];
            try {
                parseXML(xmlFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
