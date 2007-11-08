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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
 
import org.w3c.dom.Document;

// For write operation
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileInputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import org.xml.sax.SAXParseException;

/**
 * The internal NOOPHandler is needed to act as an entity resolver when
 * parsing the domain.xml.template file. The problem is that the 
 * DOCTYPE in this template has a token of the form ${com.sun.aas.installRoot}
 * which cannot be resolved using the standard entity resolver. Instead
 * we explicitly pass the DTD file name to parse against in the 
 * constructor of this class.
 **/
class NOOPHandler extends DefaultHandler {
    private final String _dtdFileName;
    
    NOOPHandler(String dtdFileName) {
        super();
        _dtdFileName = dtdFileName;
    }

    public InputSource resolveEntity(String publicId,
         String systemId) throws SAXException
    {
        InputSource is = null;
        try {
            is = new InputSource(new FileInputStream(_dtdFileName));
        } catch(Exception e) {
            throw new SAXException("cannot resolve dtd", e);
        }
        return is;
    }

}

/**
 * The DomainXMLTransformer takes as input the DTD file name, 
 * XML input file name to transform confroming to this DTD, the
 * name of the XSL transform file to apply, and the output file
 * name.
 *
 * This class is used to transform the default-domain.xml.template 
 * file into an domain xml template that can be used in the RI
 * or SE versions of the product.
 **/
public class XMLFileTransformer
{
    private static void doTransform(String domainXMLdtd, 
        String domainXMLinput, 
        String domainXMLtransform, String domainXMLoutput) 
        throws ParserConfigurationException, SAXException,
            TransformerConfigurationException, TransformerException,
            IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setNamespaceAware(false);
        //factory.setValidating(false);
        //factory.setExpandEntityReferences(false);
 
        File stylesheet = new File(domainXMLtransform);
        File inputfile = new File(domainXMLinput);
        File outputfile = new File(domainXMLoutput);
 
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new NOOPHandler(domainXMLdtd));
        Document document = builder.parse(inputfile);
 
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = tFactory.newTransformer(stylesource);
 
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputfile);
        transformer.transform(source, result);
    }
    
    public static void main (String argv [])
    {
        if (argv.length != 4) {
            System.err.println ("Usage: java XMLFileTransformer dtd inputfile stylesheet outputfile");
        } else {
            try {
                doTransform(argv[0], argv[1], argv[2], argv[3]);            
                System.exit(0);
            } catch (SAXParseException spe){
                System.err.println(spe.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.exit (1);
    }
}

