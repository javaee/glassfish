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
package org.glassfish.api.admingui;

import org.jvnet.hk2.annotations.Contract;

import java.net.URL;


/**
 *  <p>	This interface exists to provide a marker for locating modules which
 *	provide GUI features to be displayed in the GlassFish admin console.
 *	The {@link #getConfiguration()} method should either return (null), or
 *	a <code>URL</code> to the console-config.xml file.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
@Contract
public interface ConsoleProvider {

    /**
     *	<p> Returns a <code>URL</code> to the <code>console-config.xml</code>
     *	    file, or <code>null</code>.  If <code>null</code> is returned, the
     *	    default ({@link #DEFAULT_CONFIG_FILENAME}) will be used.</p>
     */
    public URL getConfiguration();

    /**
     *	<p> The default location of the <code>console-config.xml</code>.</p>
     */
    public String DEFAULT_CONFIG_FILENAME   =
	"META-INF/admingui/console-config.xml";
}
