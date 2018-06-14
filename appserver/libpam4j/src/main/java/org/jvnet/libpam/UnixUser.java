/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.jvnet.libpam;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import static org.jvnet.libpam.impl.CLibrary.libc;
import org.jvnet.libpam.impl.CLibrary.Passwd;
import org.jvnet.libpam.impl.CLibrary.Group;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Unix user. Immutable.
 *
 * @author Kohsuke Kawaguchi
 */
public class UnixUser {
    private final String userName, gecos, dir, shell;
    private final int uid,gid;
    private final Set<String> groups;

    /*package*/ UnixUser(String userName, Passwd pwd) throws PAMException {
        this.userName = userName;
        this.gecos = pwd.getPwGecos();
        this.dir = pwd.getPwDir();
        this.shell = pwd.getPwShell();
        this.uid = pwd.getPwUid();
        this.gid = pwd.getPwGid();

        long sz = 4; /*sizeof(gid_t)*/

        int ngroups = 64;
        Memory m = new Memory(ngroups*sz);
        IntByReference pngroups = new IntByReference(ngroups);
        try {
            if(libc.getgrouplist(userName,pwd.getPwGid(),m,pngroups)<0) {
                // allocate a bigger memory
                m = new Memory(pngroups.getValue()*sz);
                if(libc.getgrouplist(userName,pwd.getPwGid(),m,pngroups)<0)
                    // shouldn't happen, but just in case.
                    throw new PAMException("getgrouplist failed");
            }
            ngroups = pngroups.getValue();
        } catch (LinkageError e) {
            // some platform, notably Solaris, doesn't have the getgrouplist function
            ngroups = libc._getgroupsbymember(userName,m,ngroups,0);
            if (ngroups<0)
                throw new PAMException("_getgroupsbymember failed");
        }

        groups = new HashSet<String>();
        for( int i=0; i<ngroups; i++ ) {
            int gid = m.getInt(i * sz);
            Group grp = libc.getgrgid(gid);
            if( grp == null ) {
            	 continue;
            }
            groups.add(grp.gr_name);
        }
    }

    public UnixUser(String userName) throws PAMException {
        this(userName, Passwd.loadPasswd(userName));
    }

    /**
     * Copy constructor for mocking. Not intended for regular use. Only for testing.
     * This signature may change in the future.
     */
    protected UnixUser(String userName, String gecos, String dir, String shell, int uid, int gid, Set<String> groups) {
        this.userName = userName;
        this.gecos = gecos;
        this.dir = dir;
        this.shell = shell;
        this.uid = uid;
        this.gid = gid;
        this.groups = groups;
    }

    /**
     * Gets the unix account name. Never null.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the UID of this user.
     */
    public int getUID() {
        return uid;
    }

    /**
     * Gets the GID of this user.
     */
    public int getGID() {
        return gid;
    }

    /**
     * Gets the gecos (the real name) of this user.
     */
    public String getGecos() {
        return gecos;
    }

    /**
     * Gets the home directory of this user.
     */
    public String getDir() {
        return dir;
    }

    /**
     * Gets the shell of this user.
     */
    public String getShell() {
        return shell;
    }

    /**
     * Gets the groups that this user belongs to.
     *
     * @return
     *      never null.
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public static boolean exists(String name) {
        return libc.getpwnam(name)!=null;
    }
}
