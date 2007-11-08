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

package com.sun.enterprise.iiop;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.ejb.base.sfsb.util.SimpleKeyGenerator;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.util.Utility;

import com.sun.logging.LogDomains;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

public class EJBTargetKeyInfo {
    
    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    public static final int EJBID_OFFSET = 0;
    public static final int INSTANCEKEYLEN_OFFSET = 8;
    public static final int INSTANCEKEY_OFFSET = 12;
    public static final byte HOME_KEY = (byte)0xff;

    private byte[] oid;
    private long containerId;
    private int keyLength;
    private boolean local;
    private Object instanceKey;
    
    private boolean validOid = false;
    
    private SimpleKeyGenerator gen = new SimpleKeyGenerator();
    
    public EJBTargetKeyInfo(byte[] oid, boolean local) {
        this.oid = oid;
        this.local = local;
        
        this.instanceKey = gen.byteArrayToKey(oid, INSTANCEKEY_OFFSET, 20);
        validate();
    }
    
    public EJBTargetKeyInfo(org.omg.CORBA.Object effective_target) {
        try {
            if (StubAdapter.isStub(effective_target)) {
                if (StubAdapter.isLocal(effective_target)) {
                    this.local = true;
                }
                IOR ior = ((com.sun.corba.ee.spi.orb.ORB) ORBManager.getORB())
                    .getIOR(effective_target, false);
                java.util.Iterator iter = ior.iterator();

                if (iter.hasNext()) {
                    TaggedProfile profile = (TaggedProfile) iter.next();
                    ObjectKey objKey = profile.getObjectKey();
                    this.oid = objKey.getId().getId();
                    
                    SimpleKeyGenerator gen = new SimpleKeyGenerator();
                    this.instanceKey = gen.byteArrayToKey(oid, INSTANCEKEY_OFFSET, 20);
                    
                    validate();
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.FINE,
                    "EJBTargetKeyInfo: Exception during extraction of instance key", ex);
        }
    }
    
    public byte[] getOid() {
        return oid;
    }
    
    public Object getInstanceKey() {
        return instanceKey;
    }
    
    private void validate() {
        if ((oid != null) && (oid.length > INSTANCEKEY_OFFSET)) {
            this.containerId = Utility.bytesToLong(oid, EJBID_OFFSET);
            this.keyLength = Utility.bytesToInt(oid, INSTANCEKEYLEN_OFFSET);
            if (oid.length == keyLength + INSTANCEKEY_OFFSET) {
                validOid = true;
            } else {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "[Invalid.1] oid.length: " + oid.length
                        + "; keyLength: " + keyLength);
                }
            }
        } else {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "[Invalid.2] oid: " + oid + "; "
                    + "oid.length: " + ((oid == null) ? -1 : oid.length));
            }
        }
    }
    
    public boolean isLocal() {
        return this.local;
    }
    public boolean isValid() {
        return this.validOid;
    }
    
    public long getContainerId() {
        return this.containerId;
    }
    
    public boolean isHomeOid() {
        return isValid() && (keyLength == 1) && (oid[INSTANCEKEY_OFFSET] == HOME_KEY);
    }
    
    public int getKeyLength() {
        return this.keyLength;
    }
    
    public static int getInstanceKeyOffset() {
        return INSTANCEKEY_OFFSET;
    }
    
    public String toString() {
        StringBuilder bldr = new StringBuilder(validOid ? "valid" : "invalid");
        bldr.append(" {").append(containerId).append(" : ")
            .append(isHomeOid() ? "HOME" : "EJBO").append(" ==> ")
            .append(instanceKey).append("}");
        return bldr.toString();
    }
    
}
