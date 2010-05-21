/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.StringUtils;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.hk2.component.Habitat;

/**
 * This code has BIG performance optimizations.  That usually means more complex code.
 * In this case it means hyper ultra super-duper complicated code!  I made it as
 * simple as I can but it is still tough to understand.
 * The optimizations:
 * 1. Load either one or two configs into the runtime -- instead of all of them.
 * 2. Parse the XML stream only once if possible.
 *
 * Complexities -- if the order is correct for the "big three" elements -- no sweat.
 * The big three are servers, clusters, configs.  We always want configs last because
 * those are what get filtered out.  But we won't know WHAT to filter out without seeing
 * the other two.
 *
 * @author Byron Nevins
 */
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
// the xml parser throws runtime exceptions at the drop of a hat!  E.g. a server-ref
// does not have a ref element and you ask for it.  **  KABOOM  **
// recommend:  add some wrapper methods in here that will just set the variables to null!
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO


// strings to constants


////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
////////  TODO TODO TODO TODO TODO
class InstanceReaderFilter extends ServerReaderFilter {
    InstanceReaderFilter(String theServerName, Habitat theHabitat, URL theDomainXml,
            XMLInputFactory theXif, Logger theLogger) throws XMLStreamException {

        super(theHabitat, theDomainXml, theXif, theLogger);
        instanceName = theServerName;
    }

    /**
     * This method is called for every element.  We are very interested
     * in server, config and cluster.
     * We will only filter out config and server elements never other elements
     * We use this as a handy hook to get info about other elements -- which really
     * is a side-effect.
     *
     * @return true to NOT parse this sub-tree
     * @throws XMLStreamException
     */
    @Override
    final boolean filterOut() throws XMLStreamException {
        try {
            XMLStreamReader reader = getParent();
            String elementName = reader.getLocalName();

            if(!StringUtils.ok(elementName))
                return true; // famous last words:  "this can not ever happen" ;-)

            checkIfReparseRequired(elementName);

            if(elementName.equals(SERVER))
                return handleServer(reader);

            if(elementName.equals(CONFIG))
                return handleConfig(reader);

            // no handleCluster call because it just naturally goes into the habitat
            return false;
        }
        catch(XMLStreamException xe) {
            throw xe;
        }
        catch(Exception e) {
            // I don't trust the XML parser code in the JDK -- it likes to throw
            // unchecked exceptions!!
            throw new XMLStreamException(
                    strings.get("InstanceReaderFilter.UnknownException",
                    e.toString()), e);
        }
    }

    @Override
    final String configWasFound() {
        if(serverConfigName != null)
            return null;
        else
            return strings.get("InstanceReaderFilter.CantFindConfig", instanceName);
    }

    /**
     * The moment we hit 'configs' we will have to remember where we are and
     * IMMEDIATELY do a re-parse if the order of elements is wrong.
     */
    private void checkIfReparseRequired(final String elementName) throws XMLStreamException {
        if(elementName.equals(CLUSTERS)) {
            parsedClusters = true;
            return;
        }
        if(elementName.equals(SERVERS)) {
            parsedServers = true;
            return;
        }
        if(elementName.equals(CONFIGS)) {
            parsedConfigs = true;

            if(!parsedClusters || !parsedServers)
                reparseBegin();
        }
    }

    private void reparseBegin() throws XMLStreamException {
        InputStream stream2 = null;
        reparseReader = null;

        try {
            stream2 = domainXml.openStream();
            reparseReader = xif.createXMLStreamReader(domainXml.toExternalForm(), stream2);

            while(reparseReader.hasNext()) {
                if(parsedServers && parsedClusters)
                    break; // done!!

                if(reparseReader.next() == START_ELEMENT)
                    reparse();
            }
        }
        catch(IOException e) {
            throw new XMLStreamException("Failed to parse " + domainXml, e);
        }
        finally {
            try {
                reparseReader.close();
                stream2.close();
                reparseReader = null;
            }
            catch(Exception e) {
                // this is annoying!
            }
        }
    }

    /** note that NOTHING goes into the habitat from this reader -- it just
     * gathers data because of wrong ordring of elements
     * @param reader
     */
    private void reparse() throws XMLStreamException {
        logger.warning(strings.get("InstanceReaderFilter.ReparseNeeded"));

        String name = reparseReader.getLocalName();

        if(!StringUtils.ok(name))
            return;
        if(!parsedServers && name.equals(SERVERS)) {
            parsedServers = true;
            handleServers();
        }
        if(!parsedClusters && name.equals(CLUSTERS)) {
            parsedClusters = true;
            handleClusters();
        }
    }

