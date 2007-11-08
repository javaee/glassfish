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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.util.Utils;


/**
 * Gets the URL of the Server
 */
public class ServerURLHandler extends BaseHandler {

    /**
     * Constructor.
     */
    public ServerURLHandler() {
        super();
    }
    
    public Object handleAttribute(ObjectName peer, String attribute, 
            MBeanServerConnection mbs) throws HandlerException, AttributeNotFoundException, 
             MBeanException, ReflectionException, InstanceNotFoundException,
             IOException {
        try {
            String serverName = peer.getKeyProperty("name");
            ObjectName serverObjectName = new ObjectName("com.sun.appserv:type=server,name="+serverName+",category=config");
            String configRef = (String) mbs.getAttribute(serverObjectName, "config-ref");

            ObjectName listenersObjectName = new ObjectName("com.sun.appserv:type=virtual-server,id="+serverName+",config="+configRef+",category=config");
            String listeners = (String) mbs.getAttribute(listenersObjectName, "http-listeners");
            List listenerList = parseStringList(listeners, " ,");

            // There will be 1 URL per listener
            List urls = new ArrayList(listenerList.size());
            StringBuffer url = null;
            ObjectName listenerObjectName;

            Iterator iterator = listenerList.iterator();
            while (iterator.hasNext()) {
                // Get the next listener
                listenerObjectName = new ObjectName("com.sun.appserv:type=http-listener,id="+iterator.next()+",config="+configRef+",category=config");

                // Start the URL
                url = new StringBuffer("http");
                if (mbs.getAttribute(listenerObjectName, "security-enabled").toString().equals("true")) {
                    // Security is enabled on this port, add an 's'
                    url.append("s");
                }
                url.append("://");

                // Host
                url.append(getHost(serverObjectName, mbs));
                url.append(":");

                // Port
                url.append((String)mbs.getAttribute(listenerObjectName, "port"));

                // Add the URL to the List
                urls.add(url.toString());
            }

            iterator = urls.iterator();
            String uri;
            while(iterator.hasNext()) {
                uri = (String) iterator.next();
                if(uri.startsWith("http:")) {
                    Utils.log(Level.FINE, "Server URL = " + url);
                    return uri;
                }
            }
            if(!urls.isEmpty()) {
                return (String)urls.get(0);
            } else {
                Utils.log(Level.SEVERE, "Not able to get hold of Server URL");
                return "";
            }
        } catch (Exception ex) {
            throw new HandlerException(ex);
        }
    }



    /**
     *  This method determines the hostname of the given serverInstance
     *  ObjectName to the best of its ability.  It will attempt to obtain the node-agent....
     *
     *  @param  serverInstance  The ObjectName to use to determine the hostname
     */
    private String getHost(ObjectName serverInstance, MBeanServerConnection mbs) {
	String hostName = null;
        try {
            // Find the node agent (if there is one)
            String nodeAgentRef = (String)mbs.getAttribute(serverInstance, "node-agent-ref");
            if ((nodeAgentRef == null) || nodeAgentRef.equals("")) {
                return getDefaultHostName();
            }

            // Get the JMX connector for the node agent
            ObjectName jmxConnector = (ObjectName)mbs.invoke(
                new ObjectName("com.sun.appserv:type=node-agent,name="+nodeAgentRef+",category=config"),
                    "getJmxConnector", null, null);
            if (jmxConnector == null) {
                return getDefaultHostName();
            }

       	    // Try to get the hostname
	    // Get "client-hostname" from the properties (use this way instead
	    // of getProperty to avoid exception
	    AttributeList properties = (AttributeList)mbs.invoke(
		        jmxConnector, "getProperties", null, null);
	    Attribute att;
	    Iterator it = properties.iterator();
            while (it.hasNext()) {
	        att = (Attribute)it.next();
	        if (att.getName().equals("client-hostname")) {
	            hostName = (String)att.getValue();
	            break;
	        }
	    }

	    // Get default host name
	    if ((hostName == null) || hostName.equals("") || hostName.equals("0.0.0.0")) {
	        return getDefaultHostName();
	    }
       } catch (Exception ex) {
           Utils.log(Level.SEVERE, "Not able to get Host name.");
           return "";
       }

 
	// We found the hostname!!
	return hostName;
   }


   /**
    *  This method is used as a fallback when no Hostname is provided.
    */
    private String getDefaultHostName() {
	String defaultHostName = "localhost";
	try {
	    InetAddress host = InetAddress.getLocalHost();
	    defaultHostName = host.getCanonicalHostName();
	} catch(UnknownHostException uhe) {
            Utils.log(Level.INFO, "UnknownHostException: " + uhe);
        }
        return defaultHostName;
    }


    private List parseStringList(String line)
    {
        return parseStringList(line, null);
    }

    /**
     * Parses a string containing substrings separated from
     * each other by the specified set of separator characters and returns
     * a list of strings.
     *
     * Splits the string <code>line</code> into individual string elements 
     * separated by the field separators specified in <code>sep</code>, 
     * and returns these individual strings as a list of strings. The 
     * individual string elements are trimmed of leading and trailing
     * whitespace. Only non-empty strings are returned in the list.
     *
     * @param line The string to split
     * @param sep  The list of separators to use for determining where the
     *             string should be split. If null, then the standard
     *             separators (see StringTokenizer javadocs) are used.
     * @return     Returns the list containing the individual strings that
     *             the input string was split into.
     */
    private List parseStringList(String line, String sep)
    {
        if (line == null)
            return null;

        StringTokenizer st;
        if (sep == null)
            st = new StringTokenizer(line);
        else 
            st = new StringTokenizer(line, sep);

        String token;

        List tokens = new Vector();
        while (st.hasMoreTokens())
        {
            token = st.nextToken().trim();
            if (token.length() > 0)
                tokens.add(token);
        }

        return tokens;
    }

}
