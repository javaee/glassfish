/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import org.glassfish.ha.store.spi.StorableMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * A class to hold a collection of children SessionAttributeMetadata. This class is
 * used mainly to store a collection of AttributeMetaData that are part of a
 * WebSession. The metadata about the web session itself can be obtained
 * directly from the CompositeMetadata itself, while the metadata of its
 * attributes can be obtained from the individual SessionAttributeMetadata that is part
 * of the collection returned by getEntries().
 */
public final class CompositeMetadata implements Storeable {

    private Collection<SessionAttributeMetadata> entries;

    private String stringExtraParam;

    private StorableMap storableMap;

    private long version;

    private long maxInactiveInterval;

    private long lastAccessTime;

    private byte[] state;

    private transient static final int SAVE_ALL = 0;

    private transient static final int SAVE_EP = 1;

    private transient static final int SAVE_TIME_STAMP = 2;

    private transient int saveMode = SAVE_ALL;

    private transient Set<String> _dirtyAttributeNames = new HashSet<String>();

    private transient static Set<String> saveALL = new HashSet<String>();

    private transient static Set<String> saveEP = new HashSet<String>();

    static {
        saveALL.add(ReplicationAttributeNames.STATE);
        saveALL.add(ReplicationAttributeNames.EXTRA_PARAM);
        saveEP.add(ReplicationAttributeNames.EXTRA_PARAM);
    }


    /**
     * Construct a CompositeMetadata object
     *
     * @param version                   The version of the data. A freshly created state has a version ==
     *                                  0
     * @param lastAccessTime            the last access time of the state. This must be used in
     *                                  conjunction with getMaxInactiveInterval to determine if the
     *                                  state is idle enough to be removed.
     * @param maxInactiveInterval       the maximum time that this state can be idle in the store
     *                                  before it can be removed.
     * @param state                     The (trunk) state
     * @param stringExtraParam Some more data
     * @param entries                   the SessionAttributeMetadata that are part of this Metadata
     */
/*
    public CompositeMetadata(long version, long lastAccessTime,
                             long maxInactiveInterval, Collection<SessionAttributeMetadata> entries, byte[] state,
                             String stringExtraParam) {
        this(version, lastAccessTime, maxInactiveInterval, entries, state, stringExtraParam, null);
        saveMode = SAVE_ALL;
    }
*/

    /**
     * Construct a CompositeMetadata object
     *
     * @param version                   The version of the data. A freshly created state has a version ==
     *                                  0
     * @param lastAccessTime            the last access time of the state. This must be used in
     *                                  conjunction with getMaxInactiveInterval to determine if the
     *                                  state is idle enough to be removed.
     * @param maxInactiveInterval       the maximum time that this state can be idle in the store
     *                                  before it can be removed.
     * @param entries                   the SessionAttributeMetadata that are part of this Metadata
     * @param state                     The (trunk) state
     * @param extraParam                Some more data
     * @param stringExtraParam Some extra data. This object must be Serializable
     */
/*
    public CompositeMetadata(long version, long lastAccessTime,
                             long maxInactiveInterval, Collection<SessionAttributeMetadata> entries, byte[] state,
                             String stringExtraParam, E extraParam) {
        super(version, lastAccessTime, maxInactiveInterval, state, extraParam);
        this.entries = entries;
        this.stringExtraParam = stringExtraParam;

        this.storableMap = new SessionAttributesMapImpl(entries);
        saveMode = SAVE_ALL;
    }
*/
    public CompositeMetadata(long version, long lastAccessTime,
                             long maxInactiveInterval, Collection<SessionAttributeMetadata> entries, byte[] state) {

        // super(version, lastAccessTime, maxInactiveInterval, state, extraParam);
        this.entries = entries;
        //this.stringExtraParam = stringExtraParam;

        this.storableMap = new SessionAttributesMapImpl(entries);
        this.version = version;
        this.lastAccessTime = lastAccessTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.state = state;
        saveMode = SAVE_ALL;
    }


    /**
     * Returns a collection of Metadata (or its subclass). Note that though it
     * is possible to have a compositeMetadata itself as part of this
     * collection, typically they contain only AttributeMetaData
     *
     * @return a collection of SessionAttributeMetadata
     */
    public Collection<SessionAttributeMetadata> getEntries() {
        return entries;
    }

    /**
     * Get the container extra param associated with this metadata
     *
     * @return the container extra param or null
     */
    public String getStringExtraParam() {
        return stringExtraParam;
    }

    public Object _getAttributeValue(String attrName) {
        Object result = null;
        if (ReplicationAttributeNames.SESSION_ATTRIBUTES.equals(attrName)) {
            result = getEntries();
/*
        } else if (ReplicationAttributeNames.STRING_EXTRA_PARAM.equals(attrName)) {
            result = getStringExtraParam();
        } else if (ReplicationAttributeNames.EXTRA_PARAM.equals(attrName)) {
            result = getExtraParam();
*/
        } else if (ReplicationAttributeNames.STATE.equals(attrName)) {
            result = getState();
        }

        return result;
    }

    public Set<String> _getDirtyAttributeNames() {
        return _dirtyAttributeNames;
    }

    public byte[] getState() {
        return this.state;
    }

    public long getVersion() {
        return 0L;
    }

    @Override
    public long _storeable_getVersion() {
        return 0;
    }

    @Override
    public void _storeable_setVersion(long version) {

    }

    @Override
    public long _storeable_getLastAccessTime() {
        return 0;
    }

    @Override
    public void _storeable_setLastAccessTime(long version) {

    }

    @Override
    public long _storeable_getMaxIdleTime() {
        return 0;
    }

    @Override
    public void _storeable_setMaxIdleTime(long version) {

    }

    @Override
    public String[] _storeable_getAttributeNames() {
        return new String[0];
    }

    @Override
    public boolean[] _storeable_getDirtyStatus() {
        return new boolean[0];  
    }

    @Override
    public void _storeable_writeState(OutputStream os) throws IOException {
        
    }

    @Override
    public void _storeable_readState(InputStream is) throws IOException {
        
    }


    private static class SessionAttributesMapImpl
            implements StorableMap<String, byte[]> {

        Set<String> newKeys = new HashSet<String>();
        Set<String> modifiedKeys = new HashSet<String>();
        Set<String> deletedKeys = new HashSet<String>();
        Map<String, byte[]> map = new HashMap<String, byte[]>();

        SessionAttributesMapImpl(Collection<SessionAttributeMetadata> attrs) {
            for (SessionAttributeMetadata attr : attrs) {
                map.put(attr.getAttributeName(), attr.getState());
                if (attr.getOperation() == SessionAttributeMetadata.Operation.ADD) {
                    newKeys.add(attr.getAttributeName());
                } else if (attr.getOperation() == SessionAttributeMetadata.Operation.UPDATE) {
                    modifiedKeys.add(attr.getAttributeName());
                } else {
                    deletedKeys.add(attr.getAttributeName());
                }
            }
        }

        public byte[] get(String name) {
            return map.get(name);  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Collection getDeletedKeys() {
            return deletedKeys;
        }

        public Collection getModifiedKeys() {
            return modifiedKeys;
        }

        public Collection getNewKeys() {
            return deletedKeys;
        }
    }

}
