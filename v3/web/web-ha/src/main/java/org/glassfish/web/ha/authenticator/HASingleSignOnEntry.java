/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.web.ha.authenticator;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOnEntry;

import java.io.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Shing Wai Chan
 */
public class HASingleSignOnEntry extends SingleSignOnEntry {
    protected long maxIdleTime;

    protected long version;

    protected JavaEEIOUtils ioUtils;

    // default constructor is required by backing store
    public HASingleSignOnEntry() {
        this(null, null, null, null, null, null, 0, 0, 0, null);
    }

    public HASingleSignOnEntry(Container container, HASingleSignOnEntryMetadata m,
            JavaEEIOUtils ioUtils) {
        this(m.getId(), null, m.getAuthType(),
                m.getUsername(), m.getPassword(), m.getRealmName(),
                m.getLastAccessTime(), m.getMaxIdleTime(), m.getVersion(),
                ioUtils);

        ByteArrayInputStream bais = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(m.getPrincipalBytes());
            bis = new BufferedInputStream(bais);
            ois = ioUtils.createObjectInputStream(bis, true, this.getClass().getClassLoader());
            this.principal = (Principal)ois.readObject();
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch(IOException ex) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch(IOException ex) {
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch(IOException ex) {
                }
            }
        }

        Set<Session> sessionSet = new HashSet<Session>();
        for (HASessionData data: m.getHASessionDataList()) {
            StandardContext context = (StandardContext)container.findChild(data.getContextPath());
            Session session = null;
            try {
                session = context.getManager().findSession(data.getSessionId());
            } catch(IOException ex) {
                throw new IllegalStateException(ex);
            }
            sessionSet.add(session);
        }
        sessions = sessionSet.toArray(new Session[sessionSet.size()]);
    }



    public HASingleSignOnEntry(String id, Principal principal, String authType,
            String username, char[] password, String realmName,
            long lastAccessTime, long maxIdleTime, long version,
            JavaEEIOUtils ioUtils) {
        
        super(id, principal, authType, username, password, realmName);
        this.lastAccessTime = lastAccessTime;
        this.maxIdleTime = maxIdleTime;
        this.version = version;
        this.ioUtils = ioUtils;
    }

    public HASingleSignOnEntryMetadata getMetadata() {
        List<HASessionData> sessionDataList = new ArrayList<HASessionData>();
        for (Session session: sessions) {
            sessionDataList.add(new HASessionData(session.getId(),
                        session.getManager().getContainer().getName()));
        }

        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(bos);
            oos = ioUtils.createObjectOutputStream(baos, true);
            oos.writeObject(principal);
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch(Exception ex) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch(Exception ex) {
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch(Exception ex) {
                }
            }
        }
        return new HASingleSignOnEntryMetadata(id, baos.toByteArray(), authType,
                username, password, realmName, sessionDataList,
                lastAccessTime, maxIdleTime, version);
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public long getVersion() {
        return version;
    }
}
