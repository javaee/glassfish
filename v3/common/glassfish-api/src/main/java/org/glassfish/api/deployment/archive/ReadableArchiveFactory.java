/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.deployment.archive;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.jvnet.hk2.annotations.Contract;

import java.net.URI;

/**
 * @author Vivek Pandey
 *
 * ReadableArchiveFactory implementation should be packaged as a HK2 service, This factory implementation, when present
 * should be asked for a ReadableArchive during the deployment phase. For example, the DeployCommand can ask for
 * ReadableArchive from this factory from each of the v3 modules.
 *
 */
@Contract
public interface ReadableArchiveFactory {
    /**
     * Gives a ReadableArchive.
     *
     * @param archivePath Path to the application
     * @return returns null if it can not create archive
     */
    ReadableArchive open(URI archivePath, DeployCommandParameters commandProperties);
}
