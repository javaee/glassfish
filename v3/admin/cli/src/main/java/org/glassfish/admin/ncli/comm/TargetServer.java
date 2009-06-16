/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *   Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 *   The contents of this file are subject to the terms of either the GNU
 *   General Public License Version 2 only ("GPL") or the Common Development
 *   and Distribution License("CDDL") (collectively, the "License").  You
 *   may not use this file except in compliance with the License. You can obtain
 *   a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *   or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *   language governing permissions and limitations under the License.
 *
 *   When distributing the software, include this License Header Notice in each
 *   file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *   Sun designates this particular file as subject to the "Classpath" exception
 *   as provided by Sun in the GPL Version 2 section of the License file that
 *   accompanied this code.  If applicable, add the following below the License
 *   Header, with the fields enclosed by brackets [] replaced by your own
 *   identifying information: "Portions Copyrighted [year]
 *   [name of copyright owner]"
 *
 *   Contributor(s):
 *
 *   If you wish your version of this file to be governed by only the CDDL or
 *   only the GPL Version 2, indicate your decision by adding "[Contributor]
 *   elects to include this software in this distribution under the [CDDL or GPL
 *   Version 2] license."  If you don't indicate a single choice of license, a
 *   recipient has the option to distribute your version of this file under
 *   either the CDDL, the GPL Version 2 or to extend the choice of license to
 *   its licensees as provided above.  However, if you add GPL Version 2 code
 *   and therefore, elected the GPL Version 2 license, then the option applies
 *   only if the new code is made subject to such option by the copyright
 *   holder.
 */

package org.glassfish.admin.ncli.comm;

import static org.glassfish.admin.ncli.Constants.*;

/** Identifies a target administration server where the actual asadmin command is implemented. In the present
 *  scheme, a target server is a 5-tuple of <code> host name, port number, admin user name, admin password and
 *  a flag indicating whether admin server is secure </code>. This 5-tuple completely specifies the server as
 *  far as asadmin client is concerned. The password here is a clear-text password.
 *  <p>
 *  Instances of this class are immutable.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public final class TargetServer {

    private final String host;
    private final int    port;
    private final String user;
    private final String password;
    private final boolean secure;
    
    public TargetServer(String host, int port, String user, String password, boolean secure) {
        if (host == null)  //user and password may be null
            throw new IllegalArgumentException("host can't be null");
        this.host     = host;
        this.port     = port;
        this.user     = user;
        this.password = password;
        this.secure   = secure;
    }

    public TargetServer(String host, int port, String user, String password) {
        this(host, port, user, password, false);
    }

    public TargetServer(String host, int port) {
        this(host, port, DEFAULT_USER, null);
    }

    public TargetServer() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_USER, null);
    }

    @Override
    public boolean equals(Object ts) {
        if(ts instanceof TargetServer) {
            TargetServer that = (TargetServer) ts;
            boolean nonNullSame = this.host.equals(that.host) &&
                                  this.port == that.port      &&
                                  this.secure == that.secure;
            boolean userSame = this.isUserSame(that);
            boolean passwordSame = this.isPasswordSame(that);
            return (nonNullSame && userSame && passwordSame);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hc = this.secure ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode(); //this is either 1231 or 1237
        hc = 11 * this.host.hashCode() + 13 * port +  17 * hc;
        hc = this.user == null ? hc : hc + 19 * this.user.hashCode();
        hc = this.password == null ? hc : hc + 23 * this.password.hashCode();

        return hc;
    }
    
    // ALL Private ...

    /** The equivalence operator for user field.
     *  @param that other TargetServer
     *  @return true if the user field is same in this and that TargetServer instances, false otherwise. If both are null,
     *  they are treated as same.
     * */
    private boolean isUserSame(TargetServer that) {
        boolean userSame;
        if (this.user == null && that.user == null)
            userSame = true;
        else if ((this.user != null && that.user == null) ||
                 (this.user == null && that.user != null))
            userSame = false;
        else 
            userSame = this.user.equals(that.user);
        return userSame;
    }
    /** The equivalence operator for password field.
     *  @param that other TargetServer
     *  @return true if the user field is same in this and that TargetServer instances, false otherwise. If both are null,
     *  they are treated as same.
     * */    private boolean isPasswordSame(TargetServer that) {
        boolean passwordSame;
        if (this.password == null && that.password == null)
            passwordSame = true;
        else if ((this.password != null && that.password == null) ||
                 (this.password == null && that.password != null))
            passwordSame = false;
        else 
            passwordSame = this.password.equals(that.password);
        return passwordSame;
    }    
}
