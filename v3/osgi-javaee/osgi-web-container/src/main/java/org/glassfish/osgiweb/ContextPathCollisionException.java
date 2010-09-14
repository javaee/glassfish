/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.osgiweb;

import org.glassfish.osgijavaeebase.DeploymentException;

import java.util.Arrays;

/**
 * This exception is thrown when multiple WABs have same Web-ContextPath.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ContextPathCollisionException extends DeploymentException {
    private final String contextPath;
    private final Long[] collidingWabIds;

    /**
     * @param contextPath Context-Path for which collision is detected
     * @param collidingWabIds bundle id of the WABs that have same context path. The last entry denotes the
     * current bundle being deployed
     */
    public ContextPathCollisionException(String contextPath, Long[] collidingWabIds) {
        if (collidingWabIds.length < 2) throw new IllegalArgumentException("At least two WAB ids are needed");
        this.contextPath = contextPath;
        this.collidingWabIds = Arrays.copyOf(collidingWabIds, collidingWabIds.length);
        Arrays.sort(this.collidingWabIds);
    }

    public String getContextPath() {
        return contextPath;
    }

    public long getCurrentWabId() {
        return collidingWabIds[collidingWabIds.length -1];
    }

    public Long[] getExistingWabIds() {
        return Arrays.copyOfRange(collidingWabIds, 0, collidingWabIds.length-1);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("context path [" + contextPath + "] is same for following bundles: [");
        for(int i = 0; i < collidingWabIds.length; i++) {
            sb.append(collidingWabIds[i]);
            if (i != collidingWabIds.length-1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
