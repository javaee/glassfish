/*
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
package org.glassfish.api.branding;

import org.jvnet.hk2.annotations.Contract;

/**
 * Contract for branding of product based on a given distribution/OEM
 * 
 * @author Sreenivas Munnangi
 */

@Contract
public interface Branding { 

    /**
     * Returns version
     * example: GlassFish Application Server 10.0-SNAPSHOT
     */ 
    public String getVersion();

    /**
     * Returns full version including build id
     * example: GlassFish Application Server 10.0-SNAPSHOT (build b17)
     */
    public String getFullVersion();

    /**
     * Returns abbreviated version.
     * example: GlassFish10.0
     */
    public String getAbbreviatedVersion();

    /**
     * Returns Major version
     * example: 10
     */ 
    public String getMajorVersion();

    /**
     * Returns Minor version
     * example: 0
     */ 
    public String getMinorVersion();

    /**
     * Returns version prefix
     * example: v
     */ 
    public String getVersionPrefix();

    /**
     * Returns version suffix
     * example: prelude
     */ 
    public String getVersionSuffix();

    /**
     * Returns Build Id
     * example: b17
     */ 
    public String getBuildVersion();

    /**
     * Returns Proper Product Name
     * example: GlassFish Application Server, 
     * could be a longer name than Abbreviated Product Name
     */
    public String getProductName();

    /**
     * Returns Abbreviated Product Name
     * example: GlassFish
     */
    public String getAbbrevProductName();
} 
