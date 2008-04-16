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

package com.sun.appserv.ha.spi;

import java.util.Collection;

/**
 * A class to hold a collection of children AttributeMetadata. This class is
 * used mainly to store a collection of AttributeMetaData that are part of a
 * WebSession. The metadata about the web session itself can be obtained
 * directly from the CompositeMetadata itself, while the metadata of its
 * attributes can be obtained from the individual AttributeMetadata that is part
 * of the collection returned by getEntries().
 */
public final class CompositeMetadata extends Metadata {
    
    private byte[] state;

    private Collection<AttributeMetadata> entries;
    
    private String extraParam;    

    /**
     * Construct a CompositeMetadata object
     * 
     * @param version
     *            The version of the data. A freshly created state has a version ==
     *            0
     * @param lastAccesstime
     *            the last access time of the state. This must be used in
     *            conjunction with getMaxInactiveInterval to determine if the
     *            state is idle enough to be removed.
     * @param maxInactiveInterval
     *            the maximum time that this state can be idle in the store
     *            before it can be removed.
     * @param state
     *            The (trunk) state
     * @param extraParam
     *            Some more data
     * @param entries
     *            the AttributeMetadata that are part of this Metadata
     */
    public CompositeMetadata(long version, long lastAccessTime,
            long maxInactiveInterval, Collection<AttributeMetadata> entries, byte[] state, String extraParam) {
        super(version, lastAccessTime, maxInactiveInterval);
        this.entries = entries;
        this.state = state;
        this.extraParam = extraParam;
    }   

    /**
     * Returns a collection of Metadata (or its subclass). Note that though it
     * is possible to have a compositeMetadata itself as part of this
     * collection, typically they contain only AttributeMetaData
     * 
     * @return a collection of AttributeMetadata
     */
    public Collection<AttributeMetadata> getEntries() {
        return entries;
    }
    
    /**
     * Get the state of the object that is stored.
     * 
     * @return the state or null
     */
    public byte[] getState() {
        return state;
    }    
    
    /**
     * Get the extra param associated with this metadata
     * 
     * @return the extra param or null
     */
    public String getExtraParam() {
        return extraParam;
    }     

}
