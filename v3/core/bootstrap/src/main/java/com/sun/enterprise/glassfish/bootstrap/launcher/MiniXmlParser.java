/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.glassfish.bootstrap.launcher;

import java.io.*;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 * A fairly simple but very specific stax XML Parser.  
 * Give it the location of domain.xml and the name of the server instance and it
 * will return JVM options.
 * Currently it is all package private.
 * @author bnevins
 */
class MiniXmlParser {

    public static void main(String[] args) {
        try {
            File dxml = new File("C:/glassfish/domains/domain1/config/domain.xml");
            MiniXmlParser parser = new MiniXmlParser(dxml, "server");
            parser.read();
        }
        catch (Exception e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    MiniXmlParser(File domainXml, String serverName) {
        this.serverName = serverName;
        try {
            FileInputStream stream = new FileInputStream(domainXml);
            XMLInputFactory xif = XMLInputFactory.newInstance();
            parser = xif.createXMLStreamReader(domainXml.toURI().toString(), stream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void read() {
        try {
            getConfigRefName();
            getConfig();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws javax.xml.stream.XMLStreamException
     */
    private void getConfigRefName() throws XMLStreamException {
        if (configRef != null) {
            return;
        }   // second pass!

        skipRoot("domain");
        skipTo("servers");

        // the cursor is at the start-element of <servers>
        while (true) {
            // get to first <server> element
            skipNonStartElements();

            if (!parser.getLocalName().equals("server")) {
                throw new XMLStreamException("no server found");
            }

            // get the attributes for this <server>
            AttributeManager mgr = parseAttributes();
            String thisName = mgr.getValue("name");

            if (serverName.equals(thisName)) {
                configRef = mgr.getValue("config-ref");
                return;
            }
        }
    }

    private void getConfig() throws XMLStreamException {
        skipTo("configs");
        while (true) {
            skipTo("config");

            // get the attributes for this <config>
            AttributeManager mgr = parseAttributes();
            String thisName = mgr.getValue("name");

            if (configRef.equals(thisName)) {
                getJavaConfig();
                return;
            }
        }
    }

    private void getJavaConfig() throws XMLStreamException {
        // cursor --> <config>
        skipTo("java-config");

        // get the attributes for <java-config>
        javaConfigAttributeManager = parseAttributes();
        getJvmOptions();
    }

    private void getJvmOptions() throws XMLStreamException {
        while (skipToButNotPast("jvm-options", "java-config")) {
            jvmOptions.add(parser.getElementText());
        }

        for (String s : jvmOptions) {
            System.out.println("JVM OPTION: " + s);
        }

    }

    private void skipNonStartElements() throws XMLStreamException {
        while (true) {
            int event = parser.next();

            if (event == START_ELEMENT) {
                return;
            }
        }
    }

    private void skipRoot(String name) throws XMLStreamException {
        // The cursor is pointing at the start of the document
        // Move to the first 'top-level' element under name
        // Return with cursor pointing to first sub-element
        while (true) {
            int event = parser.next();

            if (event == START_ELEMENT) {
                if (!name.equals(parser.getLocalName())) {
                    throw new XMLStreamException("Unknown Domain XML Layout");
                }

                return;
            }
        }
    }

    /**
     * The cursor will be pointing at the START_ELEMENT of name when it returns
     * note that skipTree must be called.  Otherwise we could be fooled by a 
     * sub-element with the same name as an outer element
     * @param name the Element to skip to
     * @throws javax.xml.stream.XMLStreamException
     */
    private void skipTo(String name) throws XMLStreamException {
        while (true) {
            skipNonStartElements();
            // cursor is at a START_ELEMENT
            String localName = parser.getLocalName();

            if (name.equals(localName)) {
                return;
            }
            else {
                skipTree(localName);
            }
        }
    }

    /**
     * The cursor will be pointing at the START_ELEMENT of name when it returns
     * note that skipTree must be called.  Otherwise we could be fooled by a 
     * sub-element with the same name as an outer element
     * @param name the Element to skip to
     * @throws javax.xml.stream.XMLStreamException
     */
    private boolean skipToButNotPast(String startName, String endName) throws XMLStreamException {
        while (true) {
            int event = parser.next();

            if (event == START_ELEMENT) {
                if (parser.getLocalName().equals(startName)) {
                    return true;
                }
            }

            if (event == END_ELEMENT) {
                if (parser.getLocalName().equals(endName)) {
                    return false;
                }
            }
        }
    }

    private void skipTree(String name) throws XMLStreamException {
        // The cursor is pointing at the start-element of name.
        // throw everything in this element away and return with the cursor
        // pointing at its end-element.
        while (true) {
            int event = parser.next();

            if (event == END_ELEMENT && name.equals(parser.getLocalName())) {
                //System.out.println("END: " + parser.getLocalName());
                return;
            }
        }
    }

    private void dump() throws XMLStreamException {
        StringBuilder sb = new StringBuilder();

        sb.append("\ngetName(): ").append(parser.getName());
        sb.append("\ngetLocalName(): ").append(parser.getLocalName());
        sb.append("\ngetNamespaceURI(): ").append(parser.getNamespaceURI());
        try {
            sb.append("\ngetText(): ").append(parser.getText());
        }
        catch (Exception e) {
        }
        try {
            sb.append("\ngetElementText(): ").append(parser.getElementText());
        }
        catch (Exception e) {
        }
        sb.append("\ngetEventType(): ").append(parser.getEventType());
        sb.append("\ngetLocation(): ").append(parser.getLocation());
        try {
            sb.append("\ngetAttributeCount(): ").append(parser.getAttributeCount());
        }
        catch (Exception e) {
        }

        System.out.println(sb.toString());
    }

    private AttributeManager parseAttributes() {
        // HINT: there is probably a MUCH better way to do this

        AttributeManager mgr = new AttributeManager();

        int num = parser.getAttributeCount();

        for (int i = 0; i < num; i++) {
            mgr.add(parser.getAttributeName(i).getLocalPart(), parser.getAttributeValue(i));
        }

        return mgr;
    }
    private XMLStreamReader parser;
    private String serverName;
    private String configRef;
    private AttributeManager javaConfigAttributeManager;
    private List<String> jvmOptions = new ArrayList<String>();
}

