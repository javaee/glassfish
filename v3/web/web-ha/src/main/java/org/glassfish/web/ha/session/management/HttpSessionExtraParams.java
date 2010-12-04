/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/*
 * HttpSessionExtraParams.java
 *
 * Created on November 26, 2007, 5:56 PM
 *
 */

package org.glassfish.web.ha.session.management;


import org.glassfish.ha.store.spi.StoreEntryEvaluator;
import org.glassfish.ha.store.util.SimpleMetadata;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author Larry White
 */
public class HttpSessionExtraParams
        implements StoreEntryEvaluator<String, SimpleMetadata>, Serializable {


    /**
     * The logger to use for logging ALL web container related messages.
     */


    // Must not access directly, call getter instead
    private transient String currentOwnerInstanceName;
    
    // Must not access directly, call getter instead
    private transient String ssoId;    
    
    // The associated HttpSession
    private transient final BaseHASession session;
    
    // The id of the associated HttpSession
    private transient String id;
    
    private transient int hc;

    private transient ReplicationManagerBase rmb;

    private enum Mode {NON_QUERY_MODE, FIND_BY_OWNER};

    private Mode mode = Mode.NON_QUERY_MODE;    
    
    /** Creates a new instance of HttpSessionExtraParams */
    public HttpSessionExtraParams(BaseHASession session) {
        this.session = session;
        if(session != null) {
            this.id = session.getId();
            this.hc = id.hashCode();
        }
    }
    

    public String getId() {
        return id;
    }

    public HttpSessionExtraParams eval(String key, SimpleMetadata simpleMetadata) {
//        HttpSessionExtraParams ep = (HttpSessionExtraParams) simpleMetadata.getExtraParam();
//        return (this.equals(ep)) ? ep : null;
        throw (new RuntimeException("Not yet Implemented"));
    }


    private void setId(String id) {
        this.id = id;
        hc = id.hashCode();
    }

    public String getParentSasId() {
        throw new RuntimeException("Not yet implemented");
    }


    /** the ssoid */
    public String getSsoId() {
        if (session != null) {
            return session.getSsoId();
        } else {
            return ssoId;
        }
    }
    
    public boolean equals(Object obj) {
/*
         switch (this.mode) {
         case FIND_BY_OWNER:
            //Search mode
            if (obj instanceof HttpSessionExtraParams) {
                HttpSessionExtraParams that = (HttpSessionExtraParams) obj;
                return getCurrentOwnerInstanceName().equals(that.currentOwnerInstanceName);
            } else {
                return false;
            }
         default:
            if (obj == this) {
                return true;
            }    
            if (! (obj instanceof HttpSessionExtraParams)) {
                return false;
            }
            HttpSessionExtraParams that = (HttpSessionExtraParams) obj;
            return this.id.equals(that.id);
        }
*/
        throw new RuntimeException("Not yet implemented");
    }

    public int hashCode() {
        return this.hc;
    }
    
    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        oos.writeObject(id);
        oos.writeObject(getSsoId());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        this.id = (String) in.readObject();
        this.ssoId = (String) in.readObject();
        this.hc = id.hashCode();

    }


}
