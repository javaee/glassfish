/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class Constants {
    /**
     * Indicates if OBR is enabled or not.
     */
    public static final String OBR_ENABLED = "com.sun.enterprise.hk2.obrEnabled";
    /**
     * This property is used to decide if OBR repository should be synchronously initialized.
     */
    static final String INITIALIZE_OBR_SYNCHRONOUSLY = "com.sun.enterprise.hk2.initializeRepoSynchronously";

    /**
     * File name used to store generated OBR repository information.
     */
    static final String REPOSITORY_XML_FILE_NAME = "repository.xml";

    /**
     * URL scheme used by OBR to deploy bundles.
     */
    static final String OBR_SCHEME = "obr:";

    /**
     * No. of milliseconds a thread waits for obtaining a reference to repository admin service before timing out.
     */
    static final long OBR_TIMEOUT = 10000; // in ms

    /**
     * List of URIs of OBR repositories that are configured to be consulted while deploying bundles.
     * The URIs can be URIs of repository xml file or they can point to directories. If they represent
     * directories, then we build the repository.xml ourselves.
     */
    public static final String OBR_REPOSITORIES = "com.sun.enterprise.hk2.obrRepositories";

    /**
     * List of HK2 module repository URIs. Currently, we only support directory URIs.
     */
    public static final String HK2_REPOSITORIES = "com.sun.enterprise.hk2.repositories";
}
