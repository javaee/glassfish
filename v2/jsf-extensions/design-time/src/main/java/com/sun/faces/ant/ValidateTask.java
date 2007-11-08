/*
 * $Id: ValidateTask.java,v 1.1 2005/09/20 21:11:22 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;

/**
 * <p>An <code>Ant</code> task to perform schema validation of the
 * <code>standard-html-renderkit.xml</code> document.  The optional task,
 * <code>xmlvalidate</code> is a hassel when it comes to schema.
 */
public class ValidateTask extends Task {

    private String schemaDir;


    // -------------------------------------------------------------- Properties


    public void setSchemaDir(String schemaDir) {

        this.schemaDir = schemaDir;

    } // END setSchemaDir


    // ------------------------------------------------------- Methods from Task


    public void execute() throws BuildException {

        File dir = new File(schemaDir);
        if (!dir.isDirectory()) {
            throw new BuildException("The schemaDir '" + schemaDir +
                "' is not a directory");
        }

        if (!dir.canRead()) {
            throw new BuildException("The schemaDir '" + schemaDir +
                "' cannot be read");
        }

        SAXParser parser = getParser();
        String file = schemaDir + File.separatorChar +
            "standard-html-renderkit.xml";
        try {
            parser.parse(new File(file), new Resolver(schemaDir));
            System.out.println("The document, standard-html-renderkit.xml, is valid.");
        } catch (Exception e) {
            throw new BuildException(e);
        }

    } // END execute


    // --------------------------------------------------------- Private Methods


    private SAXParser getParser() throws BuildException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        File schemaSource = new File(schemaDir + File.separatorChar +
            "web-facesconfig_1_2.xsd");
        try {
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
            parser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaSource",
                schemaSource);
            return parser;
        } catch (Exception e) {
            throw new BuildException(e);
        }

    } // END getParser


    // ----------------------------------------------------------- Inner Classes


    private static class Resolver extends DefaultHandler {

        private String j2ee14;
        private String webServices;
        private String xml;
        private String facesConfig;

        private MessageFormat message =
            new MessageFormat("({0}: {1}, {2}): {3}");


        public Resolver(String schemaDir) {

            String basePath = schemaDir + File.separatorChar;
            j2ee14 = basePath + "j2ee_1_4.xsd";
            webServices = basePath + "j2ee_web_services_client_1_1.xsd";
            xml = basePath + "xml.xsd";
            facesConfig = basePath + "web-facesconfig_1_2.xsd";

        } // END Resolver


        private String print(SAXParseException x) {

            String msg = message.format(new Object[]
            {
                x.getSystemId(),
                new Integer(x.getLineNumber()),
                new Integer(x.getColumnNumber()),
                x.getMessage()
            });

            return msg;

        } // END print


        public void warning(SAXParseException x) {

            System.out.println("WARNING: " + print(x));

        } // END warning


        public void error(SAXParseException x) throws SAXParseException{

            System.out.println("ERROR: " + print(x));
            throw x;

        } // END error


        public void fatalError(SAXParseException x) throws SAXParseException {

            System.out.println("FATAL: " + print(x));
            throw x;

        } // END fatalError


        public InputSource resolveEntity(String publicId,
                                         String systemId) {

            InputSource source = null;
            if (systemId.indexOf("j2ee_1_4") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(j2ee14));
                    source.setSystemId(new File(j2ee14).toURL().toString());
                } catch (Exception e) {
                    //
                }
            } else if (systemId.indexOf("webservice") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(webServices));
                    source.setSystemId(
                        new File(webServices).toURL().toString());
                } catch (Exception e) {
                    //
                }
            } else if (systemId.indexOf("xml.xsd") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(xml));
                    source.setSystemId(xml);
                } catch (Exception e) {
                    //
                }
            } else if (systemId.indexOf("web-facesconfig_1_2.xsd") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(facesConfig));
                    source.setSystemId(
                        new File(facesConfig).toURL().toString());
                } catch (Exception e) {
                    //
                }
            } else {
                try {
                    source = super.resolveEntity(publicId, systemId);
                    if (source != null && publicId != null)
                        source.setPublicId(publicId);
                    if (source != null && systemId != null)
                        source.setSystemId(systemId);
                } catch (Exception e) {
                    //
                }
            }

            return source;

        } // END resolveEntity

    } // END Resolver

}
