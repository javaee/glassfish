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

import java.util.logging.*;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;


import com.sun.logging.*;

public class SFSBVersionIORInterceptor
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.IORInterceptor {

    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);
    
    private Codec codec;

    public SFSBVersionIORInterceptor(Codec c) {
        codec = c;
    }

    public void destroy() {
    }

    public String name() {
        return "SFSBVersionIORInterceptor";
    }

    // Note: this is called for all remote refs created from this ORB,
    // including EJBs and COSNaming objects.
    public void establish_components(IORInfo iorInfo) {
        try {
            _logger.log(Level.FINE,
                    "SFSBVersionIORInterceptor.establish_components->:");

            // Add OTS tagged components. These are always the same for all EJBs
            SFSBVersionPolicy versionPolicy = null;
            try {
                versionPolicy = (SFSBVersionPolicy) iorInfo
                        .get_effective_policy(POARemoteReferenceFactory.SFSB_VERSION_POLICY_TYPE);
            } catch (INV_POLICY ex) {
                _logger.log(Level.FINE,
                        "SFSBVersionIORInterceptor.establish_components: SFSB_VERSION_POLICY_TYPE not present", ex);
            }
            if (versionPolicy != null) {
                addSFSBVersionComponents(iorInfo);
            }

        } catch (Exception e) {
            _logger.log(Level.WARNING, "Exception in establish_components", e);
        } finally {
            _logger.log(Level.FINE,
                    "SFSBVersionIORInterceptor.establish_components<-:");
        }
    }

    private void addSFSBVersionComponents(IORInfo iorInfo) {
        long versionNumber = 0;

        Any versionAny = ORB.init().create_any();

        versionAny.insert_longlong(versionNumber);

        byte[] versionValue = null;
        try {
            versionValue = codec.encode_value(versionAny);
        } catch (org.omg.IOP.CodecPackage.InvalidTypeForEncoding e) {
            INTERNAL ex = new INTERNAL("InvalidTypeForEncoding " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }

        TaggedComponent sfsbVersionComponent = new TaggedComponent(
                POARemoteReferenceFactory.SFSB_VERSION_POLICY_TYPE,
                versionValue);
        iorInfo.add_ior_component(sfsbVersionComponent);
        _logger.log(Level.FINE,
                "SFSBVersionIORInterceptor.addSFSBVersionComponents added...");
    }

}

// End of file.
