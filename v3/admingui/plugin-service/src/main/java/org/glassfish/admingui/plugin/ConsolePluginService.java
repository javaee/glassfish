/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.plugin;

import org.glassfish.api.admingui.ConsoleProvider;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;


/**
 *  <p>	This class provides access to {@link IntegrationPoint}s.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
@Service
public class ConsolePluginService {
    @Inject Logger logger;
    @Inject Habitat habitat;
    @Inject ConsoleProvider providers[];

/*
    @Inject ModulesRegistry modulesRegistry;
        for(Module m : modulesRegistry.getModules()) {
            url = m.getClassLoader().getResource(ConsoleProvider.DEFAULT_CONFIG_FILENAME);
            if(url!=null)
                ; // TODO: parse url
        }
*/

    /**
     *	<p> Default constructor.</p>
     */
    public ConsolePluginService() {
    }

    /**
     *	<p> Initialize the available {@link IntegrationPoint}s.</p>
     */
    protected synchronized void init() {
	if (initialized) {
	    return;
	}
	initialized = true;

	// First find the parser
	if ((providers != null) && (providers.length > 0)) {
	    // Get our parser...
	    ConfigParser parser = new ConfigParser(habitat);
	    URL url = null;
	    String id = null;

	    // Loop through the configs and add them all
	    for (ConsoleProvider provider : providers) {
		// Read the contents from the URL
		url = provider.getConfiguration();
		if (url == null) {
		    url = provider.getClass().getClassLoader().getResource(
			ConsoleProvider.DEFAULT_CONFIG_FILENAME);
		}
		if (url == null) {
		    if (logger.isLoggable(Level.INFO)) {
			logger.info("Unable to find "
			    + ConsoleProvider.DEFAULT_CONFIG_FILENAME
			    + " file for provider '"
			    + provider.getClass().getName() + "'");
		    }
		    continue;
		}
//System.out.println("Provider *"+provider+"* : url=*"+url+"*");
		DomDocument doc = parser.parse(url);

		// Get the New IntegrationPoints
		ConsoleConfig config = (ConsoleConfig) doc.getRoot().get();

		// Save the ClassLoader for later
//System.out.println("Storing: " + config.getId() + " : " + provider.getClass().getClassLoader());
		id = config.getId();
		moduleClassLoaderMap.put(id, provider.getClass().getClassLoader());

		// Add the new IntegrationPoints
		addIntegrationPoints(config.getIntegrationPoints(), id);
	    }
	}

//System.out.println("IP Map: " + pointsByType.toString());

	// Log some trace messages
	if (logger.isLoggable(Level.FINE)) {
	    logger.fine("Console Plugin Service has been Initialized!");
	    if (logger.isLoggable(Level.FINEST)) {
		logger.finest(pointsByType.toString());
	    }
	}
    }

    /**
     *	<p> This method searches the classpath of all plugins for the requested
     *	    resource and returns all instances of it (if any).  This method
     *	    will NOT return <code>null</code>, but may return an empty
     *	    <code>List</code>.</p>
     */
    public List<URL> getResources(String name) {
	ArrayList<URL> result = new ArrayList<URL>();
	if ((providers != null) && (providers.length > 0)) {
	    // Get our parser...
	    Enumeration<URL> urls = null;
	    URL url = null;

	    // Loop through the configs and add them all
	    for (ConsoleProvider provider : providers) {
		// Read the contents from the URL
		ClassLoader loader = provider.getClass().getClassLoader();
System.out.println("** CL: '" + loader + "'");
		try {
		    urls = loader.getResources(name);
//		    url = loader.getResource(name);
//System.out.println("URL " + provider.getClass().getName() + " == '" + url + "'");
		} catch (IOException ex) {
		    if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Error getting resource '"
			    + name + "' from provider: '"
			    + provider.getClass().getName() + "'. Skipping...",
			    ex);
		    }
		    continue;
		}
		while (urls.hasMoreElements()) {
		    // Found one... add it.
		    url = urls.nextElement();
System.out.println("URL " + provider.getClass().getName() + " == '" + url + "'");
		    try {
//System.out.println("\n\n########FILE: \n" + new String(readFromURL(url)) + "\n\n");
			result.add(new URL(url, ""));
		    } catch (Exception ex) {
			// Ignore b/c this should not ever happen, we're not
			// changing the URL
			System.out.println(
			    "ConsolePluginService: URL Copy Failed!");
		    }
		}
	    }
	}
	return result;
    }

    /***********************************************************
    public static byte[] readFromURL(URL url) throws IOException {
        byte buffer[] = new byte[10000];
        byte result[] = new byte[0];

	int count = 0;
	int offset = 0;
	java.io.InputStream in = url.openStream();

	// Attempt to read up to 10K bytes.
	count = in.read(buffer);
	while (count != -1) {
	    // Make room for new content...
	    //result = Arrays.copyOf(result, offset + count);  Java 6 only...
	    // When I can depend on Java 6... replace the following 3 lines
	    // with the line above.
	    byte oldResult[] = result;
	    result = new byte[offset + count];
	    System.arraycopy(oldResult, 0, result, 0, offset);

	    // Copy in new content...
	    System.arraycopy(buffer, 0, result, offset, count);

	    // Increment the offset
	    offset += count;

	    // Attempt to read up to 10K more bytes...
	    count = in.read(buffer);
	}
        return result;
    }
    ***********************************************************/

    /**
     *	<p> This method allows new {@link IntegrationPoint}s to be added to
     *	    the known {@link IntegrationPoint}s.</p>
     */
    public void addIntegrationPoints(List<IntegrationPoint> points, String id) {
	// Add them all...
	for (IntegrationPoint point : points) {
	    addIntegrationPoint(point, id);
	}
    }

    /**
     *	<p> This method allows a new {@link IntegrationPoint} to be added to
     *	    the known {@link IntegrationPoint}s.</p>
     */
    public void addIntegrationPoint(IntegrationPoint point, String id) {
	// Associate the Provider with this IntegrationPoint so we
	// have a way to get the correct classloader
	point.setConsoleConfigId(id);

	// Add it
	pointsByType.add(point.getType(), point);
    }

    /**
     *	<p> This method returns the {@link IntegrationPoint}s associated with
     *	    the given type.</p>
     *
     *	@param	type	The type of {@link IntegrationPoint}s to retrieve.
     */
    public List<IntegrationPoint> getIntegrationPoints(String type) {
	init();	// Ensure it is initialized.
	return pointsByType.get(type);
    }

    /**
     *	<p> This method returns the <code>ClassLoader</code> associated with
     *	    the requested module.  If the requested module does not exist, has
     *	    not been initialized, or does not contain any admin console
     *	    extensions, this method will return <code>null</code>.</p>
     *
     *	@param	moduleName	The name of the module.
     *
     *	@return	<code>null</code>, or the module's <code>ClassLoader</code>.
     */
    public ClassLoader getModuleClassLoader(String moduleName) {
	return moduleClassLoaderMap.get(moduleName);
    }

    /**
     *	<p> Flag indicating intialization has already occured.</p>
     */
    private boolean initialized	= false;

    /**
     *	<p> This <code>Map</code> contains the {@link IntegrationPoint}s keyed
     *	    by the <code>type</code> of integration.</p>
     */
    private MultiMap<String, IntegrationPoint> pointsByType =
	    new MultiMap<String, IntegrationPoint>();

    /**
     *	<p> This <code>Map</code> keeps track of the <code>ClassLoader</code>
     *	    for each module that provides GUI {@link IntegrationPoint}s.  It
     *	    is keyed by the id specified in the <code>console-config.xml</code>
     *	    file from the module.</p>
     */
    private Map<String, ClassLoader> moduleClassLoaderMap =
	    new HashMap<String, ClassLoader>();
}
