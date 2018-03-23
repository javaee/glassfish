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

package org.jvnet.libpam.impl;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Native;
import com.sun.jna.Library;
import com.sun.jna.Structure;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;
import org.jvnet.libpam.PAMException;

/**
 * @author Kohsuke Kawaguchi
 */
public interface CLibrary extends Library {
    /**
     * Comparing http://linux.die.net/man/3/getpwnam
     * and my Mac OS X reveals that the structure of this field isn't very portable.
     * In particular, we cannot read the real name reliably.
     */
    public class Passwd extends Structure {
        /**
         * User name.
         */
        public String pw_name;
        /**
         * Encrypted password.
         */
        public String pw_passwd;
        public int pw_uid;
        public int pw_gid;

        // ... there are a lot more fields

        public static Passwd loadPasswd(String userName) throws PAMException {
            Passwd pwd = libc.getpwnam(userName);
            if (pwd == null) {
                throw new PAMException("No user information is available");
            }
            return pwd;
        }

        public String getPwName() {
            return pw_name;
        }

        public String getPwPasswd() {
            return pw_passwd;
        }

        public int getPwUid() {
            return pw_uid;
        }

        public int getPwGid() {
            return pw_gid;
        }

        public String getPwGecos() {
            return null;
        }

        public String getPwDir() {
            return null;
        }

        public String getPwShell() {
            return null;
        }

        protected List getFieldOrder() {
            return Arrays.asList("pw_name", "pw_passwd", "pw_uid", "pw_gid");
        }
        public void setPwName(String pw_name) {
            this.pw_name = pw_name;
        }  

        public void setPwPasswd(String pw_passwd) {
            this.pw_passwd = pw_passwd;
        }

        public void setPwUid(int pw_uid) {
            this.pw_uid = pw_uid;
        }

        public void setPwGid(int pw_gid) {
            this.pw_gid = pw_gid;
        }


    }

    public class Group extends Structure {
        public String gr_name;
        // ... the rest of the field is not interesting for us

        protected List getFieldOrder() {
            return Arrays.asList("gr_name");
        }

       public void setGrName(String gr_name) {
           this.gr_name = gr_name;
       }
    }

    Pointer calloc(int count, int size);
    Pointer strdup(String s);
    Passwd getpwnam(String username);

    /**
     * Lists up group IDs of the given user. On Linux and most BSDs, but not on Solaris.
     * See http://www.gnu.org/software/hello/manual/gnulib/getgrouplist.html
     */
    int getgrouplist(String user, int/*gid_t*/ group, Memory groups, IntByReference ngroups);

    /**
     * getgrouplist equivalent on Solaris.
     * See http://mail.opensolaris.org/pipermail/sparks-discuss/2008-September/000528.html
     */
    int _getgroupsbymember(String user, Memory groups, int maxgids, int numgids);
    Group getgrgid(int/*gid_t*/ gid);
    Group getgrnam(String name);

    // other user/group related functions that are likely useful
    // see http://www.gnu.org/software/libc/manual/html_node/Users-and-Groups.html#Users-and-Groups


    public static final CLibrary libc = Instance.init();

    static class Instance {
        private static CLibrary init() {
            if (Platform.isMac() || Platform.isOpenBSD()) {
                return (CLibrary) Native.loadLibrary("c", BSDCLibrary.class);
            } else if (Platform.isFreeBSD()) {
                return (CLibrary) Native.loadLibrary("c", FreeBSDCLibrary.class);
            } else if (Platform.isSolaris()) {
                return (CLibrary) Native.loadLibrary("c", SolarisCLibrary.class);
            } else if (Platform.isLinux()) {
                return (CLibrary) Native.loadLibrary("c", LinuxCLibrary.class);
            } else {
                return (CLibrary) Native.loadLibrary("c", CLibrary.class);
            }
        }
    }
}
