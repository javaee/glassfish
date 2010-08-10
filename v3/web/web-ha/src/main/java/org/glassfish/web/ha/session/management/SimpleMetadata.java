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


package org.glassfish.web.ha.session.management;

import org.glassfish.ha.store.api.Storeable;
import org.glassfish.ha.store.spi.Storable;

import java.io.*;
import java.util.Set;


public class SimpleMetadata  implements Storeable {
    
    private long version = -1;

    private long lastAccessTime;

    private long maxInactiveInterval;
    
    private byte[] state;

    // private HttpSessionExtraParams extraParam;

    //Default No arg constructor required for BackingStore

    public SimpleMetadata() {

    }

  /**
     * Construct a SimpleMetadata object
     *
     * @param version The version of the data. A freshly created state has a version == 0
     * 
     * @param lastAccesstime
     *            the last access time of the state. This must be used in
     *            conjunction with getMaxInactiveInterval to determine if the
     *            state is idle enough to be removed.
     * @param maxInactiveInterval
     *            the maximum time that this state can be idle in the store
     *            before it can be removed.
     */
//    public SimpleMetadata(long version, long lastAccesstime,
  //                              long maxInactiveInterval, byte[] state, HttpSessionExtraParams extraParam)
    public SimpleMetadata(long version, long lastAccesstime,
                                long maxInactiveInterval, byte[] state)

    {
        this.version = version;
        this.lastAccessTime = lastAccesstime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.state = state;
    }

    public SimpleMetadata(long version, long lastAccesstime) {
        this.version = version;
        this.lastAccessTime = lastAccessTime;
    }

  /**
     * Get the verion of the state. A freshly created state has a version == 0
     *
     * @return the version.
     */
    public long getVersion() {
        return version;
    }

  /**
     * Get the last access time of the state. This must be used in conjunction
     * with getMaxInactiveInterval to determine if the state is idle enough to
     * be removed.
     *
     * @return The time when the state was accessed last
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

  /**
     * Get the maximum time that this state can be idle in the store before it
     * can be removed.
     *
     * @return the maximum idle time. If zero or negative, then the component
     *         has no idle timeout limit
     */
    public long getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    public byte[] getState() {
        return this.state;
    }

    @Override
    public long _storeable_getVersion() {
        return version;
    }

    @Override
    public void _storeable_setVersion(long version) {
        this.version = version;
    }

    @Override
    public long _storeable_getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public void _storeable_setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public long _storeable_getMaxIdleTime() {
        return maxInactiveInterval;
    }

    @Override
    public void _storeable_setMaxIdleTime(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public String[] _storeable_getAttributeNames() {
        return new String[0];  //FIXME later
    }

    @Override
    public boolean[] _storeable_getDirtyStatus() {
        return new boolean[0];  //FIXME later
    }

    @Override
    public void _storeable_writeState(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        try {
            oos.writeLong(version);
            oos.writeLong(lastAccessTime);
            oos.writeLong(maxInactiveInterval);
            oos.writeInt(state == null ? 0 : state.length);
            if (state != null) {
                oos.write(state);
            }
        } finally {
            try { oos.flush();
                oos.close();
            } catch (Exception ex) {}
        }
    }

    @Override
    public void _storeable_readState(InputStream is) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(is);
        try {
            long tempVersion = ois.readLong();
            if (tempVersion > version) {
                version = tempVersion;
                lastAccessTime = ois.readLong();
                maxInactiveInterval = ois.readLong();
                int len = ois.readInt();
                if (len > 0) {
                    state = new byte[len];
                    ois.read(state);
                }
            }
        } finally {
            try { ois.close();
            } catch (Exception ex) {}
        }
    }
}
