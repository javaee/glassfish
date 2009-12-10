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
package org.glassfish.internal.api;

import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;

/**
 * This interface acts as a facade
 * for generating all non portable
 * jax-rpc artifacts for a single .ear or standalone module
 * It will be used for webservice clients
 *
 * @author Bhakti Mehta
 *
 */
@Contract
public interface JAXRPCCodeGenFacade {

     public void run(Habitat habitat, DeploymentContext context, String cp) throws Exception ;


}
