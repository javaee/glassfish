package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.ComponentException;

import java.util.*;

import com.sun.enterprise.config.serverbeans.*;

/**
 * Utility class for all V2 style related dotted names commands.
 *
 * User: Jerome Dochez
 * Date: Jul 9, 2008
 * Time: 11:38:50 PM
 */
public class V2DottedNameSupport {

    public Map<Dom, String> getAllDottedNodes(ConfigBeanProxy proxy) {
        return getAllDottedNodes(Dom.unwrap(proxy));
    }

    public Map<Dom, String> getAllDottedNodes(Dom root) {

        Map<Dom, String> result = new HashMap<Dom, String>();
        getAllSubDottedNames(null, root, result);
        return result;
    }

    protected void getAllSubDottedNames(String prefix, Dom parent,Map<Dom, String> result) {

        Set<String> elementNames = parent.getLeafElementNames();

        for (String childName : elementNames) {

            // by default, it's a collection unless I can find the model for it
            // and ensure this is one or not.
            // not finding the model usually means that it was a "*" element therefore
            // a collection.
            boolean collection = true;
            if (parent.model.findIgnoreCase(childName)!=null) {
                // if this is a leaf node, we should really treat it as an attribute.
                if (parent.isLeaf(childName))
                    continue;
                collection = parent.isCollection(childName);

            }

            for (Dom child : parent.nodeElements(childName)) {

                String newPrefix = (prefix==null?childName:prefix+"."+childName);


                if (collection) {

                    String name = child.getKey();
                    if (name==null) {
                        name = child.attribute("name");
                    }

                    newPrefix = (name==null?newPrefix:newPrefix+"."+name);
                    // now traverse the child
                    getAllSubDottedNames(newPrefix, child, result);
                } else {
                    getAllSubDottedNames(newPrefix, child, result);

                }
            }
        }
        if (prefix!=null) {
            result.put(parent, prefix);
        }
    }

    public Map<String, String> getNodeAttributes(Dom node, String prefix) {
        Map<String, String> result = new  HashMap<String, String>();
        for (String attrName : node.getAttributeNames()) {
            String value = node.attribute(attrName);
            if (value!=null) {
                result.put(attrName, value);
            }
        }
        for (String leafName : node.getLeafElementNames()) {
            ConfigModel.Property property = node.model.findIgnoreCase(leafName);
            if (property==null) {
                property = node.model.findIgnoreCase("*");
            }
            if (property.isLeaf()) {
                String value = node.leafElement(leafName);
                if (value!=null) {
                    result.put(leafName, value);
                }
            }
        }
        return result;
    }

    public Map<Dom, String> getMatchingNodes(Map<Dom, String> nodes, String pattern) {

        Map<Dom, String> result = new HashMap<Dom, String>();
        for (Map.Entry<Dom, String> entry : nodes.entrySet()) {

            String dottedName = entry.getValue();
            if (matches(dottedName, pattern)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public boolean matches(String dottedName, String pattern) {
        StringTokenizer patternToken = new StringTokenizer(pattern, ".");
        if (patternToken.hasMoreElements()) {
            String token = (String) patternToken.nextElement();
            if (token.startsWith("*")) {
                // let's find the end delimiter...
                if (token.length()>1) {
                    String delim = token.substring(1);
                    if (dottedName.indexOf(delim)!=-1) {
                        // found the delimiter...
                        // we have to be careful, the delimiter can be at the end of the string...
                        String remaining = dottedName.substring(dottedName.indexOf(delim) + delim.length());
                        if (remaining.length()==0) {
                            // no more dotted names, better be done with the pattern
                            return !patternToken.hasMoreElements();
                        } else {
                            remaining = remaining.substring(1);
                        }
                        if (patternToken.hasMoreElements()) {
                            return matches(remaining, pattern.substring(token.length()+1));
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (patternToken.hasMoreElements()) {
                        if (dottedName.lastIndexOf('.')==-1) {
                            // more pattern, but no more dotted names.
                            return false;
                        }
                        // now this can be tricky, seems like the get/set can accept something like *.config
                        // which really means *.*.*.config for the pattern matching mechanism.
                        // so instead of jumping one dotted name token, we may need to jump multiple tokens
                        // until we find the next delimiter, let's find this first.
                        String delim = (String) patternToken.nextElement();
                        // we are not going to check if the delim is a attribute, it has to be an element name.
                        // we will leave the attribute checking to someone else.
                        if (dottedName.contains(delim)) {
                            String remaining = dottedName.substring(dottedName.lastIndexOf(delim));
                            return matches(remaining,
                                    pattern.substring(token.length()+1));
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            } else {
                String delim;
                if (token.lastIndexOf("*")!=-1) {
                    delim = token.substring(0, token.lastIndexOf("*"));
                } else {
                    delim = token;
                }
                if (dottedName.startsWith(delim)) {
                    if (patternToken.hasMoreElements()) {
                        if (dottedName.length()<=delim.length()+1) {
                            // end of our name and more pattern to go...
                            return false;
                        }
                        String remaining = dottedName.substring(delim.length()+1);
                        return matches(remaining, pattern.substring(token.length()+1));
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    final static class TreeNode {
        final Dom node;
        final String relativeName;
        public TreeNode(Dom node, String name) {
            this.node = node;
            this.relativeName = name;
        }
    }

    public TreeNode getAliasedParent(Habitat habitat, String prefix) throws ComponentException {

        Domain domain = habitat.getComponent(Domain.class);

        // let's look first if we are dealing with a server or a cluster-name
        String name;
        String newPrefix;
        if (prefix.indexOf('.')!=-1) {
            name = prefix.substring(0, prefix.indexOf('.'));
            newPrefix = prefix.substring(name.length()+1);
        } else {
            name = prefix;
            newPrefix="";
        }

        for (Server server : domain.getServers().getServer()) {
            if (server.getName().equals(name)) {
                return new TreeNode(Dom.unwrap(server), newPrefix);
            }
        }
        if (domain.getClusters()!=null) {
            for (Cluster cluster : domain.getClusters().getCluster()) {
                if (cluster.getName().equals(name)) {
                    return new TreeNode(Dom.unwrap(cluster), newPrefix);
                }
            }
        }
        return new TreeNode(Dom.unwrap(domain), prefix);
    }
}
