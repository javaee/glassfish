/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import org.glassfish.api.admin.InstanceState;
import org.jvnet.hk2.component.Habitat;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This parses the instance state file and sets up the InstanceState singleton object for use by
 * various parts of the system
 */
public class InstanceStateFileProcessor {
    private Document xmlDoc = null;
    private String fileName;
    private Habitat habitat;
    private HashMap<String, InstanceState> instanceStates;
    private Domain domain;
    private Map<String, InstanceState.StateType> stringToStateTypeMap;

    public InstanceStateFileProcessor(Habitat habitat, HashMap<String, InstanceState> st, Domain domain, String xmlFile) {
        this.habitat = habitat;
        this.instanceStates = st;
        this.domain = domain;
        this.fileName = xmlFile;
        stringToStateTypeMap = new HashMap<String, InstanceState.StateType>();
        stringToStateTypeMap.put(InstanceState.StateType.NO_RESPONSE.getDescription(), InstanceState.StateType.NO_RESPONSE);
        stringToStateTypeMap.put(InstanceState.StateType.NOT_RUNNING.getDescription(), InstanceState.StateType.NOT_RUNNING);
        stringToStateTypeMap.put(InstanceState.StateType.STARTING.getDescription(), InstanceState.StateType.STARTING);
        stringToStateTypeMap.put(InstanceState.StateType.RUNNING.getDescription(), InstanceState.StateType.RUNNING);
        stringToStateTypeMap.put(InstanceState.StateType.RESTART_REQUIRED.getDescription(), InstanceState.StateType.RESTART_REQUIRED);
    }

    public void parse() throws Exception {
        File xmlFileObject = new File(fileName);
        if( (!xmlFileObject.exists()) ||
            (xmlFileObject.length() == 0) ) {
            createNewInstanceStateFile(xmlFileObject);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.xmlDoc = builder.parse(fileName);
        parseInstanceStateFile();
    }

    private void createNewInstanceStateFile(File xmlFileObject) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFileObject));
        writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        writer.newLine();
        writer.write("<instance-state version=\"1.0\">");
        writer.newLine();
        writer.write("<gms-enabled>false</gms-enabled>");
        writer.newLine();
        for(Server s : domain.getServers().getServer()) {
            if(s.isDas())
                continue;
            writer.write("<instance name=\""+s.getName()+"\" state=\""+
                    InstanceState.StateType.NO_RESPONSE.getDescription()+"\" />");
            writer.newLine();
        }
        writer.write("</instance-state>");
        writer.newLine();
        writer.flush();
        writer.close();
    }

    public void addNewServer(String name) throws Exception {
        if(xmlDoc == null) {
            File xmlFileObject = new File(fileName);
            if(xmlFileObject.exists()) {
                xmlFileObject.delete();
            }
            parse();
        } else {
            Node parentNode = xmlDoc.getElementsByTagName("instance-state").item(0);
            Element insNode = xmlDoc.createElement("instance");
            insNode.setAttribute("name", name);
            insNode.setAttribute("state", InstanceState.StateType.NO_RESPONSE.getDescription());
            parentNode.appendChild(insNode);
            writeDoc(new FileOutputStream(fileName));
        }
    }

    private void parseInstanceStateFile() {
        NodeList list = xmlDoc.getElementsByTagName("instance");
        if(list==null) {
            return;
        }
        for(int i=0; i<list.getLength(); i++) {
            parseInstanceElement(list.item(i));
        }
    }

    private void parseInstanceElement(Node n) {
        String name = null, state = null;
        NamedNodeMap attrs = n.getAttributes();
        if(attrs != null) {
            name = getNodeValue(attrs.getNamedItem("name"));
            state = getNodeValue(attrs.getNamedItem("state"));
        }
        if(name == null)
            return;
        InstanceState newInstanceState = null;
        if(state == null)
            newInstanceState = new InstanceState(InstanceState.StateType.NO_RESPONSE);
        else
            newInstanceState = new InstanceState(stringToStateTypeMap.get(state));
        NodeList list = n.getChildNodes();
        if(list == null)
            return;
        for(int i=0; i<list.getLength(); i++) {
            //TODO : Why we need this ? check
            String t = list.item(i).getTextContent();
            if("\n".equals(t))
                continue;
            newInstanceState.addFailedCommands(t);
        }
        instanceStates.put(name, newInstanceState);
    }

    private String getNodeValue(Node x) {
        return (x==null) ? null : x.getNodeValue();
    }

    private void writeDoc(OutputStream outputStream) throws Exception {
        TransformerFactory transformerfactory = TransformerFactory.newInstance();
        Transformer transformer = transformerfactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(this.xmlDoc);
        transformer.transform(domSource, new StreamResult(outputStream));
    }

    public void updateState(String instanceName, String newState) throws Exception {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xPath = xpf.newXPath();
        Node employee = (Node) xPath.evaluate("/instance-state/instance[@name='"+instanceName+"']/.", this.xmlDoc, XPathConstants.NODE);
        employee.getAttributes().getNamedItem("state").setNodeValue(newState);
        writeDoc(new FileOutputStream(fileName));
    }

    private Node findNode(String instanceName) {
        NodeList list = xmlDoc.getElementsByTagName("instance");
        if(list==null) {
            return null;
        }
        for(int i=0; i<list.getLength(); i++) {
            Node instance = list.item(i);
            NamedNodeMap attrs = instance.getAttributes();
            if(attrs == null)
                continue;
            String name = getNodeValue(attrs.getNamedItem("name"));
            if(instanceName.equals(name))
                return instance;
        }
        return null;
    }

    public void addFailedCommand(String instanceName, String failedCmd) throws Exception {
        Node instance = findNode(instanceName);
        if(instance == null)
            return;
        Text tNode = xmlDoc.createTextNode(failedCmd);
        Element fcNode = xmlDoc.createElement("failed-command");
        fcNode.appendChild(tNode);
        instance.appendChild(fcNode);
        writeDoc(new FileOutputStream(fileName));
    }

    public void removeFailedCommands(String instanceName) throws Exception {
        Node instance = findNode(instanceName);
        if(instance == null)
            return;
        NodeList clist = instance.getChildNodes();
        for(int j=0; j<clist.getLength(); j++) {
            instance.removeChild(clist.item(j));
        }
        writeDoc(new FileOutputStream(fileName));
    }

    public void removeInstanceNode(String instanceName) throws Exception {
        Node instance = findNode(instanceName);
        if(instance == null)
            return;
        NodeList clist = instance.getChildNodes();
        for(int j=0; j<clist.getLength(); j++) {
            instance.removeChild(clist.item(j));
        }
        Node parent = instance.getParentNode();
        parent.removeChild(instance);
        writeDoc(new FileOutputStream(fileName));
    }
}
