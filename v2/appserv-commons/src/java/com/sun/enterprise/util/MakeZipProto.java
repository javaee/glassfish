/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */

package com.sun.enterprise.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.StringTokenizer;
import java.util.ArrayList;

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
 * create the prototype_com.win2k and prototype_com.uniz files from the 
 * prototype_com file. For and example see iplanet/ias/server/src/pkg/SUNWasu.
 *
 * WARNING: This file is temporarily checked in here and really belongs 
 * more as part of the installer/build system; however, the installer is
 * compiled with an older JDK version, and will not compile this file
 * due to replaceAll.
 */
public class MakeZipProto {

    public static String parseFile(String line) {
	    StringTokenizer tok = new StringTokenizer(line);
	    tok.nextToken();
	    tok.nextToken();
	    String result = tok.nextToken().replaceAll("\\$ASINSTDIR/", "");
	    int pos = result.indexOf("=");
	    if (pos > 0) {
		    return result.substring(pos + 1);
	    } 
	    return result;
    }

    public static String parse(String line) {
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
	    //process normal files
	    if (result.startsWith("f ")) {
		    return parseFile(result);
	    }
	    return result;
    }

    protected static void writeOutputFile(String out, ArrayList fileList)
	    throws FileNotFoundException, IOException
    {
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
	    throws FileNotFoundException, IOException
    {
        ArrayList result = new ArrayList();
        BufferedReader reader = null;
	    try {
    	    reader = new BufferedReader(new FileReader(in));
		    String line = null;
		    String newLine = null;
		    while (true) {
			    line = reader.readLine();
			    if (line == null) {
				    break;
			    }
			    newLine = parse(line);
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
        ArrayList fileList)
    {
        NamedNodeMap attributes = filesetNode.getAttributes();
        Node includes = attributes.getNamedItem("includes");
        if (includes != null) {
	        StringTokenizer tok = new StringTokenizer(includes.getNodeValue());
            while (tok.hasMoreTokens()) {
	            fileList.add(tok.nextToken());
            }
        }
        ArrayList removeList = new ArrayList();
        Node excludes = attributes.getNamedItem("excludes");
        if (excludes != null) {
	        StringTokenizer tok = new StringTokenizer(excludes.getNodeValue());
            while (tok.hasMoreTokens()) {
	            removeList.add(tok.nextToken());
            }
        }
        for (int i = 0; i < fileList.size(); i++) {
            if (removeList.contains(fileList.get(i))) {
                fileList.remove(i--);
            }
        }
    }
    
    protected static void zipfileElement(Node zipfileNode)
        throws FileNotFoundException, IOException
    {
        NamedNodeMap attributes = zipfileNode.getAttributes();
        String inputFile = attributes.getNamedItem("input").getNodeValue();
        System.out.println("inputFile = " + inputFile);
        String outputFile = attributes.getNamedItem("output").getNodeValue();
        System.out.println("output = " + outputFile);
        ArrayList fileList = readInputFile(inputFile);
        for (int i = 0; i < fileList.size(); i++) {
            System.out.println("fileList before " + i + " = " + 
               fileList.get(i));
        }
        NodeList children = zipfileNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node childNode = children.item(j);
            if (childNode.getNodeName().equals("fileset")) {
                filesetElement(childNode, fileList);
            } 
        }
        for (int i = 0; i < fileList.size(); i++) {
            System.out.println("fileList after " + i + " = " + 
               fileList.get(i));
        }
        writeOutputFile(outputFile, fileList);
    }

    protected static void parseXML(String fileName)
        throws FileNotFoundException, SAXException, IOException,
        ParserConfigurationException
    {
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
	    	System.out.println("usage: MakeZipProto xmlFile");
	    } else {
		    String xmlFile = args[0];
		    try {
                parseXML(xmlFile);
			    //translate(inFile, outFile);
		    } catch (Exception ex) {
			    ex.printStackTrace();
		    }
	    }
    }
}
