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
 *
 */

package org.glassfish.embed;

import java.io.*;
import java.util.*;
import java.net.*;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embed.util.StringUtils;
import static org.glassfish.embed.ServerConstants.*;

/**
 * <code>EmbeddedInfo</code> holds all the {@link Server} details:
 * <ul>
 * <li>{@link EmbeddedFileSystem}</li>
 * <li>HTTP port</li>
 * <li>server name</li>
 * <li>archives</li>
 * </ul>
 * 
 * @author bnevins
 */
public class EmbeddedInfo {
    /**
     * Default constructor sets server name to "server" and HTTP port to "8888"
     * by default.
     */
    public EmbeddedInfo() {
        
    }

    /**
     * Sets an {@link EmbeddedFileSystem} on this <code>EmbeddedInfo</EmbeddedInfo> object
     *
     * @param efs
     * @see EmbeddedFileSystem
     */
    public void setFileSystem(EmbeddedFileSystem efs) {
        this.efs = efs;
    }

    /**
     *
     * @return the {@link EmbeddedFileSystem} used by this <code>EmbeddedInfo</object>
     */
    public EmbeddedFileSystem getFileSystem() {
        return efs;
    }

    /**
     *
     * @param f
     */
    public void addArchive(File f) {
        archives.add(f);
    }

    /**
     *
     * @param sw
     */
    public void addScatteredWar(ScatteredWar sw) {
        throw new UnsupportedOperationException("Not yet implemented");
        //scatteredWars.add(sw);
    }
    
    /**
     *
     * @param ra
     */
    public void addReadableArchive(ReadableArchive ra) {
        throw new UnsupportedOperationException("Not yet implemented");
        //readableArchives.add(ra);
    }
    
    /**
     * Set the HTTP port
     * @param port
     */
    public void setHttpPort(int port) {
        httpPort = port;
    }

    /**
     * Set the server name
     * @param newName
     */
    public void setServerName(String newName) {
        if(StringUtils.ok(newName))
            name = newName;
    }


    /**
     * Set the http listener name.
     * @param newName
     */
    public void setHttpListenerName(String newName) {
        if(StringUtils.ok(newName))
            httpListenerName = newName;
    }


    /**
     * Checks that archives exist.  Checks that the HTTP port is a valid port number.
     * Checks if an {@link EmbeddedFileSystem} has been set.
     * If none has been set, a default <code>EmbeddedFileSystem</code>
     * is created.
     * @throws org.glassfish.embed.EmbeddedException
     */
    public void validate() throws EmbeddedException {
        validateArchives();
        validatePort();
        validateFilesystem();
        validateLogging();
    }

    public void setLogging(boolean b) {
        logging = b;
    }
    
    @Override
    public String toString() {
        // TODO Finish it...
        StringBuilder sb = new StringBuilder("Dump of " + getClass().getName());
        sb.append('\n');
        sb.append("httpPort=").append(httpPort).append('\n');
        
        return sb.toString();
    }

    //////////////////////  pkg-private  //////////////////////
    // bnevins
    // This class is really just a buffer for storing a bunch of data
    // That means that if we wrote getters they would have to return a reference
    // to the long-lived object here.  In that case all encapsulation is gone anyway
    // so I went with this simpler cleaner route...

    String                  name             = DEFAULT_SERVER_NAME;
    int                     httpPort         = DEFAULT_HTTP_PORT;
    List<File>              archives         = new LinkedList<File>(); 
    List<ReadableArchive>   readableArchives = new LinkedList<ReadableArchive>();
    List<ScatteredWar>      scatteredWars    = new LinkedList<ScatteredWar>();
    EmbeddedFileSystem      efs;
    String                  httpListenerName = DEFAULT_HTTP_LISTENER_NAME;
    boolean                 logging         = false;


    //////////////////////  all private below //////////////////////

    private void validateArchives() throws EmbeddedException {
        for(File f : archives) {
            // at least make sure it exists
            if(!f.exists())
                throw new EmbeddedException("no_such_file", f);
        }
    }

    private void validateFilesystem() throws EmbeddedException {
        if(efs == null) {
            efs = new EmbeddedFileSystem();
        }
        efs.initialize();
    }

    private void validateLogging() throws EmbeddedException {
        if(logging) {
            LoggerHelper.info("log_msg");
            LoggerHelper.stopConsoleLogging();
            LoggerHelper.setLogFile(efs.getLogFile().getPath());
        }
    }
    
    private void validatePort() throws EmbeddedException {
        if(httpPort < MIN_PORT || httpPort > MAX_PORT)
            throw new EmbeddedException("bad_port", MIN_PORT, MAX_PORT, httpPort);
        // todo TODO
        // todo TODO
        // TODO todo Here is where we can see if the port is in use and assign another
        // todo TODO
        // todo TODO
    }

    private boolean isDeployable() {
        // is ther at least one deployable item?
        return 
                archives.size() > 0 || 
                readableArchives.size() > 0 || 
                scatteredWars.size() > 0;
    }

    //////////////////////   private variables  ////////////////////////////////
    
}
