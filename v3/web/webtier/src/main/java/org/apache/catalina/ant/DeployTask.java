

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.ant;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant task that implements the <code>/deploy</code> command, supported by
 * the Tomcat manager application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:23 $
 * @since 4.1
 */
public class DeployTask extends AbstractCatalinaTask {


    // ------------------------------------------------------------- Properties


    /**
     * URL of the context configuration file for this application, if any.
     */
    protected String config = null;

    public String getConfig() {
        return (this.config);
    }

    public void setConfig(String config) {
        this.config = config;
    }


    /**
     * URL of the server local web application archive (WAR) file 
     * to be deployed.
     */
    protected String localWar = null;

    public String getLocalWar() {
        return (this.localWar);
    }

    public void setLocalWar(String localWar) {
        this.localWar = localWar;
    }


    /**
     * The context path of the web application we are managing.
     */
    protected String path = null;

    public String getPath() {
        return (this.path);
    }

    public void setPath(String path) {
        this.path = path;
    }


    /**
     * Tag to associate with this to be deployed webapp.
     */
    protected String tag = null;

    public String getTag() {
        return (this.tag);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    /**
     * Update existing webapps.
     */
    protected boolean update = false;

    public boolean getUpdate() {
        return (this.update);
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }


    /**
     * URL of the web application archive (WAR) file to be deployed.
     */
    protected String war = null;

    public String getWar() {
        return (this.war);
    }

    public void setWar(String war) {
        this.war = war;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the requested operation.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {

        super.execute();
        if (path == null) {
            throw new BuildException
                ("Must specify 'path' attribute");
        }
        if ((war == null) && (localWar == null) && (tag == null)) {
            throw new BuildException
                ("Must specify either 'war', 'localWar', or 'tag' attribute");
        }

        // Building an input stream on the WAR to upload, if any
        BufferedInputStream stream = null;
        String contentType = null;
        int contentLength = -1;
        if (war != null) {
            if (war.startsWith("file:")) {
                try {
                    URL url = new URL(war);
                    URLConnection conn = url.openConnection();
                    contentLength = conn.getContentLength();
                    stream = new BufferedInputStream
                        (conn.getInputStream(), 1024);
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            } else {
                try {
                    stream = new BufferedInputStream
                        (new FileInputStream(war), 1024);
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }
            contentType = "application/octet-stream";
        }

        // Building URL
        StringBuffer sb = new StringBuffer("/deploy?path=");
        sb.append(URLEncoder.encode(this.path));
        if ((war == null) && (config != null)) {
            sb.append("&config=");
            sb.append(URLEncoder.encode(config));
        }
        if ((war == null) && (localWar != null)) {
            sb.append("&war=");
            sb.append(URLEncoder.encode(localWar));
        }
        if (update) {
            sb.append("&update=true");
        }
        if (tag != null) {
            sb.append("&tag=");
            sb.append(URLEncoder.encode(tag));
        }

        execute(sb.toString(), stream, contentType, contentLength);

    }


}
