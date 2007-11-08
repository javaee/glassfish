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
package com.sun.enterprise.admin.pluggable;

import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.diagnostics.DiagnosticAgent;
/**
 * The interface to provide access to supported Pluggable Features implemented 
 * purely on the client side (asadmin). The server side equivalent of this 
 * class is com.sun.enterprise.server.pluggable.PluggableFeatureFactors
 * and is not available to asadmin.
 * 
 * Various editions of the product may provide varied implementation for
 * same feature. This interface has different implementation for every
 * product edition. Every method in this interface provides access to
 * implementation of a pluggable feature. The pattern for this class is
 * to define getter methods for every pluggable feature, where a pluggable
 * feature is a java interface.
 */
public interface ClientPluggableFeatureFactory {

    /**
     * Name of the property used to define pluggable features list.
     */
    public static final String PLUGGABLE_FEATURES_PROPERTY_NAME =
            "com.sun.appserv.admin.pluggable.features";

    /**
     * Get access to the DomainsManager which is used to create a 
     * domain from templates.
     */
    public DomainsManager getDomainsManager();
    
    public DiagnosticAgent getDiagnosticAgent();
}
