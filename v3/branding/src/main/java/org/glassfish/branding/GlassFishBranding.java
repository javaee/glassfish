/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.branding;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import org.glassfish.api.branding.Branding;

/**
 * Branding and Version information based on given distribution/OEM
 * 
 * @author Sreenivas Munnangi
 */

@Service
@Scoped(Singleton.class) 
public class GlassFishBranding implements Branding { 

    /**
    * version strings populated during build
    */
    private final String product_name          = "GlassFish Prelude";
    private final String abbrev_product_name   = "GlassFish";
    private final String full_version          = "10.0-SNAPSHOT";
    private final String major_version         = "10";
    private final String minor_version         = "0"; 
    private final String build_id              = "sreeni-private";

    /**
     * Returns version
     */ 
    public String getVersion() {
        return product_name + " " + full_version;
    }

    /**
     * Returns full version including build id
     */
    public String getFullVersion() {
        return (getVersion() + " (build " + build_id + ")");
    }

    /**
     * Returns abbreviated version.
     */
    public String getAbbreviatedVersion() {
        return abbrev_product_name + major_version +
               "." + minor_version;
    }

    /**
     * Returns Major version
     */ 
    public String getMajorVersion() {
    	return major_version;
    }

    /**
     * Returns Minor version
     */ 
    public String getMinorVersion() {
    	return minor_version;
    }

    /**
     * Returns Build version
     */ 
    public String getBuildVersion() {
    	return build_id;
    }

    /**
     * Returns Proper Product Name
     */
    public String getProductName() {
    	return product_name;
    }

    /**
     * Returns Abbreviated Product Name
     */
    public String getAbbrevProductName() {
    	return abbrev_product_name;
    }
} 
