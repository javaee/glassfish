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

import org.jvnet.libpam.impl.PAMLibrary.*;
import static org.jvnet.libpam.impl.PAMLibrary.*;
import org.jvnet.libpam.impl.PAMLibrary.pam_conv.PamCallback;
import org.jvnet.libpam.impl.CLibrary.Passwd;
import static org.jvnet.libpam.impl.CLibrary.libc;
import com.sun.jna.Pointer;
import static com.sun.jna.Native.POINTER_SIZE;
import com.sun.jna.ptr.PointerByReference;
import java.util.Set;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * PAM authenticator.
 *
 * <p>
 * Instances are thread unsafe and non reentrant. An instace cannot be reused
 * to authenticate multiple users.
 *
 * <p>
 * For an overview of PAM programming, refer to the following resources:
 *
 * <ul>
 * <li><a href="http://www.netbsd.org/docs/guide/en/chap-pam.html">NetBSD PAM programming guide</a>
 * <li><a href="http://www.kernel.org/pub/linux/libs/pam/">Linux PAM</a>
 * </ul>
 *
 * @author Kohsuke Kawaguchi
 */
public class PAM {
    private pam_handle_t pht;
    private int ret;

    /**
     * Temporarily stored to pass a value from {@link #authenticate(String, String)}
     * to {@link pam_conv}.
     */
    private String password;

    /**
     * Creates a new authenticator.
     *
     * @param serviceName
     *      PAM service name. This corresponds to the service name that shows up
     *      in the PAM configuration,
     */
    public PAM(String serviceName) throws PAMException {
        pam_conv conv = new pam_conv(new PamCallback() {
            public int callback(int num_msg, Pointer msg, Pointer resp, Pointer _) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("pam_conv num_msg="+num_msg);
                if(password==null)
                    return PAM_CONV_ERR;

                // allocates pam_response[num_msg]. the caller will free this
                Pointer m = libc.calloc(pam_response.SIZE,num_msg);
                resp.setPointer(0,m);

                for( int i=0; i<num_msg; i++ ) {
                    pam_message pm = new pam_message(msg.getPointer(POINTER_SIZE*i));
                    if(LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine(pm.msg_style+":"+pm.msg);
                    if(pm.msg_style==PAM_PROMPT_ECHO_OFF) {
                        pam_response r = new pam_response(m.share(pam_response.SIZE*i));
                        r.setResp(password);
                        r.write(); // write to (*resp)[i]
                    }
                }

                return PAM_SUCCESS;
            }
        });

        PointerByReference phtr = new PointerByReference();
        check(libpam.pam_start(serviceName,null,conv,phtr), "pam_start failed");
        pht = new pam_handle_t(phtr.getValue());
    }

    private void check(int ret, String msg) throws PAMException {
        this.ret = ret;
        if(ret!=0) {
            if(pht!=null)
                throw new PAMException(msg+" : "+libpam.pam_strerror(pht,ret));
            else
                throw new PAMException(msg);
        }
    }

    /**
     * Authenticate the user with a password.
     *
     * @return
     *      Upon a successful authentication, return information about the user.
     * @throws PAMException
     *      If the authentication fails.
     */
    public UnixUser authenticate(String username, String password) throws PAMException {
        this.password = password;
        try {
            check(libpam.pam_set_item(pht,PAM_USER,username),"pam_set_item failed");
            check(libpam.pam_authenticate(pht,0),"pam_authenticate failed");
            check(libpam.pam_setcred(pht,0),"pam_setcred failed");
            // several different error code seem to be used to represent authentication failures
//            check(libpam.pam_acct_mgmt(pht,0),"pam_acct_mgmt failed");

            PointerByReference r = new PointerByReference();
            check(libpam.pam_get_item(pht,PAM_USER,r),"pam_get_item failed");
            String userName = r.getValue().getString(0);
            Passwd pwd = libc.getpwnam(userName);
            if(pwd==null)
                throw new PAMException("Authentication succeeded but no user information is available");
            return new UnixUser(userName,pwd);
        } finally {
            this.password = null;
        }
    }

    /**
     * Returns the groups a user belongs to
     * @param username
     * @return Set of group names
     * @throws PAMException
     * @deprecated
     *      Pointless and ugly convenience method.
     */
    public Set<String> getGroupsOfUser(String username) throws PAMException {
        return new UnixUser(username).getGroups();
    }

    /**
     * After a successful authentication, call this method to obtain the effective user name.
     * This can be different from the user name that you passed to the {@link #authenticate(String, String)}
     * method.
     */

    /**
     * Performs an early disposal of the object, instead of letting this GC-ed.
     * Since PAM may hold on to native resources that don't put pressure on Java GC,
     * doing this is a good idea.
     *
     * <p>
     * This method is called by {@link #finalize()}, too, so it's not required
     * to call this method explicitly, however.
     */
    public void dispose() {
        if(pht!=null) {
            libpam.pam_end(pht,ret);
            pht=null;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    private static final Logger LOGGER = Logger.getLogger(PAM.class.getName());
}