    /**
     * 
     * @return true if we want to filter out this server element
     */
    private boolean handleServer(XMLStreamReader r) {
        if(parsedServer)
            return true;    // we already found our server.  Filter this one out

        String name = r.getAttributeValue(null, NAME);

        if(!instanceName.equals(name))
            return true;    // filter it out!

        parsedServer = true;
        serverConfigName = r.getAttributeValue(null, CONFIG_REF);

        if(clusterConfigName != null && clusterName != null)
            return true;    // we must be in a re-parse and servers appeared AFTER clusters

        Collection<Cluster> clusters = habitat.getAllByType(Cluster.class);

        for(Cluster cluster : clusters) {
            List<ServerRef> refs = cluster.getServerRef();

            for(ServerRef ref : refs) {
                if(instanceName.equals(ref.getRef())) {
                    // match!!!
                    clusterName = cluster.getName();
                    clusterConfigName = cluster.getConfigRef();
                    return false;
                }
            }
        }

        return false;   // No cluster found
        // todo log???  check reparse flag etc.
    }

    private boolean handleConfig(XMLStreamReader reader) {
        String name = reader.getAttributeValue(null, NAME);

        if(!StringUtils.ok(name))
            return true;    // No name!!

        if(!StringUtils.ok(serverConfigName))
            // TODO throw Exception?  this is catastrophic!
            return true;

        if(name.equals(serverConfigName) || name.equals(clusterConfigName))
            return false;

        return true;
    }

    /**
     * This method is only called when in "reparse" mode
     * We need look inside for a server-ref pointing at this server and
     * then setup the 2 cluster-related element names.  The main parser has not
     * yet reached the clusters element...
     *
     * braces instead of annoying html brackets...
     * {cluster name="c1" config-ref="c1-config"..}
     *       {server-ref ref="ci1"...}>
     */
    private void handleClusters() throws XMLStreamException {
        // we are pointed at the clusters element
        // this is so hideously complicated that a "good" Exception is thrown
        // from layers below.
        String cname = null;
        String ccfgname = null;

        try {
            while(true) {
                skipToStartButNotPast(CLUSTER, CLUSTERS);
                // save them in case this is the correct cluster
                cname = reparseReader.getAttributeValue(null, NAME);
                ccfgname = reparseReader.getAttributeValue(null, CONFIG_REF);

                handleCluster();
            }
        }
        catch(GoodException ge) {
            clusterConfigName = ccfgname;
            clusterName = cname;
        }
        catch(NotFoundException e) {
            // all done!!
        }
    }

    /**
     * REPARSE ONLY!!!!
     * @throws XMLStreamException
     */
    private void handleServers() throws XMLStreamException {
        // we are pointed at the servers element

        try {
            while(!parsedServer) {
                skipToStartButNotPast(SERVER, SERVERS);
                // save them in case this is the correct cluster
                handleServer(reparseReader);
            }
        }
        catch(NotFoundException e) {
            // all done!!  not an error...
        }
    }

    private void handleCluster() throws GoodException, XMLStreamException {
        // we are pointed at the cluster element

        try {
            while(true) {
                skipToStartButNotPast(SERVER_REF, CLUSTER);
                String ref = reparseReader.getAttributeValue(null, REF);

                if(instanceName.equals(ref))
                    throw new GoodException();
            }
        }
        catch(GoodException ge) {
            throw ge;
        }
        catch(Exception ex) {
            // not an error -- we are finished with this cluster
            // the parser will theow IllegalStateException if there is no "ref"
        }
    }

    /**
     * contract -- if we bump into the "wrong" start or an end first -- it is an error!
     * returns if we find the start, exception if we find the stop
     */
    private void skipToStartButNotPast(String startName, String stopName) throws XMLStreamException, NotFoundException {
        while(reparseReader.hasNext()) {
            reparseReader.next();

            // getLocalName() will throw an exception in many states.  Be careful!!
            if(reparseReader.isStartElement() && startName.equals(reparseReader.getLocalName()))
                return;
            if(reparseReader.isEndElement() && stopName.equals(reparseReader.getLocalName()))
                throw new NotFoundException();
        }
        throw new NotFoundException();
    }
    private boolean parsedClusters;
    private boolean parsedServers;
    private boolean parsedServer;
    private boolean parsedConfigs;
    private final String instanceName;
    private String serverConfigName;
    private String clusterConfigName;
    private String clusterName;
    private XMLStreamReader reparseReader;
    private final static LocalStringsImpl strings = new LocalStringsImpl(InstanceReaderFilter.class);

    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    private static final String CLUSTERS = "clusters";
    private static final String CLUSTER = "cluster";
    private static final String REF = "ref";
    private static final String SERVER_REF = "server-ref";
    private static final String CONFIG = "config";
    private static final String CONFIGS = "configs";
    private static final String CONFIG_REF = "config-ref";
    private static final String NAME = "name";

    private static class GoodException extends Exception {
    }

    private static class NotFoundException extends Exception {
    }
}
