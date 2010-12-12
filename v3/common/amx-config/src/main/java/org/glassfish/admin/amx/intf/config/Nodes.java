package org.glassfish.admin.amx.intf.config;

import java.util.Map;

public interface Nodes extends ConfigCollectionElement, ConfigElement {

    public Map<String, Node> getNode();

    public Node getNode(String param1);

}
