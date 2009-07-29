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
package com.sun.enterprise.universal.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.common.util.logging.LoggingPropertyNames;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author bnevins
 * @author jlee
 */
public class MiniXmlParser {
    public MiniXmlParser(File domainXml) throws MiniXmlParserException {
        this(domainXml, "server");
    }

    public MiniXmlParser(File domainXml, String serverName) throws MiniXmlParserException {
        this.serverName = serverName;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(domainXml);
            read();
            valid = true;
        } catch (Exception e) {
            throw new MiniXmlParserException(strings.get("toplevel", e), e);
        }
    }

    public Map<String, String> getJavaConfig() throws MiniXmlParserException {
        checkValid();
        return javaConfig;
    }

    public List<String> getJvmOptions() throws MiniXmlParserException {
        checkValid();
        return jvmOptions;
    }

    public Map<String, String> getProfilerConfig() throws MiniXmlParserException {
        checkValid();
        return profilerConfig;
    }

    public List<String> getProfilerJvmOptions() throws MiniXmlParserException {
        checkValid();
        return profilerJvmOptions;
    }

    public Map<String, String> getProfilerSystemProperties() throws MiniXmlParserException {
        checkValid();
        return profilerSysProps;
    }

    public Map<String, String> getSystemProperties() throws MiniXmlParserException {
        checkValid();
        return sysProps;
    }

    public String getDomainName() {
        return domainName;
    }

    public Set<Integer> getAdminPorts() {
        return adminPorts;
    }

    public void setupConfigDir(File configDir) {
        loggingConfig.setupConfigDir(configDir);
    }

    public String getLogFilename() {

        try {
            Map <String,String> map =loggingConfig.getLoggingProperties();
            logFilename = map.get(LoggingPropertyNames.file);
        } catch (IOException e){
            // error message already sent to logfile.
        }

        return logFilename;
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    private void read() throws XMLStreamException {
        getConfigRefName();
        getConfig(); // might throw
        findDomainName();
        return;
    }

    /**
     * Fetches a value with a quasi xpath-like expression.  Not trying to reinvent the wheel.  Just keep dependencies
     * down at run time.
     *
     * @param path
     *
     * @return
     */
    private Node get(String path) {
        String[] elements = path.split("/");
        Node root = document;
        for (String element : elements) {
            root = find(root, element);
        }
        return root;
    }

    private Node find(Node root, String element) {
        Node node = null;
        if (root != null) {
            final NodeList childNodes = root.getChildNodes();
            final int length = childNodes.getLength();
            for (int i = 0; node == null && i < length; i++) {
                Node child = childNodes.item(i);
                if (element.equals(child.getNodeName())) {
                    node = child;
                }
            }
        }
        return node;
    }

    private void getConfigRefName() throws XMLStreamException {
        Node servers = get("domain/servers");
        new ChildNodeIterator(servers) {
            public boolean process(Node node) {
                final Map<String, String> map = parseAttributes(node);
                if (serverName.equals(map.get("name"))) {
                    configRef = map.get("config-ref");
                    parseSysPropsFromServer(node);
                    return true;
                }
                return false;
            }
        };
        if (configRef == null) {
            throw new XMLStreamException("server " + serverName + " not found");
        }
    }

    private void getConfig() throws XMLStreamException {
        Node configs = get("domain/configs");
        final boolean[] found = new boolean[1];
        found[0] = false;
        new ChildNodeIterator(configs) {
            public boolean process(Node node) {
                String name = node.getAttributes().getNamedItem("name").getNodeValue();
                if (configRef.equals(name)) {
                    parseConfig(node);
                    found[0] = true;
                    return true;
                }
                return false;
            }
        };
        if (!found[0]) {
            throw new XMLStreamException("configuration " + configRef + " not found");
        }
    }

    private void parseConfig(Node node) {
        new ChildNodeIterator(node) {
            public boolean process(Node node) {
                String name = node.getNodeName();
                if ("system-property".equals(name)) {
                    parseSystemPropertyNoOverride(node);
                } else if ("java-config".equals(name)) {
                    parseJavaConfig(node);
                } else if ("http-service".equals(name)) {
                    parseHttpService(node);
                } else if ("network-config".equals(name)) {
                    parseNetworkConfig(node);
                } else if ("log-service".equals(name)) {
                    parseLogService(node);
                } else if ("monitoring-service".equals(name)) {
                    parseMonitoringService(node);
                }
                return false;
            }
        };
        String[] listenerNames = getListenerNamesForVS(DEFAULT_ADMIN_VS_ID, vsAttributes);
        if (listenerNames == null || listenerNames.length == 0) {
            listenerNames = getListenerNamesForVS(DEFAULT_VS_ID, vsAttributes); //plan B
        }
        if (listenerNames == null || listenerNames.length <= 0) {
            return; // can not find ports
        }
        addPortsForListeners(listenerAttributes, listenerNames);

    }

    private void parseSysPropsFromServer(Node server) {
        final Node node = find(server, "system-property");
        if (node != null) {
            parseSystemPropertyWithOverride(node);
        }
    }

    private void parseSystemPropertyNoOverride(Node node) {
        parseSystemProperty(node, false);
    }

    private void parseSystemPropertyWithOverride(Node node) {
        parseSystemProperty(node, true);
    }

    private void parseSystemProperty(Node node, boolean override) {
        final NamedNodeMap map = node.getAttributes();
        String name = map.getNamedItem("name").getNodeValue();
        if (name != null) {
            if (override || !sysProps.containsKey(name)) {
                sysProps.put(name, map.getNamedItem("value").getNodeValue());
            }
        }
    }

    private void parseJavaConfig(Node node) {
        javaConfig = parseAttributes(node);
        parseJvmAndProfilerOptions(node);
    }

    private void parseJvmAndProfilerOptions(Node node) {
        new ChildNodeIterator(node) {
            public boolean process(Node node) {
                if ("jvm-options".equals(node.getNodeName())) {
                    jvmOptions.add(node.getTextContent());
                } else {
                    parseProfiler(node);
                }
                return false;
            }
        };
    }

    private void parseProfiler(Node node) {
        profilerConfig = parseAttributes(node);
        new ChildNodeIterator(node) {
            public boolean process(Node child) {
                if ("jvm-options".equals(child.getNodeName())) {
                    profilerJvmOptions.add(child.getTextContent());
                } else {
                    parseProperty(child, profilerSysProps);
                }
                return false;
            }
        };
    }

    private void parseProperty(Node node, Map<String, String> map) {
        Map<String, String> prop = parseAttributes(node);
        String name = prop.get("name");
        if (name != null) {
            map.put(name, prop.get("value"));
        }
    }

    private void findDomainName() {
        try {
            // find the domain name, if it is there
            // If we bump into the domain end tag first -- no sweat
            new ChildNodeIterator(get("domain")) {
                public boolean process(Node node) {
                    if ("property".equals(node.getNodeName())) {
                        parseDomainName(node); // property found -- maybe it is the domain name?
                    }
                    return false;
                }
            };
            if (domainName == null) {
                Logger.getLogger(MiniXmlParser.class.getName()).log(Level.INFO, strings.get("noDomainName"));

            }
        }
        catch (Exception e) {
            throw new RuntimeException(strings.get("noDomainEnd"));
        }
    }

    private void parseDomainName(Node node) {
        Map<String, String> map = parseAttributes(node);
        String name = map.get("name");
        String value = map.get("value");
        if (name == null || value == null) {
            return;
        }
        if ("administrative.domain.name".equals(name)) {
            domainName = value;
        }
    }

    private void parseLogService(Node node) {
        logFilename = parseAttributes(node).get("file");
    }

    private void parseMonitoringService(Node node) {
        // The default is, by definition, true.
        // Here are all the possibilities and their resolution:
        // 1. Attribute is not present  --> true
        // 2. Attribute is present and set to the exact string "false" --> false
        // 3. Attribute is present and set to anything except "false"  --> true

        String s = parseAttributes(node).get("monitoring-enabled");

        if(s == null)
            monitoringEnabled = true;  // case 1
        else if(s.equals("false"))
            monitoringEnabled = false; // case 2
        else
            monitoringEnabled = true;  // case 3
    }

    private void parseHttpService(Node node) {
        new ChildNodeIterator(node) {
            public boolean process(Node node) {
                String name = node.getNodeName();
                if ("virtual-server".equals(name)) {
                    vsAttributes.add(parseAttributes(node));
                }
                if ("http-listener".equals(name)) {
                    listenerAttributes.add(parseAttributes(node));
                }

                return false;
            }
        };
    }

    private void parseNetworkConfig(Node node) {
        new ChildNodeIterator(node) {
            public boolean process(Node node) {
                String name = node.getNodeName();
                if ("network-listeners".equals(name)) {
                    new ChildNodeIterator(node) {
                        public boolean process(Node node) {
                            String name = node.getNodeName();
                            if ("network-listener".equals(name)) {
                                listenerAttributes.add(parseAttributes(node));
                            }
                            return false;
                        }
                    };
                    return true;
                }
                return false;
            }
        };
    }

    private String[] getListenerNamesForVS(String vsid, List<Map<String, String>> vsAttributes) {
        String listeners = null;
        String[] listenerArray = null;
        // find the virtual server
        for (Map<String, String> atts : vsAttributes) {
            String id = atts.get("id");
            if (id != null && id.equals(vsid)) {
                listeners = atts.get("network-listeners");
                if(listeners == null) {
                    listeners = atts.get("http-listeners");
                }
                break;
            }
        }
        // make sure the "http-listeners" is kosher
        if (GFLauncherUtils.ok(listeners)) {
            listenerArray = listeners.split(",");
            if (listenerArray != null && listenerArray.length <= 0) {
                listenerArray = null;
            }
        }
        if (listenerArray == null) {
            listenerArray = new String[0];
        }
        return listenerArray;
    }

    private void addPortsForListeners(List<Map<String, String>> listenerAttributes, String[] listenerNames) {
        // get the port numbers for all the listeners
        // normally there is one listener
        for (Map<String, String> atts : listenerAttributes) {
            String id = atts.get("name");
            if(id == null) {
                id = atts.get("id");
            }
            if (id != null) {
                for (String listenerName : listenerNames) {
                    if (id.equals(listenerName)) {
                        addPort(atts.get("port"));
                        break;
                    }
                }
            }
        }
    }

    private void addPort(String portString) {
        try {
            adminPorts.add(Integer.parseInt(portString));
        } catch (Exception e) {
            // ignore, just return....
        }
    }

    private Map<String, String> parseAttributes(Node node) {
        Map<String, String> map = new HashMap<String, String>();
        if (node.hasAttributes()) {
            final NamedNodeMap attrs = node.getAttributes();
            int num = attrs.getLength();
            for (int i = 0; i < num; i++) {
                final Node item = attrs.item(i);
                map.put(item.getNodeName(), item.getNodeValue());
            }
        }
        return map;
    }

    private void checkValid() throws MiniXmlParserException {
        if (!valid)
            throw new MiniXmlParserException(strings.get("invalid"));
    }
    private static final String         DEFAULT_ADMIN_VS_ID = "__asadmin";
    private static final String         DEFAULT_VS_ID = "server";
    private LoggingConfigImpl           loggingConfig = new LoggingConfigImpl();
    private List<Map<String, String>>   listenerAttributes = new ArrayList<Map<String, String>>();
    private List<Map<String, String>>   vsAttributes = new ArrayList<Map<String, String>>();
    private Document                    document;
    private String                      configRef;
    private List<String>                jvmOptions = new ArrayList<String>();
    private List<String>                profilerJvmOptions = new ArrayList<String>();
    private Map<String, String>         javaConfig;
    private Map<String, String>         profilerConfig = Collections.emptyMap();
    private Map<String, String>         sysProps = new HashMap<String, String>();
    private Map<String, String>         profilerSysProps = new HashMap<String, String>();
    private boolean                     valid = false;
    private Set<Integer>                adminPorts = new HashSet<Integer>();
    private String                      domainName;
    private String                      logFilename;
    private String                      serverName;
    private boolean                     monitoringEnabled;
    private static final LocalStringsImpl strings = new LocalStringsImpl(MiniXmlParser.class);

    private static abstract class ChildNodeIterator {
        public ChildNodeIterator(Node node) {
            final NodeList list = node.getChildNodes();
            int length = list.getLength();
            for (int i = 0; i < length; i++) {
                final Node child = list.item(i);
                if (!(child instanceof Text) && process(child)) {
                    return;
                }
            }
        }

        public abstract boolean process(Node node);
    }
}