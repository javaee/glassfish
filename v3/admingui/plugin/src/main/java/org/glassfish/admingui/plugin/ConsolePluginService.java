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

import com.sun.enterprise.module.bootstrap.Populator;

import org.glassfish.api.admingui.ConsoleProvider;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.net.URL;
import java.util.List;
//import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  <p>	This class provides access to {@link IntegrationPoint}s.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
@Service
public class ConsolePluginService implements Populator {
// FIXME: Remove Populator interface
    @Inject Logger logger;

    @Inject Habitat habitat;

    @Inject ConsoleProvider providers[];


    /**
     *	<p> Default constructor.</p>
     */
    public ConsolePluginService() {
System.out.println("CONSTRUCTOR: Console Plugin Service!");
    }

    /**
     *	Temporary method that will go away when I can @Inject this class into
     *	web app.  Its only here now for testing purposes.
     */
    public void run(ConfigParser notused) {
System.out.println("INITIALIZING: Console Plugin Service!");

	// First find the parser
	if ((providers != null) && (providers.length > 0)) {
	    // Get our parser...
	    ConfigParser parser = new ConfigParser(habitat);
	    URL url = null;

	    // Loop through the configs and add them all
	    for (ConsoleProvider provider : providers) {
		// Read the contents from the URL
		url = provider.getConfiguration();
		if (url == null) {
		    url = provider.getClass().getClassLoader().getResource(
			ConsoleProvider.DEFAULT_CONFIG_FILENAME);
		}
		DomDocument doc = parser.parse(url);

		// Get the New IntegrationPoints
		ConsoleConfig config = (ConsoleConfig) doc.getRoot().get();

		// Add the new IntegrationPoints
		addIntegrationPoints(config.getIntegrationPoints(), provider);
	    }
	}

// Test...
System.out.println("this: " + this);
System.out.println("tree ips: " + getIntegrationPoints("tree"));
    }

    /**
     *	<p> This method allows new {@link IntegrationPoint}s to be added to
     *	    the known {@link IntegrationPoints}.</p>
     */
    public void addIntegrationPoints(List<IntegrationPoint> points, ConsoleProvider provider) {
	// Add them all...
	for (IntegrationPoint point : points) {
	    addIntegrationPoint(point, provider);
	}
    }

    /**
     *	<p> This method allows a new {@link IntegrationPoint} to be added to
     *	    the known {@link IntegrationPoint}s.</p>
     */
    public void addIntegrationPoint(IntegrationPoint point, ConsoleProvider provider) {
	// Associate the Provider with this IntegrationPoint so we
	// have a way to get the correct classloader
	point.setConsoleProvider(provider);

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
System.out.println("Getting Integration Points: Console Plugin Service!");
	return pointsByType.get(type);
    }

    /**
     *
     */
    private static final ConsolePluginService pluginService = new ConsolePluginService();

    /**
     *	<p> This <code>Map</code> contains the {@link IntegrationPoint}s keyed
     *	    by the <code>type</code> of integration.</p>
     */
    private MultiMap<String, IntegrationPoint> pointsByType =
	    new MultiMap<String, IntegrationPoint>();
}
