/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.api.embedded.web.config;

/**
 * Class through which the default <tt>VirtualServer</tt> and
 * <tt>WebListener</tt> of the <tt>EmbeddedWebContainer</tt> may be
 * configured.
 *
 * @see org.glassfish.web.embed.EmbeddedWebContainer#start(WebContainerConfig)
 */
public class WebContainerConfig {

    private int port = 8080;
    private String webListenerId = "http-listener-1";
    private String virtualServerId = "server";
    private String[] hostNames = new String[] {"localhost"};

    /**
     * Sets the port of the default <tt>WebListener</tt> (default: 8080).
     *
     * @param port the port of the default <tt>WebListener</tt>
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port of the default <tt>WebListener</tt> (default: 8080).
     *
     * @return the port of the default <tt>WebListener</tt>
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the id of the default <tt>WebListener</tt>
     * (default: <i>http-listener-1</i>).
     *
     * @param webListenerId the id of the default <tt>WebListener</tt>
     */
    public void setWebListenerId(String webListenerId) {
        this.webListenerId = webListenerId;
    }

    /**
     * Gets the id of the default <tt>WebListener</tt>
     * (default: <i>http-listener-1</i>).
     *
     * @return the id of the default <tt>WebListener</tt>
     */
    public String getWebListenerId() {
        return webListenerId;
    }

    /**
     * Sets the id of the default <tt>VirtualServer</tt>
     * (default: <i>server</i>).
     *
     * @param virtualServerId the id of the default <tt>VirtualServer</tt>
     */
    public void setVirtualServerId(String virtualServerId) {
        this.virtualServerId = virtualServerId;
    }

    /**
     * Gets the id of the default <tt>VirtualServer</tt>
     * (default: <i>server</i>).
     *
     * @return the id of the default <tt>VirtualServer</tt>
     */
    public String getVirtualServerId() {
        return virtualServerId;
    }

    /**
     * Sets the host names of the default <tt>VirtualServer</tt>
     * (default: <i>localhost</i>).
     *
     * @param hostNames the host names of the default <tt>VirtualServer</tt>
     */
    public void setHostNames(String... hostNames) {
        this.hostNames = hostNames;
    }

    /**
     * Gets the host names of the default <tt>VirtualServer</tt>
     * (default: <i>localhost</i>).
     *
     * @return the host names of the default <tt>VirtualServer</tt>
     */
    public String[] getHostNames() {
        return hostNames;
    }

}
