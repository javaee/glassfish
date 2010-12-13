/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgiweb;

import javax.servlet.ServletContext;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Built-in {@link com.sun.faces.config.configprovider.MetaInfFacesConfigResourceProvider} can't discover
 * resources named as xxx.taglib.xml. This config resource provider knows how to iterate over bundle entries in order to
 * discover the resources. It is registered as a META-INF service so that mojarra can discover it.
 *
 * @see org.glassfish.osgiweb.OSGiWebModuleDecorator#discoverJSFConfigs(org.osgi.framework.Bundle, java.util.Collection, java.util.Collection)
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiFaceletConfigResourceProvider implements com.sun.faces.spi.FaceletConfigResourceProvider, com.sun.faces.spi.ConfigurationResourceProvider {
    private static Logger logger = Logger.getLogger(OSGiFaceletConfigResourceProvider.class.getPackage().getName()); 
    public Collection<URI> getResources(ServletContext context) {
        Collection<URI> uris = (Collection<URI>) context.getAttribute(Constants.FACELET_CONFIG_ATTR);
        if (uris == null) return Collections.EMPTY_LIST;
        logger.info("Facelet Config uris = " + uris); // TODO(Sahoo): change to debug level statement
        return uris;
    }
}
