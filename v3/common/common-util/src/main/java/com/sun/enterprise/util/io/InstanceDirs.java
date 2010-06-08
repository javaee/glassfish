/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.util.io;

import java.io.File;
import java.io.IOException;

/**
 * A class for keeping track of the directories that an instance lives in and under.
 * All the methods throw checked exception to avoid the inevitable NPE otherwise - 
 * when working with invalid directories...
 *
 * Example:
 * new InstanceDirs(new File("/glassfishv3/glassfish/nodeagents/mymachine/instance1"));
 *
 * getInstanceDir()   == /glassfishv3/glassfish/nodeagents/mymachine/instance1
 * getNodeAgentDir()  == /glassfishv3/glassfish/nodeagents/mymachine
 * getNodeAgentsDir() == /glassfishv3/glassfish/nodeagents
 * getInstanceName()  == instance1
 *
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public final class InstanceDirs {
    /**
     * This constructor is used when the instance dir is known
     *
     * @param instanceDir The instance's directory
     * @throws IOException If any error including not having a grandparent directory.
     */
    public InstanceDirs(File theInstanceDir) throws IOException {
        dirs = new ServerDirs(theInstanceDir);

        if(dirs.getServerGrandParentDir() == null) {
            throw new IOException(ServerDirs.strings.get("InstanceDirs.noGrandParent", dirs.getServerDir()));
        }
    }

    /**
     * Create a InstanceDir from the more general ServerDirs instance.
     * along with getServerDirs() you can convert freely back and forth
     *
     * @param aServerDir
     */
    public InstanceDirs(ServerDirs sd) {
        dirs = sd;
    }

    public final String getInstanceName() {
        return dirs.getServerName();
    }

    public final File getInstanceDir() {
        return dirs.getServerDir();
    }

    public final File getNodeAgentDir() {
        return dirs.getServerParentDir();
    }

    public final File getNodeAgentsDir() {
        return dirs.getServerGrandParentDir();
    }

    public final ServerDirs getServerDirs() {
        return dirs;
    }

    public final File getDasPropertiesFile() {
        return dirs.getDasPropertiesFile();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private final ServerDirs dirs;
}
